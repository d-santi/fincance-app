package com.example.financeapp.ui.navigation

/**
 * Defines every navigation destination in the app.
 *
 * Auth destinations (no bottom bar):
 *   Login → Register
 *
 * Main destinations (with bottom bar on tab screens):
 *   ExpenseList  → AddEditExpense
 *   BudgetList   → AddEditBudget
 *   SharedExpenseList → AddEditSharedExpense
 */
sealed class Screen(val route: String) {

    // Auth
    object Login : Screen("login")
    object Register : Screen("register")

    // Bottom-nav tabs
    object ExpenseList : Screen("expense_list")
    object BudgetList : Screen("budget_list")
    object SharedExpenseList : Screen("shared_list")

    // Detail / form screens
    object AddEditExpense : Screen("add_edit_expense/{expenseId}") {
        const val ARG_EXPENSE_ID = "expenseId"
        /** 0L = new expense, >0 = edit existing. */
        fun routeFor(expenseId: Long = 0L) = "add_edit_expense/$expenseId"
    }

    object AddEditBudget : Screen("add_edit_budget/{budgetId}") {
        const val ARG_BUDGET_ID = "budgetId"
        fun routeFor(budgetId: Long = 0L) = "add_edit_budget/$budgetId"
    }

    object AddEditSharedExpense : Screen("add_edit_shared/{sharedExpenseId}") {
        const val ARG_SHARED_EXPENSE_ID = "sharedExpenseId"
        fun routeFor(sharedExpenseId: Long = 0L) = "add_edit_shared/$sharedExpenseId"
    }

    companion object {
        /** Routes that should display the bottom navigation bar. */
        val bottomBarRoutes = setOf(
            ExpenseList.route,
            BudgetList.route,
            SharedExpenseList.route
        )
    }
}
