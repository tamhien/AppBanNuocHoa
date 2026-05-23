package com.example.perfumeshop.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object PasswordReset : Screen("password_reset")
    object AdminDashboard : Screen("admin_dashboard")
    object AdminProducts : Screen("admin_products")
    object AdminOrders : Screen("admin_orders")
    object AdminCustomers : Screen("admin_customers")
    object AdminRevenue : Screen("admin_revenue")
    object Cart : Screen("cart")
    object Checkout : Screen("checkout")
    object OrderSuccess : Screen("order_success/{orderId}") {
        fun createRoute(orderId: Int) = "order_success/$orderId"
    }
    object PaymentWebView : Screen("payment_webview/{url}/{orderId}") {
        fun createRoute(url: String, orderId: Int) = "payment_webview/${java.net.URLEncoder.encode(url, "UTF-8")}/$orderId"
    }
}
