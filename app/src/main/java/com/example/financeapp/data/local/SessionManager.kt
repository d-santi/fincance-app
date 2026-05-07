package com.example.financeapp.data.local

import android.content.Context

class SessionManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveUserId(userId: Long) = prefs.edit().putLong(KEY_USER_ID, userId).apply()

    fun getUserId(): Long = prefs.getLong(KEY_USER_ID, NO_USER)

    fun clearSession() = prefs.edit().remove(KEY_USER_ID).apply()

    fun isLoggedIn(): Boolean = getUserId() != NO_USER

    companion object {
        private const val PREFS_NAME = "finance_session"
        private const val KEY_USER_ID = "user_id"
        const val NO_USER = -1L
    }
}
