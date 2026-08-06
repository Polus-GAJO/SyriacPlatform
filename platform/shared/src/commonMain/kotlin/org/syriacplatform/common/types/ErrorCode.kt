package org.syriacplatform.common.types

/**
 * رموز الأخطاء المشتركة في SyriacPlatform.
 *
 * يجب أن تعبّر الرموز عن نوع الخطأ بصورة مستقرة،
 * بينما توضع التفاصيل الخاصة بكل حالة داخل PlatformError.message.
 */
enum class ErrorCode {

    // General
    INVALID_ID,
    INVALID_ARGUMENT,
    INVALID_STATE,
    UNKNOWN_ERROR,

    // Services
    SERVICE_NOT_REGISTERED,

    // Existing content operations
    CONTENT_NOT_FOUND,
    LOADING_FAILED,

    // Package access and loading
    PACKAGE_NOT_FOUND,
    PACKAGE_READ_FAILED,
    PACKAGE_PARSE_FAILED,
    PACKAGE_STRUCTURE_INVALID,

    // Package compatibility
    UNSUPPORTED_SCHEMA_VERSION,
    UNSUPPORTED_PACKAGE_PROFILE,
    UNSUPPORTED_PACKAGE_FEATURE,

    // Package data
    MISSING_REQUIRED_FIELD,
    MISSING_REQUIRED_COLLECTION,
    INVALID_PACKAGE_DATA,
    DUPLICATE_IDENTIFIER,
    INVALID_REFERENCE,
    UNEXPECTED_ENTITY_TYPE,

    // Runtime entity access
    ENTITY_NOT_FOUND,

    // Resources
    RESOURCE_NOT_FOUND,
    RESOURCE_UNAVAILABLE,
    UNSUPPORTED_RESOURCE_TYPE,

    // Platform operations
    STORAGE_ERROR,
    AUDIO_ERROR,
    PERMISSION_DENIED
}