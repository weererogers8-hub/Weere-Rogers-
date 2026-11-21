package com.example.ui.components

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    fun formatPrice(value: Double, prefix: String = "$", decimals: Int = 2): String {
        val formatter = NumberFormat.getNumberInstance(Locale.US).apply {
            minimumFractionDigits = decimals
            maximumFractionDigits = decimals
        }
        return "$prefix${formatter.format(value)}"
    }

    fun formatPercent(value: Double): String {
        val sign = if (value >= 0) "+" else ""
        return String.format(Locale.US, "%s%.2f%%", sign, value)
    }

    fun formatPoints(points: Int): String {
        return "${NumberFormat.getIntegerInstance(Locale.US).format(points)} pts"
    }

    fun formatTime(millis: Long): String {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }

    fun formatDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
        return sdf.format(Date(millis))
    }
}
