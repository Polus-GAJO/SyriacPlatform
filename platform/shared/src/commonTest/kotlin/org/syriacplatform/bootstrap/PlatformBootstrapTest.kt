package org.syriacplatform.bootstrap

import kotlin.test.Test
import kotlin.test.assertEquals
import org.syriacplatform.common.result.Result
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.RuntimeState
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.navigation.AppDestination

class PlatformBootstrapTest {

    @Test
    fun createReturnsInitializedContentService() {
        val platform = PlatformBootstrap.create()

        assertEquals(
            RuntimeState.Ready,
            platform.content.runtimeState
        )

        val qoloResult =
            platform.content.loadQolo(QoloId(1))

        val qolo = when (qoloResult) {
            is Result.Success<Qolo> -> qoloResult.data
            is Result.Failure ->
                error(qoloResult.error.message ?: "Qolo loading failed")
        }

        assertEquals(
            "ܩܳܠܳܐ ܢܽܘܗܪܳܢܳܐ",
            qolo.name
        )
    }

    @Test
    fun createReturnsInitializedNavigationService() {
        val platform = PlatformBootstrap.create()

        assertEquals(
            RuntimeState.Ready,
            platform.navigation.runtimeState
        )

        assertEquals(
            AppDestination.HOME,
            platform.navigation.state.value.currentDestination
        )

        platform.navigation.navigateTo(
            AppDestination.QOLO_DETAILS
        )

        assertEquals(
            AppDestination.QOLO_DETAILS,
            platform.navigation.state.value.currentDestination
        )
    }
}