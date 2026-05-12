package com.example.perfumeshop.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
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
import com.example.perfumeshop.model.CartItem
import com.example.perfumeshop.viewmodel.CartViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    onBack: () -> Unit,
    onCheckout: (List<CartItem>) -> Unit,
    viewModel: CartViewModel = viewModel()
) {
    val cartItems = viewModel.cartItems
    val totalPrice = viewModel.totalPrice
    val isSelectAll = viewModel.isSelectAll

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Giỏ hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                BottomAppBar(
                    containerColor = Color.White,
                    modifier = Modifier.height(80.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Tổng thanh toán", fontSize = 14.sp, color = Color.Gray)
                            Text("$${String.format(Locale.US, "%.2f", totalPrice)}", 
                                fontSize = 20.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.primary)
                        }
                        Button(
                            onClick = { 
                                val selected = viewModel.getSelectedItems()
                                if (selected.isNotEmpty()) onCheckout(selected)
                            },
                            enabled = viewModel.getSelectedItems().isNotEmpty(),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            Text("Đặt hàng (${viewModel.getSelectedItems().size})")
                        }
                    }
                }
            }
        }
    ) { padding ->
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (cartItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Giỏ hàng của bạn đang trống", color = Color.Gray)
                }
            }
        } else {
            Column(modifier = Modifier.padding(padding)) {
                // Header Select All
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelectAll,
                        onCheckedChange = { viewModel.toggleSelectAll() }
                    )
                    Text("Chọn tất cả (${cartItems.size})", fontWeight = FontWeight.Medium)
                }

                HorizontalDivider()

                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(cartItems) { item ->
                        CartItemRow(
                            item = item,
                            onToggleSelection = { viewModel.toggleSelection(item.cartId) },
                            onIncrease = { viewModel.updateQuantity(item.cartId, item.quantity + 1) },
                            onDecrease = { viewModel.updateQuantity(item.cartId, item.quantity - 1) },
                            onDelete = { viewModel.deleteItem(item.cartId) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CartItemRow(
    item: CartItem,
    onToggleSelection: () -> Unit,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = item.isSelected,
            onCheckedChange = { onToggleSelection() }
        )
        
        AsyncImage(
            model = RetrofitClient.getFullImageUrl(item.imageUrl),
            contentDescription = null,
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(item.brand, fontSize = 12.sp, color = Color.Gray)
            Text("$${item.price}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDecrease, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Remove, contentDescription = null, modifier = Modifier.size(18.dp))
                }
                Text("${item.quantity}", modifier = Modifier.padding(horizontal = 8.dp))
                IconButton(onClick = onIncrease, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = Color.Red)
        }
    }
}
