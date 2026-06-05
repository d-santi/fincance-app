package com.example.financeapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.financeapp.R
import com.example.financeapp.domain.model.Participant
import com.example.financeapp.domain.model.SharedExpense
import com.example.financeapp.ui.util.formatCurrency

@Composable
fun SharedExpenseCard(
    sharedExpense: SharedExpense,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onSettle: () -> Unit,
    onToggleParticipantPaid: (Participant) -> Unit,
    modifier: Modifier = Modifier
) {
    FinanceCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = sharedExpense.description,
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = sharedExpense.category.displayName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = formatCurrency(sharedExpense.totalAmount),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Outlined.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        SuggestionChip(
            onClick = {},
            label = {
                Text(
                    if (sharedExpense.settled) stringResource(R.string.settled)
                    else stringResource(R.string.pending)
                )
            },
            modifier = Modifier.padding(vertical = 8.dp)
        )

        sharedExpense.participants.forEach { participant ->
            ParticipantRow(
                participant = participant,
                enabled = !sharedExpense.settled,
                onTogglePaid = { onToggleParticipantPaid(participant) }
            )
        }

        if (!sharedExpense.settled) {
            TextButton(onClick = onSettle) {
                Text(stringResource(R.string.settle))
            }
        }
    }
}

@Composable
private fun ParticipantRow(
    participant: Participant,
    enabled: Boolean,
    onTogglePaid: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onTogglePaid, enabled = enabled) {
                Icon(
                    imageVector = if (participant.paid) Icons.Outlined.CheckCircle
                    else Icons.Outlined.RadioButtonUnchecked,
                    contentDescription = stringResource(R.string.paid),
                    tint = if (participant.paid) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(text = participant.name, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = formatCurrency(participant.amount),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
