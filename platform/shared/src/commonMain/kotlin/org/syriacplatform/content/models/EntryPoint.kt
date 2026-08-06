package org.syriacplatform.content.models

import org.syriacplatform.common.types.EntryPointId

/**
 * نقطة دخول منطقية إلى محتوى Application Package.
 */
data class EntryPoint(
    val id: EntryPointId,
    val name: String,
    val target: EntryPointTarget,
    val isDefault: Boolean
)