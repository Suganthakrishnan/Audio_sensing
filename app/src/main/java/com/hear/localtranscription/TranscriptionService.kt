package com.hear.localtranscription

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread

class TranscriptionService : Service() {
    private val running = AtomicBoolean(false)
    private lateinit var store: TranscriptStore

    override fun onCreate() {
        super.onCreate()
        store = TranscriptStore(this)
        createChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopListening()
            else -> startListening()
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        stopListening()
        super.onDestroy()
    }

    private fun startListening() {
        if (!running.compareAndSet(false, true)) return
        startForeground(NOTIFICATION_ID, notification("Listening locally"))
        thread(name = "hear-transcription") {
            runCaptureLoop()
        }
    }

    private fun stopListening() {
        running.set(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun runCaptureLoop() {
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            publish("Microphone permission is required.")
            running.set(false)
            return
        }

        val language = store.language
        val transcriber = PreviewLocalTranscriber(language)
        val minBuffer = AudioRecord.getMinBufferSize(
            transcriber.sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
        )
        val recorder = AudioRecord(
            MediaRecorder.AudioSource.VOICE_RECOGNITION,
            transcriber.sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBuffer * 2,
        )

        val chunk = ShortArray(minBuffer / 2)
        val window = ArrayList<Float>(transcriber.sampleRate * 5)
        var transcript = store.latestText

        transcriber.use {
            recorder.startRecording()
            while (running.get()) {
                val read = recorder.read(chunk, 0, chunk.size)
                if (read <= 0) continue
                for (i in 0 until read) {
                    window.add(chunk[i] / 32768.0f)
                }
                if (window.size >= transcriber.sampleRate * 2) {
                    val text = it.transcribe(window.toFloatArray())
                    if (text.isNotBlank()) {
                        transcript = appendSegment(transcript, text)
                        publish(transcript)
                    }
                    window.clear()
                }
            }
            recorder.stop()
            recorder.release()
        }
    }

    private fun appendSegment(current: String, segment: String): String {
        if (current.isBlank()) return segment
        return "$current\n$segment"
    }

    private fun publish(text: String) {
        store.latestText = text
        TranscriptWidgetProvider.updateAll(this)
    }

    private fun notification(text: String): Notification {
        val openIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE,
        )
        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, TranscriptionService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE,
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setContentTitle("HEAR")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .setContentIntent(openIntent)
            .addAction(Notification.Action.Builder(android.R.drawable.ic_media_pause, "Stop", stopIntent).build())
            .setOngoing(true)
            .build()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Local transcription",
            NotificationManager.IMPORTANCE_LOW,
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "local_transcription"
        private const val NOTIFICATION_ID = 101
        const val ACTION_STOP = "com.hear.localtranscription.STOP"
    }
}
