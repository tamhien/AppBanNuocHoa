package com.example.perfumeshop.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.Perfume
import com.example.perfumeshop.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainScreen(
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToPasswordReset: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Perfume?>(null) }

    // Cập nhật trạng thái session mỗi khi quay lại màn hình này
    LaunchedEffect(Unit) {
        viewModel.updateSession()
    }

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
        Box(modifier = Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> UserHomeScreen(viewModel, onProductClick = { selectedProduct = it })
                1 -> UserFavoritesScreen(viewModel, onProductClick = { selectedProduct = it })
                2 -> UserHistoryScreen(viewModel)
                3 -> UserAccountScreen(
                    viewModel = viewModel,
                    onLogout = onLogout,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToRegister = onNavigateToRegister,
                    onNavigateToPasswordReset = onNavigateToPasswordReset
                )
            }
        }
    }

    // Dialog Chi tiết sản phẩm
    selectedProduct?.let { perfume ->
        ProductDetailDialog(
            perfume = perfume,
            onDismiss = { selectedProduct = null },
            onAddToCart = { qty ->
                // Xử lý thêm vào giỏ hàng ở đây
                selectedProduct = null
            }
        )
    }
}

@Composable
fun ProductDetailDialog(
    perfume: Perfume,
    onDismiss: () -> Unit,
    onAddToCart: (Int) -> Unit
) {
    var quantity by remember { mutableIntStateOf(1) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = { onAddToCart(quantity) }) {
                Text("Thêm vào giỏ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
        title = { Text(perfume.name, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = RetrofitClient.getFullImageUrl(perfume.imageUrl),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Thương hiệu: ${perfume.brand}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Giá: ${perfume.price} $", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                
                Row(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(text = "Còn lại: ${perfume.stockQuantity}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                    Text(text = "Đã bán: ${perfume.soldCount}", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(text = perfume.description, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    IconButton(onClick = { if (quantity > 1) quantity-- }) {
                        Icon(Icons.Default.Remove, null)
                    }
                    Text(text = quantity.toString(), modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = { quantity++ }) {
                        Icon(Icons.Default.Add, null)
                    }
                }
            }
        }
    )
}
