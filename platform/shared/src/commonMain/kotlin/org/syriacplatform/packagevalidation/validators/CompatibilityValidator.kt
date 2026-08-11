package org.syriacplatform.packagevalidation.validators

import org.syriacplatform.packageformat.models.PackageManifest
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.compatibility.CoreCompatibility
import org.syriacplatform.packagevalidation.compatibility.MinimumCoreVersionRule
import org.syriacplatform.packagevalidation.compatibility.SchemaCompatibilityRule

/**
 * منسق قواعد Compatibility Validation.
 *
 * يتحقق من قدرة الـ Core الحالي على فهم الحزمة وتشغيلها.
 */
class CompatibilityValidator(
    coreCompatibility: CoreCompatibility,
    private val rules:
    List<PackageValidationRule<PackageManifest>> =
        listOf(
            SchemaCompatibilityRule(coreCompatibility),
            MinimumCoreVersionRule(coreCompatibility)
        )
) : PackageValidationRule<PackageManifest> {

    override fun validate(
        value: PackageManifest
    ): List<ValidationIssue> {
        return buildList {
            rules.forEach { rule ->
                addAll(
                    rule.validate(value)
                )
            }
        }
    }
}