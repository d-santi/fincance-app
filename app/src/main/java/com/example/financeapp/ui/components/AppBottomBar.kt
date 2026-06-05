package com.example.financeapp.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.financeapp.R
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.financeapp.ui.navigation.Screen

private enum class BottomNavItem(
    val screen: Screen,
    val labelRes: Int,
    val icon: ImageVector
) {
    EXPENSES(Screen.ExpenseList, R.string.nav_expenses, Icons.Outlined.Receipt),
    BUDGET(Screen.BudgetList, R.string.nav_budget, Icons.Outlined.AccountBalance),
    SHARED(Screen.SharedExpenseList, R.string.nav_shared, Icons.Outlined.Group)
}

@Composable
fun AppBottomBar(navController: NavController) {
    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    NavigationBar {
        BottomNavItem.entries.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        // Pop up to the start destination so we don't build up a large back stack
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    Icon(item.icon, contentDescription = stringResource(item.labelRes))
                },
                label = { Text(stringResource(item.labelRes)) }
            )
        }
    }
}
