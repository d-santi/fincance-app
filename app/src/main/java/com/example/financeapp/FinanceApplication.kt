package com.example.financeapp

import android.app.Application
import com.example.financeapp.data.local.AppDatabase
import com.example.financeapp.data.local.SessionManager

class FinanceApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val sessionManager by lazy { SessionManager(this) }
}
