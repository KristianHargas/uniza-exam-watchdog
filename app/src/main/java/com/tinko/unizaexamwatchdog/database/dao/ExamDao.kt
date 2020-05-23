package com.tinko.unizaexamwatchdog.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tinko.unizaexamwatchdog.database.entity.DatabaseExam

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams WHERE subject_id = :subject")
    suspend fun getExamsForSubject(subject: String): List<DatabaseExam>

    @Insert
    suspend fun insertAll(exams: List<DatabaseExam>)

    @Query("DELETE FROM exams")
    suspend fun deleteAllExams()
}