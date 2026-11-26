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
import org.syriacplatform.resources.occasions_title
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
import androidx.compose.material3.HorizontalDivider
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
    // Keep the device direction when the user has not chosen an interface language.
    val deviceLayoutDirection = LocalLayoutDirection.current
    val interfaceLayoutDirection = when (uiLanguage) {
        "ar" -> LayoutDirection.Rtl
        "en", "fr", "de" -> LayoutDirection.Ltr
        else -> deviceLayoutDirection
    }

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
        LocalAppLocale provides uiLanguage,
        LocalLayoutDirection provides interfaceLayoutDirection
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
                    val nextLiturgicalItemId by produceState<LiturgicalItemId?>(
                        initialValue = null,
                        key1 = selectedOccasionId,
                        key2 = selectedPrayerSequenceId,
                        key3 = liturgicalItemId
                    ) {
                        value = selectedOccasionId?.let { occasionId ->
                            val loaded = platform.content.loadOccasion(occasionId)
                            val occasion = (loaded as? Result.Success)?.data
                            val sequence = occasion?.prayerSequences?.firstOrNull {
                                it.sequence.id == selectedPrayerSequenceId
                            }
                            val navigableIds = sequence?.items
                                ?.filter {
                                    it.target is ResolvedLiturgicalItemTarget.Qolo ||
                                        it.target is ResolvedLiturgicalItemTarget.NonMelodicText
                                }
                                ?.map { it.item.id }
                                .orEmpty()
                            val index = navigableIds.indexOf(liturgicalItemId)
                            if (index >= 0) navigableIds.getOrNull(index + 1) else null
                        }
                    }
                    HymnDetailsScreen(
                        services = object : HymnDetailsServices {
                            override val content = platform.content
                            override val preferences = platform.preferences
                            override val performanceTextPlayback = platform.performanceTextPlayback
                        },
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
                        onNextItem = nextLiturgicalItemId?.let { nextId ->
                            {
                                platform.audio?.stop()
                                selectedLiturgicalItemId = nextId
                                melodySession = null
                                notationOpenSession = false
                                notationPageSession = 0
                                publishSession(AppDestination.HYMN_DETAILS)
                            }
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
                MaterialTheme.colorScheme.surface
            )
            .padding(16.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.occasions_title),
            style = LiturgicalTextStyles.title(LiturgicalTitleLevel.Occasion),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(onClick = onOpenFontSize) {
                Text(stringResource(Res.string.settings_font_size))
            }
            TextButton(onClick = onOpenLanguage) {
                Text(stringResource(Res.string.settings_interface_language))
            }
        }
        HorizontalDivider(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant)
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
                            Arrangement.spacedBy(6.dp)
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
    Column(
        modifier = Modifier.fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = occasion.name,
            style = LiturgicalTextStyles.title(LiturgicalTitleLevel.Prayer),
            textAlign = TextAlign.Center
        )
        occasion.description?.let { description ->
            Text(
                text = description,
                modifier = Modifier.padding(top = 4.dp),
                style = LiturgicalTextStyles.secondary(MaterialTheme.typography.bodyMedium),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LiturgicalBackNavigation(onClick: () -> Unit, title: String? = null) {
    androidx.compose.foundation.layout.Box(modifier = Modifier.fillMaxWidth()) {
        if (title != null) {
            Text(
                text = title,
                style = LiturgicalTextStyles.title(LiturgicalTitleLevel.Prayer),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().align(Alignment.Center)
            )
        }
        TextButton(onClick = onClick, modifier = Modifier.align(Alignment.CenterStart)) {
            Text(stringResource(Res.string.back))
        }
    }
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
        color = MaterialTheme.colorScheme.outlineVariant
    )
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
                MaterialTheme.colorScheme.surface
            )
            .padding(16.dp)
    ) {
        LiturgicalBackNavigation(onClick = onBack, title = (occasionResult as? Result.Success)?.data?.occasion?.name)

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
                            ?: stringResource(Res.string.occasion_failed),
                    textAlign =
                        TextAlign.Center
                )
            }

            is Result.Success -> {
                val runtimeOccasion =
                    result.data

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
                            Arrangement.spacedBy(6.dp)
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
                            Column(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { onOpenPrayer(sequence.sequence.id) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
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
                MaterialTheme.colorScheme.surface
            )
            .padding(16.dp)
    ) {
        LiturgicalBackNavigation(onClick = {
            queueController?.stop()
            onBack()
        }, title = (occasionResult as? Result.Success)?.data?.prayerSequences
            ?.firstOrNull { it.sequence.id == prayerSequenceId }?.prayer?.name)

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
                    // Keep the original Arabic queue order; mirror it for Latin UI languages.
                    val queueLayoutDirection = if (LocalLayoutDirection.current == LayoutDirection.Rtl)
                        LayoutDirection.Ltr else LayoutDirection.Rtl
                    val arabicQueueControls = LocalLayoutDirection.current == LayoutDirection.Rtl
                    androidx.compose.runtime.CompositionLocalProvider(
                        LocalLayoutDirection provides queueLayoutDirection
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
                                ) { Text(if (arabicQueueControls) "⏮" else "⏭") }
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
                                ) { Text(if (arabicQueueControls) "⏭" else "⏮") }
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
                                Arrangement.spacedBy(6.dp)
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
            Column(
                modifier = Modifier.fillMaxWidth()
                    .clickable { onOpenHymn(item.item.id) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
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
            Column(
                modifier = Modifier.fillMaxWidth()
                    .clickable { onOpenHymn(item.item.id) }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = item.item.locationTitle
                        ?.takeIf { it.isNotBlank() }
                        ?: stringResource(Res.string.generic_text),
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
                    text = stringResource(Res.string.qolo_unresolved),
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
            text = stringResource(Res.string.qolo_details),
            style =
                MaterialTheme.typography.headlineMedium
        )

        when (
            val result =
                qoloResult
        ) {
            null -> {
                Text(
                    text = stringResource(Res.string.loading),
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
                            ?: stringResource(Res.string.qolo_failed),
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
                            stringResource(Res.string.poetic_meter, poeticMeter.toString()),
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
                text = stringResource(Res.string.back)
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
                stringResource(Res.string.occasion_not_selected),
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
                text = stringResource(Res.string.back)
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
                stringResource(Res.string.qolo_not_selected),
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
                text = stringResource(Res.string.back)
            )
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
internal fun VerseTextWithTranslation(
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
internal fun rememberPlaybackState(
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
                stringResource(Res.string.hymn_not_selected),
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
            Text(stringResource(Res.string.back))
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
internal fun MelodyNotationSection(
    content: org.syriacplatform.content.contracts.ContentService,
    loadNotationBytes: (suspend (String) -> ByteArray?)? = null,
    melodyId: MelodyId?,
    initialOpen: Boolean,
    initialPage: Int,
    onNotationChanged: (Boolean, Int) -> Unit,
    openRequest: Int = 0,
    requestedPage: Int = 0,
    showInlineButton: Boolean = true
) {
    val cache = LocalNotationCache.current
    val notationResult by produceState<Result<List<MediaAsset>>?>(
        initialValue = melodyId?.let { cache?.notations(it) }?.let { Result.Success(it) },
        key1 = content,
        key2 = melodyId
    ) {
        value = melodyId?.let { id ->
            cache?.notations(id)?.let { Result.Success(it) } ?: content.loadMelodyNotations(id).also { result ->
                if (result is Result.Success) cache?.putNotations(id, result.data)
            }
        }
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
        null -> if (melodyId != null) Text(stringResource(Res.string.notation_loading))
        is Result.Failure -> Text(result.error.message ?: stringResource(Res.string.notation_failed))
        is Result.Success -> {
            val assets = result.data
            if (assets.isNotEmpty()) {
                if (showInlineButton) {
                    Button(onClick = {
                        page = 0
                        showNotation = true
                    }) {
                        Text(stringResource(Res.string.view_notation, assets.size))
                    }
                }
                if (showNotation) {
                    val asset = assets[page.coerceIn(0, assets.lastIndex)]
                    val imageState by produceState<NotationImageState>(
                        initialValue = cache?.image(asset.path)?.let { NotationImageState.Loaded(it) } ?: NotationImageState.Loading,
                        key1 = asset.path,
                        key2 = loadNotationBytes
                    ) {
                        value = try {
                            cache?.image(asset.path)?.let { NotationImageState.Loaded(it) } ?: run {
                            val bytes = loadNotationBytes?.invoke(asset.path)
                                ?: if (loadNotationBytes == null) Res.readBytes("files/${asset.path}") else error("Notation media not found")
                            NotationImageState.Loaded(bytes.decodeToImageBitmap().also { cache?.putImage(asset.path, it) })
                            }
                        } catch (cancelled: kotlinx.coroutines.CancellationException) {
                            throw cancelled
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
                                        stringResource(Res.string.notation_progress, page + 1, assets.size),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    TextButton(onClick = { showNotation = false }) {
                                        Text(stringResource(Res.string.close))
                                    }
                                }
                                Box(
                                    modifier = Modifier.fillMaxWidth().weight(1f),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when (val state = imageState) {
                                        NotationImageState.Loading ->
                                            Text(stringResource(Res.string.notation_image_loading))
                                        NotationImageState.Failed ->
                                            Text(stringResource(Res.string.notation_image_failed))
                                        is NotationImageState.Loaded -> Image(
                                            bitmap = state.bitmap,
                                            contentDescription = stringResource(Res.string.notation_page, page + 1),
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
                                    ) { Text(stringResource(Res.string.previous)) }
                                    TextButton(onClick = {
                                        zoom = 1f
                                        setPan(Offset.Zero)
                                    }) {
                                        Text(stringResource(Res.string.reset_zoom))
                                    }
                                    TextButton(
                                        enabled = page < assets.lastIndex,
                                        onClick = { page++ }
                                    ) { Text(stringResource(Res.string.next)) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

