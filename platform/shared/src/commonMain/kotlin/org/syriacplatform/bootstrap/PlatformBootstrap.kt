package org.syriacplatform.bootstrap

import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.services.DefaultContentService
import org.syriacplatform.kernel.PlatformKernel
import org.syriacplatform.navigation.contracts.NavigationService
import org.syriacplatform.navigation.services.DefaultNavigationService

/**
 * نقطة البناء المركزية لمنصة SyriacPlatform.
 *
 * تنشئ النواة، وتسجل خدمات المنصة، ثم تهيئها قبل إعادتها
 * إلى التطبيق المستهلك.
 */
object PlatformBootstrap {

    fun create(): PlatformKernel {
        val kernel = PlatformKernel()

        kernel.registerService(
            ContentService::class,
            DefaultContentService()
        )

        kernel.registerService(
            NavigationService::class,
            DefaultNavigationService()
        )

        kernel.initialize()

        return kernel
    }
}