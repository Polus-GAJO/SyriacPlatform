package org.syriacplatform.buildtools.source.models

/** Access MelodyGroup: authored group of alternative melodies for a Qolo. */
data class MelodyGroupSource(
    val id: Long,
    val qoloId: Long,
    val qintoId: Long?,
    val name: String?
)

/** Access MelodyGroupMember: ordered reference to one canonical melody. */
data class MelodyGroupMemberSource(
    val id: Long,
    val melodyGroupId: Long,
    val melodyId: Long,
    val sort: Long
)
