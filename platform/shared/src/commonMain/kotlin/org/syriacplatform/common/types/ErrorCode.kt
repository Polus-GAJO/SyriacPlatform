package org.syriacplatform.common.types

/**
 * رموز الأخطاء المشتركة في المنصة.
 * ستُضاف رموز متخصصة عند ظهور حاجة عملية لها.
 */
enum class ErrorCode {
    INVALID_ID,
    CONTENT_NOT_FOUND,
    SERVICE_NOT_REGISTERED,
    LOADING_FAILED,
    STORAGE_ERROR,
    AUDIO_ERROR,
    PERMISSION_DENIED,
    UNKNOWN_ERROR
}