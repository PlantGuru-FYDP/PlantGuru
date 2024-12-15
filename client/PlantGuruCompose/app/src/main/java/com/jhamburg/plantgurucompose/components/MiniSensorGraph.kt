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
import com.jhamburg.plantgurucompose.ui.theme.PlantGuruComposeTheme
import com.github.mikephil.charting.formatter.ValueFormatter
import com.jhamburg.plantgurucompose.utils.SensorFormatUtil

@Composable
fun MiniSensorGraph(
    sensorData: List<SensorData>,
    sensorType: SensorType,
    modifier: Modifier = Modifier,
    graphColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
) {
    val lineColor = graphColor.toArgb()
    
    val entries = remember(sensorData, sensorType) {
        sensorData.mapIndexed { index, data ->
            Entry(
                index.toFloat(),
                when (sensorType) {
                    SensorType.EXTERNAL_TEMP -> data.extTemp
                    SensorType.SOIL_TEMP -> data.soilTemp
                    SensorType.HUMIDITY -> data.humidity.coerceIn(0f, 100f)
                    SensorType.LIGHT -> data.light.coerceIn(0f, 100f)
                    SensorType.SOIL_MOISTURE -> when (data.plantId) {
                        9 -> (data.soilMoisture1 + data.soilMoisture2) / 2
                        else -> data.soilMoisture1.coerceIn(0f, 100f)
                    }
                }
            )
        }
    }

    AndroidView(
        modifier = modifier.fillMaxSize(),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setTouchEnabled(false)
      
                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    setDrawAxisLine(true)
                    axisLineWidth = 1f
                    axisLineColor = Color.GRAY
                    textSize = 8f
                    labelCount = 2 // Only show start and end
                    valueFormatter = object : IndexAxisValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return if (value == 0f) "12h ago" else "now"
                        }
                    }
                }
                
                axisLeft.apply {
                    setDrawGridLines(false)
                    setDrawAxisLine(true)
                    axisLineWidth = 1f
                    axisLineColor = Color.GRAY
                    textSize = 8f
                    setLabelCount(2, true)
                }
                
                axisRight.isEnabled = false
                minOffset = 10f
            }
        },
        update = { chart ->
            chart.axisLeft.apply {
                val values = entries.map { it.y }
                val minValue = values.minOrNull() ?: 0f
                val maxValue = values.maxOrNull() ?: 100f
                var range = maxValue - minValue
                
                if (range < 1f) {
                    val adjustment = (1f - range) / 2f
                    range = 1f
                    val center = (maxValue + minValue) / 2f
                    val adjustedMin = center - (range / 2f)
                    val adjustedMax = center + (range / 2f)
                    
                    when (sensorType) {
                        SensorType.EXTERNAL_TEMP, SensorType.SOIL_TEMP -> {
                            val padding = range * 0.1f
                            axisMinimum = adjustedMin - padding
                            axisMaximum = adjustedMax + padding
                        }
                        else -> {
                            axisMinimum = adjustedMin.coerceIn(0f, 99f)
                            axisMaximum = adjustedMax.coerceIn(1f, 100f)
                        }
                    }
                } else {
                    val padding = range * 0.1f
                    when (sensorType) {
                        SensorType.EXTERNAL_TEMP, SensorType.SOIL_TEMP -> {
                            axisMinimum = minValue - padding
                            axisMaximum = maxValue + padding
                        }
                        else -> {
                            axisMinimum = (minValue - padding).coerceAtLeast(0f)
                            axisMaximum = (maxValue + padding).coerceAtMost(100f)
                        }
                    }
                }
                
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        val range = axisMaximum - axisMinimum
                        return SensorFormatUtil.formatValueCompact(value, range, sensorType)
                    }
                }
            }
            
            val dataSet = LineDataSet(entries, "").apply {
                setDrawValues(false)
                setDrawCircles(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
                color = lineColor
                lineWidth = 2f
                setDrawFilled(true)
                fillColor = lineColor
                fillAlpha = 50
            }
            
            chart.data = LineData(dataSet)
            chart.invalidate()
        }
    )
}

@Preview(showBackground = true, widthDp = 200)
@Composable
private fun MiniSensorGraphPreview() {
    val dataPoints = (0 until 12).map { hour ->
        val baseTemp = 20.0 + (hour % 24) * 0.5
        val baseMoisture = 70.0 - (hour % 24) * 0.3
        
        SensorData(
            plantId = 1,
            extTemp = (baseTemp + kotlin.math.sin(hour * 0.5) * 2).toFloat(),
            light = 500f,
            humidity = 60f,
            soilTemp = (baseTemp - 2).toFloat(),
            soilMoisture1 = (baseMoisture + kotlin.math.sin(hour * 0.5) * 3).toFloat(),
            soilMoisture2 = (baseMoisture - 2).toFloat(),
            timeStamp = LocalDateTime.now()
                .minusHours(12 - hour.toLong())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            sensorId = hour + 1
        )
    }

    PlantGuruComposeTheme {
        Card(
            modifier = Modifier
                .width(200.dp)
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            MiniSensorGraph(
                sensorData = dataPoints,
                sensorType = SensorType.SOIL_MOISTURE,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 200)
@Composable
private fun MiniSensorGraphTemperaturePreview() {
    val dataPoints = (0 until 12).map { hour ->
        val baseTemp = 20.0 + (hour % 12) * 1.0
        
        SensorData(
            plantId = 1,
            extTemp = 20f,
            light = 500f,
            humidity = 60f,
            soilTemp = baseTemp.toFloat(),
            soilMoisture1 = 70f,
            soilMoisture2 = 70f,
            timeStamp = LocalDateTime.now()
                .minusHours(12 - hour.toLong())
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")),
            sensorId = hour + 1
        )
    }

    PlantGuruComposeTheme {
        Card(
            modifier = Modifier
                .width(200.dp)
                .height(100.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            MiniSensorGraph(
                sensorData = dataPoints,
                sensorType = SensorType.EXTERNAL_TEMP,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
} 