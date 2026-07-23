package org.syriacplatform.bootstrap

import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.context.PlatformContext
import org.syriacplatform.kernel.PlatformKernel
import org.syriacplatform.navigation.contracts.NavigationService

/**
 * نقطة البناء المركزية لمنصة SyriacPlatform.
 *
 * تجهز خدمات المنصة، وتسجلها في النواة،
 * وتهيئها، ثم تعيد واجهة المنصة الرسمية
 * إلى التطبيق المستهلك.
 */
object PlatformBootstrap {

    fun create(
        dependencies: PlatformDependencies =
            DefaultPlatformDependencies.create()
    ): PlatformContext {
        val services =
            DefaultPlatformServices.create(
                dependencies = dependencies
            )

        return createPlatform(
            services = services
        )
    }

    private fun createPlatform(
        services: PlatformServices
    ): PlatformContext {
        val kernel = PlatformKernel()

        kernel.registerService(
            ContentService::class,
            services.content
        )

        kernel.registerService(
            NavigationService::class,
            services.navigation
        )

        kernel.initialize()

        return PlatformContext(
            kernel = kernel,
            content = services.content,
            navigation = services.navigation
        )
    }
}