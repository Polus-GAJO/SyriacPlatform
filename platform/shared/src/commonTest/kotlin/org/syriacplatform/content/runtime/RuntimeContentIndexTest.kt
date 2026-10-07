package org.syriacplatform.content.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.syriacplatform.common.types.GroupId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.TextId
import org.syriacplatform.common.types.TextOccurrenceId
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MediaSegmentId
import org.syriacplatform.common.types.MediaTimingSetId
import org.syriacplatform.common.types.PerformanceMediaId
import org.syriacplatform.common.types.TextOccurrenceMediaSegmentId
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.content.models.TextOccurrence
import org.syriacplatform.content.models.MediaSegment
import org.syriacplatform.content.models.MediaTimingSet
import org.syriacplatform.content.models.PerformanceMedia
import org.syriacplatform.content.models.TextOccurrenceMediaSegment

class RuntimeContentIndexTest {

    @Test
    fun indexProvidesCanonicalLookupById() {
        val qolo =
            Qolo(
                id = QoloId(438),
                groupId = GroupId(12),
                sort = 500,
                name = "Qolo 438",
                searchName = "Qolo 438",
                poeticMeter = null
            )

        val content =
            RuntimeContent(
                entryPoints = emptyList(),
                occasions = emptyList(),
                prayers = emptyList(),
                prayerSequences = emptyList(),
                liturgicalItems = emptyList(),
                texts = emptyList(),
                petgomos = emptyList(),
                qolos = listOf(qolo),
                melodies = emptyList(),
                qintos = emptyList(),
                melodyQintoAssignments = emptyList()
            )

        val index =
            RuntimeContentIndex.from(
                content
            )

        assertEquals(
            qolo,
            index.qolosById[
                QoloId(438)
            ]
        )

        assertNull(
            index.qolosById[
                QoloId(999)
            ]
        )
    }

    @Test
    fun indexProvidesPerformanceTimingLookupsAndGroupings() {
        val performanceOne =
            PerformanceMedia(
                id = PerformanceMediaId(401),
                liturgicalItemId = LiturgicalItemId(101),
                mediaAssetId = MediaAssetId(301),
                role = "PERFORMANCE",
                sort = 1,
                mediaTimingSetId = MediaTimingSetId(701)
            )
        val performanceTwo =
            performanceOne.copy(
                id = PerformanceMediaId(402),
                sort = 2
            )
        val timingSet =
            MediaTimingSet(
                id = MediaTimingSetId(701),
                mediaAssetId = MediaAssetId(301),
                name = null
            )
        val segmentOne =
            MediaSegment(
                id = MediaSegmentId(801),
                mediaTimingSetId = MediaTimingSetId(701),
                sequence = 1,
                startMs = 0,
                endMs = 1000
            )
        val segmentTwo =
            segmentOne.copy(
                id = MediaSegmentId(802),
                sequence = 2,
                startMs = 1000,
                endMs = 2000
            )
        val relation =
            TextOccurrenceMediaSegment(
                id = TextOccurrenceMediaSegmentId(901),
                textOccurrenceId = TextOccurrenceId(501),
                mediaSegmentId = MediaSegmentId(801)
            )

        val content =
            RuntimeContent(
                entryPoints = emptyList(),
                occasions = emptyList(),
                prayers = emptyList(),
                prayerSequences = emptyList(),
                liturgicalItems = emptyList(),
                texts = emptyList(),
                petgomos = emptyList(),
                qolos = emptyList(),
                melodies = emptyList(),
                qintos = emptyList(),
                melodyQintoAssignments = emptyList(),
                performanceMedia =
                    listOf(performanceOne, performanceTwo),
                mediaTimingSets = listOf(timingSet),
                mediaSegments = listOf(segmentOne, segmentTwo),
                textOccurrenceMediaSegments = listOf(relation)
            )

        val index = RuntimeContentIndex.from(content)

        assertEquals(
            performanceOne,
            index.performanceMediaById[PerformanceMediaId(401)]
        )
        assertEquals(
            listOf(performanceOne, performanceTwo),
            index.performanceMediaByLiturgicalItemId[
                LiturgicalItemId(101)
            ]
        )
        assertEquals(
            timingSet,
            index.mediaTimingSetsById[MediaTimingSetId(701)]
        )
        assertEquals(
            segmentOne,
            index.mediaSegmentsById[MediaSegmentId(801)]
        )
        assertEquals(
            listOf(segmentOne, segmentTwo),
            index.mediaSegmentsByTimingSetId[MediaTimingSetId(701)]
        )
        assertEquals(
            listOf(relation),
            index.textOccurrenceMediaSegmentsByTextOccurrenceId[
                TextOccurrenceId(501)
            ]
        )
        assertEquals(
            listOf(relation),
            index.textOccurrenceMediaSegmentsByMediaSegmentId[
                MediaSegmentId(801)
            ]
        )
    }

}