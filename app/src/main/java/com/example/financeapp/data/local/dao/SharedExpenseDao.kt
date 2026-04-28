package com.example.financeapp.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.financeapp.domain.model.SharedExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(sharedExpense: SharedExpense): Long

    @Update
    suspend fun update(sharedExpense: SharedExpense)

    @Delete
    suspend fun delete(sharedExpense: SharedExpense)

    @Query("SELECT * FROM shared_expenses WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllByUser(userId: Long): Flow<List<SharedExpense>>

    @Query("SELECT * FROM shared_expenses WHERE userId = :userId AND settled = 0 ORDER BY createdAt DESC")
    fun getUnsettled(userId: Long): Flow<List<SharedExpense>>

    @Query("UPDATE shared_expenses SET settled = 1 WHERE id = :id")
    suspend fun markAsSettled(id: Long)
}
