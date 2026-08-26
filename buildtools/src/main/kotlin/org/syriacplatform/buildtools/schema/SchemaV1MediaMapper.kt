package org.syriacplatform.buildtools.schema

import org.syriacplatform.buildtools.source.MediaSourceData
import org.syriacplatform.buildtools.source.models.MediaAssetSource
import org.syriacplatform.buildtools.source.models.MelodyMediaSource

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

        validateCanonicalReferences(
            mediaAssets = mediaAssets,
            melodyMedia = melodyMedia
        )

        return SchemaV1CanonicalMedia(
            mediaAssets = mediaAssets,
            melodyMedia = melodyMedia
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

    private fun validateCanonicalReferences(
        mediaAssets: List<SchemaV1MediaAsset>,
        melodyMedia: List<SchemaV1MelodyMedia>
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
    }
}