package org.syriacplatform.navigation

class NavigationController(
    initialDestination: AppDestination = AppDestination.HOME
) {

    var state: NavigationState = NavigationState(
        currentDestination = initialDestination
    )
        private set

    fun navigateTo(destination: AppDestination) {
        state = NavigationState(
            currentDestination = destination
        )
    }
}