package org.syriacplatform.navigation

import kotlin.test.Test
import kotlin.test.assertEquals

class NavigationStateTest {

    @Test
    fun startsAtHome() {
        val state = NavigationState()

        assertEquals(
            AppDestination.HOME,
            state.currentDestination
        )
    }

    @Test
    fun canRepresentQoloDetails() {
        val state = NavigationState(
            currentDestination = AppDestination.QOLO_DETAILS
        )

        assertEquals(
            AppDestination.QOLO_DETAILS,
            state.currentDestination
        )
    }
}