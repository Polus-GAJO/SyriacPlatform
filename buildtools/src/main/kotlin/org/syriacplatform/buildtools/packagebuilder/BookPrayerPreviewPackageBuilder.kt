package org.syriacplatform.buildtools.packagebuilder

import org.syriacplatform.buildtools.schema.BookPrayerPackageSliceBuilder
import org.syriacplatform.buildtools.schema.SchemaV1CanonicalMapper
import org.syriacplatform.buildtools.schema.SchemaV1EntryPoint
import org.syriacplatform.buildtools.schema.SchemaV1Occasion
import org.syriacplatform.buildtools.source.AuthorSourceData

/**
 * Development-only bridge: emits one selected book/day/prayer using the
 * existing Schema-v1 package layout. The Occasion navigation wrapper is
 * temporary; it must not be mistaken for the final Shhimo day navigation.
 */
class BookPrayerPreviewPackageBuilder(
    private val sliceBuilder: BookPrayerPackageSliceBuilder = BookPrayerPackageSliceBuilder(),
    private val canonicalMapper: SchemaV1CanonicalMapper = SchemaV1CanonicalMapper(),
    private val assembler: SchemaV1PreviewPackageAssembler = SchemaV1PreviewPackageAssembler()
) {
    fun build(
        source: AuthorSourceData,
        bookId: Long,
        dayId: Long,
        prayerId: Long,
        sequenceId: Long,
        previewOccasionId: Long,
        previewEntryPointId: Long,
        config: OccasionPackageBuildConfig
    ): SchemaV1PreviewPackage {
        require(previewOccasionId > 0 && previewEntryPointId > 0)
        require(config.occasionId == previewOccasionId)
        val slice = sliceBuilder.build(source, bookId, dayId, prayerId, sequenceId)
        require(slice.liturgicalItems.isNotEmpty()) {
            "Selected book/day/prayer has no publishable liturgical items."
        }

        val navigation = org.syriacplatform.buildtools.schema.SchemaV1NavigationContent(
            entryPoints = listOf(
                SchemaV1EntryPoint(
                    id = previewEntryPointId,
                    name = "Book $bookId / Day $dayId / Prayer $prayerId",
                    occasionId = previewOccasionId,
                    isDefault = true
                )
            ),
            occasions = listOf(
                SchemaV1Occasion(
                    id = previewOccasionId,
                    name = "Book $bookId / Day $dayId",
                    prayerSequenceIds = listOf(sequenceId)
                )
            ),
            prayerSequences = listOf(slice.sequence)
        )
        val composition = org.syriacplatform.buildtools.schema.SchemaV1CompositionDraft(
            occasionId = previewOccasionId,
            prayers = listOf(
                org.syriacplatform.buildtools.schema.SchemaV1PrayerCompositionDraft(
                    prayerId = prayerId,
                    orderedSourceItemIds = slice.sequence.liturgicalItemIds,
                    resolvedItems = slice.liturgicalItems,
                    blockedItemIds = emptyList()
                )
            ),
            diagnostics = emptyList()
        )
        return assembler.assemble(
            canonical = canonicalMapper.map(source),
            composition = composition,
            navigation = navigation,
            config = config
        )
    }
}
