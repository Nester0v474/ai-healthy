package com.example.airich.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "createdAt")
    val createdAt: LocalDate = LocalDate.now()
)

@Entity(tableName = "habit_completions")
data class HabitCompletion(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "habitId")
    val habitId: Long,

    @ColumnInfo(name = "date")
    val date: LocalDate,

    @ColumnInfo(name = "completed")
    val completed: Boolean = true
)
