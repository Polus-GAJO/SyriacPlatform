package org.syriacplatform.packagevalidation.validators

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.models.PackageProfile
import org.syriacplatform.packageformat.parsed.PackageCollectionPresence
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

/**
 * يتحقق من أن الحزمة تحتوي جميع مجموعات المحتوى
 * المطلوبة بواسطة PackageProfile المعلن.
 *
 * Required تعني أن الـ collection يجب أن تكون موجودة
 * في الحزمة، ولا تعني أنها يجب أن تحتوي عناصر.
 *
 * لذلك:
 *
 * present = true + empty list
 * → صحيح بالنسبة إلى Profile Validation.
 *
 * present = false
 * → FATAL إذا كانت المجموعة Required.
 */
class ProfileValidator :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        val presence =
            value.collectionPresence

        return buildList {
            when (value.manifest.profile) {

                PackageProfile.OCCASION -> {
                    requireCollection(
                        presence.entryPoints,
                        "entryPoints"
                    )

                    requireCollection(
                        presence.occasions,
                        "occasions"
                    )

                    requireCollection(
                        presence.prayers,
                        "prayers"
                    )

                    requireCollection(
                        presence.prayerSequences,
                        "prayerSequences"
                    )

                    requireCollection(
                        presence.liturgicalItems,
                        "liturgicalItems"
                    )

                    requireCollection(
                        presence.texts,
                        "texts"
                    )
                }

                PackageProfile.SHHIMA -> {
                    requireCollection(
                        presence.entryPoints,
                        "entryPoints"
                    )

                    requireCollection(
                        presence.prayers,
                        "prayers"
                    )

                    requireCollection(
                        presence.prayerSequences,
                        "prayerSequences"
                    )

                    requireCollection(
                        presence.liturgicalItems,
                        "liturgicalItems"
                    )

                    requireCollection(
                        presence.texts,
                        "texts"
                    )
                }

                PackageProfile.FULL_LIBRARY -> {
                    requireAllCollections(
                        presence
                    )
                }
            }
        }
    }

    private fun MutableList<ValidationIssue>.requireAllCollections(
        presence: PackageCollectionPresence
    ) {
        requireCollection(
            presence.entryPoints,
            "entryPoints"
        )

        requireCollection(
            presence.occasions,
            "occasions"
        )

        requireCollection(
            presence.prayers,
            "prayers"
        )

        requireCollection(
            presence.prayerSequences,
            "prayerSequences"
        )

        requireCollection(
            presence.liturgicalItems,
            "liturgicalItems"
        )

        requireCollection(
            presence.texts,
            "texts"
        )

        requireCollection(
            presence.qolos,
            "qolos"
        )

        requireCollection(
            presence.melodies,
            "melodies"
        )

        requireCollection(
            presence.qintos,
            "qintos"
        )

        requireCollection(
            presence.petgomos,
            "petgomos"
        )

        requireCollection(
            presence.melodyQintoAssignments,
            "melodyQintoAssignments"
        )
    }

    private fun MutableList<ValidationIssue>.requireCollection(
        isPresent: Boolean,
        collectionName: String
    ) {
        if (!isPresent) {
            add(
                ValidationIssue(
                    severity = ValidationSeverity.FATAL,
                    code = ErrorCode.INVALID_PACKAGE_DATA,
                    message =
                        "Required collection $collectionName " +
                                "is missing for the declared package profile.",
                    location =
                        "collectionPresence.$collectionName"
                )
            )
        }
    }
}