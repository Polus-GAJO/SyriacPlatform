package org.syriacplatform.navigation.contracts

import kotlinx.coroutines.flow.StateFlow
import org.syriacplatform.kernel.PlatformService
import org.syriacplatform.navigation.AppDestination
import org.syriacplatform.navigation.NavigationState

/**
 * العقد الأساسي لإدارة حالة التنقل داخل المنصة.
 */
interface NavigationService : PlatformService {

    /**
     * حالة التنقل الحالية القابلة للمراقبة.
     */
    val state: StateFlow<NavigationState>

    /**
     * الانتقال إلى وجهة جديدة.
     */
    fun navigateTo(destination: AppDestination)
}