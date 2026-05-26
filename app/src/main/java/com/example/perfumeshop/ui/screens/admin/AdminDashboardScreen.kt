package com.example.perfumeshop.ui.screens.admin

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    var selectedItem by remember { mutableStateOf<AdminMenuItem>(AdminMenuItem.Products) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color.White,
                drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(0.7f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_1),
                                contentDescription = "Admin Avatar",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text("QUẢN TRỊ VIÊN", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                        Text("Hệ thống Perfume Shop", color = Color.White.copy(0.8f), fontSize = 12.sp)
                    }
                }
                
                Spacer(Modifier.height(20.dp))
                
                val items = listOf(AdminMenuItem.Products, AdminMenuItem.Orders, AdminMenuItem.Customers, AdminMenuItem.Revenue)
                items.forEach { item ->
                    NavigationDrawerItem(
                        icon = { Icon(item.icon, null, tint = if (item == selectedItem) MaterialTheme.colorScheme.primary else Color.Gray) },
                        label = { Text(item.title, fontWeight = if (item == selectedItem) FontWeight.Bold else FontWeight.Normal) },
                        selected = item == selectedItem,
                        onClick = { selectedItem = item; scope.launch { drawerState.close() } },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(0.1f),
                            selectedTextColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
                
                Spacer(Modifier.weight(1f))
                
                HorizontalDivider(modifier = Modifier.padding(horizontal = 24.dp), color = Color(0xFFEEEEEE))
                
                NavigationDrawerItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = Color.Red) },
                    label = { Text("Đăng xuất", color = Color.Red, fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = { 
                        scope.launch { 
                            drawerState.close() 
                            onLogout()
                        } 
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                Surface(shadowElevation = 4.dp) {
                    TopAppBar(
                        title = { Text(selectedItem.title, fontWeight = FontWeight.Bold) },
                        navigationIcon = {
                            IconButton(onClick = { scope.launch { drawerState.open() } }) { 
                                Icon(Icons.Default.Menu, "Menu", tint = MaterialTheme.colorScheme.primary) 
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                    )
                }
            }
        ) { paddingValues ->
            Box(Modifier.fillMaxSize().padding(paddingValues).background(Color(0xFFF5F7F9))) {
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
