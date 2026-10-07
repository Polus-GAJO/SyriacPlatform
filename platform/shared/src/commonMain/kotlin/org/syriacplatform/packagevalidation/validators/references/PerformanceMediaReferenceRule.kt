package org.syriacplatform.packagevalidation.validators.references

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

class PerformanceMediaReferenceRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val liturgicalItemIds =
            value.liturgicalItems.map { it.id }.toSet()
        val mediaAssetIds =
            value.mediaAssets.map { it.id }.toSet()
        val timingSetIds =
            value.mediaTimingSets.map { it.id }.toSet()

        return buildList {
            value.performanceMedia.forEach { performance ->
                if (performance.liturgicalItemId !in liturgicalItemIds) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.FATAL,
                            code = ErrorCode.INVALID_REFERENCE,
                            message =
                                "PerformanceMedia ${performance.id.value} references missing " +
                                    "LiturgicalItem ${performance.liturgicalItemId.value}.",
                            location =
                                "performanceMedia[${performance.id.value}].liturgicalItemId"
                        )
                    )
                }

                if (performance.mediaAssetId !in mediaAssetIds) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.FATAL,
                            code = ErrorCode.INVALID_REFERENCE,
                            message =
                                "PerformanceMedia ${performance.id.value} references missing " +
                                    "MediaAsset ${performance.mediaAssetId.value}.",
                            location =
                                "performanceMedia[${performance.id.value}].mediaAssetId"
                        )
                    )
                }

                val timingSetId = performance.mediaTimingSetId
                if (timingSetId != null && timingSetId !in timingSetIds) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.FATAL,
                            code = ErrorCode.INVALID_REFERENCE,
                            message =
                                "PerformanceMedia ${performance.id.value} references missing " +
                                    "MediaTimingSet ${timingSetId.value}.",
                            location =
                                "performanceMedia[${performance.id.value}].mediaTimingSetId"
                        )
                    )
                }
            }
        }
    }
}
