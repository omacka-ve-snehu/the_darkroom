package cz.cvut.fit.phamgiab.filmdevassistant.core.domain

fun Int.toTimerString() : String {
    val minutes = this/60
    val seconds = this%60

    return String.format("%02d:%02d", minutes, seconds)
}

fun Int.toMinutesSeconds() : Pair<Int, Int> {
    val minutes = this/60
    val seconds = this%60

    return Pair(minutes, seconds)
}

fun String.toTimerInt() : Int {
    val minutes = this.substringBefore(":").toInt()
    val seconds = this.substringAfter(":").toInt()

    return minutes * 60 + seconds
}