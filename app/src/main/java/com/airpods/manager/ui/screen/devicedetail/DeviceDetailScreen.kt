package com.airpods.manager.ui.screen.devicedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.airpods.manager.domain.model.AirPodsDevice
import com.airpods.manager.domain.model.ConnectionState
import com.airpods.manager.ui.component.AirPodsVisual
import com.airpods.manager.ui.component.CircularBatteryIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceDetailScreen(
    deviceAddress: String,
    onBack: () -> Unit,
    onShowHistory: () -> Unit,
    viewModel: DeviceDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val device = uiState.device

    if (uiState.showRenameDialog) {
        AlertDialog(
            onDismissRequest = viewModel::dismissRenameDialog,
            title = { Text("Rename Device") },
            text = {
                OutlinedTextField(
                    value = uiState.renameInput,
                    onValueChange = viewModel::onRenameInputChange,
                    label = { Text("Device name") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = viewModel::confirmRename) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissRenameDialog) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(device?.name ?: "Device Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (device != null) {
                        IconButton(onClick = viewModel::openRenameDialog) {
                            Icon(Icons.Default.Edit, contentDescription = "Rename device")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (device != null) {
                DeviceInfoCard(device)
                BatteryCard(device)
                ConnectionCard(
                    device = device,
                    onConnect = viewModel::connect,
                    onDisconnect = viewModel::disconnect
                )
                Button(
                    onClick = onShowHistory,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View Battery History")
                }
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text("Loading device info...")
                }
            }
        }
    }
}

@Composable
private fun DeviceInfoCard(device: AirPodsDevice) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AirPodsVisual(
                model = device.model,
                leftInEar = device.leftInEar,
                rightInEar = device.rightInEar,
                size = 100.dp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(device.name, style = MaterialTheme.typography.titleLarge)
            Text(
                device.model.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                device.address,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
private fun BatteryCard(device: AirPodsDevice) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Battery", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CircularBatteryIndicator(
                    level = device.battery.left,
                    isCharging = device.battery.leftCharging,
                    label = "Left",
                    size = 80.dp
                )
                CircularBatteryIndicator(
                    level = device.battery.right,
                    isCharging = device.battery.rightCharging,
                    label = "Right",
                    size = 80.dp
                )
                CircularBatteryIndicator(
                    level = device.battery.case,
                    isCharging = device.battery.caseCharging,
                    label = "Case",
                    size = 80.dp
                )
            }
        }
    }
}

@Composable
private fun ConnectionCard(
    device: AirPodsDevice,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Connection", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = when (device.connectionState) {
                            ConnectionState.CONNECTED -> "Connected"
                            ConnectionState.CONNECTING -> "Connecting..."
                            ConnectionState.NEARBY -> "Nearby"
                            ConnectionState.DISCONNECTED -> "Disconnected"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = when (device.connectionState) {
                            ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primary
                            ConnectionState.NEARBY -> MaterialTheme.colorScheme.tertiary
                            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        }
                    )
                    if (device.lidOpen) {
                        Text(
                            "Case lid open",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }

                when (device.connectionState) {
                    ConnectionState.CONNECTED -> {
                        OutlinedButton(onClick = onDisconnect) { Text("Disconnect") }
                    }
                    ConnectionState.DISCONNECTED, ConnectionState.NEARBY -> {
                        Button(onClick = onConnect) { Text("Connect") }
                    }
                    ConnectionState.CONNECTING -> {
                        OutlinedButton(onClick = {}, enabled = false) { Text("Connecting...") }
                    }
                }
            }
        }
    }
}
