package org.syriacplatform.packagevalidation.validators

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.models.PackageManifest
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من اكتمال القيم الأساسية داخل Package Manifest.
 *
 * لا يتحقق من توافق الإصدارات؛ فهذه مسؤولية
 * CompatibilityValidator.
 */
class ManifestValidator :
    PackageValidationRule<PackageManifest> {

    override fun validate(
        value: PackageManifest
    ): List<ValidationIssue> {
        return buildList {
            requireNotBlank(
                value = value.packageId,
                location = "manifest.packageId"
            )

            requireNotBlank(
                value = value.packageName,
                location = "manifest.packageName"
            )

            requireNotBlank(
                value = value.schemaVersion,
                location = "manifest.schemaVersion"
            )

            requireNotBlank(
                value = value.packageVersion,
                location = "manifest.packageVersion"
            )

            requireNotBlank(
                value = value.contentVersion,
                location = "manifest.contentVersion"
            )

            requireNotBlank(
                value = value.application.id,
                location = "manifest.application.id"
            )

            requireNotBlank(
                value = value.application.name,
                location = "manifest.application.name"
            )

            requireNotBlank(
                value = value.application.platform,
                location = "manifest.application.platform"
            )

            requireNotBlank(
                value = value.application.defaultLanguage,
                location = "manifest.application.defaultLanguage"
            )

            requireNotBlank(
                value = value.build.generatedAt,
                location = "manifest.build.generatedAt"
            )

            requireNotBlank(
                value = value.build.buildTool,
                location = "manifest.build.buildTool"
            )

            requireNotBlank(
                value = value.build.buildVersion,
                location = "manifest.build.buildVersion"
            )

            requireNotBlank(
                value = value.build.buildRevision,
                location = "manifest.build.buildRevision"
            )

            requireNotBlank(
                value = value.compatibility.minimumCoreVersion,
                location = "manifest.compatibility.minimumCoreVersion"
            )

            requireNotBlank(
                value = value.compatibility.targetSchemaVersion,
                location = "manifest.compatibility.targetSchemaVersion"
            )

            validateSupportedFeatures(
                features = value.compatibility.supportedFeatures
            )
        }
    }

    private fun MutableList<ValidationIssue>.requireNotBlank(
        value: String,
        location: String
    ) {
        if (value.isBlank()) {
            add(
                ValidationIssue(
                    severity = ValidationSeverity.FATAL,
                    code = ErrorCode.MISSING_REQUIRED_FIELD,
                    message = "Required manifest value is missing.",
                    location = location
                )
            )
        }
    }
}

private fun MutableList<ValidationIssue>.validateSupportedFeatures(
    features: List<String>
) {
    features.forEachIndexed { index, feature ->
        if (feature.isBlank()) {
            add(
                ValidationIssue(
                    severity = ValidationSeverity.FATAL,
                    code = ErrorCode.INVALID_PACKAGE_DATA,
                    message = "Supported feature must not be blank.",
                    location =
                        "manifest.compatibility.supportedFeatures[$index]"
                )
            )
        }
    }

    features
        .map { feature -> feature.trim() }
        .groupingBy { feature -> feature }
        .eachCount()
        .filterValues { count -> count > 1 }
        .keys
        .forEach { feature ->
            add(
                ValidationIssue(
                    severity = ValidationSeverity.FATAL,
                    code = ErrorCode.INVALID_PACKAGE_DATA,
                    message =
                        "Supported feature is declared more than once: $feature",
                    location =
                        "manifest.compatibility.supportedFeatures"
                )
            )
        }
}