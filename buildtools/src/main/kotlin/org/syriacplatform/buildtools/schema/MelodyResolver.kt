package org.syriacplatform.buildtools.schema

import org.syriacplatform.buildtools.source.models.ExistsInSource
import org.syriacplatform.buildtools.source.models.MelodySource
import org.syriacplatform.buildtools.source.models.MelodyGroupSource
import org.syriacplatform.buildtools.source.models.MelodyGroupMemberSource

class MelodyResolver {

    fun resolve(
        item: ExistsInSource,
        melodies: List<MelodySource>,
        groups: List<MelodyGroupSource> = emptyList(),
        members: List<MelodyGroupMemberSource> = emptyList(),
        enableWeeklyQinto: Boolean = false
    ): MelodyResolution {
        val qoloId = requireNotNull(item.qoloId) {
            "ExistsIn ${item.id} has no QoloN and cannot " +
                    "participate in Qolo melody resolution."
        }

        val qintoId = item.qintoId

        if (
            qintoId == null ||
            qintoId <= 0L
        ) {
            return MelodyResolution.UnresolvedQinto(
                qintoId = qintoId
            )
        }

        val weekly = enableWeeklyQinto && qintoId == 22L
        val eligible = melodies.filter {
            it.qoloId == qoloId &&
                if (weekly) it.qintoId in (1L..8L) else it.qintoId == qintoId
        }
        val candidates = if (item.melodyGroupId != null && enableWeeklyQinto) {
            val group = requireNotNull(groups.find { it.id == item.melodyGroupId }) {
                "Missing MelodyGroup for ExistsIn ${item.id}."
            }
            require(group.qoloId == qoloId) { "MelodyGroup Qolo mismatch for ExistsIn ${item.id}." }
            val byId = eligible.associateBy { it.id }
            members.filter { it.melodyGroupId == group.id }
                .sortedWith(compareBy<MelodyGroupMemberSource> { it.sort }.thenBy { it.id })
                .map { requireNotNull(byId[it.melodyId]) { "Incompatible MelodyGroup member ${it.melodyId}." } }
        } else {
            eligible.sortedWith(compareBy<MelodySource> { if (weekly) it.qintoId else null }.thenBy { it.id })
        }

        if (weekly && candidates.isNotEmpty()) {
            return MelodyResolution.Ambiguous(qoloId, qintoId, candidates.map { it.id })
        }

        return when (candidates.size) {
            0 -> {
                MelodyResolution.NoCandidate(
                    qoloId = qoloId,
                    qintoId = qintoId
                )
            }

            1 -> {
                MelodyResolution.Resolved(
                    melodyId = candidates.single().id
                )
            }

            else -> {
                MelodyResolution.Ambiguous(
                    qoloId = qoloId,
                    qintoId = qintoId,
                    melodyIds = candidates.map { it.id }
                )
            }
        }
    }
}