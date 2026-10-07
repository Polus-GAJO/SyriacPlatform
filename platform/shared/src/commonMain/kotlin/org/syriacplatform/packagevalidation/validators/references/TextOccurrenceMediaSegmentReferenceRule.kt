package org.syriacplatform.packagevalidation.validators.references

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

class TextOccurrenceMediaSegmentReferenceRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val mediaSegmentIds =
            value.mediaSegments.map { it.id }.toSet()

        val textOccurrenceIds =
            value.liturgicalItems
                .flatMap { item ->
                    when (val target = item.target) {
                        is LiturgicalItemTarget.Qolo ->
                            target.verses
                        is LiturgicalItemTarget.UnresolvedQolo ->
                            target.verses
                        is LiturgicalItemTarget.Text ->
                            emptyList()
                    }
                }
                .map { occurrence -> occurrence.id }
                .toSet()

        return buildList {
            value.textOccurrenceMediaSegments.forEach { relation ->
                if (relation.textOccurrenceId !in textOccurrenceIds) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.FATAL,
                            code = ErrorCode.INVALID_REFERENCE,
                            message =
                                "TextOccurrenceMediaSegment ${relation.id.value} references missing " +
                                    "TextOccurrence ${relation.textOccurrenceId.value}.",
                            location =
                                "textOccurrenceMediaSegments[${relation.id.value}].textOccurrenceId"
                        )
                    )
                }

                if (relation.mediaSegmentId !in mediaSegmentIds) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.FATAL,
                            code = ErrorCode.INVALID_REFERENCE,
                            message =
                                "TextOccurrenceMediaSegment ${relation.id.value} references missing " +
                                    "MediaSegment ${relation.mediaSegmentId.value}.",
                            location =
                                "textOccurrenceMediaSegments[${relation.id.value}].mediaSegmentId"
                        )
                    )
                }
            }
        }
    }
}
