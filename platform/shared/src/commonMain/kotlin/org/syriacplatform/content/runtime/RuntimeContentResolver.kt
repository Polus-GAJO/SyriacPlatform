package org.syriacplatform.content.runtime

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.EntryPointId
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.common.types.PrayerSequenceId
import org.syriacplatform.content.models.EntryPointTarget
import org.syriacplatform.content.models.LiturgicalItemTarget

/**
 * يحل العلاقات القانونية داخل Runtime اعتمادًا
 * على RuntimeContentIndex المبني بعد Package Validation.
 */
class RuntimeContentResolver(
    private val store: RuntimeContentStore
) {

    fun resolveEntryPoint(
        id: EntryPointId
    ): Result<RuntimeEntryPoint> {
        val entryPoint =
            store.index.entryPointsById[id]
                ?: return notFound(
                    "EntryPoint",
                    id.value
                )

        val target =
            entryPoint.target

        return when (target) {
            is EntryPointTarget.Occasion -> {
                when (
                    val occasionResult =
                        resolveOccasion(
                            target.occasionId
                        )
                ) {
                    is Result.Success ->
                        Result.Success(
                            RuntimeEntryPoint(
                                entryPoint = entryPoint,
                                occasion =
                                    occasionResult.data
                            )
                        )

                    is Result.Failure ->
                        occasionResult
                }
            }
        }
    }

    fun resolveDefaultEntryPoint():
            Result<RuntimeEntryPoint> {

        val entryPoint =
            store.content.entryPoints
                .firstOrNull { item ->
                    item.isDefault
                }
                ?: return Result.Failure(
                    PlatformError(
                        code =
                            ErrorCode.CONTENT_NOT_FOUND,
                        message =
                            "Default EntryPoint was not found."
                    )
                )

        return resolveEntryPoint(
            entryPoint.id
        )
    }

    fun resolveOccasion(
        id: OccasionId
    ): Result<RuntimeOccasion> {
        val occasion =
            store.index.occasionsById[id]
                ?: return notFound(
                    "Occasion",
                    id.value
                )

        val resolvedSequences =
            mutableListOf<RuntimePrayerSequence>()

        occasion.prayerSequenceIds.forEach {
                sequenceId ->

            when (
                val result =
                    resolvePrayerSequence(
                        sequenceId
                    )
            ) {
                is Result.Success ->
                    resolvedSequences.add(
                        result.data
                    )

                is Result.Failure ->
                    return result
            }
        }

        return Result.Success(
            RuntimeOccasion(
                occasion = occasion,
                prayerSequences =
                    resolvedSequences
            )
        )
    }

    fun resolvePrayerSequence(
        id: PrayerSequenceId
    ): Result<RuntimePrayerSequence> {
        val sequence =
            store.index
                .prayerSequencesById[id]
                ?: return notFound(
                    "PrayerSequence",
                    id.value
                )

        val prayer =
            store.index
                .prayersById[
                sequence.prayerId
            ]
                ?: return notFound(
                    "Prayer",
                    sequence.prayerId.value
                )

        val resolvedItems =
            mutableListOf<ResolvedLiturgicalItem>()

        /*
         * map through the original List deliberately.
         * Order and repeated usages are preserved.
         */
        sequence.liturgicalItemIds.forEach {
                itemId ->

            when (
                val result =
                    resolveLiturgicalItem(
                        itemId
                    )
            ) {
                is Result.Success ->
                    resolvedItems.add(
                        result.data
                    )

                is Result.Failure ->
                    return result
            }
        }

        return Result.Success(
            RuntimePrayerSequence(
                sequence = sequence,
                prayer = prayer,
                items = resolvedItems
            )
        )
    }

    fun resolveLiturgicalItem(
        id: LiturgicalItemId
    ): Result<ResolvedLiturgicalItem> {
        val item =
            store.index
                .liturgicalItemsById[id]
                ?: return notFound(
                    "LiturgicalItem",
                    id.value
                )

        return when (
            val target = item.target
        ) {
            is LiturgicalItemTarget.Text -> {
                val text =
                    store.index
                        .textsById[
                        target.textId
                    ]
                        ?: return notFound(
                            "Text",
                            target.textId.value
                        )

                val petgomo =
                    if (target.petgomoId != null) {
                        store.index
                            .petgomosById[
                            target.petgomoId
                        ]
                            ?: return notFound(
                                "Petgomo",
                                target.petgomoId.value
                            )
                    } else {
                        null
                    }

                Result.Success(
                    ResolvedLiturgicalItem(
                        item = item,
                        target =
                            ResolvedLiturgicalItemTarget.Text(
                                text = text,
                                petgomo = petgomo
                            )
                    )
                )
            }

            is LiturgicalItemTarget.Qolo -> {
                val qolo =
                    store.index
                        .qolosById[
                        target.qoloId
                    ]
                        ?: return notFound(
                            "Qolo",
                            target.qoloId.value
                        )

                val melody =
                    store.index
                        .melodiesById[
                        target.effectiveMelodyId
                    ]
                        ?: return notFound(
                            "Melody",
                            target.effectiveMelodyId.value
                        )

                Result.Success(
                    ResolvedLiturgicalItem(
                        item = item,
                        target =
                            ResolvedLiturgicalItemTarget.Qolo(
                                qolo = qolo,
                                effectiveMelody =
                                    melody
                            )
                    )
                )
            }
        }
    }

    private fun <T> notFound(
        entityName: String,
        id: Long
    ): Result<T> {
        return Result.Failure(
            PlatformError(
                code =
                    ErrorCode.CONTENT_NOT_FOUND,
                message =
                    "$entityName was not found: $id"
            )
        )
    }
}