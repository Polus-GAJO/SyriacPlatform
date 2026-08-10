package org.syriacplatform.packagevalidation.validators

import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.validators.integrity.CanonicalIdUniquenessRule
import org.syriacplatform.packagevalidation.validators.integrity.DefaultEntryPointUniquenessRule
import org.syriacplatform.packagevalidation.validators.integrity.MelodyQintoAssignmentUniquenessRule

/**
 * منسق قواعد التحقق من سلامة البيانات الداخلية للحزمة.
 *
 * Integrity Validation لا يتحقق من وجود المراجع،
 * بل من تماسك الكيانات وتعريفاتها القانونية.
 */
class IntegrityValidator(
    private val rules:
    List<PackageValidationRule<ParsedApplicationPackage>> =
        listOf(
            CanonicalIdUniquenessRule(),
            DefaultEntryPointUniquenessRule(),
            MelodyQintoAssignmentUniquenessRule()
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