package com.hear.localtranscription

enum class Language(val code: String, val displayName: String) {
    English("en", "English"),
    Tamil("ta", "Tamil");

    companion object {
        fun fromCode(code: String): Language = entries.firstOrNull { it.code == code } ?: English
    }
}
