package com.example.financeapp.ui.screens.expense

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financeapp.R
import com.example.financeapp.domain.model.Expense
import com.example.financeapp.domain.model.ExpenseCategory
import com.example.financeapp.ui.components.ConfirmDialog
import com.example.financeapp.ui.components.EmptyState
import com.example.financeapp.ui.components.ExpenseListItem
import com.example.financeapp.ui.components.FilterChipItem
import com.example.financeapp.ui.components.FilterChipRow
import com.example.financeapp.ui.components.FinanceCard
import com.example.financeapp.ui.components.FinanceDatePickerDialog
import com.example.financeapp.ui.util.endOfDay
import com.example.financeapp.ui.util.formatCurrency
import com.example.financeapp.ui.util.startOfDay
import com.example.financeapp.viewmodel.ExpenseFilter
import com.example.financeapp.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseListScreen(
    viewModel: ExpenseViewModel,
    onAddExpense: () -> Unit,
    onEditExpense: (Long) -> Unit,
    onLogout: () -> Unit
) {
    val expenses by viewModel.expenses.collectAsStateWithLifecycle()
    val totalThisMonth by viewModel.totalThisMonth.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()

    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showDateRangePicker by remember { mutableStateOf(false) }
    var rangeStart by remember { mutableStateOf(System.currentTimeMillis()) }
    var rangeEnd by remember { mutableStateOf(System.currentTimeMillis()) }
    var pickingStart by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.expense_deleted)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.expenses_title)) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = stringResource(R.string.logout))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_expense))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FinanceCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = stringResource(R.string.total_this_month),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(totalThisMonth),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            FilterChipRow(
                chips = listOf(
                    FilterChipItem(
                        label = stringResource(R.string.filter_all),
                        selected = activeFilter is ExpenseFilter.All,
                        onClick = { viewModel.setFilter(ExpenseFilter.All) }
                    ),
                    FilterChipItem(
                        label = stringResource(R.string.filter_recurring),
                        selected = activeFilter is ExpenseFilter.Recurring,
                        onClick = { viewModel.setFilter(ExpenseFilter.Recurring) }
                    ),
                    FilterChipItem(
                        label = stringResource(R.string.filter_category),
                        selected = activeFilter is ExpenseFilter.ByCategory,
                        onClick = { showCategoryPicker = true }
                    ),
                    FilterChipItem(
                        label = stringResource(R.string.filter_date_range),
                        selected = activeFilter is ExpenseFilter.ByDateRange,
                        onClick = { showDateRangePicker = true }
                    )
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            if (expenses.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.Receipt,
                    title = stringResource(R.string.empty_expenses_title),
                    subtitle = stringResource(R.string.empty_expenses_subtitle),
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(expenses, key = { it.id }) { expense ->
                        ExpenseListItem(
                            expense = expense,
                            onClick = { onEditExpense(expense.id) },
                            onDelete = { expenseToDelete = expense }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    expenseToDelete?.let { expense ->
        ConfirmDialog(
            message = stringResource(R.string.confirm_delete_expense),
            onConfirm = {
                viewModel.deleteExpense(expense)
                expenseToDelete = null
                scope.launch { snackbarHostState.showSnackbar(deletedMessage) }
            },
            onDismiss = { expenseToDelete = null }
        )
    }

    if (showCategoryPicker) {
        CategoryPickerDialog(
            onSelect = { category ->
                viewModel.setFilter(ExpenseFilter.ByCategory(category))
                showCategoryPicker = false
            },
            onDismiss = { showCategoryPicker = false }
        )
    }

    if (showDateRangePicker) {
        FinanceDatePickerDialog(
            initialSelectedDateMillis = if (pickingStart) rangeStart else rangeEnd,
            onConfirm = { millis ->
                if (pickingStart) {
                    rangeStart = startOfDay(millis)
                    pickingStart = false
                } else {
                    rangeEnd = endOfDay(millis)
                    viewModel.setFilter(ExpenseFilter.ByDateRange(rangeStart, rangeEnd))
                    showDateRangePicker = false
                    pickingStart = true
                }
            },
            onDismiss = {
                showDateRangePicker = false
                pickingStart = true
            }
        )
    }
}

@Composable
private fun CategoryPickerDialog(
    onSelect: (ExpenseCategory) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.select_category)) },
        text = {
            Column {
                ExpenseCategory.entries.forEach { category ->
                    TextButton(
                        onClick = { onSelect(category) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(category.displayName)
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
