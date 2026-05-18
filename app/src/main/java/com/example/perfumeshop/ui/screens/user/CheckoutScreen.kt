package com.example.perfumeshop.ui.screens.user

import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.CartItem
import com.example.perfumeshop.viewmodel.CheckoutViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckoutScreen(
    items: List<CartItem>,
    onBack: () -> Unit,
    onOrderSuccess: (Int) -> Unit,
    viewModel: CheckoutViewModel = viewModel()
) {
    LaunchedEffect(items) {
        viewModel.checkoutItems = items
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Thanh toán", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                        }
                    }
                )
            },
            bottomBar = {
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
                            Text("${String.format(Locale.US, "%.1f", viewModel.totalAmount)}$", 
                                fontSize = 20.sp, 
                                fontWeight = FontWeight.Bold, 
                                color = MaterialTheme.colorScheme.primary)
                        }
                        Button(
                            onClick = { viewModel.placeOrder(onOrderSuccess) },
                            enabled = !viewModel.isLoading,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                        ) {
                            if (viewModel.isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                            else Text("Đặt hàng")
                        }
                    }
                }
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFF8F8F8)),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    SectionTitle("Thông tin giao hàng", Icons.Default.LocationOn)
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            OutlinedTextField(
                                value = viewModel.recipientName,
                                onValueChange = { viewModel.recipientName = it },
                                label = { Text("Họ và tên người nhận") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = viewModel.recipientPhone,
                                onValueChange = { viewModel.recipientPhone = it },
                                label = { Text("Số điện thoại") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = viewModel.recipientAddress,
                                onValueChange = { viewModel.recipientAddress = it },
                                label = { Text("Địa chỉ chi tiết") },
                                modifier = Modifier.fillMaxWidth(),
                                minLines = 2
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = viewModel.note,
                                onValueChange = { viewModel.note = it },
                                label = { Text("Ghi chú đơn hàng (Không bắt buộc)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item { SectionTitle("Sản phẩm đã chọn", Icons.Default.ShoppingCart) }
                items(viewModel.checkoutItems) { item ->
                    CheckoutItemRow(item)
                    HorizontalDivider(color = Color(0xFFEEEEEE))
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    SectionTitle("Phương thức thanh toán", Icons.Default.Payment)
                    val paymentMethods = listOf(
                        "Thanh toán khi nhận hàng (COD)",
                        "Thanh toán qua VNPAY",
                        "Chuyển khoản ngân hàng",
                        "Ví điện tử (Momo/ZaloPay)"
                    )
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(vertical = 8.dp)) {
                            paymentMethods.forEach { method ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = viewModel.paymentMethod == method,
                                        onClick = { viewModel.paymentMethod = method }
                                    )
                                    Text(method, modifier = Modifier.padding(start = 8.dp))
                                }
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tổng số lượng:", color = Color.Gray)
                                Text("${viewModel.totalQuantity} sản phẩm", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tổng tiền hàng:", color = Color.Gray)
                                Text("${String.format(Locale.US, "%.1f", viewModel.totalAmount)}$", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Phí vận chuyển:", color = Color.Gray)
                                Text("Miễn phí", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
                            }
                            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(), 
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Text("Tổng thanh toán:", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${String.format(Locale.US, "%.1f", viewModel.totalAmount)}$", 
                                    fontSize = 22.sp, 
                                    fontWeight = FontWeight.Bold, 
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                
                item {
                    viewModel.errorMessage?.let {
                        Text(
                            text = it,
                            color = Color.Red,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    }
                }
            }
        }

        // WebView Overlay cho VNPay
        viewModel.vnpayUrl?.let { url ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .clickable(enabled = true, onClick = {}) 
            ) {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.apply {
                                javaScriptEnabled = true
                                domStorageEnabled = true
                                databaseEnabled = true
                                useWideViewPort = true
                                loadWithOverviewMode = true
                                javaScriptCanOpenWindowsAutomatically = true
                                setSupportMultipleWindows(false) // Đổi thành false để tải trong cùng WebView
                                mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                            }
                            
                            webChromeClient = android.webkit.WebChromeClient()
                            
                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                    val currentUrl = request?.url.toString()
                                    if (currentUrl.contains("vnpay_return")) {
                                        if (currentUrl.contains("vnp_ResponseCode=00")) {
                                            viewModel.vnpayUrl = null
                                            onOrderSuccess(0) 
                                        } else {
                                            viewModel.vnpayUrl = null
                                            viewModel.errorMessage = "Thanh toán không thành công"
                                        }
                                        return true
                                    }
                                    return false // Cho phép các URL khác (như trang nhập OTP) load bình thường
                                }
                            }
                            loadUrl(url)
                            requestFocus()
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
                // Nút đóng WebView thủ công
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(40.dp)
                        .align(Alignment.TopEnd),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.5f),
                    onClick = { viewModel.vnpayUrl = null }
                ) {
                    Icon(
                        Icons.Default.Close, 
                        contentDescription = "Đóng", 
                        tint = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SectionTitle(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 12.dp)) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun CheckoutItemRow(item: CartItem) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = RetrofitClient.getFullImageUrl(item.imageUrl),
            contentDescription = null,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(4.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.Medium, maxLines = 1)
            Text("SL: ${item.quantity}", fontSize = 13.sp, color = Color.Gray)
        }
        Text("${String.format(Locale.US, "%.1f", item.price * item.quantity)}$", fontWeight = FontWeight.Bold)
    }
}
