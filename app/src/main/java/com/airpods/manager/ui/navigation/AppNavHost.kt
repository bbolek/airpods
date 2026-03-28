package com.airpods.manager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.airpods.manager.ui.screen.dashboard.DashboardScreen
import com.airpods.manager.ui.screen.devicedetail.DeviceDetailScreen
import com.airpods.manager.ui.screen.devices.DeviceListScreen
import com.airpods.manager.ui.screen.settings.SettingsScreen

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onNavigateToDevices = { navController.navigate(Screen.DeviceList.route) },
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToDetail = { address ->
                    navController.navigate(Screen.DeviceDetail().createRoute(address))
                }
            )
        }

        composable(Screen.DeviceList.route) {
            DeviceListScreen(
                onBack = { navController.popBackStack() },
                onDeviceClick = { address ->
                    navController.navigate(Screen.DeviceDetail().createRoute(address))
                }
            )
        }

        composable(
            route = Screen.DeviceDetail().route,
            arguments = listOf(navArgument("address") { type = NavType.StringType })
        ) { backStackEntry ->
            val address = backStackEntry.arguments?.getString("address") ?: return@composable
            DeviceDetailScreen(
                deviceAddress = address,
                onBack = { navController.popBackStack() },
                onShowHistory = { navController.navigate(Screen.BatteryHistory.createRoute(address)) }
            )
        }

        composable(
            route = Screen.BatteryHistory.route,
            arguments = listOf(navArgument("address") { type = NavType.StringType })
        ) { backStackEntry ->
            val address = backStackEntry.arguments?.getString("address") ?: return@composable
            // Battery history embedded in device detail for now
            DeviceDetailScreen(
                deviceAddress = address,
                onBack = { navController.popBackStack() },
                onShowHistory = {}
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
