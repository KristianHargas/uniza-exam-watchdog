package com.tinko.unizaexamwatchdog.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.tinko.unizaexamwatchdog.domain.Subject

@Entity(tableName = "subjects")
data class DatabaseSubject(
    @PrimaryKey
    @ColumnInfo(name = "subject_id")
    val id: String,
    val name: String,
    val term: String,
    @ColumnInfo(name = "exams_url")
    var examsUrl: String
)

fun List<DatabaseSubject>.asDomainModel(): List<Subject> {
    return map {
        Subject(
            id = it.id,
            name = it.name,
            term = it.term,
            examsUrl = it.examsUrl
        )
    }
}
