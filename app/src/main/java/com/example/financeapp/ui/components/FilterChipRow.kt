package com.example.financeapp.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.financeapp.ui.theme.Primary
import com.example.financeapp.ui.theme.Surface

data class FilterChipItem(
    val label: String,
    val selected: Boolean,
    val onClick: () -> Unit
)

@Composable
fun FilterChipRow(
    chips: List<FilterChipItem>,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            FilterChip(
                selected = chip.selected,
                onClick = chip.onClick,
                label = { Text(chip.label) },
                shape = FinanceShapes.chip,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Primary,
                    selectedLabelColor = Surface
                )
            )
        }
    }
}
