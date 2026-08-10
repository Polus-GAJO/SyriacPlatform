package org.syriacplatform.packagevalidation.validators.references

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من أن LiturgicalItem من نوع Qolo
 * يشير إلى Qolo وMelody موجودين فعليًا في الحزمة.
 *
 * لا يتحقق هنا من أن اللحن صالح للقولو؛
 * فهذه مسؤولية Semantic Validation.
 */
class LiturgicalItemQoloReferenceRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val qoloIds =
            value.qolos
                .map { qolo -> qolo.id }
                .toSet()

        val melodyIds =
            value.melodies
                .map { melody -> melody.id }
                .toSet()

        return buildList {
            value.liturgicalItems.forEach { item ->
                val target = item.target

                if (target is LiturgicalItemTarget.Qolo) {

                    if (target.qoloId !in qoloIds) {
                        add(
                            ValidationIssue(
                                severity = ValidationSeverity.FATAL,
                                code = ErrorCode.INVALID_REFERENCE,
                                message =
                                    "Liturgical item ${item.id.value} " +
                                            "references missing qolo " +
                                            "${target.qoloId.value}.",
                                location =
                                    "liturgicalItems[${item.id.value}]" +
                                            ".target.qoloId"
                            )
                        )
                    }

                    if (target.effectiveMelodyId !in melodyIds) {
                        add(
                            ValidationIssue(
                                severity = ValidationSeverity.FATAL,
                                code = ErrorCode.INVALID_REFERENCE,
                                message =
                                    "Liturgical item ${item.id.value} " +
                                            "references missing effective melody " +
                                            "${target.effectiveMelodyId.value}.",
                                location =
                                    "liturgicalItems[${item.id.value}]" +
                                            ".target.effectiveMelodyId"
                            )
                        )
                    }
                }
            }
        }
    }
}