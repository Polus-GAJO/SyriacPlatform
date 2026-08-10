package org.syriacplatform.packagevalidation.validators.references

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من أن كل Melody تشير إلى Qolo موجود فعليًا في الحزمة.
 */
class MelodyQoloReferenceRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val qoloIds =
            value.qolos
                .map { qolo -> qolo.id }
                .toSet()

        return buildList {
            value.melodies.forEach { melody ->
                if (melody.qoloId !in qoloIds) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.FATAL,
                            code = ErrorCode.INVALID_REFERENCE,
                            message =
                                "Melody ${melody.id.value} " +
                                        "references missing qolo " +
                                        "${melody.qoloId.value}.",
                            location =
                                "melodies[${melody.id.value}].qoloId"
                        )
                    )
                }
            }
        }
    }
}