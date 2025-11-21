package com.electricsheep.app.ui.components

import com.electricsheep.app.data.model.Mood
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar

/**
 * Data class representing processed chart data for a specific time period
 */
data class ChartDataPoint(
    val x: Float,
    val y: Float,
    val label: String
)

/**
 * Processor for mood chart data aggregation by time intervals.
 * Handles grouping and averaging mood scores for daily, weekly, and monthly views.
 */
class MoodChartDataProcessor {
    
    /**
     * Process mood data for daily view - show average per day
     */
    fun processDailyData(moods: List<Mood>): List<ChartDataPoint> {
        val dailyAverages = mutableMapOf<LocalDate, MutableList<Int>>()
        
        moods.forEach { mood ->
            val date = Instant.ofEpochMilli(mood.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            dailyAverages.getOrPut(date) { mutableListOf() }.add(mood.score)
        }
        
        val sortedDates = dailyAverages.keys.sorted()
        return sortedDates.mapIndexed { index, date ->
            val scores = dailyAverages[date]!!
            val average = scores.average().toFloat()
            ChartDataPoint(
                x = index.toFloat(),
                y = average,
                label = DateTimeFormatter.ofPattern("MMM d").format(date)
            )
        }
    }
    
    /**
     * Process mood data for weekly view - show average per week
     */
    fun processWeeklyData(moods: List<Mood>): List<ChartDataPoint> {
        val weeklyAverages = mutableMapOf<String, MutableList<Int>>()
        
        moods.forEach { mood ->
            val date = Instant.ofEpochMilli(mood.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val weekKey = getWeekKey(date)
            weeklyAverages.getOrPut(weekKey) { mutableListOf() }.add(mood.score)
        }
        
        val sortedWeeks = weeklyAverages.keys.sorted()
        return sortedWeeks.mapIndexed { index, weekKey ->
            val scores = weeklyAverages[weekKey]!!
            val average = scores.average().toFloat()
            ChartDataPoint(
                x = index.toFloat(),
                y = average,
                label = weekKey
            )
        }
    }
    
    /**
     * Process mood data for monthly view - show average per month
     */
    fun processMonthlyData(moods: List<Mood>): List<ChartDataPoint> {
        val monthlyAverages = mutableMapOf<String, MutableList<Int>>()
        
        moods.forEach { mood ->
            val date = Instant.ofEpochMilli(mood.timestamp)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
            val monthKey = "${date.year}-${date.monthValue.toString().padStart(2, '0')}"
            monthlyAverages.getOrPut(monthKey) { mutableListOf() }.add(mood.score)
        }
        
        val sortedMonths = monthlyAverages.keys.sorted()
        return sortedMonths.mapIndexed { index, monthKey ->
            val scores = monthlyAverages[monthKey]!!
            val average = scores.average().toFloat()
            val (year, month) = monthKey.split("-")
            val monthName = java.time.Month.of(month.toInt())
            ChartDataPoint(
                x = index.toFloat(),
                y = average,
                label = "${monthName.name.take(3)} $year"
            )
        }
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
}



