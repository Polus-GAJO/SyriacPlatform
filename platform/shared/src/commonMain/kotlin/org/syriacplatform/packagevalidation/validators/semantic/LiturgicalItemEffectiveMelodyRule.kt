package org.syriacplatform.packagevalidation.validators.semantic

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من أن effectiveMelody المختارة لظهور Qolo
 * تنتمي إلى Qolo نفسه وفق النموذج القانوني الحالي:
 *
 * Qolo 1 -> many Melody
 *
 * وجود Melody نفسها مسؤولية Reference Validation.
 */
class LiturgicalItemEffectiveMelodyRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val melodiesById =
            value.melodies.associateBy { melody ->
                melody.id
            }

        return buildList {
            value.liturgicalItems.forEach { item ->
                val target = item.target

                if (target is LiturgicalItemTarget.Qolo) {
                    val melody =
                        melodiesById[target.effectiveMelodyId]

                    /*
                     * إذا لم توجد Melody، فلا ننتج خطأ هنا.
                     * ReferenceValidator مسؤول عن المرجع المفقود.
                     */
                    if (
                        melody != null &&
                        melody.qoloId != target.qoloId
                    ) {
                        add(
                            ValidationIssue(
                                severity = ValidationSeverity.FATAL,
                                code = ErrorCode.INVALID_PACKAGE_DATA,
                                message =
                                    "Effective melody " +
                                            "${target.effectiveMelodyId.value} " +
                                            "belongs to qolo " +
                                            "${melody.qoloId.value}, " +
                                            "but liturgical item " +
                                            "${item.id.value} uses qolo " +
                                            "${target.qoloId.value}.",
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