package org.syriacplatform.packagevalidation.validators.references

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

class MediaSegmentReferenceRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val timingSetIds =
            value.mediaTimingSets.map { it.id }.toSet()

        return buildList {
            value.mediaSegments.forEach { segment ->
                if (segment.mediaTimingSetId !in timingSetIds) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.FATAL,
                            code = ErrorCode.INVALID_REFERENCE,
                            message =
                                "MediaSegment ${segment.id.value} references missing " +
                                    "MediaTimingSet ${segment.mediaTimingSetId.value}.",
                            location =
                                "mediaSegments[${segment.id.value}].mediaTimingSetId"
                        )
                    )
                }
            }
        }
    }
}
