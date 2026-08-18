package org.syriacplatform.buildtools.schema

import org.syriacplatform.buildtools.source.AuthorSourceData

class SchemaV1NavigationMapper {

    fun map(
        source: AuthorSourceData,
        composition: SchemaV1CompositionDraft
    ): SchemaV1NavigationContent {
        require(
            composition.occasionId == source.occasion.id
        ) {
            "Composition Occasion ${composition.occasionId} " +
                    "does not match source Occasion " +
                    "${source.occasion.id}."
        }

        require(
            !composition.hasBlockingDiagnostics
        ) {
            "Schema v1 navigation cannot be emitted while " +
                    "the Occasion composition contains " +
                    "${composition.diagnostics.size} " +
                    "package-blocking diagnostics."
        }

        val prayerSequences =
            composition.prayers.map { prayer ->
                val sequenceId =
                    ProjectionIdFactory.prayerSequenceId(
                        occasionId = source.occasion.id,
                        prayerId = prayer.prayerId
                    )

                SchemaV1PrayerSequence(
                    id = sequenceId,
                    prayerId = prayer.prayerId,
                    liturgicalItemIds =
                        prayer.orderedSourceItemIds
                )
            }

        val occasionName =
            requireNotNull(source.occasion.name) {
                "Occasion ${source.occasion.id} has no name."
            }

        val occasion =
            SchemaV1Occasion(
                id = source.occasion.id,
                name = occasionName,
                prayerSequenceIds =
                    prayerSequences.map { it.id }
            )

        val entryPoint =
            SchemaV1EntryPoint(
                id =
                    ProjectionIdFactory
                        .entryPointIdForOccasion(
                            source.occasion.id
                        ),
                name = occasionName,
                occasionId = source.occasion.id,
                isDefault = true
            )

        return SchemaV1NavigationContent(
            entryPoints = listOf(entryPoint),
            occasions = listOf(occasion),
            prayerSequences = prayerSequences
        )
    }
}