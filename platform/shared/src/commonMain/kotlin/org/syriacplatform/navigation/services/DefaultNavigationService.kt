package org.syriacplatform.navigation.services

import org.syriacplatform.common.types.RuntimeState
import org.syriacplatform.kernel.ServiceMetadata
import org.syriacplatform.navigation.AppDestination
import org.syriacplatform.navigation.NavigationController
import org.syriacplatform.navigation.NavigationState
import org.syriacplatform.navigation.contracts.NavigationService

/**
 * التنفيذ الافتراضي لخدمة التنقل.
 */
class DefaultNavigationService(
    private val controller: NavigationController = NavigationController()
) : NavigationService {

    override val metadata = ServiceMetadata(
        name = "Navigation Service",
        version = "1.0"
    )

    override var runtimeState = RuntimeState.NotInitialized
        private set

    override val state: NavigationState
        get() = controller.state

    override fun initialize() {
        runtimeState = RuntimeState.Ready
    }

    override fun navigateTo(destination: AppDestination) {
        controller.navigateTo(destination)
    }
}