package com.tinko.unizaexamwatchdog.domain

import com.tinko.unizaexamwatchdog.database.DatabaseExam
import java.util.*

data class Exam(
    val date: Date,
    val room: String,
    val teacher: String,
    val capacity: Int,
    val note: String,
    val subjectId: String
)

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

fun List<Exam>.asDatabaseModel(): List<DatabaseExam> {
    return map {
        it.asDatabaseModel()
    }
}
