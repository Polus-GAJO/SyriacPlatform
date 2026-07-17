package org.syriacplatform.navigation

import kotlin.test.Test
import kotlin.test.assertEquals
import org.syriacplatform.common.types.RuntimeState
import org.syriacplatform.navigation.services.DefaultNavigationService

class DefaultNavigationServiceTest {

    @Test
    fun initializesAndNavigates() {
        val service = DefaultNavigationService()

        assertEquals(
            RuntimeState.NotInitialized,
            service.runtimeState
        )

        assertEquals(
            AppDestination.HOME,
            service.state.value.currentDestination
        )

        service.initialize()

        assertEquals(
            RuntimeState.Ready,
            service.runtimeState
        )

        service.navigateTo(
            AppDestination.QOLO_DETAILS
        )

        assertEquals(
            AppDestination.QOLO_DETAILS,
            service.state.value.currentDestination
        )
    }
}