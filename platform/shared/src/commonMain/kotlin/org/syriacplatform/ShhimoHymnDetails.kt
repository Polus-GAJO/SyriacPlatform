package org.syriacplatform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.syriacplatform.audio.contracts.AudioService
import org.syriacplatform.audio.PerformanceTextOccurrencePlaybackController
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.QintoId
import org.syriacplatform.content.repository.ContentRepository
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
    selectedMelodyId: Long?,
    notationOpen: Boolean,
    notationPage: Int,
    recordingIds: Map<Long, Long>,
    onMelodyChanged: (Long?) -> Unit,
    onNotationChanged: (Boolean, Int) -> Unit,
    onRecordingSelected: (Long, Long) -> Unit,
    onBack: () -> Unit,
    onNextItem: (() -> Unit)? = null,
    weeklyQintoId: Long = 1L
) {
    val repository = remember(source) {
        ApplicationPackageContentRepository(ApplicationPackageLoader(source), CoreCompatibilityDefaults.CURRENT)
    }
    val services = remember(repository, audioService, weeklyQintoId) {
        val selectedQinto = QintoId(weeklyQintoId)
        val scopedRepository = object : ContentRepository by repository {
            override suspend fun loadLiturgicalItem(id: LiturgicalItemId) =
                repository.loadLiturgicalItemForQinto(id, selectedQinto)
            override suspend fun loadPerformanceMedia(liturgicalItemId: LiturgicalItemId) =
                repository.loadPerformanceMediaForQinto(liturgicalItemId, selectedQinto)
        }
        val contentService = DefaultContentService(scopedRepository).also { it.initialize() }
        val preferencesService = DefaultPreferencesService(
            InMemoryUserPreferencesStore()
        ).also { it.initialize() }
        object : HymnDetailsServices {
            override val content = contentService
            override val preferences = preferencesService
            override val performanceTextPlayback = PerformanceTextOccurrencePlaybackController(contentService, audioService)
            override val loadNotationBytes: (suspend (String) -> ByteArray?) = { path -> source.readBytesOrNull(path) }
        }
    }
    HymnDetailsScreen(
        services = services,
        audioService = audioService,
        liturgicalItemId = LiturgicalItemId(itemId),
        initialMelodyId = selectedMelodyId?.let(::MelodyId),
        notationOpen = notationOpen,
        notationPage = notationPage,
        onMelodyChanged = { onMelodyChanged(it?.value) },
        onNotationChanged = onNotationChanged,
        preferredRecordingIds = recordingIds.mapKeys { MelodyId(it.key) }.mapValues { MediaAssetId(it.value) },
        onRecordingSelected = { melody, recording -> onRecordingSelected(melody.value, recording.value) },
        onBack = onBack,
        onNextItem = onNextItem
    )
}
