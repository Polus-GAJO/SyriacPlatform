package org.syriacplatform.audio

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import org.syriacplatform.audio.models.MediaResource
import org.syriacplatform.audio.models.PlaybackState
import org.syriacplatform.audio.models.PlaybackStatus
import org.syriacplatform.common.types.MediaAssetId

class AudioDomainContractsTest {

    @Test
    fun defaultPlaybackStateIsIdleAndEmpty() {
        val state =
            PlaybackState()

        assertEquals(
            PlaybackStatus.Idle,
            state.status
        )
        assertNull(
            state.mediaAssetId
        )
        assertEquals(
            0L,
            state.positionMs
        )
        assertNull(
            state.durationMs
        )
    }

    @Test
    fun mediaResourceKeepsCanonicalMediaIdentity() {
        val resource =
            MediaResource(
                mediaAssetId =
                    MediaAssetId(466L),
                uri =
                    "package://media/audio/media-000466.mp4"
            )

        assertEquals(
            MediaAssetId(466L),
            resource.mediaAssetId
        )
        assertEquals(
            "package://media/audio/media-000466.mp4",
            resource.uri
        )
    }
}
