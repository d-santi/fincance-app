package com.example.financeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.financeapp.domain.model.Budget
import com.example.financeapp.domain.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(budget: Budget): Long

    @Update
    suspend fun update(budget: Budget)

    @Delete
    suspend fun delete(budget: Budget)

    // All budgets the user set for a given month/year
    @Query("SELECT * FROM budget WHERE userId = :userId AND month = :month AND year = :year")
    fun getByMonth(userId: Long, month: Int, year: Int): Flow<List<Budget>>

    // Single budget for a specific category + month (for edit or comparison)
    @Query("SELECT * FROM budget WHERE userId = :userId AND category = :category AND month = :month AND year = :year LIMIT 1")
    suspend fun getByCategoryAndMonth(
        userId: Long,
        category: ExpenseCategory,
        month: Int,
        year: Int
    ): Budget?
}
