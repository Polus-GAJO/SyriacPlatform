package org.syriacplatform.buildtools.source

/**
 * Resolves published Access translations by canonical TextID or PetN.
 * Independent of liturgical occurrences; suitable for Schema-v1 mapping.
 * The loader validates source integrity before this index is constructed.
 */
data class PublishedTranslation(
    val translationId: Long,
    val languageCode: String,
    val content: String
)

data class PublishedTranslationIndex(
    val byTextId: Map<Long, List<PublishedTranslation>>,
    val byPetgomoId: Map<Long, List<PublishedTranslation>>
)

class PublishedTranslationIndexBuilder {
    fun build(source: AuthorSourceData): PublishedTranslationIndex {
        PublishedTranslationValidator().validate(source)
        val languageCodes = source.languages.associate { it.id to it.code }
        val translations = source.translations.associateBy { it.id }
        val byText = mutableMapOf<Long, MutableList<PublishedTranslation>>()
        val byPetgomo = mutableMapOf<Long, MutableList<PublishedTranslation>>()

        source.translationLinks.forEach { link ->
            val translation = requireNotNull(translations[link.translationId])
            val entry = PublishedTranslation(
                translationId = translation.id,
                languageCode = requireNotNull(languageCodes[translation.languageId]),
                content = translation.content
            )
            if (link.textId != null) {
                byText.getOrPut(link.textId) { mutableListOf() }.add(entry)
            } else {
                byPetgomo.getOrPut(requireNotNull(link.petgomoId)) { mutableListOf() }.add(entry)
            }
        }
        val ordering = compareBy<PublishedTranslation>({ it.languageCode }, { it.translationId })
        return PublishedTranslationIndex(
            byTextId = byText.mapValues { (_, values) -> values.sortedWith(ordering) },
            byPetgomoId = byPetgomo.mapValues { (_, values) -> values.sortedWith(ordering) }
        )
    }
}
