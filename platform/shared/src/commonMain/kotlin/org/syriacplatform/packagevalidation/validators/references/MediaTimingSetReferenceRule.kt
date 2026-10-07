package org.syriacplatform.packagevalidation.validators.references

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

class MediaTimingSetReferenceRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val mediaAssetIds =
            value.mediaAssets.map { it.id }.toSet()

        return buildList {
            value.mediaTimingSets.forEach { timingSet ->
                if (timingSet.mediaAssetId !in mediaAssetIds) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.FATAL,
                            code = ErrorCode.INVALID_REFERENCE,
                            message =
                                "MediaTimingSet ${timingSet.id.value} references missing " +
                                    "MediaAsset ${timingSet.mediaAssetId.value}.",
                            location =
                                "mediaTimingSets[${timingSet.id.value}].mediaAssetId"
                        )
                    )
                }
            }
        }
    }
}
