package com.example.financeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.financeapp.data.local.SessionManager
import com.example.financeapp.data.local.dao.BudgetDao
import com.example.financeapp.data.local.dao.ExpenseDao
import com.example.financeapp.domain.model.Budget
import com.example.financeapp.domain.model.ExpenseCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

// ── Supporting models ─────────────────────────────────────────────────────────

data class YearMonth(val month: Int, val year: Int)

data class BudgetSummary(
    val budget: Budget,
    val spent: Double
) {
    val remaining: Double get() = budget.monthlyLimit - spent
    val progress: Float get() = (spent / budget.monthlyLimit).toFloat().coerceIn(0f, 1f)
    val isOverBudget: Boolean get() = spent > budget.monthlyLimit
}

// ── Form state ────────────────────────────────────────────────────────────────

data class BudgetFormState(
    val monthlyLimit: String = "",
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val error: BudgetFormError? = null,
    val isSaved: Boolean = false
)

enum class BudgetFormError {
    EMPTY_LIMIT,
    INVALID_LIMIT,
    LIMIT_TOO_HIGH
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class BudgetViewModel(
    private val budgetDao: BudgetDao,
    private val expenseDao: ExpenseDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val userId = sessionManager.getUserId()

    // ── Month selector ────────────────────────────────────────────────────

    private val _selectedYearMonth = MutableStateFlow(currentYearMonth())
    val selectedYearMonth: StateFlow<YearMonth> = _selectedYearMonth.asStateFlow()

    // ── Budget vs actual (reactive) ───────────────────────────────────────

    /**
     * For the selected month, combines the list of budgets with the actual
     * spending per category in a single reactive stream.
     * Emits a new list whenever budgets, expenses, or the selected month change.
     */
    val budgetSummaries: StateFlow<List<BudgetSummary>> = _selectedYearMonth
        .flatMapLatest { ym ->
            val (start, end) = monthBounds(ym.month, ym.year)
            combine(
                budgetDao.getByMonth(userId, ym.month, ym.year),
                expenseDao.getTotalsByCategoryForMonth(userId, start, end)
            ) { budgets, categoryTotals ->
                val totalsMap = categoryTotals.associateBy { it.category }
                budgets.map { budget ->
                    val spent = totalsMap[budget.category]?.total ?: 0.0
                    BudgetSummary(budget, spent)
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Form ──────────────────────────────────────────────────────────────

    private val _formState = MutableStateFlow(BudgetFormState())
    val formState: StateFlow<BudgetFormState> = _formState.asStateFlow()

    // ── Month navigation ──────────────────────────────────────────────────

    fun goToPreviousMonth() {
        _selectedYearMonth.update { ym ->
            if (ym.month == 1) YearMonth(12, ym.year - 1)
            else YearMonth(ym.month - 1, ym.year)
        }
    }

    fun goToNextMonth() {
        _selectedYearMonth.update { ym ->
            if (ym.month == 12) YearMonth(1, ym.year + 1)
            else YearMonth(ym.month + 1, ym.year)
        }
    }

    // ── List actions ──────────────────────────────────────────────────────

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch { budgetDao.delete(budget) }
    }

    // ── Form actions ──────────────────────────────────────────────────────

    /** Pre-populates the form when the user taps edit on an existing budget. */
    fun loadBudgetForEdit(budget: Budget) {
        _selectedYearMonth.value = YearMonth(budget.month, budget.year)
        _formState.update {
            BudgetFormState(
                monthlyLimit = budget.monthlyLimit.toString(),
                category = budget.category
            )
        }
    }

    fun loadBudgetForEdit(id: Long) {
        if (id == 0L) return
        viewModelScope.launch {
            budgetDao.getById(id, userId)?.let { loadBudgetForEdit(it) }
        }
    }

    fun updateMonthlyLimit(value: String) = _formState.update { it.copy(monthlyLimit = value, error = null) }
    fun updateCategory(value: ExpenseCategory) = _formState.update { it.copy(category = value) }

    /**
     * Saves the budget for the selected month. If a budget for that category
     * already exists, it updates it — enforcing one budget per category/month.
     */
    fun saveBudget() {
        val state = _formState.value
        val error = validateForm(state)
        if (error != null) {
            _formState.update { it.copy(error = error) }
            return
        }

        val ym = _selectedYearMonth.value
        viewModelScope.launch {
            val existing = budgetDao.getByCategoryAndMonth(userId, state.category, ym.month, ym.year)
            val budget = Budget(
                id = existing?.id ?: 0L,
                monthlyLimit = state.monthlyLimit.toDouble(),
                category = state.category,
                month = ym.month,
                year = ym.year,
                userId = userId
            )
            if (existing != null) budgetDao.update(budget)
            else budgetDao.insert(budget)
            _formState.update { it.copy(isSaved = true) }
        }
    }

    fun resetForm() = _formState.update { BudgetFormState(isSaved = false) }
    fun clearFormError() = _formState.update { it.copy(error = null) }

    // ── Validation ────────────────────────────────────────────────────────

    private fun validateForm(state: BudgetFormState): BudgetFormError? {
        if (state.monthlyLimit.isBlank()) return BudgetFormError.EMPTY_LIMIT
        val limit = state.monthlyLimit.toDoubleOrNull()
        if (limit == null || limit <= 0) return BudgetFormError.INVALID_LIMIT
        if (limit > 999_999_999) return BudgetFormError.LIMIT_TOO_HIGH
        return null
    }

    // ── Helpers ───────────────────────────────────────────────────────────

    private fun currentYearMonth(): YearMonth {
        val cal = Calendar.getInstance()
        return YearMonth(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR))
    }

    private fun monthBounds(month: Int, year: Int): Pair<Long, Long> {
        val cal = Calendar.getInstance().apply { set(year, month - 1, 1) }

        val start = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val end = (cal.clone() as Calendar).apply {
            set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis

        return start to end
    }

    companion object {
        fun factory(
            budgetDao: BudgetDao,
            expenseDao: ExpenseDao,
            sessionManager: SessionManager
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { BudgetViewModel(budgetDao, expenseDao, sessionManager) }
        }
    }
}
