package com.example.perfumeshop.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.perfumeshop.viewmodel.HomeViewModel

@Composable
fun UserFavoritesScreen(
    viewModel: HomeViewModel,
    onProductClick: (com.example.perfumeshop.model.Perfume) -> Unit
) {
    if (viewModel.favoritePerfumes.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Favorite, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                Text("Danh sách yêu thích trống", style = MaterialTheme.typography.bodyLarge)
            }
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(viewModel.favoritePerfumes) { perfume ->
                HomeProductItem(
                    perfume = perfume,
                    isFavorite = true,
                    onFavoriteToggle = { viewModel.toggleFavorite(perfume.id) },
                    onClick = { onProductClick(perfume) }
                )
            }
        }
    }
}
