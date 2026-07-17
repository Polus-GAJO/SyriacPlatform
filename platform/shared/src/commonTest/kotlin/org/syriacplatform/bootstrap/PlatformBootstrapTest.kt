package org.syriacplatform.bootstrap

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.models.Qolo

class PlatformBootstrapTest {

    @Test
    fun createReturnsInitializedPlatform() {
        val kernel = PlatformBootstrap.create()

        val resolved = kernel.resolveService(ContentService::class)
        val resolvedSuccess = assertIs<Result.Success<*>>(resolved)
        val contentService = resolvedSuccess.data as ContentService

        val qoloResult = contentService.loadQolo(QoloId(1))
        val qoloSuccess = assertIs<Result.Success<*>>(qoloResult)
        val qolo = qoloSuccess.data as Qolo

        assertEquals(
            "ܩܳܠܳܐ ܢܽܘܗܪܳܢܳܐ",
            qolo.name
        )
    }
}