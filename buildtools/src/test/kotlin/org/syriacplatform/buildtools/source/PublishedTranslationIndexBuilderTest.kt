package org.syriacplatform.buildtools.source

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.syriacplatform.buildtools.source.models.LanguageSource
import org.syriacplatform.buildtools.source.models.TranslationSource
import org.syriacplatform.buildtools.source.models.TranslationLinkSource

class PublishedTranslationIndexBuilderTest {
    private fun fixture(): AuthorSourceData = AuthorSourceDataLoader().load(
        Path.of("..", "author-database", "samples", "mapping-analysis").toAbsolutePath().normalize()
    )

    @Test fun sharedTranslationIsAttachedToBothCanonicalSources() {
        val base = fixture()
        val textId = base.texts.first().id
        val petId = base.petgomos.first().id
        val source = base.copy(
            languages = listOf(LanguageSource(1, "ar", "Arabic")),
            translations = listOf(TranslationSource(10, 1, "Shared", null, null, "Draft")),
            translationLinks = listOf(
                TranslationLinkSource(1, 10, textId, null, true),
                TranslationLinkSource(2, 10, null, petId, true)
            )
        )
        val index = PublishedTranslationIndexBuilder().build(source)
        assertEquals(listOf("Shared"), index.byTextId[textId]?.map { it.content })
        assertEquals(listOf("Shared"), index.byPetgomoId[petId]?.map { it.content })
        assertEquals("ar", index.byTextId[textId]?.single()?.languageCode)
    }

    @Test fun translationsAreOrderedByLanguageCode() {
        val base = fixture()
        val id = base.texts.first().id
        val source = base.copy(
            languages = listOf(LanguageSource(1, "ar", "Arabic"), LanguageSource(2, "fr", "French")),
            translations = listOf(
                TranslationSource(21, 2, "Français", null, null, "Approved"),
                TranslationSource(20, 1, "Arabic", null, null, "Approved")
            ),
            translationLinks = listOf(
                TranslationLinkSource(1, 21, id, null, true),
                TranslationLinkSource(2, 20, id, null, true)
            )
        )
        assertEquals(listOf("ar", "fr"),
            PublishedTranslationIndexBuilder().build(source).byTextId[id]?.map { it.languageCode })
    }

    @Test fun oldExportWithoutTranslationFilesYieldsEmptyIndex() {
        val index = PublishedTranslationIndexBuilder().build(fixture())
        assertTrue(index.byTextId.isEmpty())
        assertTrue(index.byPetgomoId.isEmpty())
    }
}
