package com.tinko.unizaexamwatchdog.database

import android.content.Context
import androidx.room.*
import com.tinko.unizaexamwatchdog.database.dao.ExamDao
import com.tinko.unizaexamwatchdog.database.dao.SubjectDao
import com.tinko.unizaexamwatchdog.database.entity.DatabaseExam
import com.tinko.unizaexamwatchdog.database.entity.DatabaseSubject

private const val DATABASE_NAME = "watchdog-db"

/**
 * Singleton used for getting reference to Room database instance.
 *
 * Provides access to all DAOs (Database Access Objects) of the application.
 *
 * Inspiration: https://codelabs.developers.google.com/codelabs/android-room-with-a-view-kotlin/#6.
 */
@Database(
    entities = [DatabaseSubject::class, DatabaseExam::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
public abstract class MyRoomDatabase : RoomDatabase() {

    /**
     * Database access object for subjects.
     *
     * @return database access object for subjects.
     */
    abstract fun subjectDao(): SubjectDao

    /**
     * Database access object for exams.
     *
     * @return database access object for exams.
     */
    abstract fun examDao(): ExamDao

    companion object {
        @Volatile
        private var INSTANCE: MyRoomDatabase? = null

        /**
         * Static factory method for obtaining database instance.
         *
         * @param context application context.
         * @return database instance.
         */
        fun getDatabase(context: Context): MyRoomDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) return tempInstance
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