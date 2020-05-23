package com.tinko.unizaexamwatchdog.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tinko.unizaexamwatchdog.domain.Exam
import java.util.*

/**
 * Database entity which holds exam data.
 *
 * @property id id of the exam.
 * @property date date of the exam.
 * @property room room name.
 * @property teacher name of the teacher.
 * @property capacity capacity of the exam.
 * @property note note associated with the exam.
 * @property subjectId id of the subject this exam is associated with.
 */
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

/**
 * Extension method which converts [DatabaseExam] to domain model used within the application - [Exam].
 *
 * @return converted [Exam] object.
 */
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

/**
 * Extension method which converts list of [DatabaseExam] objects to the list of domain model objects - [Exam].
 *
 * @return converted list of [Exam] objects.
 */
fun List<DatabaseExam>.asDomainModel(): List<Exam> {
    return map {
        it.asDomainModel()
    }
}