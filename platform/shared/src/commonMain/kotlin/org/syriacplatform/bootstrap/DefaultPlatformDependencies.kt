package org.syriacplatform.bootstrap

import org.syriacplatform.content.repository.JsonContentRepository

/**
 * ينشئ الاعتمادات الافتراضية المستخدمة
 * عند تشغيل المنصة داخل التطبيق الحقيقي.
 */
object DefaultPlatformDependencies {

    fun create(): PlatformDependencies {
        return PlatformDependencies(
            contentRepository = JsonContentRepository()
        )
    }
}