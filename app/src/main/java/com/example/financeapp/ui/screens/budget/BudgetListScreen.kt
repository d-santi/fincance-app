package com.example.financeapp.ui.screens.budget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import com.example.financeapp.ui.components.BudgetSummaryCard
import com.example.financeapp.ui.components.ConfirmDialog
import com.example.financeapp.ui.components.EmptyState
import com.example.financeapp.ui.components.MonthSelector
import com.example.financeapp.viewmodel.BudgetSummary
import com.example.financeapp.viewmodel.BudgetViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetListScreen(
    viewModel: BudgetViewModel,
    onAddBudget: () -> Unit,
    onEditBudget: (Long) -> Unit
) {
    val summaries by viewModel.budgetSummaries.collectAsStateWithLifecycle()
    val selectedYearMonth by viewModel.selectedYearMonth.collectAsStateWithLifecycle()
    var budgetToDelete by remember { mutableStateOf<BudgetSummary?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.budget_deleted)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.budget_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBudget,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_budget))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MonthSelector(
                yearMonth = selectedYearMonth,
                onPrevious = viewModel::goToPreviousMonth,
                onNext = viewModel::goToNextMonth,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            if (summaries.isEmpty()) {
                EmptyState(
                    icon = Icons.Outlined.AccountBalance,
                    title = stringResource(R.string.empty_budget_title),
                    subtitle = stringResource(R.string.empty_budget_subtitle),
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(summaries, key = { it.budget.id }) { summary ->
                        BudgetSummaryCard(
                            summary = summary,
                            onClick = { onEditBudget(summary.budget.id) },
                            onDelete = { budgetToDelete = summary }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    budgetToDelete?.let { summary ->
        ConfirmDialog(
            message = stringResource(R.string.confirm_delete_budget),
            onConfirm = {
                viewModel.deleteBudget(summary.budget)
                budgetToDelete = null
                scope.launch { snackbarHostState.showSnackbar(deletedMessage) }
            },
            onDismiss = { budgetToDelete = null }
        )
    }
}
