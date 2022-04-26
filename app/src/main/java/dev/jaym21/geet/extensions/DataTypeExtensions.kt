package dev.jaym21.geet.extensions

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity

fun Int.normalizeTrackNumber(): Int {
    var returnValue = this
    while (returnValue >= 1000) {
        returnValue -= 1000
    }
    return returnValue
}

inline val Fragment.safeActivity: FragmentActivity
    get() = activity ?: throw IllegalStateException("Fragment not attached")