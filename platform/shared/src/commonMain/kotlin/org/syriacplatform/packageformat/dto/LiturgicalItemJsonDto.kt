package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

/**
 * البنية الفيزيائية لسجل داخل liturgical-items.json.
 *
 * للنوع text:
 * - targetId يشير إلى TextContent.
 * - petgomoId خاص بهذا الظهور النصي.
 *
 * للنوع qolo:
 * - targetId يشير إلى Qolo.
 * - effectiveMelodyId يحدد اللحن الفعلي.
 * - verses تحفظ الأبيات المرتبة لهذا الظهور الليتورجي.
 */
@Serializable
internal data class LiturgicalItemJsonDto(
    val id: Long,
    val type: String,
    val targetId: Long,
    val effectiveMelodyId: Long? = null,
    val petgomoId: Long? = null,
    val verses: List<LiturgicalTextRefJsonDto> = emptyList()
)