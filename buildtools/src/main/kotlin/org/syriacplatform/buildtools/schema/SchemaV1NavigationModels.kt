package org.syriacplatform.buildtools.schema

data class SchemaV1EntryPoint(
    val id: Long,
    val name: String,
    val occasionId: Long,
    val isDefault: Boolean
)

data class SchemaV1Occasion(
    val id: Long,
    val name: String,
    val description: String? = null,
    val prayerSequenceIds: List<Long>
)

data class SchemaV1PrayerSequence(
    val id: Long,
    val prayerId: Long,
    val liturgicalItemIds: List<Long>
)

data class SchemaV1NavigationContent(
    val entryPoints: List<SchemaV1EntryPoint>,
    val occasions: List<SchemaV1Occasion>,
    val prayerSequences: List<SchemaV1PrayerSequence>
)