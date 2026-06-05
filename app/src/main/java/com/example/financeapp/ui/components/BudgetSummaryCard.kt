package com.example.financeapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.financeapp.R
import com.example.financeapp.ui.theme.OverBudget
import com.example.financeapp.ui.theme.Primary
import com.example.financeapp.ui.theme.ProgressTrack
import com.example.financeapp.ui.util.formatCurrency
import com.example.financeapp.viewmodel.BudgetSummary

@Composable
fun BudgetSummaryCard(
    summary: BudgetSummary,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val progressColor = if (summary.isOverBudget) OverBudget else Primary

    FinanceCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = summary.budget.category.displayName,
                style = MaterialTheme.typography.titleSmall
            )
            if (summary.isOverBudget) {
                Text(
                    text = stringResource(R.string.over_budget),
                    style = MaterialTheme.typography.labelMedium,
                    color = OverBudget
                )
            }
        }
        LinearProgressIndicator(
            progress = { summary.progress },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            color = progressColor,
            trackColor = ProgressTrack
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = stringResource(R.string.spent),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(text = formatCurrency(summary.spent))
            }
            Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                Text(
                    text = stringResource(R.string.remaining),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatCurrency(summary.remaining),
                    color = if (summary.isOverBudget) OverBudget else MaterialTheme.colorScheme.onSurface
                )
            }
        }
        Text(
            text = "${stringResource(R.string.monthly_limit)}: ${formatCurrency(summary.budget.monthlyLimit)}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp)
        )
        androidx.compose.material3.TextButton(onClick = onDelete) {
            Text(stringResource(R.string.delete))
        }
    }
}
