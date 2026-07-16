package org.syriacplatform.kernel

import org.syriacplatform.common.types.RuntimeState

/**
 * العقد الأساسي لجميع خدمات المنصة.
 */
interface PlatformService {

    /**
     * الاسم الرسمي للخدمة.
     */
    val metadata: ServiceMetadata

    /**
     * حالة تشغيل الخدمة.
     */
    val runtimeState: RuntimeState

    /**
     * تهيئة الخدمة.
     */
    fun initialize()
}