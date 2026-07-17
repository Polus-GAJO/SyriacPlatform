package org.syriacplatform.bootstrap

import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.services.DefaultContentService
import org.syriacplatform.context.PlatformContext
import org.syriacplatform.kernel.PlatformKernel
import org.syriacplatform.navigation.contracts.NavigationService
import org.syriacplatform.navigation.services.DefaultNavigationService

/**
 * نقطة البناء المركزية لمنصة SyriacPlatform.
 *
 * تنشئ النواة، وتسجل الخدمات، وتهيئها، ثم تعيد
 * واجهة المنصة الرسمية إلى التطبيق المستهلك.
 */
object PlatformBootstrap {

    fun create(): PlatformContext {
        val kernel = PlatformKernel()

        val contentService: ContentService =
            DefaultContentService()

        val navigationService: NavigationService =
            DefaultNavigationService()

        kernel.registerService(
            ContentService::class,
            contentService
        )

        kernel.registerService(
            NavigationService::class,
            navigationService
        )

        kernel.initialize()

        return PlatformContext(
            kernel = kernel,
            content = contentService,
            navigation = navigationService
        )
    }
}