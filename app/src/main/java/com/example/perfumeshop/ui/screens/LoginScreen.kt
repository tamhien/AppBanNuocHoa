package com.example.perfumeshop.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.perfumeshop.R
import com.example.perfumeshop.utils.SessionManager
import com.example.perfumeshop.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: (String) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "Store Logo",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 16.dp)
        )

        Text(text = "Đăng Nhập", fontSize = 28.sp, style = MaterialTheme.typography.headlineLarge)
        
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = viewModel.username,
            onValueChange = { viewModel.onUsernameChange(it) },
            label = { Text("Tên đăng nhập") },
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.password,
            onValueChange = { viewModel.onPasswordChange(it) },
            label = { Text("Mật khẩu") },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = null)
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage!!, 
                color = MaterialTheme.colorScheme.error, 
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login(sessionManager, onSuccess = onLoginSuccess) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Đăng Nhập", fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToRegister) {
            Text("Chưa có tài khoản? Đăng ký ngay")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen(onNavigateToRegister = {}, onLoginSuccess = {})
}
