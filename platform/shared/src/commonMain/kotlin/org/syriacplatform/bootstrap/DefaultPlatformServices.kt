package org.syriacplatform.bootstrap

import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.services.DefaultContentService
import org.syriacplatform.navigation.contracts.NavigationService
import org.syriacplatform.navigation.services.DefaultNavigationService

/**
 * ينشئ خدمات المنصة الافتراضية باستعمال
 * الاعتمادات التي تم تجهيزها مسبقًا.
 */
object DefaultPlatformServices {

    fun create(
        dependencies: PlatformDependencies
    ): PlatformServices {
        val contentService: ContentService =
            DefaultContentService(
                repository = dependencies.contentRepository
            )

        val navigationService: NavigationService =
            DefaultNavigationService()

        return PlatformServices(
            content = contentService,
            navigation = navigationService
        )
    }
}