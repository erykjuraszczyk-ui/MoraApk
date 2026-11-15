package pl.mora.app.domain

import pl.mora.app.data.MoraPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

data class ChatMessage(
    val role: String,
    val content: String,
    val id: Long = System.nanoTime()
)

class AssistantRepository(private val preferences: MoraPreferences) {
    suspend fun reply(history: List<ChatMessage>): String = withContext(Dispatchers.IO) {
        val config = preferences.aiConfiguration()
        val apiKey = preferences.apiKey()
        require(config.endpoint.startsWith("https://", ignoreCase = true)) {
            "Endpoint musi używać HTTPS."
        }
        require(config.model.isNotBlank()) { "Wpisz identyfikator modelu w Ustawieniach." }
        require(!apiKey.isNullOrBlank()) { "Dodaj klucz API w Ustawieniach." }

        val endpoint = if (config.endpoint.endsWith("/chat/completions")) {
            config.endpoint
        } else {
            "${config.endpoint.trimEnd('/')}/chat/completions"
        }

        val messages = JSONArray()
            .put(
                JSONObject()
                    .put("role", "system")
                    .put(
                        "content",
                        "Jesteś Morą, polskojęzycznym asystentem AI. Odpowiadaj jasno, życzliwie i konkretnie. " +
                            "Nie udawaj dostępu do telefonu ani funkcji, których aplikacja nie udostępnia."
                    )
            )
        history.takeLast(14).forEach { message ->
            messages.put(
                JSONObject()
                    .put("role", if (message.role == "assistant") "assistant" else "user")
                    .put("content", message.content)
            )
        }

        val body = JSONObject()
            .put("model", config.model)
            .put("messages", messages)
            .put("temperature", 0.7)

        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20_000
            readTimeout = 60_000
            doOutput = true
            setRequestProperty("Authorization", "Bearer $apiKey")
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }

        try {
            connection.outputStream.use { it.write(body.toString().toByteArray(Charsets.UTF_8)) }
            val status = connection.responseCode
            val stream = if (status in 200..299) connection.inputStream else connection.errorStream
            val response = stream?.use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).readText()
            }.orEmpty()

            if (status !in 200..299) {
                val detail = runCatching {
                    JSONObject(response).optJSONObject("error")?.optString("message")
                }.getOrNull().orEmpty()
                throw IllegalStateException(
                    if (detail.isNotBlank()) "Serwer AI ($status): $detail" else "Serwer AI zwrócił HTTP $status."
                )
            }

            val content = JSONObject(response)
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .optString("content")
                .trim()

            content.ifBlank { "Otrzymałam pustą odpowiedź. Spróbuj ponownie." }
        } finally {
            connection.disconnect()
        }
    }
}
