package com.example.financeapp.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budget")
data class Budget(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val monthlyLimit: Double,
    val month: Int,
    val year: Int,
    val category: ExpenseCategory,
    val userId: Long,
    val createdAt: Long = System.currentTimeMillis()
)