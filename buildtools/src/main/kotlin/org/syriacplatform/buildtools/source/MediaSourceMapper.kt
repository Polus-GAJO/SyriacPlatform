package org.syriacplatform.buildtools.source

import org.syriacplatform.buildtools.source.models.ExistsInMediaSource
import org.syriacplatform.buildtools.source.models.ExistsInTextMediaSegmentSource
import org.syriacplatform.buildtools.source.models.MediaAssetSource
import org.syriacplatform.buildtools.source.models.MediaSegmentSource
import org.syriacplatform.buildtools.source.models.MediaTimingSetSource
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

    fun toExistsInMedia(
        row: CsvRow
    ): ExistsInMediaSource {
        val role = row.requiredText("Role")
        val sort = row.requiredLong("Sort")

        require(role == PERFORMANCE_ROLE) {
            "ExistsInMedia ${row["ExistsInMediaID"]} has unsupported " +
                    "Role '$role'."
        }

        require(sort > 0L) {
            "ExistsInMedia ${row["ExistsInMediaID"]} must have " +
                    "Sort > 0, but was $sort."
        }

        return ExistsInMediaSource(
            id = row.requiredLong("ExistsInMediaID"),
            existsInId = row.requiredLong("ExistsInID"),
            mediaAssetId = row.requiredLong("MediaAssetID"),
            role = role,
            sort = sort,
            mediaTimingSetId = row.optionalLong("MediaTimingSetID")
        )
    }

    fun toExistsInTextMediaSegment(
        row: CsvRow
    ): ExistsInTextMediaSegmentSource {
        return ExistsInTextMediaSegmentSource(
            id = row.requiredLong("ExistsInTextMediaSegmentID"),
            existsInTextId = row.requiredLong("ExistsInTextID"),
            mediaSegmentId = row.requiredLong("MediaSegmentID")
        )
    }

    fun toMediaSegment(
        row: CsvRow
    ): MediaSegmentSource {
        val sequence = row.requiredLong("Sequence")
        val startMs =
            row.optionalLong("StartMs")
                ?.let(::authorTimingToElapsedMs)

        val endMs =
            row.optionalLong("EndMs")
                ?.let(::authorTimingToElapsedMs)

        require(sequence > 0L) {
            "MediaSegment ${row["MediaSegmentID"]} must have " +
                    "Sequence > 0, but was $sequence."
        }

        require(startMs == null || startMs >= 0L) {
            "MediaSegment ${row["MediaSegmentID"]} must have " +
                    "StartMs >= 0, but was $startMs."
        }

        return MediaSegmentSource(
            id = row.requiredLong("MediaSegmentID"),
            mediaTimingSetId = row.requiredLong("MediaTimingSetID"),
            sequence = sequence,
            startMs = startMs,
            endMs = endMs
        )
    }

    fun toMediaTimingSet(
        row: CsvRow
    ): MediaTimingSetSource {
        return MediaTimingSetSource(
            id = row.requiredLong("MediaTimingSetID"),
            mediaAssetId = row.requiredLong("MediaAssetID"),
            name = row["Name"]
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
        )
    }

    /**
     * Author DB stores timing as a numeric MMSSmmm value rather than
     * elapsed milliseconds. For example, 123456 represents 01:23.456
     * and must become 83,456 elapsed milliseconds.
     *
     * Negative values are preserved so the existing source validation /
     * downstream playable-interval rules can reject or ignore them.
     */
    private fun authorTimingToElapsedMs(
        value: Long
    ): Long {
        if (value < 0L) {
            return value
        }

        val milliseconds =
            value % 1_000L

        val seconds =
            (value / 1_000L) % 100L

        val minutes =
            value / 100_000L

        return (
            minutes * 60_000L +
                seconds * 1_000L +
                milliseconds
        )
    }

    private fun CsvRow.optionalLong(
        columnName: String
    ): Long? {
        val rawValue =
            this[columnName]
                ?.trim()
                ?.takeIf { it.isNotEmpty() }
                ?: return null

        return rawValue.toLongOrNull()
            ?: error(
                "Column '$columnName' must contain a Long, " +
                        "but was '$rawValue'."
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

        const val PERFORMANCE_ROLE =
            "PERFORMANCE"

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