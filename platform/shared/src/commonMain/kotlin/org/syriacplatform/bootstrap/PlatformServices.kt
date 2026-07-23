package org.syriacplatform.bootstrap

import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.navigation.contracts.NavigationService

/**
 * مجموعة خدمات المنصة الجاهزة للتسجيل في النواة.
 *
 * لا تنشئ هذه الفئة الخدمات، بل تجمعها فقط
 * في بنية واضحة تمرر إلى نقطة بناء المنصة.
 */
data class PlatformServices(
    val content: ContentService,
    val navigation: NavigationService
)