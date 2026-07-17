package org.syriacplatform.navigation

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * يدير حالة التنقل الداخلية للمنصة.
 */
class NavigationController(
    initialDestination: AppDestination = AppDestination.HOME
) {

    private val _state = MutableStateFlow(
        NavigationState(
            currentDestination = initialDestination
        )
    )

    /**
     * حالة التنقل الحالية القابلة للمراقبة.
     */
    val state: StateFlow<NavigationState> =
        _state.asStateFlow()

    /**
     * الانتقال إلى وجهة جديدة.
     */
    fun navigateTo(destination: AppDestination) {
        _state.value = NavigationState(
            currentDestination = destination
        )
    }
}