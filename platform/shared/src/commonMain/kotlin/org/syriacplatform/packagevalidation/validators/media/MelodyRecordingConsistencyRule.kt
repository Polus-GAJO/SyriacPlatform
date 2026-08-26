package org.syriacplatform.packagevalidation.validators.media

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

class MelodyRecordingConsistencyRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        return buildList {
            value.melodies.forEach { melody ->
                val hasCanonicalRecordings =
                    melody.recordingIds.isNotEmpty()

                if (melody.hasRecording != hasCanonicalRecordings) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.FATAL,
                            code = ErrorCode.INVALID_PACKAGE_DATA,
                            message =
                                "Melody ${melody.id.value} hasRecording=" +
                                        "${melody.hasRecording} but recordingIds contains " +
                                        "${melody.recordingIds.size} item(s).",
                            location =
                                "melodies[${melody.id.value}].hasRecording"
                        )
                    )
                }

                melody.recordingIds
                    .groupingBy { it }
                    .eachCount()
                    .filterValues { count -> count > 1 }
                    .keys
                    .sortedBy { it.value }
                    .forEach { duplicateId ->
                        add(
                            ValidationIssue(
                                severity = ValidationSeverity.FATAL,
                                code = ErrorCode.INVALID_PACKAGE_DATA,
                                message =
                                    "Melody ${melody.id.value} contains duplicate recordingId " +
                                            "${duplicateId.value}.",
                                location =
                                    "melodies[${melody.id.value}].recordingIds"
                            )
                        )
                    }
            }
        }
    }
}