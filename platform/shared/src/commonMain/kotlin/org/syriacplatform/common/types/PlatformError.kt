package org.syriacplatform.common.types

/**
 * وصف موحد للخطأ مع إمكانية إضافة تفاصيل مفيدة.
 */
data class PlatformError(
    val code: ErrorCode,
    val message: String? = null,
    val cause: Throwable? = null
)

