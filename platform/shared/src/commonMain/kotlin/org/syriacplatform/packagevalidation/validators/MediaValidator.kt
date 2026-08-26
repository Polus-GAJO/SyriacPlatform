package org.syriacplatform.packagevalidation.validators

import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.validators.media.MediaAssetPathRule
import org.syriacplatform.packagevalidation.validators.media.MelodyRecordingConsistencyRule
import org.syriacplatform.packagevalidation.validators.media.MelodyRecordingReferenceRule

/**
 * Coordinates validation rules for canonical package media metadata.
 */
class MediaValidator(
    private val rules:
    List<PackageValidationRule<ParsedApplicationPackage>> =
        listOf(
            MelodyRecordingReferenceRule(),
            MelodyRecordingConsistencyRule(),
            MediaAssetPathRule()
        )
) : PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
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