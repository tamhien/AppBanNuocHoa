package com.example.perfumeshop.ui.screens.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.perfumeshop.R
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.Perfume
import com.example.perfumeshop.viewmodel.AdminViewModel
import kotlinx.coroutines.launch

sealed class AdminMenuItem(val title: String, val icon: ImageVector) {
    object Products : AdminMenuItem("Quản lý sản phẩm", Icons.Default.Inventory)
    object Orders : AdminMenuItem("Quản lý đơn hàng", Icons.Default.ShoppingCart)
    object Customers : AdminMenuItem("Quản lý khách hàng", Icons.Default.People)
    object Revenue : AdminMenuItem("Thống kê doanh thu", Icons.Default.BarChart)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(adminViewModel: AdminViewModel = viewModel()) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var selectedItem by remember { mutableStateOf<AdminMenuItem>(AdminMenuItem.Products) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Box(
                    modifier = Modifier.fillMaxWidth().height(180.dp).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(id = R.drawable.img_1),
                            contentDescription = "Admin Avatar",
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Admin Manager", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(16.dp))
                val items = listOf(AdminMenuItem.Products, AdminMenuItem.Orders, AdminMenuItem.Customers, AdminMenuItem.Revenue)
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, null) },
                        label = { Text(item.title) },
                        selected = item == selectedItem,
                        onClick = { selectedItem = item; scope.launch { drawerState.close() } },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                Spacer(Modifier.weight(1f))
                HorizontalDivider()
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
                    label = { Text("Đăng xuất") },
                    selected = false,
                    onClick = { scope.launch { drawerState.close() } },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedItem.title) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) { Icon(Icons.Default.Menu, "Menu") }
                    }
                )
            }
        ) { paddingValues ->
            Box(Modifier.fillMaxSize().padding(paddingValues)) {
                AdminContent(selectedItem, adminViewModel)
            }
        }
    }
}

@Composable
fun AdminContent(item: AdminMenuItem, viewModel: AdminViewModel) {
    when (item) {
        AdminMenuItem.Products -> AdminProductsContent(viewModel)
        else -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(item.title) }
    }
}

@Composable
fun AdminProductsContent(viewModel: AdminViewModel) {
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

    viewModel.errorMessage?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.errorMessage = null },
            title = { Text("Thông báo") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = { viewModel.errorMessage = null }) { Text("OK") } }
        )
    }
}

@Composable
fun ProductAdminItem(perfume: Perfume, onEdit: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            AsyncImage(
                model = RetrofitClient.getFullImageUrl(perfume.imageUrl),
                contentDescription = null,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(perfume.name, fontWeight = FontWeight.Bold)
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

    // Dùng GetContent để mở trình quản lý file có "Browse" (menu 3 gạch)
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
                        Modifier.fillMaxWidth().height(180.dp).clip(RoundedCornerShape(12.dp))
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
                    OutlinedTextField(name, { name = it }, label = { Text("Tên sản phẩm (có dấu)") }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences))
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
