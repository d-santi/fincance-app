package com.example.financeapp.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val amount: Double,
    val category: ExpenseCategory,
    val date: Long = System.currentTimeMillis(),
    val isRecurring: Boolean,
    val userId: Long
)
