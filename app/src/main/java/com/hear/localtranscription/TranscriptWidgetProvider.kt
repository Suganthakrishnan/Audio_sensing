package com.hear.localtranscription

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews

class TranscriptWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        ids.forEach { updateWidget(context, manager, it) }
    }

    companion object {
        fun updateAll(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(ComponentName(context, TranscriptWidgetProvider::class.java))
            ids.forEach { updateWidget(context, manager, it) }
        }

        private fun updateWidget(context: Context, manager: AppWidgetManager, id: Int) {
            val store = TranscriptStore(context)
            val views = RemoteViews(context.packageName, R.layout.transcript_widget)
            val text = store.latestText.ifBlank { "Open HEAR and start listening." }
            views.setTextViewText(R.id.widget_title, "Live transcript - ${store.language.displayName}")
            views.setTextViewText(R.id.widget_transcript, text)
            views.setOnClickPendingIntent(
                R.id.widget_transcript,
                PendingIntent.getActivity(
                    context,
                    0,
                    Intent(context, MainActivity::class.java),
                    PendingIntent.FLAG_IMMUTABLE,
                ),
            )
            manager.updateAppWidget(id, views)
        }
    }
}
