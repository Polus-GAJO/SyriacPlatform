package org.syriacplatform.buildtools.schema

data class SchemaV1LiturgicalTextRef(
    val textId: Long,
    val petgomoId: Long?
)

sealed interface SchemaV1LiturgicalItem {
    val id: Long
    val verses: List<SchemaV1LiturgicalTextRef>
}

data class SchemaV1QoloLiturgicalItem(
    override val id: Long,
    val qoloId: Long,

    /*
     * موجودة فقط عندما يستطيع Build Tools
     * حسم Melody واحدة بصورة قانونية.
     */
    val effectiveMelodyId: Long? = null,

    /*
     * تستخدم عندما توجد Melodies مرشحة معروفة
     * ولكن لا يجوز اختيار واحدة منها اعتباطيًا.
     *
     * في حالة Qinto غير المحددة تكون القائمة فارغة.
     */
    val melodyCandidateIds: List<Long> = emptyList(),

    override val verses: List<SchemaV1LiturgicalTextRef>
) : SchemaV1LiturgicalItem

data class SchemaV1UnresolvedQoloLiturgicalItem(
    override val id: Long,
    override val verses: List<SchemaV1LiturgicalTextRef>
) : SchemaV1LiturgicalItem

data class SchemaV1PrayerCompositionDraft(
    val prayerId: Long,
    val orderedSourceItemIds: List<Long>,
    val resolvedItems: List<SchemaV1LiturgicalItem>,
    val blockedItemIds: List<Long>
)

data class SchemaV1CompositionDraft(
    val occasionId: Long,
    val prayers: List<SchemaV1PrayerCompositionDraft>,
    val diagnostics: List<CompositionDiagnostic>
) {
    val hasBlockingDiagnostics: Boolean
        get() = diagnostics.isNotEmpty()

    val resolvedItemCount: Int
        get() = prayers.sumOf {
            it.resolvedItems.size
        }

    val blockedItemCount: Int
        get() = prayers.sumOf {
            it.blockedItemIds.size
        }
}

data class CompositionDiagnostic(
    val code: String,
    val message: String,
    val existsInId: Long
)