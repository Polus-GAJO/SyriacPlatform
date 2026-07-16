package org.syriacplatform.common.types

/**
 * يمثل الحالة الحالية لأي خدمة داخل المنصة.
 */
enum class RuntimeState {

    NotInitialized,

    Initializing,

    Ready,

    Busy,

    Error
}