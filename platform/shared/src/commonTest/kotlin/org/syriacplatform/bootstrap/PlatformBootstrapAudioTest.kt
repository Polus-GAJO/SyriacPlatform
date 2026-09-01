package org.syriacplatform.bootstrap

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.syriacplatform.audio.contracts.AudioPlayerBackend
import org.syriacplatform.audio.models.AudioPlayerEvent
import org.syriacplatform.audio.models.MediaResource
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.RuntimeState
import org.syriacplatform.content.repository.FakeContentRepository

class PlatformBootstrapAudioTest {

    @Test
    fun audioBackendCreatesInitializedPlatformAudioService() {
        val backend =
            RecordingAudioBackend()

        val platform =
            PlatformBootstrap.create(
                dependencies =
                    PlatformDependencies(
                        contentRepository =
                            FakeContentRepository(),
                        audioPlayerBackend =
                            backend
                    )
            )

        val audio =
            assertNotNull(
                platform.audio
            )

        assertEquals(
            RuntimeState.Ready,
            audio.runtimeState
        )

        platform.shutdown()

        assertEquals(
            RuntimeState.NotInitialized,
            audio.runtimeState
        )
        assertEquals(
            1,
            backend.releaseCount
        )

        platform.shutdown()

        assertEquals(
            1,
            backend.releaseCount
        )
    }

    private class RecordingAudioBackend :
        AudioPlayerBackend {

        var releaseCount =
            0
            private set

        private var listener:
            ((AudioPlayerEvent) -> Unit)? =
            null

        override fun setEventListener(
            listener: ((AudioPlayerEvent) -> Unit)?
        ) {
            this.listener =
                listener
        }

        override fun prepare(
            resource: MediaResource
        ): Result<Unit> =
            Result.Success(Unit)

        override fun play(): Result<Unit> =
            Result.Success(Unit)

        override fun pause(): Result<Unit> =
            Result.Success(Unit)

        override fun stop(): Result<Unit> =
            Result.Success(Unit)

        override fun seekTo(
            positionMs: Long
        ): Result<Unit> =
            Result.Success(Unit)

        override fun release() {
            releaseCount +=
                1

            listener =
                null
        }
    }
}