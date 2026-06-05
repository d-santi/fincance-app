package com.example.financeapp.ui.screens.budget

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.financeapp.R
import com.example.financeapp.ui.components.CategoryDropdown
import com.example.financeapp.ui.components.ErrorBanner
import com.example.financeapp.ui.components.FinancePrimaryButton
import com.example.financeapp.ui.components.MoneyTextField
import com.example.financeapp.ui.util.budgetFormErrorMessage
import com.example.financeapp.viewmodel.BudgetViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBudgetScreen(
    budgetId: Long,
    viewModel: BudgetViewModel,
    onNavigateBack: () -> Unit
) {
    val isEditing = budgetId != 0L
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    LaunchedEffect(budgetId) {
        if (budgetId > 0L) viewModel.loadBudgetForEdit(budgetId)
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
                        if (isEditing) stringResource(R.string.budget_edit)
                        else stringResource(R.string.budget_new)
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
                    message = budgetFormErrorMessage(error),
                    modifier = Modifier.padding(bottom = 12.dp)
                )
            }

            MoneyTextField(
                value = formState.monthlyLimit,
                onValueChange = viewModel::updateMonthlyLimit,
                label = stringResource(R.string.monthly_limit)
            )
            Spacer(modifier = Modifier.height(12.dp))
            CategoryDropdown(
                selected = formState.category,
                onSelected = viewModel::updateCategory
            )
            Spacer(modifier = Modifier.height(24.dp))
            FinancePrimaryButton(
                text = stringResource(R.string.save),
                onClick = { viewModel.saveBudget() }
            )
        }
    }
}
