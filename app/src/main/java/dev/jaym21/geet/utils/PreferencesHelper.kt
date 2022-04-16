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

    fun setQueueIds(context: Context, ids: LongArray) {
        val sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES_HELPER, Activity.MODE_PRIVATE)
        val gson = Gson()
        val idsArrayString = gson.toJson(ids)
        val editor = sharedPreferences.edit()
        editor.putString(Constants.QUEUE_IDS, idsArrayString)
        editor.apply()
    }

    fun getQueueIds(context: Context): LongArray {
        val sharedPreferences = context.getSharedPreferences(Constants.PREFERENCES_HELPER, Activity.MODE_PRIVATE)
        val idsArrayStringJson = sharedPreferences.getString(Constants.QUEUE_IDS, null)
        val gson = Gson()
        val list = gson.fromJson(idsArrayStringJson, ArrayList::class.java)
        val longList = mutableListOf<Long>()
        if (list != null) {
            for (i in list) {
                val d = i as Double
                val l = d.toLong()
                longList.add(l)
            }
        }
        return longList.toLongArray()
    }
}