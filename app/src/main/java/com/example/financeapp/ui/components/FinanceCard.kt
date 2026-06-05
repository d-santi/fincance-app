package com.example.financeapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.financeapp.ui.theme.Outline

@Composable
fun FinanceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickModifier = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Surface(
        modifier = modifier.then(clickModifier),
        shape = FinanceShapes.card,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, Outline)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}
