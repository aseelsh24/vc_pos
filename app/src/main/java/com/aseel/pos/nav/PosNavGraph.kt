package com.aseel.pos.nav

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.aseel.pos.data.SeedDataManager
import com.aseel.pos.data.Transaction
import com.aseel.pos.ui.TransactionsViewModel
import com.aseel.pos.ui.screens.AdaptivePosScreen
import com.aseel.pos.ui.screens.InventoryReportScreen
import com.aseel.pos.ui.screens.PosScreen
import com.aseel.pos.ui.screens.ProductManagementScreen
import com.aseel.pos.ui.screens.ReceiptPreviewScreen
import com.aseel.pos.ui.screens.SettingsScreen
import com.aseel.pos.ui.screens.TransactionsScreen

sealed class Screen(val route: String) {
    data object Pos : Screen("pos")
    data object Transactions : Screen("transactions")
    data object InventoryReport : Screen("inventory_report")
    data object ProductManagement : Screen("product_management")
    data object Settings : Screen("settings")
    data object ReceiptPreview : Screen("receipt_preview") {
        fun createRoute(transactionId: Long) = "receipt_preview/$transactionId"
    }
}

@Composable
fun PosNavGraph() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = Screen.Pos.route
    ) {
        composable(Screen.Pos.route) {
            AdaptivePosScreen(
                onNavigateToTransactions = {
                    navController.navigate(Screen.Transactions.route)
                },
                onNavigateToInventoryReport = {
                    navController.navigate(Screen.InventoryReport.route)
                },
                onNavigateToProductManagement = {
                    navController.navigate(Screen.ProductManagement.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }
        
        composable(Screen.Transactions.route) {
            TransactionsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToReceipt = { transaction ->
                    navController.navigate(Screen.ReceiptPreview.createRoute(transaction.id))
                }
            )
        }
        
        composable(
            route = Screen.ReceiptPreview.route + "/{transactionId}",
            arguments = listOf(
                navArgument("transactionId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getLong("transactionId") ?: 0L
            val viewModel: TransactionsViewModel = hiltViewModel()
            
            var transaction by remember { mutableStateOf<Transaction?>(null) }
            var isLoading by remember { mutableStateOf(true) }
            
            LaunchedEffect(transactionId) {
                viewModel.getTransactionById(transactionId) { result ->
                    transaction = result
                    isLoading = false
                }
            }
            
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (transaction != null) {
                ReceiptPreviewScreen(
                    transaction = transaction!!,
                    storeName = "ASEEL POS Store",
                    cashierName = "Admin",
                    onNavigateBack = { navController.popBackStack() },
                    onPrintToPdf = {
                        // TODO: Implement PDF printing
                        // This will be handled by the ReceiptPreviewScreen
                    },
                    onPrintToThermal = {
                        // TODO: Implement thermal printing
                        // This will be handled by the ReceiptPreviewScreen
                    }
                )
            } else {
                // Handle error - transaction not found
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "المعاملة غير موجودة",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("العودة")
                        }
                    }
                }
            }
        }
        
        composable(Screen.InventoryReport.route) {
            InventoryReportScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProductManagement = {
                    navController.navigate(Screen.ProductManagement.route)
                }
            )
        }
        
        composable(Screen.ProductManagement.route) {
            ProductManagementScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToProductManagement = {
                    navController.navigate(Screen.ProductManagement.route)
                },
                onNavigateToInventoryReports = {
                    navController.navigate(Screen.InventoryReport.route)
                }
            )
        }
    }
}
