package org.syriacplatform.content.runtime

import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.PerformanceMedia

/**
 * Resolve recorded performances by the exact liturgical occurrence.
 * A shared Qolo or Melody must never imply a shared performance recording.
 */
class LiturgicalPerformanceResolver(private val store: RuntimeContentStore) {
    data class Recording(
        val performance: PerformanceMedia,
        val asset: MediaAsset
    )

    fun recordingsFor(itemId: LiturgicalItemId): List<Recording> =
        store.index.performanceMediaByLiturgicalItemId[itemId]
            .orEmpty()
            .sortedWith(compareBy<PerformanceMedia> { it.sort }.thenBy { it.id.value })
            .mapNotNull { performance ->
                store.index.mediaAssetsById[performance.mediaAssetId]?.let { asset ->
                    Recording(performance, asset)
                }
            }
}
