package org.syriacplatform.bootstrap

import org.syriacplatform.audio.PerformanceTextOccurrencePlaybackController
import org.syriacplatform.audio.contracts.AudioService
import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.navigation.contracts.NavigationService
import org.syriacplatform.user.contracts.PreferencesService

/**
 * مجموعة خدمات المنصة الجاهزة للتسجيل في النواة.
 *
 * لا تنشئ هذه الفئة الخدمات، بل تجمعها فقط
 * في بنية واضحة تمرر إلى نقطة بناء المنصة.
 */
data class PlatformServices(
    val content: ContentService,
    val navigation: NavigationService,
    val preferences: PreferencesService,
    val audio: AudioService? = null,
    val performanceTextPlayback: PerformanceTextOccurrencePlaybackController? = null
)