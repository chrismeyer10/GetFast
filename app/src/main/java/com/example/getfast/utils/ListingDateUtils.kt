package com.example.getfast.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ListingDateUtils {
    private val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun isRecent(dateString: String, now: Date = Date()): Boolean {
        return try {
            val listingDate = parseDate(dateString, now) ?: return false
            val diff = now.time - listingDate.time
            diff in 0 until 10 * 60 * 1000
        } catch (_: Exception) {
            false
        }
    }

    private fun parseDate(dateString: String, now: Date): Date? {
        return try {
            when {
                dateString.startsWith("Heute") -> {
                    val timePart = dateString.substringAfter(",").trim().take(5)
                    val today = dateFormatter.format(now)
                    dateTimeFormatter.parse("$today $timePart")
                }
                dateString.startsWith("Vor") -> {
                    val minutes = Regex("Vor\\s+(\\d+)\\s+Min(?:\\.|uten)?", RegexOption.IGNORE_CASE)
                        .find(dateString)?.groupValues?.get(1)?.toIntOrNull()
                    minutes?.let { Date(now.time - it * 60 * 1000) }
                }
                dateString.startsWith("Gerade") -> now
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }
}
