package com.example.airich.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "food_items")
data class FoodItem(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "calories_per_100g")
    val caloriesPer100g: Double,
    
    @ColumnInfo(name = "protein")
    val protein: Double, // в граммах на 100г
    
    @ColumnInfo(name = "carbs")
    val carbs: Double,   // в граммах на 100г
    
    @ColumnInfo(name = "fat")
    val fat: Double      // в граммах на 100г
)

