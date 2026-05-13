package com.example.perfumeshop.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.perfumeshop.model.Order
import com.example.perfumeshop.model.OrderItem
import com.example.perfumeshop.viewmodel.HistoryViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserHistoryScreen(viewModel: HistoryViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        viewModel.fetchOrders()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                TopAppBar(
                    title = { Text("Lịch sử đơn hàng", fontWeight = FontWeight.Bold) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
                // Filter Bar
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(viewModel.statuses) { (statusValue, statusLabel) ->
                        val isSelected = viewModel.selectedStatus == statusValue
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.onStatusSelected(statusValue) },
                            label = { Text(statusLabel) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F8F8))
        ) {
            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (viewModel.orders.isEmpty()) {
                EmptyHistory()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(viewModel.orders) { order ->
                        OrderCard(order)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyHistory() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.History, 
                contentDescription = null, 
                modifier = Modifier.size(80.dp), 
                tint = Color.LightGray
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("Bạn chưa có đơn hàng nào", color = Color.Gray, fontSize = 16.sp)
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: ID & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF0F2F5))
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Mã đơn: #${order.orderId}", 
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = order.orderDate.replace("T", " ").take(16), 
                        fontSize = 11.sp, 
                        color = Color.Gray
                    )
                }
                StatusChip(order.status)
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))

            // Items list
            order.items.forEach { item ->
                HistoryItemRow(item)
                Spacer(modifier = Modifier.height(12.dp))
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))

            // Footer
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "PTTT: ${order.paymentMethod}", 
                    fontSize = 12.sp, 
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Tổng thanh toán:", fontSize = 14.sp, color = Color.Gray)
                    Text(
                        text = "${String.format(Locale.US, "%.1f", order.totalAmount)}$", 
                        color = MaterialTheme.colorScheme.primary, 
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }
        }
    }
}

@Composable
fun HistoryItemRow(item: OrderItem) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        AsyncImage(
            model = RetrofitClient.getFullImageUrl(item.imageUrl),
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF5F5F5)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(item.brand, fontSize = 12.sp, color = Color.Gray)
            Text("x${item.quantity}", fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
        Text(
            text = "${String.format(Locale.US, "%.1f", item.unitPrice)}$",
            fontSize = 15.sp, 
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun StatusChip(status: String) {
    val (color, text) = when (status) {
        "Pending" -> Color(0xFFFFA000) to "Chờ xác nhận"
        "Processing" -> Color(0xFF1976D2) to "Đang xử lý"
        "Shipping" -> Color(0xFF0288D1) to "Đang giao"
        "Completed" -> Color(0xFF388E3C) to "Hoàn thành"
        "Cancelled" -> Color(0xFFD32F2F) to "Đã hủy"
        else -> Color.Gray to status
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
