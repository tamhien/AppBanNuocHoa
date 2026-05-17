package com.example.perfumeshop.ui.screens.admin

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.Order
import com.example.perfumeshop.model.OrderItem
import com.example.perfumeshop.viewmodel.AdminViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminOrdersScreen(viewModel: AdminViewModel) {
    val orders by viewModel.orders.collectAsState()
    val context = LocalContext.current
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilterStatus by remember { mutableStateOf("All") }
    
    val statusList = listOf(
        "All" to "Tất cả",
        "Pending" to "Chờ xác nhận",
        "Processing" to "Đã xác nhận",
        "Shipping" to "Đang vận chuyển",
        "Completed" to "Đã giao",
        "Cancelled" to "Đã hủy"
    )

    // Cập nhật lại dữ liệu mỗi khi vào màn hình
    LaunchedEffect(Unit) {
        viewModel.loadOrders()
    }

    // Logic Lọc & Tìm kiếm
    val filteredOrders = remember(orders, searchQuery, selectedFilterStatus) {
        orders.filter { order ->
            val matchesStatus = if (selectedFilterStatus == "All") true 
                               else order.status?.equals(selectedFilterStatus, ignoreCase = true) == true
            
            val query = searchQuery.trim().lowercase()
            val matchesSearch = if (query.isEmpty()) true
            else {
                order.orderId.toString().contains(query) || 
                (order.recipientName ?: "").lowercase().contains(query) ||
                (order.userFullName ?: "").lowercase().contains(query) ||
                (order.recipientPhone ?: "").contains(query)
            }
            
            matchesStatus && matchesSearch
        }
    }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        // Thanh Tìm kiếm và Lọc
        Surface(shadowElevation = 2.dp, color = Color.White) {
            Column(modifier = Modifier.padding(bottom = 8.dp)) {
                // Ô tìm kiếm
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Mã đơn, tên khách, số điện thoại...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color.Gray) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, null, tint = Color.Gray)
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color(0xFFF8F9FA),
                        focusedContainerColor = Color(0xFFF8F9FA),
                        unfocusedBorderColor = Color.Transparent,
                        focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                )

                // Danh sách Chip Trạng thái
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(statusList) { (statusValue, statusLabel) ->
                        val isSelected = selectedFilterStatus == statusValue
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFilterStatus = statusValue },
                            label = { Text(statusLabel, fontSize = 13.sp) },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF0F0F0)
                            ),
                            border = null
                        )
                    }
                }
            }
        }

        // Danh sách đơn hàng
        Box(modifier = Modifier.weight(1f)) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (filteredOrders.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Inbox, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Text("Không tìm thấy đơn hàng nào", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredOrders, key = { it.orderId }) { order ->
                        AdminOrderCard(
                            order = order,
                            onStatusChange = { newStatus ->
                                viewModel.updateOrderStatus(order.orderId, newStatus) { _, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AdminOrderCard(order: Order, onStatusChange: (String) -> Unit) {
    var isExpanded by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }

    val statusOptions = listOf(
        "Pending" to "Chờ xác nhận",
        "Processing" to "Đã xác nhận",
        "Shipping" to "Đang vận chuyển",
        "Completed" to "Đã giao",
        "Cancelled" to "Đã hủy"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: ID & Ngày
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("Mã đơn: #${order.orderId}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                    Text((order.orderDate ?: "").replace("T", " ").take(16), fontSize = 12.sp, color = Color.Gray)
                }
                
                Box {
                    StatusChipAdmin(order.status ?: "Pending", onClick = { showStatusMenu = true })
                    DropdownMenu(expanded = showStatusMenu, onDismissRequest = { showStatusMenu = false }) {
                        statusOptions.forEach { (value, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    showStatusMenu = false
                                    if (order.status?.equals(value, ignoreCase = true) != true) onStatusChange(value)
                                }
                            )
                        }
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color(0xFFF0F0F0))

            // Thông tin khách hàng
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${order.recipientName} (${order.userFullName ?: "Khách"})", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(order.recipientPhone ?: "", fontSize = 14.sp)
                }
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(16.dp).padding(top = 2.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(order.recipientAddress ?: "", fontSize = 14.sp, maxLines = 2, lineHeight = 18.sp)
                }
                if (!order.note.isNullOrBlank()) {
                    Row(verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.EditNote, null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Ghi chú: ${order.note}", fontSize = 13.sp, color = Color.DarkGray, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                    }
                }
            }

            // Danh sách sản phẩm (Có thể thu gọn/mở rộng)
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                onClick = { isExpanded = !isExpanded },
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Sản phẩm (${order.items?.size ?: 0})", color = MaterialTheme.colorScheme.primary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    order.items?.forEach { item ->
                        AdminOrderItemRow(item)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp, color = Color(0xFFF0F0F0))

            // Footer: PTTT & Tổng tiền
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "PTTT: ${order.paymentMethod}", fontSize = 12.sp, color = Color.Gray)
                Column(horizontalAlignment = Alignment.End) {
                    Text("Tổng thanh toán", fontSize = 12.sp, color = Color.Gray)
                    Text("${String.format(Locale.US, "%.1f", order.totalAmount)}$", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
            }
        }
    }
}

@Composable
fun AdminOrderItemRow(item: OrderItem) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().background(Color(0xFFFAFAFA), RoundedCornerShape(8.dp)).padding(8.dp)
    ) {
        AsyncImage(
            model = RetrofitClient.getFullImageUrl(item.imageUrl),
            contentDescription = null,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(4.dp)).background(Color.White),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name ?: "", fontSize = 13.sp, fontWeight = FontWeight.Medium, maxLines = 1)
            Text("${item.brand} | x${item.quantity}", fontSize = 11.sp, color = Color.Gray)
        }
        Text("${String.format(Locale.US, "%.1f", item.unitPrice)}$", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun StatusChipAdmin(status: String, onClick: () -> Unit) {
    val (color, text) = when (status) {
        "Pending" -> Color(0xFFFFA000) to "Chờ xác nhận"
        "Processing" -> Color(0xFF1976D2) to "Đã xác nhận"
        "Shipping" -> Color(0xFFFF9800) to "Đang vận chuyển"
        "Completed" -> Color(0xFF388E3C) to "Đã giao"
        "Cancelled" -> Color(0xFFD32F2F) to "Đã hủy"
        else -> Color.Gray to status
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(text = text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.ArrowDropDown, null, tint = color, modifier = Modifier.size(18.dp))
        }
    }
}
