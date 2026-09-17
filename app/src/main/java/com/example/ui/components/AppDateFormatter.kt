package com.example.ui.components

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * High-performance shared date formatter to avoid costly SimpleDateFormat
 * allocations during fast LazyColumn scrolling and item binding.
 */
object AppDateFormatter {
    private val format = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())

    @Synchronized
    fun format(date: Date): String = format.format(date)

    @Synchronized
    fun format(timeMs: Long): String = format.format(Date(timeMs))
}
