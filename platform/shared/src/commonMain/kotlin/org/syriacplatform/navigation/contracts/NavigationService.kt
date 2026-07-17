package org.syriacplatform.navigation.contracts

import org.syriacplatform.kernel.PlatformService
import org.syriacplatform.navigation.AppDestination
import org.syriacplatform.navigation.NavigationState

/**
 * العقد الأساسي لإدارة حالة التنقل داخل المنصة.
 */
interface NavigationService : PlatformService {

    /**
     * حالة التنقل الحالية.
     */
    val state: NavigationState

    /**
     * الانتقال إلى وجهة جديدة.
     */
    fun navigateTo(destination: AppDestination)
}