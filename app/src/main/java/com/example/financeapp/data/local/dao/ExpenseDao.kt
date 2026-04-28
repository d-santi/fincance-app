package com.example.financeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.financeapp.domain.model.Expense
import com.example.financeapp.domain.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    fun getAllByUser(userId: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND category = :category ORDER BY date DESC")
    fun getByCategory(userId: Long, category: ExpenseCategory): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getByDateRange(userId: Long, startDate: Long, endDate: Long): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND isRecurring = 1 ORDER BY date DESC")
    fun getRecurring(userId: Long): Flow<List<Expense>>

    // Useful for dashboard: total spent in a date range
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate")
    fun getTotalByDateRange(userId: Long, startDate: Long, endDate: Long): Flow<Double?>

    // Useful for budget vs actual comparison per category
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND category = :category AND date BETWEEN :startDate AND :endDate")
    fun getTotalByCategoryAndDateRange(
        userId: Long,
        category: ExpenseCategory,
        startDate: Long,
        endDate: Long
    ): Flow<Double?>
}
