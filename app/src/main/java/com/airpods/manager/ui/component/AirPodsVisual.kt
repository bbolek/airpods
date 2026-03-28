package com.airpods.manager.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airpods.manager.ble.model.AirPodsModel
import com.airpods.manager.ui.theme.AirPodsGray
import com.airpods.manager.ui.theme.AirPodsWhite

@Composable
fun AirPodsVisual(
    model: AirPodsModel,
    leftInEar: Boolean,
    rightInEar: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // Left earbud
        Canvas(modifier = Modifier.size(size * 0.4f, size)) {
            drawAirPod(
                isRight = false,
                isInEar = leftInEar,
                model = model
            )
        }

        Spacer(modifier = Modifier.width(size * 0.1f))

        // Right earbud (mirrored)
        Canvas(modifier = Modifier.size(size * 0.4f, size)) {
            drawAirPod(
                isRight = true,
                isInEar = rightInEar,
                model = model
            )
        }
    }
}

private fun DrawScope.drawAirPod(
    isRight: Boolean,
    isInEar: Boolean,
    model: AirPodsModel
) {
    val bodyColor = if (isInEar) AirPodsWhite else AirPodsGray.copy(alpha = 0.5f)
    val shadowColor = Color.Black.copy(alpha = 0.15f)

    when (model) {
        AirPodsModel.AIRPODS_MAX -> drawAirPodsMaxCup(bodyColor, shadowColor)
        else -> drawEarbudWithStem(bodyColor, shadowColor, isRight, model.hasStem)
    }
}

private fun DrawScope.drawEarbudWithStem(
    bodyColor: Color,
    shadowColor: Color,
    isRight: Boolean,
    hasStem: Boolean
) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    // Earbud body (rounded rectangle)
    val bodyWidth = canvasWidth * 0.85f
    val bodyHeight = canvasHeight * 0.38f
    val bodyTop = canvasHeight * 0.05f
    val bodyLeft = (canvasWidth - bodyWidth) / 2

    // Shadow
    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(bodyLeft + 2f, bodyTop + 2f),
        size = Size(bodyWidth, bodyHeight),
        cornerRadius = CornerRadius(bodyWidth * 0.4f)
    )

    // Body
    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(bodyLeft, bodyTop),
        size = Size(bodyWidth, bodyHeight),
        cornerRadius = CornerRadius(bodyWidth * 0.4f)
    )

    if (hasStem) {
        // Stem
        val stemWidth = canvasWidth * 0.28f
        val stemHeight = canvasHeight * 0.5f
        val stemLeft = (canvasWidth - stemWidth) / 2
        val stemTop = bodyTop + bodyHeight - 4f

        drawRoundRect(
            color = shadowColor,
            topLeft = Offset(stemLeft + 2f, stemTop + 2f),
            size = Size(stemWidth, stemHeight),
            cornerRadius = CornerRadius(stemWidth * 0.4f)
        )

        drawRoundRect(
            color = bodyColor,
            topLeft = Offset(stemLeft, stemTop),
            size = Size(stemWidth, stemHeight),
            cornerRadius = CornerRadius(stemWidth * 0.4f)
        )
    }
}

private fun DrawScope.drawAirPodsMaxCup(bodyColor: Color, shadowColor: Color) {
    val canvasWidth = size.width
    val canvasHeight = size.height

    val cupWidth = canvasWidth * 0.9f
    val cupHeight = canvasHeight * 0.75f
    val left = (canvasWidth - cupWidth) / 2
    val top = (canvasHeight - cupHeight) / 2

    drawRoundRect(
        color = shadowColor,
        topLeft = Offset(left + 2f, top + 2f),
        size = Size(cupWidth, cupHeight),
        cornerRadius = CornerRadius(cupWidth * 0.3f)
    )

    drawRoundRect(
        color = bodyColor,
        topLeft = Offset(left, top),
        size = Size(cupWidth, cupHeight),
        cornerRadius = CornerRadius(cupWidth * 0.3f)
    )
}
