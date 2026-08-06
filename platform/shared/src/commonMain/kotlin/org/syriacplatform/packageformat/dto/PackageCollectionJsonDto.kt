package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

/**
 * الغلاف القياسي لمجموعات JSON داخل Application Package.
 */
@Serializable
data class PackageCollectionJsonDto<T>(
    val items: List<T>
)