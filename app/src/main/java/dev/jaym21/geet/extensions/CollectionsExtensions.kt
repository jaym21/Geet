package dev.jaym21.geet.extensions

fun <T> List<T>?.moveElement(fromIndex: Int, toIndex: Int): List<T> {
    if (this == null) {
        return emptyList()
    }
    return toMutableList().apply {
        add(toIndex, removeAt(fromIndex))
    }
}