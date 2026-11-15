package org.syriacplatform.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.PrayerSequenceId

class AppSessionStateTest {
    @Test
    fun startsAtHome() {
        assertEquals(AppDestination.HOME, AppSessionState().normalized().destination)
    }

    @Test
    fun completeHymnSelectionSurvivesNormalization() {
        val original = AppSessionState(
            destination = AppDestination.HYMN_DETAILS,
            occasionId = OccasionId(379),
            prayerSequenceId = PrayerSequenceId(12),
            liturgicalItemId = LiturgicalItemId(4132),
            melodyId = MelodyId(5),
            notationViewerOpen = true,
            notationPage = 2
        )
        assertEquals(original, original.normalized())
    }

    @Test
    fun missingHymnSelectionFallsBackToPrayer() {
        val restored = AppSessionState(
            destination = AppDestination.HYMN_DETAILS,
            occasionId = OccasionId(379),
            prayerSequenceId = PrayerSequenceId(12),
            notationViewerOpen = true,
            notationPage = 3
        ).normalized()

        assertEquals(AppDestination.PRAYER_DETAILS, restored.destination)
        assertFalse(restored.notationViewerOpen)
        assertEquals(0, restored.notationPage)
    }

    @Test
    fun invalidNotationStateIsClosedAndPageClamped() {
        val restored = AppSessionState(
            destination = AppDestination.HYMN_DETAILS,
            occasionId = OccasionId(379),
            prayerSequenceId = PrayerSequenceId(12),
            liturgicalItemId = LiturgicalItemId(4132),
            melodyId = MelodyId(5),
            notationViewerOpen = true,
            notationPage = -4
        ).normalized()

        assertTrue(restored.notationViewerOpen)
        assertEquals(0, restored.notationPage)
    }
}
