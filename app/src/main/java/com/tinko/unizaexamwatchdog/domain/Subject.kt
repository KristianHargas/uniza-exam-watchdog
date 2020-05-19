package com.tinko.unizaexamwatchdog.domain

import com.tinko.unizaexamwatchdog.database.DatabaseSubject

const val SUMMER_SUBJECT = "summer"
const val WINTER_SUBJECT = "winter"

data class Subject(
    val id: String,
    val name: String,
    val term: String,
    var examsUrl: String
)

fun List<Subject>.asDatabaseModel(): List<DatabaseSubject> {
    return map {
        DatabaseSubject(
            id = it.id,
            name = it.name,
            term = it.term,
            examsUrl = it.examsUrl
        )
    }
}
