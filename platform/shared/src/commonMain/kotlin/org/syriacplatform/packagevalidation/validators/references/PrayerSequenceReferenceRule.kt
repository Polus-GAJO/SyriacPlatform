package org.syriacplatform.packagevalidation.validators.references

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

class PrayerSequenceReferenceRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val prayerIds =
            value.prayers
                .map { prayer -> prayer.id }
                .toSet()

        return buildList {
            value.prayerSequences.forEach { sequence ->
                if (sequence.prayerId !in prayerIds) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.FATAL,
                            code = ErrorCode.INVALID_REFERENCE,
                            message =
                                "Prayer sequence ${sequence.id.value} " +
                                        "references missing prayer " +
                                        "${sequence.prayerId.value}.",
                            location =
                                "prayerSequences[${sequence.id.value}].prayerId"
                        )
                    )
                }
            }
        }
    }
}