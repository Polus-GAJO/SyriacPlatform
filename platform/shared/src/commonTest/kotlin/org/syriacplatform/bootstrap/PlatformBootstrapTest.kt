package org.syriacplatform.bootstrap

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.RuntimeState
import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.navigation.AppDestination
import org.syriacplatform.navigation.contracts.NavigationService

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
            RuntimeState.Ready,
            contentService.runtimeState
        )

        assertEquals(
            "ܩܳܠܳܐ ܢܽܘܗܪܳܢܳܐ",
            qolo.name
        )
    }

    @Test
    fun createReturnsInitializedNavigationService() {
        val kernel = PlatformBootstrap.create()

        val resolved = kernel.resolveService(NavigationService::class)
        val resolvedSuccess = assertIs<Result.Success<*>>(resolved)
        val navigationService =
            resolvedSuccess.data as NavigationService

        assertEquals(
            RuntimeState.Ready,
            navigationService.runtimeState
        )

        assertEquals(
            AppDestination.HOME,
            navigationService.state.currentDestination
        )

        navigationService.navigateTo(
            AppDestination.QOLO_DETAILS
        )

        assertEquals(
            AppDestination.QOLO_DETAILS,
            navigationService.state.currentDestination
        )
    }
}