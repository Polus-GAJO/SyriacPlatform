package org.syriacplatform.packagevalidation.validators

import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.validators.semantic.LiturgicalItemEffectiveMelodyRule

/**
 * منسق قواعد التحقق الدلالي للحزمة.
 *
 * Semantic Validation يتحقق من أن العلاقات الموجودة
 * ليست صحيحة مرجعيًا فقط، بل متوافقة في معناها أيضًا.
 */
class SemanticValidator(
    private val rules:
    List<PackageValidationRule<ParsedApplicationPackage>> =
        listOf(
            LiturgicalItemEffectiveMelodyRule()
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