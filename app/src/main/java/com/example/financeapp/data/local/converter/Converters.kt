package com.example.financeapp.data.local.converter

import androidx.room.TypeConverter
import com.example.financeapp.domain.model.ExpenseCategory

class Converters {
    @TypeConverter
    fun fromExpenseCategory(category: ExpenseCategory): String = category.name

    @TypeConverter
    fun toExpenseCategory(value: String): ExpenseCategory = ExpenseCategory.valueOf(value)
}
