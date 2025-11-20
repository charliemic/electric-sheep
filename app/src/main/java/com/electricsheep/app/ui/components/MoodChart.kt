package com.electricsheep.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.electricsheep.app.config.MoodConfig
import com.electricsheep.app.data.model.Mood
import com.electricsheep.app.ui.theme.Spacing
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * Time interval for mood chart visualization
 */
enum class MoodChartInterval {
    DAILY,
    WEEKLY,
    MONTHLY
}

/**
 * Custom Canvas-based mood chart with fixed Y-axis range (1-10).
 * 
 * This implementation ensures:
 * 1. Accurate display of actual mood data values
 * 2. Fixed Y-axis range from 1 to 10
 * 
 * @param moods List of mood entries to display
 * @param modifier Modifier for the chart container
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodChart(
    moods: List<Mood>,
    modifier: Modifier = Modifier
) {
    var selectedInterval by remember { mutableStateOf(MoodChartInterval.WEEKLY) }
    
    // Process data based on selected interval
    val chartData = remember(moods, selectedInterval) {
        processMoodDataForInterval(moods, selectedInterval)
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Mood chart showing ${moods.size} entries over ${selectedInterval.name.lowercase()} intervals"
            },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            // Header with title and interval selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mood Trend",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                // Interval selector buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                    modifier = Modifier.semantics {
                        contentDescription = "Time interval selector"
                    }
                ) {
                    MoodChartInterval.values().forEach { interval ->
                        FilterChip(
                            selected = selectedInterval == interval,
                            onClick = { selectedInterval = interval },
                            label = {
                                Text(
                                    text = interval.displayName,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            modifier = Modifier.semantics {
                                contentDescription = "Select ${interval.displayName} view"
                            }
                        )
                    }
                }
            }
            
            // Chart display
            if (chartData.entries.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No mood data available for this period",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.semantics {
                            contentDescription = "No mood data available for this period"
                        }
                    )
                }
            } else {
                // Custom Canvas chart with fixed Y-axis range
                CustomMoodChartCanvas(
                    dataPoints = chartData.entries,
                    labels = chartData.labels,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .semantics {
                            contentDescription = "Line chart showing mood scores from ${MoodConfig.MIN_SCORE} to ${MoodConfig.MAX_SCORE}"
                        }
                )
            }
        }
    }
}

/**
 * Custom Canvas implementation for mood chart with fixed Y-axis range.
 * Uses Box with Canvas for drawing and Text composables for labels.
 */
@Composable
private fun CustomMoodChartCanvas(
    dataPoints: List<ChartDataPoint>,
    labels: List<String>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (dataPoints.isEmpty()) return@Canvas
            
            val padding = 40.dp.toPx()
            val chartWidth = size.width - (padding * 2)
            val chartHeight = size.height - (padding * 2)
            val chartLeft = padding
            val chartTop = padding
            val chartBottom = size.height - padding
            
            // Fixed Y-axis range: 1 to 10
            val minY = MoodConfig.MIN_SCORE.toFloat()
            val maxY = MoodConfig.MAX_SCORE.toFloat()
            val yRange = maxY - minY
            
            // Find X range from data
            val minX = dataPoints.minOfOrNull { it.x } ?: 0f
            val maxX = dataPoints.maxOfOrNull { it.x } ?: 0f
            val xRange = if (maxX > minX) maxX - minX else 1f
            
            // Draw Y-axis grid lines (1-10)
            for (i in MoodConfig.MIN_SCORE..MoodConfig.MAX_SCORE) {
                val yValue = i.toFloat()
                val yPosition = chartBottom - ((yValue - minY) / yRange) * chartHeight
                
                // Grid line
                drawLine(
                    color = surfaceVariant.copy(alpha = 0.3f),
                    start = Offset(chartLeft, yPosition),
                    end = Offset(chartLeft + chartWidth, yPosition),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Draw Y-axis line
            drawLine(
                color = onSurfaceVariant.copy(alpha = 0.5f),
                start = Offset(chartLeft, chartTop),
                end = Offset(chartLeft, chartBottom),
                strokeWidth = 2.dp.toPx()
            )
            
            // Draw X-axis line
            drawLine(
                color = onSurfaceVariant.copy(alpha = 0.5f),
                start = Offset(chartLeft, chartBottom),
                end = Offset(chartLeft + chartWidth, chartBottom),
                strokeWidth = 2.dp.toPx()
            )
            
            // Draw data line and points
            if (dataPoints.size > 1) {
                val path = Path().apply {
                    val firstPoint = dataPoints[0]
                    val firstX = chartLeft + ((firstPoint.x - minX) / xRange) * chartWidth
                    val firstY = chartBottom - ((firstPoint.y - minY) / yRange) * chartHeight
                    moveTo(firstX, firstY)
                    
                    for (i in 1 until dataPoints.size) {
                        val point = dataPoints[i]
                        val x = chartLeft + ((point.x - minX) / xRange) * chartWidth
                        val y = chartBottom - ((point.y - minY) / yRange) * chartHeight
                        lineTo(x, y)
                    }
                }
                
                // Draw line
                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(width = 4.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
                
                // Draw data points
                dataPoints.forEach { point ->
                    val x = chartLeft + ((point.x - minX) / xRange) * chartWidth
                    val y = chartBottom - ((point.y - minY) / yRange) * chartHeight
                    
                    drawCircle(
                        color = primaryColor,
                        radius = 4.dp.toPx(),
                        center = Offset(x, y)
                    )
                }
            } else if (dataPoints.size == 1) {
                // Single point
                val point = dataPoints[0]
                val x = chartLeft + ((point.x - minX) / xRange) * chartWidth
                val y = chartBottom - ((point.y - minY) / yRange) * chartHeight
                
                drawCircle(
                    color = primaryColor,
                    radius = 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }
        
        // Y-axis labels (1-10) - positioned absolutely
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 8.dp, top = 40.dp, bottom = 40.dp)
                .fillMaxHeight()
        ) {
            val padding = 40.dp
            val chartHeight = 200.dp - (padding * 2)
            val step = chartHeight / (MoodConfig.MAX_SCORE - MoodConfig.MIN_SCORE)
            
            for (i in MoodConfig.MAX_SCORE downTo MoodConfig.MIN_SCORE) {
                Spacer(modifier = Modifier.height((step - 12.dp).coerceAtLeast(0.dp)))
                Text(
                    text = i.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = onSurfaceVariant,
                    modifier = Modifier.height(12.dp)
                )
            }
        }
        
        // X-axis labels - positioned at bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(start = 40.dp, end = 40.dp, bottom = 8.dp)
                .fillMaxWidth()
        ) {
            val labelCount = minOf(labels.size, 5) // Show max 5 labels
            val labelStep = if (dataPoints.size > 1) (dataPoints.size - 1) / (labelCount - 1) else 0
            
            for (i in 0 until labelCount) {
                val index = i * labelStep
                if (index < labels.size) {
                    Box(modifier = Modifier.weight(1f)) {
                        Text(
                            text = labels[index],
                            style = MaterialTheme.typography.labelSmall,
                            color = onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Data class to hold processed chart data
 */
private data class ProcessedChartData(
    val entries: List<ChartDataPoint>,
    val labels: List<String>
)

/**
 * Process mood data for the selected time interval
 */
private fun processMoodDataForInterval(
    moods: List<Mood>,
    interval: MoodChartInterval
): ProcessedChartData {
    if (moods.isEmpty()) {
        return ProcessedChartData(
            entries = emptyList(),
            labels = emptyList()
        )
    }
    
    // Sort moods by timestamp (ascending for chart)
    val sortedMoods = moods.sortedBy { it.timestamp }
    
    return when (interval) {
        MoodChartInterval.DAILY -> processDailyData(sortedMoods)
        MoodChartInterval.WEEKLY -> processWeeklyData(sortedMoods)
        MoodChartInterval.MONTHLY -> processMonthlyData(sortedMoods)
    }
}

/**
 * Process mood data for daily view - show average per day
 */
private fun processDailyData(moods: List<Mood>): ProcessedChartData {
    val dailyAverages = mutableMapOf<LocalDate, MutableList<Int>>()
    
    moods.forEach { mood ->
        val date = Instant.ofEpochMilli(mood.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        dailyAverages.getOrPut(date) { mutableListOf() }.add(mood.score)
    }
    
    val sortedDates = dailyAverages.keys.sorted()
    val entries = sortedDates.mapIndexed { index, date ->
        val scores = dailyAverages[date]!!
        val average = scores.average().toFloat()
        ChartDataPoint(
            x = index.toFloat(),
            y = average.coerceIn(MoodConfig.MIN_SCORE.toFloat(), MoodConfig.MAX_SCORE.toFloat()),
            label = DateTimeFormatter.ofPattern("MMM d").format(date)
        )
    }
    
    val labels = sortedDates.map { date ->
        DateTimeFormatter.ofPattern("MMM d").format(date)
    }
    
    return ProcessedChartData(
        entries = entries,
        labels = labels
    )
}

/**
 * Process mood data for weekly view - show average per week
 */
private fun processWeeklyData(moods: List<Mood>): ProcessedChartData {
    val weeklyAverages = mutableMapOf<String, MutableList<Int>>()
    
    moods.forEach { mood ->
        val date = Instant.ofEpochMilli(mood.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val weekKey = getWeekKey(date)
        weeklyAverages.getOrPut(weekKey) { mutableListOf() }.add(mood.score)
    }
    
    val sortedWeeks = weeklyAverages.keys.sorted()
    val entries = sortedWeeks.mapIndexed { index, weekKey ->
        val scores = weeklyAverages[weekKey]!!
        val average = scores.average().toFloat()
        ChartDataPoint(
            x = index.toFloat(),
            y = average.coerceIn(MoodConfig.MIN_SCORE.toFloat(), MoodConfig.MAX_SCORE.toFloat()),
            label = weekKey
        )
    }
    
    return ProcessedChartData(
        entries = entries,
        labels = sortedWeeks
    )
}

/**
 * Process mood data for monthly view - show average per month
 */
private fun processMonthlyData(moods: List<Mood>): ProcessedChartData {
    val monthlyAverages = mutableMapOf<String, MutableList<Int>>()
    
    moods.forEach { mood ->
        val date = Instant.ofEpochMilli(mood.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()
        val monthKey = "${date.year}-${date.monthValue.toString().padStart(2, '0')}"
        monthlyAverages.getOrPut(monthKey) { mutableListOf() }.add(mood.score)
    }
    
    val sortedMonths = monthlyAverages.keys.sorted()
    val entries = sortedMonths.mapIndexed { index, monthKey ->
        val scores = monthlyAverages[monthKey]!!
        val average = scores.average().toFloat()
        val (year, month) = monthKey.split("-")
        val monthName = java.time.Month.of(month.toInt())
        ChartDataPoint(
            x = index.toFloat(),
            y = average.coerceIn(MoodConfig.MIN_SCORE.toFloat(), MoodConfig.MAX_SCORE.toFloat()),
            label = "${monthName.name.take(3)} $year"
        )
    }
    
    val labels = sortedMonths.map { monthKey ->
        val (year, month) = monthKey.split("-")
        val monthName = java.time.Month.of(month.toInt())
        "${monthName.name.take(3)} $year"
    }
    
    return ProcessedChartData(
        entries = entries,
        labels = labels
    )
}

/**
 * Get week key in format "YYYY-WW" for grouping
 */
private fun getWeekKey(date: LocalDate): String {
    val calendar = Calendar.getInstance()
    calendar.set(date.year, date.monthValue - 1, date.dayOfMonth)
    val week = calendar.get(Calendar.WEEK_OF_YEAR)
    return "${date.year}-W${week.toString().padStart(2, '0')}"
}

/**
 * Display name for interval enum
 */
private val MoodChartInterval.displayName: String
    get() = when (this) {
        MoodChartInterval.DAILY -> "Day"
        MoodChartInterval.WEEKLY -> "Week"
        MoodChartInterval.MONTHLY -> "Month"
    }
