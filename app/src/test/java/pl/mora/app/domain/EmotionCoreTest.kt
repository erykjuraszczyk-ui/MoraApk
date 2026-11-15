package pl.mora.app.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class EmotionCoreTest {
    private val core = EmotionCore()

    @Test
    fun detectsJoyFromPolishText() {
        assertEquals(EmotionState.JOY, core.analyze("Cieszę się, wyszło świetnie!").state)
    }

    @Test
    fun detectsSadnessFromPolishText() {
        assertEquals(EmotionState.SADNESS, core.analyze("Jest mi smutno i przykro.").state)
    }

    @Test
    fun defaultsToCalmWithoutSignals() {
        assertEquals(EmotionState.CALM, core.analyze("Jutro mam spotkanie o dziesiątej.").state)
    }

    @Test
    fun exposesAllEightStates() {
        assertEquals(8, EmotionState.entries.size)
    }
}
