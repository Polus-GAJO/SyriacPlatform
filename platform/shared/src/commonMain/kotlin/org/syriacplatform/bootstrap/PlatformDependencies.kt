package org.syriacplatform.bootstrap

import org.syriacplatform.audio.contracts.AudioPlayerBackend
import org.syriacplatform.content.repository.ContentRepository

/**
 * مجموعة الاعتمادات التي تحتاجها المنصة عند البناء.
 *
 * تسمح بإدخال الاعتمادات الحقيقية في التطبيق،
 * أو الاعتمادات البديلة في الاختبارات.
 */
data class PlatformDependencies(
    val contentRepository: ContentRepository,
    val audioPlayerBackend: AudioPlayerBackend? = null
)