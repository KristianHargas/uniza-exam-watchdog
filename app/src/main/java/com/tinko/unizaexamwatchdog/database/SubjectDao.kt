package com.tinko.unizaexamwatchdog.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY term DESC")
    fun getAllSubjects(): LiveData<List<DatabaseSubject>>

    @Query("SELECT count(*) FROM subjects")
    suspend fun getSubjectCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subjects: List<DatabaseSubject>)
}