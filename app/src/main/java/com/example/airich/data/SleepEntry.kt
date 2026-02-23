package com.example.airich.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "sleep_entries")
data class SleepEntry(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "date")
    val date: LocalDate,
    
    @ColumnInfo(name = "bedTime")
    val bedTime: String, // "HH:mm"
    
    @ColumnInfo(name = "wakeTime")
    val wakeTime: String, // "HH:mm"
    
    @ColumnInfo(name = "quality")
    val quality: Int = 3, // 1-5
    
    @ColumnInfo(name = "note")
    val note: String? = null
)
