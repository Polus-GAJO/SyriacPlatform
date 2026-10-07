package org.syriacplatform.content.runtime

import org.syriacplatform.common.types.EntryPointId
import org.syriacplatform.common.types.LiturgicalItemId
import org.syriacplatform.common.types.MediaAssetId
import org.syriacplatform.common.types.MelodyId
import org.syriacplatform.common.types.OccasionId
import org.syriacplatform.common.types.PetgomoId
import org.syriacplatform.common.types.PrayerId
import org.syriacplatform.common.types.PrayerSequenceId
import org.syriacplatform.common.types.QintoId
import org.syriacplatform.common.types.QoloId
import org.syriacplatform.common.types.TextId
import org.syriacplatform.common.types.TextOccurrenceId
import org.syriacplatform.content.models.EntryPoint
import org.syriacplatform.content.models.LiturgicalItem
import org.syriacplatform.content.models.MediaAsset
import org.syriacplatform.content.models.Melody
import org.syriacplatform.content.models.MelodyQintoAssignment
import org.syriacplatform.content.models.Occasion
import org.syriacplatform.content.models.Petgomo
import org.syriacplatform.content.models.Prayer
import org.syriacplatform.content.models.PrayerSequence
import org.syriacplatform.content.models.Qinto
import org.syriacplatform.content.models.Qolo
import org.syriacplatform.content.models.TextContent
import org.syriacplatform.content.models.TextOccurrence
import org.syriacplatform.content.models.LiturgicalItemTarget

/**
 * فهارس القراءة السريعة للمحتوى القانوني داخل Runtime.
 *
 * تُبنى بعد Package Validation، ولذلك يمكن الاعتماد
 * على uniqueness الخاصة بالـ canonical IDs.
 */
class RuntimeContentIndex private constructor(
    val entryPointsById: Map<EntryPointId, EntryPoint>,
    val occasionsById: Map<OccasionId, Occasion>,
    val prayersById: Map<PrayerId, Prayer>,
    val prayerSequencesById:
    Map<PrayerSequenceId, PrayerSequence>,
    val liturgicalItemsById:
    Map<LiturgicalItemId, LiturgicalItem>,
    val textsById: Map<TextId, TextContent>,
    val textOccurrencesById:
    Map<TextOccurrenceId, TextOccurrence>,
    val petgomosById: Map<PetgomoId, Petgomo>,
    val qolosById: Map<QoloId, Qolo>,
    val melodiesById: Map<MelodyId, Melody>,
    val mediaAssetsById: Map<MediaAssetId, MediaAsset>,
    val qintosById: Map<QintoId, Qinto>,

    /**
     * MelodyQintoAssignment لا تملك Canonical ID مستقلة،
     * لذلك نفهرس العلاقات بحسب طرفيها.
     */
    val melodyQintoAssignmentsByMelodyId:
    Map<MelodyId, List<MelodyQintoAssignment>>,

    val melodyQintoAssignmentsByQintoId:
    Map<QintoId, List<MelodyQintoAssignment>>
) {

    companion object {

        fun from(
            content: RuntimeContent
        ): RuntimeContentIndex {
            return RuntimeContentIndex(
                entryPointsById =
                    content.entryPoints.associateBy { it.id },

                occasionsById =
                    content.occasions.associateBy { it.id },

                prayersById =
                    content.prayers.associateBy { it.id },

                prayerSequencesById =
                    content.prayerSequences.associateBy { it.id },

                liturgicalItemsById =
                    content.liturgicalItems.associateBy { it.id },

                textsById =
                    content.texts.associateBy { it.id },

                textOccurrencesById =
                    content.liturgicalItems
                        .flatMap { item ->
                            when (val target = item.target) {
                                is LiturgicalItemTarget.Qolo ->
                                    target.verses

                                is LiturgicalItemTarget.UnresolvedQolo ->
                                    target.verses

                                is LiturgicalItemTarget.Text ->
                                    emptyList()
                            }
                        }
                        .associateBy { occurrence ->
                            occurrence.id
                        },

                petgomosById =
                    content.petgomos.associateBy { it.id },

                qolosById =
                    content.qolos.associateBy { it.id },

                melodiesById =
                    content.melodies.associateBy { it.id },

                mediaAssetsById =
                    content.mediaAssets.associateBy { it.id },

                qintosById =
                    content.qintos.associateBy { it.id },

                melodyQintoAssignmentsByMelodyId =
                    content
                        .melodyQintoAssignments
                        .groupBy { it.melodyId },

                melodyQintoAssignmentsByQintoId =
                    content
                        .melodyQintoAssignments
                        .groupBy { it.qintoId }
            )
        }
    }
}
