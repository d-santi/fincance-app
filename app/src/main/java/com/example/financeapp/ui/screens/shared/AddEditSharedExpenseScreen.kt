package com.example.financeapp.ui.screens.shared

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financeapp.R
import com.example.financeapp.ui.components.CategoryDropdown
import com.example.financeapp.ui.components.ErrorBanner
import com.example.financeapp.ui.components.FinancePrimaryButton
import com.example.financeapp.ui.components.FinanceTextButton
import com.example.financeapp.ui.components.FinanceTextField
import com.example.financeapp.ui.components.MoneyTextField
import com.example.financeapp.ui.util.sharedFormErrorMessage
import com.example.financeapp.viewmodel.SharedExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSharedExpenseScreen(
    sharedExpenseId: Long,
    viewModel: SharedExpenseViewModel,
    onNavigateBack: () -> Unit
) {
    val isEditing = sharedExpenseId != 0L
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    LaunchedEffect(sharedExpenseId) {
        if (sharedExpenseId > 0L) viewModel.loadSharedExpenseForEdit(sharedExpenseId)
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
                        if (isEditing) stringResource(R.string.shared_edit)
                        else stringResource(R.string.shared_new)
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
                    message = sharedFormErrorMessage(error),
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
                value = formState.totalAmount,
                onValueChange = viewModel::updateTotalAmount,
                label = stringResource(R.string.total_amount)
            )
            Spacer(modifier = Modifier.height(12.dp))
            CategoryDropdown(
                selected = formState.category,
                onSelected = viewModel::updateCategory
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.participants),
                style = MaterialTheme.typography.titleSmall
            )
            Spacer(modifier = Modifier.height(8.dp))

            formState.participants.forEachIndexed { index, participant ->
                ParticipantFormRow(
                    name = participant.name,
                    amount = participant.amount,
                    paid = participant.paid,
                    canRemove = formState.participants.size > 2,
                    onNameChange = { viewModel.updateParticipantName(index, it) },
                    onAmountChange = { viewModel.updateParticipantAmount(index, it) },
                    onPaidChange = { viewModel.toggleParticipantPaid(index) },
                    onRemove = { viewModel.removeParticipant(index) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            FinanceTextButton(
                text = stringResource(R.string.add_participant),
                onClick = viewModel::addParticipant
            )
            Spacer(modifier = Modifier.height(24.dp))
            FinancePrimaryButton(
                text = stringResource(R.string.save),
                onClick = { viewModel.saveSharedExpense(sharedExpenseId) }
            )
        }
    }
}

@Composable
private fun ParticipantFormRow(
    name: String,
    amount: String,
    paid: Boolean,
    canRemove: Boolean,
    onNameChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onPaidChange: () -> Unit,
    onRemove: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = paid, onCheckedChange = { onPaidChange() })
                Text(
                    text = stringResource(R.string.paid),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            if (canRemove) {
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.remove_participant))
                }
            }
        }
        FinanceTextField(
            value = name,
            onValueChange = onNameChange,
            label = stringResource(R.string.participant_name)
        )
        Spacer(modifier = Modifier.height(8.dp))
        MoneyTextField(
            value = amount,
            onValueChange = onAmountChange,
            label = stringResource(R.string.participant_amount)
        )
    }
}
