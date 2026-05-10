package com.example.perfumeshop.ui.screens.user

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.perfumeshop.viewmodel.HomeViewModel

@Composable
fun UserAccountScreen(
    viewModel: HomeViewModel,
    onLogout: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToPasswordReset: () -> Unit
) {
    var showUpdateDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showUpdateDialog) {
        if (viewModel.userProfile != null) {
            UpdateProfileDialog(
                user = viewModel.userProfile!!,
                onDismiss = { showUpdateDialog = false },
                onUpdate = { name, email, phone, addr ->
                    viewModel.updateProfile(name, email, phone, addr) { success, msg ->
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        if (success) showUpdateDialog = false
                    }
                }
            )
        } else {
            // Hiển thị loading hoặc tự động fetch nếu profile đang null
            LaunchedEffect(Unit) {
                viewModel.fetchUserProfile()
            }
            AlertDialog(
                onDismissRequest = { showUpdateDialog = false },
                text = { CircularProgressIndicator() },
                confirmButton = {}
            )
        }
    }

    if (showPasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showPasswordDialog = false },
            onConfirm = { current, new ->
                viewModel.changePassword(current, new) { success, msg ->
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                    if (success) showPasswordDialog = false
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Default.AccountCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        if (viewModel.isLoggedIn) {
            Text(
                text = viewModel.userFullName ?: "Người dùng",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(32.dp))

            AccountOptionItem(
                icon = Icons.Default.Edit,
                label = "Cập nhật thông tin",
                onClick = { 
                    viewModel.fetchUserProfile()
                    showUpdateDialog = true 
                }
            )
            AccountOptionItem(
                icon = Icons.Default.Lock,
                label = "Đổi mật khẩu",
                onClick = { showPasswordDialog = true }
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    viewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Đăng xuất")
            }
        } else {
            Text(
                text = "Bạn chưa đăng nhập",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Đăng nhập")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("Đăng ký")
            }

            TextButton(
                onClick = onNavigateToPasswordReset,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Text("Quên mật khẩu?")
            }
        }
    }
}

@Composable
fun UpdateProfileDialog(
    user: com.example.perfumeshop.model.UserResponse,
    onDismiss: () -> Unit,
    onUpdate: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(user.fullName) }
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.phone) }
    var address by remember { mutableStateOf(user.address) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cập nhật thông tin", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Họ tên") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = email, 
                    onValueChange = { email = it }, 
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = phone, 
                    onValueChange = { phone = it }, 
                    label = { Text("Số điện thoại") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = address, 
                    onValueChange = { address = it }, 
                    label = { Text("Địa chỉ") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(onClick = { onUpdate(name, email, phone, address) }) { Text("Lưu") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@Composable
fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var current by remember { mutableStateOf("") }
    var new by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Đổi mật khẩu") },
        text = {
            Column {
                OutlinedTextField(
                    value = current, 
                    onValueChange = { current = it }, 
                    label = { Text("Mật khẩu hiện tại") },
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = new, 
                    onValueChange = { new = it }, 
                    label = { Text("Mật khẩu mới") },
                    visualTransformation = PasswordVisualTransformation()
                )
                OutlinedTextField(
                    value = confirm, 
                    onValueChange = { confirm = it }, 
                    label = { Text("Nhập lại mật khẩu mới") },
                    visualTransformation = PasswordVisualTransformation()
                )
            }
        },
        confirmButton = {
            Button(onClick = { 
                if (new == confirm) onConfirm(current, new)
                else { /* Show error */ }
            }) { Text("Xác nhận") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}

@Composable
fun AccountOptionItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = label, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
        }
    }
}
