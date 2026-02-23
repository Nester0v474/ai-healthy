package com.example.airich.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "meal_entries",
    foreignKeys = [
        ForeignKey(
            entity = FoodItem::class,
            parentColumns = ["id"],
            childColumns = ["foodId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["foodId"])]
)
data class MealEntry(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "date")
    val date: Long, // timestamp в миллисекундах
    
    @ColumnInfo(name = "mealType")
    val mealType: MealType,
    
    @ColumnInfo(name = "foodId")
    val foodId: Long,
    
    @ColumnInfo(name = "amountInGrams")
    val amountInGrams: Double
)

