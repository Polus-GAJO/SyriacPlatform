package org.syriacplatform.buildtools.schema

import org.syriacplatform.buildtools.source.models.ExistsInSource
import org.syriacplatform.buildtools.source.models.MelodySource

class MelodyResolver {

    fun resolve(
        item: ExistsInSource,
        melodies: List<MelodySource>
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

        val candidates = melodies
            .filter {
                it.qoloId == qoloId &&
                        it.qintoId == qintoId
            }
            .sortedBy { it.id }

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