package org.syriacplatform.packagevalidation.validators.references

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من أن جميع LiturgicalItem IDs المشار إليها
 * داخل PrayerSequence موجودة فعليًا في الحزمة.
 */
class PrayerSequenceLiturgicalItemReferenceRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val liturgicalItemIds =
            value.liturgicalItems
                .map { item -> item.id }
                .toSet()

        return buildList {
            value.prayerSequences.forEach { sequence ->
                sequence.liturgicalItemIds.forEachIndexed { index, itemId ->
                    if (itemId !in liturgicalItemIds) {
                        add(
                            ValidationIssue(
                                severity = ValidationSeverity.FATAL,
                                code = ErrorCode.INVALID_REFERENCE,
                                message =
                                    "Prayer sequence ${sequence.id.value} " +
                                            "references missing liturgical item " +
                                            "${itemId.value}.",
                                location =
                                    "prayerSequences[${sequence.id.value}]" +
                                            ".liturgicalItemIds[$index]"
                            )
                        )
                    }
                }
            }
        }
    }
}