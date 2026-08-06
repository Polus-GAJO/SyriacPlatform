package org.syriacplatform.content.models

/**
 * ترجمة واحدة مرتبطة ببيت شعري أو فتغام.
 *
 * تمثل هذه الترجمة محتوى ليتورجيًا، وليست نصًا
 * خاصًا بواجهة المستخدم أو Localization Service.
 */
data class TextTranslation(
    val language: String,
    val content: String
)