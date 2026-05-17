package com.example.perfumeshop.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.Perfume
import com.example.perfumeshop.viewmodel.AdminViewModel

@Composable
fun AdminProductsScreen(viewModel: AdminViewModel) {
    val perfumes by viewModel.perfumes.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingPerfume by remember { mutableStateOf<Perfume?>(null) }
    var perfumeToDelete by remember { mutableStateOf<Perfume?>(null) }

    Box(Modifier.fillMaxSize()) {
        if (viewModel.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(perfumes) { perfume ->
                    ProductAdminItem(
                        perfume = perfume,
                        onEdit = { editingPerfume = perfume; showDialog = true },
                        onDelete = { perfumeToDelete = perfume }
                    )
                }
            }
        }
        FloatingActionButton(
            onClick = { editingPerfume = null; showDialog = true },
            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp)
        ) { Icon(Icons.Default.Add, "Thêm") }
    }

    if (showDialog) {
        ProductDialog(
            perfume = editingPerfume,
            viewModel = viewModel,
            onDismiss = { showDialog = false },
            onConfirm = { p ->
                if (editingPerfume == null) viewModel.addPerfume(p) { showDialog = false }
                else viewModel.updatePerfume(editingPerfume!!.id, p) { showDialog = false }
            }
        )
    }

    // Dialog xác nhận xóa
    perfumeToDelete?.let { perfume ->
        AlertDialog(
            onDismissRequest = { perfumeToDelete = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa sản phẩm '${perfume.name}' không? Thao tác này không thể hoàn tác.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePerfume(perfume.id)
                        perfumeToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { perfumeToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun ProductAdminItem(perfume: Perfume, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = RetrofitClient.getFullImageUrl(perfume.imageUrl),
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(perfume.name ?: "", fontWeight = FontWeight.Bold)
                Text("${perfume.price} $", color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, "Sửa", tint = Color.Blue) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Xóa", tint = Color.Red) }
        }
    }
}

@Composable
fun ProductDialog(perfume: Perfume?, viewModel: AdminViewModel, onDismiss: () -> Unit, onConfirm: (Perfume) -> Unit) {
    var name by remember { mutableStateOf(perfume?.name ?: "") }
    var brand by remember { mutableStateOf(perfume?.brand ?: "") }
    var price by remember { mutableStateOf(perfume?.price?.toString() ?: "") }
    var stock by remember { mutableStateOf(perfume?.stockQuantity?.toString() ?: "") }
    var imageUrl by remember { mutableStateOf(perfume?.imageUrl ?: "") }
    var description by remember { mutableStateOf(perfume?.description ?: "") }
    var gender by remember { mutableStateOf(perfume?.gender ?: "Unisex") }

    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            isUploading = true
            viewModel.uploadImage(context, it) { uploadedPath ->
                isUploading = false
                if (uploadedPath != null) imageUrl = uploadedPath
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (perfume == null) "Thêm sản phẩm" else "Sửa sản phẩm") },
        text = {
            LazyColumn(Modifier.fillMaxWidth()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(12.dp))
                            .background(Color.LightGray.copy(alpha = 0.3f))
                            .clickable { pickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isUploading) CircularProgressIndicator()
                        else if (imageUrl.isNotEmpty()) {
                            AsyncImage(RetrofitClient.getFullImageUrl(imageUrl), null, Modifier.fillMaxSize(), contentScale = ContentScale.Fit)
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.CloudUpload, null, Modifier.size(48.dp), tint = Color.Gray)
                                Text("Nhấn để Browse ảnh từ Downloads", color = Color.Gray)
                            }
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(name, { name = it }, label = { Text("Tên sản phẩm") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(brand, { brand = it }, label = { Text("Thương hiệu") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(price, { price = it }, label = { Text("Giá ($)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(stock, { stock = it }, label = { Text("Số lượng") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(description, { description = it }, label = { Text("Mô tả (Tiếng Việt)") }, modifier = Modifier.fillMaxWidth(), minLines = 3, keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences))
                    
                    Text("Giới tính:", modifier = Modifier.padding(top = 12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(gender == "Men", { gender = "Men" }); Text("Nam")
                        Spacer(Modifier.width(8.dp))
                        RadioButton(gender == "Women", { gender = "Women" }); Text("Nữ")
                        Spacer(Modifier.width(8.dp))
                        RadioButton(gender == "Unisex", { gender = "Unisex" }); Text("Unisex")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val p = Perfume(perfume?.id ?: 0, name.trim(), brand.trim(), description.trim(), price.toDoubleOrNull() ?: 0.0, stock.toIntOrNull() ?: 0, imageUrl, gender)
                    onConfirm(p)
                },
                enabled = !isUploading && name.isNotBlank()
            ) { Text("Lưu sản phẩm") }
        },
        dismissButton = { TextButton(onDismiss) { Text("Hủy") } }
    )
}
