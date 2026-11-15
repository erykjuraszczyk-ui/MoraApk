package pl.mora.app.data

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

data class MemoryItem(
    val id: Long,
    val text: String,
    val emotion: String,
    val createdAt: Long
)

/**
 * Optional local conversation memory. The complete JSON document is encrypted
 * before it is written to the app-private files directory.
 */
class MemoryRepository(
    context: Context,
    private val vault: SecureVault
) {
    private val file = File(context.filesDir, "mora_memory.enc")

    suspend fun list(): List<MemoryItem> = withContext(Dispatchers.IO) {
        readUnsafe()
    }

    suspend fun add(text: String, emotion: String) = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext
        val entries = readUnsafe().toMutableList()
        entries += MemoryItem(
            id = System.currentTimeMillis(),
            text = text.take(4_000),
            emotion = emotion,
            createdAt = System.currentTimeMillis()
        )
        val json = JSONArray()
        entries.takeLast(250).forEach { item ->
            json.put(
                JSONObject()
                    .put("id", item.id)
                    .put("text", item.text)
                    .put("emotion", item.emotion)
                    .put("createdAt", item.createdAt)
            )
        }
        file.writeText(vault.encrypt(json.toString()), Charsets.UTF_8)
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        if (file.exists()) file.delete()
    }

    private fun readUnsafe(): List<MemoryItem> {
        if (!file.exists()) return emptyList()
        val plain = vault.decrypt(file.readText(Charsets.UTF_8)) ?: return emptyList()
        return try {
            val array = JSONArray(plain)
            (0 until array.length()).mapNotNull { index ->
                val row = array.optJSONObject(index) ?: return@mapNotNull null
                MemoryItem(
                    id = row.optLong("id"),
                    text = row.optString("text"),
                    emotion = row.optString("emotion", "Spokój"),
                    createdAt = row.optLong("createdAt")
                )
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
