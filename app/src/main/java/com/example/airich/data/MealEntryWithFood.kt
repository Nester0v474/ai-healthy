package com.example.airich.data

import androidx.room.Embedded
import androidx.room.Relation

data class MealEntryWithFood(
    @Embedded val mealEntry: MealEntry,
    @Relation(
        parentColumn = "foodId",
        entityColumn = "id"
    )
    val foodItem: FoodItem
) {
    val totalCalories: Double
        get() = (foodItem.caloriesPer100g * mealEntry.amountInGrams) / 100.0

    val totalProtein: Double
        get() = (foodItem.protein * mealEntry.amountInGrams) / 100.0

    val totalCarbs: Double
        get() = (foodItem.carbs * mealEntry.amountInGrams) / 100.0

    val totalFat: Double
        get() = (foodItem.fat * mealEntry.amountInGrams) / 100.0
}
