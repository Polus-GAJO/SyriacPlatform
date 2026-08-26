package org.syriacplatform.content.repository

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.packageformat.loading.ApplicationPackageLoader
import org.syriacplatform.packageformat.loading.PackageLoadResult
import org.syriacplatform.packagevalidation.compatibility.CoreCompatibility
import org.syriacplatform.content.runtime.RuntimeContentStore
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.content.runtime.RuntimeContentResolver
import org.syriacplatform.content.runtime.RuntimeEntryPoint
import org.syriacplatform.content.runtime.RuntimeOccasion
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.content.runtime.ResolvedLiturgicalItem

/**
 * ContentRepository يعتمد على Application Package الكاملة
 * بدل قراءة Collection منفردة مباشرة من JSON.
 *
 * الحزمة تُحمّل وتُتحقق مرة واحدة ثم تُستخدم
 * لجميع عمليات قراءة المحتوى اللاحقة.
 */
class ApplicationPackageContentRepository(
    private val loader: ApplicationPackageLoader,
    private val coreCompatibility: CoreCompatibility
) : ContentRepository {

    private var cachedStore:
            RuntimeContentStore? = null

    override suspend fun loadQolo(
        id: QoloId
    ): Result<Qolo> {
        return when (
            val storeResult =
                loadStore()
        ) {
            is Result.Success -> {
                val qolo =
                    storeResult.data
                        .index
                        .qolosById[id]

                if (qolo != null) {
                    Result.Success(qolo)
                } else {
                    Result.Failure(
                        PlatformError(
                            code =
                                ErrorCode.CONTENT_NOT_FOUND,
                            message =
                                "Qolo was not found: ${id.value}"
                        )
                    )
                }
            }

            is Result.Failure ->
                storeResult
        }
    }

    override suspend fun loadAllQolos():
            Result<List<Qolo>> {

        return when (
            val storeResult =
                loadStore()
        ) {
            is Result.Success ->
                Result.Success(
                    storeResult.data
                        .content
                        .qolos
                )

            is Result.Failure ->
                storeResult
        }
    }

    private suspend fun loadStore():
            Result<RuntimeContentStore> {

        cachedStore?.let { store ->
            return Result.Success(store)
        }

        return when (
            val result =
                loader.load(
                    coreCompatibility =
                        coreCompatibility
                )
        ) {
            is PackageLoadResult.Success -> {
                val store =
                    RuntimeContentStore.from(
                        result.packageData
                    )

                cachedStore = store

                Result.Success(store)
            }

            is PackageLoadResult.ValidationFailed -> {
                Result.Failure(
                    PlatformError(
                        code =
                            ErrorCode.PACKAGE_STRUCTURE_INVALID,
                        message =
                            "Application package failed validation: " +
                                    "${result.validationReport.fatalIssues.size} " +
                                    "fatal issue(s)."
                    )
                )
            }

            is PackageLoadResult.Failure ->
                Result.Failure(
                    result.error
                )
        }
    }

    override suspend fun loadDefaultEntryPoint():
            Result<RuntimeEntryPoint> {

        return when (
            val storeResult =
                loadStore()
        ) {
            is Result.Success -> {
                val resolver =
                    RuntimeContentResolver(
                        store = storeResult.data
                    )

                resolver.resolveDefaultEntryPoint()
            }

            is Result.Failure ->
                storeResult
        }
    }

    override suspend fun loadOccasion(
        id: OccasionId
    ): Result<RuntimeOccasion> {

        return when (
            val storeResult =
                loadStore()
        ) {
            is Result.Success -> {
                val resolver =
                    RuntimeContentResolver(
                        store = storeResult.data
                    )

                resolver.resolveOccasion(id)
            }

            is Result.Failure ->
                storeResult
        }
    }

    override suspend fun loadEntryPoints():
            Result<List<EntryPoint>> {

        return when (
            val storeResult =
                loadStore()
        ) {
            is Result.Success ->
                Result.Success(
                    storeResult.data
                        .content
                        .entryPoints
                )

            is Result.Failure ->
                storeResult
        }
    }

    override suspend fun loadOccasions():
            Result<List<Occasion>> {

        return when (
            val storeResult =
                loadStore()
        ) {
            is Result.Success ->
                Result.Success(
                    storeResult.data
                        .content
                        .occasions
                )

            is Result.Failure ->
                storeResult
        }
    }

    override suspend fun loadLiturgicalItem(
        id: LiturgicalItemId
    ): Result<ResolvedLiturgicalItem> {

        return when (
            val storeResult = loadStore()
        ) {
            is Result.Success -> {
                val resolver =
                    RuntimeContentResolver(
                        store = storeResult.data
                    )

                resolver.resolveLiturgicalItem(id)
            }

            is Result.Failure ->
                storeResult
        }
    }
    override suspend fun loadMediaAsset(
        id: MediaAssetId
    ): Result<MediaAsset> {
        return when (
            val storeResult =
                loadStore()
        ) {
            is Result.Success -> {
                RuntimeContentResolver(
                    store = storeResult.data
                ).resolveMediaAsset(id)
            }

            is Result.Failure ->
                storeResult
        }
    }

    override suspend fun loadMelodyRecordings(
        id: MelodyId
    ): Result<List<MediaAsset>> {
        return when (
            val storeResult =
                loadStore()
        ) {
            is Result.Success -> {
                RuntimeContentResolver(
                    store = storeResult.data
                ).resolveMelodyRecordings(id)
            }

            is Result.Failure ->
                storeResult
        }
    }
}