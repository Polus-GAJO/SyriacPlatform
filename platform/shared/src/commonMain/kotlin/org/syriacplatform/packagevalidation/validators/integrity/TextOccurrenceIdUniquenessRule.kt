package org.syriacplatform.packagevalidation.validators.integrity

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من أن هوية TextOccurrence السياقية
 * تُعرّف مرة واحدة فقط داخل الحزمة.
 *
 * TextOccurrence كيان سياقي مستقل حتى عندما
 * تشير عدة ظهورات إلى TextContent نفسه.
 */
class TextOccurrenceIdUniquenessRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val occurrenceIds =
            value.liturgicalItems.flatMap { item ->
                when (val target = item.target) {
                    is LiturgicalItemTarget.Qolo ->
                        target.verses.map { it.id.value }

                    is LiturgicalItemTarget.UnresolvedQolo ->
                        target.verses.map { it.id.value }

                    is LiturgicalItemTarget.Text ->
                        emptyList()
                }
            }

        return occurrenceIds
            .groupingBy { id -> id }
            .eachCount()
            .filterValues { count -> count > 1 }
            .keys
            .sorted()
            .map { duplicateId ->
                ValidationIssue(
                    severity = ValidationSeverity.FATAL,
                    code = ErrorCode.INVALID_PACKAGE_DATA,
                    message =
                        "TextOccurrence ID $duplicateId is defined " +
                                "more than once in liturgicalItems.",
                    location =
                        "liturgicalItems.textOccurrences[id=$duplicateId]"
                )
            }
    }
}
