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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.outlined.*
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

    LaunchedEffect(Unit) {
        viewModel.updateSession()
        cartViewModel.fetchCart()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Surface(
                shadowElevation = 4.dp,
                color = Color.White
            ) {
                Column(modifier = Modifier.statusBarsPadding()) {
                    TopAppBar(
                        title = {
                            if (isSearchExpanded) {
                                TextField(
                                    value = viewModel.searchQuery,
                                    onValueChange = { viewModel.onSearchQueryChange(it) },
                                    placeholder = { Text("Tìm kiếm mùi hương...") },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    textStyle = MaterialTheme.typography.bodyLarge
                                )
                            } else {
                                Text(
                                    "PRINCE PHOM", 
                                    fontWeight = FontWeight.Black, 
                                    fontSize = 24.sp,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = { 
                                isSearchExpanded = !isSearchExpanded 
                                if (!isSearchExpanded) viewModel.onSearchQueryChange("")
                            }) {
                                Icon(
                                    imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search, 
                                    contentDescription = null,
                                    tint = Color.DarkGray
                                )
                            }
                            Box {
                                IconButton(onClick = onNavigateToCart) {
                                    Icon(Icons.Outlined.ShoppingCart, null, tint = Color.DarkGray)
                                }
                                if (cartViewModel.cartItems.isNotEmpty()) {
                                    Surface(
                                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 4.dp),
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape
                                    ) {
                                        Text(
                                            text = cartViewModel.cartItems.size.toString(), 
                                            color = Color.White, 
                                            fontSize = 9.sp, 
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val navItems = listOf(
                    Triple(0, "Trang chủ", Icons.Default.Home),
                    Triple(1, "Yêu thích", Icons.Default.FavoriteBorder),
                    Triple(2, "Lịch sử", Icons.Default.History),
                    Triple(3, "Tài khoản", Icons.Default.PersonOutline)
                )

                navItems.forEach { (index, label, icon) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { 
                            Icon(
                                imageVector = icon, 
                                contentDescription = label,
                                tint = if (selectedTab == index) MaterialTheme.colorScheme.primary else Color.Gray
                            ) 
                        },
                        label = { 
                            Text(
                                label, 
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 11.sp
                            ) 
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF8F9FA))) {
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

    // Modal Sheet Style Dialog cho Chi tiết sản phẩm
    selectedProduct?.let { perfume ->
        ModalProductDetail(
            perfume = perfume,
            onDismiss = { selectedProduct = null },
            onAddToCart = { qty ->
                cartViewModel.addToCart(perfume.id, qty) { success, message ->
                    selectedProduct = null
                    scope.launch {
                        snackbarHostState.showSnackbar(message)
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModalProductDetail(
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
            if (response.isSuccessful) reviews = response.body() ?: emptyList()
        } finally { isLoadingReviews = false }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp)
        ) {
            item {
                Box(modifier = Modifier.fillMaxWidth().height(350.dp).clip(RoundedCornerShape(24.dp))) {
                    AsyncImage(
                        model = RetrofitClient.getFullImageUrl(perfume.imageUrl),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    
                    if (isOutOfStock) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("HẾT HÀNG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(perfume.brand ?: "Brand", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text(perfume.name ?: "Name", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        "${String.format(java.util.Locale.US, "%.1f", perfume.price)}$",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    repeat(5) { i ->
                        Icon(
                            Icons.Default.Star, null, 
                            modifier = Modifier.size(18.dp),
                            tint = if (i < 4) Color(0xFFFFC107) else Color.LightGray // Giả lập rating trung bình
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("(${reviews.size} đánh giá)", color = Color.Gray, fontSize = 14.sp)
                    Spacer(Modifier.weight(1f))
                    Text("Đã bán: ${perfume.soldCount ?: 0}", color = Color.Gray, fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Mô tả sản phẩm", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(perfume.description ?: "Không có mô tả.", color = Color.DarkGray, lineHeight = 20.sp, fontSize = 14.sp)
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (!isOutOfStock) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(Color(0xFFF5F5F5), RoundedCornerShape(16.dp)).padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { if (quantity > 1) quantity-- }) { Icon(Icons.Default.Remove, null) }
                            Text(text = quantity.toString(), modifier = Modifier.padding(horizontal = 16.dp), fontWeight = FontWeight.Bold)
                            IconButton(onClick = { if (quantity < (perfume.stockQuantity ?: 0)) quantity++ }) { Icon(Icons.Default.Add, null) }
                        }
                        
                        Button(
                            onClick = { onAddToCart(quantity) },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
                        ) {
                            Text("THÊM VÀO GIỎ", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = { },
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("SẢN PHẨM HIỆN ĐANG HẾT HÀNG")
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                Text("Đánh giá (${reviews.size})", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (isLoadingReviews) {
                item { Box(Modifier.fillMaxWidth().padding(20.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            } else if (reviews.isEmpty()) {
                item { Text("Chưa có đánh giá nào.", color = Color.Gray, modifier = Modifier.padding(bottom = 20.dp)) }
            } else {
                items(reviews.size) { index ->
                    val review = reviews[index]
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFBFBFB)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primary.copy(0.1f)) {
                                    Text(
                                        review.fullName?.take(1)?.uppercase() ?: "K", 
                                        modifier = Modifier.padding(8.dp),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(review.fullName ?: "Khách hàng", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Row {
                                        repeat(5) { i ->
                                            Icon(Icons.Default.Star, null, modifier = Modifier.size(12.dp), tint = if (i < review.rating) Color(0xFFFFC107) else Color.LightGray)
                                        }
                                    }
                                }
                                Spacer(Modifier.weight(1f))
                                Text(review.createdAt?.take(10) ?: "", fontSize = 12.sp, color = Color.Gray)
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(review.comment, fontSize = 13.sp, color = Color.DarkGray)
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}
