package org.syriacplatform.presentation.shhimo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.stringResource
import org.syriacplatform.resources.Res
import org.syriacplatform.resources.loading
import org.syriacplatform.resources.back
import org.syriacplatform.resources.play_all
import org.syriacplatform.resources.stop_playback
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.syriacplatform.presentation.theme.LiturgicalTextStyles
import org.syriacplatform.presentation.theme.LiturgicalTitleLevel
import androidx.compose.ui.unit.dp
import org.syriacplatform.packageformat.loading.ApplicationPackageLoader
import org.syriacplatform.packageformat.loading.PackageLoadResult
import org.syriacplatform.packageformat.loading.PackageSource
import org.syriacplatform.packageformat.loading.ShhimoDailyNavigationLoader
import org.syriacplatform.packageformat.models.PackageProfile
import org.syriacplatform.packagevalidation.compatibility.CoreCompatibilityDefaults
import org.syriacplatform.content.runtime.RuntimeContentStore
import org.syriacplatform.content.models.LiturgicalItemTarget

class ShhimoNavigationState {
    var selectedDay by mutableStateOf<Long?>(null)
    var selectedSequence by mutableStateOf<Long?>(null)
}

/** A Shhima-specific entry screen; the existing Occasion App remains untouched. */
@Composable
fun ShhimoDailyScreen(
    source: PackageSource,
    onOpenLiturgicalItem: (Long, List<Long>) -> Unit,
    navigationState: ShhimoNavigationState = remember { ShhimoNavigationState() },
    modifier: Modifier = Modifier,
    settings: @Composable () -> Unit = {},
    qintoSelector: @Composable () -> Unit = {},
    queueActive: Boolean = false,
    queuePreparing: Boolean = false,
    onPlayPrayer: (List<Long>) -> Unit = {},
    onStopPrayer: () -> Unit = {},
    queueControls: @Composable () -> Unit = {}
) {
    var state by remember(source) { mutableStateOf<ShhimoScreenState>(ShhimoScreenState.Loading) }


    LaunchedEffect(source) {
        state = try {
            val loaded = ApplicationPackageLoader(source).load(CoreCompatibilityDefaults.CURRENT)
            val success = loaded as? PackageLoadResult.Success
                ?: error(when (loaded) {
                    is PackageLoadResult.ValidationFailed ->
                        loaded.validationReport.issues.joinToString { it.message }
                    is PackageLoadResult.Failure -> loaded.error.toString()
                    else -> "Package loading failed"
                })
            require(success.packageData.manifest.profile == PackageProfile.SHHIMA) {
                "Expected a Shhima package"
            }
            val navigation = ShhimoDailyNavigationLoader(source).load()
            val store = RuntimeContentStore.from(success.packageData)
            val content = store.content
            val sequences = content.prayerSequences.associateBy { it.id.value }
            val prayers = content.prayers.associateBy { it.id.value }
            val items = content.liturgicalItems.associateBy { it.id.value }
            val qoloNames = content.qolos.associate { it.id to it.name }
            require(navigation.days.flatMap { it.prayers }.all {
                val sequence = sequences[it.sequenceId]
                sequence != null && sequence.prayerId.value == it.prayerId &&
                    it.prayerId in prayers && sequence.liturgicalItemIds.all { id -> id.value in items }
            }) { "Shhimo navigation references missing or mismatched content" }
            ShhimoScreenState.Ready(
                navigation.days.mapNotNull { day ->
                    val available = day.prayers.mapNotNull { prayer ->
                        val sequence = requireNotNull(sequences[prayer.sequenceId])
                        if (sequence.liturgicalItemIds.isEmpty()) return@mapNotNull null
                        ShhimoPrayerUi(
                            prayer.prayerId,
                            prayer.sequenceId,
                            requireNotNull(prayers[prayer.prayerId]).name,
                            sequence.liturgicalItemIds.map { it.value }
                        )
                    }
                    if (available.isEmpty()) null else ShhimoDayUi(day.dayId, day.dayName, available)
                },
                items.mapValues { (_, item) ->
                    val locationTitle = item.locationTitle?.takeIf { it.isNotBlank() }
                    val qoloTitle = (item.target as? LiturgicalItemTarget.Qolo)
                        ?.qoloId?.let(qoloNames::get)?.takeIf { it.isNotBlank() }
                    listOfNotNull(locationTitle, qoloTitle)
                        .joinToString(": ")
                        .ifBlank { "ܩܳܠܳܐ" }
                },

            )
        } catch (e: Exception) {
            ShhimoScreenState.Error(e.message ?: "Unable to load Shhima")
        }
    }

    Column(
        modifier.fillMaxSize().safeDrawingPadding().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val ready = state as? ShhimoScreenState.Ready
        val day = ready?.days?.firstOrNull { it.id == navigationState.selectedDay }
        val prayer = day?.prayers?.firstOrNull { it.sequenceId == navigationState.selectedSequence }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                if (day != null) {
                    TextButton(onClick = {
                        if (prayer != null) {
                            onStopPrayer()
                            navigationState.selectedSequence = null
                        }
                        else navigationState.selectedDay = null
                    }) {
                        Text(stringResource(Res.string.back))
                    }
                }
            }
            Text(
                text = if (day == null) "ܫܚܺܝܡܳܐ"
                    else if (prayer != null) prayer.name else day.dayName ?: dayLabel(day.id),
                style = LiturgicalTextStyles.title(
                    if (day == null) LiturgicalTitleLevel.Occasion else LiturgicalTitleLevel.Prayer
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )
            Box(Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                qintoSelector()
            }
        }
        if (day == null) settings()

        HorizontalDivider(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        when (val current = state) {
            ShhimoScreenState.Loading -> Text(stringResource(Res.string.loading))
            is ShhimoScreenState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
            is ShhimoScreenState.Ready -> when {
                prayer != null -> {
                    TextButton(
                        onClick = { if (queueActive || queuePreparing) onStopPrayer() else onPlayPrayer(prayer.itemIds) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(if (queueActive || queuePreparing) Res.string.stop_playback else Res.string.play_all))
                    }
                    queueControls()
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(prayer.itemIds) { id ->
                            Text(
                                text = current.itemTitles[id] ?: "ܩܳܠܳܐ",
                                style = LiturgicalTextStyles.section(),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                                    .clickable {
                                        onOpenLiturgicalItem(id, prayer.itemIds)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
                day != null -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(day.prayers) { entry ->
                            Text(
                                entry.name,
                                style = LiturgicalTextStyles.title(LiturgicalTitleLevel.Prayer),
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { navigationState.selectedSequence = entry.sequenceId }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(current.days) { entry ->
                            Text(
                                entry.dayName ?: dayLabel(entry.id),
                                style = LiturgicalTextStyles.title(LiturgicalTitleLevel.Prayer),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                                    .clickable { navigationState.selectedDay = entry.id }
                                    .padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun dayLabel(id: Long): String = when (id) {
    1L -> "ܚܰܕ ܒܫܰܒܳܐ"
    2L -> "ܬܪܶܝܢ ܒܫܰܒܳܐ"
    3L -> "ܬܠܳܬܳܐ ܒܫܰܒܳܐ"
    4L -> "ܐܰܪܒܥܳܐ ܒܫܰܒܳܐ"
    5L -> "ܚܰܡܫܳܐ ܒܫܰܒܳܐ"
    6L -> "ܥܪܽܘܒܬܳܐ"
    7L -> "ܫܰܒܬܳܐ"
    else -> "اليوم $id"
}

private sealed interface ShhimoScreenState {
    data object Loading : ShhimoScreenState
    data class Error(val message: String) : ShhimoScreenState
    data class Ready(val days: List<ShhimoDayUi>, val itemTitles: Map<Long, String>) : ShhimoScreenState
}
private data class ShhimoDayUi(val id: Long, val dayName: String?, val prayers: List<ShhimoPrayerUi>)
private data class ShhimoPrayerUi(
    val prayerId: Long,
    val sequenceId: Long,
    val name: String,
    val itemIds: List<Long>
)

