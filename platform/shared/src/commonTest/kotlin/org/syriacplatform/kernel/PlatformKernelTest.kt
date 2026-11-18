package org.syriacplatform.kernel

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertSame
import kotlin.test.assertFailsWith
import org.syriacplatform.common.types.ErrorCode
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

    @Test
    fun duplicateRegistrationIsRejectedAndOriginalServiceRemainsRegistered() {
        val kernel = PlatformKernel()
        val original = RecordingPlatformService()
        val replacement = RecordingPlatformService()

        assertIs<Result.Success<*>>(
            kernel.registerService(RecordingPlatformService::class, original)
        )

        val duplicate = assertIs<Result.Failure>(
            kernel.registerService(RecordingPlatformService::class, replacement)
        )
        assertEquals(ErrorCode.INVALID_STATE, duplicate.error.code)

        val resolved = assertIs<Result.Success<*>>(
            kernel.resolveService(RecordingPlatformService::class)
        )
        assertSame(original, resolved.data)

        kernel.initialize()
        assertEquals(RuntimeState.Ready, original.runtimeState)
        assertEquals(RuntimeState.NotInitialized, replacement.runtimeState)
    }

    @Test
    fun failedInitializationRollsBackPreviouslyInitializedServices() {
        val events = mutableListOf<String>()
        val kernel = PlatformKernel()
        kernel.registerService(FirstLifecycleService::class, FirstLifecycleService(events))
        kernel.registerService(FailingLifecycleService::class, FailingLifecycleService(events))

        assertFailsWith<IllegalStateException> { kernel.initialize() }

        assertEquals(
            listOf("first:init", "failing:init", "failing:shutdown", "first:shutdown"),
            events
        )
    }

    @Test
    fun shutdownReleasesServicesInReverseRegistrationOrder() {
        val events = mutableListOf<String>()
        val kernel = PlatformKernel()
        kernel.registerService(FirstLifecycleService::class, FirstLifecycleService(events))
        kernel.registerService(SecondLifecycleService::class, SecondLifecycleService(events))

        kernel.initialize()
        kernel.shutdown()

        assertEquals(
            listOf("first:init", "second:init", "second:shutdown", "first:shutdown"),
            events
        )
    }

    private class FirstLifecycleService(private val events: MutableList<String>) : PlatformService {
        override val metadata = ServiceMetadata("First", "1.0")
        override var runtimeState = RuntimeState.NotInitialized
            private set

        override fun initialize() {
            events.add("first:init")
            runtimeState = RuntimeState.Ready
        }

        override fun shutdown() {
            events.add("first:shutdown")
            runtimeState = RuntimeState.NotInitialized
        }
    }

    private class SecondLifecycleService(private val events: MutableList<String>) : PlatformService {
        override val metadata = ServiceMetadata("Second", "1.0")
        override var runtimeState = RuntimeState.NotInitialized
            private set

        override fun initialize() {
            events.add("second:init")
            runtimeState = RuntimeState.Ready
        }

        override fun shutdown() {
            events.add("second:shutdown")
            runtimeState = RuntimeState.NotInitialized
        }
    }

    private class FailingLifecycleService(private val events: MutableList<String>) : PlatformService {
        override val metadata = ServiceMetadata("Failing", "1.0")
        override val runtimeState = RuntimeState.Error

        override fun initialize() {
            events.add("failing:init")
            throw IllegalStateException("Simulated initialization failure")
        }

        override fun shutdown() {
            events.add("failing:shutdown")
        }
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