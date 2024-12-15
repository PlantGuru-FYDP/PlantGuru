package com.jhamburg.plantgurucompose.utils

import android.content.Context
import android.util.Log
import com.jhamburg.plantgurucompose.models.TimeRange
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object DateTimeUtil {
    private val ISO_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private val ISO_FORMATTER_NO_MS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
    private val DISPLAY_FORMATTER_24H = DateTimeFormatter.ofPattern("MMM dd, HH:mm")
    private val DISPLAY_FORMATTER_12H = DateTimeFormatter.ofPattern("MMM dd, hh:mm a")
    private val SQL_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    private val DATE_ONLY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val GRAPH_TIME_24H = DateTimeFormatter.ofPattern("H:mm")
    private val GRAPH_TIME_12H = DateTimeFormatter.ofPattern("h:mma")
    private val GRAPH_DATE = DateTimeFormatter.ofPattern("MMM d")

    fun parseTimestamp(timestamp: String): LocalDateTime {
        return try {
            when {
                timestamp.contains(".") ->
                    LocalDateTime.parse(timestamp, ISO_FORMATTER)
                        .atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime()

                timestamp.contains("T") ->
                    LocalDateTime.parse(timestamp, ISO_FORMATTER_NO_MS)
                        .atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime()

                timestamp.contains(":") ->
                    LocalDateTime.parse(timestamp, SQL_FORMATTER)
                        .atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(ZoneId.systemDefault())
                        .toLocalDateTime()

                else -> LocalDateTime.parse(timestamp + " 00:00:00", SQL_FORMATTER)
            }
        } catch (e: DateTimeParseException) {
            Log.e("DateTimeUtil", "Error parsing timestamp: $timestamp", e)
            LocalDateTime.now()
        }
    }

    fun formatForDisplay(context: Context, timestamp: String): String {
        val dateTime = parseTimestamp(timestamp)
        return formatForDisplay(context, dateTime)
    }

    fun formatForDisplay(context: Context, dateTime: LocalDateTime): String {
        val use24Hour = PreferenceManager.is24HourFormat(context)
        return dateTime.format(if (use24Hour) DISPLAY_FORMATTER_24H else DISPLAY_FORMATTER_12H)
    }

    fun formatForApi(dateTime: LocalDateTime): String {
        return dateTime.format(ISO_FORMATTER)
    }

    fun formatForTimeRange(dateTime: LocalDateTime, timeRange: TimeRange): String {
        return when (timeRange) {
            TimeRange.HOUR_1, TimeRange.HOURS_12, TimeRange.HOURS_24 ->
                dateTime.format(DateTimeFormatter.ofPattern("HH:mm"))

            TimeRange.WEEK_1, TimeRange.MONTH_1, TimeRange.CUSTOM ->
                dateTime.format(GRAPH_DATE)
        }
    }

    fun formatForMarkerView(dateTime: LocalDateTime): String {
        return dateTime.format(DateTimeFormatter.ofPattern("MMM d, HH:mm"))
    }

    fun formatTimestampForMarker(timestamp: String): String {
        val dateTime = parseTimestamp(timestamp)
        return formatForMarkerView(dateTime)
    }

    fun formatForGraph(context: Context, dateTime: LocalDateTime, timeRange: TimeRange): String {
        val use24Hour = PreferenceManager.is24HourFormat(context)
        return dateTime.format(if (use24Hour) GRAPH_TIME_24H else GRAPH_TIME_12H)
    }

    fun formatForGraphMarker(context: Context, dateTime: LocalDateTime): String {
        return formatForGraph(
            context,
            dateTime,
            TimeRange.HOURS_24
        )
    }
}
