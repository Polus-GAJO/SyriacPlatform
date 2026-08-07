package org.syriacplatform.packagevalidation.validators.references

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

class OccasionReferenceRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val prayerSequenceIds =
            value.prayerSequences
                .map { sequence -> sequence.id }
                .toSet()

        return buildList {
            value.occasions.forEach { occasion ->
                occasion.prayerSequenceIds.forEachIndexed { index, sequenceId ->
                    if (sequenceId !in prayerSequenceIds) {
                        add(
                            ValidationIssue(
                                severity = ValidationSeverity.FATAL,
                                code = ErrorCode.INVALID_REFERENCE,
                                message =
                                    "Occasion ${occasion.id.value} " +
                                            "references missing prayer sequence " +
                                            "${sequenceId.value}.",
                                location =
                                    "occasions[${occasion.id.value}]" +
                                            ".prayerSequenceIds[$index]"
                            )
                        )
                    }
                }
            }
        }
    }
}