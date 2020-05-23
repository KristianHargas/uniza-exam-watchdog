package com.tinko.unizaexamwatchdog.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.domain.WINTER_TERM_STRING
import com.tinko.unizaexamwatchdog.domain.Term
import java.util.*

/**
 * Database entity which holds subject data.
 *
 * @property id id of the subject.
 * @property name name of the subject.
 * @property term term of this subject (winter/summer).
 * @property examsUrl url to get exams of this subject from.
 * @property watched indicator whether this subject should be watched by the watchdog or not.
 * @property lastCheck timestamp of last exam check of this subject.
 */
@Entity(tableName = "subjects")
data class DatabaseSubject(
    @PrimaryKey
    @ColumnInfo(name = "subject_id")
    val id: String,
    val name: String,
    val term: String,
    @ColumnInfo(name = "exams_url")
    val examsUrl: String,
    var watched: Boolean = false,
    @ColumnInfo(name = "last_check")
    var lastCheck: Date? = null
)

/**
 * Extension method which converts [DatabaseSubject] to domain model used within the application - [Subject].
 *
 * @return converted [Subject] object.
 */
fun DatabaseSubject.asDomainModel(): Subject {
    return Subject(
        id = this.id,
        name = this.name,
        term = if (this.term == WINTER_TERM_STRING) Term.WINTER else Term.SUMMER,
        examsUrl = this.examsUrl,
        watched = this.watched,
        lastCheck = this.lastCheck
    )
}

/**
 * Extension method which converts list of [DatabaseSubject] objects to the list of domain model objects - [Subject].
 *
 * @return converted list of [Subject] objects.
 */
fun List<DatabaseSubject>.asDomainModel(): List<Subject> {
    return map {
        it.asDomainModel()
    }
}