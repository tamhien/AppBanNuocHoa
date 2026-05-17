package com.example.perfumeshop.ui.screens.user

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
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
    var showRatingDialog by remember { mutableStateOf(false) }
    var selectedItemForRating by remember { mutableStateOf<Pair<OrderItem, Int>?>(null) } // Pair of Item and OrderId

    LaunchedEffect(Unit) {
        viewModel.fetchOrders()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White).statusBarsPadding()) {
                // Filter Bar
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
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
                
                // Hiển thị lỗi nếu có
                viewModel.errorMessage?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(8.dp),
                            fontSize = 12.sp
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
                        OrderCard(
                            order = order,
                            onRateItem = { item ->
                                selectedItemForRating = item to order.orderId
                                showRatingDialog = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showRatingDialog && selectedItemForRating != null) {
        val context = LocalContext.current
        RatingDialog(
            itemName = selectedItemForRating!!.first.name ?: "",
            onDismiss = { showRatingDialog = false },
            onConfirm = { rating, comment ->
                viewModel.postReview(
                    perfumeId = selectedItemForRating!!.first.perfumeId,
                    orderId = selectedItemForRating!!.second,
                    rating = rating,
                    comment = comment
                ) { success, msg ->
                    showRatingDialog = false
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) {
                        viewModel.fetchOrders() // Tải lại để ẩn nút đánh giá
                    }
                }
            }
        )
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
fun OrderCard(order: Order, onRateItem: (OrderItem) -> Unit) {
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
                        text = order.orderDate?.replace("T", " ")?.take(16) ?: "N/A", 
                        fontSize = 11.sp, 
                        color = Color.Gray
                    )
                }
                StatusChip(order.status ?: "N/A")
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color(0xFFEEEEEE))

            // Items list
            order.items?.forEach { item ->
                Column {
                    HistoryItemRow(item)
                    // Hiển thị nút đánh giá khi đơn hàng hoàn thành và sản phẩm chưa được đánh giá
                    if (order.status?.equals("completed", ignoreCase = true) == true && item.isReviewed == 0) {
                        TextButton(
                            onClick = { onRateItem(item) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Icon(Icons.Default.Star, null, modifier = Modifier.size(16.dp), tint = Color(0xFFFFC107))
                            Spacer(Modifier.width(4.dp))
                            Text("Đánh giá sản phẩm", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else if (item.isReviewed > 0) {
                        Text(
                            text = "Đã đánh giá",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.End).padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
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
            Text(item.name ?: "", fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
            Text(item.brand ?: "", fontSize = 12.sp, color = Color.Gray)
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
fun RatingDialog(
    itemName: String,
    onDismiss: () -> Unit,
    onConfirm: (Int, String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đánh giá sản phẩm", fontWeight = FontWeight.Bold) },
        text = {
            // Thêm verticalScroll để không bị nén khi hiện bàn phím
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
            ) {
                Text(itemName, fontSize = 14.sp, color = Color.Gray)
                Spacer(Modifier.height(12.dp))

                Text("Chọn mức độ hài lòng:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(Modifier.height(8.dp))

                // Layout gọn gàng hơn cho Stars & Radio
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (i in 1..5) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { rating = i }
                        ) {
                            Icon(
                                imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                tint = if (i <= rating) Color(0xFFFFC107) else Color.LightGray,
                                modifier = Modifier.size(28.dp)
                            )
                            RadioButton(
                                selected = rating == i,
                                onClick = { rating = i },
                                colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFFFC107)),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Viết nhận xét của bạn...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(8.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(rating, comment) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Gửi đánh giá")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Composable
fun StatusChip(status: String) {
    val (color, text) = when (status.lowercase()) {
        "pending" -> Color(0xFFFFA000) to "Chờ xác nhận"
        "confirmed", "processing" -> Color(0xFF1976D2) to "Đang xử lý"
        "shipping" -> Color(0xFF0288D1) to "Đang giao"
        "completed" -> Color(0xFF388E3C) to "Hoàn thành"
        "cancelled" -> Color(0xFFD32F2F) to "Đã hủy"
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
