package org.syriacplatform.content.models

import org.syriacplatform.common.types.PetgomoId
import org.syriacplatform.common.types.TextId

/**
 * مرجع سياقي لظهور بيت نصي داخل مكوّن ليتورجي.
 *
 * يشير إلى TextContent قانوني قابل لإعادة الاستخدام،
 * مع الاحتفاظ بالمعلومات الخاصة بهذا الظهور الليتورجي.
 *
 * ترتيب LiturgicalTextRef داخل القائمة هو ترتيب ظهور
 * الأبيات في هذا الاستعمال الليتورجي.
 *
 * يمكن أن يتكرر textId نفسه أكثر من مرة، لأن البيت
 * الواحد قد يظهر عدة مرات في الترتيلة نفسها.
 *
 * Petgomo مرتبط بهذا الظهور السياقي للبيت،
 * وليس بكيان TextContent نفسه.
 */
data class LiturgicalTextRef(
    val textId: TextId,
    val petgomoId: PetgomoId? = null
)