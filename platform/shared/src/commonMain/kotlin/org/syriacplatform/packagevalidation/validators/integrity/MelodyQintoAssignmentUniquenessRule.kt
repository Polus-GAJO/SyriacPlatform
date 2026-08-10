package org.syriacplatform.packagevalidation.validators.integrity

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من أن العلاقة بين Melody وQinto
 * لا تُعرّف أكثر من مرة داخل الحزمة.
 *
 * uniqueness تعتمد على الزوج:
 * melodyId + qintoId
 *
 * ولا تعتمد على role.
 */
class MelodyQintoAssignmentUniquenessRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        return value.melodyQintoAssignments
            .groupingBy { assignment ->
                assignment.melodyId to assignment.qintoId
            }
            .eachCount()
            .filterValues { count ->
                count > 1
            }
            .keys
            .map { (melodyId, qintoId) ->
                ValidationIssue(
                    severity = ValidationSeverity.FATAL,
                    code = ErrorCode.INVALID_PACKAGE_DATA,
                    message =
                        "Melody-Qinto assignment is defined more than once: " +
                                "melodyId=${melodyId.value}, " +
                                "qintoId=${qintoId.value}.",
                    location =
                        "melodyQintoAssignments[" +
                                "melodyId=${melodyId.value}," +
                                "qintoId=${qintoId.value}]"
                )
            }
    }
}