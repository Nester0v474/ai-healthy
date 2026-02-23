package com.example.airich.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.airich.data.HealthyTaskEntry
import com.example.airich.repository.HealthyTaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

class HealthyTaskViewModel(private val repository: HealthyTaskRepository) : ViewModel() {
    
    // LiveData для всех записей
    private val _allHealthyTaskEntries = MutableLiveData<List<HealthyTaskEntry>>(emptyList())
    val allHealthyTaskEntries: LiveData<List<HealthyTaskEntry>> = _allHealthyTaskEntries
    
    // LiveData для активных записей (не выполненных)
    private val _activeHealthyTaskEntries = MutableLiveData<List<HealthyTaskEntry>>(emptyList())
    val activeHealthyTaskEntries: LiveData<List<HealthyTaskEntry>> = _activeHealthyTaskEntries
    
    // LiveData для выполненных записей
    private val _completedHealthyTaskEntries = MutableLiveData<List<HealthyTaskEntry>>(emptyList())
    val completedHealthyTaskEntries: LiveData<List<HealthyTaskEntry>> = _completedHealthyTaskEntries
    
    // StateFlow для ошибок
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    // StateFlow для фильтра (all, active, completed)
    private val _filter = MutableStateFlow("all")
    val filter: StateFlow<String> = _filter.asStateFlow()
    
    init {
        try {
            loadAllEntries()
            loadActiveEntries()
            loadCompletedEntries()
        } catch (e: Exception) {
            android.util.Log.e("HealthyTaskViewModel", "Ошибка в init блоке", e)
            _error.value = "Ошибка инициализации: ${e.message}"
        }
    }
    
    private fun loadAllEntries() {
        viewModelScope.launch {
            try {
                repository.getAllHealthyTaskEntries().collect { entries ->
                    _allHealthyTaskEntries.value = entries ?: emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("HealthyTaskViewModel", "Ошибка загрузки всех записей", e)
                _allHealthyTaskEntries.value = emptyList()
            }
        }
    }
    
    private fun loadActiveEntries() {
        viewModelScope.launch {
            try {
                repository.getActiveHealthyTaskEntries().collect { entries ->
                    _activeHealthyTaskEntries.value = entries ?: emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("HealthyTaskViewModel", "Ошибка загрузки активных записей", e)
                _activeHealthyTaskEntries.value = emptyList()
            }
        }
    }
    
    private fun loadCompletedEntries() {
        viewModelScope.launch {
            try {
                repository.getCompletedHealthyTaskEntries().collect { entries ->
                    _completedHealthyTaskEntries.value = entries ?: emptyList()
                }
            } catch (e: Exception) {
                android.util.Log.e("HealthyTaskViewModel", "Ошибка загрузки выполненных записей", e)
                _completedHealthyTaskEntries.value = emptyList()
            }
        }
    }
    
    fun addHealthyTaskEntry(title: String, description: String?) {
        viewModelScope.launch {
            try {
                if (title.isBlank()) {
                    _error.value = "Название не может быть пустым"
                    return@launch
                }
                
                val healthyTaskEntry = HealthyTaskEntry(
                    date = LocalDate.now(),
                    title = title.trim(),
                    description = description?.takeIf { it.isNotBlank() }?.trim(),
                    isCompleted = false
                )
                
                repository.insertHealthyTaskEntry(healthyTaskEntry)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Ошибка при добавлении записи: ${e.message}"
            }
        }
    }
    
    fun updateHealthyTaskEntry(healthyTaskEntry: HealthyTaskEntry) {
        viewModelScope.launch {
            try {
                repository.updateHealthyTaskEntry(healthyTaskEntry)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Ошибка при обновлении записи: ${e.message}"
            }
        }
    }
    
    fun deleteHealthyTaskEntry(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteHealthyTaskEntry(id)
            } catch (e: Exception) {
                _error.value = "Ошибка при удалении записи: ${e.message}"
            }
        }
    }
    
    fun toggleCompletionStatus(id: Long, isCompleted: Boolean) {
        viewModelScope.launch {
            try {
                repository.toggleCompletionStatus(id, isCompleted)
            } catch (e: Exception) {
                _error.value = "Ошибка при изменении статуса: ${e.message}"
            }
        }
    }
    
    fun setFilter(filter: String) {
        _filter.value = filter
    }
}

class HealthyTaskViewModelFactory(private val repository: HealthyTaskRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HealthyTaskViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HealthyTaskViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

