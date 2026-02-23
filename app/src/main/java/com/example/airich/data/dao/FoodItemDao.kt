package com.example.airich.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.airich.data.FoodItem
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodItemDao {
    @Query("SELECT * FROM food_items ORDER BY name ASC")
    fun getAllFoodItems(): Flow<List<FoodItem>>
    
    @Query("SELECT * FROM food_items WHERE name LIKE '%' || :searchQuery || '%' ESCAPE '\\' ORDER BY name ASC")
    fun searchFoodItems(searchQuery: String): Flow<List<FoodItem>>
    
    @Query("SELECT * FROM food_items WHERE id = :id")
    suspend fun getFoodItemById(id: Long): FoodItem?
    
    @Query("SELECT COUNT(*) FROM food_items")
    suspend fun getFoodItemCount(): Int
    
    @Insert
    suspend fun insertFoodItem(foodItem: FoodItem): Long
    
    @Insert
    suspend fun insertFoodItems(foodItems: List<FoodItem>)
}

