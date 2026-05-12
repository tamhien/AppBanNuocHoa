package com.example.perfumeshop.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.perfumeshop.ui.screens.auth.LoginScreen
import com.example.perfumeshop.ui.screens.auth.RegisterScreen
import com.example.perfumeshop.ui.screens.user.UserMainScreen
import com.example.perfumeshop.ui.screens.user.PasswordResetScreen
import com.example.perfumeshop.ui.screens.user.CartScreen
import com.example.perfumeshop.ui.screens.admin.AdminDashboardScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToForgotPassword = { navController.navigate(Screen.PasswordReset.route) },
                onLoginSuccess = { role ->
                    if (role == "admin") {
                        navController.navigate(Screen.AdminDashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onRegisterSuccess = { navController.navigate(Screen.Login.route) }
            )
        }
        composable(Screen.Home.route) {
            UserMainScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = { navController.navigate(Screen.Login.route) },
                onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                onNavigateToPasswordReset = { navController.navigate(Screen.PasswordReset.route) },
                onNavigateToCart = { navController.navigate(Screen.Cart.route) }
            )
        }
        composable(Screen.Cart.route) {
            CartScreen(
                onBack = { navController.popBackStack() },
                onCheckout = { selectedItems ->
                    // Chuyển sang màn hình Thanh toán (Checkout) - sẽ làm sau
                }
            )
        }
        composable(Screen.PasswordReset.route) {
            PasswordResetScreen(onBack = { navController.popBackStack() })
        }
        composable(Screen.AdminDashboard.route) {
            AdminDashboardScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AdminDashboard.route) { inclusive = true }
                    }
                }
            )
        }
    }
}
