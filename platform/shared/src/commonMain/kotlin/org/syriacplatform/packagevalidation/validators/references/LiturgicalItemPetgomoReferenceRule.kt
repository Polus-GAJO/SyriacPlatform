package org.syriacplatform.packagevalidation.validators.references

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من أن Petgomo الاختياري المرتبط بظهور Text
 * يشير إلى Petgomo موجود فعليًا في الحزمة.
 */
class LiturgicalItemPetgomoReferenceRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val petgomoIds =
            value.petgomos
                .map { petgomo -> petgomo.id }
                .toSet()

        return buildList {
            value.liturgicalItems.forEach { item ->
                val target = item.target

                if (target is LiturgicalItemTarget.Text) {
                    val petgomoId = target.petgomoId

                    if (
                        petgomoId != null &&
                        petgomoId !in petgomoIds
                    ) {
                        add(
                            ValidationIssue(
                                severity = ValidationSeverity.FATAL,
                                code = ErrorCode.INVALID_REFERENCE,
                                message =
                                    "Liturgical item ${item.id.value} " +
                                            "references missing petgomo " +
                                            "${petgomoId.value}.",
                                location =
                                    "liturgicalItems[${item.id.value}]" +
                                            ".target.petgomoId"
                            )
                        )
                    }
                }
            }
        }
    }
}