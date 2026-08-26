package org.syriacplatform.packagevalidation.validators.integrity

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من أن كل هوية قانونية Canonical ID
 * تُعرّف مرة واحدة فقط داخل مجموعة الكيانات الخاصة بها.
 *
 * هذه القاعدة لا تمنع تكرار استعمال الهوية في العلاقات
 * أو التسلسلات الليتورجية.
 */
class CanonicalIdUniquenessRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        return buildList {

            checkDuplicates(
                collectionName = "entryPoints",
                ids = value.entryPoints.map { it.id.value }
            )

            checkDuplicates(
                collectionName = "occasions",
                ids = value.occasions.map { it.id.value }
            )

            checkDuplicates(
                collectionName = "prayers",
                ids = value.prayers.map { it.id.value }
            )

            checkDuplicates(
                collectionName = "prayerSequences",
                ids = value.prayerSequences.map { it.id.value }
            )

            checkDuplicates(
                collectionName = "liturgicalItems",
                ids = value.liturgicalItems.map { it.id.value }
            )

            checkDuplicates(
                collectionName = "texts",
                ids = value.texts.map { it.id.value }
            )

            checkDuplicates(
                collectionName = "petgomos",
                ids = value.petgomos.map { it.id.value }
            )

            checkDuplicates(
                collectionName = "qolos",
                ids = value.qolos.map { it.id.value }
            )

            checkDuplicates(
                collectionName = "melodies",
                ids = value.melodies.map { it.id.value }
            )

            checkDuplicates(
                collectionName = "mediaAssets",
                ids = value.mediaAssets.map { it.id.value }
            )

            checkDuplicates(
                collectionName = "qintos",
                ids = value.qintos.map { it.id.value }
            )
        }
    }

    private fun MutableList<ValidationIssue>.checkDuplicates(
        collectionName: String,
        ids: List<Long>
    ) {
        ids
            .groupingBy { id -> id }
            .eachCount()
            .filterValues { count -> count > 1 }
            .keys
            .sorted()
            .forEach { duplicateId ->
                add(
                    ValidationIssue(
                        severity = ValidationSeverity.FATAL,
                        code = ErrorCode.INVALID_PACKAGE_DATA,
                        message =
                            "Canonical ID $duplicateId is defined more than once " +
                                    "in $collectionName.",
                        location =
                            "$collectionName[id=$duplicateId]"
                    )
                )
            }
    }
}