package org.syriacplatform.content.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MediaSegmentId
import org.syriacplatform.common.types.MediaTimingSetId
import org.syriacplatform.common.types.PerformanceMediaId
import org.syriacplatform.common.types.TextOccurrenceId
import org.syriacplatform.common.types.TextOccurrenceMediaSegmentId
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.models.MediaSegment
import org.syriacplatform.content.models.MediaTimingSet
import org.syriacplatform.content.models.PerformanceMedia
import org.syriacplatform.content.models.TextOccurrenceMediaSegment
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith

class RuntimeContentTest {

    @Test
    fun runtimeContentIsBuiltFromValidatedPackageContent() {
        val packageData =
            packageWith(
                qolos = listOf(
                    Qolo(
                        id = QoloId(438),
                        groupId =
                            org.syriacplatform.common.types.GroupId(12),
                        sort = 500,
                        name = "Qolo 438",
                        searchName = "Qolo 438",
                        poeticMeter = null
                    )
                )
            )

        val content =
            RuntimeContent.from(
                packageData
            )

        assertEquals(
            1,
            content.qolos.size
        )

        assertEquals(
            QoloId(438),
            content.qolos.single().id
        )
    }

    @Test
    fun runtimeContentCarriesPerformanceTimingCollections() {
        val packageData =
            packageWith(
                performanceMedia = listOf(
                    PerformanceMedia(
                        id = PerformanceMediaId(401),
                        liturgicalItemId = LiturgicalItemId(101),
                        mediaAssetId = MediaAssetId(301),
                        role = "PERFORMANCE",
                        sort = 1,
                        mediaTimingSetId = MediaTimingSetId(701)
                    )
                ),
                mediaTimingSets = listOf(
                    MediaTimingSet(
                        id = MediaTimingSetId(701),
                        mediaAssetId = MediaAssetId(301),
                        name = null
                    )
                ),
                mediaSegments = listOf(
                    MediaSegment(
                        id = MediaSegmentId(801),
                        mediaTimingSetId = MediaTimingSetId(701),
                        sequence = 1,
                        startMs = null,
                        endMs = 1250
                    )
                ),
                textOccurrenceMediaSegments = listOf(
                    TextOccurrenceMediaSegment(
                        id = TextOccurrenceMediaSegmentId(901),
                        textOccurrenceId = TextOccurrenceId(501),
                        mediaSegmentId = MediaSegmentId(801)
                    )
                )
            )

        val content = RuntimeContent.from(packageData)

        assertEquals(
            PerformanceMediaId(401),
            content.performanceMedia.single().id
        )
        assertEquals(
            MediaTimingSetId(701),
            content.mediaTimingSets.single().id
        )
        assertEquals(
            MediaSegmentId(801),
            content.mediaSegments.single().id
        )
        assertEquals(
            TextOccurrenceMediaSegmentId(901),
            content.textOccurrenceMediaSegments.single().id
        )
    }

}