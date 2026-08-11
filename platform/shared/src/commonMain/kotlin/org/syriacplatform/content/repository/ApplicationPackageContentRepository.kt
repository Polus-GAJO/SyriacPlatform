package org.syriacplatform.content.repository

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.packageformat.loading.ApplicationPackageLoader
import org.syriacplatform.packageformat.loading.PackageLoadResult
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.compatibility.CoreCompatibility

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

    private var cachedPackage:
            ParsedApplicationPackage? = null

    override suspend fun loadQolo(
        id: QoloId
    ): Result<Qolo> {
        return when (
            val packageResult =
                loadPackage()
        ) {
            is Result.Success -> {
                val qolo =
                    packageResult.data.qolos
                        .firstOrNull { item ->
                            item.id == id
                        }

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
                packageResult
        }
    }

    override suspend fun loadAllQolos():
            Result<List<Qolo>> {

        return when (
            val packageResult =
                loadPackage()
        ) {
            is Result.Success ->
                Result.Success(
                    packageResult.data.qolos
                )

            is Result.Failure ->
                packageResult
        }
    }

    private suspend fun loadPackage():
            Result<ParsedApplicationPackage> {

        cachedPackage?.let { packageData ->
            return Result.Success(
                packageData
            )
        }

        return when (
            val result =
                loader.load(
                    coreCompatibility =
                        coreCompatibility
                )
        ) {
            is PackageLoadResult.Success -> {
                cachedPackage =
                    result.packageData

                Result.Success(
                    result.packageData
                )
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
}