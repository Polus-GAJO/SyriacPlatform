package org.syriacplatform.packagevalidation.validators.references

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من أن جميع مراجع Petgomo السياقية داخل
 * LiturgicalItem تشير إلى Petgomo موجود فعليًا.
 *
 * يشمل:
 * - Petgomo الاختياري للنص المستقل.
 * - Petgomo الاختياري لكل بيت داخل Qolo.
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
                when (
                    val target = item.target
                ) {
                    is LiturgicalItemTarget.Text -> {
                        val petgomoId =
                            target.petgomoId

                        if (
                            petgomoId != null &&
                            petgomoId !in petgomoIds
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
                                                "references missing " +
                                                "petgomo " +
                                                "${petgomoId.value}.",
                                    location =
                                        "liturgicalItems" +
                                                "[${item.id.value}]" +
                                                ".target.petgomoId"
                                )
                            )
                        }
                    }

                    is LiturgicalItemTarget.Qolo -> {
                        target.verses.forEachIndexed {
                                index,
                                verse ->

                            val petgomoId =
                                verse.petgomoId

                            if (
                                petgomoId != null &&
                                petgomoId !in petgomoIds
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
                                                    "missing petgomo " +
                                                    "${petgomoId.value}.",
                                        location =
                                            "liturgicalItems" +
                                                    "[${item.id.value}]" +
                                                    ".target.verses[$index]" +
                                                    ".petgomoId"
                                    )
                                )
                            }
                        }
                    }

                    is LiturgicalItemTarget.UnresolvedQolo -> {
                        target.verses.forEachIndexed {
                                index,
                                verse ->

                            val petgomoId =
                                verse.petgomoId

                            if (
                                petgomoId != null &&
                                petgomoId !in petgomoIds
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
                                                    "missing petgomo " +
                                                    "${petgomoId.value}.",
                                        location =
                                            "liturgicalItems" +
                                                    "[${item.id.value}]" +
                                                    ".target.verses[$index]" +
                                                    ".petgomoId"
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