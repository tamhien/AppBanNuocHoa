package com.example.perfumeshop.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object AdminDashboard : Screen("admin_dashboard")
    object AdminProducts : Screen("admin_products")
    object AdminOrders : Screen("admin_orders")
    object AdminCustomers : Screen("admin_customers")
    object AdminRevenue : Screen("admin_revenue")
}
