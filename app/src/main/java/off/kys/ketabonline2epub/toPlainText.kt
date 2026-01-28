package off.kys.ketabonline2epub

import org.jsoup.Jsoup

fun String.toPlainText(): String = Jsoup.parse(this).text()

