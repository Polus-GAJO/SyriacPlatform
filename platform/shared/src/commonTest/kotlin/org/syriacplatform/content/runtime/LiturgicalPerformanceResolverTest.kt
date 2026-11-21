package org.syriacplatform.content.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.PerformanceMediaId
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.PerformanceMedia

class LiturgicalPerformanceResolverTest {
    @Test
    fun recordingsRemainScopedToTheirExactLiturgicalItem() {
        val first = LiturgicalItemId(101L)
        val second = LiturgicalItemId(102L)
        val assetA = MediaAsset(MediaAssetId(1L), "audio", "media/first.mp3")
        val assetB = MediaAsset(MediaAssetId(2L), "audio", "media/second.mp3")
        val content = RuntimeContent(
            entryPoints = emptyList(), occasions = emptyList(), prayers = emptyList(),
            prayerSequences = emptyList(), liturgicalItems = emptyList(), texts = emptyList(),
            petgomos = emptyList(), qolos = emptyList(), melodies = emptyList(),
            qintos = emptyList(), melodyQintoAssignments = emptyList(),
            mediaAssets = listOf(assetA, assetB),
            performanceMedia = listOf(
                PerformanceMedia(PerformanceMediaId(1L), first, assetA.id, "performance", 1L),
                PerformanceMedia(PerformanceMediaId(2L), second, assetB.id, "performance", 1L)
            )
        )
        val resolver = LiturgicalPerformanceResolver(
            RuntimeContentStore(content, RuntimeContentIndex.from(content))
        )
        assertEquals(listOf("media/first.mp3"), resolver.recordingsFor(first).map { it.asset.path })
        assertEquals(listOf("media/second.mp3"), resolver.recordingsFor(second).map { it.asset.path })
        assertTrue(resolver.recordingsFor(LiturgicalItemId(103L)).isEmpty())
    }
}
