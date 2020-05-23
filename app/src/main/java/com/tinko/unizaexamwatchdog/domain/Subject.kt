package com.tinko.unizaexamwatchdog.domain

import com.tinko.unizaexamwatchdog.database.entity.DatabaseSubject
import java.util.*

/**
 * [String] representation of [Term.SUMMER] used by the database and preferences.
 */
const val SUMMER_TERM_STRING = "summer"

/**
 * [String] representation of [Term.WINTER] used by the database and preferences.
 */
const val WINTER_TERM_STRING = "winter"

/**
 * Enum holding information regarding the term of the subject (summer/winter).
 *
 * @property label [String] representation.
 */
enum class Term(private val label: String) {
    SUMMER(SUMMER_TERM_STRING),
    WINTER(WINTER_TERM_STRING);

    override fun toString(): String = label
}

/**
 * Domain model which holds subject data.
 *
 * This object is used within the application to store data about subject.
 *
 * @property id id of the subject.
 * @property name name of the subject.
 * @property term term of this subject (winter/summer).
 * @property examsUrl url to get exams of this subject from.
 * @property watched indicator whether this subject should be watched by the watchdog or not.
 * @property lastCheck timestamp of last exam check of this subject.
 */
data class Subject(
    val id: String,
    val name: String,
    val term: Term,
    val examsUrl: String,
    var watched: Boolean = false,
    var lastCheck: Date? = null
)

/**
 * Extension method which converts [Subject] to database entity used by the database - [DatabaseSubject].
 *
 * @return converted [DatabaseSubject] object.
 */
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

/**
 * Extension method which converts list of [Subject] objects to the list of database entity objects - [DatabaseSubject].
 *
 * @return converted list of [DatabaseSubject] objects.
 */
fun List<Subject>.asDatabaseModel(): List<DatabaseSubject> {
    return map {
        it.asDatabaseModel()
    }
}

/**
 * Extension method which filters the list of subjects according to provided [Term].
 *
 * @return filtered list of subjects.
 */
fun List<Subject>.filter(term: Term): List<Subject> {
    return filter {
        it.term == term
    }
}
