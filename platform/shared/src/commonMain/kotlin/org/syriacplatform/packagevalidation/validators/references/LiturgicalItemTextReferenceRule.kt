package org.syriacplatform.packagevalidation.validators.references

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من أن كل LiturgicalItem من نوع Text
 * يشير إلى TextContent موجود فعليًا في الحزمة.
 */
class LiturgicalItemTextReferenceRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val textIds =
            value.texts
                .map { text -> text.id }
                .toSet()

        return buildList {
            value.liturgicalItems.forEach { item ->
                val target = item.target

                if (
                    target is LiturgicalItemTarget.Text &&
                    target.textId !in textIds
                ) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.FATAL,
                            code = ErrorCode.INVALID_REFERENCE,
                            message =
                                "Liturgical item ${item.id.value} " +
                                        "references missing text " +
                                        "${target.textId.value}.",
                            location =
                                "liturgicalItems[${item.id.value}].target.textId"
                        )
                    )
                }
            }
        }
    }
}