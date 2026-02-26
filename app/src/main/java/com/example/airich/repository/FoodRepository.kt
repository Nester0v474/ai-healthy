package com.example.airich.repository

import com.example.airich.data.FoodDatabase
import com.example.airich.data.FoodItem
import com.example.airich.data.MealEntry
import com.example.airich.data.MealEntryWithFood
import com.example.airich.data.MealType
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class FoodRepository(private val database: FoodDatabase) {

    fun getAllFoodItems(): Flow<List<FoodItem>> {
        return database.foodItemDao().getAllFoodItems()
    }

    fun searchFoodItems(query: String): Flow<List<FoodItem>> {

        val escapedQuery = query.replace("%", "\\%").replace("_", "\\_")
        return database.foodItemDao().searchFoodItems(escapedQuery)
    }

    suspend fun getFoodItemById(id: Long): FoodItem? {
        return database.foodItemDao().getFoodItemById(id)
    }

    suspend fun getFoodItemCount(): Int {
        return database.foodItemDao().getFoodItemCount()
    }

    suspend fun insertFoodItem(foodItem: FoodItem): Long {
        return database.foodItemDao().insertFoodItem(foodItem)
    }

    suspend fun insertFoodItems(foodItems: List<FoodItem>) {
        database.foodItemDao().insertFoodItems(foodItems)
    }

    fun getMealEntriesForToday(): Flow<List<MealEntryWithFood>> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_MONTH, 1)
        val endOfDay = calendar.timeInMillis

        return database.mealEntryDao().getMealEntriesWithFoodForDay(startOfDay, endOfDay)
    }

    suspend fun insertMealEntry(mealEntry: MealEntry): Long {
        return database.mealEntryDao().insertMealEntry(mealEntry)
    }

    suspend fun deleteMealEntry(id: Long) {
        database.mealEntryDao().deleteMealEntry(id)
    }

    fun getTodayTimestamp(): Long {
        return System.currentTimeMillis()
    }
}
