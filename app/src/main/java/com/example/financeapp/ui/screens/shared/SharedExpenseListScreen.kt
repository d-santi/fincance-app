package com.example.financeapp.ui.screens.shared

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Group
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
import com.example.financeapp.domain.model.SharedExpense
import com.example.financeapp.ui.components.ConfirmDialog
import com.example.financeapp.ui.components.EmptyState
import com.example.financeapp.ui.components.FilterChipItem
import com.example.financeapp.ui.components.FilterChipRow
import com.example.financeapp.ui.components.SharedExpenseCard
import com.example.financeapp.viewmodel.SharedExpenseFilter
import com.example.financeapp.viewmodel.SharedExpenseViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedExpenseListScreen(
    viewModel: SharedExpenseViewModel,
    onAddSharedExpense: () -> Unit,
    onEditSharedExpense: (Long) -> Unit
) {
    val sharedExpenses by viewModel.sharedExpenses.collectAsStateWithLifecycle()
    val activeFilter by viewModel.activeFilter.collectAsStateWithLifecycle()
    var itemToDelete by remember { mutableStateOf<SharedExpense?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(R.string.shared_deleted)
    val settledMessage = stringResource(R.string.shared_settled)

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.shared_title)) })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSharedExpense,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_shared))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            FilterChipRow(
                chips = listOf(
                    FilterChipItem(
                        label = stringResource(R.string.filter_all),
                        selected = activeFilter is SharedExpenseFilter.All,
                        onClick = { viewModel.setFilter(SharedExpenseFilter.All) }
                    ),
                    FilterChipItem(
                        label = stringResource(R.string.filter_unsettled),
                        selected = activeFilter is SharedExpenseFilter.Unsettled,
                        onClick = { viewModel.setFilter(SharedExpenseFilter.Unsettled) }
                    )
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (sharedExpenses.isEmpty()) {
                val isUnsettledFilter = activeFilter is SharedExpenseFilter.Unsettled
                EmptyState(
                    icon = Icons.Outlined.Group,
                    title = stringResource(
                        if (isUnsettledFilter) R.string.empty_shared_unsettled_title
                        else R.string.empty_shared_title
                    ),
                    subtitle = stringResource(
                        if (isUnsettledFilter) R.string.empty_shared_unsettled_subtitle
                        else R.string.empty_shared_subtitle
                    ),
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(sharedExpenses, key = { it.id }) { item ->
                        SharedExpenseCard(
                            sharedExpense = item,
                            onClick = { onEditSharedExpense(item.id) },
                            onDelete = { itemToDelete = item },
                            onSettle = {
                                viewModel.markAsSettled(item.id)
                                scope.launch { snackbarHostState.showSnackbar(settledMessage) }
                            },
                            onToggleParticipantPaid = { participant ->
                                viewModel.markParticipantAsPaid(item, participant.name)
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
    }

    itemToDelete?.let { item ->
        ConfirmDialog(
            message = stringResource(R.string.confirm_delete_shared),
            onConfirm = {
                viewModel.deleteSharedExpense(item)
                itemToDelete = null
                scope.launch { snackbarHostState.showSnackbar(deletedMessage) }
            },
            onDismiss = { itemToDelete = null }
        )
    }
}
