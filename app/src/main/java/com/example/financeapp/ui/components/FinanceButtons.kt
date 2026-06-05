package com.example.financeapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.financeapp.ui.theme.OnPrimary
import com.example.financeapp.ui.theme.Primary

@Composable
fun FinancePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
        enabled = enabled && !loading,
        shape = FinanceShapes.button,
        colors = ButtonDefaults.buttonColors(containerColor = Primary)
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.height(20.dp),
                color = OnPrimary,
                strokeWidth = 2.dp
            )
        } else {
            Text(text)
        }
    }
}

@Composable
fun FinanceTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(onClick = onClick, modifier = modifier) {
        Text(text, color = Primary)
    }
}
