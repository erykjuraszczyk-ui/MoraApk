package pl.mora.app.domain

import java.util.Locale

enum class EmotionState(
    val label: String,
    val argb: Long
) {
    CALM("Spokój", 0xFF77B8FF),
    JOY("Radość", 0xFFFFD166),
    SADNESS("Smutek", 0xFF7197E8),
    ANGER("Złość", 0xFFFF5A62),
    FEAR("Strach", 0xFFB39DDB),
    SURPRISE("Zaskoczenie", 0xFF69E0D0),
    DISGUST("Odraza", 0xFF9BC66B),
    DETERMINATION("Determinacja", 0xFFFF8A50)
}

data class EmotionResult(
    val state: EmotionState,
    val confidence: Float,
    val matchedSignals: Int
)

/**
 * Transparent keyword-based text heuristic. It is not a diagnosis and does
 * not claim to know the user's internal emotional state.
 */
class EmotionCore {
    private val signals = linkedMapOf(
        EmotionState.JOY to listOf("cieszę", "rados", "świetnie", "super", "wspaniale", "udało się", "haha", "😊", "😁"),
        EmotionState.SADNESS to listOf("smut", "przykro", "przygnęb", "samot", "płacz", "beznadziej", "😢", "☹"),
        EmotionState.ANGER to listOf("wkur", "zły", "zła", "złości", "iryt", "denerw", "nienaw", "😡", "🤬"),
        EmotionState.FEAR to listOf("boję", "boje", "strach", "lęk", "lek", "niepokój", "obaw", "😨"),
        EmotionState.SURPRISE to listOf("niespodz", "zaskocz", "naprawdę?!", "wow", "ojej", "😮", "😲"),
        EmotionState.DISGUST to listOf("obrzyd", "wstręt", "wstret", "odraż", "odraz", "mdli", "🤢"),
        EmotionState.DETERMINATION to listOf("zrobię", "dam radę", "dam rade", "działam", "konsekwent", "postanawiam", "wytrwam", "💪")
    )

    fun analyze(text: String): EmotionResult {
        val normalized = text.lowercase(Locale.forLanguageTag("pl-PL"))
        val scores = signals.mapValues { (_, words) ->
            words.count { normalized.contains(it) }
        }
        val winner = scores.maxByOrNull { it.value }
        val hits = winner?.value ?: 0
        if (hits == 0) {
            return EmotionResult(EmotionState.CALM, 0.45f, 0)
        }
        val confidence = (0.55f + hits * 0.12f).coerceAtMost(0.91f)
        return EmotionResult(winner!!.key, confidence, hits)
    }
}
