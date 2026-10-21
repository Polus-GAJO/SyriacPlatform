package org.syriacplatform.buildtools.source

import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import org.syriacplatform.buildtools.source.models.LanguageSource
import org.syriacplatform.buildtools.source.models.TranslationSource
import org.syriacplatform.buildtools.source.models.TranslationLinkSource

class PublishedTranslationValidatorTest {
    private val validator = PublishedTranslationValidator()
    private fun base(): AuthorSourceData = AuthorSourceDataLoader().load(
        Path.of("..", "author-database", "samples", "mapping-analysis").toAbsolutePath().normalize()
    )
    private fun withTranslations(): AuthorSourceData {
        val source = base()
        return source.copy(
            languages = listOf(LanguageSource(1, "ar", "Arabic")),
            translations = listOf(
                TranslationSource(41, 1, "Arabic translation", null, null, "Approved")
            ),
            translationLinks = listOf(
                TranslationLinkSource(1, 41, source.texts.first().id, null, true),
                TranslationLinkSource(2, 41, null, source.petgomos.first().id, true)
            )
        )
    }

    @Test fun acceptsSharedPublishedTranslationAcrossTextAndPetgomo() {
        validator.validate(withTranslations())
    }

    @Test fun rejectsMissingTranslationReference() {
        val data = withTranslations()
        val bad = data.copy(translationLinks = data.translationLinks.map {
            it.copy(translationId = 999)
        })
        assertTrue(assertFailsWith<IllegalArgumentException> {
            validator.validate(bad)
        }.message!!.contains("missing TranslationID"))
    }

    @Test fun rejectsMissingLanguageReference() {
        val data = withTranslations()
        assertTrue(assertFailsWith<IllegalArgumentException> {
            validator.validate(data.copy(languages = emptyList()))
        }.message!!.contains("missing LanguageID"))
    }

    @Test fun rejectsTwoPublishedLinksForSameTextAndLanguage() {
        val data = withTranslations()
        val second = TranslationSource(42, 1, "Alternate", null, null, "Draft")
        val firstLink = data.translationLinks.first()
        val bad = data.copy(
            translations = data.translations + second,
            translationLinks = data.translationLinks +
                firstLink.copy(id = 3, translationId = 42)
        )
        assertTrue(assertFailsWith<IllegalArgumentException> {
            validator.validate(bad)
        }.message!!.contains("Multiple published translations"))
    }

    @Test fun rejectsMissingCanonicalText() {
        val data = withTranslations()
        val bad = data.copy(translationLinks = listOf(
            data.translationLinks.first().copy(textId = -9999)
        ))
        assertTrue(assertFailsWith<IllegalArgumentException> {
            validator.validate(bad)
        }.message!!.contains("missing TextID"))
    }

    @Test fun legacyExportWithoutTranslationsIsValid() {
        validator.validate(base())
    }
}
