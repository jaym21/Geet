package dev.jaym21.geet.models

data class Queue(
    var queueTitle: String = "All Songs",
    var queue: LongArray = LongArray(0),
    var currentId: Long = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Queue

        if (queueTitle != other.queueTitle) return false
        if (!queue.contentEquals(other.queue)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = queueTitle.hashCode()
        result = 31 * result + queue.contentHashCode()
        return result
    }
}