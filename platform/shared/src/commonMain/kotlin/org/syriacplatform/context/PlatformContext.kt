package org.syriacplatform.context

import org.syriacplatform.content.contracts.ContentService
import org.syriacplatform.kernel.PlatformKernel
import org.syriacplatform.navigation.contracts.NavigationService

/**
 * الواجهة الرسمية التي تتعامل معها التطبيقات المستهلكة للمنصة.
 *
 * تخفي تفاصيل النواة وسجل الخدمات، وتعرض خدمات المنصة
 * من خلال خصائص واضحة وعالية المستوى.
 */
class PlatformContext internal constructor(
    internal val kernel: PlatformKernel,
    val content: ContentService,
    val navigation: NavigationService
)