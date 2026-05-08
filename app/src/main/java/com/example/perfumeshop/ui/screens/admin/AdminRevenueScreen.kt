package com.example.perfumeshop.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.perfumeshop.viewmodel.AdminViewModel

@Composable
fun AdminRevenueScreen(viewModel: AdminViewModel) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.BarChart, null, modifier = Modifier.size(64.dp))
        Text("Thống kê doanh thu", style = MaterialTheme.typography.headlineSmall)
        Text("(Tính năng đang được cập nhật)")
    }
}
