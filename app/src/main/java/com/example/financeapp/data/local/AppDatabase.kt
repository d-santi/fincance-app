package com.example.financeapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.financeapp.data.local.converter.Converters
import com.example.financeapp.data.local.dao.BudgetDao
import com.example.financeapp.data.local.dao.ExpenseDao
import com.example.financeapp.data.local.dao.SharedExpenseDao
import com.example.financeapp.data.local.dao.UserDao
import com.example.financeapp.domain.model.Budget
import com.example.financeapp.domain.model.Expense
import com.example.financeapp.domain.model.SharedExpense
import com.example.financeapp.domain.model.User

@Database(
    entities = [User::class, Expense::class, Budget::class, SharedExpense::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun budgetDao(): BudgetDao
    abstract fun sharedExpenseDao(): SharedExpenseDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "finance_app.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
