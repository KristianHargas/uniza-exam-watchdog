package com.tinko.unizaexamwatchdog.domain

import java.util.*

data class Exam(
    val date: Date,
    val room: String,
    val teacher: String,
    val capacity: Int,
    val note: String,
    val subjectId: String
)