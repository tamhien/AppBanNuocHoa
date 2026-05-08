package com.example.perfumeshop.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.perfumeshop.model.UserResponse
import com.example.perfumeshop.viewmodel.AdminViewModel

@Composable
fun AdminCustomersScreen(viewModel: AdminViewModel) {
    val users by viewModel.users.collectAsState()
    var userToDelete by remember { mutableStateOf<UserResponse?>(null) }

    Box(Modifier.fillMaxSize()) {
        if (viewModel.isLoading) {
            CircularProgressIndicator(Modifier.align(Alignment.Center))
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(users) { user ->
                    CustomerItem(
                        user = user,
                        onLock = { /* Chức năng khóa tài khoản */ },
                        onDelete = { userToDelete = user }
                    )
                }
            }
        }
    }

    // Dialog xác nhận xóa khách hàng
    userToDelete?.let { user ->
        AlertDialog(
            onDismissRequest = { userToDelete = null },
            title = { Text("Xác nhận xóa") },
            text = { Text("Bạn có chắc chắn muốn xóa tài khoản của '${user.fullName}'? Hành động này không thể khôi phục.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteUser(user.userId)
                        userToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                ) {
                    Text("Xóa")
                }
            },
            dismissButton = {
                TextButton(onClick = { userToDelete = null }) {
                    Text("Hủy")
                }
            }
        )
    }
}

@Composable
fun CustomerItem(user: UserResponse, onLock: () -> Unit, onDelete: () -> Unit) {
    Card(Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, null, modifier = Modifier.size(40.dp), tint = Color.Gray)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(user.fullName, fontWeight = FontWeight.Bold)
                Text(user.username, style = MaterialTheme.typography.bodySmall)
                Text(user.email, style = MaterialTheme.typography.bodySmall)
                Text(user.phone, style = MaterialTheme.typography.bodySmall)
            }
            Row {
                IconButton(onClick = onLock) {
                    Icon(Icons.Default.Block, "Khóa", tint = Color(0xFFFFA500)) // Màu cam (Orange)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Xóa", tint = Color.Red)
                }
            }
        }
    }
}
