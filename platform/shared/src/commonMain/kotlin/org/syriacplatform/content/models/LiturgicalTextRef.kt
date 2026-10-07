package org.syriacplatform.content.models

import org.syriacplatform.common.types.PetgomoId
import org.syriacplatform.common.types.TextId
import org.syriacplatform.common.types.TextOccurrenceId

/**
 * ظهور سياقي مستقل لنص قابل لإعادة الاستخدام داخل مكوّن ليتورجي.
 *
 * id يعرّف الظهور نفسه، بينما textId يشير إلى TextContent
 * القانوني القابل لإعادة الاستخدام.
 *
 * ترتيب TextOccurrence داخل القائمة هو ترتيب ظهور
 * الأبيات في هذا الاستعمال الليتورجي.
 *
 * يمكن أن يتكرر textId نفسه أكثر من مرة، لكن لكل ظهور
 * سياقي TextOccurrenceId مستقل.
 *
 * Petgomo مرتبط بهذا الظهور السياقي للبيت،
 * وليس بكيان TextContent نفسه.
 */
data class TextOccurrence(
    val id: TextOccurrenceId,
    val textId: TextId,
    val petgomoId: PetgomoId? = null
)

/*
 * Compatibility alias for code that still uses the pre-4C name.
 * The Core domain contract is now TextOccurrence.
 */
typealias LiturgicalTextRef = TextOccurrence
