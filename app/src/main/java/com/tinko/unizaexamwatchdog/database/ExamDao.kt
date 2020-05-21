package com.tinko.unizaexamwatchdog.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface ExamDao {
    @Query("SELECT * FROM exams WHERE subject_id = :subject")
    suspend fun getExamsForSubject(subject: String): List<DatabaseExam>

    @Insert
    suspend fun insertAll(exams: List<DatabaseExam>)
}