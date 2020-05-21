package com.tinko.unizaexamwatchdog.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.tinko.unizaexamwatchdog.domain.Exam
import java.util.*

@Entity(tableName = "exams",
        foreignKeys = [ForeignKey(
            entity = DatabaseSubject::class,
            parentColumns = arrayOf("subject_id"),
            childColumns = arrayOf("subject_id"),
            onDelete = ForeignKey.CASCADE
        )]
)
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