package com.hear.localtranscription

interface LocalTranscriber : AutoCloseable {
    val sampleRate: Int

    fun transcribe(samples: FloatArray): String
}

class PreviewLocalTranscriber(
    private val language: Language,
) : LocalTranscriber {
    override val sampleRate: Int = SAMPLE_RATE
    private var segment = 0

    override fun transcribe(samples: FloatArray): String {
        if (samples.isEmpty()) return ""
        val rms = kotlin.math.sqrt(samples.fold(0.0) { sum, sample -> sum + sample * sample } / samples.size)
        segment += 1
        val level = when {
            rms > 0.08 -> "clear speech"
            rms > 0.02 -> "soft speech"
            else -> "quiet audio"
        }
        return when (language) {
            Language.English -> "Preview transcript $segment: local microphone captured $level on this phone."
            Language.Tamil -> "முன்னோட்ட உரை $segment: இந்த போனில் உள்ளூர் மைக்ரோஃபோன் ஒலி பதிவானது."
        }
    }

    override fun close() {
    }

    companion object {
        const val SAMPLE_RATE = 16000
    }
}
