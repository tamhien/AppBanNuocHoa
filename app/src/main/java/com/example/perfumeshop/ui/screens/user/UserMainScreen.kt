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
import kotlinx.coroutines.launch
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.Perfume
import com.example.perfumeshop.model.Review
import com.example.perfumeshop.viewmodel.CartViewModel
import com.example.perfumeshop.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserMainScreen(
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToPasswordReset: () -> Unit,
    onNavigateToCart: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var selectedProduct by remember { mutableStateOf<Perfume?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Cập nhật trạng thái session mỗi khi quay lại màn hình này
    LaunchedEffect(Unit) {
        viewModel.updateSession()
        cartViewModel.fetchCart()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                            IconButton(onClick = onNavigateToCart) {
                                Icon(Icons.Outlined.ShoppingCart, null)
                            }
                            if (cartViewModel.cartItems.isNotEmpty()) {
                                Surface(
                                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp),
                                    color = Color.Red,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = cartViewModel.cartItems.size.toString(), 
                                        color = Color.White, 
                                        fontSize = 10.sp, 
                                        modifier = Modifier.padding(horizontal = 5.dp)
                                    )
                                }
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
                0 -> UserHomeScreen(
                    viewModel = viewModel, 
                    onProductClick = { selectedProduct = it }
                )
                1 -> UserFavoritesScreen(
                    viewModel = viewModel, 
                    onProductClick = { selectedProduct = it }
                )
                2 -> UserHistoryScreen()
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
                cartViewModel.addToCart(perfume.id, qty) { success, message ->
                    selectedProduct = null
                    // Hiển thị thông báo
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
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
    val isOutOfStock = (perfume.stockQuantity ?: 0) <= 0
    
    var reviews by remember { mutableStateOf<List<Review>>(emptyList()) }
    var isLoadingReviews by remember { mutableStateOf(true) }

    LaunchedEffect(perfume.id) {
        try {
            val response = RetrofitClient.instance.getPerfumeReviews(perfume.id)
            if (response.isSuccessful) {
                reviews = response.body() ?: emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            isLoadingReviews = false
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = { onAddToCart(quantity) },
                enabled = !isOutOfStock
            ) {
                Text(if (isOutOfStock) "Hết hàng" else "Thêm vào giỏ")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        },
        title = { Text(perfume.name ?: "Unknown", fontWeight = FontWeight.Bold) },
        text = {
            androidx.compose.foundation.lazy.LazyColumn(modifier = Modifier.fillMaxWidth()) {
                item {
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
                    Text(text = "Thương hiệu: ${perfume.brand ?: ""}", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "Giá: ${String.format(java.util.Locale.US, "%.1f", perfume.price ?: 0.0)}$", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                    
                    Row(modifier = Modifier.padding(vertical = 8.dp)) {
                        Text(
                            text = if (isOutOfStock) "Hết hàng" else "Còn lại: ${perfume.stockQuantity}", 
                            style = MaterialTheme.typography.bodySmall, 
                            color = if (isOutOfStock) Color.Red else Color.Unspecified,
                            modifier = Modifier.weight(1f)
                        )
                        Text(text = "Đã bán: ${perfume.soldCount ?: 0}", style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = perfume.description ?: "", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (!isOutOfStock) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = { if (quantity > 1) quantity-- }) {
                                Icon(Icons.Default.Remove, null)
                            }
                            Text(text = quantity.toString(), modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.titleMedium)
                            IconButton(onClick = { if (quantity < (perfume.stockQuantity ?: 0)) quantity++ }) {
                                Icon(Icons.Default.Add, null)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Đánh giá từ khách hàng", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (isLoadingReviews) {
                    item { CircularProgressIndicator(modifier = Modifier.padding(16.dp)) }
                } else if (reviews.isEmpty()) {
                    item { Text("Chưa có đánh giá nào cho sản phẩm này", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
                } else {
                    items(reviews.size) { index ->
                        val review = reviews[index]
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(review.fullName ?: "Khách hàng", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Spacer(Modifier.width(8.dp))
                                Row {
                                    for (i in 1..5) {
                                        Icon(
                                            Icons.Default.Star, null, 
                                            modifier = Modifier.size(14.dp),
                                            tint = if (i <= review.rating) Color(0xFFFFC107) else Color.LightGray
                                        )
                                    }
                                }
                            }
                            Text(review.comment, style = MaterialTheme.typography.bodySmall)
                            Text(review.createdAt?.take(10) ?: "", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                            HorizontalDivider(modifier = Modifier.padding(top = 8.dp), thickness = 0.5.dp)
                        }
                    }
                }
            }
        }
    )
}
