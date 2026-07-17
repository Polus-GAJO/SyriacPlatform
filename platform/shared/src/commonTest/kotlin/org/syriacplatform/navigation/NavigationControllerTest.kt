package org.syriacplatform.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationControllerTest {

    @Test
    fun startsAtHomeByDefault() {
        val controller = NavigationController()

        assertEquals(
            AppDestination.HOME,
            controller.state.currentDestination
        )
    }

    @Test
    fun navigatesToQoloDetails() {
        val controller = NavigationController()

        controller.navigateTo(AppDestination.QOLO_DETAILS)

        assertEquals(
            AppDestination.QOLO_DETAILS,
            controller.state.currentDestination
        )
    }
}