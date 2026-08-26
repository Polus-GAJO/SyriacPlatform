package org.syriacplatform.content.runtime

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.Melody

class RuntimeMediaLookupTest {

    @Test
    fun resolvesMediaAssetById() {
        val asset =
            mediaAsset(
                id = 7L,
                path = "media/audio/media-000007.mp3"
            )

        val resolver =
            resolver(
                melodies = emptyList(),
                mediaAssets = listOf(asset)
            )

        val result =
            assertIs<Result.Success<MediaAsset>>(
                resolver.resolveMediaAsset(
                    MediaAssetId(7L)
                )
            )

        assertEquals(
            asset,
            result.data
        )
    }

    @Test
    fun resolvesMelodyRecordingsInCanonicalOrder() {
        val first =
            mediaAsset(
                id = 292L,
                type = "VIDEO",
                path = "media/video/media-000292.mp4"
            )

        val second =
            mediaAsset(
                id = 293L,
                type = "AUDIO",
                path = "media/audio/media-000293.mp3"
            )

        val melody =
            Melody(
                id = MelodyId(602L),
                qoloId = QoloId(1L),
                name = "Melody 602",
                searchName = "Melody 602",
                hasRecording = true,
                recordingIds =
                    listOf(
                        MediaAssetId(292L),
                        MediaAssetId(293L)
                    )
            )

        val resolver =
            resolver(
                melodies = listOf(melody),
                mediaAssets =
                    listOf(
                        second,
                        first
                    )
            )

        val result =
            assertIs<Result.Success<List<MediaAsset>>>(
                resolver.resolveMelodyRecordings(
                    MelodyId(602L)
                )
            )

        assertEquals(
            listOf(
                first,
                second
            ),
            result.data
        )
    }

    @Test
    fun missingMelodyReturnsFailure() {
        val resolver =
            resolver(
                melodies = emptyList(),
                mediaAssets = emptyList()
            )

        assertIs<Result.Failure>(
            resolver.resolveMelodyRecordings(
                MelodyId(999L)
            )
        )
    }

    private fun resolver(
        melodies: List<Melody>,
        mediaAssets: List<MediaAsset>
    ): RuntimeContentResolver {
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
                melodies = melodies,
                qintos = emptyList(),
                melodyQintoAssignments = emptyList(),
                mediaAssets = mediaAssets
            )

        return RuntimeContentResolver(
            store =
                RuntimeContentStore(
                    content = content,
                    index =
                        RuntimeContentIndex.from(
                            content
                        )
                )
        )
    }

    private fun mediaAsset(
        id: Long,
        type: String = "AUDIO",
        path: String
    ): MediaAsset {
        return MediaAsset(
            id = MediaAssetId(id),
            type = type,
            path = path
        )
    }
}