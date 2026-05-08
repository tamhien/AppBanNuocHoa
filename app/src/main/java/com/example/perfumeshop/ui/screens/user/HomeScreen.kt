package com.example.perfumeshop.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.Perfume
import com.example.perfumeshop.viewmodel.HomeViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isSearchExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier
                .statusBarsPadding()
                .background(MaterialTheme.colorScheme.surface)) {
                TopAppBar(
                    title = {
                        if (isSearchExpanded) {
                            TextField(
                                value = viewModel.searchQuery,
                                onValueChange = { viewModel.onSearchQueryChange(it) },
                                placeholder = { Text("Tìm tên, thương hiệu, giá...") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                )
                            )
                        } else {
                            Text("PrincePhom Shop", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                        }
                    },
                    actions = {
                        IconButton(onClick = { 
                            isSearchExpanded = !isSearchExpanded 
                            if (!isSearchExpanded) viewModel.onSearchQueryChange("")
                        }) {
                            Icon(if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search, null)
                        }
                        Box(modifier = Modifier.padding(end = 8.dp)) {
                            IconButton(onClick = { /* Chuyển đến Giỏ hàng */ }) {
                                Icon(Icons.Outlined.ShoppingCart, null)
                            }
                            Surface(
                                modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp),
                                color = Color.Red,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("0", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 5.dp))
                            }
                        }
                    }
                )
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, null) },
                    label = { Text("Trang chủ") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Favorite, null) },
                    label = { Text("Yêu thích") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.History, null) },
                    label = { Text("Lịch sử") }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Tài khoản") }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            CategoryTabs(
                selectedCategory = viewModel.currentGender,
                onCategorySelected = { viewModel.fetchPerfumes(it) }
            )

            if (viewModel.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.errorMessage != null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(viewModel.errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            } else {
                ProductGrid(
                    perfumes = viewModel.filteredPerfumes,
                    favorites = viewModel.favorites,
                    onFavoriteToggle = { viewModel.toggleFavorite(it) }
                )
            }
        }
    }
}

@Composable
fun CategoryTabs(selectedCategory: String?, onCategorySelected: (String?) -> Unit) {
    val categories = listOf(
        "Tất cả" to null, 
        "Nước hoa nam" to "Men", 
        "Nước hoa nữ" to "Women",
        "Unisex" to "Unisex"
    )
    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(categories) { (label, value) ->
            FilterChip(
                selected = selectedCategory == value,
                onClick = { onCategorySelected(value) },
                label = { Text(label) },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun ProductGrid(perfumes: List<Perfume>, favorites: Set<Int>, onFavoriteToggle: (Int) -> Unit) {
    if (perfumes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy sản phẩm nào")
        }
    } else {
        LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(perfumes) { perfume ->
                ProductItem(perfume, favorites.contains(perfume.id), onFavoriteToggle = { onFavoriteToggle(perfume.id) })
            }
        }
    }
}

@Composable
fun ProductItem(perfume: Perfume, isFavorite: Boolean, onFavoriteToggle: () -> Unit) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent), modifier = Modifier.fillMaxWidth()) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFFF0F0F0))) {
                AsyncImage(
                    model = RetrofitClient.getFullImageUrl(perfume.imageUrl),
                    contentDescription = null, 
                    modifier = Modifier.fillMaxSize(), 
                    contentScale = ContentScale.Crop
                )
                
                Surface(color = Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(4.dp), modifier = Modifier.padding(8.dp)) {
                    val genderLabel = when(perfume.gender) {
                        "Men" -> "Nam"
                        "Women" -> "Nữ"
                        else -> "Unisex"
                    }
                    Text(text = genderLabel, color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }

                IconButton(onClick = onFavoriteToggle, modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)) {
                    Icon(imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, contentDescription = null, tint = if (isFavorite) Color.Red else Color.Gray)
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(text = perfume.name.uppercase(), fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(text = perfume.brand, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(text = "${perfume.price} $", color = Color.DarkGray, fontSize = 14.sp)
        }
    }
}
