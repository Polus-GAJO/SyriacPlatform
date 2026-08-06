package org.syriacplatform.content.models

import org.syriacplatform.common.types.TextId

/**
 * يمثل بيتًا شعريًا واحدًا داخل المحتوى القانوني.
 *
 * TextContent هو أصغر وحدة نصية في النموذج.
 * لا يحتوي معلومات الموضع الليتورجي أو القولو أو الفتغام.
 */
data class TextContent(
    val id: TextId,
    val syriac: String,
    val translations: List<TextTranslation>
)