package org.syriacplatform.buildtools.schema

class SchemaV1PackageMediaSelector {

    fun select(
        canonicalMedia: SchemaV1CanonicalMedia,
        melodyIds: Set<Long>
    ): SchemaV1CanonicalMedia {
        if (melodyIds.isEmpty()) {
            return SchemaV1CanonicalMedia(
                mediaAssets = emptyList(),
                melodyMedia = emptyList()
            )
        }

        val selectedMelodyMedia =
            canonicalMedia.melodyMedia
                .filter {
                    it.melodyId in melodyIds &&
                            it.role == RECORDING_ROLE
                }
                .sortedWith(
                    compareBy<SchemaV1MelodyMedia> {
                        it.melodyId
                    }.thenBy {
                        it.sort
                    }.thenBy {
                        it.id
                    }
                )

        val selectedMediaAssetIds =
            selectedMelodyMedia
                .mapTo(mutableSetOf()) {
                    it.mediaAssetId
                }

        val selectedMediaAssets =
            canonicalMedia.mediaAssets
                .filter {
                    it.id in selectedMediaAssetIds
                }
                .sortedBy {
                    it.id
                }

        require(
            selectedMediaAssets.size ==
                    selectedMediaAssetIds.size
        ) {
            "Package media selection could not resolve every " +
                    "referenced MediaAsset."
        }

        return SchemaV1CanonicalMedia(
            mediaAssets = selectedMediaAssets,
            melodyMedia = selectedMelodyMedia
        )
    }

    private companion object {
        const val RECORDING_ROLE =
            "RECORDING"
    }
}