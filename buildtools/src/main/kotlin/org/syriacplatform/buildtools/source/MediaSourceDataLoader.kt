package org.syriacplatform.buildtools.source

import java.nio.file.Files
import java.nio.file.Path
import org.syriacplatform.buildtools.source.models.ExistsInMediaSource
import org.syriacplatform.buildtools.source.models.MediaAssetSource
import org.syriacplatform.buildtools.source.models.MediaTimingSetSource
import org.syriacplatform.buildtools.source.models.MelodyMediaSource

class MediaSourceDataLoader(
    private val csvReader: CsvTableReader =
        CsvTableReader(),
    private val mapper: MediaSourceMapper =
        MediaSourceMapper()
) {

    fun load(
        directory: Path
    ): MediaSourceData {
        require(
            Files.isDirectory(directory)
        ) {
            "Media source directory does not exist: $directory"
        }

        val mediaAssets =
            readRows(
                directory = directory,
                fileName = "MediaAsset.csv"
            ).map(
                mapper::toMediaAsset
            )

        val melodyMedia =
            readRows(
                directory = directory,
                fileName = "MelodyMedia.csv"
            )
                .filter(
                    ::isPublishedMelodyMedia
                )
                .map(
                    mapper::toMelodyMedia
                )

        val existsInMedia =
            readRows(
                directory = directory,
                fileName = "ExistsInMedia.csv"
            )
                .filter(
                    ::isPublishedExistsInMedia
                )
                .map(
                    mapper::toExistsInMedia
                )

        val mediaTimingSets =
            readRows(
                directory = directory,
                fileName = "MediaTimingSet.csv"
            ).map(
                mapper::toMediaTimingSet
            )

        validateUniqueMediaAssetIds(
            mediaAssets
        )

        validateUniqueMelodyMediaIds(
            melodyMedia
        )

        validateUniqueExistsInMediaIds(
            existsInMedia
        )

        validateUniqueMediaTimingSetIds(
            mediaTimingSets
        )

        validateUniqueSourceRelativePaths(
            mediaAssets
        )

        validateMediaAssetReferences(
            mediaAssets = mediaAssets,
            melodyMedia = melodyMedia,
            existsInMedia = existsInMedia,
            mediaTimingSets = mediaTimingSets
        )

        validateTimingSetReferences(
            existsInMedia = existsInMedia,
            mediaTimingSets = mediaTimingSets
        )

        return MediaSourceData(
            mediaAssets = mediaAssets,
            melodyMedia = melodyMedia,
            existsInMedia = existsInMedia,
            mediaTimingSets = mediaTimingSets
        )
    }

    private fun isPublishedMelodyMedia(
        row: CsvRow
    ): Boolean {
        val publicationStatus =
            row["publicationStatus"]
                ?.trim()
                ?.takeIf {
                    it.isNotEmpty()
                }

        require(
            publicationStatus != null
        ) {
            "MelodyMedia ${row["MelodyMediaID"]} must have " +
                    "PublicationStatus."
        }

        require(
            publicationStatus in SUPPORTED_PUBLICATION_STATUSES
        ) {
            "MelodyMedia ${row["MelodyMediaID"]} has unsupported " +
                    "PublicationStatus '$publicationStatus'."
        }

        return publicationStatus == PUBLISHED_STATUS
    }

    private fun isPublishedExistsInMedia(
        row: CsvRow
    ): Boolean {
        val publicationStatus =
            row["publicationStatus"]
                ?.trim()
                ?.takeIf {
                    it.isNotEmpty()
                }

        require(
            publicationStatus != null
        ) {
            "ExistsInMedia ${row["ExistsInMediaID"]} must have " +
                    "PublicationStatus."
        }

        require(
            publicationStatus in SUPPORTED_PUBLICATION_STATUSES
        ) {
            "ExistsInMedia ${row["ExistsInMediaID"]} has unsupported " +
                    "PublicationStatus '$publicationStatus'."
        }

        return publicationStatus == PUBLISHED_STATUS
    }

    private fun readRows(
        directory: Path,
        fileName: String
    ): List<CsvRow> {
        val path =
            directory.resolve(fileName)

        require(
            Files.isRegularFile(path)
        ) {
            "Required Media source file was not found: $path"
        }

        return csvReader
            .read(path)
            .rows
    }

    private fun validateUniqueMediaAssetIds(
        mediaAssets: List<MediaAssetSource>
    ) {
        val duplicateIds =
            mediaAssets
                .groupingBy { it.id }
                .eachCount()
                .filterValues { it > 1 }
                .keys
                .sorted()

        require(
            duplicateIds.isEmpty()
        ) {
            "MediaAsset.csv contains duplicate MediaAssetID values: " +
                    duplicateIds.joinToString()
        }
    }

    private fun validateUniqueMelodyMediaIds(
        melodyMedia: List<MelodyMediaSource>
    ) {
        val duplicateIds =
            melodyMedia
                .groupingBy { it.id }
                .eachCount()
                .filterValues { it > 1 }
                .keys
                .sorted()

        require(
            duplicateIds.isEmpty()
        ) {
            "MelodyMedia.csv contains duplicate MelodyMediaID values: " +
                    duplicateIds.joinToString()
        }
    }

    private fun validateUniqueExistsInMediaIds(
        existsInMedia: List<ExistsInMediaSource>,
        mediaTimingSets: List<MediaTimingSetSource>
    ) {
        val duplicateIds =
            existsInMedia
                .groupingBy { it.id }
                .eachCount()
                .filterValues { it > 1 }
                .keys
                .sorted()

        require(
            duplicateIds.isEmpty()
        ) {
            "ExistsInMedia.csv contains duplicate ExistsInMediaID values: " +
                    duplicateIds.joinToString()
        }
    }

    private fun validateUniqueMediaTimingSetIds(
        mediaTimingSets: List<MediaTimingSetSource>
    ) {
        val duplicateIds =
            mediaTimingSets
                .groupingBy { it.id }
                .eachCount()
                .filterValues { it > 1 }
                .keys
                .sorted()

        require(
            duplicateIds.isEmpty()
        ) {
            "MediaTimingSet.csv contains duplicate MediaTimingSetID values: " +
                    duplicateIds.joinToString()
        }
    }

    private fun validateUniqueSourceRelativePaths(
        mediaAssets: List<MediaAssetSource>
    ) {
        val duplicatePaths =
            mediaAssets
                .groupingBy {
                    it.sourceRelativePath
                        .lowercase()
                }
                .eachCount()
                .filterValues { it > 1 }
                .keys
                .sorted()

        require(
            duplicatePaths.isEmpty()
        ) {
            "MediaAsset.csv contains duplicate SourceRelativePath " +
                    "values: ${duplicatePaths.joinToString()}"
        }
    }

    private companion object {
        const val PUBLISHED_STATUS =
            "PUBLISHED"

        val SUPPORTED_PUBLICATION_STATUSES =
            setOf(
                PUBLISHED_STATUS,
                "ARCHIVE"
            )
    }

    private fun validateMediaAssetReferences(
        mediaAssets: List<MediaAssetSource>,
        melodyMedia: List<MelodyMediaSource>,
        existsInMedia: List<ExistsInMediaSource>
    ) {
        val mediaAssetIds =
            mediaAssets
                .mapTo(mutableSetOf()) {
                    it.id
                }

        val missingMelodyMediaIds =
            melodyMedia
                .map { it.mediaAssetId }
                .filterNot {
                    it in mediaAssetIds
                }
                .distinct()
                .sorted()

        require(
            missingMelodyMediaIds.isEmpty()
        ) {
            "MelodyMedia.csv references missing MediaAssetID values: " +
                    missingMelodyMediaIds.joinToString()
        }

        val missingExistsInMediaIds =
            existsInMedia
                .map { it.mediaAssetId }
                .filterNot {
                    it in mediaAssetIds
                }
                .distinct()
                .sorted()

        require(
            missingExistsInMediaIds.isEmpty()
        ) {
            "ExistsInMedia.csv references missing MediaAssetID values: " +
                    missingExistsInMediaIds.joinToString()
        }

        val missingTimingSetAssetIds =
            mediaTimingSets
                .map { it.mediaAssetId }
                .filterNot {
                    it in mediaAssetIds
                }
                .distinct()
                .sorted()

        require(
            missingTimingSetAssetIds.isEmpty()
        ) {
            "MediaTimingSet.csv references missing MediaAssetID values: " +
                    missingTimingSetAssetIds.joinToString()
        }
    }

    private fun validateTimingSetReferences(
        existsInMedia: List<ExistsInMediaSource>,
        mediaTimingSets: List<MediaTimingSetSource>
    ) {
        val timingSetIds =
            mediaTimingSets
                .mapTo(mutableSetOf()) {
                    it.id
                }

        val missingTimingSetIds =
            existsInMedia
                .mapNotNull {
                    it.mediaTimingSetId
                }
                .filterNot {
                    it in timingSetIds
                }
                .distinct()
                .sorted()

        require(
            missingTimingSetIds.isEmpty()
        ) {
            "ExistsInMedia.csv references missing MediaTimingSetID values: " +
                    missingTimingSetIds.joinToString()
        }
    }
}