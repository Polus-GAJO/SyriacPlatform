package org.syriacplatform.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationControllerTest {

    @Test
    fun startsAtHomeByDefault() {
        val controller = NavigationController()

        assertEquals(
            AppDestination.HOME,
            controller.state.value.currentDestination
        )
    }

    @Test
    fun startsAtProvidedDestination() {
        val controller = NavigationController(
            initialDestination = AppDestination.QOLO_DETAILS
        )

        assertEquals(
            AppDestination.QOLO_DETAILS,
            controller.state.value.currentDestination
        )
    }

    @Test
    fun navigateToUpdatesCurrentDestination() {
        val controller = NavigationController()

        controller.navigateTo(
            AppDestination.QOLO_DETAILS
        )

        assertEquals(
            AppDestination.QOLO_DETAILS,
            controller.state.value.currentDestination
        )
    }
}