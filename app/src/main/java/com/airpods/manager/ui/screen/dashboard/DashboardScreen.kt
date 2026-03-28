package com.airpods.manager.ui.screen.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airpods.manager.domain.model.AirPodsDevice
import com.airpods.manager.ui.component.AirPodsVisual
import com.airpods.manager.ui.component.CircularBatteryIndicator
import com.airpods.manager.ui.component.SignalStrengthBar
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToDevices: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (String) -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AirPods Manager") },
                actions = {
                    IconButton(onClick = onNavigateToDevices) {
                        Icon(Icons.Default.Devices, contentDescription = "Devices")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.activeDevice != null -> {
                    DeviceContent(
                        device = uiState.activeDevice!!,
                        onDeviceClick = { onNavigateToDetail(uiState.activeDevice!!.address) }
                    )
                }
                uiState.isScanning -> {
                    ScanningContent()
                }
                else -> {
                    NoDeviceContent()
                }
            }
        }
    }
}

@Composable
private fun DeviceContent(device: AirPodsDevice, onDeviceClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Device name and model
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = device.model.displayName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }

        // AirPods visual representation
        AirPodsVisual(
            model = device.model,
            leftInEar = device.leftInEar,
            rightInEar = device.rightInEar,
            size = 140.dp
        )

        // In-ear status
        if (device.leftInEar || device.rightInEar) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                if (device.leftInEar) InEarChip("Left")
                if (device.rightInEar) InEarChip("Right")
            }
        }

        // Lid status
        if (device.lidOpen) {
            Text(
                text = "Case Open",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Battery indicators
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            CircularBatteryIndicator(
                level = device.battery.left,
                isCharging = device.battery.leftCharging,
                label = "Left",
                size = 90.dp
            )
            CircularBatteryIndicator(
                level = device.battery.right,
                isCharging = device.battery.rightCharging,
                label = "Right",
                size = 90.dp
            )
            CircularBatteryIndicator(
                level = device.battery.case,
                isCharging = device.battery.caseCharging,
                label = "Case",
                size = 90.dp
            )
        }

        // Signal strength
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SignalStrengthBar(rssi = device.rssi)
            Text(
                text = "${device.rssi} dBm",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
private fun InEarChip(label: String) {
    androidx.compose.material3.SuggestionChip(
        onClick = {},
        label = { Text("$label In-Ear") }
    )
}

@Composable
private fun ScanningContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(modifier = Modifier.size(48.dp))
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Scanning for AirPods...",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Open your AirPods case to wake them up",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}

@Composable
private fun NoDeviceContent() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No AirPods Found",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Make sure Bluetooth is enabled and your AirPods are nearby",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
