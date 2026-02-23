package com.example.airich.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.airich.data.SleepEntry
import com.example.airich.repository.SleepRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class SleepViewModel(private val repository: SleepRepository) : ViewModel() {

    val recentEntries = repository.getRecentSleepEntries(14)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addOrUpdateEntry(entry: SleepEntry) {
        viewModelScope.launch {
            repository.insertOrUpdateSleepEntry(entry)
        }
    }

    fun deleteEntry(id: Long) {
        viewModelScope.launch {
            repository.deleteSleepEntry(id)
        }
    }
}

class SleepViewModelFactory(private val repository: SleepRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SleepViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SleepViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
