package com.airpods.manager.domain.model

data class BatteryState(
    val left: Int,          // 0-100, -1 = unknown
    val right: Int,         // 0-100, -1 = unknown
    val case: Int,          // 0-100, -1 = unknown
    val leftCharging: Boolean,
    val rightCharging: Boolean,
    val caseCharging: Boolean
) {
    val lowestEarbud: Int get() = when {
        left == -1 && right == -1 -> -1
        left == -1 -> right
        right == -1 -> left
        else -> minOf(left, right)
    }

    val isAnyLow: Boolean get() = (left in 1..20) || (right in 1..20) || (case in 1..20)
    val isCritical: Boolean get() = (left in 1..10) || (right in 1..10)

    companion object {
        val UNKNOWN = BatteryState(-1, -1, -1, false, false, false)
    }
}
