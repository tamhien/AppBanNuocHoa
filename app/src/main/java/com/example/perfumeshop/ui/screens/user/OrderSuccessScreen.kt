package com.example.perfumeshop.ui.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OrderSuccessScreen(
    orderId: Int,
    onContinueShopping: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.CheckCircle, 
                contentDescription = null, 
                modifier = Modifier.size(100.dp), 
                tint = Color(0xFF4CAF50)
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Đặt hàng thành công!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Mã đơn hàng: #$orderId", color = Color.Gray)
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onContinueShopping,
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                Text("Tiếp tục mua sắm")
            }
        }
    }
}
