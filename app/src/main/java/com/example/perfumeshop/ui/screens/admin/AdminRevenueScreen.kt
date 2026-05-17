package com.example.perfumeshop.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.RevenueResponse
import com.example.perfumeshop.viewmodel.AdminViewModel
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminRevenueScreen(viewModel: AdminViewModel) {
    var revenueData by remember { mutableStateOf<RevenueResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedMonth by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val apiService = RetrofitClient.instance

    fun loadRevenue(month: String? = null) {
        scope.launch {
            isLoading = true
            try {
                val response = apiService.getRevenue(month)
                if (response.isSuccessful) {
                    revenueData = response.body()
                } else {
                    // Log error if needed
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedMonth) { 
        loadRevenue(selectedMonth) 
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)).padding(16.dp)) {
        if (isLoading && revenueData == null) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Quản lý doanh thu", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { loadRevenue(selectedMonth) }) { Icon(Icons.Default.Refresh, null) }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                // Tổng quan
                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.weight(1f)) {
                        RevenueCardSmall(
                            title = "Tổng doanh thu",
                            value = "${String.format(Locale.US, "%.1f", revenueData?.totalRevenue ?: 0.0)}$",
                            color = Color(0xFF4CAF50)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        RevenueCardSmall(
                            title = "Tổng đơn hàng",
                            value = "${revenueData?.orderCount ?: 0}",
                            color = Color(0xFF2196F3)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text("Thống kê theo tháng", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))

                // Danh sách tháng
                androidx.compose.foundation.lazy.LazyRow(
                    contentPadding = PaddingValues(vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedMonth == null,
                            onClick = { selectedMonth = null },
                            label = { Text("Tất cả") }
                        )
                    }
                    val stats = revenueData?.monthlyStats ?: emptyList()
                    items(stats.size) { index ->
                        val stat = stats[index]
                        FilterChip(
                            selected = selectedMonth == stat.month,
                            onClick = { selectedMonth = stat.month },
                            label = { Text(stat.month) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Chi tiết đơn hàng trong tháng
                Text(
                    text = if (selectedMonth == null) "Chọn một tháng để xem chi tiết" else "Đơn hàng trong tháng $selectedMonth",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }

                androidx.compose.foundation.lazy.LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    val orders = revenueData?.ordersDetail ?: emptyList()
                    if (orders.isEmpty() && selectedMonth != null && !isLoading) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("Không có đơn hàng nào trong tháng này")
                            }
                        }
                    }
                    items(orders.size) { index ->
                        val order = orders[index]
                        OrderRevenueItem(order)
                    }
                }
            }
        }
    }
}

@Composable
fun RevenueCardSmall(title: String, value: String, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        }
    }
}

@Composable
fun OrderRevenueItem(order: com.example.perfumeshop.model.Order) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Mã ĐH: #${order.orderId}", fontWeight = FontWeight.Bold)
                Text("${String.format(Locale.US, "%.1f", order.totalAmount)}$", color = Color(0xFFE91E63), fontWeight = FontWeight.Bold)
            }
            Text("Khách: ${order.userFullName ?: "N/A"}", fontSize = 13.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Ngày: ${order.orderDate?.take(10) ?: "N/A"} ", fontSize = 13.sp, color = Color.Gray)
                val statusText = order.status ?: "N/A"
                Surface(
                    color = when(statusText.trim().lowercase(Locale.US)) {
                        "completed" -> Color(0xFFE8F5E9)
                        "cancelled" -> Color(0xFFFFEBEE)
                        else -> Color(0xFFFFF3E0)
                    },
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = statusText,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = when(statusText.trim().lowercase(Locale.US)) {
                            "completed" -> Color(0xFF2E7D32)
                            "cancelled" -> Color(0xFFC62828)
                            else -> Color(0xFFEF6C00)
                        }
                    )
                }
            }
            
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
            
            order.items?.forEach { item ->
                Text("• ${item.name} x${item.quantity}", fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun RevenueCard(title: String, value: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(24.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(12.dp)) {
                Icon(icon, null, modifier = Modifier.padding(12.dp).size(32.dp), tint = color)
            }
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(title, color = Color.Gray, fontSize = 14.sp)
                Text(value, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp, color = color)
            }
        }
    }
}
