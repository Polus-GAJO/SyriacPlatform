package org.syriacplatform.navigation

import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.PrayerSequenceId
import org.syriacplatform.common.types.QoloId

/**
 * Small, platform-independent snapshot of the user's current location.
 *
 * Contains identifiers and presentation state only; canonical content is
 * resolved again through ContentService after restoration.
 */
data class AppSessionState(
    val destination: AppDestination = AppDestination.HOME,
    val occasionId: OccasionId? = null,
    val prayerSequenceId: PrayerSequenceId? = null,
    val liturgicalItemId: LiturgicalItemId? = null,
    val qoloId: QoloId? = null,
    val melodyId: MelodyId? = null,
    val notationViewerOpen: Boolean = false,
    val notationPage: Int = 0
) {
    /**
     * Never restore a destination without its required selection.
     * Missing ancestors fall back to the nearest representable screen.
     * Existence of identifiers in the loaded package is checked separately.
     */
    fun normalized(): AppSessionState {
        val safeDestination = when (destination) {
            AppDestination.HOME -> AppDestination.HOME
            AppDestination.OCCASION_DETAILS ->
                if (occasionId != null) AppDestination.OCCASION_DETAILS else AppDestination.HOME
            AppDestination.PRAYER_DETAILS ->
                if (prayerSequenceId != null && occasionId != null) AppDestination.PRAYER_DETAILS
                else if (occasionId != null) AppDestination.OCCASION_DETAILS else AppDestination.HOME
            AppDestination.HYMN_DETAILS ->
                if (liturgicalItemId != null && prayerSequenceId != null && occasionId != null)
                    AppDestination.HYMN_DETAILS
                else if (prayerSequenceId != null && occasionId != null) AppDestination.PRAYER_DETAILS
                else if (occasionId != null) AppDestination.OCCASION_DETAILS else AppDestination.HOME
            AppDestination.QOLO_DETAILS ->
                if (qoloId != null) AppDestination.QOLO_DETAILS
                else if (occasionId != null) AppDestination.OCCASION_DETAILS else AppDestination.HOME
        }
        val canShowNotation =
            safeDestination == AppDestination.HYMN_DETAILS && melodyId != null
        return copy(
            destination = safeDestination,
            notationViewerOpen = canShowNotation && notationViewerOpen,
            notationPage = if (canShowNotation) notationPage.coerceAtLeast(0) else 0
        )
    }
}
