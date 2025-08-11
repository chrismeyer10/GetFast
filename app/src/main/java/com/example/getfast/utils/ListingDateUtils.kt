package com.example.getfast.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ListingDateUtils {
    private val dateTimeFormatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun isRecent(dateString: String, now: Date = Date()): Boolean {
        return try {
            val listingDate = parseDate(dateString) ?: return false
            val diff = now.time - listingDate.time
            diff in 0 until 10 * 60 * 1000
        } catch (_: Exception) {
            false
        }
    }

    private fun parseDate(dateString: String): Date? {
        return try {
            if (dateString.startsWith("Heute")) {
                val timePart = dateString.substringAfter(",").trim().take(5)
                val today = dateFormatter.format(Date())
                dateTimeFormatter.parse("$today $timePart")
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }
}
