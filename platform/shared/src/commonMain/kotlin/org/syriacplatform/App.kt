package org.syriacplatform

import androidx.compose.foundation.background
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
import org.syriacplatform.resources.whole_hymn
import org.jetbrains.compose.resources.stringResource
import org.syriacplatform.presentation.locale.LocalAppLocale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.safeDrawingPadding
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


@Composable
fun App(
    platform: PlatformContext,
    initialSession: AppSessionState = AppSessionState(),
    onSessionChanged: (AppSessionState) -> Unit = {}
) {
    val navigationState by
    platform.navigation.state.collectAsState()
    val userPreferences by platform.preferences.preferences.collectAsState()
    val systemDensity = LocalDensity.current
    val scaledDensity = remember(systemDensity, userPreferences.fontScale) {
        Density(systemDensity.density, systemDensity.fontScale * userPreferences.fontScale)
    }
    val preferencesScope = rememberCoroutineScope()
    var fontSizeDialogOpen by remember { mutableStateOf(false) }
    var languageDialogOpen by remember { mutableStateOf(false) }
    val uiLanguage = userPreferences.interfaceLanguage

    var selectedOccasionId by remember {
        mutableStateOf<OccasionId?>(initialSession.occasionId)
    }

    var selectedPrayerSequenceId by remember {
        mutableStateOf<PrayerSequenceId?>(initialSession.prayerSequenceId)
    }

    var selectedLiturgicalItemId by remember {
        mutableStateOf<LiturgicalItemId?>(initialSession.liturgicalItemId)
    }

    var selectedQoloId by remember {
        mutableStateOf<QoloId?>(initialSession.qoloId)
    }

    var melodySession by remember { mutableStateOf(initialSession.melodyId) }
    var notationOpenSession by remember { mutableStateOf(initialSession.notationViewerOpen) }
    var notationPageSession by remember { mutableStateOf(initialSession.notationPage) }

    val preferredRecordingIds =
        remember {
            mutableStateMapOf<
                MelodyId,
                MediaAssetId
            >()
        }

    fun publishSession(destination: AppDestination) {
        onSessionChanged(
            AppSessionState(
                destination = destination,
                occasionId = selectedOccasionId,
                prayerSequenceId = selectedPrayerSequenceId,
                liturgicalItemId = selectedLiturgicalItemId,
                qoloId = selectedQoloId,
                melodyId = melodySession,
                notationViewerOpen = notationOpenSession,
                notationPage = notationPageSession
            ).normalized()
        )
    }

    SideEffect {
        publishSession(navigationState.currentDestination)
    }

    CompositionLocalProvider(
        LocalDensity provides scaledDensity,
        LocalAppLocale provides uiLanguage
    ) {
    MaterialTheme {
        when (navigationState.currentDestination) {

            AppDestination.HOME -> {
                HomeScreen(
                    platform = platform,
                    onOpenFontSize = { fontSizeDialogOpen = true },
                    onOpenLanguage = { languageDialogOpen = true },

                    onOpenOccasion = { occasionId ->
                        selectedOccasionId =
                            occasionId

                        publishSession(AppDestination.OCCASION_DETAILS)
                        platform.navigation.navigateTo(
                            AppDestination.OCCASION_DETAILS
                        )
                    }
                )
            }

            AppDestination.HYMN_DETAILS -> {
                val liturgicalItemId =
                    selectedLiturgicalItemId

                if (liturgicalItemId != null) {
                    HymnDetailsScreen(
                        platform = platform,
                        audioService = platform.audio,
                        liturgicalItemId =
                            liturgicalItemId,
                        preferredRecordingIds =
                            preferredRecordingIds,
                        initialMelodyId = melodySession,
                        notationOpen = notationOpenSession,
                        notationPage = notationPageSession,
                        onMelodyChanged = { melodyId ->
                            if (melodySession != melodyId) {
                                melodySession = melodyId
                                notationOpenSession = false
                                notationPageSession = 0
                            }
                            publishSession(AppDestination.HYMN_DETAILS)
                        },
                        onNotationChanged = { open, page ->
                            notationOpenSession = open
                            notationPageSession = page
                            onSessionChanged(
                                AppSessionState(
                                    destination = AppDestination.HYMN_DETAILS,
                                    occasionId = selectedOccasionId,
                                    prayerSequenceId = selectedPrayerSequenceId,
                                    liturgicalItemId = selectedLiturgicalItemId,
                                    qoloId = selectedQoloId,
                                    melodyId = melodySession,
                                    notationViewerOpen = open,
                                    notationPage = page
                                ).normalized()
                            )
                        },
                        onRecordingSelected = {
                                melodyId,
                                recordingId ->
                            preferredRecordingIds[
                                melodyId
                            ] =
                                recordingId
                        },
                        onBack = {
                            selectedLiturgicalItemId = null
                            melodySession = null
                            notationOpenSession = false
                            notationPageSession = 0
                            publishSession(AppDestination.PRAYER_DETAILS)

                            platform.navigation.navigateTo(
                                AppDestination.PRAYER_DETAILS
                            )
                        }
                    )
                } else {
                    MissingHymnSelectionScreen(
                        onBack = {
                            publishSession(AppDestination.PRAYER_DETAILS)
                            platform.navigation.navigateTo(
                                AppDestination.PRAYER_DETAILS
                            )
                        }
                    )
                }
            }

            AppDestination.OCCASION_DETAILS -> {
                val occasionId =
                    selectedOccasionId

                if (occasionId != null) {
                    OccasionDetailsScreen(
                        platform = platform,
                        occasionId = occasionId,
                        onOpenPrayer = { prayerSequenceId ->
                            selectedPrayerSequenceId =
                                prayerSequenceId

                            publishSession(AppDestination.PRAYER_DETAILS)
                            platform.navigation.navigateTo(
                                AppDestination.PRAYER_DETAILS
                            )
                        },
                        onBack = {
                            selectedOccasionId = null
                            selectedPrayerSequenceId = null
                            selectedLiturgicalItemId = null
                            melodySession = null
                            notationOpenSession = false
                            notationPageSession = 0
                            publishSession(AppDestination.HOME)

                            platform.navigation.navigateTo(
                                AppDestination.HOME
                            )
                        }
                    )
                } else {
                    MissingOccasionSelectionScreen(
                        onBack = {
                            publishSession(AppDestination.HOME)
                            platform.navigation.navigateTo(
                                AppDestination.HOME
                            )
                        }
                    )
                }
            }

            AppDestination.PRAYER_DETAILS -> {
                val prayerSequenceId =
                    selectedPrayerSequenceId

                if (prayerSequenceId != null) {
                    PrayerDetailsScreen(
                        platform = platform,
                        occasionId = selectedOccasionId,
                        prayerSequenceId = prayerSequenceId,
                        preferredRecordingIds =
                            preferredRecordingIds,
                        onOpenHymn = { liturgicalItemId ->
                            selectedLiturgicalItemId =
                                liturgicalItemId

                            publishSession(AppDestination.HYMN_DETAILS)
                            platform.navigation.navigateTo(
                                AppDestination.HYMN_DETAILS
                            )
                        },
                        onBack = {
                            selectedPrayerSequenceId = null
                            selectedLiturgicalItemId = null
                            melodySession = null
                            notationOpenSession = false
                            notationPageSession = 0
                            publishSession(AppDestination.OCCASION_DETAILS)

                            platform.navigation.navigateTo(
                                AppDestination.OCCASION_DETAILS
                            )
                        }
                    )
                } else {
                    MissingPrayerSelectionScreen(
                        onBack = {
                            publishSession(AppDestination.OCCASION_DETAILS)
                            platform.navigation.navigateTo(
                                AppDestination.OCCASION_DETAILS
                            )
                        }
                    )
                }
            }

            AppDestination.QOLO_DETAILS -> {
                val qoloId =
                    selectedQoloId

                if (qoloId != null) {
                    QoloDetailsScreen(
                        platform = platform,
                        qoloId = qoloId,
                        onBack = {
                            selectedQoloId = null

                            if (selectedOccasionId != null) {
                                publishSession(AppDestination.OCCASION_DETAILS)
                                platform.navigation.navigateTo(
                                    AppDestination.OCCASION_DETAILS
                                )
                            } else {
                                publishSession(AppDestination.HOME)
                                platform.navigation.navigateTo(
                                    AppDestination.HOME
                                )
                            }
                        }
                    )
                } else {
                    MissingQoloSelectionScreen(
                        onBack = {
                            if (selectedOccasionId != null) {
                                publishSession(AppDestination.OCCASION_DETAILS)
                                platform.navigation.navigateTo(
                                    AppDestination.OCCASION_DETAILS
                                )
                            } else {
                                platform.navigation.navigateTo(
                                    AppDestination.HOME
                                )
                            }
                        }
                    )
                }
            }
        }
    }
    if (languageDialogOpen) {
        Dialog(onDismissRequest = { languageDialogOpen = false }) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(stringResource(Res.string.settings_interface_language))
                    listOf("ar" to "العربية", "en" to "English", "fr" to "Français", "de" to "Deutsch").forEach { (code, label) ->
                        TextButton(onClick = {
                            preferencesScope.launch {
                                platform.preferences.update(userPreferences.copy(interfaceLanguage = code))
                            }
                            languageDialogOpen = false
                        }) {
                            Text((if (uiLanguage == code) "✓ " else "") + label)
                        }
                    }
                }
            }
        }
    }
    if (fontSizeDialogOpen) {
        var draftScale by remember(userPreferences.fontScale) {
            mutableStateOf(userPreferences.fontScale)
        }
        Dialog(onDismissRequest = { fontSizeDialogOpen = false }) {
            Surface(shape = RoundedCornerShape(16.dp)) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(stringResource(Res.string.settings_font_size_value, (draftScale * 100).toInt()))
                    Slider(
                        value = draftScale,
                        onValueChange = { draftScale = it },
                        valueRange = 0.8f..1.6f,
                        steps = 7
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { draftScale = 1f }) {
                            Text("100%")
                        }
                        TextButton(onClick = {
                            preferencesScope.launch {
                                platform.preferences.update(
                                    userPreferences.copy(fontScale = draftScale)
                                )
                            }
                            fontSizeDialogOpen = false
                        }) {
                            Text(stringResource(Res.string.settings_save))
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun HomeScreen(
    platform: PlatformContext,
    onOpenFontSize: () -> Unit,
    onOpenLanguage: () -> Unit,
    onOpenOccasion: (OccasionId) -> Unit
) {
    val occasionsResult by
    produceState<Result<List<Occasion>>?>(
        initialValue = null,
        key1 = platform
    ) {
        value =
            platform.content.loadOccasions()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onOpenFontSize) {
                Text(stringResource(Res.string.settings_font_size))
            }
            TextButton(onClick = onOpenLanguage) {
                Text(stringResource(Res.string.settings_interface_language))
            }
        }
        when (
            val result =
                occasionsResult
        ) {
            null -> {
                Text(
                    text = stringResource(Res.string.loading)
                )
            }

            is Result.Failure -> {
                Text(
                    text =
                        result.error.message
                            ?: stringResource(Res.string.occasions_failed),
                    textAlign =
                        TextAlign.Center
                )
            }

            is Result.Success -> {
                val occasions =
                    result.data

                if (occasions.isEmpty()) {
                    Text(
                        text =
                            stringResource(Res.string.occasions_empty)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement =
                            Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                    ) {
                        items(occasions) {
                                occasion ->

                            OccasionListItem(
                                occasion = occasion,
                                onClick = {
                                    onOpenOccasion(
                                        occasion.id
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OccasionListItem(
    occasion: Occasion,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier =
            Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    vertical = 8.dp
                ),
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text = occasion.name,
                style = LiturgicalTextStyles.title(LiturgicalTitleLevel.Occasion),
                textAlign = TextAlign.Center
            )

            occasion.description?.let {
                    description ->

                Text(
                    text = description,
                    modifier =
                        Modifier.padding(
                            top = 4.dp
                        ),
                    style =
                        LiturgicalTextStyles.secondary(MaterialTheme.typography.bodyMedium),
                    textAlign =
                        TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun LiturgicalBackNavigation(onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        TextButton(onClick = onClick) {
            Text(
                text = stringResource(Res.string.back),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OccasionDetailsScreen(
    platform: PlatformContext,
    occasionId: OccasionId,
    onOpenPrayer: (PrayerSequenceId) -> Unit,
    onBack: () -> Unit
) {
    val occasionResult by
    produceState<Result<RuntimeOccasion>?>(
        initialValue = null,
        key1 = platform,
        key2 = occasionId
    ) {
        value =
            platform.content.loadOccasion(
                occasionId
            )
    }

    val noteId = (occasionResult as? Result.Success)
        ?.data?.occasion?.noteId
    val noteResult by produceState<Result<org.syriacplatform.content.models.Note>?>(
        initialValue = null,
        key1 = platform,
        key2 = noteId
    ) {
        value = noteId?.let { platform.content.loadNote(it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp)
    ) {
        LiturgicalBackNavigation(onClick = onBack)

        when (
            val result =
                occasionResult
        ) {
            null -> {
                Text(
                    text = "Loading..."
                )
            }

            is Result.Failure -> {
                Text(
                    text =
                        result.error.message
                            ?: stringResource(Res.string.occasion_failed),
                    textAlign =
                        TextAlign.Center
                )
            }

            is Result.Success -> {
                val runtimeOccasion =
                    result.data

                Text(
                    text = runtimeOccasion.occasion.name,
                    modifier = Modifier.fillMaxWidth(),
                    style = LiturgicalTextStyles.title(LiturgicalTitleLevel.Occasion),
                    textAlign = TextAlign.Center
                )

                runtimeOccasion
                    .occasion
                    .description
                    ?.let { description ->
                        Text(
                            text = description,
                            modifier =
                                Modifier.padding(
                                    top = 8.dp,
                                    bottom = 16.dp
                                ),
                            style = LiturgicalTextStyles.secondary(MaterialTheme.typography.bodyLarge)
                        )
                    }

                if (
                    runtimeOccasion
                        .prayerSequences
                        .isEmpty()
                ) {
                    Text(
                        text =
                            stringResource(Res.string.prayers_empty),
                        modifier =
                            Modifier.padding(
                                top = 24.dp
                            )
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement =
                            Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
                    ) {
                        val noteText = (noteResult as? Result.Success)?.data?.text
                        if (!noteText.isNullOrBlank()) {
                            item {
                                Text(
                                    text = LiturgicalNoteText.annotated(noteText),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    style = LiturgicalTextStyles.note(
                                        MaterialTheme.typography.bodyMedium,
                                        LiturgicalLanguage.Arabic
                                    ),
                                    textAlign = TextAlign.Right
                                )
                            }
                        }
                        items(
                            runtimeOccasion
                                .prayerSequences
                        ) { sequence ->
                            Column(modifier = Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    onOpenPrayer(
                                        sequence.sequence.id
                                    )
                                },
                                modifier =
                                    Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            ) {
                                Column(
                                    modifier =
                                        Modifier.fillMaxWidth(),
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = sequence.prayer.name,
                                        style = LiturgicalTextStyles.title(LiturgicalTitleLevel.Prayer),
                                        textAlign = TextAlign.Center
                                    )

                                    sequence.prayer.description?.let {
                                            description ->
                                        Text(
                                            text = description,
                                            modifier =
                                                Modifier.padding(
                                                    top = 4.dp
                                                ),
                                            style =
                                                LiturgicalTextStyles.secondary(MaterialTheme.typography.bodyMedium),
                                            textAlign =
                                                TextAlign.Center
                                        )
                                    }
                                }
                            }
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun PrayerSequenceNotes(
    platform: PlatformContext,
    noteIds: List<Long>
) {
    val notesResult by produceState<List<String>>(
        initialValue = emptyList(),
        key1 = platform,
        key2 = noteIds
    ) {
        value = noteIds.mapNotNull { noteId ->
            (platform.content.loadNote(noteId) as? Result.Success)
                ?.data?.text?.takeIf { it.isNotBlank() }
        }
    }

    notesResult.forEach { noteText ->
        Text(
            text = noteText,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun PrayerDetailsScreen(
    platform: PlatformContext,
    occasionId: OccasionId?,
    prayerSequenceId: PrayerSequenceId,
    preferredRecordingIds:
        Map<MelodyId, MediaAssetId>,
    onOpenHymn: (LiturgicalItemId) -> Unit,
    onBack: () -> Unit
) {
    val audioService =
        platform.audio

    val playbackState =
        rememberPlaybackState(
            audioService
        )

    val queueController =
        remember(audioService) {
            audioService?.let {
                PlaybackQueueController(
                    it
                )
            }
        }

    val queueState =
        rememberPlaybackQueueState(
            queueController
        )

    val coroutineScope =
        rememberCoroutineScope()

    var playAllMessage by
        remember(
            prayerSequenceId
        ) {
            mutableStateOf<String?>(null)
        }

    LaunchedEffect(
        playbackState,
        queueController
    ) {
        queueController
            ?.handlePlaybackState(
                playbackState
            )
    }

    val occasionResult by
    produceState<Result<RuntimeOccasion>?>(
        initialValue = null,
        key1 = platform,
        key2 = occasionId,
        key3 = prayerSequenceId
    ) {
        value =
            if (occasionId != null) {
                platform.content.loadOccasion(
                    occasionId
                )
            } else {
                null
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 8.dp)
    ) {
        LiturgicalBackNavigation(onClick = {
            queueController?.stop()
            onBack()
        })

        when (
            val result =
                occasionResult
        ) {
            null -> {
                Text(
                    text = stringResource(Res.string.loading)
                )
            }

            is Result.Failure -> {
                Text(
                    text =
                        result.error.message
                            ?: stringResource(Res.string.prayer_failed)
                )
            }

            is Result.Success -> {
                val sequence =
                    result.data
                        .prayerSequences
                        .firstOrNull { item ->
                            item.sequence.id ==
                                    prayerSequenceId
                        }

                if (sequence == null) {
                    Text(
                        text =
                            stringResource(Res.string.prayer_not_found)
                    )
                } else {
                    Text(
                        text = sequence.prayer.name,
                        modifier = Modifier.fillMaxWidth(),
                        style = LiturgicalTextStyles.title(LiturgicalTitleLevel.Prayer),
                        textAlign = TextAlign.Center
                    )

                    sequence.prayer.description?.let {
                            description ->
                        Text(
                            text = description,
                            modifier =
                                Modifier.padding(
                                    top = 8.dp,
                                    bottom = 16.dp
                                ),
                            style = LiturgicalTextStyles.secondary(MaterialTheme.typography.bodyLarge)
                        )
                    }

                    PrayerSequenceNotes(platform, sequence.sequence.noteIds)

                    val queueActive = queueState.status == PlaybackQueueStatus.Loading ||
                        queueState.status == PlaybackQueueStatus.Playing ||
                        queueState.status == PlaybackQueueStatus.Paused
                    androidx.compose.runtime.CompositionLocalProvider(
                        androidx.compose.ui.platform.LocalLayoutDirection provides
                            androidx.compose.ui.unit.LayoutDirection.Ltr
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 8.dp),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (queueActive) {
                                androidx.compose.material3.TextButton(
                                    enabled = queueState.currentIndex != null,
                                    onClick = { queueController?.next() }
                                ) { Text("⏮") }
                                androidx.compose.material3.TextButton(
                                    onClick = {
                                        playAllMessage = null
                                        queueController?.stop()
                                    }
                                ) { Text("■ " + stringResource(Res.string.stop_playback)) }
                                TextButton(
                                    enabled = queueState.status != PlaybackQueueStatus.Loading,
                                    onClick = {
                                        if (queueState.status == PlaybackQueueStatus.Playing)
                                            queueController?.pause()
                                        else queueController?.resume()
                                    }
                                ) {
                                    Text(
                                        if (queueState.status == PlaybackQueueStatus.Playing) "⏸" else "▶"
                                    )
                                }
                                androidx.compose.material3.TextButton(
                                    enabled = queueState.currentIndex != null,
                                    onClick = { queueController?.previous() }
                                ) { Text("⏭") }
                            } else {
                                TextButton(
                                    enabled = queueController != null,
                                    onClick = {
                                        playAllMessage = null
                                        coroutineScope.launch {
                                            when (val built = PrayerPlaybackQueueBuilder(platform.content)
                                                .build(sequence)) {
                                                is Result.Failure ->
                                                    playAllMessage = "prepare"
                                                is Result.Success -> {
                                                    if (built.data.isEmpty()) {
                                                        playAllMessage = "empty"
                                                    } else {
                                                        when (queueController?.start(built.data)) {
                                                            is Result.Success -> playAllMessage = null
                                                            else -> playAllMessage = "start"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                ) { Text(stringResource(Res.string.play_all)) }
                            }
                        }
                    }
                    if (queueActive) {
                        val number = queueState.currentIndex?.plus(1)
                        if (number != null) {
                            Text(
                                text = stringResource(Res.string.recording_progress, number, queueState.entries.size),
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.labelSmall,
                                textAlign = TextAlign.Right
                            )
                        }
                    }

                    playAllMessage?.let {
                            message ->
                        Text(
                            text = when (message) {
                                "prepare" -> stringResource(Res.string.queue_prepare_failed)
                                "empty" -> stringResource(Res.string.queue_empty)
                                "start" -> stringResource(Res.string.queue_start_failed)
                                else -> message
                            },
                            modifier =
                                Modifier.padding(
                                    bottom = 8.dp
                                )
                        )
                    }

                    if (sequence.items.isEmpty()) {
                        Text(
                            text =
                                stringResource(Res.string.liturgical_items_empty)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                        ) {
                            items(sequence.items) {
                                    item ->
                                ResolvedLiturgicalItemView(
                                    item = item,
                                    onOpenHymn = {
                                            liturgicalItemId ->
                                        queueController?.stop()
                                        onOpenHymn(
                                            liturgicalItemId
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

    }
}

@Composable
private fun MissingPrayerSelectionScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(24.dp),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                stringResource(Res.string.prayer_not_selected),
            textAlign =
                TextAlign.Center
        )

        Button(
            onClick = onBack,
            modifier =
                Modifier.padding(top = 8.dp)
        ) {
            Text(
                text = stringResource(Res.string.back)
            )
        }
    }
}

@Composable
private fun ResolvedLiturgicalItemView(
    item: ResolvedLiturgicalItem,
    onOpenHymn: (LiturgicalItemId) -> Unit
) {
    when (
        val target =
            item.target
    ) {
        is ResolvedLiturgicalItemTarget.Text -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 8.dp
                    )
            ) {
                Text(
                    text =
                        target.text.syriac,
                    style =
                        MaterialTheme.typography.bodyLarge
                )

                target.petgomo?.let {
                        petgomo ->
                    Text(
                        text =
                            petgomo.syriac,
                        modifier =
                            Modifier.padding(
                                top = 4.dp
                            ),
                        style =
                            MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        is ResolvedLiturgicalItemTarget.Qolo -> {
            Button(
                onClick = { onOpenHymn(item.item.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                val locationTitle = item.item.locationTitle
                    ?.takeIf { it.isNotBlank() }
                val qoloName = target.qolo.name
                    .takeIf { it.isNotBlank() }
                Text(
                    text = listOfNotNull(locationTitle, qoloName)
                        .joinToString(": ")
                        .ifBlank { "Qolo" },
                    style = LiturgicalTextStyles.section(),
                    textAlign = TextAlign.Center
                )
            }
        }

        is ResolvedLiturgicalItemTarget.NonMelodicText -> {
            Button(
                onClick = { onOpenHymn(item.item.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = item.item.locationTitle
                        ?.takeIf { it.isNotBlank() }
                        ?: "Text",
                    style = LiturgicalTextStyles.section(),
                    textAlign = TextAlign.Center
                )
            }
        }

        is ResolvedLiturgicalItemTarget.UnresolvedQolo -> {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 8.dp
                    ),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Qolo unresolved",
                    style =
                        MaterialTheme.typography.titleMedium,
                    textAlign =
                        TextAlign.Center
                )

                target.verses.forEach { verse ->
                    verse.petgomo?.let { petgomo ->
                        Text(
                            text =
                                petgomo.syriac,
                            modifier =
                                Modifier.padding(
                                    top = 8.dp
                                ),
                            style =
                                MaterialTheme.typography.titleMedium,
                            textAlign =
                                TextAlign.Center
                        )
                    }

                    Text(
                        text =
                            verse.text.syriac,
                        modifier =
                            Modifier.padding(
                                top = 4.dp
                            ),
                        style =
                            SyriacTextStyles.body(),
                        textAlign =
                            TextAlign.Right
                    )
                }
            }
        }
    }
}

@Composable
private fun QoloDetailsScreen(
    platform: PlatformContext,
    qoloId: QoloId,
    onBack: () -> Unit
) {
    val qoloResult by
    produceState<Result<Qolo>?>(
        initialValue = null,
        key1 = platform,
        key2 = qoloId
    ) {
        value =
            platform.content.loadQolo(
                qoloId
            )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(24.dp),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text = "Qolo Details",
            style =
                MaterialTheme.typography.headlineMedium
        )

        when (
            val result =
                qoloResult
        ) {
            null -> {
                Text(
                    text = "Loading...",
                    modifier =
                        Modifier.padding(
                            top = 24.dp
                        )
                )
            }

            is Result.Failure -> {
                Text(
                    text =
                        result.error.message
                            ?: "Qolo loading failed",
                    modifier =
                        Modifier.padding(
                            top = 24.dp
                        ),
                    textAlign =
                        TextAlign.Center
                )
            }

            is Result.Success -> {
                val qolo =
                    result.data

                Text(
                    text = qolo.name,
                    modifier =
                        Modifier.padding(
                            top = 24.dp
                        ),
                    style =
                        MaterialTheme.typography.headlineLarge,
                    textAlign =
                        TextAlign.Center
                )

                qolo.poeticMeter?.let {
                        poeticMeter ->
                    Text(
                        text =
                            "Poetic meter: $poeticMeter",
                        modifier =
                            Modifier.padding(
                                top = 12.dp
                            ),
                        style =
                            MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        Button(
            onClick = onBack,
            modifier =
                Modifier.padding(
                    top = 24.dp
                )
        ) {
            Text(
                text = "Back"
            )
        }
    }
}

@Composable
private fun MissingOccasionSelectionScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(24.dp),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                "No Occasion was selected.",
            textAlign =
                TextAlign.Center
        )

        Button(
            onClick = onBack,
            modifier =
                Modifier.padding(
                    top = 24.dp
                )
        ) {
            Text(
                text = "Back"
            )
        }
    }
}

@Composable
private fun MissingQoloSelectionScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(24.dp),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                "لم تُحدَّد ترتيلة لعرضها.",
            textAlign =
                TextAlign.Center
        )

        Button(
            onClick = onBack,
            modifier =
                Modifier.padding(
                    top = 24.dp
                )
        ) {
            Text(
                text = "Back"
            )
        }
    }
}

@Composable
private fun HymnDetailsScreen(
    platform: PlatformContext,
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
    onBack: () -> Unit
) {
    val itemResult by produceState<Result<ResolvedLiturgicalItem>?>(
        initialValue = null,
        key1 = platform,
        key2 = liturgicalItemId
    ) {
        value = platform.content.loadLiturgicalItem(liturgicalItemId)
    }

    val occurrenceNoteId =
        (itemResult as? Result.Success)?.data?.item?.noteId

    val occurrenceNoteResult by produceState<Result<org.syriacplatform.content.models.Note>?>(
        initialValue = null,
        key1 = platform,
        key2 = occurrenceNoteId
    ) {
        value = occurrenceNoteId?.let { platform.content.loadNote(it) }
    }

    val playbackState = rememberPlaybackState(audioService)
    val activeTextInterval by (
        platform.performanceTextPlayback?.activeInterval
            ?: remember { kotlinx.coroutines.flow.MutableStateFlow<org.syriacplatform.audio.ActiveTextInterval?>(null) }
    ).collectAsState()

    val coroutineScope =
        rememberCoroutineScope()

    // Remember the user's last explicitly chosen translation language.
    // A preference alone never opens translations automatically.
    val userPreferences by platform.preferences.preferences.collectAsState()

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
            key1 = platform,
            key2 = liturgicalItemId
        ) {
            value =
                platform.content.loadPerformanceMedia(
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
        key1 = platform,
        key2 = toolbarMelodyId
    ) {
        value = toolbarMelodyId?.let { platform.content.loadMelodyNotations(it) }
    }
    val notationCount = (toolbarNotations as? Result.Success)?.data?.size ?: 0
    val toolbarLanguages = toolbarVerses.flatMap { verse ->
        verse.petgomo?.translations.orEmpty() + verse.text.translations
    }.filter { it.content.isNotBlank() }.map { it.language }.distinct().sorted()
    val preferredTranslationLanguage = userPreferences.translationLanguage
    val orderedToolbarLanguages = toolbarLanguages.sortedWith(
        compareBy<String> { it != preferredTranslationLanguage }.thenBy { it }
    )

    androidx.compose.foundation.layout.Box(
        modifier = Modifier.fillMaxSize().safeDrawingPadding()
    ) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(start = 24.dp, end = 24.dp, top = 50.dp, bottom = 8.dp)
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
                                    ?.takeIf { it.isNotBlank() } ?: "Text",
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
                                                platform.performanceTextPlayback?.clearActiveInterval()
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
                            text = "التسجيلات المتاحة",
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
                                            platform.performanceTextPlayback?.clearActiveInterval()
                                            selectedPerformanceId = resolved.performance.id
                                            performancePlaybackMessage = null
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                ) {
                                    val performer = resolved.mediaAsset.performer
                                        ?.takeIf { it.isNotBlank() } ?: "تسجيل النص"
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
                        Text("تعذّر تحميل تسجيلات النص")
                    }
                    performancePlaybackMessage?.let { message ->
                        Text(text = message, modifier = Modifier.padding(bottom = 8.dp))
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 92.dp)
                    ) {
                        itemsIndexed(target.verses) { index, verse ->
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth().clickable(
                                    enabled = selectedPerformance != null &&
                                        platform.performanceTextPlayback != null,
                                    onClick = {
                                        val performance = selectedPerformance
                                        val playback = platform.performanceTextPlayback
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
                                                            ?: "Performance text playback failed"
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
                                            platform.performanceTextPlayback?.clearActiveInterval()
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
                        Text(
                            text = "الألحان المتاحة",
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

                        target.melodyCandidates.forEach { melody ->
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
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
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
                        platform = platform,
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
                        key1 = platform,
                        key2 = selectedMelody?.id
                    ) {
                        value =
                            if (selectedMelody != null) {
                                platform.content.loadMelodyRecordings(
                                    selectedMelody.id
                                )
                            } else {
                                null
                            }
                    }

                    when (val recordings = recordingsResult) {
                        null -> Unit

                        is Result.Failure -> {
                            Text(
                                "تعذّر تحميل تسجيلات اللحن"
                            )
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
                                                                    platform.performanceTextPlayback?.clearActiveInterval()
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
                                                                    platform.performanceTextPlayback?.clearActiveInterval()
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
                                                                ) "إيقاف اللحن مؤقتًا" else "سماع اللحن"
                                                            )
                                                        }
                                                    }
                                        androidx.compose.material3.TextButton(
                                            onClick = { melodyRecordingsExpanded = !melodyRecordingsExpanded }
                                        ) { Text(
                            text = "التسجيلات المتاحة",
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
                                            ?: "تسجيل لحن"

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
                                                platform.performanceTextPlayback?.clearActiveInterval()

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
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
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
                            text = "تسجيلات الترتيلة المتاحة",
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
                                                ?: "تسجيل ترتيلة"

                                        Button(
                                            enabled =
                                                platform.performanceTextPlayback !=
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
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
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
                                    "تعذّر تحميل تسجيلات الترتيلة"
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
                        Text("لا يوجد نص متاح لهذه الترتيلة")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 92.dp)
                        ) {
                            item {
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
                            itemsIndexed(target.verses) { verseIndex, verse ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth().clickable(
                                        enabled = selectedPerformance != null &&
                                            platform.performanceTextPlayback != null,
                                        onClick = {
                                        val performance =
                                            selectedPerformance
                                        val playback =
                                            platform.performanceTextPlayback

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
                                                                ?: "Performance text playback failed"
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
            }
        }

    }
    androidx.compose.material3.Surface(
        modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        shadowElevation = 3.dp
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)
        ) {
            androidx.compose.material3.TextButton(
                onClick = { translationMenuOpen = true },
                enabled = toolbarLanguages.isNotEmpty(),
                modifier = Modifier.align(Alignment.CenterStart)
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
                                    platform.preferences.update(
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
            androidx.compose.material3.TextButton(
                enabled = notationCount > 0,
                onClick = {
                    if (notationCount == 1) {
                        notationRequestedPage = 0
                        notationOpenRequest++
                    }
                    else notationChoiceOpen = true
                },
                modifier = Modifier.align(Alignment.Center)
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
            androidx.compose.material3.TextButton(
                onClick = { audioService?.stop(); onBack() },
                modifier = Modifier.align(Alignment.CenterEnd)
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
                platform.performanceTextPlayback?.clearActiveInterval()
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
    shownTranslation?.let { translation ->
        androidx.compose.material3.Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
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

/**
 * The Petgomo and its following text remain separate content entities.
 * Only their available translations are combined for display, in Syriac
 * reading order: Petgomo first, then text. A missing translation does not
 * suppress the available one.
 */
internal fun combinedVerseTranslation(
    petgomoTranslations: List<org.syriacplatform.content.models.TextTranslation>,
    textTranslations: List<org.syriacplatform.content.models.TextTranslation>,
    language: String?
): String? {
    if (language == null) return null
    val parts = listOf(petgomoTranslations, textTranslations).mapNotNull { translations ->
        translations.firstOrNull { it.language == language && it.content.isNotBlank() }
            ?.content?.trim()
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString("\n\n")
}

@Composable
private fun VerseTextWithTranslation(
    verseNumber: Int,
    petgomo: String?,
    syriacText: String,
    petgomoTranslations: List<org.syriacplatform.content.models.TextTranslation>,
    textTranslations: List<org.syriacplatform.content.models.TextTranslation>,
    language: String?,
    onShowTranslation: (String) -> Unit
) {
    androidx.compose.runtime.CompositionLocalProvider(
        androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                petgomo?.let {
                    Text(
                        text = remember(it) { LiturgicalAnnotatedText.fromSource(it, allRed = true) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        style = SyriacTextStyles.body(),
                        color = LiturgicalColors.Red,
                        textAlign = TextAlign.Right
                    )
                }
                Text(
                    text = remember(syriacText) { LiturgicalAnnotatedText.fromSource(syriacText) },
                    modifier = Modifier.fillMaxWidth(),
                    style = SyriacTextStyles.body(),
                    color = LiturgicalColors.Black,
                    textAlign = TextAlign.Right
                )
            }
            Column(
                modifier = Modifier.padding(start = 5.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = verseNumber.toString(),
                    modifier = Modifier.padding(top = 10.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                VerseTranslationTrigger(
                    petgomoTranslations = petgomoTranslations,
                    textTranslations = textTranslations,
                    language = language,
                    onShow = onShowTranslation
                )
            }
        }
    }
}

@Composable
private fun VerseTranslationTrigger(
    petgomoTranslations: List<org.syriacplatform.content.models.TextTranslation>,
    textTranslations: List<org.syriacplatform.content.models.TextTranslation>,
    language: String?,
    onShow: (String) -> Unit
) {
    val translation = combinedVerseTranslation(petgomoTranslations, textTranslations, language)
    if (translation != null) {
        Text(
            text = if (language == "ar") "ع" else if (language == "en") "en" else language.orEmpty(),
            modifier = Modifier
                .clickable { onShow(translation) }
                .padding(start = 5.dp, end = 3.dp, top = 6.dp, bottom = 6.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun rememberPlaybackQueueState(
    controller: PlaybackQueueController?
): PlaybackQueueState {
    if (controller == null) {
        return PlaybackQueueState()
    }

    val state by
        controller.state.collectAsState()

    return state
}

@Composable
private fun rememberPlaybackState(
    audioService: AudioService?
): PlaybackState {
    if (audioService == null) {
        return PlaybackState()
    }

    val state by audioService.state.collectAsState()
    return state
}

@Composable
private fun MissingHymnSelectionScreen(
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(24.dp),
        verticalArrangement =
            Arrangement.Center,
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text =
                "No Hymn was selected.",
            textAlign =
                TextAlign.Center
        )

        Button(
            onClick = onBack,
            modifier =
                Modifier.padding(
                    top = 24.dp
                )
        ) {
            Text("Back")
        }
    }
}

private sealed interface NotationImageState {
    data object Loading : NotationImageState
    data class Loaded(val bitmap: ImageBitmap) : NotationImageState
    data object Failed : NotationImageState
}

/**
 * Experimental notation viewer. The media paths are relative to the
 * packaged Compose resources, just like ResourcePackageSource.
 */
@Composable
private fun MelodyNotationSection(
    platform: PlatformContext,
    melodyId: MelodyId?,
    initialOpen: Boolean,
    initialPage: Int,
    onNotationChanged: (Boolean, Int) -> Unit,
    openRequest: Int = 0,
    requestedPage: Int = 0,
    showInlineButton: Boolean = true
) {
    val notationResult by produceState<Result<List<MediaAsset>>?>(
        initialValue = null,
        key1 = platform,
        key2 = melodyId
    ) {
        value = melodyId?.let { platform.content.loadMelodyNotations(it) }
    }
    var showNotation by remember(melodyId) { mutableStateOf(initialOpen) }
    var page by remember(melodyId) { mutableStateOf(initialPage) }
    LaunchedEffect(openRequest) {
        if (openRequest > 0) {
            page = requestedPage
            showNotation = true
        }
    }
    SideEffect { onNotationChanged(showNotation, page) }

    when (val result = notationResult) {
        null -> if (melodyId != null) Text("Loading notation...")
        is Result.Failure -> Text(result.error.message ?: "Notation loading failed")
        is Result.Success -> {
            val assets = result.data
            if (assets.isNotEmpty()) {
                if (showInlineButton) {
                    Button(onClick = {
                        page = 0
                        showNotation = true
                    }) {
                        Text("View notation (${assets.size})")
                    }
                }
                if (showNotation) {
                    val asset = assets[page.coerceIn(0, assets.lastIndex)]
                    val imageState by produceState<NotationImageState>(
                        initialValue = NotationImageState.Loading,
                        key1 = asset.path
                    ) {
                        value = try {
                            NotationImageState.Loaded(
                                Res.readBytes("files/${asset.path}").decodeToImageBitmap()
                            )
                        } catch (_: Exception) {
                            NotationImageState.Failed
                        }
                    }
                    val bitmap = (imageState as? NotationImageState.Loaded)?.bitmap
                    var zoom by rememberSaveable(asset.path) { mutableStateOf(1f) }
                    var panX by rememberSaveable(asset.path) { mutableStateOf(0f) }
                    var panY by rememberSaveable(asset.path) { mutableStateOf(0f) }
                    val pan = Offset(panX, panY)
                    fun setPan(value: Offset) {
                        panX = value.x
                        panY = value.y
                    }
                    var viewportWidth by remember { mutableStateOf(0f) }
                    var viewportHeight by remember { mutableStateOf(0f) }

                    fun constrainedPan(candidate: Offset, scale: Float): Offset {
                        val image = bitmap ?: return Offset.Zero
                        if (viewportWidth <= 0f || viewportHeight <= 0f) return Offset.Zero
                        val fit = minOf(
                            viewportWidth / image.width,
                            viewportHeight / image.height
                        )
                        val horizontalLimit =
                            ((image.width * fit * scale - viewportWidth) / 2f).coerceAtLeast(0f)
                        val verticalLimit =
                            ((image.height * fit * scale - viewportHeight) / 2f).coerceAtLeast(0f)
                        return Offset(
                            candidate.x.coerceIn(-horizontalLimit, horizontalLimit),
                            candidate.y.coerceIn(-verticalLimit, verticalLimit)
                        )
                    }

                    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
                        val nextZoom = (zoom * zoomChange).coerceIn(1f, 5f)
                        setPan(constrainedPan(pan + panChange, nextZoom))
                        zoom = nextZoom
                    }
                    Dialog(
                        onDismissRequest = { showNotation = false },
                        properties = DialogProperties(usePlatformDefaultWidth = false)
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .safeDrawingPadding()
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        "Notation ${page + 1} / ${assets.size}",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    TextButton(onClick = { showNotation = false }) {
                                        Text("Close")
                                    }
                                }
                                Box(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (val state = imageState) {
                                        NotationImageState.Loading ->
                                            Text("Loading notation image...")
                                        NotationImageState.Failed ->
                                            Text("Unable to display this notation image.")
                                        is NotationImageState.Loaded -> Image(
                                            bitmap = state.bitmap,
                                            contentDescription = "Notation page ${page + 1}",
                                            contentScale = ContentScale.Fit,
                                            modifier = Modifier.fillMaxSize()
                                                .onSizeChanged {
                                                    viewportWidth = it.width.toFloat()
                                                    viewportHeight = it.height.toFloat()
                                                    setPan(constrainedPan(pan, zoom))
                                                }
                                                .transformable(transformState)
                                                .graphicsLayer(
                                                    scaleX = zoom,
                                                    scaleY = zoom,
                                                    translationX = pan.x,
                                                    translationY = pan.y
                                                )
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        enabled = page > 0,
                                        onClick = { page-- }
                                    ) { Text("Previous") }
                                    TextButton(onClick = {
                                        zoom = 1f
                                        setPan(Offset.Zero)
                                    }) {
                                        Text("Reset zoom")
                                    }
                                    TextButton(
                                        enabled = page < assets.lastIndex,
                                        onClick = { page++ }
                                    ) { Text("Next") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

