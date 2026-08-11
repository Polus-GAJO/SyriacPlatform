package org.syriacplatform.packagevalidation

import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.compatibility.CoreCompatibility
import org.syriacplatform.packagevalidation.validators.CompatibilityValidator
import org.syriacplatform.packagevalidation.validators.IntegrityValidator
import org.syriacplatform.packagevalidation.validators.ManifestValidator
import org.syriacplatform.packagevalidation.validators.ReferenceValidator
import org.syriacplatform.packagevalidation.validators.SemanticValidator
import org.syriacplatform.packagevalidation.validators.ProfileValidator

/**
 * المنسق الأعلى لعملية التحقق من الحزمة بعد Parsing.
 *
 * يجمع نتائج جميع طبقات التحقق الحالية في ValidationReport واحد.
 *
 * لا يتوقف عند أول FATAL؛ بل يجمع جميع المشكلات
 * التي تستطيع طبقات التحقق اكتشافها في تشغيل واحد.
 */
class PackageValidator(
    coreCompatibility: CoreCompatibility,
    private val manifestValidator: ManifestValidator =
        ManifestValidator(),
    private val compatibilityValidator: CompatibilityValidator =
        CompatibilityValidator(
            coreCompatibility = coreCompatibility
        ),
    private val profileValidator: ProfileValidator =
        ProfileValidator(),
    private val referenceValidator: ReferenceValidator =
        ReferenceValidator(),
    private val integrityValidator: IntegrityValidator =
        IntegrityValidator(),
    private val semanticValidator: SemanticValidator =
        SemanticValidator()
) {

    fun validate(
        value: ParsedApplicationPackage
    ): ValidationReport {
        val issues = buildList {
            addAll(
                manifestValidator.validate(
                    value.manifest
                )
            )

            addAll(
                compatibilityValidator.validate(
                    value.manifest
                )
            )

            addAll(
                profileValidator.validate(
                    value
                )
            )

            addAll(
                referenceValidator.validate(
                    value
                )
            )

            addAll(
                integrityValidator.validate(
                    value
                )
            )

            addAll(
                semanticValidator.validate(
                    value
                )
            )
        }

        return ValidationReport(
            issues = issues
        )
    }
}