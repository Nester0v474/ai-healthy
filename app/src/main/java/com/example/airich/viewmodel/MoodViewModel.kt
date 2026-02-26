package com.example.airich.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.airich.data.MoodEntry
import com.example.airich.repository.MoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MoodViewModel(private val repository: MoodRepository) : ViewModel() {

    private val _weeklyMoodData = MutableLiveData<List<MoodDataPoint>>(emptyList())
    val weeklyMoodData: LiveData<List<MoodDataPoint>> = _weeklyMoodData

    private val _recentEntries = MutableLiveData<List<MoodEntry>>(emptyList())
    val recentEntries: LiveData<List<MoodEntry>> = _recentEntries

    private val _hasTodayEntry = MutableLiveData<Boolean>(false)
    val hasTodayEntry: LiveData<Boolean> = _hasTodayEntry

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {

        try {
            loadWeeklyData()
            loadRecentEntries()
            checkTodayEntry()
        } catch (e: Exception) {
            android.util.Log.e("MoodViewModel", "Ошибка в init блоке", e)
            _error.value = "Ошибка инициализации: ${e.message}"
        }
    }

    private fun loadWeeklyData() {
        viewModelScope.launch {
            try {
                repository.getMoodEntriesForLast7Days().collect { entries ->
                    val dataPoints = generateWeeklyDataPoints(entries ?: emptyList())
                    _weeklyMoodData.value = dataPoints
                }
            } catch (e: Exception) {
                android.util.Log.e("MoodViewModel", "Ошибка загрузки данных за неделю", e)
                _weeklyMoodData.value = emptyList()
            }
        }
    }

    private fun loadRecentEntries() {
        viewModelScope.launch {
            try {
                repository.getRecentMoodEntries(10).collect { entries ->
                    _recentEntries.value = entries ?: emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("MoodViewModel", "Ошибка загрузки последних записей", e)
                _recentEntries.value = emptyList()
            }
        }
    }

    private fun checkTodayEntry() {
        viewModelScope.launch {
            try {
                val todayEntry = repository.getMoodEntryForToday()
                _hasTodayEntry.value = todayEntry != null
            } catch (e: Exception) {
                android.util.Log.e("MoodViewModel", "Ошибка проверки записи за сегодня", e)
                _hasTodayEntry.value = false
            }
        }
    }

    fun addMoodEntry(moodScore: Int, note: String?) {
        viewModelScope.launch {
            try {
                if (moodScore < 1 || moodScore > 5) {
                    _error.value = "Оценка настроения должна быть от 1 до 5"
                    return@launch
                }

                val moodEntry = MoodEntry(
                    date = LocalDate.now(),
                    moodScore = moodScore,
                    note = note?.takeIf { it.isNotBlank() }
                )

                repository.insertMoodEntry(moodEntry)
                _error.value = null
                checkTodayEntry()
            } catch (e: Exception) {
                _error.value = "Ошибка при добавлении записи: ${e.message}"
            }
        }
    }

    fun deleteMoodEntry(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteMoodEntry(id)
                checkTodayEntry()
            } catch (e: Exception) {
                _error.value = "Ошибка при удалении записи: ${e.message}"
            }
        }
    }

    private fun generateWeeklyDataPoints(entries: List<MoodEntry>): List<MoodDataPoint> {
        val today = LocalDate.now()
        val dataPoints = mutableListOf<MoodDataPoint>()

        val entriesMap = entries.associateBy { it.date }

        for (i in 6 downTo 0) {
            val date = today.minusDays(i.toLong())
            val entry = entriesMap[date]

            val dayName = when (date.dayOfWeek) {
                DayOfWeek.MONDAY -> "Пн"
                DayOfWeek.TUESDAY -> "Вт"
                DayOfWeek.WEDNESDAY -> "Ср"
                DayOfWeek.THURSDAY -> "Чт"
                DayOfWeek.FRIDAY -> "Пт"
                DayOfWeek.SATURDAY -> "Сб"
                DayOfWeek.SUNDAY -> "Вс"
            }

            dataPoints.add(
                MoodDataPoint(
                    dayName = dayName,
                    date = date,
                    moodScore = entry?.moodScore?.toFloat() ?: 0f,
                    hasEntry = entry != null
                )
            )
        }

        return dataPoints
    }

    data class MoodDataPoint(
        val dayName: String,
        val date: LocalDate,
        val moodScore: Float,
        val hasEntry: Boolean
    )
}

class MoodViewModelFactory(private val repository: MoodRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MoodViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
