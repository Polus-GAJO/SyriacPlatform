package org.syriacplatform.content.repository

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.EntryPointId
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.GroupId
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.EntryPointTarget
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.runtime.RuntimeEntryPoint
import org.syriacplatform.content.runtime.RuntimeOccasion
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.content.runtime.ResolvedLiturgicalItem

class FakeContentRepository : ContentRepository {

    private val qolos = listOf(
        Qolo(
            id = QoloId(1),
            groupId = GroupId(1),
            sort = 1,
            name = "ܩܳܠܳܐ ܢܽܘܗܪܳܢܳܐ",
            searchName = "ܩܠܐ ܢܘܗܪܢܐ",
            poeticMeter = null
        )
    )

    private val occasion =
        Occasion(
            id = OccasionId(1),
            name = "Test Occasion",
            description = null,
            prayerSequenceIds = emptyList()
        )

    private val runtimeOccasion =
        RuntimeOccasion(
            occasion = occasion,
            prayerSequences = emptyList()
        )

    private val defaultEntryPoint =
        RuntimeEntryPoint(
            entryPoint =
                EntryPoint(
                    id = EntryPointId(1),
                    name = "Default Entry Point",
                    target =
                        EntryPointTarget.Occasion(
                            occasionId = OccasionId(1)
                        ),
                    isDefault = true
                ),
            occasion = runtimeOccasion
        )

    override suspend fun loadQolo(
        id: QoloId
    ): Result<Qolo> {
        val qolo =
            qolos.firstOrNull { item ->
                item.id == id
            }

        return if (qolo != null) {
            Result.Success(qolo)
        } else {
            Result.Failure(
                PlatformError(
                    code = ErrorCode.CONTENT_NOT_FOUND,
                    message =
                        "Qolo was not found: ${id.value}"
                )
            )
        }
    }

    override suspend fun loadAllQolos():
            Result<List<Qolo>> {
        return Result.Success(qolos)
    }

    override suspend fun loadDefaultEntryPoint():
            Result<RuntimeEntryPoint> {
        return Result.Success(
            defaultEntryPoint
        )
    }

    override suspend fun loadOccasion(
        id: OccasionId
    ): Result<RuntimeOccasion> {
        return if (id == occasion.id) {
            Result.Success(
                runtimeOccasion
            )
        } else {
            Result.Failure(
                PlatformError(
                    code = ErrorCode.CONTENT_NOT_FOUND,
                    message =
                        "Occasion was not found: ${id.value}"
                )
            )
        }
    }

    override suspend fun loadEntryPoints():
            Result<List<EntryPoint>> {
        return Result.Success(
            listOf(
                defaultEntryPoint.entryPoint
            )
        )
    }

    override suspend fun loadOccasions():
            Result<List<Occasion>> {
        return Result.Success(
            listOf(
                occasion
            )
        )
    }

    override suspend fun loadLiturgicalItem(
        id: LiturgicalItemId
    ): Result<ResolvedLiturgicalItem> {
        return Result.Failure(
            PlatformError(
                code = ErrorCode.CONTENT_NOT_FOUND,
                message =
                    "LiturgicalItem was not found: ${id.value}"
            )
        )
    }
    override suspend fun loadMediaAsset(
        id: MediaAssetId
    ): Result<MediaAsset> {
        return Result.Failure(
            PlatformError(
                code = ErrorCode.CONTENT_NOT_FOUND,
                message =
                    "MediaAsset was not found: ${id.value}"
            )
        )
    }

    override suspend fun loadMelodyRecordings(
        id: MelodyId
    ): Result<List<MediaAsset>> {
        return Result.Failure(
            PlatformError(
                code = ErrorCode.CONTENT_NOT_FOUND,
                message =
                    "Melody was not found: ${id.value}"
            )
        )
    }
}