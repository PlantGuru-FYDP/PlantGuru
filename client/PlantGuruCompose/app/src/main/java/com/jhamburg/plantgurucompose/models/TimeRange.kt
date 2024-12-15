package com.jhamburg.plantgurucompose.models

enum class TimeRange(
    val label: String,
    val hours: Int,
    val defaultGranularity: Int,
    val numPoints: Int
) {
    HOUR_1("1H", 1, 2, 30),         // 2-minute intervals
    HOURS_12("12H", 12, 15, 48),    // 15-minute intervals
    HOURS_24("24H", 24, 30, 48),    // 30-minute intervals
    WEEK_1("1W", 168, 180, 56),     // 3-hour intervals
    MONTH_1("1M", 720, 720, 60),    // 12-hour intervals
    CUSTOM("Custom", -1, 30, 20);    // default to 30-minute intervals

    companion object {
        fun getGranularityForHours(hours: Int): Int = when {
            hours <= 1 -> 2       // 2-minute intervals
            hours <= 12 -> 15     // 15-minute intervals
            hours <= 24 -> 30     // 30-minute intervals
            hours <= 168 -> 180   // 3-hour intervals
            else -> 720          // 12-hour intervals
        }
    }
} 