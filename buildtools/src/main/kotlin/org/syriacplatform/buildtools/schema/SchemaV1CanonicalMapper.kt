package org.syriacplatform.buildtools.schema

import org.syriacplatform.buildtools.source.AuthorSourceData
import org.syriacplatform.buildtools.source.models.MelodySource
import org.syriacplatform.buildtools.source.models.PetgomoSource
import org.syriacplatform.buildtools.source.models.PrayerSource
import org.syriacplatform.buildtools.source.models.QintoSource
import org.syriacplatform.buildtools.source.models.QoloSource
import org.syriacplatform.buildtools.source.models.TextSource

class SchemaV1CanonicalMapper {

    fun map(
        source: AuthorSourceData
    ): SchemaV1CanonicalContent {
        return mapInternal(
            source = source,
            media = null
        )
    }

    fun map(
        source: AuthorSourceData,
        media: SchemaV1CanonicalMedia
    ): SchemaV1CanonicalContent {
        return mapInternal(
            source = source,
            media = media
        )
    }

    private fun mapInternal(
        source: AuthorSourceData,
        media: SchemaV1CanonicalMedia?
    ): SchemaV1CanonicalContent {
        return SchemaV1CanonicalContent(
            prayers = source.prayers
                .map(::mapPrayer),

            qolos = source.qolos
                .filter { it.id > 0L }
                .map(::mapQolo),

            texts = source.texts
                .map(::mapText),

            petgomos = source.petgomos
                .map(::mapPetgomo),

            qintos = source.qintos
                .filter { it.id > 0L }
                .map(::mapQinto),

            melodies = source.melodies
                .map { melody ->
                    mapMelody(
                        source = melody,
                        recordingIds = media
                            ?.recordingsForMelody(
                                melody.id
                            )
                            ?.map {
                                it.mediaAssetId
                            }
                    )
                }
        )
    }

    fun mapPrayer(
        source: PrayerSource
    ): SchemaV1Prayer {
        return SchemaV1Prayer(
            id = source.id,
            name = source.name.required(
                entity = "Prayer",
                id = source.id,
                field = "Prayer"
            )
        )
    }

    fun mapQolo(
        source: QoloSource
    ): SchemaV1Qolo {
        return SchemaV1Qolo(
            id = source.id,

            groupId = source.groupId.required(
                entity = "Qolo",
                id = source.id,
                field = "GroupN"
            ),

            sort = source.sort.required(
                entity = "Qolo",
                id = source.id,
                field = "Sort"
            ),

            name = source.name.required(
                entity = "Qolo",
                id = source.id,
                field = "Qolo"
            ),

            searchName = source.searchName.required(
                entity = "Qolo",
                id = source.id,
                field = "QoloSerch"
            ),

            poeticMeter = source.poeticMeter
        )
    }

    fun mapText(
        source: TextSource
    ): SchemaV1Text {
        return SchemaV1Text(
            id = source.id,

            syriac = source.syriac.required(
                entity = "Text",
                id = source.id,
                field = "TheText"
            ),

            translations = emptyList()
        )
    }

    fun mapPetgomo(
        source: PetgomoSource
    ): SchemaV1Petgomo {
        return SchemaV1Petgomo(
            id = source.id,

            syriac = source.syriac.required(
                entity = "Petgomo",
                id = source.id,
                field = "Petgomo"
            ),

            translations = emptyList()
        )
    }

    fun mapQinto(
        source: QintoSource
    ): SchemaV1Qinto {
        require(source.id > 0L) {
            "Qinto ${source.id} is an unresolved authoring " +
                    "placeholder and cannot become a Schema v1 Qinto."
        }

        return SchemaV1Qinto(
            id = source.id,

            name = source.name.required(
                entity = "Qinto",
                id = source.id,
                field = "Qinto"
            )
        )
    }

    fun mapMelody(
        source: MelodySource,
        recordingIds: List<Long>? = null
    ): SchemaV1Melody {
        val resolvedRecordingIds =
            recordingIds
                ?: emptyList()

        return SchemaV1Melody(
            id = source.id,

            qoloId = source.qoloId.required(
                entity = "Melody",
                id = source.id,
                field = "QoloN"
            ),

            name = source.name.required(
                entity = "Melody",
                id = source.id,
                field = "Melody"
            ),

            searchName = source.searchName
                ?: source.name.required(
                    entity = "Melody",
                    id = source.id,
                    field = "Melody"
                ),

            hasRecording =
                if (recordingIds != null) {
                    resolvedRecordingIds
                        .isNotEmpty()
                } else {
                    source.hasRecording
                        ?: false
                },

            recordingIds =
                resolvedRecordingIds
        )
    }

    private fun <T : Any> T?.required(
        entity: String,
        id: Long,
        field: String
    ): T {
        return this ?: error(
            "$entity $id cannot be mapped to Schema v1: " +
                    "required source field '$field' is missing."
        )
    }
}