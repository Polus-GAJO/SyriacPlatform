package org.syriacplatform.bootstrap

import org.syriacplatform.audio.PerformanceTextOccurrencePlaybackController
import org.syriacplatform.audio.contracts.AudioService
import org.syriacplatform.audio.resources.ComposeResourceMediaResourceResolver
import org.syriacplatform.audio.services.DefaultAudioService
import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.content.services.DefaultContentService
import org.syriacplatform.navigation.contracts.NavigationService
import org.syriacplatform.navigation.services.DefaultNavigationService
import org.syriacplatform.user.contracts.PreferencesService
import org.syriacplatform.user.services.DefaultPreferencesService

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

        val audioService: AudioService? =
            dependencies.audioPlayerBackend?.let { backend ->
                DefaultAudioService(
                    resourceResolver =
                        ComposeResourceMediaResourceResolver(),
                    playerBackend =
                        backend
                )
            }

        val performanceTextPlayback =
            audioService?.let { audio ->
                PerformanceTextOccurrencePlaybackController(
                    contentService = contentService,
                    audioService = audio
                )
            }

        val preferencesService: PreferencesService =
            DefaultPreferencesService(dependencies.userPreferencesStore)

        return PlatformServices(
            content = contentService,
            navigation = navigationService,
            preferences = preferencesService,
            audio = audioService,
            performanceTextPlayback = performanceTextPlayback
        )
    }
}