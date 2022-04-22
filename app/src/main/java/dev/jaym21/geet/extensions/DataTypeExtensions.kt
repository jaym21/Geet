package dev.jaym21.geet.extensions

fun Int.normalizeTrackNumber(): Int {
    var returnValue = this
    while (returnValue >= 1000) {
        returnValue -= 1000
    }
    return returnValue
}