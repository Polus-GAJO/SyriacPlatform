package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

/**
 * البنية الفيزيائية لمرجع بيت نصي داخل
 * ظهور ترتيلة Qolo في LiturgicalItem.
 *
 * يحتفظ بالمعلومات السياقية الخاصة بهذا الظهور،
 * ولا يكرر محتوى TextContent نفسه.
 */
@Serializable
internal data class LiturgicalTextRefJsonDto(
    val textId: Long,
    val petgomoId: Long? = null
)