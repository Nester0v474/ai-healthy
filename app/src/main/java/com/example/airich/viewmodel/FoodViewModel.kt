package com.example.airich.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.airich.data.FoodItem
import com.example.airich.data.MealEntry
import com.example.airich.data.MealEntryWithFood
import com.example.airich.data.MealType
import com.example.airich.repository.FoodRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FoodViewModel(private val repository: FoodRepository) : ViewModel() {

    private val _mealEntries = MutableLiveData<List<MealEntryWithFood>>(emptyList())
    val mealEntries: LiveData<List<MealEntryWithFood>> = _mealEntries

    private val _searchResults = MutableStateFlow<List<FoodItem>>(emptyList())
    val searchResults: StateFlow<List<FoodItem>> = _searchResults.asStateFlow()

    private val _dailySummary = MutableLiveData<DailySummary>(
        DailySummary(calories = 0.0, protein = 0.0, carbs = 0.0, fat = 0.0)
    )
    val dailySummary: LiveData<DailySummary> = _dailySummary

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    init {

        try {
            loadTodayMeals()
            initializeFoodDatabase()
        } catch (e: Exception) {
            android.util.Log.e("FoodViewModel", "Ошибка в init блоке", e)
            _error.value = "Ошибка инициализации: ${e.message}"
        }
    }

    private fun loadTodayMeals() {
        viewModelScope.launch {
            try {
                repository.getMealEntriesForToday().collect { entries ->
                    _mealEntries.value = entries ?: emptyList()
                    updateDailySummary(entries ?: emptyList())
                }
            } catch (e: Exception) {
                android.util.Log.e("FoodViewModel", "Ошибка загрузки записей", e)
                _mealEntries.value = emptyList()
                updateDailySummary(emptyList())
            }
        }
    }

    private fun updateDailySummary(entries: List<MealEntryWithFood>) {
        val totalCalories = entries.sumOf { it.totalCalories }
        val totalProtein = entries.sumOf { it.totalProtein }
        val totalCarbs = entries.sumOf { it.totalCarbs }
        val totalFat = entries.sumOf { it.totalFat }

        _dailySummary.value = DailySummary(
            calories = totalCalories,
            protein = totalProtein,
            carbs = totalCarbs,
            fat = totalFat
        )
    }

    fun searchFoodItems(query: String) {
        viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
            } else {
                repository.searchFoodItems(query).collect { items ->
                    _searchResults.value = items
                }
            }
        }
    }

    suspend fun insertFoodItem(foodItem: FoodItem): Long {
        return repository.insertFoodItem(foodItem)
    }

    fun addMealEntry(foodId: Long, mealType: MealType, amountInGrams: Double) {
        viewModelScope.launch {
            try {
                val foodItem = repository.getFoodItemById(foodId)
                if (foodItem == null) {
                    _error.value = "Продукт не найден"
                    return@launch
                }

                val mealEntry = MealEntry(
                    date = repository.getTodayTimestamp(),
                    mealType = mealType,
                    foodId = foodId,
                    amountInGrams = amountInGrams
                )

                repository.insertMealEntry(mealEntry)
                _error.value = null
            } catch (e: Exception) {
                _error.value = "Ошибка при добавлении записи: ${e.message}"
            }
        }
    }

    fun deleteMealEntry(id: Long) {
        viewModelScope.launch {
            try {
                repository.deleteMealEntry(id)
            } catch (e: Exception) {
                _error.value = "Ошибка при удалении записи: ${e.message}"
            }
        }
    }

    private fun initializeFoodDatabase() {
        viewModelScope.launch {
            try {

                if (repository.getFoodItemCount() == 0) {

                val defaultFoods = listOf(
                    FoodItem(name = "Куриная грудка", caloriesPer100g = 165.0, protein = 31.0, carbs = 0.0, fat = 3.6),
                    FoodItem(name = "Рис вареный", caloriesPer100g = 130.0, protein = 2.7, carbs = 28.0, fat = 0.3),
                    FoodItem(name = "Овсянка", caloriesPer100g = 389.0, protein = 16.9, carbs = 66.3, fat = 6.9),
                    FoodItem(name = "Яйцо куриное", caloriesPer100g = 155.0, protein = 13.0, carbs = 1.1, fat = 11.0),
                    FoodItem(name = "Бананы", caloriesPer100g = 89.0, protein = 1.1, carbs = 23.0, fat = 0.3),
                    FoodItem(name = "Яблоки", caloriesPer100g = 52.0, protein = 0.3, carbs = 14.0, fat = 0.2),
                    FoodItem(name = "Творог", caloriesPer100g = 101.0, protein = 16.0, carbs = 1.3, fat = 5.0),
                    FoodItem(name = "Гречка", caloriesPer100g = 343.0, protein = 13.3, carbs = 71.5, fat = 3.4),
                    FoodItem(name = "Лосось", caloriesPer100g = 208.0, protein = 20.0, carbs = 0.0, fat = 13.0),
                    FoodItem(name = "Брокколи", caloriesPer100g = 34.0, protein = 2.8, carbs = 7.0, fat = 0.4)
                )
                repository.insertFoodItems(defaultFoods)
                }
            } catch (e: Exception) {
                android.util.Log.e("FoodViewModel", "Ошибка инициализации базы данных", e)
            }
        }
    }

    data class DailySummary(
        val calories: Double,
        val protein: Double,
        val carbs: Double,
        val fat: Double
    )
}

class FoodViewModelFactory(private val repository: FoodRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FoodViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FoodViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
