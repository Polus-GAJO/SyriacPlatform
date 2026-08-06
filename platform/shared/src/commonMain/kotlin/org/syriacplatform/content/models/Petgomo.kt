package org.syriacplatform.content.models

import org.syriacplatform.common.types.PetgomoId

/**
 * يمثل فتغامًا قانونيًا مستقلًا وقابلًا لإعادة الاستعمال.
 *
 * لا يُضمّن الفتغام داخل البيت، بل يُربط بظهور
 * البيت داخل عنصر ليتورجي معيّن.
 */
data class Petgomo(
    val id: PetgomoId,
    val syriac: String,
    val translations: List<TextTranslation>
)