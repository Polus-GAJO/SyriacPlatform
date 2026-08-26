package org.syriacplatform.buildtools.schema

data class SchemaV1MediaAsset(
    val id: Long,
    val mediaType: String,
    val sourceRelativePath: String,
    val performer: String? = null
)

data class SchemaV1MelodyMedia(
    val id: Long,
    val melodyId: Long,
    val mediaAssetId: Long,
    val role: String,
    val sort: Long
)

data class SchemaV1CanonicalMedia(
    val mediaAssets: List<SchemaV1MediaAsset>,
    val melodyMedia: List<SchemaV1MelodyMedia>
) {
    fun recordingsForMelody(
        melodyId: Long
    ): List<SchemaV1MelodyMedia> {
        return melodyMedia
            .asSequence()
            .filter {
                it.melodyId == melodyId &&
                        it.role == RECORDING_ROLE
            }
            .sortedWith(
                compareBy<SchemaV1MelodyMedia> {
                    it.sort
                }.thenBy {
                    it.id
                }
            )
            .toList()
    }

    fun hasRecording(
        melodyId: Long
    ): Boolean {
        return recordingsForMelody(
            melodyId
        ).isNotEmpty()
    }

    private companion object {
        const val RECORDING_ROLE =
            "RECORDING"
    }
}