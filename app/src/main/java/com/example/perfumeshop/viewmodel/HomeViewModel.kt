package com.example.perfumeshop.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.perfumeshop.api.RetrofitClient
import com.example.perfumeshop.model.Perfume
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    var allPerfumes by mutableStateOf<List<Perfume>>(emptyList())
        private set
    
    var filteredPerfumes by mutableStateOf<List<Perfume>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var currentGender by mutableStateOf<String?>(null)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var favorites by mutableStateOf<Set<Int>>(emptySet())
        private set

    init {
        fetchPerfumes()
    }

    fun fetchPerfumes(gender: String? = null) {
        currentGender = gender
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getAllPerfumes(gender)
                if (response.isSuccessful) {
                    allPerfumes = response.body() ?: emptyList()
                    applyFilters()
                } else {
                    errorMessage = "Lỗi tải dữ liệu: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Lỗi kết nối: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        applyFilters()
    }

    private fun applyFilters() {
        filteredPerfumes = if (searchQuery.isBlank()) {
            allPerfumes
        } else {
            // Sử dụng bộ lọc thông minh hơn để hỗ trợ tiếng Việt có dấu
            allPerfumes.filter { 
                it.name.contains(searchQuery, ignoreCase = true) || 
                it.brand.contains(searchQuery, ignoreCase = true) ||
                it.price.toString().contains(searchQuery)
            }
        }
    }

    fun toggleFavorite(perfumeId: Int) {
        favorites = if (favorites.contains(perfumeId)) {
            favorites - perfumeId
        } else {
            favorites + perfumeId
        }
    }
}
