package org.syriacplatform.presentation.shhimo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.Alignment
import org.syriacplatform.presentation.theme.LiturgicalColors
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

class ShhimoNavigationState {
    var selectedDay by mutableStateOf<Long?>(null)
    var selectedSequence by mutableStateOf<Long?>(null)
}

/** A Shhima-specific entry screen; the existing Occasion App remains untouched. */
@Composable
fun ShhimoDailyScreen(
    source: PackageSource,
    onOpenLiturgicalItem: (Long) -> Unit,
    navigationState: ShhimoNavigationState = remember { ShhimoNavigationState() },
    modifier: Modifier = Modifier,
    settings: @Composable () -> Unit = {}
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
                    item.locationTitle ?: item.qintoTitle ?: "ܩܳܠܳܐ"
                },

            )
        } catch (e: Exception) {
            ShhimoScreenState.Error(e.message ?: "Unable to load Shhima")
        }
    }

    Column(modifier.fillMaxSize().safeDrawingPadding().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("ܫܚܺܝܡܳܐ", style = LiturgicalTextStyles.title(LiturgicalTitleLevel.Occasion), modifier = Modifier.fillMaxWidth())
        if (navigationState.selectedDay == null) settings()
        when (val current = state) {
            ShhimoScreenState.Loading -> Text("جارٍ تحميل الصلوات…")
            is ShhimoScreenState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
            is ShhimoScreenState.Ready -> {
                val day = current.days.firstOrNull { it.id == navigationState.selectedDay }
                val prayer = day?.prayers?.firstOrNull { it.sequenceId == navigationState.selectedSequence }
                if (day != null) {
                    TextButton(onClick = { if (prayer != null) navigationState.selectedSequence = null else navigationState.selectedDay = null }) {
                        Text("رجوع")
                    }
                }
                when {
                    prayer != null -> {
                        Text(prayer.name, style = LiturgicalTextStyles.title(LiturgicalTitleLevel.Prayer), modifier = Modifier.fillMaxWidth())
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(prayer.itemIds) { id ->
                                Button(
                                    onClick = { onOpenLiturgicalItem(id) },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                ) {
                                    Text(
                                        current.itemTitles[id] ?: "ܩܳܠܳܐ",
                                        style = LiturgicalTextStyles.section(),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                    day != null -> {
                        Text(day.dayName ?: dayLabel(day.id), style = LiturgicalTextStyles.title(LiturgicalTitleLevel.Prayer), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(day.prayers) { entry ->
                                Text(entry.name, style = LiturgicalTextStyles.title(LiturgicalTitleLevel.Prayer), modifier = Modifier.fillMaxWidth()
                                    .clickable { navigationState.selectedSequence = entry.sequenceId }
                                    .padding(12.dp))
                            }
                        }
                    }
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(current.days) { entry ->
                            Text(entry.dayName ?: dayLabel(entry.id), style = LiturgicalTextStyles.title(LiturgicalTitleLevel.Prayer), textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                                .clickable { navigationState.selectedDay = entry.id }
                                .padding(12.dp))
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

