package org.syriacplatform.buildtools.schema

import org.syriacplatform.buildtools.source.MediaSourceData
import org.syriacplatform.buildtools.source.models.MediaAssetSource
import org.syriacplatform.buildtools.source.models.ExistsInMediaSource
import org.syriacplatform.buildtools.source.models.MelodyMediaSource
import org.syriacplatform.buildtools.source.models.MediaTimingSetSource

class SchemaV1MediaMapper {

    fun map(
        source: MediaSourceData
    ): SchemaV1CanonicalMedia {
        val mediaAssets =
            source.mediaAssets
                .map(::mapMediaAsset)

        val melodyMedia =
            source.melodyMedia
                .map(::mapMelodyMedia)

        val performanceMedia =
            source.existsInMedia
                .filter { it.role == PERFORMANCE_ROLE }
                .map(::mapPerformanceMedia)

        val mediaTimingSets =
            source.mediaTimingSets
                .map(::mapMediaTimingSet)

        validateCanonicalReferences(
            mediaAssets = mediaAssets,
            melodyMedia = melodyMedia,
            performanceMedia = performanceMedia,
            mediaTimingSets = mediaTimingSets
        )

        return SchemaV1CanonicalMedia(
            mediaAssets = mediaAssets,
            melodyMedia = melodyMedia,
            performanceMedia = performanceMedia,
            mediaTimingSets = mediaTimingSets
        )
    }

    private fun mapMediaAsset(
        source: MediaAssetSource
    ): SchemaV1MediaAsset {
        require(source.id > 0L) {
            "MediaAsset ${source.id} must have a positive id."
        }

        return SchemaV1MediaAsset(
            id = source.id,
            mediaType = source.mediaType,
            sourceRelativePath =
                source.sourceRelativePath,
            performer = source.performer
        )
    }

    private fun mapMelodyMedia(
        source: MelodyMediaSource
    ): SchemaV1MelodyMedia {
        require(source.id > 0L) {
            "MelodyMedia ${source.id} must have a positive id."
        }

        require(source.melodyId > 0L) {
            "MelodyMedia ${source.id} must reference a positive Melody id."
        }

        require(source.mediaAssetId > 0L) {
            "MelodyMedia ${source.id} must reference a positive MediaAsset id."
        }

        return SchemaV1MelodyMedia(
            id = source.id,
            melodyId = source.melodyId,
            mediaAssetId = source.mediaAssetId,
            role = source.role,
            sort = source.sort
        )
    }

    private fun mapPerformanceMedia(
        source: ExistsInMediaSource
    ): SchemaV1PerformanceMedia {
        require(source.id > 0L) {
            "ExistsInMedia ${source.id} must have a positive id."
        }

        require(source.existsInId > 0L) {
            "ExistsInMedia ${source.id} must reference a positive LiturgicalItem id."
        }

        require(source.mediaAssetId > 0L) {
            "ExistsInMedia ${source.id} must reference a positive MediaAsset id."
        }

        return SchemaV1PerformanceMedia(
            id = source.id,
            liturgicalItemId = source.existsInId,
            mediaAssetId = source.mediaAssetId,
            role = source.role,
            sort = source.sort,
            mediaTimingSetId = source.mediaTimingSetId
        )
    }

    private fun mapMediaTimingSet(
        source: MediaTimingSetSource
    ): SchemaV1MediaTimingSet {
        require(source.id > 0L) {
            "MediaTimingSet ${source.id} must have a positive id."
        }

        require(source.mediaAssetId > 0L) {
            "MediaTimingSet ${source.id} must reference a positive MediaAsset id."
        }

        return SchemaV1MediaTimingSet(
            id = source.id,
            mediaAssetId = source.mediaAssetId,
            name = source.name
        )
    }

    private fun validateCanonicalReferences(
        mediaAssets: List<SchemaV1MediaAsset>,
        melodyMedia: List<SchemaV1MelodyMedia>,
        performanceMedia: List<SchemaV1PerformanceMedia>,
        mediaTimingSets: List<SchemaV1MediaTimingSet>
    ) {
        val mediaAssetIds =
            mediaAssets
                .mapTo(mutableSetOf()) {
                    it.id
                }

        val missingMediaAssetIds =
            melodyMedia
                .map {
                    it.mediaAssetId
                }
                .filterNot {
                    it in mediaAssetIds
                }
                .distinct()
                .sorted()

        require(
            missingMediaAssetIds.isEmpty()
        ) {
            "Canonical MelodyMedia references missing MediaAsset ids: " +
                    missingMediaAssetIds.joinToString()
        }

        val missingTimingSetMediaAssetIds =
            mediaTimingSets
                .map { it.mediaAssetId }
                .filterNot { it in mediaAssetIds }
                .distinct()
                .sorted()

        require(
            missingTimingSetMediaAssetIds.isEmpty()
        ) {
            "Canonical MediaTimingSet references missing MediaAsset ids: " +
                    missingTimingSetMediaAssetIds.joinToString()
        }

        val timingSetIds =
            mediaTimingSets
                .mapTo(mutableSetOf()) { it.id }

        val missingPerformanceTimingSetIds =
            performanceMedia
                .mapNotNull { it.mediaTimingSetId }
                .filterNot { it in timingSetIds }
                .distinct()
                .sorted()

        require(
            missingPerformanceTimingSetIds.isEmpty()
        ) {
            "Canonical PERFORMANCE media references missing MediaTimingSet ids: " +
                    missingPerformanceTimingSetIds.joinToString()
        }

        val missingPerformanceMediaAssetIds =
            performanceMedia
                .map { it.mediaAssetId }
                .filterNot { it in mediaAssetIds }
                .distinct()
                .sorted()

        require(
            missingPerformanceMediaAssetIds.isEmpty()
        ) {
            "Canonical PERFORMANCE media references missing MediaAsset ids: " +
                    missingPerformanceMediaAssetIds.joinToString()
        }
    }

    private companion object {
        const val PERFORMANCE_ROLE = "PERFORMANCE"
    }
}
