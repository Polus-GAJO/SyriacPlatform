package org.syriacplatform.packagevalidation.validators.references

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MediaSegmentId
import org.syriacplatform.common.types.MediaTimingSetId
import org.syriacplatform.common.types.PerformanceMediaId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.TextId
import org.syriacplatform.common.types.TextOccurrenceId
import org.syriacplatform.common.types.TextOccurrenceMediaSegmentId
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.LiturgicalItemTarget
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.MediaSegment
import org.syriacplatform.content.models.MediaTimingSet
import org.syriacplatform.content.models.PerformanceMedia
import org.syriacplatform.content.models.TextOccurrence
import org.syriacplatform.content.models.TextOccurrenceMediaSegment
import org.syriacplatform.packagevalidation.PackageValidationTestFixture.packageWith
import org.syriacplatform.packagevalidation.ValidationSeverity
import org.syriacplatform.packagevalidation.validators.ReferenceValidator

class PerformanceTimingReferenceRuleTest {

    @Test
    fun rejectsAllBrokenPerformanceTimingReferences() {
        val packageData =
            packageWith(
                performanceMedia = listOf(
                    PerformanceMedia(
                        id = PerformanceMediaId(401),
                        liturgicalItemId = LiturgicalItemId(999),
                        mediaAssetId = MediaAssetId(998),
                        role = "PERFORMANCE",
                        sort = 1,
                        mediaTimingSetId = MediaTimingSetId(997)
                    )
                ),
                mediaTimingSets = listOf(
                    MediaTimingSet(
                        id = MediaTimingSetId(701),
                        mediaAssetId = MediaAssetId(996),
                        name = null
                    )
                ),
                mediaSegments = listOf(
                    MediaSegment(
                        id = MediaSegmentId(801),
                        mediaTimingSetId = MediaTimingSetId(995),
                        sequence = 1,
                        startMs = null,
                        endMs = 1250
                    )
                ),
                textOccurrenceMediaSegments = listOf(
                    TextOccurrenceMediaSegment(
                        id = TextOccurrenceMediaSegmentId(901),
                        textOccurrenceId = TextOccurrenceId(994),
                        mediaSegmentId = MediaSegmentId(993)
                    )
                )
            )

        val issues = ReferenceValidator().validate(packageData)

        val locations =
            setOf(
                "performanceMedia[401].liturgicalItemId",
                "performanceMedia[401].mediaAssetId",
                "performanceMedia[401].mediaTimingSetId",
                "mediaTimingSets[701].mediaAssetId",
                "mediaSegments[801].mediaTimingSetId",
                "textOccurrenceMediaSegments[901].textOccurrenceId",
                "textOccurrenceMediaSegments[901].mediaSegmentId"
            )

        assertEquals(locations, issues.map { it.location }.toSet())
        assertEquals(7, issues.size)
        assertTrue(
            issues.all { it.severity == ValidationSeverity.FATAL }
        )
        assertTrue(
            issues.all { it.code == ErrorCode.INVALID_REFERENCE }
        )
    }

    @Test
    fun acceptsValidPerformanceTimingReferences() {
        val mediaAsset =
            MediaAsset(
                id = MediaAssetId(301),
                type = "AUDIO",
                path = "media/performance.mp3",
                performer = null
            )
        val occurrence =
            TextOccurrence(
                id = TextOccurrenceId(501),
                textId = TextId(1001)
            )

        val packageData =
            packageWith(
                liturgicalItems = listOf(
                    LiturgicalItem(
                        id = LiturgicalItemId(101),
                        target =
                            LiturgicalItemTarget.Qolo(
                                qoloId = QoloId(201),
                                verses = listOf(occurrence)
                            )
                    )
                ),
                mediaAssets = listOf(mediaAsset),
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
                        startMs = 0,
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

        val issues = ReferenceValidator().validate(packageData)

        assertTrue(
            issues.none { issue ->
                issue.location.startsWith("performanceMedia") ||
                    issue.location.startsWith("mediaTimingSets") ||
                    issue.location.startsWith("mediaSegments") ||
                    issue.location.startsWith("textOccurrenceMediaSegments")
            }
        )
    }
}
