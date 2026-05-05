package com.example.perfumeshop.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object AdminDashboard : Screen("admin_dashboard")
}
