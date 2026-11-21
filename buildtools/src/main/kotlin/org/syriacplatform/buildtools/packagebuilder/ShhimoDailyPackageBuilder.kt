package org.syriacplatform.buildtools.packagebuilder

import org.syriacplatform.buildtools.schema.BookPrayerPackageSliceBuilder
import org.syriacplatform.buildtools.schema.SchemaV1CanonicalMapper
import org.syriacplatform.buildtools.schema.SchemaV1CanonicalMedia
import org.syriacplatform.buildtools.schema.SchemaV1PackageMediaSelector
import org.syriacplatform.buildtools.schema.SchemaV1QoloLiturgicalItem
import org.syriacplatform.buildtools.schema.SchemaV1CompositionDraft
import org.syriacplatform.buildtools.schema.SchemaV1NavigationContent
import org.syriacplatform.buildtools.schema.SchemaV1PrayerCompositionDraft
import org.syriacplatform.buildtools.schema.ShhimoDailySequencePlanner
import org.syriacplatform.buildtools.source.AuthorSourceData

/**
 * Produces the full seven-day Shhimo content, reusing Schema-v1 canonical
 * content and item serialization without publishing Occasion navigation.
 * The adapter's internal occasionId is solely for the legacy assembler.
 */
class ShhimoDailyPackageBuilder(
    private val planner: ShhimoDailySequencePlanner = ShhimoDailySequencePlanner(),
    private val slices: BookPrayerPackageSliceBuilder = BookPrayerPackageSliceBuilder(),
    private val canonicalMapper: SchemaV1CanonicalMapper = SchemaV1CanonicalMapper(),
    private val assembler: SchemaV1PreviewPackageAssembler = SchemaV1PreviewPackageAssembler(),
    private val mediaSelector: SchemaV1PackageMediaSelector = SchemaV1PackageMediaSelector()
) {
    fun build(source: AuthorSourceData, bookId: Long, media: SchemaV1CanonicalMedia = SchemaV1CanonicalMedia(emptyList(), emptyList())): ShhimoDailyPackage {
        require(bookId > 0L)
        val plan = planner.plan(source.existsIn, bookId)
        require(plan.isNotEmpty()) { "No daily prayers in book $bookId." }
        val slicesByPrayer = plan.map { entry ->
            entry to slices.build(
                source, bookId, entry.dayId, entry.prayerId, entry.sequenceId
            )
        }
        require(slicesByPrayer.all { (entry, slice) ->
            slice.liturgicalItems.size == entry.itemCount
        }) { "Daily prayer item count mismatch." }
        val sequences = slicesByPrayer.map { it.second.sequence }
        val items = slicesByPrayer.flatMap { it.second.liturgicalItems }
        require(items.map { it.id }.distinct().size == items.size) {
            "Duplicate liturgical item across daily prayers."
        }
        val melodyIds = items.filterIsInstance<SchemaV1QoloLiturgicalItem>()
            .flatMap { item -> listOfNotNull(item.effectiveMelodyId) + item.melodyCandidateIds }
            .toSet()
        val selectedMedia = mediaSelector.select(media, melodyIds, items)
        val internalOccasionId = 1L
        val config = OccasionPackageBuildConfig.developmentPreview(internalOccasionId).copy(
            packageId = "org.syriacplatform.shhimo.book$bookId",
            packageName = "Shhimo daily prayers",
            contentVersion = "shhimo-book-$bookId-daily-preview"
        )
        val assembled = assembler.assemble(
            canonical = canonicalMapper.map(
                source,
                selectedMedia
            ),
            composition = SchemaV1CompositionDraft(
                occasionId = internalOccasionId,
                prayers = slicesByPrayer.map { (entry, slice) ->
                    SchemaV1PrayerCompositionDraft(
                        prayerId = entry.prayerId,
                        orderedSourceItemIds = slice.sequence.liturgicalItemIds,
                        resolvedItems = slice.liturgicalItems,
                        blockedItemIds = emptyList()
                    )
                },
                diagnostics = emptyList()
            ),
            navigation = SchemaV1NavigationContent(
                entryPoints = emptyList(),
                occasions = emptyList(),
                prayerSequences = sequences
            ),
            config = config,
            media = selectedMedia
        )
        return ShhimoDailyPackage(
            content = assembled.copy(
                manifest = assembled.manifest.copy(
                    profile = "Shhima",
                    supportedFeatures = listOf("book-daily-navigation")
                )
            ),
            days = plan.groupBy { it.dayId }.toSortedMap().map { (dayId, prayers) ->
                ShhimoDailyPackageDay(
                    dayId,
                    prayers.map { ShhimoDailyPackagePrayer(it.prayerId, it.sequenceId) }
                )
            }
        )
    }
}

data class ShhimoDailyPackage(
    val content: SchemaV1PreviewPackage,
    val days: List<ShhimoDailyPackageDay>
)
data class ShhimoDailyPackageDay(
    val dayId: Long,
    val prayers: List<ShhimoDailyPackagePrayer>
)
data class ShhimoDailyPackagePrayer(
    val prayerId: Long,
    val sequenceId: Long
)
