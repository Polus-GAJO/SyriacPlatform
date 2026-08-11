package org.syriacplatform.packageformat.loading

import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.ValidationReport

sealed interface PackageLoadResult {

    data class Success(
        val packageData: ParsedApplicationPackage,
        val validationReport: ValidationReport
    ) : PackageLoadResult

    data class ValidationFailed(
        val packageData: ParsedApplicationPackage,
        val validationReport: ValidationReport
    ) : PackageLoadResult

    data class Failure(
        val error: PlatformError
    ) : PackageLoadResult
}