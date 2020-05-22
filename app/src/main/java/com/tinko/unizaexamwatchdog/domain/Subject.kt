package com.tinko.unizaexamwatchdog.domain

import com.tinko.unizaexamwatchdog.database.DatabaseSubject
import java.util.*

const val SUMMER_TERM_STRING = "summer"
const val WINTER_TERM_STRING = "winter"

enum class Term(private val label: String) {
    SUMMER(SUMMER_TERM_STRING),
    WINTER(WINTER_TERM_STRING);

    override fun toString(): String = label
}

data class Subject(
    val id: String,
    val name: String,
    val term: Term,
    val examsUrl: String,
    var watched: Boolean = false,
    var lastCheck: Date? = null
)

fun List<Subject>.asDatabaseModel(): List<DatabaseSubject> {
    return map {
        it.asDatabaseModel()
    }
}

fun Subject.asDatabaseModel(): DatabaseSubject {
    return DatabaseSubject(
        id = this.id,
        name = this.name,
        term = this.term.toString(),
        examsUrl = this.examsUrl,
        watched = this.watched,
        lastCheck = this.lastCheck
    )
}

fun List<Subject>.filter(term: Term): List<Subject> {
    return filter {
        it.term == term
    }
}

fun List<Subject>.hasSameSubjectsAs(subjects: List<Subject>): Boolean {
    if (this.size == subjects.size) {
        var match = true
        this.forEachIndexed { index, subject ->
            val otherSubject = subjects[index]
            if (subject.id != otherSubject.id) {
                match = false
            }
        }

        return match
    }

    return false
}