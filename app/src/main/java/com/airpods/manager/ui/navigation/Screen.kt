package com.airpods.manager.ui.navigation

sealed class Screen(val route: String) {
    data object Dashboard : Screen("dashboard")
    data object DeviceList : Screen("devices")
    data class DeviceDetail(val address: String = "{address}") :
        Screen("device/{address}") {
        fun createRoute(address: String) = "device/$address"
    }
    data object BatteryHistory : Screen("history/{address}") {
        fun createRoute(address: String) = "history/$address"
    }
    data object Settings : Screen("settings")
}
