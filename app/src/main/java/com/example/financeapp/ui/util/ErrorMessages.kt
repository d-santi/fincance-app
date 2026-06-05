package com.example.financeapp.ui.util

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.financeapp.R
import com.example.financeapp.viewmodel.AuthError
import com.example.financeapp.viewmodel.BudgetFormError
import com.example.financeapp.viewmodel.ExpenseFormError
import com.example.financeapp.viewmodel.SharedExpenseFormError

@StringRes
fun authErrorRes(error: AuthError): Int = when (error) {
    AuthError.EMPTY_FIELDS -> R.string.error_empty_fields
    AuthError.INVALID_EMAIL -> R.string.error_invalid_email
    AuthError.WEAK_PASSWORD -> R.string.error_weak_password
    AuthError.INVALID_CREDENTIALS -> R.string.error_invalid_credentials
    AuthError.EMAIL_ALREADY_EXISTS -> R.string.error_email_exists
    AuthError.UNKNOWN -> R.string.error_unknown
}

@StringRes
fun expenseFormErrorRes(error: ExpenseFormError): Int = when (error) {
    ExpenseFormError.EMPTY_DESCRIPTION -> R.string.error_empty_description
    ExpenseFormError.INVALID_AMOUNT -> R.string.error_invalid_amount
    ExpenseFormError.AMOUNT_TOO_HIGH -> R.string.error_amount_too_high
}

@StringRes
fun budgetFormErrorRes(error: BudgetFormError): Int = when (error) {
    BudgetFormError.EMPTY_LIMIT -> R.string.error_empty_limit
    BudgetFormError.INVALID_LIMIT -> R.string.error_invalid_limit
    BudgetFormError.LIMIT_TOO_HIGH -> R.string.error_limit_too_high
}

@StringRes
fun sharedFormErrorRes(error: SharedExpenseFormError): Int = when (error) {
    SharedExpenseFormError.EMPTY_DESCRIPTION -> R.string.error_empty_description
    SharedExpenseFormError.INVALID_TOTAL_AMOUNT -> R.string.error_invalid_total
    SharedExpenseFormError.NOT_ENOUGH_PARTICIPANTS -> R.string.error_not_enough_participants
    SharedExpenseFormError.EMPTY_PARTICIPANT_NAME -> R.string.error_empty_participant_name
    SharedExpenseFormError.INVALID_PARTICIPANT_AMOUNT -> R.string.error_invalid_participant_amount
    SharedExpenseFormError.AMOUNTS_DO_NOT_MATCH_TOTAL -> R.string.error_amounts_mismatch
}

@Composable
fun authErrorMessage(error: AuthError): String = stringResource(authErrorRes(error))

@Composable
fun expenseFormErrorMessage(error: ExpenseFormError): String = stringResource(expenseFormErrorRes(error))

@Composable
fun budgetFormErrorMessage(error: BudgetFormError): String = stringResource(budgetFormErrorRes(error))

@Composable
fun sharedFormErrorMessage(error: SharedExpenseFormError): String = stringResource(sharedFormErrorRes(error))
