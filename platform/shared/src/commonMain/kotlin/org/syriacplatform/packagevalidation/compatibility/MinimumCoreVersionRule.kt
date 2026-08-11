package org.syriacplatform.packagevalidation.compatibility

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.Version
import org.syriacplatform.packageformat.models.PackageManifest
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من أن إصدار الـ Core الحالي يساوي أو يتجاوز
 * الحد الأدنى المطلوب من الحزمة.
 */
class MinimumCoreVersionRule(
    private val coreCompatibility: CoreCompatibility
) : PackageValidationRule<PackageManifest> {

    override fun validate(
        value: PackageManifest
    ): List<ValidationIssue> {
        val minimumCoreVersion =
            Version.parseOrNull(
                value.compatibility.minimumCoreVersion
            )

        if (minimumCoreVersion == null) {
            return listOf(
                ValidationIssue(
                    severity = ValidationSeverity.FATAL,
                    code = ErrorCode.INVALID_PACKAGE_DATA,
                    message =
                        "Minimum Core version " +
                                "${value.compatibility.minimumCoreVersion} " +
                                "is not a valid semantic version.",
                    location =
                        "manifest.compatibility.minimumCoreVersion"
                )
            )
        }

        if (
            coreCompatibility.version <
            minimumCoreVersion
        ) {
            return listOf(
                ValidationIssue(
                    severity = ValidationSeverity.FATAL,
                    code = ErrorCode.INVALID_PACKAGE_DATA,
                    message =
                        "Package requires Core version " +
                                "$minimumCoreVersion or newer, " +
                                "but current Core version is " +
                                "${coreCompatibility.version}.",
                    location =
                        "manifest.compatibility.minimumCoreVersion"
                )
            )
        }

        return emptyList()
    }
}