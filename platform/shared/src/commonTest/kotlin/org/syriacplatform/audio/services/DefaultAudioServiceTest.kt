package org.syriacplatform.audio.services

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.syriacplatform.audio.contracts.MediaResourceResolver
import org.syriacplatform.audio.models.MediaResource
import org.syriacplatform.audio.models.PlaybackStatus
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.ErrorCode
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.PlatformError
import org.syriacplatform.common.types.RuntimeState
import org.syriacplatform.content.models.MediaAsset

class DefaultAudioServiceTest {

    @Test
    fun initializeThenLoadPlayPauseSeekStopFollowsCanonicalStateFlow() {
        val asset =
            mediaAsset(
                id = 293L
            )

        val service =
            DefaultAudioService(
                resourceResolver =
                    SuccessfulResolver()
            )

        assertEquals(
            RuntimeState.NotInitialized,
            service.runtimeState
        )
        assertEquals(
            PlaybackStatus.Idle,
            service.state.value.status
        )

        service.initialize()

        assertEquals(
            RuntimeState.Ready,
            service.runtimeState
        )

        assertIs<Result.Success<Unit>>(
            service.load(asset)
        )
        assertEquals(
            PlaybackStatus.Ready,
            service.state.value.status
        )
        assertEquals(
            asset.id,
            service.state.value.mediaAssetId
        )

        assertIs<Result.Success<Unit>>(
            service.play()
        )
        assertEquals(
            PlaybackStatus.Playing,
            service.state.value.status
        )

        assertIs<Result.Success<Unit>>(
            service.seekTo(12_345L)
        )
        assertEquals(
            12_345L,
            service.state.value.positionMs
        )
        assertEquals(
            PlaybackStatus.Playing,
            service.state.value.status
        )

        assertIs<Result.Success<Unit>>(
            service.pause()
        )
        assertEquals(
            PlaybackStatus.Paused,
            service.state.value.status
        )

        assertIs<Result.Success<Unit>>(
            service.stop()
        )
        assertEquals(
            PlaybackStatus.Idle,
            service.state.value.status
        )
        assertEquals(
            null,
            service.state.value.mediaAssetId
        )
    }

    @Test
    fun operationsBeforeInitializeFailWithoutChangingPlaybackState() {
        val service =
            DefaultAudioService(
                resourceResolver =
                    SuccessfulResolver()
            )

        val result =
            assertIs<Result.Failure>(
                service.play()
            )

        assertEquals(
            ErrorCode.INVALID_STATE,
            result.error.code
        )
        assertEquals(
            PlaybackStatus.Idle,
            service.state.value.status
        )
    }

    @Test
    fun resolverFailureProducesPlaybackErrorButServiceRemainsReady() {
        val asset =
            mediaAsset(
                id = 466L,
                path =
                    "media/audio/melodies/media-000466.mp4"
            )

        val service =
            DefaultAudioService(
                resourceResolver =
                    FailingResolver()
            )

        service.initialize()

        val result =
            assertIs<Result.Failure>(
                service.load(asset)
            )

        assertEquals(
            ErrorCode.RESOURCE_UNAVAILABLE,
            result.error.code
        )
        assertEquals(
            RuntimeState.Ready,
            service.runtimeState
        )
        assertEquals(
            PlaybackStatus.Error,
            service.state.value.status
        )
        assertEquals(
            asset.id,
            service.state.value.mediaAssetId
        )
    }

    @Test
    fun negativeSeekIsRejectedAndCurrentStateIsPreserved() {
        val service =
            DefaultAudioService(
                resourceResolver =
                    SuccessfulResolver()
            )

        service.initialize()

        assertIs<Result.Success<Unit>>(
            service.load(
                mediaAsset(
                    id = 7L
                )
            )
        )

        val before =
            service.state.value

        val result =
            assertIs<Result.Failure>(
                service.seekTo(-1L)
            )

        assertEquals(
            ErrorCode.INVALID_ARGUMENT,
            result.error.code
        )
        assertEquals(
            before,
            service.state.value
        )
    }

    private fun mediaAsset(
        id: Long,
        path: String =
            "media/audio/melodies/media-$id.mp3"
    ): MediaAsset {
        return MediaAsset(
            id =
                MediaAssetId(id),
            type = "AUDIO",
            path = path
        )
    }

    private class SuccessfulResolver :
        MediaResourceResolver {

        override fun resolve(
            mediaAsset: MediaAsset
        ): Result<MediaResource> {
            return Result.Success(
                MediaResource(
                    mediaAssetId =
                        mediaAsset.id,
                    uri =
                        "package://${mediaAsset.path}"
                )
            )
        }
    }

    private class FailingResolver :
        MediaResourceResolver {

        override fun resolve(
            mediaAsset: MediaAsset
        ): Result<MediaResource> {
            return Result.Failure(
                PlatformError(
                    code =
                        ErrorCode.RESOURCE_UNAVAILABLE,
                    message =
                        "Media resource is unavailable."
                )
            )
        }
    }
}
