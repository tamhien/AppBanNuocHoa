package com.example.perfumeshop.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.FavoriteRequest
import com.example.perfumeshop.model.Perfume
import com.example.perfumeshop.utils.SessionManager
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val sessionManager = SessionManager(application)
    private val apiService = RetrofitClient.instance

    var allPerfumes by mutableStateOf<List<Perfume>>(emptyList())
        private set
    var filteredPerfumes by mutableStateOf<List<Perfume>>(emptyList())
        private set
    var favoritePerfumes by mutableStateOf<List<Perfume>>(emptyList())
        private set
    var favoriteIds by mutableStateOf<Set<Int>>(emptySet())
        private set

    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var currentGender by mutableStateOf<String?>(null)
        private set
    var searchQuery by mutableStateOf("")
        private set

    var isLoggedIn by mutableStateOf(sessionManager.isLoggedIn())
        private set

    var userFullName by mutableStateOf(sessionManager.getFullName())
        private set
    
    var userProfile by mutableStateOf<com.example.perfumeshop.model.UserResponse?>(null)
        private set

    init {
        fetchPerfumes()
        if (isLoggedIn) {
            fetchFavorites()
            fetchUserProfile()
        }
    }

    fun updateSession() {
        isLoggedIn = sessionManager.isLoggedIn()
        userFullName = sessionManager.getFullName()
        if (isLoggedIn) {
            fetchFavorites()
            fetchUserProfile()
        } else {
            favoriteIds = emptySet()
            favoritePerfumes = emptyList()
            userProfile = null
        }
    }

    fun fetchUserProfile() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return
        viewModelScope.launch {
            try {
                val response = apiService.getProfile(userId)
                if (response.isSuccessful) userProfile = response.body()
            } catch (e: Exception) {}
        }
    }

    fun updateProfile(fullName: String, email: String, phone: String, address: String, onResult: (Boolean, String) -> Unit) {
        val userId = sessionManager.getUserId()
        viewModelScope.launch {
            try {
                val response = apiService.updateProfile(userId, com.example.perfumeshop.model.UpdateProfileRequest(fullName, email, phone, address))
                if (response.isSuccessful) {
                    sessionManager.saveSession(userId, sessionManager.getRole() ?: "user", fullName)
                    userFullName = fullName
                    fetchUserProfile()
                    onResult(true, "Cập nhật thành công")
                } else onResult(false, "Lỗi cập nhật")
            } catch (e: Exception) { onResult(false, "Lỗi kết nối") }
        }
    }

    fun changePassword(current: String, new: String, onResult: (Boolean, String) -> Unit) {
        val userId = sessionManager.getUserId()
        viewModelScope.launch {
            try {
                val response = apiService.changePassword(userId, com.example.perfumeshop.model.ChangePasswordRequest(
                    com.example.perfumeshop.utils.HashUtils.sha256(current),
                    com.example.perfumeshop.utils.HashUtils.sha256(new)
                ))
                if (response.isSuccessful) onResult(true, "Đổi mật khẩu thành công")
                else {
                    val errorMsg = if (response.code() == 400) "Mật khẩu hiện tại không đúng" else "Lỗi đổi mật khẩu"
                    onResult(false, errorMsg)
                }
            } catch (e: Exception) { onResult(false, "Lỗi kết nối") }
        }
    }

    fun logout() {
        sessionManager.clearSession()
        updateSession()
    }

    fun fetchPerfumes(gender: String? = null) {
        currentGender = gender
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val response = apiService.getAllPerfumes(gender)
                if (response.isSuccessful) {
                    allPerfumes = response.body() ?: emptyList()
                    applyFilters()
                } else { errorMessage = "Lỗi tải dữ liệu" }
            } catch (e: Exception) { errorMessage = e.message }
            finally { isLoading = false }
        }
    }

    fun fetchFavorites() {
        val userId = sessionManager.getUserId()
        if (userId == -1) return

        viewModelScope.launch {
            try {
                val response = apiService.getFavorites(userId)
                if (response.isSuccessful) {
                    favoritePerfumes = response.body() ?: emptyList()
                    favoriteIds = favoritePerfumes.map { it.id }.toSet()
                }
            } catch (e: Exception) { /* Log error */ }
        }
    }

    fun toggleFavorite(perfumeId: Int) {
        val userId = sessionManager.getUserId()
        if (userId == -1) {
            errorMessage = "Vui lòng đăng nhập để yêu thích"
            return
        }

        // Cập nhật giao diện lập tức (Optimistic Update)
        val isCurrentlyFavorite = favoriteIds.contains(perfumeId)
        favoriteIds = if (isCurrentlyFavorite) favoriteIds - perfumeId else favoriteIds + perfumeId

        viewModelScope.launch {
            try {
                val response = if (isCurrentlyFavorite) {
                    apiService.removeFavorite(userId, perfumeId)
                } else {
                    apiService.addFavorite(FavoriteRequest(userId, perfumeId))
                }

                if (response.isSuccessful) {
                    fetchFavorites() // Đồng bộ lại với server
                } else {
                    // Hoàn tác nếu lỗi
                    favoriteIds = if (isCurrentlyFavorite) favoriteIds + perfumeId else favoriteIds - perfumeId
                    errorMessage = "Không thể cập nhật yêu thích"
                }
            } catch (e: Exception) {
                // Hoàn tác nếu lỗi kết nối
                favoriteIds = if (isCurrentlyFavorite) favoriteIds + perfumeId else favoriteIds - perfumeId
                errorMessage = "Lỗi kết nối: ${e.message}"
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        applyFilters()
    }

    fun clearError() {
        errorMessage = null
    }

    private fun applyFilters() {
        filteredPerfumes = if (searchQuery.isBlank()) allPerfumes
        else allPerfumes.filter { 
            it.name.contains(searchQuery, ignoreCase = true) || 
            it.brand.contains(searchQuery, ignoreCase = true)
        }
    }
}
