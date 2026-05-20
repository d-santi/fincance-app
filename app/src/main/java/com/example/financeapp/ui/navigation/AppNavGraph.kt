package com.example.financeapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.financeapp.FinanceApplication
import com.example.financeapp.ui.components.AppBottomBar
import com.example.financeapp.ui.screens.auth.LoginScreen
import com.example.financeapp.ui.screens.auth.RegisterScreen
import com.example.financeapp.ui.screens.budget.AddEditBudgetScreen
import com.example.financeapp.ui.screens.budget.BudgetListScreen
import com.example.financeapp.ui.screens.expense.AddEditExpenseScreen
import com.example.financeapp.ui.screens.expense.ExpenseListScreen
import com.example.financeapp.ui.screens.shared.AddEditSharedExpenseScreen
import com.example.financeapp.ui.screens.shared.SharedExpenseListScreen
import com.example.financeapp.viewmodel.AuthViewModel
import com.example.financeapp.viewmodel.BudgetViewModel
import com.example.financeapp.viewmodel.ExpenseViewModel
import com.example.financeapp.viewmodel.SharedExpenseViewModel

/**
 * Root navigation graph.
 *
 * Design decisions:
 * - Single NavHost at the app root. The Scaffold wrapping it shows/hides the
 *   bottom bar based on the current destination route.
 * - [AuthViewModel] is created here (Activity-scoped via the composable's
 *   ViewModelStoreOwner) so both Login and Register share the same instance.
 * - Feature ViewModels (Expense, Budget, SharedExpense) are destination-scoped:
 *   each NavBackStackEntry owns its own instance, which is cleared when the
 *   destination leaves the stack.
 * - The start destination is resolved synchronously from [SessionManager] so
 *   no recomposition or redirect is needed at startup.
 */
@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val app = context.applicationContext as FinanceApplication

    // Activity-scoped - shared between Login and Register so auth state
    // (isAuthenticated, error) is consistent across both screens.
    val authViewModel: AuthViewModel = viewModel(
        factory = AuthViewModel.factory(app.database.userDao(), app.sessionManager)
    )

    // Resolved once; SessionManager reads SharedPreferences synchronously.
    val startDestination = remember {
        if (app.sessionManager.isLoggedIn()) Screen.ExpenseList.route else Screen.Login.route
    }

    val backStack by navController.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    // Logout clears the session via AuthViewModel and returns to Login,
    // removing the entire back stack so the user cannot navigate back.
    val onLogout: () -> Unit = {
        authViewModel.logout()
        navController.navigate(Screen.Login.route) {
            popUpTo(0) { inclusive = true }
        }
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in Screen.bottomBarRoutes) {
                AppBottomBar(navController)
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {

            // Auth

            composable(Screen.Login.route) {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.ExpenseList.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.ExpenseList.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }

            // Expenses

            composable(Screen.ExpenseList.route) {
                val vm: ExpenseViewModel = viewModel(
                    factory = ExpenseViewModel.factory(app.database.expenseDao(), app.sessionManager)
                )
                ExpenseListScreen(
                    viewModel = vm,
                    onAddExpense = { navController.navigate(Screen.AddEditExpense.routeFor()) },
                    onEditExpense = { id -> navController.navigate(Screen.AddEditExpense.routeFor(id)) },
                    onLogout = onLogout
                )
            }

            composable(
                route = Screen.AddEditExpense.route,
                arguments = listOf(
                    navArgument(Screen.AddEditExpense.ARG_EXPENSE_ID) { type = NavType.LongType }
                )
            ) { entry ->
                val expenseId = entry.arguments!!.getLong(Screen.AddEditExpense.ARG_EXPENSE_ID)
                val vm: ExpenseViewModel = viewModel(
                    factory = ExpenseViewModel.factory(app.database.expenseDao(), app.sessionManager)
                )
                AddEditExpenseScreen(
                    expenseId = expenseId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Budget

            composable(Screen.BudgetList.route) {
                val vm: BudgetViewModel = viewModel(
                    factory = BudgetViewModel.factory(
                        app.database.budgetDao(),
                        app.database.expenseDao(),
                        app.sessionManager
                    )
                )
                BudgetListScreen(
                    viewModel = vm,
                    onAddBudget = { navController.navigate(Screen.AddEditBudget.routeFor()) },
                    onEditBudget = { id -> navController.navigate(Screen.AddEditBudget.routeFor(id)) }
                )
            }

            composable(
                route = Screen.AddEditBudget.route,
                arguments = listOf(
                    navArgument(Screen.AddEditBudget.ARG_BUDGET_ID) { type = NavType.LongType }
                )
            ) { entry ->
                val budgetId = entry.arguments!!.getLong(Screen.AddEditBudget.ARG_BUDGET_ID)
                val vm: BudgetViewModel = viewModel(
                    factory = BudgetViewModel.factory(
                        app.database.budgetDao(),
                        app.database.expenseDao(),
                        app.sessionManager
                    )
                )
                AddEditBudgetScreen(
                    budgetId = budgetId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Shared expenses

            composable(Screen.SharedExpenseList.route) {
                val vm: SharedExpenseViewModel = viewModel(
                    factory = SharedExpenseViewModel.factory(
                        app.database.sharedExpenseDao(),
                        app.sessionManager
                    )
                )
                SharedExpenseListScreen(
                    viewModel = vm,
                    onAddSharedExpense = { navController.navigate(Screen.AddEditSharedExpense.routeFor()) },
                    onEditSharedExpense = { id -> navController.navigate(Screen.AddEditSharedExpense.routeFor(id)) }
                )
            }

            composable(
                route = Screen.AddEditSharedExpense.route,
                arguments = listOf(
                    navArgument(Screen.AddEditSharedExpense.ARG_SHARED_EXPENSE_ID) {
                        type = NavType.LongType
                    }
                )
            ) { entry ->
                val sharedExpenseId =
                    entry.arguments!!.getLong(Screen.AddEditSharedExpense.ARG_SHARED_EXPENSE_ID)
                val vm: SharedExpenseViewModel = viewModel(
                    factory = SharedExpenseViewModel.factory(
                        app.database.sharedExpenseDao(),
                        app.sessionManager
                    )
                )
                AddEditSharedExpenseScreen(
                    sharedExpenseId = sharedExpenseId,
                    viewModel = vm,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
