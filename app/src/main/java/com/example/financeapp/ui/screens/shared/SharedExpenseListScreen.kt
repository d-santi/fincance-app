package com.example.financeapp.ui.screens.shared

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.financeapp.viewmodel.SharedExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedExpenseListScreen(
    viewModel: SharedExpenseViewModel,
    onAddSharedExpense: () -> Unit,
    onEditSharedExpense: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Compartidos") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSharedExpense) {
                Icon(Icons.Default.Add, contentDescription = "Agregar gasto compartido")
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Text("Gastos compartidos — próximamente", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
