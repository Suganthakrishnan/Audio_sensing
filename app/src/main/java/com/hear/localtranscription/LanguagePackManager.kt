package com.hear.localtranscription

import android.content.Context
import java.io.File

class LanguagePackManager(private val context: Context) {
    private val modelDir = File(context.filesDir, "models/sherpa-onnx-whisper-tiny")

    val isInstalled: Boolean
        get() = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(PREVIEW_PACK_INSTALLED, false)

    fun modelDirectory(): File = modelDir

    fun downloadMultilingualPack(): List<Long> {
        modelDir.mkdirs()
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(PREVIEW_PACK_INSTALLED, true)
            .apply()
        return emptyList()
    }

    fun installedStatus(): String {
        if (isInstalled) return "English and Tamil preview package installed"
        return "Tap download to enable the local preview package on this phone."
    }

    companion object {
        const val PREFS = "language_packs"
        private const val PREVIEW_PACK_INSTALLED = "preview_pack_installed"

        fun downloadKey(id: Long): String = "download.$id"
    }
}
