package com.tinko.unizaexamwatchdog.network

import com.tinko.unizaexamwatchdog.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import java.io.IOException
import java.util.*

private const val URL_SUBJECTS = "https://vzdelavanie.uniza.sk/vzdelavanie/predmety_s.php"
private const val EXAM_TAG = "riadny termín"

/**
 * This function scrapes HTML document of the user's subjects returned from the server.
 *
 * Uses Jsoup library for scraping.
 *
 * @param sessionCookie active session cookie of the user used for authorisation.
 * @return list of parsed user's subjects.
 */
suspend fun scrapeSubjects(sessionCookie: String): List<Subject> = withContext(Dispatchers.IO) {
    val subjects: MutableList<Subject> = mutableListOf()

    try {
        // fetch HTML document
        val doc: Document = Jsoup.connect(URL_SUBJECTS)
            .header(HEADER_COOKIE_NAME, sessionCookie)
            .get()

        // select all rows from table of user's subjects
        val tableRows: Elements = doc.select("table#id-tabulka-predmety-s table tbody > tr")

        // term of the subject (summer/winter)
        var term: Term? = null

        tableRows.forEach {
            // check header to determine current term (summer or winter)
            // following id is placed on header for subjects belonging to winter term
            if (it.select("td").first().id() == "predmety-s-lng5")
                term = Term.WINTER
            else if (it.select("td").first().id() == "predmety-s-lng6")
                term = Term.SUMMER

            // subject has 5 columns and follows header for winter or summer term
            if (term != null && it.childrenSize() == 5) {
                val subjectIdAndName = it.child(0).text().trim()
                val subjectId = subjectIdAndName.substring(0, subjectIdAndName.indexOf(' ')).trim()
                val subjectName = it.child(0).select("a").text().trim()
                val subjectExamsUrl = it.child(3).select("a").attr("abs:href").trim()

                // create new subject object
                subjects.add(Subject(subjectId, subjectName.capitalize(), term!!, subjectExamsUrl))
            }
        }
    } catch (ex: IOException) {
        subjects.clear()
    }

    subjects
}

/**
 * This function scrapes HTML document of the subject's exams returned from the server.
 *
 * Uses Jsoup library for scraping.
 *
 * @param subject exams of this subject will get scraped.
 * @param sessionCookie active session cookie of the user used for authorisation.
 * @return list of parsed subject's exams.
 */
suspend fun scrapeExams(subject: Subject, sessionCookie: String): List<Exam> =
    withContext(Dispatchers.IO) {
        val exams: MutableList<Exam> = mutableListOf()

        // exception is propagated and caught by the caller
        val doc: Document = Jsoup.connect(subject.examsUrl)
            .header(HEADER_COOKIE_NAME, sessionCookie)
            .get()

        // select all rows holding exam data
        val tableRows: Elements = doc.select("table#id-tabulka-terminy-s table tbody > tr")

        tableRows.forEach {
            // this class is placed on header and also we only seek exam terms
            if (!it.hasClass("hdr6a") && it.child(5).text().trim() == EXAM_TAG) {
                // parse the date in format: '18.05.2020 / 09:00'
                val dateString = it.child(0).text().trim()
                val calendar = Calendar.getInstance()
                calendar.clear()
                calendar.set(
                    dateString.substring(6, 10).toInt(),
                    dateString.substring(3, 5).toInt() - 1,
                    dateString.substring(0, 2).toInt(),
                    dateString.substring(13, 15).toInt(),
                    dateString.substring(16, 18).toInt()
                )

                // get all additional information
                val date = calendar.time
                val room = it.child(1).text().trim()
                val teacher = it.child(2).text().trim()
                val capacity = it.child(3).text().trim().toInt()
                val note = it.child(6).text().trim()

                // create new exam object
                exams.add(Exam(date, room, teacher, capacity, note, subject.id))
            }
        }

        exams
    }
