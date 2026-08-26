package org.syriacplatform.packagevalidation.validators.media

import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.packageformat.parsed.ParsedApplicationPackage
import org.syriacplatform.packagevalidation.PackageValidationRule
import org.syriacplatform.packagevalidation.ValidationIssue
import org.syriacplatform.packagevalidation.ValidationSeverity

class MediaAssetPathRule :
    PackageValidationRule<ParsedApplicationPackage> {

    override fun validate(
        value: ParsedApplicationPackage
    ): List<ValidationIssue> {
        return buildList {
            value.mediaAssets.forEach { mediaAsset ->
                val reason =
                    invalidReason(mediaAsset.path)

                if (reason != null) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.FATAL,
                            code = ErrorCode.INVALID_PACKAGE_DATA,
                            message =
                                "MediaAsset ${mediaAsset.id.value} has invalid package path " +
                                        "'${mediaAsset.path}': $reason",
                            location =
                                "mediaAssets[${mediaAsset.id.value}].path"
                        )
                    )
                }

                if (mediaAsset.type.isBlank()) {
                    add(
                        ValidationIssue(
                            severity = ValidationSeverity.FATAL,
                            code = ErrorCode.INVALID_PACKAGE_DATA,
                            message =
                                "MediaAsset ${mediaAsset.id.value} has a blank media type.",
                            location =
                                "mediaAssets[${mediaAsset.id.value}].type"
                        )
                    )
                }
            }
        }
    }

    private fun invalidReason(
        path: String
    ): String? {
        if (path.isBlank()) {
            return "path is blank."
        }

        if ('\\' in path) {
            return "backslash is not allowed."
        }

        if (path.startsWith("/")) {
            return "absolute paths are not allowed."
        }

        if (
            Regex(
                "^[A-Za-z][A-Za-z0-9+.-]*:"
            ).containsMatchIn(path)
        ) {
            return "drive letters and URI schemes are not allowed."
        }

        if (!path.startsWith("media/")) {
            return "path must be rooted under media/."
        }

        val segments =
            path.split('/')

        if (segments.any { it.isEmpty() }) {
            return "empty path segments are not allowed."
        }

        if (
            segments.any {
                it == "." ||
                        it == ".."
            }
        ) {
            return "dot path segments are not allowed."
        }

        return null
    }
}