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

@Composable
fun AdminRevenueScreen(viewModel: AdminViewModel) {
    var revenueData by remember { mutableStateOf<RevenueResponse?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val apiService = RetrofitClient.instance

    fun loadRevenue() {
        scope.launch {
            isLoading = true
            try {
                val response = apiService.getRevenue()
                if (response.isSuccessful) revenueData = response.body()
            } catch (e: Exception) {}
            finally { isLoading = false }
        }
    }

    LaunchedEffect(Unit) { loadRevenue() }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5)).padding(16.dp)) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            Column {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Tổng quan doanh thu", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    IconButton(onClick = { loadRevenue() }) { Icon(Icons.Default.Refresh, null) }
                }
                
                Spacer(modifier = Modifier.height(24.dp))

                RevenueCard(
                    title = "Tổng doanh thu",
                    value = "${String.format(Locale.US, "%.1f", revenueData?.totalRevenue ?: 0.0)}$",
                    icon = Icons.Default.TrendingUp,
                    color = Color(0xFF4CAF50)
                )

                Spacer(modifier = Modifier.height(16.dp))

                RevenueCard(
                    title = "Tổng số đơn hàng thành công",
                    value = "${revenueData?.orderCount ?: 0} đơn hàng",
                    icon = Icons.Default.BarChart,
                    color = Color(0xFF2196F3)
                )

                Spacer(modifier = Modifier.height(32.dp))
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ghi chú:", fontWeight = FontWeight.Bold)
                        Text("- Doanh thu chỉ tính trên các đơn hàng có trạng thái 'Hoàn thành' (Completed).", fontSize = 13.sp, color = Color.Gray)
                        Text("- Dữ liệu được cập nhật thời gian thực từ hệ thống.", fontSize = 13.sp, color = Color.Gray)
                    }
                }
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
