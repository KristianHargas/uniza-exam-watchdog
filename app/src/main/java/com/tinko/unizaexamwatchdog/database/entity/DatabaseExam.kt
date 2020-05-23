package com.tinko.unizaexamwatchdog.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tinko.unizaexamwatchdog.domain.Exam
import java.util.*

@Entity(tableName = "exams")
data class DatabaseExam(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "exam_id")
    var id: Int = 0,
    val date: Date,
    val room: String,
    val teacher: String,
    val capacity: Int,
    val note: String,
    @ColumnInfo(name = "subject_id")
    val subjectId: String
)

fun DatabaseExam.asDomainModel(): Exam {
    return Exam(
        date = this.date,
        room = this.room,
        teacher = this.teacher,
        capacity = this.capacity,
        note = this.note,
        subjectId = this.subjectId
    )
}

fun List<DatabaseExam>.asDomainModel(): List<Exam> {
    return map {
        it.asDomainModel()
    }
}