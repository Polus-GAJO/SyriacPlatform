package org.syriacplatform.buildtools.source

/**
 * Checks the published-only translation export before mapping to Schema v1.
 * Older author exports without translation CSVs remain valid (empty lists).
 */
class PublishedTranslationValidator {
    fun validate(source: AuthorSourceData) {
        val languages = source.languages.associateBy { it.id }
        val translations = source.translations.associateBy { it.id }
        val textIds = source.texts.map { it.id }.toSet()
        val petgomoIds = source.petgomos.map { it.id }.toSet()

        require(languages.size == source.languages.size) { "Duplicate LanguageID in Languages.csv" }
        require(source.languages.map { it.code }.distinct().size == source.languages.size) {
            "Duplicate LanguageCode in Languages.csv"
        }
        require(translations.size == source.translations.size) { "Duplicate TranslationID in Translations.csv" }
        require(source.translationLinks.map { it.id }.distinct().size == source.translationLinks.size) {
            "Duplicate LinkID in TranslationLinks.csv"
        }
        source.translations.forEach { translation ->
            require(translation.languageId in languages) {
                "TranslationID=${translation.id} references missing LanguageID=${translation.languageId}"
            }
        }
        val publishedSourceLanguages = mutableSetOf<Pair<String, Long>>()
        source.translationLinks.forEach { link ->
            require(link.isPublished) { "LinkID=${link.id} is not published" }
            require((link.textId == null) != (link.petgomoId == null)) {
                "LinkID=${link.id} must reference exactly one TextID or PetN"
            }
            val translation = requireNotNull(translations[link.translationId]) {
                "LinkID=${link.id} references missing TranslationID=${link.translationId}"
            }
            val key = if (link.textId != null) {
                require(link.textId in textIds) {
                    "LinkID=${link.id} references missing TextID=${link.textId}"
                }
                "Text:${link.textId}" to translation.languageId
            } else {
                require(link.petgomoId in petgomoIds) {
                    "LinkID=${link.id} references missing PetN=${link.petgomoId}"
                }
                "Petgomo:${link.petgomoId}" to translation.languageId
            }
            require(publishedSourceLanguages.add(key)) {
                "Multiple published translations for ${key.first}, LanguageID=${key.second}"
            }
        }
    }
}
