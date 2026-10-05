package com.hear.localtranscription

import android.content.Context

class TranscriptStore(context: Context) {
    private val prefs = context.getSharedPreferences("transcript", Context.MODE_PRIVATE)

    var latestText: String
        get() = prefs.getString("latestText", "") ?: ""
        set(value) = prefs.edit().putString("latestText", value.takeLast(4000)).apply()

    var language: Language
        get() = Language.fromCode(prefs.getString("language", Language.English.code) ?: Language.English.code)
        set(value) = prefs.edit().putString("language", value.code).apply()

    fun clear() {
        latestText = ""
    }
}
