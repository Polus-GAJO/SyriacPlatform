package org.syriacplatform.packagevalidation.validators

import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.validators.references.EntryPointReferenceRule
import org.syriacplatform.packagevalidation.validators.references.OccasionReferenceRule
import org.syriacplatform.packagevalidation.validators.references.PrayerSequenceReferenceRule
import org.syriacplatform.packagevalidation.validators.references.PrayerSequenceLiturgicalItemReferenceRule
import org.syriacplatform.packagevalidation.validators.references.LiturgicalItemTextReferenceRule
import org.syriacplatform.packagevalidation.validators.references.LiturgicalItemPetgomoReferenceRule

/**
 * منسق قواعد التحقق من المراجع بين كيانات الحزمة.
 */
class ReferenceValidator(
    private val rules:
    List<PackageValidationRule<ParsedApplicationPackage>> =
        listOf(
            EntryPointReferenceRule(),
            OccasionReferenceRule(),
            PrayerSequenceReferenceRule(),
            PrayerSequenceLiturgicalItemReferenceRule(),
            LiturgicalItemTextReferenceRule(),
            LiturgicalItemPetgomoReferenceRule()
        )
) : PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        return buildList {
            rules.forEach { rule ->
                addAll(
                    rule.validate(value)
                )
            }
        }
    }
}