package off.kys.ketabonline2epub

fun String.normalize(): String = this.replace("\n", "<br/>").replace("\r", "")