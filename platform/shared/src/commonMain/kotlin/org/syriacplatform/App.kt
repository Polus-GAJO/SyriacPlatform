package org.syriacplatform

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.syriacplatform.audio.contracts.AudioService
import org.syriacplatform.audio.models.PlaybackState
import org.syriacplatform.audio.models.PlaybackStatus
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.runtime.ResolvedLiturgicalItem
import org.syriacplatform.content.runtime.ResolvedLiturgicalItemTarget
import org.syriacplatform.content.runtime.RuntimeOccasion
import org.syriacplatform.context.PlatformContext
import org.syriacplatform.navigation.AppDestination
import org.syriacplatform.common.types.PrayerSequenceId
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.presentation.theme.SyriacTextStyles

@Composable
fun App(
    platform: PlatformContext
) {
    val navigationState by
    platform.navigation.state.collectAsState()

    var selectedOccasionId by remember {
        mutableStateOf<OccasionId?>(null)
    }

    var selectedPrayerSequenceId by remember {
        mutableStateOf<PrayerSequenceId?>(null)
    }

    var selectedLiturgicalItemId by remember {
        mutableStateOf<LiturgicalItemId?>(null)
    }

    var selectedQoloId by remember {
        mutableStateOf<QoloId?>(null)
    }

    MaterialTheme {
        when (navigationState.currentDestination) {

            AppDestination.HOME -> {
                HomeScreen(
                    platform = platform,
                    onOpenOccasion = { occasionId ->
                        selectedOccasionId =
                            occasionId

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
                        onBack = {
                            selectedLiturgicalItemId =
                                null

                            platform.navigation.navigateTo(
                                AppDestination.PRAYER_DETAILS
                            )
                        }
                    )
                } else {
                    MissingHymnSelectionScreen(
                        onBack = {
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

                            platform.navigation.navigateTo(
                                AppDestination.PRAYER_DETAILS
                            )
                        },
                        onBack = {
                            selectedOccasionId = null
                            selectedPrayerSequenceId = null

                            platform.navigation.navigateTo(
                                AppDestination.HOME
                            )
                        }
                    )
                } else {
                    MissingOccasionSelectionScreen(
                        onBack = {
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
                        onOpenHymn = { liturgicalItemId ->
                            selectedLiturgicalItemId =
                                liturgicalItemId

                            platform.navigation.navigateTo(
                                AppDestination.HYMN_DETAILS
                            )
                        },
                        onBack = {
                            selectedPrayerSequenceId = null

                            platform.navigation.navigateTo(
                                AppDestination.OCCASION_DETAILS
                            )
                        }
                    )
                } else {
                    MissingPrayerSelectionScreen(
                        onBack = {
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
                            selectedQoloId =
                                null

                            if (selectedOccasionId != null) {
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
                } else {
                    MissingQoloSelectionScreen(
                        onBack = {
                            if (selectedOccasionId != null) {
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
}

@Composable
private fun HomeScreen(
    platform: PlatformContext,
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
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(24.dp),
        horizontalAlignment =
            Alignment.CenterHorizontally
    ) {
        Text(
            text = "SyriacPlatform",
            style =
                MaterialTheme.typography.headlineMedium
        )

        Text(
            text = "Occasions",
            modifier = Modifier.padding(
                top = 8.dp,
                bottom = 24.dp
            ),
            style =
                MaterialTheme.typography.titleLarge
        )

        when (
            val result =
                occasionsResult
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
                            ?: "Occasions loading failed",
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
                            "No Occasions available"
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement =
                            Arrangement.spacedBy(
                                12.dp
                            )
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
            Modifier.fillMaxWidth()
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
                style = SyriacTextStyles.body(),
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
                        MaterialTheme.typography.bodyMedium,
                    textAlign =
                        TextAlign.Center
                )
            }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(24.dp)
    ) {
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
                            ?: "Occasion loading failed",
                    textAlign =
                        TextAlign.Center
                )
            }

            is Result.Success -> {
                val runtimeOccasion =
                    result.data

                Text(
                    text = runtimeOccasion.occasion.name,
                    style = SyriacTextStyles.body()
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
                                )
                        )
                    }

                if (
                    runtimeOccasion
                        .prayerSequences
                        .isEmpty()
                ) {
                    Text(
                        text =
                            "No prayer sequences available",
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
                            Arrangement.spacedBy(
                                20.dp
                            )
                    ) {
                        items(
                            runtimeOccasion
                                .prayerSequences
                        ) { sequence ->
                            Button(
                                onClick = {
                                    onOpenPrayer(
                                        sequence.sequence.id
                                    )
                                },
                                modifier =
                                    Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier =
                                        Modifier.fillMaxWidth(),
                                    horizontalAlignment =
                                        Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = sequence.prayer.name,
                                        style = SyriacTextStyles.body(),
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
                                                MaterialTheme.typography.bodyMedium,
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
private fun PrayerDetailsScreen(
    platform: PlatformContext,
    occasionId: OccasionId?,
    prayerSequenceId: PrayerSequenceId,
    onOpenHymn: (LiturgicalItemId) -> Unit,
    onBack: () -> Unit
) {
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
            .background(
                MaterialTheme.colorScheme.primaryContainer
            )
            .padding(24.dp)
    ) {
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
                            ?: "Prayer loading failed"
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
                            "Prayer sequence was not found."
                    )
                } else {
                    Text(
                        text = sequence.prayer.name,
                        style = SyriacTextStyles.body()
                    )

                    sequence.prayer.description?.let {
                            description ->
                        Text(
                            text = description,
                            modifier =
                                Modifier.padding(
                                    top = 8.dp,
                                    bottom = 16.dp
                                )
                        )
                    }

                    if (sequence.items.isEmpty()) {
                        Text(
                            text =
                                "No liturgical items available"
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(
                                    12.dp
                                )
                        ) {
                            items(sequence.items) {
                                    item ->
                                ResolvedLiturgicalItemView(
                                    item = item,
                                    onOpenHymn =
                                        onOpenHymn
                                )
                            }
                        }
                    }
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
                "No Prayer was selected.",
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
                onClick = {
                    onOpenHymn(
                        item.item.id
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = 8.dp
                    )
            ) {
                Column(
                    modifier =
                        Modifier.fillMaxWidth(),
                    horizontalAlignment =
                        Alignment.CenterHorizontally
                ) {
                    Text(
                        text = target.qolo.name,
                        style =
                            SyriacTextStyles.body(),
                        textAlign =
                            TextAlign.Center
                    )

                    Text(
                        text =
                            target.effectiveMelody?.name
                        ?: "Melody unresolved",
                        modifier =
                            Modifier.padding(
                                top = 4.dp
                            ),
                        style =
                            MaterialTheme.typography.bodyMedium,
                        textAlign =
                            TextAlign.Center
                    )
                }
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
                "No Qolo was selected.",
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
    onBack: () -> Unit
) {
    val itemResult by produceState<Result<ResolvedLiturgicalItem>?>(
        initialValue = null,
        key1 = platform,
        key2 = liturgicalItemId
    ) {
        value = platform.content.loadLiturgicalItem(liturgicalItemId)
    }

    val playbackState = rememberPlaybackState(audioService)

    var pendingAutoPlayId by remember(liturgicalItemId) {
        mutableStateOf<MediaAssetId?>(null)
    }

    var isSeeking by remember(liturgicalItemId) {
        mutableStateOf(false)
    }

    var seekPositionMs by remember(liturgicalItemId) {
        mutableStateOf(0L)
    }

    var selectedRecordingId by remember(liturgicalItemId) {
        mutableStateOf<MediaAssetId?>(null)
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(24.dp)
    ) {
        when (val result = itemResult) {
            null -> Text("Loading...")

            is Result.Failure -> {
                Text(result.error.message ?: "Hymn loading failed")
            }

            is Result.Success -> {
                val target = result.data.target

                if (target is ResolvedLiturgicalItemTarget.Qolo) {
                    val effectiveMelody = target.effectiveMelody

                    Text(
                        text = target.qolo.name,
                        style = SyriacTextStyles.body()
                    )

                    Text(
                        text = effectiveMelody?.name ?: "Melody unresolved",
                        modifier = Modifier.padding(top = 6.dp, bottom = 12.dp),
                        style = MaterialTheme.typography.titleMedium
                    )

                    val recordingsResult by produceState<Result<List<MediaAsset>>?>(
                        initialValue = null,
                        key1 = platform,
                        key2 = effectiveMelody?.id
                    ) {
                        value =
                            if (effectiveMelody != null) {
                                platform.content.loadMelodyRecordings(
                                    effectiveMelody.id
                                )
                            } else {
                                null
                            }
                    }

                    when (val recordings = recordingsResult) {
                        null -> {
                            if (effectiveMelody != null) {
                                Text("Loading recordings...")
                            }
                        }

                        is Result.Failure -> {
                            Text(
                                recordings.error.message
                                    ?: "Recordings loading failed"
                            )
                        }

                        is Result.Success -> {
                            val availableRecordings =
                                recordings.data

                            val selectedRecording =
                                availableRecordings
                                    .firstOrNull { recording ->
                                        recording.id ==
                                            selectedRecordingId
                                    }
                                    ?: availableRecordings.firstOrNull()

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
                                }
                            }

                            if (availableRecordings.size > 1) {
                                Text(
                                    text = "Recordings",
                                    modifier =
                                        Modifier.padding(
                                            bottom = 8.dp
                                        ),
                                    style =
                                        MaterialTheme.typography.titleSmall
                                )

                                availableRecordings.forEach {
                                        recording ->

                                    val performerName =
                                        recording.performer
                                            ?.takeIf {
                                                it.isNotBlank()
                                            }
                                            ?: "Recording ${recording.id.value}"

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

                                                selectedRecordingId =
                                                    recording.id

                                                seekPositionMs = 0L
                                                isSeeking = false
                                            }
                                        },
                                        modifier =
                                            Modifier
                                                .fillMaxWidth()
                                                .padding(
                                                    bottom = 6.dp
                                                )
                                    ) {
                                        Text(
                                            text =
                                                if (
                                                    selectedRecording?.id ==
                                                    recording.id
                                                ) {
                                                    "[Selected] $performerName"
                                                } else {
                                                    performerName
                                                }
                                        )
                                    }
                                }
                            }

                            if (selectedRecording != null) {
                                when (
                                    val status =
                                        playbackState.status
                                ) {
                                    PlaybackStatus.Playing -> {
                                        Button(
                                            enabled =
                                                audioService != null,
                                            onClick = {
                                                audioService?.pause()
                                            },
                                            modifier =
                                                Modifier.padding(
                                                    bottom = 8.dp
                                                )
                                        ) {
                                            Text("Pause")
                                        }
                                    }

                                    PlaybackStatus.Paused -> {
                                        Button(
                                            enabled =
                                                audioService != null,
                                            onClick = {
                                                audioService?.play()
                                            },
                                            modifier =
                                                Modifier.padding(
                                                    bottom = 8.dp
                                                )
                                        ) {
                                            Text("Play")
                                        }
                                    }

                                    PlaybackStatus.Ready,
                                    PlaybackStatus.Ended -> {
                                        Button(
                                            enabled =
                                                audioService != null &&
                                                playbackState.mediaAssetId ==
                                                    selectedRecording.id,
                                            onClick = {
                                                audioService?.play()
                                            },
                                            modifier =
                                                Modifier.padding(
                                                    bottom = 8.dp
                                                )
                                        ) {
                                            Text("Play")
                                        }
                                    }

                                    PlaybackStatus.Loading -> {
                                        Button(
                                            enabled = false,
                                            onClick = {},
                                            modifier =
                                                Modifier.padding(
                                                    bottom = 8.dp
                                                )
                                        ) {
                                            Text("Loading...")
                                        }
                                    }

                                    PlaybackStatus.Error,
                                    PlaybackStatus.Idle -> {
                                        Button(
                                            enabled =
                                                audioService != null,
                                            onClick = {
                                                if (
                                                    audioService !=
                                                        null
                                                ) {
                                                    pendingAutoPlayId =
                                                        selectedRecording.id

                                                    if (
                                                        audioService.load(
                                                            selectedRecording
                                                        )
                                                        is Result.Failure
                                                    ) {
                                                        pendingAutoPlayId =
                                                            null
                                                    }
                                                }
                                            },
                                            modifier =
                                                Modifier.padding(
                                                    bottom = 8.dp
                                                )
                                        ) {
                                            Text("Play recording")
                                        }
                                    }
                                }

                                if (
                                    playbackState.mediaAssetId ==
                                    selectedRecording.id &&
                                    playbackState.status != PlaybackStatus.Idle &&
                                    playbackState.status != PlaybackStatus.Ended &&
                                    playbackState.status != PlaybackStatus.Error
                                ) {
                                    Button(
                                        enabled =
                                            audioService != null,
                                        onClick = {
                                            pendingAutoPlayId =
                                                null
                                            audioService?.stop()
                                        },
                                        modifier =
                                            Modifier.padding(
                                                bottom = 8.dp
                                            )
                                    ) {
                                        Text("Stop")
                                    }
                                }

                                if (
                                    playbackState.mediaAssetId ==
                                    selectedRecording.id
                                ) {
                                    Text("Audio: ${playbackState.status}")

                                    Text(
                                        "Position: ${playbackState.positionMs} ms"
                                    )

                                    playbackState.durationMs
                                        ?.takeIf { duration ->
                                            duration > 0L
                                        }
                                        ?.let { duration ->
                                            val sliderValue =
                                                seekPositionMs
                                                    .coerceIn(
                                                        0L,
                                                        duration
                                                    )
                                                    .toFloat()

                                            Slider(
                                                value =
                                                    sliderValue,
                                                onValueChange = {
                                                        value ->
                                                    isSeeking =
                                                        true

                                                    seekPositionMs =
                                                        value
                                                            .toLong()
                                                            .coerceIn(
                                                                0L,
                                                                duration
                                                            )
                                                },
                                                onValueChangeFinished = {
                                                    audioService?.seekTo(
                                                        seekPositionMs
                                                            .coerceIn(
                                                                0L,
                                                                duration
                                                            )
                                                    )

                                                    isSeeking =
                                                        false
                                                },
                                                valueRange =
                                                    0f..duration.toFloat(),
                                                enabled =
                                                    audioService != null &&
                                                    playbackState.status !=
                                                        PlaybackStatus.Loading &&
                                                    playbackState.status !=
                                                        PlaybackStatus.Error &&
                                                    playbackState.status !=
                                                        PlaybackStatus.Idle,
                                                modifier =
                                                    Modifier.fillMaxWidth()
                                            )

                                            Text(
                                                "Duration: $duration ms"
                                            )
                                        }
                                }
                            } else {
                                Text("No recording available")
                            }
                        }
                    }

                    if (target.verses.isEmpty()) {
                        Text("No verses available")
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement =
                                Arrangement.spacedBy(12.dp)
                        ) {
                            items(target.verses) { verse ->
                                Column(
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    verse.petgomo?.let { petgomo ->
                                        Text(
                                            text = petgomo.syriac,
                                            modifier = Modifier.fillMaxWidth(),
                                            style = SyriacTextStyles.body()
                                        )
                                    }

                                    Text(
                                        text = verse.text.syriac,
                                        modifier = Modifier.fillMaxWidth(),
                                        style = SyriacTextStyles.body()
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text("Selected liturgical item is not a Qolo.")
                }
            }
        }

        Button(
            onClick = {
                audioService?.stop()
                onBack()
            },
            modifier = Modifier.padding(top = 24.dp)
        ) {
            Text("Back")
        }
    }
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
