package com.example.financeapp.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shared_expenses")
data class SharedExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: Long,
    val description: String,
    val totalAmount: Double,
    val category: ExpenseCategory,
    val settled: Boolean = false,
    val participants: String,
    val createdAt: Long = System.currentTimeMillis()
)

data class Participant(
    val name: String,
    val amount: Double,
    val paid: Boolean =  false
)