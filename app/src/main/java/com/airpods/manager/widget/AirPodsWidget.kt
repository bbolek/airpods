package com.airpods.manager.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

class AirPodsWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // In production, read from a shared DataStore or singleton
        val prefs = context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val deviceName = prefs.getString("device_name", "AirPods") ?: "AirPods"
        val leftBattery = prefs.getInt("left_battery", -1)
        val rightBattery = prefs.getInt("right_battery", -1)
        val caseBattery = prefs.getInt("case_battery", -1)
        val leftCharging = prefs.getBoolean("left_charging", false)
        val rightCharging = prefs.getBoolean("right_charging", false)
        val caseCharging = prefs.getBoolean("case_charging", false)

        provideContent {
            GlanceTheme {
                WidgetContent(
                    deviceName = deviceName,
                    leftBattery = leftBattery,
                    rightBattery = rightBattery,
                    caseBattery = caseBattery,
                    leftCharging = leftCharging,
                    rightCharging = rightCharging,
                    caseCharging = caseCharging
                )
            }
        }
    }

    companion object {
        fun updateWidgetPrefs(
            context: Context,
            deviceName: String,
            leftBattery: Int,
            rightBattery: Int,
            caseBattery: Int,
            leftCharging: Boolean,
            rightCharging: Boolean,
            caseCharging: Boolean
        ) {
            context.getSharedPreferences("widget_prefs", Context.MODE_PRIVATE).edit().apply {
                putString("device_name", deviceName)
                putInt("left_battery", leftBattery)
                putInt("right_battery", rightBattery)
                putInt("case_battery", caseBattery)
                putBoolean("left_charging", leftCharging)
                putBoolean("right_charging", rightCharging)
                putBoolean("case_charging", caseCharging)
                apply()
            }
        }
    }
}

@Composable
private fun WidgetContent(
    deviceName: String,
    leftBattery: Int,
    rightBattery: Int,
    caseBattery: Int,
    leftCharging: Boolean,
    rightCharging: Boolean,
    caseCharging: Boolean
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(12.dp)
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Text(
                text = deviceName,
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = GlanceTheme.colors.onSurface
                )
            )
            Spacer(modifier = GlanceModifier.defaultWeight())
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (leftBattery != -1) {
                    BatteryCell(
                        label = "L",
                        level = leftBattery,
                        charging = leftCharging
                    )
                    Spacer(modifier = GlanceModifier.width(12.dp))
                }
                if (rightBattery != -1) {
                    BatteryCell(
                        label = "R",
                        level = rightBattery,
                        charging = rightCharging
                    )
                    Spacer(modifier = GlanceModifier.width(12.dp))
                }
                if (caseBattery != -1) {
                    BatteryCell(
                        label = "Case",
                        level = caseBattery,
                        charging = caseCharging
                    )
                }
            }
        }
    }
}

@Composable
private fun BatteryCell(label: String, level: Int, charging: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = if (charging) "⚡$level%" else "$level%",
            style = TextStyle(
                fontWeight = FontWeight.Medium,
                color = GlanceTheme.colors.onSurface
            )
        )
        Text(
            text = label,
            style = TextStyle(color = GlanceTheme.colors.onSurface)
        )
    }
}
