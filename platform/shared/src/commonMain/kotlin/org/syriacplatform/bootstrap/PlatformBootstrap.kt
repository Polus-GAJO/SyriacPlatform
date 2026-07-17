package org.syriacplatform.bootstrap

import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.services.DefaultContentService
import org.syriacplatform.kernel.PlatformKernel

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

        kernel.initialize()

        return kernel
    }
}