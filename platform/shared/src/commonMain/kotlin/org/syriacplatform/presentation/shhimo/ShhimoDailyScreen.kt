package org.syriacplatform.presentation.shhimo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import org.syriacplatform.presentation.theme.LiturgicalFontFamilies
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
import org.syriacplatform.content.runtime.LiturgicalPerformanceResolver
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.content.models.MediaAsset

/** A Shhima-specific entry screen; the existing Occasion App remains untouched. */
@Composable
fun ShhimoDailyScreen(
    source: PackageSource,
    onOpenLiturgicalItem: (Long) -> Unit,
    onPlayRecording: (MediaAsset) -> Unit = {},
    externalItemDetails: Boolean = false,
    modifier: Modifier = Modifier
) {
    var state by remember(source) { mutableStateOf<ShhimoScreenState>(ShhimoScreenState.Loading) }
    var selectedDay by remember(source) { mutableStateOf<Long?>(null) }
    var selectedSequence by remember(source) { mutableStateOf<Long?>(null) }
    var selectedItem by remember(source) { mutableStateOf<Long?>(null) }

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
            val performanceResolver = LiturgicalPerformanceResolver(store)
            val sequences = content.prayerSequences.associateBy { it.id.value }
            val prayers = content.prayers.associateBy { it.id.value }
            val items = content.liturgicalItems.associateBy { it.id.value }
            require(navigation.days.flatMap { it.prayers }.all {
                val sequence = sequences[it.sequenceId]
                sequence != null && sequence.prayerId.value == it.prayerId &&
                    it.prayerId in prayers && sequence.liturgicalItemIds.all { id -> id.value in items }
            }) { "Shhimo navigation references missing or mismatched content" }
            val texts = content.texts.associateBy { it.id.value }
            val petgomos = content.petgomos.associateBy { it.id.value }
            ShhimoScreenState.Ready(
                navigation.days.map { day ->
                    ShhimoDayUi(day.dayId, day.prayers.map { prayer ->
                        val sequence = requireNotNull(sequences[prayer.sequenceId])
                        ShhimoPrayerUi(
                            prayer.prayerId,
                            prayer.sequenceId,
                            requireNotNull(prayers[prayer.prayerId]).name,
                            sequence.liturgicalItemIds.map { it.value }
                        )
                    })
                },
                items.mapValues { (_, item) ->
                    item.locationTitle ?: item.qintoTitle ?: "ܩܳܠܳܐ"
                },
                items.mapValues { (_, item) ->
                    val occurrences = when (val target = item.target) {
                        is LiturgicalItemTarget.Text -> listOf(target.textId.value to target.petgomoId?.value)
                        is LiturgicalItemTarget.NonMelodicText -> target.verses.map { it.textId.value to it.petgomoId?.value }
                        is LiturgicalItemTarget.Qolo -> target.verses.map { it.textId.value to it.petgomoId?.value }
                        is LiturgicalItemTarget.UnresolvedQolo -> target.verses.map { it.textId.value to it.petgomoId?.value }
                    }
                    occurrences.mapNotNull { (textId, petgomoId) ->
                        texts[textId]?.let { verse ->
                            ShhimoVerseUi(verse.syriac, petgomoId?.let { petgomos[it]?.syriac })
                        }
                    }
                },
                items.mapValues { (_, item) ->
                    performanceResolver.recordingsFor(item.id).map { recording -> recording.asset }
                }
            )
        } catch (e: Exception) {
            ShhimoScreenState.Error(e.message ?: "Unable to load Shhima")
        }
    }

    val syriacFont = LiturgicalFontFamilies.syriac()
    Column(modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("ܫܚܺܝܡܳܐ", style = LiturgicalTextStyles.title(LiturgicalTitleLevel.Occasion), modifier = Modifier.fillMaxWidth())
        when (val current = state) {
            ShhimoScreenState.Loading -> Text("جارٍ تحميل الصلوات…")
            is ShhimoScreenState.Error -> Text(current.message, color = MaterialTheme.colorScheme.error)
            is ShhimoScreenState.Ready -> {
                val day = current.days.firstOrNull { it.id == selectedDay }
                val prayer = day?.prayers?.firstOrNull { it.sequenceId == selectedSequence }
                if (day != null) {
                    TextButton(onClick = { if (selectedItem != null) selectedItem = null else if (prayer != null) selectedSequence = null else selectedDay = null }) {
                        Text("رجوع")
                    }
                }
                when {
                    prayer != null && selectedItem != null -> {
                        Text(current.itemTitles[selectedItem] ?: "ܩܳܠܳܐ", style = MaterialTheme.typography.titleLarge)
                        val performances = current.itemPerformances[selectedItem].orEmpty()
                        if (performances.isEmpty()) {
                            Text("لا يوجد تسجيل أداء مرفق في الحزمة الحالية",
                                style = MaterialTheme.typography.bodySmall)
                        } else {
                            performances.forEachIndexed { index, asset ->
                                TextButton(onClick = { onPlayRecording(asset) }) {
                                    Text("▶ " + (asset.performer ?: "تسجيل الأداء ${index + 1}"))
                                }
                            }
                        }
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(current.itemVerses[selectedItem].orEmpty()) { verse ->
                                Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                                    verse.petgomo?.let { petgomo ->
                                        Text(petgomo, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                                            fontFamily = syriacFont, textAlign = TextAlign.Right,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                    Text(verse.syriac, modifier = Modifier.fillMaxWidth(), fontFamily = syriacFont,
                                        textAlign = TextAlign.Right, style = MaterialTheme.typography.bodyLarge)
                                }
                            }
                        }
                    }
                    prayer != null -> {
                        Text(prayer.name, style = MaterialTheme.typography.titleLarge)
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(prayer.itemIds) { id ->
                                Text(
                                    current.itemTitles[id] ?: "ܩܳܠܳܐ",
                                    modifier = Modifier.fillMaxWidth()
                                        .clickable { if (!externalItemDetails) selectedItem = id; onOpenLiturgicalItem(id) }
                                        .padding(12.dp)
                                )
                            }
                        }
                    }
                    day != null -> {
                        Text(dayLabel(day.id), style = MaterialTheme.typography.titleLarge)
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(day.prayers) { entry ->
                                Text(entry.name, modifier = Modifier.fillMaxWidth()
                                    .clickable { selectedSequence = entry.sequenceId }
                                    .padding(12.dp))
                            }
                        }
                    }
                    else -> LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(current.days) { entry ->
                            Text(dayLabel(entry.id), modifier = Modifier.fillMaxWidth()
                                .clickable { selectedDay = entry.id }
                                .padding(12.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun dayLabel(id: Long): String = when (id) {
    1L -> "الأحد"
    2L -> "الاثنين"
    3L -> "الثلاثاء"
    4L -> "الأربعاء"
    5L -> "الخميس"
    6L -> "الجمعة"
    7L -> "السبت"
    else -> "اليوم $id"
}

private sealed interface ShhimoScreenState {
    data object Loading : ShhimoScreenState
    data class Error(val message: String) : ShhimoScreenState
    data class Ready(val days: List<ShhimoDayUi>, val itemTitles: Map<Long, String>, val itemVerses: Map<Long, List<ShhimoVerseUi>>, val itemPerformances: Map<Long, List<MediaAsset>>) : ShhimoScreenState
}
private data class ShhimoDayUi(val id: Long, val prayers: List<ShhimoPrayerUi>)
private data class ShhimoPrayerUi(
    val prayerId: Long,
    val sequenceId: Long,
    val name: String,
    val itemIds: List<Long>
)

private data class ShhimoVerseUi(val syriac: String, val petgomo: String?)
