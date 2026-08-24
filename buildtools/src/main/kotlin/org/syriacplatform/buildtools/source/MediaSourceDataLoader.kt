package org.syriacplatform.buildtools.source

import java.nio.file.Files
import java.nio.file.Path
import org.syriacplatform.buildtools.source.models.MediaAssetSource
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
            ).map(
                mapper::toMelodyMedia
            )

        validateUniqueMediaAssetIds(
            mediaAssets
        )

        validateUniqueMelodyMediaIds(
            melodyMedia
        )

        validateUniqueSourceRelativePaths(
            mediaAssets
        )

        validateMediaAssetReferences(
            mediaAssets = mediaAssets,
            melodyMedia = melodyMedia
        )

        return MediaSourceData(
            mediaAssets = mediaAssets,
            melodyMedia = melodyMedia
        )
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

    private fun validateMediaAssetReferences(
        mediaAssets: List<MediaAssetSource>,
        melodyMedia: List<MelodyMediaSource>
    ) {
        val mediaAssetIds =
            mediaAssets
                .mapTo(mutableSetOf()) {
                    it.id
                }

        val missingIds =
            melodyMedia
                .map { it.mediaAssetId }
                .filterNot {
                    it in mediaAssetIds
                }
                .distinct()
                .sorted()

        require(
            missingIds.isEmpty()
        ) {
            "MelodyMedia.csv references missing MediaAssetID values: " +
                    missingIds.joinToString()
        }
    }
}