package com.tinko.unizaexamwatchdog.network

import com.tinko.unizaexamwatchdog.domain.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements

const val URL_SUBJECTS = "https://vzdelavanie.uniza.sk/vzdelavanie/predmety_s.php"
const val HEADER_COOKIE_NAME = "Cookie"

suspend fun scrapeSubjects(sessionCookie: String): List<Subject> = withContext(Dispatchers.IO) {
    val subjects: MutableList<Subject> = mutableListOf()

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

            subjects.add(Subject(subjectId, subjectName.capitalize(), term!!, subjectExamsUrl))
        }
    }

    return@withContext subjects
}
