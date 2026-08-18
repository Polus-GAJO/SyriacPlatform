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

                    val effectiveMelodyId =
                        target.effectiveMelodyId

                    if (effectiveMelodyId != null) {
                        val melody =
                            melodiesById[
                                effectiveMelodyId
                            ]

                        /*
                         * المراجع المفقودة مسؤولية
                         * Reference Validation.
                         */
                        if (
                            melody != null &&
                            melody.qoloId != target.qoloId
                        ) {
                            add(
                                ValidationIssue(
                                    severity =
                                        ValidationSeverity.FATAL,
                                    code =
                                        ErrorCode.INVALID_PACKAGE_DATA,
                                    message =
                                        "Effective melody " +
                                                "${effectiveMelodyId.value} " +
                                                "belongs to qolo " +
                                                "${melody.qoloId.value}, " +
                                                "but liturgical item " +
                                                "${item.id.value} uses qolo " +
                                                "${target.qoloId.value}.",
                                    location =
                                        "liturgicalItems[" +
                                                "${item.id.value}]" +
                                                ".target." +
                                                "effectiveMelodyId"
                                )
                            )
                        }
                    }

                    target.melodyCandidateIds
                        .forEach { candidateId ->

                            val candidate =
                                melodiesById[
                                    candidateId
                                ]

                            /*
                             * المراجع المفقودة مسؤولية
                             * Reference Validation.
                             */
                            if (
                                candidate != null &&
                                candidate.qoloId !=
                                target.qoloId
                            ) {
                                add(
                                    ValidationIssue(
                                        severity =
                                            ValidationSeverity.FATAL,
                                        code =
                                            ErrorCode.INVALID_PACKAGE_DATA,
                                        message =
                                            "Melody candidate " +
                                                    "${candidateId.value} " +
                                                    "belongs to qolo " +
                                                    "${candidate.qoloId.value}, " +
                                                    "but liturgical item " +
                                                    "${item.id.value} uses qolo " +
                                                    "${target.qoloId.value}.",
                                        location =
                                            "liturgicalItems[" +
                                                    "${item.id.value}]" +
                                                    ".target." +
                                                    "melodyCandidateIds"
                                    )
                                )
                            }
                        }
                }
            }
        }
    }
}