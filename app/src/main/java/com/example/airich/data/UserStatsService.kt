package com.example.airich.data

import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.example.airich.data.dao.MealEntryDao
import com.example.airich.data.dao.MoodEntryDao
import com.example.airich.data.dao.HealthyTaskEntryDao
import com.example.airich.data.dao.SleepEntryDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Сервис для получения статистики пользователя для AI-анализа
 */
class UserStatsService(private val context: Context) {
    
    private val database = FoodDatabase.getDatabase(context)
    private val moodDao: MoodEntryDao = database.moodEntryDao()
    private val mealDao: MealEntryDao = database.mealEntryDao()
    private val taskDao: HealthyTaskEntryDao = database.healthyTaskEntryDao()
    private val sleepDao: SleepEntryDao = database.sleepEntryDao()
    
    /**
     * Получает статистику настроения за последние дни
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getMoodStats(days: Int = 7): String = withContext(Dispatchers.IO) {
        try {
            val endDate = LocalDate.now()
            val startDate = endDate.minusDays(days.toLong())
            
            val moodEntries = moodDao.getMoodEntriesForDateRange(startDate, endDate).first()
            
            if (moodEntries.isEmpty()) {
                return@withContext "Нет данных о настроении за последние $days дней."
            }
            
            val avgMood = moodEntries.map { it.moodScore }.average()
            val moodCount = moodEntries.size
            
            val moodTrend = when {
                moodEntries.size >= 2 -> {
                    val recent = moodEntries.takeLast(3).map { it.moodScore }.average()
                    val older = moodEntries.take(moodEntries.size - 3).map { it.moodScore }.average()
                    when {
                        recent > older + 0.5 -> "улучшается"
                        recent < older - 0.5 -> "ухудшается"
                        else -> "стабильное"
                    }
                }
                else -> "недостаточно данных"
            }
            
            val lastMood = moodEntries.last()
            val lastMoodDate = lastMood.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault()))
            
            buildString {
                append("Статистика настроения за последние $days дней:\n")
                append("- Средний балл настроения: ${String.format("%.1f", avgMood)}/5\n")
                append("- Количество записей: $moodCount\n")
                append("- Тренд: настроение $moodTrend\n")
                append("- Последняя запись: ${lastMoodDate} (балл: ${lastMood.moodScore}/5")
                lastMood.note?.let { append(", заметка: $it") }
                append(")\n")
            }
        } catch (e: Exception) {
            android.util.Log.e("UserStatsService", "Ошибка получения статистики настроения", e)
            "Ошибка получения данных о настроении."
        }
    }
    
    /**
     * Получает статистику питания за последние дни
     */
    suspend fun getFoodStats(days: Int = 7): String = withContext(Dispatchers.IO) {
        try {
            val endOfDay = System.currentTimeMillis()
            val startOfDay = endOfDay - (days * 24 * 60 * 60 * 1000L)
            
            // Получаем записи с информацией о продуктах
            val mealEntriesWithFood = mealDao.getMealEntriesWithFoodForDay(startOfDay, endOfDay).first()
            
            if (mealEntriesWithFood.isEmpty()) {
                return@withContext "Нет данных о питании за последние $days дней."
            }
            
            // Группируем по дням
            val mealsByDay = mealEntriesWithFood.groupBy { 
                java.util.Date(it.mealEntry.date).let { date ->
                    java.text.SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(date)
                }
            }
            
            // Подсчитываем популярные продукты
            val foodFrequency = mealEntriesWithFood
                .groupBy { it.foodItem.name }
                .mapValues { it.value.size }
                .toList()
                .sortedByDescending { it.second }
                .take(10)
            
            val totalMeals = mealEntriesWithFood.size
            val daysWithMeals = mealsByDay.size
            val uniqueFoods = mealEntriesWithFood.map { it.foodItem.name }.distinct().size
            
            // Подсчитываем общие калории
            val totalCalories = mealEntriesWithFood.sumOf { it.totalCalories.toLong() }
            val avgCaloriesPerDay = if (daysWithMeals > 0) totalCalories / daysWithMeals else 0
            
            // Получаем последнюю запись
            val lastMeal = mealEntriesWithFood.maxByOrNull { it.mealEntry.date }
            val lastMealDate = lastMeal?.let {
                java.text.SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(java.util.Date(it.mealEntry.date))
            } ?: "нет данных"
            
            buildString {
                append("Статистика питания за последние $days дней:\n")
                append("- Всего приемов пищи: $totalMeals\n")
                append("- Дней с записями: $daysWithMeals\n")
                append("- Разных продуктов: $uniqueFoods\n")
                append("- Средние калории в день: $avgCaloriesPerDay ккал\n")
                append("- Последняя запись о питании: $lastMealDate")
                lastMeal?.let {
                    val mealType = when (it.mealEntry.mealType) {
                        MealType.BREAKFAST -> "Завтрак"
                        MealType.LUNCH -> "Обед"
                        MealType.DINNER -> "Ужин"
                        MealType.SNACK -> "Перекус"
                    }
                    append(" ($mealType: ${it.foodItem.name})\n")
                } ?: append("\n")
                append("\n")
                
                if (foodFrequency.isNotEmpty()) {
                    append("Наиболее часто употребляемые продукты:\n")
                    foodFrequency.take(5).forEachIndexed { index, (foodName, count) ->
                        append("${index + 1}. $foodName - $count раз(а)\n")
                    }
                    append("\n")
                }
                
                // Детальная информация о приемах пищи по датам
                if (mealsByDay.isNotEmpty()) {
                    append("Детальная информация о приемах пищи по датам:\n")
                    val sortedEntries = mealsByDay.entries.toList().sortedByDescending { it.key } // Новые даты сначала
                    sortedEntries.forEach { entry ->
                        val date = entry.key
                        val meals = entry.value
                        append("\n$date:\n")
                        // Группируем по типу приема пищи
                        val mealsByType = meals.groupBy { it.mealEntry.mealType }
                        
                        mealsByType.forEach { (mealType, typeMeals) ->
                            val mealTypeName = when (mealType) {
                                MealType.BREAKFAST -> "Завтрак"
                                MealType.LUNCH -> "Обед"
                                MealType.DINNER -> "Ужин"
                                MealType.SNACK -> "Перекус"
                            }
                            append("  $mealTypeName:\n")
                            typeMeals.forEach { mealWithFood ->
                                val amount = String.format("%.0f", mealWithFood.mealEntry.amountInGrams)
                                val calories = String.format("%.0f", mealWithFood.totalCalories)
                                append("    - ${mealWithFood.foodItem.name} (${amount}г, ${calories} ккал)\n")
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("UserStatsService", "Ошибка получения статистики питания", e)
            "Ошибка получения данных о питании."
        }
    }
    
    /**
     * Получает статистику сна за последние дни
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getSleepStats(days: Int = 7): String = withContext(Dispatchers.IO) {
        try {
            val endDate = LocalDate.now()
            val startDate = endDate.minusDays(days.toLong())
            val sleepEntries = sleepDao.getSleepEntriesForDateRange(startDate, endDate).first()

            if (sleepEntries.isEmpty()) {
                return@withContext "Нет данных о сне за последние $days дней."
            }

            val avgQuality = sleepEntries.map { it.quality }.average()
            val entriesCount = sleepEntries.size

            buildString {
                append("Статистика сна за последние $days дней:\n")
                append("- Записей: $entriesCount\n")
                append("- Средняя оценка качества сна: ${String.format("%.1f", avgQuality)}/5\n")
                append("- Последние записи:\n")
                sleepEntries.take(5).forEachIndexed { i, entry ->
                    append("  ${i + 1}. ${entry.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault()))}: ${entry.bedTime}—${entry.wakeTime}, качество: ${entry.quality}/5")
                    entry.note?.let { append(", заметка: $it") }
                    append("\n")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("UserStatsService", "Ошибка получения статистики сна", e)
            "Нет данных о сне."
        }
    }

    /**
     * Получает статистику задач за последние дни
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getTaskStats(days: Int = 7): String = withContext(Dispatchers.IO) {
        try {
            val endDate = LocalDate.now()
            val startDate = endDate.minusDays(days.toLong())
            
            val taskEntries = taskDao.getHealthyTaskEntriesForDateRange(startDate, endDate).first()
            
            if (taskEntries.isEmpty()) {
                return@withContext "Нет данных о задачах за последние $days дней."
            }
            
            val totalTasks = taskEntries.size
            val completedTasks = taskEntries.count { it.isCompleted }
            val activeTasks = totalTasks - completedTasks
            val completionRate = if (totalTasks > 0) {
                (completedTasks.toDouble() / totalTasks * 100).toInt()
            } else {
                0
            }
            
            // Группируем по дням
            val tasksByDay = taskEntries.groupBy { it.date }
            
            // Получаем активные задачи
            val activeTaskEntries = taskDao.getActiveHealthyTaskEntries().first()
            val activeTasksList = activeTaskEntries.take(5)
            
            // Получаем последние выполненные задачи
            val completedTaskEntries = taskDao.getCompletedHealthyTaskEntries().first()
            val recentCompleted = completedTaskEntries.take(5)
            
            buildString {
                append("Статистика задач за последние $days дней:\n")
                append("- Всего задач: $totalTasks\n")
                append("- Выполнено: $completedTasks\n")
                append("- Активных: $activeTasks\n")
                append("- Процент выполнения: $completionRate%\n")
                append("- Дней с задачами: ${tasksByDay.size}\n\n")
                
                if (activeTasksList.isNotEmpty()) {
                    append("Активные задачи:\n")
                    activeTasksList.forEachIndexed { index, task ->
                        val dateStr = task.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault()))
                        append("${index + 1}. ${task.title}")
                        task.description?.let { append(" - $it") }
                        append(" (создана: $dateStr)\n")
                    }
                    append("\n")
                }
                
                if (recentCompleted.isNotEmpty()) {
                    append("Последние выполненные задачи:\n")
                    recentCompleted.take(3).forEachIndexed { index, task ->
                        val dateStr = task.date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault()))
                        append("${index + 1}. ${task.title} (выполнена: $dateStr)\n")
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("UserStatsService", "Ошибка получения статистики задач", e)
            "Ошибка получения данных о задачах."
        }
    }
    
    /**
     * Получает полную статистику пользователя для AI
     */
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun getFullUserStats(): String = withContext(Dispatchers.IO) {
        buildString {
            append("=== ДАННЫЕ ПОЛЬЗОВАТЕЛЯ ===\n\n")
            append(getMoodStats(7))
            append("\n")
            append(getFoodStats(7))
            append("\n")
            append(getSleepStats(7))
            append("\n")
            append(getTaskStats(7))
            append("\n")
        }
    }
}

