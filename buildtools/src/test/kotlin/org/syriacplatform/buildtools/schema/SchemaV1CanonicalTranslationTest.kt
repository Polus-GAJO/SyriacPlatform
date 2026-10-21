package org.syriacplatform.buildtools.schema

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.buildtools.source.AuthorSourceDataLoader
import org.syriacplatform.buildtools.source.models.LanguageSource
import org.syriacplatform.buildtools.source.models.TranslationLinkSource
import org.syriacplatform.buildtools.source.models.TranslationSource

class SchemaV1CanonicalTranslationTest {
    private fun base() = AuthorSourceDataLoader().load(
        Path.of("..", "author-database", "samples", "mapping-analysis")
            .toAbsolutePath().normalize()
    )

    @Test
    fun mapsSharedPublishedTranslationToTextAndPetgomo() {
        val source = base()
        val textId = source.texts.first().id
        val petId = source.petgomos.first().id
        val withTranslations = source.copy(
            languages = listOf(LanguageSource(1, "ar", "Arabic")),
            translations = listOf(
                TranslationSource(51, 1, "Published Arabic", null, null, "Draft")
            ),
            translationLinks = listOf(
                TranslationLinkSource(1, 51, textId, null, true),
                TranslationLinkSource(2, 51, null, petId, true)
            )
        )

        val mapped = SchemaV1CanonicalMapper().map(withTranslations)
        assertEquals(
            listOf(SchemaV1Translation("ar", "Published Arabic")),
            mapped.texts.single { it.id == textId }.translations
        )
        assertEquals(
            listOf(SchemaV1Translation("ar", "Published Arabic")),
            mapped.petgomos.single { it.id == petId }.translations
        )
        assertEquals(
            source.texts.first().syriac,
            mapped.texts.single { it.id == textId }.syriac
        )
    }

    @Test
    fun olderExportsKeepEmptyTranslationArrays() {
        val mapped = SchemaV1CanonicalMapper().map(base())
        assertTrue(mapped.texts.all { it.translations.isEmpty() })
        assertTrue(mapped.petgomos.all { it.translations.isEmpty() })
    }
}
