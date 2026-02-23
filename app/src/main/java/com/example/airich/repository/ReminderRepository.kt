package com.example.airich.repository

import com.example.airich.data.FoodDatabase
import com.example.airich.data.Reminder
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val database: FoodDatabase) {

    fun getAllReminders(): Flow<List<Reminder>> = database.reminderDao().getAllReminders()

    suspend fun getReminderById(id: Long): Reminder? = database.reminderDao().getReminderById(id)

    suspend fun addReminder(reminder: Reminder): Long = database.reminderDao().insertReminder(reminder)

    suspend fun updateReminder(reminder: Reminder) = database.reminderDao().updateReminder(reminder)

    suspend fun deleteReminder(id: Long) = database.reminderDao().deleteReminder(id)
}
