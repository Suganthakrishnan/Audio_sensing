package com.hear.localtranscription

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File

class LanguagePackDownloadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1L)
        if (id < 0) return

        val prefs = context.getSharedPreferences(LanguagePackManager.PREFS, Context.MODE_PRIVATE)
        val fileName = prefs.getString(LanguagePackManager.downloadKey(id), null) ?: return
        val manager = context.getSystemService(DownloadManager::class.java)
        val cursor = manager.query(DownloadManager.Query().setFilterById(id)) ?: return

        cursor.use {
            if (!it.moveToFirst()) return
            val status = it.getInt(it.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            if (status != DownloadManager.STATUS_SUCCESSFUL) return
            val uriText = it.getString(it.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
            val targetDir = LanguagePackManager(context).modelDirectory()
            targetDir.mkdirs()
            context.contentResolver.openInputStream(Uri.parse(uriText))?.use { input ->
                File(targetDir, fileName).outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            prefs.edit().remove(LanguagePackManager.downloadKey(id)).apply()
        }
    }
}
