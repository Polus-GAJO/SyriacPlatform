package org.syriacplatform.buildtools.schema

import org.syriacplatform.buildtools.source.AuthorSourceData
import org.syriacplatform.buildtools.source.models.ExistsInSource
import org.syriacplatform.buildtools.source.models.ExistsInTextSource

class SchemaV1CompositionMapper(
    private val melodyResolver: MelodyResolver =
        MelodyResolver()
) {

    fun map(
        source: AuthorSourceData
    ): SchemaV1CompositionDraft {
        val diagnostics =
            mutableListOf<CompositionDiagnostic>()

        val occasionExistsInIds =
            source.occasionLinks
                .filter {
                    it.occasionId == source.occasion.id
                }
                .mapNotNull {
                    it.existsInId
                }
                .toSet()

        val selectedItems =
            source.existsIn.filter {
                it.id in occasionExistsInIds
            }

        val itemsByPrayer =
            selectedItems.groupBy {
                it.prayerId
            }

        val prayers =
            source.prayers
                .sortedBy { it.id }
                .map { prayer ->
                    val prayerItems =
                        itemsByPrayer[prayer.id]
                            .orEmpty()
                            .sortedWith(
                                compareBy<ExistsInSource>(
                                    { it.sort ?: Long.MAX_VALUE },
                                    { it.id }
                                )
                            )

                    val resolved =
                        mutableListOf<
                                SchemaV1QoloLiturgicalItem
                                >()

                    val blocked =
                        mutableListOf<Long>()

                    prayerItems.forEach { item ->
                        when (
                            val resolution =
                                melodyResolver.resolve(
                                    item = item,
                                    melodies = source.melodies
                                )
                        ) {
                            is MelodyResolution.Resolved -> {
                                resolved +=
                                    mapQoloItem(
                                        source = source,
                                        item = item,
                                        effectiveMelodyId =
                                            resolution.melodyId,
                                        melodyCandidateIds =
                                            emptyList()
                                    )
                            }

                            is MelodyResolution.UnresolvedQinto -> {
                                /*
                                 * وجود Qolo في الصلاة مستقل عن حسم Melody.
                                 *
                                 * Qinto = null أو 0 لا يسمح باختراع Melody،
                                 * لكنه لم يعد يمنع إصدار Qolo occurrence.
                                 */
                                resolved +=
                                    mapQoloItem(
                                        source = source,
                                        item = item,
                                        effectiveMelodyId = null,
                                        melodyCandidateIds =
                                            emptyList()
                                    )
                            }

                            is MelodyResolution.NoCandidate -> {
                                /*
                                 * لدينا Qolo occurrence قانونية، لكن لا توجد
                                 * Melody مطابقة للقينة المحددة حاليًا.
                                 *
                                 * لا نخترع Melody ولا نحذف Qolo.
                                 */
                                resolved +=
                                    mapQoloItem(
                                        source = source,
                                        item = item,
                                        effectiveMelodyId = null,
                                        melodyCandidateIds =
                                            emptyList()
                                    )
                            }

                            is MelodyResolution.Ambiguous -> {
                                /*
                                 * نحافظ على جميع المرشحين الحقيقية،
                                 * ولا نختار أول Melody أو أقل ID.
                                 */
                                resolved +=
                                    mapQoloItem(
                                        source = source,
                                        item = item,
                                        effectiveMelodyId = null,
                                        melodyCandidateIds =
                                            resolution.melodyIds
                                    )
                            }
                        }
                    }

                    SchemaV1PrayerCompositionDraft(
                        prayerId = prayer.id,

                        orderedSourceItemIds =
                            prayerItems.map {
                                it.id
                            },

                        resolvedItems = resolved,

                        blockedItemIds = blocked
                    )
                }

        return SchemaV1CompositionDraft(
            occasionId = source.occasion.id,
            prayers = prayers,
            diagnostics = diagnostics
        )
    }

    private fun mapQoloItem(
        source: AuthorSourceData,
        item: ExistsInSource,
        effectiveMelodyId: Long?,
        melodyCandidateIds: List<Long>
    ): SchemaV1QoloLiturgicalItem {
        val qoloId = requireNotNull(
            item.qoloId
        ) {
            "ExistsIn ${item.id} has no QoloN."
        }

        return SchemaV1QoloLiturgicalItem(
            id = item.id,
            qoloId = qoloId,
            effectiveMelodyId =
                effectiveMelodyId,

            melodyCandidateIds =
                melodyCandidateIds,
            verses = mapVerses(
                source = source,
                existsInId = item.id
            )
        )
    }

    private fun mapVerses(
        source: AuthorSourceData,
        existsInId: Long
    ): List<SchemaV1LiturgicalTextRef> {
        val petExisByOccurrence =
            source.petExis
                .groupBy {
                    it.existsInTextId
                }

        return source.existsInTexts
            .filter {
                it.existsInId == existsInId
            }
            .sortedWith(
                compareBy<ExistsInTextSource>(
                    {
                        it.sortInPrayer
                            ?: Int.MAX_VALUE
                    },
                    { it.id }
                )
            )
            .map { occurrence ->
                val textId =
                    requireNotNull(
                        occurrence.textId
                    ) {
                        "ExistsInText ${occurrence.id} " +
                                "has no TextID."
                    }

                val petgomoAssignments =
                    petExisByOccurrence[
                        occurrence.id
                    ].orEmpty()

                require(
                    petgomoAssignments.size <= 1
                ) {
                    "ExistsInText ${occurrence.id} has " +
                            "${petgomoAssignments.size} Petgomo " +
                            "assignments; Schema v1 supports at " +
                            "most one contextual petgomoId per " +
                            "verse occurrence."
                }

                SchemaV1LiturgicalTextRef(
                    textId = textId,

                    petgomoId =
                        petgomoAssignments
                            .singleOrNull()
                            ?.petgomoId
                )
            }
    }
}