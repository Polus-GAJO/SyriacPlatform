package org.syriacplatform.packageformat.mappers

import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.packageformat.dto.PackageManifestJsonDto
import org.syriacplatform.packageformat.models.ApplicationInfo
import org.syriacplatform.packageformat.models.BuildInfo
import org.syriacplatform.packageformat.models.CompatibilityInfo
import org.syriacplatform.packageformat.models.PackageManifest
import org.syriacplatform.packageformat.models.PackageProfile

/**
 * يحول التمثيل الفيزيائي للـ JSON إلى نموذج Package Manifest القانوني.
 */
internal fun PackageManifestJsonDto.toDomain(): Result<PackageManifest> {
    val resolvedProfile = when (
        profile.trim().lowercase()
    ) {
        "occasion" ->
            PackageProfile.OCCASION

        "shhima" ->
            PackageProfile.SHHIMA

        "fulllibrary",
        "full-library",
        "full_library" ->
            PackageProfile.FULL_LIBRARY

        else -> {
            return Result.Failure(
                PlatformError(
                    code = ErrorCode.UNSUPPORTED_PACKAGE_PROFILE,
                    message = "Unsupported package profile: $profile"
                )
            )
        }
    }

    return Result.Success(
        PackageManifest(
            packageId = packageId,
            packageName = packageName,
            schemaVersion = schemaVersion,
            packageVersion = packageVersion,
            contentVersion = contentVersion,
            application = ApplicationInfo(
                id = application.id,
                name = application.name,
                platform = application.platform,
                defaultLanguage = application.defaultLanguage
            ),
            profile = resolvedProfile,
            build = BuildInfo(
                generatedAt = build.generatedAt,
                buildTool = build.buildTool,
                buildVersion = build.buildVersion,
                buildRevision = build.buildRevision
            ),
            compatibility = CompatibilityInfo(
                minimumCoreVersion =
                    compatibility.minimumCoreVersion,
                targetSchemaVersion =
                    compatibility.targetSchemaVersion,
                supportedFeatures =
                    compatibility.supportedFeatures.toList()
            )
        )
    )
}