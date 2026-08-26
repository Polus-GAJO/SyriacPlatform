package org.syriacplatform.buildtools.source

import org.syriacplatform.buildtools.source.models.MediaAssetSource
import org.syriacplatform.buildtools.source.models.MelodyMediaSource

class MediaSourceMapper {

    fun toMediaAsset(
        row: CsvRow
    ): MediaAssetSource {
        val mediaType = row.requiredText("MediaType")

        require(mediaType in SUPPORTED_MEDIA_TYPES) {
            "MediaAsset ${row["MediaAssetID"]} has unsupported " +
                    "MediaType '$mediaType'."
        }

        val relativePath =
            row.requiredText("SourceRelativePath")

        require(isSafeRelativePath(relativePath)) {
            "MediaAsset ${row["MediaAssetID"]} has invalid " +
                    "SourceRelativePath '$relativePath'."
        }

        return MediaAssetSource(
            id = row.requiredLong("MediaAssetID"),
            mediaType = mediaType,
            sourceRelativePath = relativePath,
            performer = row["Performer"]
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        )
    }

    fun toMelodyMedia(
        row: CsvRow
    ): MelodyMediaSource {
        val role = row.requiredText("Role")
        val sort = row.requiredLong("Sort")

        require(role == RECORDING_ROLE) {
            "MelodyMedia ${row["MelodyMediaID"]} has unsupported " +
                    "Role '$role'."
        }

        require(sort > 0L) {
            "MelodyMedia ${row["MelodyMediaID"]} must have " +
                    "Sort > 0, but was $sort."
        }

        return MelodyMediaSource(
            id = row.requiredLong("MelodyMediaID"),
            melodyId = row.requiredLong("MelodyN"),
            mediaAssetId = row.requiredLong("MediaAssetID"),
            role = role,
            sort = sort
        )
    }

    private fun CsvRow.requiredLong(
        columnName: String
    ): Long {
        val rawValue = this[columnName]
            ?: error(
                "Required column '$columnName' is null or empty."
            )

        return rawValue.toLongOrNull()
            ?: error(
                "Column '$columnName' must contain a Long, " +
                        "but was '$rawValue'."
            )
    }

    private fun CsvRow.requiredText(
        columnName: String
    ): String {
        return this[columnName]
            ?.takeIf { it.isNotBlank() }
            ?: error(
                "Required column '$columnName' is null, empty, " +
                        "or blank."
            )
    }

    private fun isSafeRelativePath(
        value: String
    ): Boolean {
        if (value.isBlank()) {
            return false
        }

        if (
            value.startsWith("/") ||
            value.startsWith("\\")
        ) {
            return false
        }

        if (
            DRIVE_ABSOLUTE_PATH
                .matches(value)
        ) {
            return false
        }

        val segments =
            value.split('/', '\\')

        if (
            segments.any {
                it == ".."
            }
        ) {
            return false
        }

        return true
    }

    private companion object {
        const val RECORDING_ROLE =
            "RECORDING"

        val SUPPORTED_MEDIA_TYPES =
            setOf(
                "AUDIO",
                "NOTATION",
                "IMAGE",
                "DOCUMENT",
                "VIDEO"
            )

        val DRIVE_ABSOLUTE_PATH =
            Regex(
                pattern = "^[A-Za-z]:[\\\\/].*"
            )
    }
}