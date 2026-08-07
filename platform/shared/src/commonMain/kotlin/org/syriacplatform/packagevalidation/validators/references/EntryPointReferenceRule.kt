package org.syriacplatform.packagevalidation.validators.references

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.content.models.EntryPointTarget
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

class EntryPointReferenceRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val occasionIds =
            value.occasions
                .map { occasion -> occasion.id }
                .toSet()

        return buildList {
            value.entryPoints.forEach { entryPoint ->
                when (val target = entryPoint.target) {
                    is EntryPointTarget.Occasion -> {
                        if (target.occasionId !in occasionIds) {
                            add(
                                ValidationIssue(
                                    severity = ValidationSeverity.FATAL,
                                    code = ErrorCode.INVALID_REFERENCE,
                                    message =
                                        "Entry point ${entryPoint.id.value} " +
                                                "references missing occasion " +
                                                "${target.occasionId.value}.",
                                    location =
                                        "entryPoints[${entryPoint.id.value}].target"
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}