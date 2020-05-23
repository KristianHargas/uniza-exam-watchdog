package com.tinko.unizaexamwatchdog.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tinko.unizaexamwatchdog.database.entity.DatabaseExam

/**
 * Data access object interface used by Room database to interact with exams.
 */
@Dao
interface ExamDao {
    @Query("SELECT * FROM exams WHERE subject_id = :subjectId")
    suspend fun getExamsForSubject(subjectId: String): List<DatabaseExam>

    @Insert
    suspend fun insertAll(exams: List<DatabaseExam>)

    @Query("DELETE FROM exams")
    suspend fun deleteAllExams()
}