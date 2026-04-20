package com.example.calendarapp.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.YearMonth

data class CalendarSummary(
    val monthCount: Int,
    val yearCount: Int,
    val message: String,
)

class CalendarViewModel(application: Application) : AndroidViewModel(application) {
    private val preferences = application.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _selections = MutableStateFlow(loadSelections())
    val selections: StateFlow<Map<String, Set<Int>>> = _selections.asStateFlow()

    fun toggleDay(month: YearMonth, day: Int) {
        val key = month.toString()
        val current = _selections.value[key].orEmpty().toMutableSet()

        if (!current.add(day)) {
            current.remove(day)
        }

        val updated = _selections.value.toMutableMap().apply {
            if (current.isEmpty()) {
                remove(key)
            } else {
                put(key, current.toSet())
            }
        }
        _selections.value = updated
        saveSelections(updated)
    }

    fun summaryFor(month: YearMonth): CalendarSummary {
        val monthCount = selections.value[month.toString()].orEmpty().size
        val yearPrefix = "${month.year}-"
        val yearCount = selections.value
            .filterKeys { it.startsWith(yearPrefix) }
            .values
            .sumOf { it.size }

        val message = if (monthCount == 0) {
            "\u8fd9\u4e2a\u6708\u8fd8\u6ca1\u6709\u9ad8\u4eae\u65e5\u671f\uff0c\u53ef\u4ee5\u4ece\u4eca\u5929\u5f00\u59cb\u8bb0\u5f55\u3002"
        } else {
            "\u4f60\u8fd9\u4e2a\u6708\u5df2\u7ecf\u6709 $monthCount \u5929\u88ab\u9ad8\u4eae\uff0c\u4eca\u5e74\u7d2f\u8ba1 $yearCount \u5929\u3002"
        }

        return CalendarSummary(
            monthCount = monthCount,
            yearCount = yearCount,
            message = message,
        )
    }

    fun monthlyCountsForYear(year: Int): List<Int> {
        return (1..12).map { month ->
            selections.value[YearMonth.of(year, month).toString()].orEmpty().size
        }
    }

    fun yearTotal(year: Int): Int {
        return monthlyCountsForYear(year).sum()
    }

    fun availableYears(defaultYear: Int): List<Int> {
        val years = selections.value.keys
            .mapNotNull { key -> key.substringBefore("-").toIntOrNull() }
            .toMutableSet()
        years += defaultYear
        return years.sorted()
    }

    private fun loadSelections(): Map<String, Set<Int>> {
        return preferences.all.mapNotNull { (key, value) ->
            val parsed = (value as? String)
                .orEmpty()
                .split(",")
                .mapNotNull { token -> token.toIntOrNull() }
                .toSet()
            if (parsed.isEmpty()) {
                null
            } else {
                key to parsed
            }
        }.toMap()
    }

    private fun saveSelections(selections: Map<String, Set<Int>>) {
        preferences.edit().apply {
            clear()
            selections.forEach { (key, days) ->
                putString(key, days.sorted().joinToString(","))
            }
        }.apply()
    }

    private companion object {
        const val PREFS_NAME = "calendar_selections"
    }
}
