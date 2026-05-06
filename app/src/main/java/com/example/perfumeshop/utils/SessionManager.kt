package com.example.perfumeshop.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    fun saveSession(userId: Int, role: String, fullName: String) {
        prefs.edit().apply {
            putInt("user_id", userId)
            putString("role", role)
            putString("full_name", fullName)
            putBoolean("is_logged_in", true)
            apply()
        }
    }

    fun getUserId(): Int = prefs.getInt("user_id", -1)
    fun getRole(): String? = prefs.getString("role", null)
    fun getFullName(): String? = prefs.getString("full_name", null)
    fun isLoggedIn(): Boolean = prefs.getBoolean("is_logged_in", false)

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
