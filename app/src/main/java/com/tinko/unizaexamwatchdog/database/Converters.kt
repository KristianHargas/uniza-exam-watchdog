package com.tinko.unizaexamwatchdog.database

import androidx.room.TypeConverter
import java.util.*

// https://developer.android.com/training/data-storage/room/referencing-data?fbclid=IwAR0lKA0Mkk3XQsWw2AVumQDzby3aDPJmdl9PJ5hoEcQX9KoDxdqHzfQFQyA
class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }
}