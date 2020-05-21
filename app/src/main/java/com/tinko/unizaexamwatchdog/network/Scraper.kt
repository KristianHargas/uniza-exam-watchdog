package com.tinko.unizaexamwatchdog.network

import android.util.Log
import com.tinko.unizaexamwatchdog.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException
import java.time.format.DateTimeFormatter
import java.util.*

const val URL_SUBJECTS = "https://vzdelavanie.uniza.sk/vzdelavanie/predmety_s.php"
const val HEADER_COOKIE_NAME = "Cookie"

suspend fun scrapeSubjects(sessionCookie: String): List<Subject> = withContext(Dispatchers.IO) {
    val subjects: MutableList<Subject> = mutableListOf()

    try {
        val doc: Document = Jsoup.connect(URL_SUBJECTS)
            .header(HEADER_COOKIE_NAME, sessionCookie)
            .get()

        // table of subjects
        val tableRows: Elements = doc.select("table#id-tabulka-predmety-s table tbody > tr")

        // default
        var term: Term? = null

        tableRows.forEach {
            // check header to determine current term (summer or winter)
            if (it.select("td").first().id() == "predmety-s-lng5")
                term = Term.WINTER
            else if (it.select("td").first().id() == "predmety-s-lng6")
                term = Term.SUMMER

            // subject has 5 columns
            if (term != null && it.childrenSize() == 5) {
                val subjectIdAndName = it.child(0).text()
                val subjectId = subjectIdAndName.subSequence(0, subjectIdAndName.indexOf(' ')).toString()
                val subjectName = it.child(0).select("a").text();
                val subjectExamsUrl = it.child(3).select("a").attr("abs:href")

                subjects.add(Subject(subjectId, subjectName.capitalize(), term!!, subjectExamsUrl, watched = false))
            }
        }
    } catch(ex: IOException) {
        Log.e("Scraper", "Error during scraping!")
    }

    return@withContext subjects
}

suspend fun scrapeExams(subject: Subject, sessionCookie: String): List<Exam> = withContext(Dispatchers.IO) {
    val exams: MutableList<Exam> = mutableListOf()

    val doc: Document = Jsoup.connect(subject.examsUrl)
        .header(HEADER_COOKIE_NAME, sessionCookie)
        .get()

    // table of exams
    val tableRows: Elements = doc.select("table#id-tabulka-terminy-s table tbody > tr")

    tableRows.forEach {
        // this class is placed on header and also we only seek exam terms
        if (!it.hasClass("hdr6a") && it.child(5).text().trim() == "riadny termín") {
            // parse the date: '18.05.2020 / 09:00'
            val dateString = it.child(0).text().trim()
            val calendar = Calendar.getInstance()
            calendar.clear()
            calendar.set(
                dateString.subSequence(6, 10).toString().toInt(),
                dateString.subSequence(3, 5).toString().toInt() - 1,
                dateString.subSequence(0, 2).toString().toInt(),
                dateString.subSequence(13, 15).toString().toInt(),
                dateString.subSequence(16, 18).toString().toInt()
            )
            val date = calendar.time

            val room = it.child(1).text().trim()
            val teacher = it.child(2).text().trim()
            val capacity = it.child(3).text().trim().toInt()
            val note = it.child(6).text().trim()

            exams.add(Exam(date, room, teacher, capacity, note, subject.id))
        }
    }

    exams
}
