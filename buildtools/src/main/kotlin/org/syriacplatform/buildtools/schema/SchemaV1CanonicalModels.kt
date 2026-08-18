package org.syriacplatform.buildtools.schema

data class SchemaV1Prayer(
    val id: Long,
    val name: String,
    val description: String? = null
)

data class SchemaV1Qolo(
    val id: Long,
    val groupId: Long,
    val sort: Long,
    val name: String,
    val searchName: String,
    val poeticMeter: String? = null
)

data class SchemaV1Translation(
    val language: String,
    val content: String
)

data class SchemaV1Text(
    val id: Long,
    val syriac: String,
    val translations: List<SchemaV1Translation> = emptyList()
)

data class SchemaV1Petgomo(
    val id: Long,
    val syriac: String,
    val translations: List<SchemaV1Translation> = emptyList()
)

data class SchemaV1Qinto(
    val id: Long,
    val name: String
)

data class SchemaV1Melody(
    val id: Long,
    val qoloId: Long,
    val name: String,
    val searchName: String,
    val hasRecording: Boolean
)

data class SchemaV1CanonicalContent(
    val prayers: List<SchemaV1Prayer>,
    val qolos: List<SchemaV1Qolo>,
    val texts: List<SchemaV1Text>,
    val petgomos: List<SchemaV1Petgomo>,
    val qintos: List<SchemaV1Qinto>,
    val melodies: List<SchemaV1Melody>
)