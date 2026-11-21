package org.syriacplatform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.syriacplatform.audio.contracts.AudioService
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.content.repository.ApplicationPackageContentRepository
import org.syriacplatform.content.services.DefaultContentService
import org.syriacplatform.packageformat.loading.ApplicationPackageLoader
import org.syriacplatform.packageformat.loading.PackageSource
import org.syriacplatform.packagevalidation.compatibility.CoreCompatibilityDefaults
import org.syriacplatform.user.services.DefaultPreferencesService
import org.syriacplatform.user.storage.InMemoryUserPreferencesStore

/**
 * Shhimo adapter: provides the existing hymn UI with package-backed content.
 * Does not depend on the Occasion navigation or PlatformContext.
 */
@Composable
fun ShhimoHymnDetails(
    source: PackageSource,
    audioService: AudioService,
    itemId: Long,
    onBack: () -> Unit
) {
    val services = remember(source) {
        val content = DefaultContentService(
            ApplicationPackageContentRepository(
                ApplicationPackageLoader(source),
                CoreCompatibilityDefaults.CURRENT
            )
        ).also { it.initialize() }
        val preferences = DefaultPreferencesService(
            InMemoryUserPreferencesStore()
        ).also { it.initialize() }
        object : HymnDetailsServices {
            override val content = content
            override val preferences = preferences
            override val performanceTextPlayback = null
        }
    }
    HymnDetailsScreen(
        services = services,
        audioService = audioService,
        liturgicalItemId = LiturgicalItemId(itemId),
        initialMelodyId = null,
        notationOpen = false,
        notationPage = 0,
        onMelodyChanged = {},
        onNotationChanged = { _, _ -> },
        preferredRecordingIds = emptyMap<MelodyId, MediaAssetId>(),
        onRecordingSelected = { _, _ -> },
        onBack = onBack
    )
}
