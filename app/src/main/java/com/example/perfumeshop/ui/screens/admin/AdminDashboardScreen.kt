package com.example.perfumeshop.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.perfumeshop.R
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
fun AdminDashboardScreen(
    onLogout: () -> Unit,
    adminViewModel: AdminViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // Mặc định luôn hiển thị giao diện quản lý sản phẩm
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
                    onClick = { 
                        scope.launch { 
                            drawerState.close() 
                            onLogout()
                        } 
                    },
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
        AdminMenuItem.Products -> AdminProductsScreen(viewModel)
        AdminMenuItem.Customers -> AdminCustomersScreen(viewModel)
        AdminMenuItem.Orders -> AdminOrdersScreen(viewModel)
        AdminMenuItem.Revenue -> AdminRevenueScreen(viewModel)
    }
}
