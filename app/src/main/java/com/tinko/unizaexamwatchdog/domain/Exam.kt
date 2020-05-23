package com.tinko.unizaexamwatchdog.domain

import com.tinko.unizaexamwatchdog.database.entity.DatabaseExam
import java.util.*

/**
 * Domain model which holds exam data.
 *
 * This object is used within the application to store data about exam.
 *
 * @property date date of the exam.
 * @property room room name.
 * @property teacher name of the teacher.
 * @property capacity capacity of the exam.
 * @property note note associated with the exam.
 * @property subjectId id of the subject this exam is associated with.
 */
data class Exam(
    val date: Date,
    val room: String,
    val teacher: String,
    val capacity: Int,
    val note: String,
    val subjectId: String
)

/**
 * Extension method which converts [Exam] to database entity used by the database - [DatabaseExam].
 *
 * @return converted [DatabaseExam] object.
 */
fun Exam.asDatabaseModel(): DatabaseExam {
    return DatabaseExam(
        date = this.date,
        room = this.room,
        teacher = this.teacher,
        capacity = this.capacity,
        note = this.note,
        subjectId = this.subjectId
    )
}

/**
 * Extension method which converts list of [Exam] objects to the list of database entity objects - [DatabaseExam].
 *
 * @return converted list of [DatabaseExam] objects.
 */
fun List<Exam>.asDatabaseModel(): List<DatabaseExam> {
    return map {
        it.asDatabaseModel()
    }
}
