package org.syriacplatform.packagevalidation.validators.references

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من أن جميع مراجع Text داخل LiturgicalItem
 * تشير إلى TextContent موجود فعليًا في الحزمة.
 *
 * يشمل:
 * - Text المستقل.
 * - أبيات Qolo المرتبة.
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
                when (
                    val target = item.target
                ) {
                    is LiturgicalItemTarget.Text -> {
                        if (
                            target.textId !in textIds
                        ) {
                            add(
                                ValidationIssue(
                                    severity =
                                        ValidationSeverity.FATAL,
                                    code =
                                        ErrorCode.INVALID_REFERENCE,
                                    message =
                                        "Liturgical item ${item.id.value} " +
                                                "references missing text " +
                                                "${target.textId.value}.",
                                    location =
                                        "liturgicalItems[${item.id.value}]" +
                                                ".target.textId"
                                )
                            )
                        }
                    }

                    is LiturgicalItemTarget.Qolo -> {
                        target.verses.forEachIndexed {
                                index,
                                verse ->

                            if (
                                verse.textId !in textIds
                            ) {
                                add(
                                    ValidationIssue(
                                        severity =
                                            ValidationSeverity.FATAL,
                                        code =
                                            ErrorCode.INVALID_REFERENCE,
                                        message =
                                            "Liturgical item " +
                                                    "${item.id.value} " +
                                                    "verse $index references " +
                                                    "missing text " +
                                                    "${verse.textId.value}.",
                                        location =
                                            "liturgicalItems" +
                                                    "[${item.id.value}]" +
                                                    ".target.verses[$index]" +
                                                    ".textId"
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}