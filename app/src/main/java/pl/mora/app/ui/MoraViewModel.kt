package pl.mora.app.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import android.app.Application
import pl.mora.app.data.MemoryItem
import pl.mora.app.data.MemoryRepository
import pl.mora.app.data.MoraPreferences
import pl.mora.app.data.SecureVault
import pl.mora.app.domain.AssistantRepository
import pl.mora.app.domain.ChatMessage
import pl.mora.app.domain.EmotionCore
import pl.mora.app.domain.EmotionResult
import pl.mora.app.domain.EmotionState
import kotlinx.coroutines.launch

data class MoraUiState(
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            role = "assistant",
            content = "Cześć! Jestem Mora. Możemy rozmawiać tekstowo lub głosowo. Skonfiguruj model AI w Ustawieniach, aby rozpocząć pełną rozmowę."
        )
    ),
    val emotion: EmotionResult = EmotionResult(EmotionState.CALM, 0.45f, 0),
    val cortanaMode: Boolean = false,
    val memoryEnabled: Boolean = false,
    val memories: List<MemoryItem> = emptyList(),
    val endpoint: String = "https://api.openai.com/v1",
    val model: String = "",
    val apiKeyConfigured: Boolean = false,
    val reducedMotion: Boolean = false,
    val sending: Boolean = false,
    val statusMessage: String? = null
)

class MoraViewModel(
    private val preferences: MoraPreferences,
    private val memoryRepository: MemoryRepository,
    private val assistantRepository: AssistantRepository
) : ViewModel() {
    var uiState by mutableStateOf(MoraUiState())
        private set

    private val emotionCore = EmotionCore()

    init {
        refreshSettings()
        refreshMemory()
    }

    fun refreshSettings() {
        val config = preferences.aiConfiguration()
        uiState = uiState.copy(
            endpoint = config.endpoint,
            model = config.model,
            apiKeyConfigured = preferences.hasApiKey(),
            memoryEnabled = preferences.memoryEnabled(),
            cortanaMode = preferences.cortanaMode(),
            reducedMotion = preferences.reducedMotion()
        )
    }

    fun sendMessage(rawText: String) {
        val text = rawText.trim()
        if (text.isBlank() || uiState.sending) return

        val emotion = emotionCore.analyze(text)
        val history = (uiState.messages + ChatMessage("user", text)).takeLast(16)
        val persist = uiState.memoryEnabled
        uiState = uiState.copy(
            messages = history,
            emotion = emotion,
            sending = true,
            statusMessage = null
        )

        viewModelScope.launch {
            if (persist) memoryRepository.add("Użytkownik: $text", emotion.state.label)

            try {
                val answer = assistantRepository.reply(history)
                uiState = uiState.copy(
                    messages = (uiState.messages + ChatMessage("assistant", answer)).takeLast(30),
                    statusMessage = null
                )
                if (persist) memoryRepository.add("Mora: $answer", emotion.state.label)
            } catch (error: Exception) {
                uiState = uiState.copy(
                    messages = (uiState.messages + ChatMessage(
                        "assistant",
                        "Nie mogę teraz połączyć się z modelem AI. ${error.message ?: "Sprawdź konfigurację i połączenie."}"
                    )).takeLast(30),
                    statusMessage = "Sprawdź endpoint HTTPS, model, klucz API i połączenie internetowe."
                )
            } finally {
                uiState = uiState.copy(sending = false)
                refreshMemory()
            }
        }
    }

    fun saveAiConfiguration(endpoint: String, model: String, apiKey: String) {
        val normalized = endpoint.trim().trimEnd('/')
        if (!normalized.startsWith("https://", ignoreCase = true)) {
            uiState = uiState.copy(statusMessage = "Wymagany jest endpoint HTTPS.")
            return
        }
        if (model.isBlank()) {
            uiState = uiState.copy(statusMessage = "Wpisz nazwę modelu.")
            return
        }
        preferences.saveAiConfiguration(normalized, model, apiKey)
        refreshSettings()
        uiState = uiState.copy(
            statusMessage = if (preferences.hasApiKey()) "Konfiguracja AI została zapisana." else "Zapisano endpoint i model. Dodaj klucz API."
        )
    }

    fun clearApiKey() {
        preferences.clearApiKey()
        refreshSettings()
        uiState = uiState.copy(statusMessage = "Usunięto lokalnie zapisany klucz API.")
    }

    fun setMemoryEnabled(enabled: Boolean) {
        preferences.setMemoryEnabled(enabled)
        uiState = uiState.copy(
            memoryEnabled = enabled,
            statusMessage = if (enabled) "Pamięć włączona. Nowe wpisy będą szyfrowane lokalnie." else "Pamięć wyłączona. Dotychczasowe wpisy pozostają do ręcznego usunięcia."
        )
    }

    fun refreshMemory() {
        viewModelScope.launch {
            uiState = uiState.copy(memories = memoryRepository.list())
        }
    }

    fun clearMemory() {
        viewModelScope.launch {
            memoryRepository.clear()
            uiState = uiState.copy(memories = emptyList(), statusMessage = "Lokalna pamięć została usunięta.")
        }
    }

    fun setCortanaMode(enabled: Boolean) {
        preferences.setCortanaMode(enabled)
        uiState = uiState.copy(cortanaMode = enabled)
    }

    fun setReducedMotion(enabled: Boolean) {
        preferences.setReducedMotion(enabled)
        uiState = uiState.copy(reducedMotion = enabled)
    }
}

class MoraViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val vault = SecureVault()
        val preferences = MoraPreferences(application, vault)
        val memory = MemoryRepository(application, vault)
        val assistant = AssistantRepository(preferences)
        return MoraViewModel(preferences, memory, assistant) as T
    }
}
