package com.example.perfumeshop.ui.screens.user

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    
    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            viewModel.clearError()
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        // Banner Section
        HomeHeroSection()

        HomeCategoryTabs(
            selectedCategory = viewModel.currentGender,
            onCategorySelected = { gender -> viewModel.fetchPerfumes(gender) }
        )

        if (viewModel.isLoading && viewModel.filteredPerfumes.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
fun HomeHeroSection() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Placeholder cho ảnh banner, dùng màu gradient premium
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF2C3E50), Color(0xFF000000))
                        )
                    )
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "BỘ SƯU TẬP MỚI", 
                    color = Color.White.copy(alpha = 0.7f), 
                    fontSize = 12.sp, 
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    "Hương Thơm Đẳng Cấp", 
                    color = Color.White, 
                    fontSize = 20.sp, 
                    fontWeight = FontWeight.Black
                )
                Spacer(Modifier.height(8.dp))
                Surface(
                    color = Color.White,
                    shape = RoundedCornerShape(8.dp),
                    onClick = { /* Navigate to collection */ }
                ) {
                    Text(
                        "KHÁM PHÁ NGAY", 
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun HomeCategoryTabs(selectedCategory: String?, onCategorySelected: (String?) -> Unit) {
    val categories = listOf(
        "Tất cả" to null, 
        "Nam" to "Men", 
        "Nữ" to "Women",
        "Unisex" to "Unisex"
    )
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp), 
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(categories) { (label, value) ->
            val isSelected = selectedCategory == value
            Surface(
                selected = isSelected,
                onClick = { onCategorySelected(value) },
                shape = RoundedCornerShape(12.dp),
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White,
                border = if (isSelected) null else BorderStroke(1.dp, Color(0xFFEEEEEE)),
                tonalElevation = if (isSelected) 4.dp else 0.dp
            ) {
                Text(
                    label, 
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else Color.DarkGray
                )
            }
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
        Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Text("Chúng tôi đang cập nhật sản phẩm...", color = Color.Gray)
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2), 
            contentPadding = PaddingValues(16.dp), 
            horizontalArrangement = Arrangement.spacedBy(16.dp), 
            verticalArrangement = Arrangement.spacedBy(20.dp)
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
    val isOutOfStock = (perfume.stockQuantity ?: 0) <= 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Card(
            shape = RoundedCornerShape(20.dp), 
            colors = CardDefaults.cardColors(containerColor = Color.White), 
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, Color(0xFFF0F0F0))
        ) {
            Box(modifier = Modifier.fillMaxWidth().height(190.dp)) {
                AsyncImage(
                    model = RetrofitClient.getFullImageUrl(perfume.imageUrl),
                    contentDescription = null, 
                    modifier = Modifier.fillMaxSize(), 
                    contentScale = ContentScale.Crop
                )
                
                // Gender Tag
                Surface(
                    color = Color.White.copy(alpha = 0.9f), 
                    shape = RoundedCornerShape(bottomEnd = 12.dp), 
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    val genderLabel = when(perfume.gender) {
                        "Men" -> "MEN"
                        "Women" -> "WOMEN"
                        else -> "UNISEX"
                    }
                    Text(
                        text = genderLabel, 
                        color = Color.Black, 
                        fontSize = 9.sp, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Favorite Button
                IconButton(
                    onClick = onFavoriteToggle, 
                    modifier = Modifier.align(Alignment.TopEnd).padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, 
                        contentDescription = null, 
                        tint = if (isFavorite) Color.Red else Color.Black.copy(alpha = 0.4f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (isOutOfStock) {
                    Box(
                        modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "HẾT HÀNG", 
                            color = Color.Black, 
                            fontWeight = FontWeight.Black, 
                            fontSize = 12.sp,
                            modifier = Modifier.background(Color.White, RoundedCornerShape(4.dp)).padding(4.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(Modifier.height(10.dp))
        
        Text(
            text = perfume.brand?.uppercase() ?: "", 
            fontSize = 10.sp, 
            color = MaterialTheme.colorScheme.primary, 
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Text(
            text = perfume.name ?: "", 
            fontWeight = FontWeight.SemiBold, 
            fontSize = 14.sp,
            maxLines = 1, 
            overflow = TextOverflow.Ellipsis,
            color = Color(0xFF2C3E50)
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${String.format(java.util.Locale.US, "%.1f", perfume.price)}$", 
                color = Color.Black, 
                fontSize = 15.sp, 
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(Modifier.weight(1f))
            Icon(Icons.Default.AddCircle, null, tint = Color.Black, modifier = Modifier.size(20.dp))
        }
    }
}
