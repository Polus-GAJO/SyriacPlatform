package org.syriacplatform.packagevalidation.validators.integrity

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من عدم وجود أكثر من EntryPoint افتراضي واحد.
 *
 * عدم وجود EntryPoint افتراضي مسموح في هذه المرحلة،
 * لأن إلزام وجود واحد قد يعتمد لاحقًا على PackageProfile.
 */
class DefaultEntryPointUniquenessRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val defaultEntryPoints =
            value.entryPoints.filter { entryPoint ->
                entryPoint.isDefault
            }

        if (defaultEntryPoints.size <= 1) {
            return emptyList()
        }

        return listOf(
            ValidationIssue(
                severity = ValidationSeverity.FATAL,
                code = ErrorCode.INVALID_PACKAGE_DATA,
                message =
                    "Package defines more than one default entry point.",
                location = "entryPoints.isDefault"
            )
        )
    }
}