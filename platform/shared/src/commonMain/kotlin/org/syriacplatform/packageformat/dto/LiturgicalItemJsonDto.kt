package org.syriacplatform.packageformat.dto

import kotlinx.serialization.Serializable

/**
 * البنية الفيزيائية لسجل داخل liturgical-items.json.
 *
 * petgomoId خاص بظهور Text داخل هذا الموضع.
 */
@Serializable
internal data class LiturgicalItemJsonDto(
    val id: Long,
    val type: String,
    val targetId: Long,
    val effectiveMelodyId: Long? = null,
    val petgomoId: Long? = null
)