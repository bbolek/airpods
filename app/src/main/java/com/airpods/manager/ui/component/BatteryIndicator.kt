package com.airpods.manager.ui.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airpods.manager.ui.theme.BatteryGreen
import com.airpods.manager.ui.theme.BatteryOrange
import com.airpods.manager.ui.theme.BatteryRed
import com.airpods.manager.ui.theme.BatteryYellow

@Composable
fun CircularBatteryIndicator(
    level: Int,           // 0-100, -1 = unknown
    isCharging: Boolean,
    label: String,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    val animatedLevel by animateFloatAsState(
        targetValue = if (level == -1) 0f else level / 100f,
        animationSpec = tween(durationMillis = 600),
        label = "battery_level"
    )

    val color = batteryColor(level)
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(size)
        ) {
            Canvas(modifier = Modifier.size(size)) {
                val strokeWidth = this.size.width * 0.1f
                val arcSize = this.size.width - strokeWidth
                val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                // Track arc
                drawArc(
                    color = trackColor,
                    startAngle = -220f,
                    sweepAngle = 260f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = Size(arcSize, arcSize),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )

                // Filled arc
                if (level != -1) {
                    drawArc(
                        color = color,
                        startAngle = -220f,
                        sweepAngle = 260f * animatedLevel,
                        useCenter = false,
                        topLeft = topLeft,
                        size = Size(arcSize, arcSize),
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (level == -1) "--" else "$level%",
                    fontSize = (size.value * 0.2).sp,
                    fontWeight = FontWeight.Bold,
                    color = if (level == -1) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else color
                )
                if (isCharging) {
                    Text(
                        text = "⚡",
                        fontSize = (size.value * 0.15).sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

fun batteryColor(level: Int): Color = when {
    level == -1 -> Color.Gray
    level <= 10 -> BatteryRed
    level <= 20 -> BatteryOrange
    level <= 40 -> BatteryYellow
    else -> BatteryGreen
}
