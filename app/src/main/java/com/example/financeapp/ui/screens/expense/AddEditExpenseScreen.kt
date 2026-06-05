package com.example.financeapp.ui.screens.expense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financeapp.R
import com.example.financeapp.ui.components.CategoryDropdown
import com.example.financeapp.ui.components.ErrorBanner
import com.example.financeapp.ui.components.FinanceDatePickerDialog
import com.example.financeapp.ui.components.FinancePrimaryButton
import com.example.financeapp.ui.components.FinanceTextField
import com.example.financeapp.ui.components.MoneyTextField
import com.example.financeapp.ui.util.expenseFormErrorMessage
import com.example.financeapp.ui.util.formatDate
import com.example.financeapp.viewmodel.ExpenseFormError
import com.example.financeapp.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    expenseId: Long,
    viewModel: ExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val isEditing = expenseId != 0L
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(expenseId) {
        if (expenseId > 0L) viewModel.loadExpenseForEdit(expenseId)
        else viewModel.resetForm()
    }

    LaunchedEffect(formState.isSaved) {
        if (formState.isSaved) onNavigateBack()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditing) stringResource(R.string.expense_edit)
                        else stringResource(R.string.expense_new)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            formState.error?.let { error ->
                ErrorBanner(
                    message = expenseFormErrorMessage(error),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            FinanceTextField(
                value = formState.description,
                onValueChange = viewModel::updateDescription,
                label = stringResource(R.string.description)
            )
            Spacer(modifier = Modifier.height(12.dp))
            MoneyTextField(
                value = formState.amount,
                onValueChange = viewModel::updateAmount,
                label = stringResource(R.string.amount)
            )
            Spacer(modifier = Modifier.height(12.dp))
            CategoryDropdown(
                selected = formState.category,
                onSelected = viewModel::updateCategory
            )
            Spacer(modifier = Modifier.height(12.dp))
            FinanceTextField(
                value = formatDate(formState.date),
                onValueChange = {},
                label = stringResource(R.string.date),
                enabled = false
            )
            TextButtonDate(onClick = { showDatePicker = true })
            Spacer(modifier = Modifier.height(12.dp))
            RowSwitch(
                label = stringResource(R.string.recurring),
                checked = formState.isRecurring,
                onCheckedChange = viewModel::updateIsRecurring
            )
            Spacer(modifier = Modifier.height(24.dp))
            FinancePrimaryButton(
                text = stringResource(R.string.save),
                onClick = { viewModel.saveExpense(expenseId) }
            )
        }
    }

    if (showDatePicker) {
        FinanceDatePickerDialog(
            initialSelectedDateMillis = formState.date,
            onConfirm = { viewModel.updateDate(it) },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun TextButtonDate(onClick: () -> Unit) {
    androidx.compose.material3.TextButton(onClick = onClick) {
        Text(stringResource(R.string.date), color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun RowSwitch(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
