package dev.jaym21.geet.models

import android.provider.MediaStore

enum class SortType(val rawValue: String) {
    A_Z(MediaStore.Audio.Media.DEFAULT_SORT_ORDER),
    Z_A(MediaStore.Audio.Media.DEFAULT_SORT_ORDER + " DESC"),
    YEAR(MediaStore.Audio.Media.YEAR + " DESC"),
    DURATION(MediaStore.Audio.Media.DURATION + " DESC");

    companion object {
        fun fromString(raw: String): SortType {
            return SortType.values().single { it.rawValue == raw }
        }

        fun toString(value: SortType): String = value.rawValue
    }
}