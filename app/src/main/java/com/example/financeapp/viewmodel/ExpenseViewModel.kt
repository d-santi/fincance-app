package com.example.financeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.financeapp.data.local.SessionManager
import com.example.financeapp.data.local.dao.ExpenseDao
import com.example.financeapp.domain.model.Expense
import com.example.financeapp.domain.model.ExpenseCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

sealed class ExpenseFilter {
    object All : ExpenseFilter()
    object Recurring : ExpenseFilter()
    data class ByCategory(val category: ExpenseCategory) : ExpenseFilter()
    data class ByDateRange(val startDate: Long, val endDate: Long) : ExpenseFilter()
}

data class ExpenseFormState(
    val description: String = "",
    val amount: String = "",
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val date: Long = System.currentTimeMillis(),
    val isRecurring: Boolean = false,
    val error: ExpenseFormError? = null,
    val isSaved: Boolean = false
)

enum class ExpenseFormError {
    EMPTY_DESCRIPTION,
    INVALID_AMOUNT,
    AMOUNT_TOO_HIGH
}

class ExpenseViewModel(
    private val expenseDao: ExpenseDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val userId = sessionManager.getUserId()

    private val _filter = MutableStateFlow<ExpenseFilter>(ExpenseFilter.All)
    val activeFilter: StateFlow<ExpenseFilter> = _filter.asStateFlow()

    val expenses: StateFlow<List<Expense>> = _filter
        .flatMapLatest { filter ->
            when (filter) {
                is ExpenseFilter.All -> expenseDao.getAllByUser(userId)
                is ExpenseFilter.Recurring -> expenseDao.getRecurring(userId)
                is ExpenseFilter.ByCategory -> expenseDao.getByCategory(userId, filter.category)
                is ExpenseFilter.ByDateRange -> expenseDao.getByDateRange(userId, filter.startDate, filter.endDate)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val totalThisMonth: StateFlow<Double> = expenseDao
        .getTotalByDateRange(userId, monthStart(), monthEnd())
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0.0)


    private val _formState = MutableStateFlow(ExpenseFormState())
    val formState: StateFlow<ExpenseFormState> = _formState.asStateFlow()

    // List actions

    fun setFilter(filter: ExpenseFilter) = _filter.update { filter }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch { expenseDao.delete(expense) }
    }

    // Form actions

    /** Pre-populates the form for editing an existing expense. */
    fun loadExpenseForEdit(expense: Expense) {
        _formState.update {
            ExpenseFormState(
                description = expense.description,
                amount = expense.amount.toString(),
                category = expense.category,
                date = expense.date,
                isRecurring = expense.isRecurring
            )
        }
    }

    fun updateDescription(value: String) = _formState.update { it.copy(description = value, error = null) }
    fun updateAmount(value: String) = _formState.update { it.copy(amount = value, error = null) }
    fun updateCategory(value: ExpenseCategory) = _formState.update { it.copy(category = value) }
    fun updateDate(value: Long) = _formState.update { it.copy(date = value) }
    fun updateIsRecurring(value: Boolean) = _formState.update { it.copy(isRecurring = value) }

    /**
     * Saves a new expense when [existingId] is 0, or updates an existing one otherwise.
     * On success, sets [ExpenseFormState.isSaved] to true so the UI can navigate back.
     */
    fun saveExpense(existingId: Long = 0L) {
        val state = _formState.value
        val error = validateForm(state)
        if (error != null) {
            _formState.update { it.copy(error = error) }
            return
        }

        val expense = Expense(
            id = existingId,
            description = state.description.trim(),
            amount = state.amount.toDouble(),
            category = state.category,
            date = state.date,
            isRecurring = state.isRecurring,
            userId = userId
        )

        viewModelScope.launch {
            if (existingId == 0L) expenseDao.insert(expense)
            else expenseDao.update(expense)
            _formState.update { it.copy(isSaved = true) }
        }
    }

    /** Call this when opening the form so isSaved is always clean. */
    fun resetForm() = _formState.update { ExpenseFormState() }

    fun clearFormError() = _formState.update { it.copy(error = null) }

    // Validation

    private fun validateForm(state: ExpenseFormState): ExpenseFormError? {
        if (state.description.isBlank()) return ExpenseFormError.EMPTY_DESCRIPTION
        val amount = state.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) return ExpenseFormError.INVALID_AMOUNT
        if (amount > 999_999_999) return ExpenseFormError.AMOUNT_TOO_HIGH
        return null
    }

    // Date helpers

    private fun monthStart(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun monthEnd(): Long = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.timeInMillis

    companion object {
        fun factory(
            expenseDao: ExpenseDao,
            sessionManager: SessionManager
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { ExpenseViewModel(expenseDao, sessionManager) }
        }
    }
}
