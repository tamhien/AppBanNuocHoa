package com.example.perfumeshop.ui.screens.user

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.Perfume
import com.example.perfumeshop.viewmodel.HomeViewModel

@Composable
fun UserHomeScreen(
    viewModel: HomeViewModel,
    onProductClick: (Perfume) -> Unit
) {
    val context = LocalContext.current
    
    // Hiển thị thông báo lỗi bằng Toast thay vì làm mất toàn bộ giao diện
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError() // Xóa lỗi sau khi hiển thị
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        HomeCategoryTabs(
            selectedCategory = viewModel.currentGender,
            onCategorySelected = { gender -> viewModel.fetchPerfumes(gender) }
        )

        if (viewModel.isLoading && viewModel.filteredPerfumes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            HomeProductGrid(
                perfumes = viewModel.filteredPerfumes,
                favorites = viewModel.favoriteIds,
                onFavoriteToggle = { viewModel.toggleFavorite(it) },
                onProductClick = onProductClick
            )
        }
    }
}

@Composable
fun HomeCategoryTabs(selectedCategory: String?, onCategorySelected: (String?) -> Unit) {
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
fun HomeProductGrid(
    perfumes: List<Perfume>, 
    favorites: Set<Int>, 
    onFavoriteToggle: (Int) -> Unit,
    onProductClick: (Perfume) -> Unit
) {
    if (perfumes.isEmpty()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Không tìm thấy sản phẩm nào")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), 
            contentPadding = PaddingValues(16.dp), 
            horizontalArrangement = Arrangement.spacedBy(16.dp), 
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(perfumes) { perfume ->
                HomeProductItem(
                    perfume = perfume, 
                    isFavorite = favorites.contains(perfume.id), 
                    onFavoriteToggle = { onFavoriteToggle(perfume.id) },
                    onClick = { onProductClick(perfume) }
                )
            }
        }
    }
}

@Composable
fun HomeProductItem(
    perfume: Perfume, 
    isFavorite: Boolean, 
    onFavoriteToggle: () -> Unit, 
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp), 
        colors = CardDefaults.cardColors(containerColor = Color.Transparent), 
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
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
            Text(text = "${String.format(java.util.Locale.US, "%.1f", perfume.price)}$", color = Color.DarkGray, fontSize = 14.sp)
        }
    }
}
