package pl.mora.app.data

import android.content.Context

data class AiConfiguration(
    val endpoint: String,
    val model: String
)

class MoraPreferences(context: Context, private val vault: SecureVault) {
    private val prefs = context.getSharedPreferences("mora_preferences", Context.MODE_PRIVATE)

    fun aiConfiguration() = AiConfiguration(
        endpoint = prefs.getString("ai_endpoint", "https://api.openai.com/v1") ?: "",
        model = prefs.getString("ai_model", "") ?: ""
    )

    fun saveAiConfiguration(endpoint: String, model: String, newApiKey: String) {
        prefs.edit()
            .putString("ai_endpoint", endpoint.trim().trimEnd('/'))
            .putString("ai_model", model.trim())
            .apply()
        if (newApiKey.isNotBlank()) {
            prefs.edit().putString("ai_api_key_cipher", vault.encrypt(newApiKey.trim())).apply()
        }
    }

    fun apiKey(): String? =
        prefs.getString("ai_api_key_cipher", null)?.let(vault::decrypt)

    fun hasApiKey(): Boolean = !apiKey().isNullOrBlank()

    fun clearApiKey() {
        prefs.edit().remove("ai_api_key_cipher").apply()
    }

    fun memoryEnabled(): Boolean = prefs.getBoolean("memory_enabled", false)
    fun setMemoryEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("memory_enabled", enabled).apply()
    }

    fun cortanaMode(): Boolean = prefs.getBoolean("cortana_mode", false)
    fun setCortanaMode(enabled: Boolean) {
        prefs.edit().putBoolean("cortana_mode", enabled).apply()
    }

    fun reducedMotion(): Boolean = prefs.getBoolean("reduced_motion", false)
    fun setReducedMotion(enabled: Boolean) {
        prefs.edit().putBoolean("reduced_motion", enabled).apply()
    }
}
