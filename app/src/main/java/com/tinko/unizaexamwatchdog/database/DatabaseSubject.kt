package com.tinko.unizaexamwatchdog.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tinko.unizaexamwatchdog.domain.Subject
import com.tinko.unizaexamwatchdog.domain.WINTER_TERM_STRING
import com.tinko.unizaexamwatchdog.domain.Term

@Entity(tableName = "subjects")
data class DatabaseSubject(
    @PrimaryKey
    @ColumnInfo(name = "subject_id")
    val id: String,
    val name: String,
    val term: String,
    @ColumnInfo(name = "exams_url")
    val examsUrl: String,
    var watched: Boolean
)

fun List<DatabaseSubject>.asDomainModel(): List<Subject> {
    return map {
        it.asDomainModel()
    }
}

fun DatabaseSubject.asDomainModel(): Subject {
    return Subject(
        id = this.id,
        name = this.name,
        term = if (this.term == WINTER_TERM_STRING) Term.WINTER else Term.SUMMER,
        examsUrl = this.examsUrl,
        watched = this.watched
    )
}