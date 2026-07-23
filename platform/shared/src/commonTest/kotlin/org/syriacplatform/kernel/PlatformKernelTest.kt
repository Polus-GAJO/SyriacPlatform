package org.syriacplatform.kernel

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.QoloId
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
}