package com.example.musicappmvvm.shared

import android.content.Context
import android.content.SharedPreferences
import com.example.musicappmvvm.utils.Constants.MUSIC_SHARED_PREFERENCES

class SharedInstance {
    companion object {
        @Volatile
        var sharedPref: SharedPreferences? = null
        var editor: SharedPreferences.Editor? = null

        fun getSharedPref(context: Context): SharedPreferences {
            if (sharedPref == null) {
                sharedPref =
                    context.getSharedPreferences(MUSIC_SHARED_PREFERENCES, Context.MODE_PRIVATE)
            }
            return sharedPref!!
        }
        /*fun getSharedPref1(context: Context): SharedPreferences? =
            sharedPref ?: synchronized(this) {
                return context.getSharedPreferences(MUSIC_SHARED_PREFERENCES, Context.MODE_PRIVATE)
            }*/

        fun getSharedPrefEditor(context: Context): SharedPreferences.Editor {
            if (sharedPref == null) {
                sharedPref =
                    context.getSharedPreferences(MUSIC_SHARED_PREFERENCES, Context.MODE_PRIVATE)
                editor = sharedPref?.edit()
            }
            return context.getSharedPreferences(MUSIC_SHARED_PREFERENCES, Context.MODE_PRIVATE).edit()
        }

    }
}