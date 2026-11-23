package org.syriacplatform

import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.widthIn
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.ContentScale
import org.jetbrains.compose.resources.decodeToImageBitmap
import org.syriacplatform.resources.Res
import org.syriacplatform.resources.settings_font_size
import org.syriacplatform.resources.settings_font_size_value
import org.syriacplatform.resources.settings_save
import org.syriacplatform.resources.settings_interface_language
import org.syriacplatform.resources.loading
import org.syriacplatform.resources.occasions_failed
import org.syriacplatform.resources.occasions_empty
import org.syriacplatform.resources.occasion_failed
import org.syriacplatform.resources.prayers_empty
import org.syriacplatform.resources.back
import org.syriacplatform.resources.prayer_failed
import org.syriacplatform.resources.prayer_not_found
import org.syriacplatform.resources.queue_prepare_failed
import org.syriacplatform.resources.queue_empty
import org.syriacplatform.resources.queue_start_failed
import org.syriacplatform.resources.play_all
import org.syriacplatform.resources.stop_playback
import org.syriacplatform.resources.recording_progress
import org.syriacplatform.resources.liturgical_items_empty
import org.syriacplatform.resources.prayer_not_selected
import org.syriacplatform.resources.hymn_loading
import org.syriacplatform.resources.hymn_failed
import org.syriacplatform.resources.hymn_unavailable
import org.syriacplatform.resources.translation
import org.syriacplatform.resources.hide_translation
import org.syriacplatform.resources.notation
import org.syriacplatform.resources.notation_number
import org.syriacplatform.resources.return_to_prayer
import org.syriacplatform.resources.segment
import org.syriacplatform.resources.segment_generic
import org.syriacplatform.resources.available_recordings
import org.syriacplatform.resources.text_recording
import org.syriacplatform.resources.text_recordings_failed
import org.syriacplatform.resources.melodies_available
import org.syriacplatform.resources.melody_recordings_failed
import org.syriacplatform.resources.melody_recording
import org.syriacplatform.resources.hymn_recordings_available
import org.syriacplatform.resources.hymn_recording
import org.syriacplatform.resources.hymn_recordings_failed
import org.syriacplatform.resources.hymn_text_unavailable
import org.syriacplatform.resources.listen_melody
import org.syriacplatform.resources.pause_melody
import org.syriacplatform.resources.notation_loading
import org.syriacplatform.resources.notation_failed
import org.syriacplatform.resources.view_notation
import org.syriacplatform.resources.notation_progress
import org.syriacplatform.resources.close
import org.syriacplatform.resources.notation_image_loading
import org.syriacplatform.resources.notation_image_failed
import org.syriacplatform.resources.notation_page
import org.syriacplatform.resources.previous
import org.syriacplatform.resources.reset_zoom
import org.syriacplatform.resources.next
import org.syriacplatform.resources.qolo_unresolved
import org.syriacplatform.resources.qolo_details
import org.syriacplatform.resources.qolo_failed
import org.syriacplatform.resources.poetic_meter
import org.syriacplatform.resources.occasion_not_selected
import org.syriacplatform.resources.qolo_not_selected
import org.syriacplatform.resources.hymn_not_selected
import org.syriacplatform.resources.performance_playback_failed
import org.syriacplatform.resources.generic_text
import org.syriacplatform.resources.whole_hymn
import org.jetbrains.compose.resources.stringResource
import org.syriacplatform.presentation.locale.LocalAppLocale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import org.syriacplatform.presentation.audio.CompactAudioPlayer
import androidx.compose.foundation.layout.PaddingValues
import org.syriacplatform.audio.contracts.AudioService
import org.syriacplatform.audio.models.PlaybackState
import org.syriacplatform.audio.models.PlaybackStatus
import org.syriacplatform.audio.queue.PlaybackQueueController
import org.syriacplatform.audio.queue.PlaybackQueueState
import org.syriacplatform.audio.queue.PlaybackQueueStatus
import org.syriacplatform.audio.queue.PrayerPlaybackQueueBuilder
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.PerformanceMediaId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.runtime.ResolvedLiturgicalItem
import org.syriacplatform.content.runtime.ResolvedLiturgicalItemTarget
import org.syriacplatform.content.runtime.ResolvedPerformanceMedia
import org.syriacplatform.content.runtime.RuntimeOccasion
import org.syriacplatform.context.PlatformContext
import org.syriacplatform.navigation.AppDestination
import org.syriacplatform.navigation.AppSessionState
import org.syriacplatform.common.types.PrayerSequenceId
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.presentation.theme.SyriacTextStyles
import org.syriacplatform.presentation.theme.LiturgicalAnnotatedText
import org.syriacplatform.presentation.theme.LiturgicalColors
import org.syriacplatform.presentation.theme.LiturgicalFontFamilies
import org.syriacplatform.presentation.theme.LiturgicalTextStyles
import org.syriacplatform.presentation.theme.LiturgicalTitleLevel
import org.syriacplatform.presentation.theme.LiturgicalLanguage
import org.syriacplatform.presentation.theme.LiturgicalNoteText
import kotlinx.coroutines.launch


/** Shhimo-only parchment preview; the Occasion app keeps its original styling. */
val LocalParchmentHymnBackground = compositionLocalOf { false }

/** Services required by the hymn screen, independent of app navigation and kernel. */
internal interface HymnDetailsServices {
    val content: org.syriacplatform.content.contracts.ContentService
    val preferences: org.syriacplatform.user.contracts.PreferencesService
    val performanceTextPlayback: org.syriacplatform.audio.PerformanceTextOccurrencePlaybackController?
    val loadNotationBytes: (suspend (String) -> ByteArray?)? get() = null
}

@Composable
internal fun HymnDetailsScreen(
    services: HymnDetailsServices,
    audioService: AudioService?,
    liturgicalItemId: LiturgicalItemId,
    initialMelodyId: MelodyId?,
    notationOpen: Boolean,
    notationPage: Int,
    onMelodyChanged: (MelodyId?) -> Unit,
    onNotationChanged: (Boolean, Int) -> Unit,
    preferredRecordingIds:
        Map<MelodyId, MediaAssetId>,
    onRecordingSelected:
        (MelodyId, MediaAssetId) -> Unit,
    onBack: () -> Unit,
    onNextItem: (() -> Unit)? = null
) {
    val parchmentBackground = LocalParchmentHymnBackground.current
    val itemResult by produceState<Result<ResolvedLiturgicalItem>?>(
        initialValue = null,
        key1 = services.content,
        key2 = liturgicalItemId
    ) {
        value = services.content.loadLiturgicalItem(liturgicalItemId)
    }

    val occurrenceNoteId =
        (itemResult as? Result.Success)?.data?.item?.noteId

    val occurrenceNoteResult by produceState<Result<org.syriacplatform.content.models.Note>?>(
        initialValue = null,
        key1 = services.content,
        key2 = occurrenceNoteId
    ) {
        value = occurrenceNoteId?.let { services.content.loadNote(it) }
    }

    val playbackState = rememberPlaybackState(audioService)
    // Resolve the localized fallback during composition, not inside playback callbacks.
    val performancePlaybackFailedLabel = stringResource(Res.string.performance_playback_failed)
    val activeTextInterval by (
        services.performanceTextPlayback?.activeInterval
            ?: remember { kotlinx.coroutines.flow.MutableStateFlow<org.syriacplatform.audio.ActiveTextInterval?>(null) }
    ).collectAsState()

    val coroutineScope =
        rememberCoroutineScope()

    // Remember the user's last explicitly chosen translation language.
    // A preference alone never opens translations automatically.
    val userPreferences by services.preferences.preferences.collectAsState()

    var pendingAutoPlayId by remember(liturgicalItemId) {
        mutableStateOf<MediaAssetId?>(null)
    }

    var isSeeking by remember(liturgicalItemId) {
        mutableStateOf(false)
    }

    var seekPositionMs by remember(liturgicalItemId) {
        mutableStateOf(0L)
    }

    var selectedTranslationLanguage by rememberSaveable(liturgicalItemId.value) {
        mutableStateOf<String?>(null)
    }

    var shownTranslation by rememberSaveable(liturgicalItemId.value) {
        mutableStateOf<String?>(null)
    }

    var selectedMelodyId by remember(liturgicalItemId) {
        mutableStateOf<MelodyId?>(initialMelodyId)
    }

    var selectedRecordingId by remember(liturgicalItemId) {
        mutableStateOf<MediaAssetId?>(null)
    }

    var selectedPerformanceId by remember(liturgicalItemId) {
        mutableStateOf<PerformanceMediaId?>(null)
    }

    var performancePlaybackMessage by remember(liturgicalItemId) {
        mutableStateOf<String?>(null)
    }

    val performanceMediaResult by
        produceState<Result<List<ResolvedPerformanceMedia>>?>(
            initialValue = null,
            key1 = services.content,
            key2 = liturgicalItemId
        ) {
            value =
                services.content.loadPerformanceMedia(
                    liturgicalItemId
                )
        }
    LaunchedEffect(
        playbackState.positionMs,
        playbackState.mediaAssetId,
        isSeeking
    ) {
        if (!isSeeking) {
            seekPositionMs =
                playbackState.positionMs
        }
    }
    LaunchedEffect(
        playbackState.status,
        playbackState.mediaAssetId,
        pendingAutoPlayId,
        audioService
    ) {
        val pendingId = pendingAutoPlayId

        if (
            audioService != null &&
            pendingId != null &&
            playbackState.status == PlaybackStatus.Ready &&
            playbackState.mediaAssetId == pendingId
        ) {
            audioService.play()
            pendingAutoPlayId = null
        }
    }

    var translationMenuOpen by remember { mutableStateOf(false) }
    var notationOpenRequest by remember { mutableStateOf(0) }
    var notationRequestedPage by remember { mutableStateOf(0) }
    var notationChoiceOpen by remember { mutableStateOf(false) }
    var melodyRecordingsExpanded by remember(liturgicalItemId) { mutableStateOf(false) }
    var melodyCandidatesExpanded by remember(liturgicalItemId) { mutableStateOf(false) }
    var performanceRecordingsExpanded by remember(liturgicalItemId) { mutableStateOf(false) }
    val toolbarTarget = (itemResult as? Result.Success)?.data?.target
    val toolbarVerses = when (toolbarTarget) {
        is ResolvedLiturgicalItemTarget.Qolo -> toolbarTarget.verses
        is ResolvedLiturgicalItemTarget.NonMelodicText -> toolbarTarget.verses
        is ResolvedLiturgicalItemTarget.UnresolvedQolo -> toolbarTarget.verses
        else -> emptyList()
    }
    val toolbarMelodyId = (toolbarTarget as? ResolvedLiturgicalItemTarget.Qolo)
        ?.let { target ->
            target.effectiveMelody?.id
                ?: target.melodyCandidates.firstOrNull { it.id == selectedMelodyId }?.id
                ?: target.melodyCandidates.singleOrNull()?.id
        }
    val toolbarNotations by produceState<Result<List<MediaAsset>>?>(
        initialValue = null,
        key1 = services.content,
        key2 = toolbarMelodyId
    ) {
        value = toolbarMelodyId?.let { services.content.loadMelodyNotations(it) }
    }
    val notationCount = (toolbarNotations as? Result.Success)?.data?.size ?: 0
    val toolbarLanguages = toolbarVerses.flatMap { verse ->
        verse.petgomo?.translations.orEmpty() + verse.text.translations
    }.filter { it.content.isNotBlank() }.map { it.language }.distinct().sorted()
    val preferredTranslationLanguage = userPreferences.translationLanguage
    val orderedToolbarLanguages = toolbarLanguages.sortedWith(
        compareBy<String> { it != preferredTranslationLanguage }.thenBy { it }
    )

    val hymnScrollState = rememberScrollState()
    LaunchedEffect(liturgicalItemId) { hymnScrollState.scrollTo(0) }

    androidx.compose.foundation.layout.Box(
        modifier = if (parchmentBackground) Modifier.fillMaxSize() else Modifier.fillMaxSize().safeDrawingPadding()
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(if (parchmentBackground) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.primaryContainer)
            .verticalScroll(hymnScrollState)
            .padding(
                start = 24.dp,
                end = 24.dp,
                top = 50.dp + if (parchmentBackground) WindowInsets.statusBars.asPaddingValues().calculateTopPadding() else 0.dp,
                bottom = 92.dp
            )
    ) {
        when (val result = itemResult) {
            null -> Text(stringResource(Res.string.hymn_loading))

            is Result.Failure -> {
                Text(stringResource(Res.string.hymn_failed))
            }

            is Result.Success -> {
                val target = result.data.target

                val translatedVerses = when (target) {
                    is ResolvedLiturgicalItemTarget.NonMelodicText -> target.verses
                    is ResolvedLiturgicalItemTarget.Qolo -> target.verses
                    is ResolvedLiturgicalItemTarget.UnresolvedQolo -> target.verses
                    is ResolvedLiturgicalItemTarget.Text -> emptyList()
                }
                val languages = translatedVerses.flatMap { verse ->
                    verse.petgomo?.translations.orEmpty() + verse.text.translations
                }.filter { it.content.isNotBlank() }
                    .map { it.language }.distinct().sorted()

                if (target is ResolvedLiturgicalItemTarget.NonMelodicText) {
                    val availablePerformances =
                        (performanceMediaResult as? Result.Success)?.data.orEmpty()
                    val selectedPerformance =
                        availablePerformances.firstOrNull {
                            it.performance.id == selectedPerformanceId
                        } ?: availablePerformances.firstOrNull()
                    val fullTextRecording = selectedPerformance?.mediaAsset

                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.ui.platform.LocalLayoutDirection provides
                            androidx.compose.ui.unit.LayoutDirection.Ltr
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = result.data.item.locationTitle
                                    ?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.generic_text),
                                modifier = Modifier.weight(1f),
                                style = LiturgicalTextStyles.section(),
                                textAlign = TextAlign.Center
                            )
                            androidx.compose.material3.IconButton(
                                enabled = audioService != null && fullTextRecording != null,
                                onClick = {
                                    val recording = fullTextRecording
                                    if (recording != null) {
                                        val isFullRecording =
                                            activeTextInterval == null &&
                                                playbackState.mediaAssetId == recording.id
                                        when {
                                            isFullRecording &&
                                                playbackState.status == PlaybackStatus.Playing ->
                                                audioService?.pause()
                                            isFullRecording &&
                                                (playbackState.status == PlaybackStatus.Paused ||
                                                    playbackState.status == PlaybackStatus.Ready) ->
                                                audioService?.play()
                                            else -> {
                                                services.performanceTextPlayback?.clearActiveInterval()
                                                pendingAutoPlayId = recording.id
                                                if (audioService?.load(recording) is Result.Failure) {
                                                    pendingAutoPlayId = null
                                                }
                                            }
                                        }
                                    }
                                }
                            ) {
                                Text(
                                    if (fullTextRecording != null &&
                                        activeTextInterval == null &&
                                        playbackState.mediaAssetId == fullTextRecording.id &&
                                        playbackState.status == PlaybackStatus.Playing
                                    ) "Ⅱ" else "▶",
                                    style = MaterialTheme.typography.titleLarge
                                )
                            }
                        }
                    }
                    if (availablePerformances.size > 1) {
                        androidx.compose.material3.TextButton(
                            onClick = {
                                performanceRecordingsExpanded = !performanceRecordingsExpanded
                            }
                        ) { Text(
                            text = stringResource(Res.string.available_recordings),
                            style = LiturgicalTextStyles.secondary(
                                MaterialTheme.typography.bodyMedium,
                                LiturgicalLanguage.Arabic
                            )
                        ) }
                        if (performanceRecordingsExpanded) {
                            availablePerformances.forEach { resolved ->
                                Button(
                                    enabled = audioService != null,
                                    onClick = {
                                        if (selectedPerformanceId != resolved.performance.id) {
                                            pendingAutoPlayId = null
                                            audioService?.stop()
                                            services.performanceTextPlayback?.clearActiveInterval()
                                            selectedPerformanceId = resolved.performance.id
                                            performancePlaybackMessage = null
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                ) {
                                    val performer = resolved.mediaAsset.performer
                                        ?.takeIf { it.isNotBlank() } ?: stringResource(Res.string.text_recording)
                                    Text(
                                        if (selectedPerformance?.performance?.id ==
                                            resolved.performance.id
                                        ) "✓ $performer" else performer
                                    )
                                }
                            }
                        }
                    }
                    if (performanceMediaResult is Result.Failure) {
                        Text(stringResource(Res.string.text_recordings_failed))
                    }
                    performancePlaybackMessage?.let { message ->
                        Text(text = message, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        target.verses.forEachIndexed { index, verse ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = if (parchmentBackground) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth().clickable(
                                    enabled = selectedPerformance != null &&
                                        services.performanceTextPlayback != null,
                                    onClick = {
                                        val performance = selectedPerformance
                                        val playback = services.performanceTextPlayback
                                        if (performance != null && playback != null) {
                                            performancePlaybackMessage = null
                                            coroutineScope.launch {
                                                when (val playResult = playback.play(
                                                    performanceId = performance.performance.id,
                                                    textOccurrenceId = verse.id
                                                )) {
                                                    is Result.Success -> performancePlaybackMessage = null
                                                    is Result.Failure -> performancePlaybackMessage =
                                                        playResult.error.message
                                                            ?: performancePlaybackFailedLabel
                                                }
                                            }
                                        }
                                    }
                                )
                            ) {
                                VerseTextWithTranslation(
                                    verseNumber = index + 1,
                                    petgomo = verse.petgomo?.syriac,
                                    syriacText = verse.text.syriac,
                                    petgomoTranslations = verse.petgomo?.translations.orEmpty(),
                                    textTranslations = verse.text.translations,
                                    language = selectedTranslationLanguage,
                                    onShowTranslation = { shownTranslation = it }
                                )
                            }
                        }
                    }
                } else if (target is ResolvedLiturgicalItemTarget.Qolo) {
                    val effectiveMelody = target.effectiveMelody

                    val selectedMelody =
                        effectiveMelody
                            ?: if (target.melodyCandidates.size == 1) {
                                target.melodyCandidates.first()
                            } else {
                                target.melodyCandidates
                                    .firstOrNull { melody ->
                                        melody.id == selectedMelodyId
                                    }
                            }

                    LaunchedEffect(selectedMelody?.id) {
                        if (selectedMelody != null && selectedMelodyId != selectedMelody.id) {
                            selectedMelodyId = selectedMelody.id
                            onMelodyChanged(selectedMelody.id)
                        }
                    }

                    val fullHymnRecording =
                        (performanceMediaResult as? Result.Success)
                            ?.data
                            ?.let { available ->
                                available.firstOrNull {
                                    it.performance.id == selectedPerformanceId
                                } ?: available.firstOrNull()
                            }
                            ?.mediaAsset

                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.ui.platform.LocalLayoutDirection provides
                            androidx.compose.ui.unit.LayoutDirection.Ltr
                    ) {
                    androidx.compose.foundation.layout.Box(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = target.qolo.name,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .fillMaxWidth()
                                .padding(horizontal = 52.dp),
                            style = LiturgicalTextStyles.title(LiturgicalTitleLevel.Hymn),
                            textAlign = TextAlign.Center
                        )
                        androidx.compose.material3.IconButton(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            enabled = audioService != null && fullHymnRecording != null,
                            onClick = {
                                val recording = fullHymnRecording
                                if (recording != null) {
                                    val isFullHymn =
                                        activeTextInterval == null &&
                                            playbackState.mediaAssetId == recording.id
                                    when {
                                        isFullHymn && playbackState.status == PlaybackStatus.Playing ->
                                            audioService?.pause()
                                        isFullHymn &&
                                            (playbackState.status == PlaybackStatus.Paused ||
                                                playbackState.status == PlaybackStatus.Ready) ->
                                            audioService?.play()
                                        else -> {
                                            services.performanceTextPlayback?.clearActiveInterval()
                                            pendingAutoPlayId = recording.id
                                            if (audioService?.load(recording) is Result.Failure) {
                                                pendingAutoPlayId = null
                                            }
                                        }
                                    }
                                }
                            }
                        ) {
                            val playingFullHymn =
                                fullHymnRecording != null &&
                                    activeTextInterval == null &&
                                    playbackState.mediaAssetId == fullHymnRecording.id &&
                                    playbackState.status == PlaybackStatus.Playing
                            Text(
                                if (playingFullHymn) "Ⅱ" else "▶",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    }
                    }

                    if (
                        effectiveMelody == null &&
                        target.melodyCandidates.size > 1
                    ) {
                        androidx.compose.material3.TextButton(
                            onClick = { melodyCandidatesExpanded = !melodyCandidatesExpanded }
                        ) {
                            Text(
                            text = stringResource(Res.string.melodies_available),
                            modifier =
                                Modifier.padding(
                                    top = 8.dp,
                                    bottom = 8.dp
                                ),
                            style = LiturgicalTextStyles.section(
                                MaterialTheme.typography.titleSmall,
                                LiturgicalLanguage.Arabic
                            )
                        )

                        }

                        if (melodyCandidatesExpanded) target.melodyCandidates.forEach { melody ->
                            Button(
                                onClick = {
                                    if (selectedMelodyId != melody.id) {
                                        pendingAutoPlayId = null
                                        audioService?.stop()
                                        selectedRecordingId = null
                                        selectedMelodyId = melody.id
                                        onMelodyChanged(melody.id)
                                        onNotationChanged(false, 0)
                                        seekPositionMs = 0L
                                        isSeeking = false
                                    }
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                            ) {
                                Text(
                                    text =
                                        if (selectedMelodyId == melody.id) {
                                            "✓ ${melody.name}"
                                        } else {
                                            melody.name
                                        },
                                    style = SyriacTextStyles.body()
                                )
                            }
                        }
                    }

                    selectedMelody?.name?.takeIf { it.isNotBlank() }?.let { melodyName ->
                        Text(
                            text = (itemResult as? Result.Success)?.data?.item?.qintoTitle
                                ?.takeIf { it.isNotBlank() }
                                ?.let { "ܩܺܝܢܬܳܐ $it : $melodyName" }
                                ?: melodyName,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp, bottom = 12.dp),
                            style = LiturgicalTextStyles.secondary(
                                MaterialTheme.typography.titleMedium,
                                LiturgicalLanguage.Syriac
                            ),
                            textAlign = TextAlign.Center
                        )
                    }

                    MelodyNotationSection(
                        content = services.content,
                        loadNotationBytes = services.loadNotationBytes,
                        melodyId = selectedMelody?.id,
                        initialOpen = notationOpen,
                        initialPage = notationPage,
                        onNotationChanged = onNotationChanged,
                        openRequest = notationOpenRequest,
                        requestedPage = notationRequestedPage,
                        showInlineButton = false
                    )

                    val recordingsResult by produceState<Result<List<MediaAsset>>?>(
                        initialValue = null,
                        key1 = services.content,
                        key2 = selectedMelody?.id
                    ) {
                        value =
                            if (selectedMelody != null) {
                                services.content.loadMelodyRecordings(
                                    selectedMelody.id
                                )
                            } else {
                                null
                            }
                    }

                    when (val recordings = recordingsResult) {
                        null -> Unit

                        is Result.Failure -> {
                            Text(stringResource(Res.string.melody_recordings_failed))
                        }

                        is Result.Success -> {
                            val availableRecordings =
                                recordings.data

                            val preferredRecordingId =
                                selectedMelody
                                    ?.id
                                    ?.let {
                                        melodyId ->
                                        preferredRecordingIds[
                                            melodyId
                                        ]
                                    }

                            val selectedRecording =
                                availableRecordings
                                    .firstOrNull {
                                            recording ->
                                        recording.id ==
                                            selectedRecordingId
                                    }
                                    ?: availableRecordings
                                        .firstOrNull {
                                                recording ->
                                            recording.id ==
                                                preferredRecordingId
                                        }
                                    ?: availableRecordings
                                        .firstOrNull()

                            LaunchedEffect(
                                availableRecordings,
                                selectedRecordingId
                            ) {
                                if (
                                    selectedRecordingId == null &&
                                    selectedRecording != null
                                ) {
                                    selectedRecordingId =
                                        selectedRecording.id

                                    selectedMelody?.let {
                                            melody ->
                                        onRecordingSelected(
                                            melody.id,
                                            selectedRecording.id
                                        )
                                    }
                                }
                            }

                            if (availableRecordings.isNotEmpty()) {
                                androidx.compose.runtime.CompositionLocalProvider(
                                    androidx.compose.ui.platform.LocalLayoutDirection provides
                                        androidx.compose.ui.unit.LayoutDirection.Ltr
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                                    if (selectedRecording != null && availableRecordings.isNotEmpty()) {
                                                        androidx.compose.material3.TextButton(
                                                            enabled = audioService != null,
                                                            onClick = {
                                                                if (activeTextInterval?.mediaAssetId == playbackState.mediaAssetId) {
                                                                    services.performanceTextPlayback?.clearActiveInterval()
                                                                    pendingAutoPlayId = selectedRecording.id
                                                                    if (audioService?.load(selectedRecording) is Result.Failure) {
                                                                        pendingAutoPlayId = null
                                                                    }
                                                                } else if (playbackState.mediaAssetId == selectedRecording.id &&
                                                                    playbackState.status == PlaybackStatus.Playing
                                                                ) {
                                                                    audioService?.pause()
                                                                } else if (playbackState.mediaAssetId == selectedRecording.id &&
                                                                    playbackState.status != PlaybackStatus.Idle &&
                                                                    playbackState.status != PlaybackStatus.Error
                                                                ) {
                                                                    if (playbackState.status == PlaybackStatus.Ended) {
                                                                        audioService?.seekTo(0L)
                                                                    }
                                                                    audioService?.play()
                                                                } else {
                                                                    services.performanceTextPlayback?.clearActiveInterval()
                                                                    pendingAutoPlayId = selectedRecording.id
                                                                    if (audioService?.load(selectedRecording) is Result.Failure) {
                                                                        pendingAutoPlayId = null
                                                                    }
                                                                }
                                                            },
                                                            modifier = Modifier.padding(bottom = 8.dp)
                                                        ) {
                                                            Text(
                                                                if (playbackState.mediaAssetId == selectedRecording.id &&
                                                                    playbackState.status == PlaybackStatus.Playing
                                                                ) stringResource(Res.string.pause_melody) else stringResource(Res.string.listen_melody)
                                                            )
                                                        }
                                                    }
                                        androidx.compose.material3.TextButton(
                                            onClick = { melodyRecordingsExpanded = !melodyRecordingsExpanded }
                                        ) { Text(
                            text = stringResource(Res.string.available_recordings),
                            style = LiturgicalTextStyles.secondary(
                                MaterialTheme.typography.bodyMedium,
                                LiturgicalLanguage.Arabic
                            )
                        ) }
                                    }
                                }
                                if (melodyRecordingsExpanded) availableRecordings.forEach {
                                        recording ->

                                    val performerName =
                                        recording.performer
                                            ?.takeIf { it.isNotBlank() }
                                            ?: stringResource(Res.string.melody_recording)

                                    Button(
                                        enabled =
                                            audioService != null,
                                        onClick = {
                                            if (
                                                selectedRecordingId !=
                                                recording.id
                                            ) {
                                                pendingAutoPlayId =
                                                    null

                                                audioService?.stop()
                                                services.performanceTextPlayback?.clearActiveInterval()

                                                selectedRecordingId =
                                                    recording.id

                                                selectedMelody?.let {
                                                    melody ->
                                                    onRecordingSelected(
                                                        melody.id,
                                                        recording.id
                                                    )
                                                }

                                                seekPositionMs = 0L
                                                isSeeking = false
                                            }
                                        },
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    bottom = 6.dp
                                                ),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    ) {
                                        Text(
                                            text =
                                                if (
                                                    selectedRecording?.id ==
                                                    recording.id
                                                ) {
                                                    "✓ $performerName"
                                                } else {
                                                    performerName
                                                }
                                        )
                                    }
                                }
                            }


                        }
                    }

                    val selectedPerformance =
                        when (
                            val performances =
                                performanceMediaResult
                        ) {
                            is Result.Success -> {
                                val available =
                                    performances.data

                                val selected =
                                    available.firstOrNull {
                                        it.performance.id ==
                                            selectedPerformanceId
                                    } ?: available.firstOrNull()

                                LaunchedEffect(
                                    available,
                                    selectedPerformanceId
                                ) {
                                    if (
                                        selectedPerformanceId == null &&
                                        selected != null
                                    ) {
                                        selectedPerformanceId =
                                            selected.performance.id
                                    }
                                }

                                if (available.size > 1) {
                                    androidx.compose.material3.TextButton(
                                        onClick = {
                                            performanceRecordingsExpanded = !performanceRecordingsExpanded
                                        }
                                    ) { Text(
                            text = stringResource(Res.string.hymn_recordings_available),
                            style = LiturgicalTextStyles.secondary(
                                MaterialTheme.typography.bodyMedium,
                                LiturgicalLanguage.Arabic
                            )
                        ) }
                                    if (performanceRecordingsExpanded) available.forEach { resolved ->
                                        val performerName =
                                            resolved.mediaAsset.performer
                                                ?.takeIf {
                                                    it.isNotBlank()
                                                }
                                                ?: stringResource(Res.string.hymn_recording)

                                        Button(
                                            enabled =
                                                services.performanceTextPlayback !=
                                                    null,
                                            onClick = {
                                                if (
                                                    selectedPerformanceId !=
                                                    resolved.performance.id
                                                ) {
                                                    audioService?.stop()
                                                    selectedPerformanceId =
                                                        resolved.performance.id
                                                    performancePlaybackMessage =
                                                        null
                                                }
                                            },
                                            modifier =
                                                Modifier
                                                    .fillMaxWidth()
                                                    .padding(
                                                        bottom = 6.dp
                                                    ),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.40f),
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        ) {
                                            Text(
                                                if (
                                                    selected?.performance?.id ==
                                                    resolved.performance.id
                                                ) {
                                                    "✓ $performerName"
                                                } else {
                                                    performerName
                                                }
                                            )
                                        }
                                    }
                                }

                                selected
                            }

                            is Result.Failure -> {
                                Text(
                                    stringResource(Res.string.hymn_recordings_failed)
                                )
                                null
                            }

                            null -> null
                        }

                    performancePlaybackMessage?.let { message ->
                        Text(
                            text = message,
                            modifier =
                                Modifier.padding(
                                    bottom = 8.dp
                                )
                        )
                    }

                    if (target.verses.isEmpty()) {
                        Text(stringResource(Res.string.hymn_text_unavailable))
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            run {
                                val note = (occurrenceNoteResult as? Result.Success)?.data?.text
                                if (!note.isNullOrBlank()) {
                                    Text(
                                        text = LiturgicalNoteText.annotated(note),
                                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                                        style = LiturgicalTextStyles.note(
                                            MaterialTheme.typography.bodyMedium,
                                            LiturgicalLanguage.Arabic
                                        ),
                                        textAlign = TextAlign.Right
                                    )
                                }
                            }
                            target.verses.forEachIndexed { verseIndex, verse ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (parchmentBackground) androidx.compose.ui.graphics.Color.Transparent else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth().clickable(
                                        enabled = selectedPerformance != null &&
                                            services.performanceTextPlayback != null,
                                        onClick = {
                                        val performance =
                                            selectedPerformance
                                        val playback =
                                            services.performanceTextPlayback

                                        if (
                                            performance != null &&
                                            playback != null
                                        ) {
                                            performancePlaybackMessage =
                                                null

                                            coroutineScope.launch {
                                                when (
                                                    val playResult =
                                                        playback.play(
                                                            performanceId =
                                                                performance
                                                                    .performance
                                                                    .id,
                                                            textOccurrenceId =
                                                                verse.id
                                                        )
                                                ) {
                                                    is Result.Success -> {
                                                        performancePlaybackMessage =
                                                            null
                                                    }

                                                    is Result.Failure -> {
                                                        performancePlaybackMessage =
                                                            playResult
                                                                .error
                                                                .message
                                                                ?: performancePlaybackFailedLabel
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    )
                                ) {
                                    VerseTextWithTranslation(
                                        verseNumber = verseIndex + 1,
                                        petgomo = verse.petgomo?.syriac,
                                        syriacText = verse.text.syriac,
                                        petgomoTranslations = verse.petgomo?.translations.orEmpty(),
                                        textTranslations = verse.text.translations,
                                        language = selectedTranslationLanguage,
                                        onShowTranslation = { shownTranslation = it }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(stringResource(Res.string.hymn_unavailable))
                }
                if (onNextItem != null) {
                    androidx.compose.material3.TextButton(
                        onClick = onNextItem,
                        modifier = Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 18.dp),
                        colors = androidx.compose.material3.ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    ) {
                        Text("↓", style = MaterialTheme.typography.headlineMedium)
                    }
                }
            }
        }

    }
    androidx.compose.material3.Surface(
        modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth()
            .then(if (parchmentBackground) Modifier.statusBarsPadding() else Modifier),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.60f),
        border = null,
        tonalElevation = 0.dp,
        shadowElevation = 1.dp
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
        ) {
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
            androidx.compose.material3.TextButton(
                onClick = { translationMenuOpen = true },
                enabled = toolbarLanguages.isNotEmpty(),
            ) { Text(stringResource(Res.string.translation)) }
            androidx.compose.material3.DropdownMenu(
                expanded = translationMenuOpen,
                onDismissRequest = { translationMenuOpen = false }
            ) {
                orderedToolbarLanguages.forEach { language ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(if (language == "ar") "ع" else language) },
                        onClick = {
                            val newSelection =
                                if (selectedTranslationLanguage == language) null else language
                            selectedTranslationLanguage = newSelection
                            shownTranslation = null
                            translationMenuOpen = false
                            if (newSelection != null &&
                                userPreferences.translationLanguage != newSelection
                            ) {
                                coroutineScope.launch {
                                    services.preferences.update(
                                        userPreferences.copy(translationLanguage = newSelection)
                                    )
                                }
                            }
                        }
                    )
                }
                if (selectedTranslationLanguage != null) {
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(stringResource(Res.string.hide_translation)) },
                        onClick = {
                            selectedTranslationLanguage = null
                            shownTranslation = null
                            translationMenuOpen = false
                        }
                    )
                }
            }
            }
            Box(modifier = Modifier.align(Alignment.Center)) {
            androidx.compose.material3.TextButton(
                enabled = notationCount > 0,
                onClick = {
                    if (notationCount == 1) {
                        notationRequestedPage = 0
                        notationOpenRequest++
                    }
                    else notationChoiceOpen = true
                },
            ) { Text(stringResource(Res.string.notation)) }
            androidx.compose.material3.DropdownMenu(
                expanded = notationChoiceOpen,
                onDismissRequest = { notationChoiceOpen = false }
            ) {
                repeat(notationCount) { index ->
                    androidx.compose.material3.DropdownMenuItem(
                        text = { Text(stringResource(Res.string.notation_number, index + 1)) },
                        onClick = {
                            notationChoiceOpen = false
                            notationRequestedPage = index
                            notationOpenRequest++
                        }
                    )
                }
            }
            }
            androidx.compose.material3.TextButton(
                onClick = { audioService?.stop(); onBack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) { Text(stringResource(Res.string.return_to_prayer)) }
        }
    }
    val audioPlayerVisible = audioService != null &&
        playbackState.mediaAssetId != null &&
        playbackState.status != PlaybackStatus.Idle &&
        playbackState.status != PlaybackStatus.Error
    if (audioPlayerVisible && audioService != null) {
        val segmentVerses = when (
            val target = (itemResult as? Result.Success)?.data?.target
        ) {
            is ResolvedLiturgicalItemTarget.Qolo -> target.verses
            is ResolvedLiturgicalItemTarget.NonMelodicText -> target.verses
            else -> emptyList()
        }
        val segmentNumber = segmentVerses.indexOfFirst { verse ->
            verse.id == activeTextInterval?.textOccurrenceId
        }.takeIf { it >= 0 }?.plus(1)
        CompactAudioPlayer(
            title = if (activeTextInterval?.mediaAssetId == playbackState.mediaAssetId) {
                segmentNumber?.let { stringResource(Res.string.segment, it) } ?: stringResource(Res.string.segment_generic)
            } else {
                stringResource(Res.string.whole_hymn)
            },
            status = playbackState.status,
            positionMs = playbackState.positionMs,
            durationMs = playbackState.durationMs,
            intervalStartMs = activeTextInterval?.takeIf {
                it.mediaAssetId == playbackState.mediaAssetId
            }?.startMs,
            intervalEndMs = activeTextInterval?.takeIf {
                it.mediaAssetId == playbackState.mediaAssetId
            }?.endMs,
            onTogglePlayback = {
                when (playbackState.status) {
                    PlaybackStatus.Playing -> audioService.pause()
                    PlaybackStatus.Ready, PlaybackStatus.Paused -> audioService.play()
                    PlaybackStatus.Ended -> {
                        audioService.seekTo(activeTextInterval?.takeIf {
                            it.mediaAssetId == playbackState.mediaAssetId
                        }?.startMs ?: 0L)
                        audioService.play()
                    }
                    else -> Unit
                }
            },
            onSeek = { position ->
                val interval = activeTextInterval?.takeIf {
                    it.mediaAssetId == playbackState.mediaAssetId
                }
                audioService.seekTo(
                    if (interval != null) position.coerceIn(interval.startMs, interval.endMs) else position
                )
            },
            onClose = {
                pendingAutoPlayId = null
                audioService.stop()
                services.performanceTextPlayback?.clearActiveInterval()
            },
            modifier = Modifier.align(Alignment.BottomCenter)
                .then(if (parchmentBackground) Modifier.navigationBarsPadding() else Modifier)
        )
    }
    shownTranslation?.let { translation ->
        androidx.compose.material3.Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .then(if (parchmentBackground) Modifier.navigationBarsPadding() else Modifier)
                .padding(horizontal = 12.dp)
                .padding(bottom = if (audioPlayerVisible) 64.dp else 0.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.93f),
            shadowElevation = 8.dp
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = translation,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontFamily = when (selectedTranslationLanguage) {
                            "ar" -> LiturgicalFontFamilies.arabic()
                            "en", "fr" -> LiturgicalFontFamilies.latin()
                            else -> MaterialTheme.typography.bodyLarge.fontFamily
                        },
                        textDirection = if (selectedTranslationLanguage == "ar") {
                            TextDirection.Rtl
                        } else {
                            TextDirection.Ltr
                        }
                    ),
                    textAlign = if (selectedTranslationLanguage == "ar") TextAlign.Right else TextAlign.Start
                )
                androidx.compose.material3.TextButton(onClick = { shownTranslation = null }) {
                    Text("×")
                }
            }
        }
    }
}
}

