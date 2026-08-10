package org.syriacplatform.packagevalidation.validators.references

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من أن كل MelodyQintoAssignment
 * تشير إلى Melody موجودة فعليًا في الحزمة.
 */
class MelodyQintoAssignmentMelodyReferenceRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val melodyIds =
            value.melodies
                .map { melody -> melody.id }
                .toSet()

        return buildList {
            value.melodyQintoAssignments.forEachIndexed { index, assignment ->
                if (assignment.melodyId !in melodyIds) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.FATAL,
                            code = ErrorCode.INVALID_REFERENCE,
                            message =
                                "Melody-qinto assignment at index $index " +
                                        "references missing melody " +
                                        "${assignment.melodyId.value}.",
                            location =
                                "melodyQintoAssignments[$index].melodyId"
                        )
                    )
                }
            }
        }
    }
}