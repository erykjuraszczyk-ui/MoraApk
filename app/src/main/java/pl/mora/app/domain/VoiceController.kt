package pl.mora.app.domain

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
import java.util.Locale

data class VoiceOption(
    val id: String,
    val label: String,
    val localeTag: String,
    val requiresNetwork: Boolean
)

data class VoiceSettings(
    val speechRate: Float = 1.0f,
    val pitch: Float = 1.0f,
    val selectedVoiceId: String? = null,
    val ready: Boolean = false
)

data class VoiceUiData(
    val settings: VoiceSettings = VoiceSettings(),
    val voices: List<VoiceOption> = emptyList()
)

/**
 * MORA Voice Core 1.4.0.
 * Speech recognition and synthesis are provided by Android's installed services.
 */
class VoiceController(
    private val activity: Activity,
    private val onVoiceStateChanged: (VoiceUiData) -> Unit = {}
) : RecognitionListener, TextToSpeech.OnInitListener {

    private val preferences = activity.getSharedPreferences("mora_voice_preferences", Context.MODE_PRIVATE)

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

    private var speechRate = preferences.getFloat(KEY_SPEECH_RATE, DEFAULT_SPEECH_RATE)
        .coerceIn(MIN_SPEECH_RATE, MAX_SPEECH_RATE)
    private var pitch = preferences.getFloat(KEY_PITCH, DEFAULT_PITCH)
        .coerceIn(MIN_PITCH, MAX_PITCH)
    private var selectedVoiceId: String? = preferences.getString(KEY_VOICE_ID, null)

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

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, POLISH_LANGUAGE)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, POLISH_LANGUAGE)
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Mów do Mory…")
        }

        return try {
            engine.cancel()
            resultCallback = onResult
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

    fun updateSpeechRate(value: Float): VoiceUiData {
        speechRate = value.coerceIn(MIN_SPEECH_RATE, MAX_SPEECH_RATE)
        preferences.edit().putFloat(KEY_SPEECH_RATE, speechRate).apply()
        if (ttsReady) tts.setSpeechRate(speechRate)
        return publishState()
    }

    fun updatePitch(value: Float): VoiceUiData {
        pitch = value.coerceIn(MIN_PITCH, MAX_PITCH)
        preferences.edit().putFloat(KEY_PITCH, pitch).apply()
        if (ttsReady) tts.setPitch(pitch)
        return publishState()
    }

    fun selectVoice(voiceId: String): VoiceUiData {
        val voice = tts.voices.orEmpty().firstOrNull {
            it.name == voiceId && it.locale.language == POLISH_LANGUAGE_CODE
        } ?: return publishState()

        if (ttsReady) tts.voice = voice
        selectedVoiceId = voice.name
        preferences.edit().putString(KEY_VOICE_ID, selectedVoiceId).apply()
        return publishState()
    }

    fun refreshVoiceOptions(): VoiceUiData = publishState()

    fun release() {
        if (released) return
        released = true
        resultCallback = null
        recognizer?.destroy()
        tts.stop()
        tts.shutdown()
    }

    override fun onInit(status: Int) {
        if (status != TextToSpeech.SUCCESS || released) {
            ttsReady = false
            publishState()
            return
        }

        val polishVoices = tts.voices.orEmpty().filter {
            it.locale.language == POLISH_LANGUAGE_CODE
        }
        val selected = polishVoices.firstOrNull { it.name == selectedVoiceId }
            ?: polishVoices.firstOrNull()

        if (selected != null) {
            tts.voice = selected
            selectedVoiceId = selected.name
            preferences.edit().putString(KEY_VOICE_ID, selectedVoiceId).apply()
            ttsReady = true
        } else {
            val languageResult = tts.setLanguage(Locale.forLanguageTag(POLISH_LANGUAGE))
            ttsReady = languageResult != TextToSpeech.LANG_MISSING_DATA &&
                languageResult != TextToSpeech.LANG_NOT_SUPPORTED
        }

        if (ttsReady) {
            tts.setSpeechRate(speechRate)
            tts.setPitch(pitch)
        }
        publishState()
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
        resultCallback = null
    }

    override fun onReadyForSpeech(params: Bundle?) = Unit
    override fun onBeginningOfSpeech() = Unit
    override fun onRmsChanged(rmsdB: Float) = Unit
    override fun onBufferReceived(buffer: ByteArray?) = Unit
    override fun onEndOfSpeech() = Unit
    override fun onPartialResults(partialResults: Bundle?) = Unit
    override fun onEvent(eventType: Int, params: Bundle?) = Unit

    private fun publishState(): VoiceUiData {
        val voices = tts.voices.orEmpty()
            .filter { it.locale.language == POLISH_LANGUAGE_CODE }
            .sortedBy { it.name }
            .map { it.toOption() }
        val state = VoiceUiData(
            settings = VoiceSettings(
                speechRate = speechRate,
                pitch = pitch,
                selectedVoiceId = selectedVoiceId,
                ready = ttsReady
            ),
            voices = voices
        )
        onVoiceStateChanged(state)
        return state
    }

    private fun Voice.toOption() = VoiceOption(
        id = name,
        label = "${locale.getDisplayName(Locale.forLanguageTag(POLISH_LANGUAGE))} · $name",
        localeTag = locale.toLanguageTag(),
        requiresNetwork = isNetworkConnectionRequired
    )

    private companion object {
        const val POLISH_LANGUAGE = "pl-PL"
        const val POLISH_LANGUAGE_CODE = "pl"
        const val DEFAULT_SPEECH_RATE = 1.0f
        const val DEFAULT_PITCH = 1.0f
        const val MIN_SPEECH_RATE = 0.75f
        const val MAX_SPEECH_RATE = 1.25f
        const val MIN_PITCH = 0.8f
        const val MAX_PITCH = 1.2f
        const val KEY_SPEECH_RATE = "speech_rate"
        const val KEY_PITCH = "speech_pitch"
        const val KEY_VOICE_ID = "speech_voice_id"
    }
}
