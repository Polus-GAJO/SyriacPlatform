package org.syriacplatform.packagevalidation.validators.media

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

class MelodyRecordingReferenceRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val mediaAssetIds =
            value.mediaAssets
                .map { mediaAsset -> mediaAsset.id }
                .toSet()

        return buildList {
            value.melodies.forEach { melody ->
                melody.recordingIds.forEachIndexed { index, recordingId ->
                    if (recordingId !in mediaAssetIds) {
                        add(
                            ValidationIssue(
                                severity = ValidationSeverity.FATAL,
                                code = ErrorCode.INVALID_REFERENCE,
                                message =
                                    "Melody ${melody.id.value} references missing MediaAsset " +
                                            "${recordingId.value}.",
                                location =
                                    "melodies[${melody.id.value}].recordingIds[$index]"
                            )
                        )
                    }
                }
            }
        }
    }
}