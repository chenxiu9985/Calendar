package com.example.calendarapp.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.YearMonth

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
            put(key, current.toSet())
        }
        _selections.value = updated
        saveSelections(updated)
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
