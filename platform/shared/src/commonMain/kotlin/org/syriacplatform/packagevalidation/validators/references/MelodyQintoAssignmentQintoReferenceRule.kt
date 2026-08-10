package org.syriacplatform.packagevalidation.validators.references

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من أن كل MelodyQintoAssignment
 * تشير إلى Qinto موجودة فعليًا في الحزمة.
 */
class MelodyQintoAssignmentQintoReferenceRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val qintoIds =
            value.qintos
                .map { qinto -> qinto.id }
                .toSet()

        return buildList {
            value.melodyQintoAssignments.forEachIndexed { index, assignment ->
                if (assignment.qintoId !in qintoIds) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.FATAL,
                            code = ErrorCode.INVALID_REFERENCE,
                            message =
                                "Melody-qinto assignment at index $index " +
                                        "references missing qinto " +
                                        "${assignment.qintoId.value}.",
                            location =
                                "melodyQintoAssignments[$index].qintoId"
                        )
                    )
                }
            }
        }
    }
}