package org.syriacplatform.buildtools.schema

class SchemaV1PackageMediaSelector {

    fun select(
        canonicalMedia: SchemaV1CanonicalMedia,
        melodyIds: Set<Long>,
        liturgicalItems: List<SchemaV1LiturgicalItem> = emptyList()
    ): SchemaV1CanonicalMedia {
        val liturgicalItemIds =
            liturgicalItems.mapTo(mutableSetOf()) { it.id }

        val textOccurrenceIds =
            liturgicalItems
                .flatMap { it.verses }
                .mapTo(mutableSetOf()) { it.id }

        val selectedMelodyMedia =
            canonicalMedia.melodyMedia
                .filter {
                    it.melodyId in melodyIds &&
                            it.role == RECORDING_ROLE
                }
                .sortedWith(
                    compareBy<SchemaV1MelodyMedia> { it.melodyId }
                        .thenBy { it.sort }
                        .thenBy { it.id }
                )

        val selectedPerformanceMedia =
            canonicalMedia.performanceMedia
                .filter {
                    it.liturgicalItemId in liturgicalItemIds &&
                            it.role == PERFORMANCE_ROLE
                }
                .sortedWith(
                    compareBy<SchemaV1PerformanceMedia> { it.liturgicalItemId }
                        .thenBy { it.sort }
                        .thenBy { it.id }
                )

        val selectedTimingSetIds =
            selectedPerformanceMedia
                .mapNotNullTo(mutableSetOf()) { it.mediaTimingSetId }

        val selectedMediaTimingSets =
            canonicalMedia.mediaTimingSets
                .filter { it.id in selectedTimingSetIds }
                .sortedBy { it.id }

        require(selectedMediaTimingSets.size == selectedTimingSetIds.size) {
            "Package media selection could not resolve every referenced MediaTimingSet."
        }

        val selectedMediaSegments =
            canonicalMedia.mediaSegments
                .filter { it.mediaTimingSetId in selectedTimingSetIds }
                .sortedWith(
                    compareBy<SchemaV1MediaSegment> { it.mediaTimingSetId }
                        .thenBy { it.sequence }
                        .thenBy { it.id }
                )

        val selectedMediaSegmentIds =
            selectedMediaSegments.mapTo(mutableSetOf()) { it.id }

        val selectedTextOccurrenceMediaSegments =
            canonicalMedia.textOccurrenceMediaSegments
                .filter { it.mediaSegmentId in selectedMediaSegmentIds }
                .sortedWith(
                    compareBy<SchemaV1TextOccurrenceMediaSegment> { it.mediaSegmentId }
                        .thenBy { it.id }
                )

        val missingTextOccurrenceIds =
            selectedTextOccurrenceMediaSegments
                .map { it.textOccurrenceId }
                .filterNot { it in textOccurrenceIds }
                .distinct()
                .sorted()

        require(missingTextOccurrenceIds.isEmpty()) {
            "Selected PERFORMANCE timing references TextOccurrence ids " +
                    "not present in the package: " +
                    missingTextOccurrenceIds.joinToString()
        }

        val selectedMediaAssetIds =
            buildSet {
                selectedMelodyMedia.forEach { add(it.mediaAssetId) }
                selectedPerformanceMedia.forEach { add(it.mediaAssetId) }
                selectedMediaTimingSets.forEach { add(it.mediaAssetId) }
            }

        val selectedMediaAssets =
            canonicalMedia.mediaAssets
                .filter { it.id in selectedMediaAssetIds }
                .sortedBy { it.id }

        require(selectedMediaAssets.size == selectedMediaAssetIds.size) {
            "Package media selection could not resolve every referenced MediaAsset."
        }

        return SchemaV1CanonicalMedia(
            mediaAssets = selectedMediaAssets,
            melodyMedia = selectedMelodyMedia,
            performanceMedia = selectedPerformanceMedia,
            mediaTimingSets = selectedMediaTimingSets,
            mediaSegments = selectedMediaSegments,
            textOccurrenceMediaSegments = selectedTextOccurrenceMediaSegments
        )
    }

    private companion object {
        const val RECORDING_ROLE = "RECORDING"
        const val PERFORMANCE_ROLE = "PERFORMANCE"
    }
}
