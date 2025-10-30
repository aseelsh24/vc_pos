package com.aseel.pos.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.aseel.pos.data.SeedDataManager
import com.aseel.pos.ui.screens.PosScreen
import com.aseel.pos.ui.screens.SettingsScreen
import com.aseel.pos.ui.screens.TransactionsScreen

sealed class Screen(val route: String) {
    data object Pos : Screen("pos")
    data object Transactions : Screen("transactions")
    data object Settings : Screen("settings")
}

@Composable
fun PosNavGraph() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Pos.route
    ) {
        composable(Screen.Pos.route) {
            PosScreen(
                onNavigateToTransactions = {
                    navController.navigate(Screen.Transactions.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.Transactions.route) {
            TransactionsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
