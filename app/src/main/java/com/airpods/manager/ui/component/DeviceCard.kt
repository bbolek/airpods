package com.airpods.manager.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airpods.manager.domain.model.AirPodsDevice
import com.airpods.manager.domain.model.ConnectionState

@Composable
fun DeviceCard(
    device: AirPodsDevice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (device.connectionState) {
                    ConnectionState.CONNECTED -> Icons.Default.BluetoothConnected
                    ConnectionState.DISCONNECTED -> Icons.Default.BluetoothDisabled
                    else -> Icons.Default.Bluetooth
                },
                contentDescription = null,
                tint = when (device.connectionState) {
                    ConnectionState.CONNECTED -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                },
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = device.model.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            // Battery summary
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val b = device.battery
                if (b.left != -1) BatteryChip(level = b.left, label = "L", charging = b.leftCharging)
                if (b.right != -1) BatteryChip(level = b.right, label = "R", charging = b.rightCharging)
                if (b.case != -1) BatteryChip(level = b.case, label = "C", charging = b.caseCharging)
            }
        }
    }
}

@Composable
private fun BatteryChip(level: Int, label: String, charging: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (charging) "⚡" else "$level%",
            style = MaterialTheme.typography.labelLarge,
            color = batteryColor(level)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        )
    }
}
