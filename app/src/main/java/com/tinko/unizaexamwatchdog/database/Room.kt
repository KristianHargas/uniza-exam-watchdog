package com.tinko.unizaexamwatchdog.database

import android.content.Context
import androidx.room.*
import com.tinko.unizaexamwatchdog.database.dao.ExamDao
import com.tinko.unizaexamwatchdog.database.dao.SubjectDao
import com.tinko.unizaexamwatchdog.database.entity.DatabaseExam
import com.tinko.unizaexamwatchdog.database.entity.DatabaseSubject

private const val DATABASE_NAME = "watchdog-db"

// Annotates class to be a Room Database with a table (entity) of the Word class
// https://codelabs.developers.google.com/codelabs/android-room-with-a-view-kotlin/#6
@Database(entities = [DatabaseSubject::class, DatabaseExam::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
public abstract class MyRoomDatabase : RoomDatabase() {

    abstract fun subjectDao(): SubjectDao
    abstract fun examDao(): ExamDao

    companion object {
        // Singleton prevents multiple instances of database opening at the
        // same time.
        @Volatile
        private var INSTANCE: MyRoomDatabase? = null

        fun getDatabase(context: Context): MyRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MyRoomDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}