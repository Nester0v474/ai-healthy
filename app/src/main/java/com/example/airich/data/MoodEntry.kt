package com.example.airich.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "date")
    val date: LocalDate,

    @ColumnInfo(name = "moodScore")
    val moodScore: Int,

    @ColumnInfo(name = "note")
    val note: String? = null
)
