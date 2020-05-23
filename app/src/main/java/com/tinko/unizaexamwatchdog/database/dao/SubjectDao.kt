package com.tinko.unizaexamwatchdog.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.tinko.unizaexamwatchdog.database.entity.DatabaseSubject
import java.util.*

/**
 * Data access object interface used by Room database to interact with subjects.
 */
@Dao
interface SubjectDao {
    @Query("SELECT * FROM subjects ORDER BY name")
    fun getAllSubjects(): LiveData<List<DatabaseSubject>>

    @Query("SELECT count(*) FROM subjects")
    suspend fun getSubjectCount(): Int

    @Query("SELECT * FROM subjects WHERE watched = 1")
    suspend fun getWatchedSubjects(): List<DatabaseSubject>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(subjects: List<DatabaseSubject>)

    @Update
    suspend fun updateSubject(subject: DatabaseSubject)

    @Query("UPDATE subjects SET last_check = :date WHERE subject_id = :subjectId")
    suspend fun updateLastCheck(subjectId: String, date: Date)

    @Query("DELETE FROM subjects")
    suspend fun deleteAllSubjects()
}