package com.example.airich.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Converters {
    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    }

    @TypeConverter
    fun fromMealType(value: MealType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toMealType(value: String?): MealType? {
        return value?.let { MealType.valueOf(it) }
    }

    @TypeConverter
    @RequiresApi(Build.VERSION_CODES.O)
    fun fromLocalDate(date: LocalDate?): String? {
        return date?.format(dateFormatter)
    }

    @TypeConverter
    @RequiresApi(Build.VERSION_CODES.O)
    fun toLocalDate(dateString: String?): LocalDate? {
        return dateString?.let {
            try {
                LocalDate.parse(it, dateFormatter)
            } catch (_: Exception) {
                null
            }
        }
    }

    @TypeConverter
    @Suppress("UNUSED")
    fun fromBoolean(value: Boolean?): Int {
        return if (value == true) 1 else 0
    }

    @TypeConverter
    @Suppress("UNUSED")
    fun toBoolean(value: Int?): Boolean {
        return value == 1
    }
}
