package dev.jaym21.geet.utils

import android.app.Activity
import android.content.Context
import com.google.gson.Gson

object PreferencesHelper {

    fun setSongSortOrder(context: Context, sortOrder: String) {
        val sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES_HELPER, Activity.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(Constants.SONG_SORT_ORDER, sortOrder)
        editor.apply()
    }

    fun getSongSortOrder(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES_HELPER, Activity.MODE_PRIVATE)
        return sharedPreferences.getString(Constants.SONG_SORT_ORDER, null)
    }
}