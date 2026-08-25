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

/**
 * ظپظ‡ط§ط±ط³ ط§ظ„ظ‚ط±ط§ط،ط© ط§ظ„ط³ط±ظٹط¹ط© ظ„ظ„ظ…ط­طھظˆظ‰ ط§ظ„ظ‚ط§ظ†ظˆظ†ظٹ ط¯ط§ط®ظ„ Runtime.
 *
 * طھظڈط¨ظ†ظ‰ ط¨ط¹ط¯ Package ValidationطŒ ظˆظ„ط°ظ„ظƒ ظٹظ…ظƒظ† ط§ظ„ط§ط¹طھظ…ط§ط¯
 * ط¹ظ„ظ‰ uniqueness ط§ظ„ط®ط§طµط© ط¨ط§ظ„ظ€ canonical IDs.
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
    val petgomosById: Map<PetgomoId, Petgomo>,
    val qolosById: Map<QoloId, Qolo>,
    val melodiesById: Map<MelodyId, Melody>,
    val mediaAssetsById: Map<MediaAssetId, MediaAsset>,
    val qintosById: Map<QintoId, Qinto>,

    /**
     * MelodyQintoAssignment ظ„ط§ طھظ…ظ„ظƒ Canonical ID ظ…ط³طھظ‚ظ„ط©طŒ
     * ظ„ط°ظ„ظƒ ظ†ظپظ‡ط±ط³ ط§ظ„ط¹ظ„ط§ظ‚ط§طھ ط¨ط­ط³ط¨ ط·ط±ظپظٹظ‡ط§.
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
