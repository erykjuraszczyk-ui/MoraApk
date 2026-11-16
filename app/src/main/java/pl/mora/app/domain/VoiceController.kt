package pl.mora.app.domain

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import java.util.Locale

/**
 * Voice Core 1.3.0
 * Uses Android's installed speech services; no audio is uploaded by this class itself.
 */
class VoiceController(private val activity: Activity) :
    RecognitionListener,
    TextToSpeech.OnInitListener {

    private val recognizer: SpeechRecognizer? =
        if (SpeechRecognizer.isRecognitionAvailable(activity)) {
            SpeechRecognizer.createSpeechRecognizer(activity)
        } else {
            null
        }

    private val tts = TextToSpeech(activity, this)
    private var ttsReady = false
    private var released = false
    private var resultCallback: ((String) -> Unit)? = null

    init {
        recognizer?.setRecognitionListener(this)
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) = Unit
            override fun onDone(utteranceId: String?) = Unit
            @Deprecated("Deprecated by Android")
            override fun onError(utteranceId: String?) = Unit
        })
    }

    fun startListening(onResult: (String) -> Unit): Boolean {
        val engine = recognizer ?: return false
        if (released) return false
        resultCallback = onResult
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, POLISH_LANGUAGE)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, POLISH_LANGUAGE)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, true)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Mów do Mory…")
        }
        return try {
            engine.cancel()
            engine.startListening(intent)
            true
        } catch (_: RuntimeException) {
            resultCallback = null
            false
        }
    }

    fun speak(text: String) {
        if (released || !ttsReady || text.isBlank()) return
        tts.speak(
            text.trim(),
            TextToSpeech.QUEUE_FLUSH,
            null,
            "mora-${System.currentTimeMillis()}"
        )
    }

    fun stopSpeaking() {
        if (!released) tts.stop()
    }

    fun stopListening() {
        resultCallback = null
        recognizer?.cancel()
    }

    fun release() {
        if (released) return
        released = true
        resultCallback = null
        recognizer?.destroy()
        tts.stop()
        tts.shutdown()
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS || released) return
        val polish = tts.setLanguage(Locale.forLanguageTag(POLISH_LANGUAGE))
        ttsReady = polish != TextToSpeech.LANG_MISSING_DATA &&
            polish != TextToSpeech.LANG_NOT_SUPPORTED
        if (ttsReady) {
            tts.setSpeechRate(DEFAULT_SPEECH_RATE)
            tts.setPitch(DEFAULT_PITCH)
        }
    }

    override fun onResults(results: Bundle?) {
        val text = results
            ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            ?.firstOrNull()
            ?.trim()
        if (!text.isNullOrBlank()) resultCallback?.invoke(text)
        resultCallback = null
    }

    override fun onError(error: Int) {
        // Clear callback so a stale recognition result cannot be sent later.
        resultCallback = null
    }

    override fun onReadyForSpeech(params: Bundle?) = Unit
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() = Unit
    override fun onPartialResults(partialResults: Bundle?) = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    private companion object {
        const val POLISH_LANGUAGE = "pl-PL"
        const val DEFAULT_SPEECH_RATE = 1.0f
        const val DEFAULT_PITCH = 1.0f
    }
}
