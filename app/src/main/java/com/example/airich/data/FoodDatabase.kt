package com.example.airich.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.airich.data.dao.FoodItemDao
import com.example.airich.data.dao.MealEntryDao
import com.example.airich.data.dao.MoodEntryDao
import com.example.airich.data.dao.HealthyTaskEntryDao
import com.example.airich.data.dao.ChatMessageDao
import com.example.airich.data.dao.HabitDao
import com.example.airich.data.dao.HabitCompletionDao
import com.example.airich.data.dao.ReminderDao
import com.example.airich.data.dao.SleepEntryDao

@Database(
    entities = [
        FoodItem::class, MealEntry::class, MoodEntry::class, HealthyTaskEntry::class, ChatMessageEntity::class,
        Habit::class, HabitCompletion::class, Reminder::class, SleepEntry::class
    ],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun moodEntryDao(): MoodEntryDao
    abstract fun healthyTaskEntryDao(): HealthyTaskEntryDao
    abstract fun chatMessageDao(): ChatMessageDao
    abstract fun habitDao(): HabitDao
    abstract fun habitCompletionDao(): HabitCompletionDao
    abstract fun reminderDao(): ReminderDao
    abstract fun sleepEntryDao(): SleepEntryDao

    companion object {
        @Volatile
        private var INSTANCE: FoodDatabase? = null

        fun getDatabase(context: Context): FoodDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = INSTANCE ?: run {
                    try {
                        val appContext = context.applicationContext
                        Room.databaseBuilder(
                            appContext,
                            FoodDatabase::class.java,
                            "food_database"
                        )
                            .fallbackToDestructiveMigration()
                            .build()
                    } catch (e: Exception) {
                        android.util.Log.e("FoodDatabase", "Ошибка создания базы данных", e)
                        android.util.Log.e("FoodDatabase", "Stack trace", e)
                        throw e
                    }
                }
                INSTANCE = instance
                instance
            }
        }
    }
}
