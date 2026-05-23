package com.example.perfumeshop.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.perfumeshop.ui.screens.auth.LoginScreen
import com.example.perfumeshop.ui.screens.auth.RegisterScreen
import com.example.perfumeshop.ui.screens.user.*
import com.example.perfumeshop.ui.screens.admin.AdminDashboardScreen
import com.example.perfumeshop.viewmodel.CartViewModel

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    // Khởi tạo CartViewModel ở đây để có thể chia sẻ dữ liệu giữa Cart và Checkout
    val cartViewModel: CartViewModel = viewModel()
    
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
                onNavigateToCart = { navController.navigate(Screen.Cart.route) },
                cartViewModel = cartViewModel
            )
        }
        composable(Screen.Cart.route) {
            CartScreen(
                onBack = { navController.popBackStack() },
                onCheckout = { selectedItems ->
                    // Dữ liệu đã có trong cartViewModel.getSelectedItems()
                    navController.navigate(Screen.Checkout.route)
                },
                viewModel = cartViewModel
            )
        }
        composable(Screen.Checkout.route) {
            CheckoutScreen(
                items = cartViewModel.getSelectedItems(),
                onBack = { navController.popBackStack() },
                onOrderSuccess = { orderId ->
                    navController.navigate(Screen.OrderSuccess.createRoute(orderId)) {
                        popUpTo(Screen.Cart.route) { inclusive = true }
                    }
                    // Sau khi đặt hàng thành công, cần fetch lại cart vì server đã xóa items
                    cartViewModel.fetchCart()
                },
                onNavigateToPayment = { url, orderId ->
                    navController.navigate(Screen.PaymentWebView.createRoute(url, orderId))
                }
            )
        }
        composable(
            route = Screen.PaymentWebView.route,
            arguments = listOf(
                navArgument("url") { type = NavType.StringType },
                navArgument("orderId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url")?.let { 
                java.net.URLDecoder.decode(it, "UTF-8") 
            } ?: ""
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            
            PaymentWebViewScreen(
                url = url,
                onBack = { navController.popBackStack() },
                onPaymentFinished = { success ->
                    if (success) {
                        navController.navigate(Screen.OrderSuccess.createRoute(orderId)) {
                            popUpTo(Screen.Cart.route) { inclusive = true }
                        }
                        cartViewModel.fetchCart()
                    } else {
                        navController.popBackStack()
                    }
                }
            )
        }
        composable(
            route = Screen.OrderSuccess.route,
            arguments = listOf(navArgument("orderId") { type = NavType.IntType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getInt("orderId") ?: 0
            OrderSuccessScreen(
                orderId = orderId,
                onContinueShopping = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
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
