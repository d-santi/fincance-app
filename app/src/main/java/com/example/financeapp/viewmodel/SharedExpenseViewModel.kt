package com.example.financeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.financeapp.data.local.SessionManager
import com.example.financeapp.data.local.dao.SharedExpenseDao
import com.example.financeapp.domain.model.ExpenseCategory
import com.example.financeapp.domain.model.Participant
import com.example.financeapp.domain.model.SharedExpense
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs

// ── Filter ────────────────────────────────────────────────────────────────────

sealed class SharedExpenseFilter {
    object All : SharedExpenseFilter()
    object Unsettled : SharedExpenseFilter()
}

// ── Form state ────────────────────────────────────────────────────────────────

data class ParticipantFormEntry(
    val name: String = "",
    val amount: String = "",
    val paid: Boolean = false
)

data class SharedExpenseFormState(
    val description: String = "",
    val totalAmount: String = "",
    val category: ExpenseCategory = ExpenseCategory.OTHER,
    val participants: List<ParticipantFormEntry> = listOf(ParticipantFormEntry(), ParticipantFormEntry()),
    val error: SharedExpenseFormError? = null,
    val isSaved: Boolean = false
)

enum class SharedExpenseFormError {
    EMPTY_DESCRIPTION,
    INVALID_TOTAL_AMOUNT,
    NOT_ENOUGH_PARTICIPANTS,
    EMPTY_PARTICIPANT_NAME,
    INVALID_PARTICIPANT_AMOUNT,
    AMOUNTS_DO_NOT_MATCH_TOTAL
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class SharedExpenseViewModel(
    private val sharedExpenseDao: SharedExpenseDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val userId = sessionManager.getUserId()

    // ── List state ────────────────────────────────────────────────────────

    private val _filter = MutableStateFlow<SharedExpenseFilter>(SharedExpenseFilter.All)
    val activeFilter: StateFlow<SharedExpenseFilter> = _filter.asStateFlow()

    val sharedExpenses: StateFlow<List<SharedExpense>> = _filter
        .flatMapLatest { filter ->
            when (filter) {
                is SharedExpenseFilter.All -> sharedExpenseDao.getAllByUser(userId)
                is SharedExpenseFilter.Unsettled -> sharedExpenseDao.getUnsettled(userId)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ── Form state ────────────────────────────────────────────────────────

    private val _formState = MutableStateFlow(SharedExpenseFormState())
    val formState: StateFlow<SharedExpenseFormState> = _formState.asStateFlow()

    // ── List actions ──────────────────────────────────────────────────────

    fun setFilter(filter: SharedExpenseFilter) = _filter.update { filter }

    fun deleteSharedExpense(sharedExpense: SharedExpense) {
        viewModelScope.launch { sharedExpenseDao.delete(sharedExpense) }
    }

    fun markAsSettled(id: Long) {
        viewModelScope.launch { sharedExpenseDao.markAsSettled(id) }
    }

    /** Marks a single participant as paid without settling the whole expense. */
    fun markParticipantAsPaid(sharedExpense: SharedExpense, participantName: String) {
        viewModelScope.launch {
            val updated = sharedExpense.copy(
                participants = sharedExpense.participants.map { p ->
                    if (p.name == participantName) p.copy(paid = true) else p
                }
            )
            sharedExpenseDao.update(updated)
        }
    }

    // ── Form actions ──────────────────────────────────────────────────────

    /** Pre-populates the form for editing an existing shared expense. */
    fun loadSharedExpenseForEdit(sharedExpense: SharedExpense) {
        _formState.update {
            SharedExpenseFormState(
                description = sharedExpense.description,
                totalAmount = sharedExpense.totalAmount.toString(),
                category = sharedExpense.category,
                participants = sharedExpense.participants.map { p ->
                    ParticipantFormEntry(name = p.name, amount = p.amount.toString(), paid = p.paid)
                }
            )
        }
    }

    fun updateDescription(value: String) = _formState.update { it.copy(description = value, error = null) }
    fun updateTotalAmount(value: String) = _formState.update { it.copy(totalAmount = value, error = null) }
    fun updateCategory(value: ExpenseCategory) = _formState.update { it.copy(category = value) }

    fun updateParticipantName(index: Int, name: String) {
        _formState.update { state ->
            state.copy(
                participants = state.participants.mapIndexed { i, p ->
                    if (i == index) p.copy(name = name) else p
                },
                error = null
            )
        }
    }

    fun updateParticipantAmount(index: Int, amount: String) {
        _formState.update { state ->
            state.copy(
                participants = state.participants.mapIndexed { i, p ->
                    if (i == index) p.copy(amount = amount) else p
                },
                error = null
            )
        }
    }

    fun toggleParticipantPaid(index: Int) {
        _formState.update { state ->
            state.copy(
                participants = state.participants.mapIndexed { i, p ->
                    if (i == index) p.copy(paid = !p.paid) else p
                }
            )
        }
    }

    fun addParticipant() {
        _formState.update { it.copy(participants = it.participants + ParticipantFormEntry()) }
    }

    /** Removes a participant. Enforces a minimum of 2 participants. */
    fun removeParticipant(index: Int) {
        if (_formState.value.participants.size <= 2) return
        _formState.update { state ->
            state.copy(participants = state.participants.filterIndexed { i, _ -> i != index })
        }
    }

    /**
     * Saves a new shared expense when [existingId] is 0, or updates an existing one otherwise.
     * Validates that all participant amounts sum to the declared total.
     */
    fun saveSharedExpense(existingId: Long = 0L) {
        val state = _formState.value
        val error = validateForm(state)
        if (error != null) {
            _formState.update { it.copy(error = error) }
            return
        }

        val participants = state.participants.map { entry ->
            Participant(name = entry.name.trim(), amount = entry.amount.toDouble(), paid = entry.paid)
        }

        val sharedExpense = SharedExpense(
            id = existingId,
            userId = userId,
            description = state.description.trim(),
            totalAmount = state.totalAmount.toDouble(),
            category = state.category,
            participants = participants,
            settled = false
        )

        viewModelScope.launch {
            if (existingId == 0L) sharedExpenseDao.insert(sharedExpense)
            else sharedExpenseDao.update(sharedExpense)
            _formState.update { it.copy(isSaved = true) }
        }
    }

    fun resetForm() = _formState.update { SharedExpenseFormState() }
    fun clearFormError() = _formState.update { it.copy(error = null) }

    // ── Validation ────────────────────────────────────────────────────────

    private fun validateForm(state: SharedExpenseFormState): SharedExpenseFormError? {
        if (state.description.isBlank()) return SharedExpenseFormError.EMPTY_DESCRIPTION

        val total = state.totalAmount.toDoubleOrNull()
        if (total == null || total <= 0) return SharedExpenseFormError.INVALID_TOTAL_AMOUNT

        if (state.participants.size < 2) return SharedExpenseFormError.NOT_ENOUGH_PARTICIPANTS

        if (state.participants.any { it.name.isBlank() }) return SharedExpenseFormError.EMPTY_PARTICIPANT_NAME

        val amounts = state.participants.map { it.amount.toDoubleOrNull() }
        if (amounts.any { it == null || it <= 0 }) return SharedExpenseFormError.INVALID_PARTICIPANT_AMOUNT

        val sum = amounts.filterNotNull().sum()
        if (abs(sum - total) > 0.01) return SharedExpenseFormError.AMOUNTS_DO_NOT_MATCH_TOTAL

        return null
    }

    companion object {
        fun factory(
            sharedExpenseDao: SharedExpenseDao,
            sessionManager: SessionManager
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer { SharedExpenseViewModel(sharedExpenseDao, sessionManager) }
        }
    }
}
