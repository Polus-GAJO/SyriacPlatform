package org.syriacplatform.buildtools.schema

sealed interface MelodyResolution {

    data class Resolved(
        val melodyId: Long
    ) : MelodyResolution

    data class UnresolvedQinto(
        val qintoId: Long?
    ) : MelodyResolution

    data class NoCandidate(
        val qoloId: Long,
        val qintoId: Long
    ) : MelodyResolution

    data class Ambiguous(
        val qoloId: Long,
        val qintoId: Long,
        val melodyIds: List<Long>
    ) : MelodyResolution
}