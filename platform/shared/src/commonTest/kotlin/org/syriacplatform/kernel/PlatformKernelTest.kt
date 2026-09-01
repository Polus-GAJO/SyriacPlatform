package org.syriacplatform.kernel

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.QoloId

import org.syriacplatform.common.types.RuntimeState
import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.repository.FakeContentRepository
import org.syriacplatform.content.services.DefaultContentService

class PlatformKernelTest {

    @Test
    fun registerInitializeAndResolveContentService() = runTest {
        val kernel = PlatformKernel()

        val contentService = DefaultContentService(
            repository = FakeContentRepository()
        )

        val registration = kernel.registerService(
            ContentService::class,
            contentService
        )

        assertIs<Result.Success<*>>(registration)

        kernel.initialize()

        val resolved =
            kernel.resolveService(ContentService::class)

        val success =
            assertIs<Result.Success<*>>(resolved)

        val service =
            success.data as ContentService

        val qoloResult =
            service.loadQolo(QoloId(1))

        val qoloSuccess =
            assertIs<Result.Success<*>>(qoloResult)

        val qolo =
            qoloSuccess.data as Qolo

        assertEquals(
            "ܩܳܠܳܐ ܢܽܘܗܪܳܢܳܐ",
            qolo.name
        )
    }

    @Test
    fun shutdownIsForwardedToRegisteredServices() {
        val kernel =
            PlatformKernel()

        val service =
            RecordingPlatformService()

        kernel.registerService(
            RecordingPlatformService::class,
            service
        )

        kernel.initialize()

        assertEquals(
            RuntimeState.Ready,
            service.runtimeState
        )

        kernel.shutdown()

        assertEquals(
            RuntimeState.NotInitialized,
            service.runtimeState
        )
        assertEquals(
            1,
            service.shutdownCount
        )
    }

    private class RecordingPlatformService :
        PlatformService {

        override val metadata =
            ServiceMetadata(
                name = "Recording Service",
                version = "1.0"
            )

        override var runtimeState =
            RuntimeState.NotInitialized
            private set

        var shutdownCount =
            0
            private set

        override fun initialize() {
            runtimeState =
                RuntimeState.Ready
        }

        override fun shutdown() {
            shutdownCount +=
                1

            runtimeState =
                RuntimeState.NotInitialized
        }
    }
}