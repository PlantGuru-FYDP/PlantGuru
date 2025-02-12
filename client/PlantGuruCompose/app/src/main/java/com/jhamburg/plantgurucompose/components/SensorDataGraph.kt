package com.jhamburg.plantgurucompose.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import android.graphics.Color
import android.view.ViewGroup
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.components.LimitLine
import com.jhamburg.plantgurucompose.models.*
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeParseException
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import com.jhamburg.plantgurucompose.R
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import android.graphics.Rect
import android.widget.TextView
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import com.github.mikephil.charting.formatter.ValueFormatter
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.formatter.IFillFormatter
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.jhamburg.plantgurucompose.utils.DateTimeUtil
import com.jhamburg.plantgurucompose.utils.SensorFormatUtil
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.time.Instant
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.ChartTouchListener
import android.view.MotionEvent

private class DateLabel(private val context: Context) : MarkerView(context, R.layout.marker_view) {
    private val tvContent: TextView = findViewById(R.id.tvContent)
    private val tvDate: TextView = findViewById(R.id.tvDate)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        e?.let { entry ->
            val range = entries?.maxOfOrNull { it.y }?.minus(entries?.minOfOrNull { it.y } ?: 0f) ?: 0f
            tvContent.text = SensorFormatUtil.formatValue(entry.y, range, sensorType)
            tvDate.text = timestamps.getOrNull(entry.x.toInt())?.let { timestamp ->
                val dateTime = DateTimeUtil.parseTimestamp(timestamp)
                DateTimeUtil.formatForGraph(context, dateTime, TimeRange.HOURS_24)
            } ?: ""
        }
    }

    override fun getOffset(): MPPointF {
        return MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
    }

    companion object {
        var timestamps: List<String> = emptyList()
        var sensorType: SensorType = SensorType.SOIL_MOISTURE
        var entries: List<Entry>? = null
    }
}

class ProjectionEntry(x: Float, y: Float, val confidence: Double) : Entry(x, y) {
    override fun copy(): ProjectionEntry = ProjectionEntry(x, y, confidence)
}

@Composable
fun SensorDataGraph(
    sensorData: List<SensorData>,
    wateringEvents: List<WateringEvent>,
    timeRange: TimeRange,
    sensorType: SensorType,
    showWateringEvents: Boolean = true,
    forceDecimalPrecision: Boolean = false
) {
    val context = LocalContext.current
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface.toArgb()
    
    val firstTimestamp = sensorData.minOfOrNull { 
        DateTimeUtil.parseTimestamp(it.timeStamp).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    } ?: 0L

    var yAxisPrecision by remember { mutableIntStateOf(1) }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            LineChart(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                description.isEnabled = false
                setTouchEnabled(true)
                isDragEnabled = true
                setScaleEnabled(true)
                setPinchZoom(false)
                isScaleXEnabled = true
                isScaleYEnabled = true
                axisRight.isEnabled = false
                legend.isEnabled = false
                setBackgroundColor(Color.TRANSPARENT)
                
                marker = null
                DateLabel.timestamps = sensorData.map { it.timeStamp }
                DateLabel.sensorType = sensorType
                setDrawMarkers(false)
                isHighlightPerTapEnabled = false
                isHighlightPerDragEnabled = false
                
                setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                    override fun onValueSelected(e: Entry?, h: Highlight?) {}
                    override fun onNothingSelected() { highlightValue(null) }
                })

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    labelRotationAngle = -45f
                    setAvoidFirstLastClipping(true)
                    spaceMin = 0.4f
                    spaceMax = 0.2f
                    yOffset = 15f
                    xOffset = 10f
                    textSize = 11f
                    textColor = onSurfaceColor
                    gridColor = Color.argb(25, Color.red(onSurfaceColor), 
                                            Color.green(onSurfaceColor), 
                                            Color.blue(onSurfaceColor))
                    gridLineWidth = 0.5f
                    axisLineWidth = 1f
                    axisLineColor = Color.argb(77, Color.red(onSurfaceColor), 
                                                Color.green(onSurfaceColor), 
                                                Color.blue(onSurfaceColor))
                }

                axisLeft.apply {
                    textColor = onSurfaceColor
                    textSize = 11f
                    setDrawGridLines(true)
                    gridColor = Color.argb(25, Color.red(onSurfaceColor), 
                                            Color.green(onSurfaceColor), 
                                            Color.blue(onSurfaceColor))
                    gridLineWidth = 0.5f
                    axisLineWidth = 1f
                    axisLineColor = Color.argb(77, Color.red(onSurfaceColor), 
                                                Color.green(onSurfaceColor), 
                                                Color.blue(onSurfaceColor))
                    setDrawZeroLine(false)
                    spaceTop = 15f
                    spaceBottom = 15f
                    
                    setLabelCount(8, false)
                    
                    valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            val range = axisMaximum - axisMinimum
                            return when {
                                range > 1000 -> String.format( "%.1fk", value / 1000)
                                range < 1 || forceDecimalPrecision -> String.format("%.1f", value)
                                else -> String.format("%.0f", value)
                            }
                        }
                    }
                }

                description.textColor = onSurfaceColor
                legend.textColor = onSurfaceColor

                onChartGestureListener = object : OnChartGestureListener {
                    override fun onChartGestureStart(
                        me: MotionEvent?,
                        lastPerformedGesture: ChartTouchListener.ChartGesture?
                    ) {
                    }

                    override fun onChartGestureEnd(
                        me: MotionEvent?,
                        lastPerformedGesture: ChartTouchListener.ChartGesture?
                    ) {
                    }

                    override fun onChartLongPressed(me: MotionEvent?) {
                    }

                    override fun onChartDoubleTapped(me: MotionEvent?) {

                    }

                    override fun onChartSingleTapped(me: MotionEvent?) {

                    }

                    override fun onChartFling(
                        me1: MotionEvent?,
                        me2: MotionEvent?,
                        velocityX: Float,
                        velocityY: Float
                    ) {
                    }

                    override fun onChartScale(me: MotionEvent?, scaleX: Float, scaleY: Float) {
                        if (forceDecimalPrecision) {
                            val visibleYRange = axisLeft.mAxisRange / scaleY
                            yAxisPrecision = calculatePrecision(visibleYRange)

                            axisLeft.valueFormatter = object : ValueFormatter() {
                                override fun getFormattedValue(value: Float): String {
                                    return String.format("%.${yAxisPrecision}f", value)
                                }
                            }

                            notifyDataSetChanged()
                            invalidate()
                        }
                    }

                    override fun onChartTranslate(me: MotionEvent?, dX: Float, dY: Float) {
                    }
                }
            }
        },
        update = { chart ->
            chart.axisLeft.apply {
                val values = sensorData.map { data ->
                    when (sensorType) {
                        SensorType.SOIL_MOISTURE -> if (data.plantId == 9) {
                            (data.soilMoisture1 + data.soilMoisture2) / 2
                        } else {
                            data.soilMoisture1
                        }
                        SensorType.EXTERNAL_TEMP -> data.extTemp
                        SensorType.HUMIDITY -> data.humidity
                        SensorType.LIGHT -> data.light
                        SensorType.SOIL_TEMP -> data.soilTemp
                    }
                }
                
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val range = axisMaximum - axisMinimum
                        return SensorFormatUtil.formatValue(value, range, sensorType)
                    }
                }
            }

            chart.xAxis.valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val dateTime = Instant.ofEpochMilli(value.toLong())
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime()
                    return DateTimeUtil.formatForTimeRange(dateTime, timeRange)
                }
            }

            val realData = sensorData.filter { it.sensorId >= 0 }
            val projectedData = sensorData.filter { it.sensorId < 0 }

            chart.data = LineData().apply {
                addDataSet(createRealDataSet(realData, sensorType))

                if (projectedData.isNotEmpty()) {
                    val projectedEntries = createProjectedEntries(projectedData, realData, sensorType)
                    addDataSet(createProjectedDataSet(projectedEntries, sensorType))
                    createConfidenceIntervalDataSet(projectedEntries).forEach { addDataSet(it) }
                }
            }

            // Clear all limit lines first
            chart.xAxis.removeAllLimitLines()

            // First add midnight markers (they should be in the background)
            addMidnightMarkers(chart, sensorData, realData, projectedData)

            // Then add watering events if enabled (they should be on top)
            if (showWateringEvents) {
                addWateringEventMarkers(chart, wateringEvents, sensorData)
            }

            chart.invalidate()
        }
    )
}

private fun createRealDataSet(realData: List<SensorData>, sensorType: SensorType): LineDataSet {
    val entries = realData.map { data ->
        val value = getSensorValue(data, sensorType)
        val timestamp = DateTimeUtil.parseTimestamp(data.timeStamp)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
            .toFloat()
        Entry(timestamp, value)
    }

    return LineDataSet(entries, "${sensorType.label} ${sensorType.unit}").apply {
        color = Color.parseColor("#4CAF50")
        valueTextColor = color
        setDrawValues(false)
        valueTextSize = 10f
        setCircleColor(color)
        circleRadius = 2.5f
        mode = LineDataSet.Mode.CUBIC_BEZIER
        lineWidth = 2f
        setDrawFilled(true)
        fillAlpha = 50
        fillColor = color
        setDrawCircles(true)
        setDrawCircleHole(false)
    }
}

private fun createProjectedDataSet(entries: List<ProjectionEntry>, sensorType: SensorType): LineDataSet {
    return LineDataSet(entries, "Projected ${sensorType.label}").apply {
        color = Color.BLUE
        valueTextColor = color
        setDrawValues(false)
        valueTextSize = 10f
        circleRadius = 0f
        mode = LineDataSet.Mode.LINEAR
        lineWidth = 2f
        setDrawFilled(true)
        fillAlpha = 50
        fillColor = Color.BLUE
        enableDashedLine(10f, 5f, 0f)
        setDrawCircles(false)
        setDrawCircleHole(false)
    }
}

private fun createConfidenceIntervalDataSet(projectedEntries: List<ProjectionEntry>): List<LineDataSet> {
    val upperEntries = projectedEntries.map { entry ->
        Entry(entry.x, entry.y + (entry.confidence.toFloat() / 2))
    }
    
    val lowerEntries = projectedEntries.map { entry ->
        Entry(entry.x, entry.y - (entry.confidence.toFloat() / 2))
    }

    val upperDataSet = LineDataSet(upperEntries, "Upper Bound").apply {
        color = Color.BLUE
        setDrawValues(false)
        setDrawCircles(false)
        lineWidth = 1.5f
        mode = LineDataSet.Mode.CUBIC_BEZIER
        form = Legend.LegendForm.NONE
        isHighlightEnabled = false
        setDrawFilled(false)
    }
    
    val lowerDataSet = LineDataSet(lowerEntries, "Lower Bound").apply {
        color = Color.BLUE
        setDrawValues(false)
        setDrawCircles(false)
        lineWidth = 1.5f
        mode = LineDataSet.Mode.CUBIC_BEZIER
        form = Legend.LegendForm.NONE
        isHighlightEnabled = false
        setDrawFilled(false)
    }
    
    return listOf(upperDataSet, lowerDataSet)
}

private fun getSensorValue(data: SensorData, sensorType: SensorType): Float {
    return when (sensorType) {
        SensorType.SOIL_MOISTURE -> if (data.plantId == 9) {
            ((data.soilMoisture1 + data.soilMoisture2) / 2).coerceIn(0f, 100f)
        } else {
            data.soilMoisture1.coerceIn(0f, 100f)
        }
        SensorType.EXTERNAL_TEMP -> data.extTemp
        SensorType.HUMIDITY -> data.humidity.coerceIn(0f, 100f)
        SensorType.LIGHT -> data.light.coerceIn(0f, 100000f)
        SensorType.SOIL_TEMP -> data.soilTemp
    }
}

private fun createProjectedEntries(
    projectedData: List<SensorData>,
    realData: List<SensorData>,
    sensorType: SensorType
): List<ProjectionEntry> {
    val lastRealTimestamp = DateTimeUtil.parseTimestamp(realData.last().timeStamp)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()

    return projectedData.mapIndexed { index, data ->
        val value = getSensorValue(data, sensorType)
        val timestamp = DateTimeUtil.parseTimestamp(data.timeStamp)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
            .toFloat()
        
        val xValue = if (index == 0) {
            lastRealTimestamp.toFloat()
        } else {
            timestamp
        }
        
        ProjectionEntry(xValue, value, 1.0 - data.confidence)
    }
}

private fun addWateringEventMarkers(
    chart: LineChart,
    wateringEvents: List<WateringEvent>,
    sensorData: List<SensorData>
) {
    Log.d("SensorDataGraph", "Adding ${wateringEvents.size} watering events")
    
    wateringEvents.forEach { event ->
        val eventDateTime = DateTimeUtil.parseTimestamp(event.timeStamp)
        val eventTimestamp = eventDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
            .toFloat()
        
        val limitLine = LimitLine(eventTimestamp).apply {
            lineWidth = 2f
            lineColor = Color.BLUE
            enableDashedLine(10f, 5f, 0f)
            labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            textSize = 12f
            textColor = Color.BLUE
            label = "💧"
        }
        chart.xAxis.addLimitLine(limitLine)
    }
}

private fun addMidnightMarkers(
    chart: LineChart,
    sensorData: List<SensorData>,
    realData: List<SensorData>,
    projectedData: List<SensorData>
) {
    if (sensorData.isEmpty()) return
    
    val firstDateTime = DateTimeUtil.parseTimestamp(sensorData.first().timeStamp)
    val lastDateTime = DateTimeUtil.parseTimestamp(sensorData.last().timeStamp)

    var currentDateTime = firstDateTime
        .plusDays(1)
        .withHour(0)
        .withMinute(0)
        .withSecond(0)
        .withNano(0)

    while (currentDateTime.isBefore(lastDateTime)) {
        val timestamp = currentDateTime
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
            .toFloat()

        val limitLine = createMidnightMarker(timestamp, currentDateTime)
        chart.xAxis.addLimitLine(limitLine)

        currentDateTime = currentDateTime.plusDays(1)
    }
}

private fun createMidnightMarker(timestamp: Float, dateTime: LocalDateTime): LimitLine {
    return LimitLine(timestamp).apply {
        lineWidth = 1f
        lineColor = Color.GRAY
        enableDashedLine(5f, 5f, 0f)
        labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
        textSize = 8f
        textColor = Color.GRAY
        label = dateTime.format(DateTimeFormatter.ofPattern("MMM d"))
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun SensorDataGraphPreviewSoilMoisture() {
    val dataPoints = generatePreviewData(24)
    val wateringEvents = listOf(
        generatePreviewWateringEvent(dataPoints[6]),
        generatePreviewWateringEvent(dataPoints[18])
    )
    MaterialTheme {
        SensorDataGraph(
            sensorData = dataPoints,
            wateringEvents = wateringEvents,
            timeRange = TimeRange.HOURS_24,
            sensorType = SensorType.SOIL_MOISTURE
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun SensorDataGraphPreviewTemperature() {
    val dataPoints = generatePreviewData(24)
    val wateringEvents = listOf(
        generatePreviewWateringEvent(dataPoints[6]),
        generatePreviewWateringEvent(dataPoints[18])
    )
    MaterialTheme {
        SensorDataGraph(
            sensorData = dataPoints,
            wateringEvents = wateringEvents,
            timeRange = TimeRange.HOURS_24,
            sensorType = SensorType.EXTERNAL_TEMP
        )
    }
}

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun SensorDataGraphPreviewMidnight() {
    val now = LocalDateTime.now()
    val dataPoints = (0 until 12).map { hour ->
        val baseTemp = 20.0 + (hour % 24) * 0.5
        val baseMoisture = 70.0 - (hour % 24) * 0.3
        val baseLight = if (hour in 6..18) 500 + (hour - 12) * 100 else 50

        val actualHour = if (hour < 4) {
            (22 + hour) % 24
        } else {
            hour - 4
        }

        val daysAgo = if (actualHour >= 22) 1 else 0
        val timestamp = now.minusDays(daysAgo.toLong()).withHour(actualHour).withMinute(0).withSecond(0).withNano(0)

        SensorData(
            plantId = 1,
            extTemp = (baseTemp + kotlin.math.sin(hour * 0.5) * 2).toFloat(),
            light = (baseLight + kotlin.math.sin(hour * 0.5) * 50).toFloat(),
            humidity = (60 + kotlin.math.sin(hour * 0.5) * 5).toFloat(),
            soilTemp = (baseTemp - 2).toFloat(),
            soilMoisture1 = (baseMoisture + kotlin.math.sin(hour * 0.5) * 3).toFloat(),
            soilMoisture2 = (baseMoisture - 2).toFloat(),
            timeStamp = timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")),
            sensorId = hour + 1
        )
    }.sortedBy { it.timeStamp }

    val wateringEvents = listOf(
        WateringEvent(
            wateringId = 1,
            plantId = 1,
            timeStamp = dataPoints[1].timeStamp,
            wateringDuration = 30,
            peakTemp = 25f,
            peakMoisture = 85f,
            avgTemp = 23f,
            avgMoisture = 80f,
            volume = 250f
        ),
        WateringEvent(
            wateringId = 2,
            plantId = 1,
            timeStamp = dataPoints[3].timeStamp,
            wateringDuration = 30,
            peakTemp = 25f,
            peakMoisture = 85f,
            avgTemp = 23f,
            avgMoisture = 80f,
            volume = 250f
        )
    )

    MaterialTheme {
        SensorDataGraph(
            sensorData = dataPoints,
            wateringEvents = wateringEvents,
            timeRange = TimeRange.HOURS_12,
            sensorType = SensorType.SOIL_MOISTURE
        )
    }
}

private fun generatePreviewData(hours: Int): List<SensorData> {
    val now = LocalDateTime.now()
    return (0 until hours).map { hour ->
        val baseTemp = 20.0 + (hour % 24) * 0.5
        val baseMoisture = 70.0 - (hour % 24) * 0.3
        val baseLight = if (hour in 6..18) 500 + (hour - 12) * 100 else 50

        val timestamp = now.minusHours((hours - 1 - hour).toLong())
            .atZone(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
        
        Log.d("SensorDataGraph", "Generated timestamp: $timestamp")
        
        SensorData(
            plantId = 1,
            extTemp = (baseTemp + kotlin.math.sin(hour * 0.5) * 2).toFloat(),
            light = (baseLight + kotlin.math.sin(hour * 0.5) * 50).toFloat(),
            humidity = (60 + kotlin.math.sin(hour * 0.5) * 5).toFloat(),
            soilTemp = (baseTemp - 2).toFloat(),
            soilMoisture1 = (baseMoisture + kotlin.math.sin(hour * 0.5) * 3).toFloat(),
            soilMoisture2 = (baseMoisture - 2).toFloat(),
            timeStamp = timestamp,
            sensorId = hour + 1
        )
    }
}

private fun generatePreviewWateringEvent(sensorData: SensorData) = WateringEvent(
    wateringId = 1,
    wateringDuration = 30,
    peakTemp = 25f,
    peakMoisture = 85f,
    avgTemp = 23f,
    avgMoisture = 80f,
    plantId = 1,
    timeStamp = sensorData.timeStamp,
    volume = 250f
)

@Preview(showBackground = true, widthDp = 400)
@Composable
private fun SensorDataGraphPreviewWithProjections() {
    val now = LocalDateTime.now()
    
    val historicalData = generatePreviewData(12)

    val wateringEvents = listOf(
        WateringEvent(
            wateringId = 1,
            plantId = 1,
            timeStamp = historicalData[3].timeStamp,
            wateringDuration = 30,
            peakTemp = 25f,
            peakMoisture = 85f,
            avgTemp = 23f,
            avgMoisture = 80f,
            volume = 250f
        ),
        WateringEvent(
            wateringId = 2,
            plantId = 1,
            timeStamp = historicalData[9].timeStamp,
            wateringDuration = 30,
            peakTemp = 25f,
            peakMoisture = 85f,
            avgTemp = 23f,
            avgMoisture = 80f,
            volume = 250f
        )
    )

    val projectedData = (0 until 12).map { hour ->
        val baseTemp = 20.0 + ((12 + hour) % 24) * 0.5
        val baseMoisture = 70.0 - ((12 + hour) % 24) * 0.3
        val baseLight = if ((12 + hour) in 6..18) 500 + ((12 + hour) - 12) * 100 else 50

        SensorData(
            plantId = 1,
            extTemp = (baseTemp + kotlin.math.sin(hour * 0.5) * 2).toFloat(),
            light = (baseLight + kotlin.math.sin(hour * 0.5) * 50).toFloat(),
            humidity = (60 + kotlin.math.sin(hour * 0.5) * 5).toFloat(),
            soilTemp = (baseTemp - 2).toFloat(),
            soilMoisture1 = (baseMoisture + kotlin.math.sin(hour * 0.5) * 3).toFloat(),
            soilMoisture2 = (baseMoisture - 2).toFloat(),
            timeStamp = now.plusHours(hour.toLong())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")),
            sensorId = -1,
            confidence = ((hour + 0.5) * 2.0)
        )
    }

    val combinedData = (historicalData + projectedData).sortedBy { it.timeStamp }

    MaterialTheme {
        SensorDataGraph(
            sensorData = combinedData,
            wateringEvents = wateringEvents,
            timeRange = TimeRange.HOURS_24,
            sensorType = SensorType.SOIL_MOISTURE,
            showWateringEvents = true
        )
    }
}

private fun calculatePrecision(range: Float): Int {
    return when {
        range<= 0.01 ->3
        range<= 0.1 -> 2
        range<= 1 -> 1
        else-> 0
    }
}
