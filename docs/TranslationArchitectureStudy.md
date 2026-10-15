# SyriacPlatform — Translation Architecture Study

**Status:** Design discussion; no implementation approved
**Date:** 2026-10-15
**Scope:** Text and Petgomo translations from Author Database to Application Package

## Current verified implementation

- Domain `TextContent(id, syriac, translations)` and `TextTranslation(language, content)` already exist.
- `TextContentJsonDto` and `TextTranslationJsonDto` support optional translation arrays, with `language` and `content`.
- `SchemaV1Text` and `SchemaV1Petgomo` already support `List<SchemaV1Translation>`; `SchemaV1PackageWriter` writes translation arrays for both text and Petgomo.
- Current Access CSV `TextSource` contains source text and authoring metadata, but no translation rows; the current export-to-build mapping must be extended before translations can be populated from Access.
- Authored text identity (`TextID`) is distinct from contextual `ExistsInText` occurrence identity and from `ExistsIn.ID` liturgical item identity. Do not collapse these IDs.

## Proposed authoring model (not yet implemented)

**Option A — canonical translations (recommended starting point):**

- `TextTranslation` author table: `TranslationID` (stable PK), `TextID` (FK to `Texts.TextID`), `LanguageCode`, `TranslatedText`, plus optional editorial status and source/translator metadata.
- `PetgomoTranslation` author table: `TranslationID` (stable PK), `PetgomoID` (FK to `Petgomo`), `LanguageCode`, `TranslatedText`, with equivalent optional metadata.
- An individual Syriac text translated once can be reused in every Qolo, prayer, and occasion referencing that canonical `TextID`. A Petgomo is translated by its own canonical ID, not by the verse it happens to accompany.
- Proposed initial language tags: `ar`, `en`, `fr` (BCP-47-style codes); validate language codes rather than treating translated content as UI localization.
- Do not impose one translation per language until editorial policy is decided. Multiple editions/translators may be needed; the current package format supports only a list of language/content pairs and has no translation-edition identifier.

**Option B — contextual translations (future extension only if required):**

- A translation of a verse that materially depends on its occurrence could be linked to `ExistsInText.ID`, *in addition to*, not instead of, the canonical `TextID` translation.
- Such overrides require an explicit precedence rule and a versioned package/runtime contract. Do not silently select a contextual translation or infer it from Qolo/Location.

## Questions to settle before Access schema changes

1. Are translations meant to be literal, liturgical, explanatory, or multiple parallel editions?
2. Can one canonical `TextID` have more than one translation for the same language? If yes, how should the preferred edition be chosen?
3. Are Petgomo translations authored independently from verse translations?
4. Must translation records retain translator, source publication, review status, and revision history?
5. Do some repeated texts require occasion-specific translations, or can this be deferred until a documented real example appears?
6. How should missing translations appear to readers (Syriac only, unavailable label, or language fallback)?

## Suggested implementation sequence (after design approval)

1. Inspect the actual Access schema and existing translation-related tables, if any; do not create duplicates based only on this proposal.
2. Decide canonical identity and editorial uniqueness rules, including same-language variants.
3. Implement Access authoring tables/forms and referential integrity.
4. Extend controlled export with translation CSVs and unit tests.
5. Extend Build Tools loading, mapping, deterministic ordering and validation, then write existing Schema-v1 translation arrays where sufficient.
6. Add package/runtime tests for missing translations, multiple languages, repeated TextIDs and Petgomo linkage.
7. Only then design language selection and translated-verse display in the application.

## Boundaries and safeguards

- Author Database remains authoritative; Build Tools must not invent or machine-translate content.
- Preserve Syriac text, authored occurrence ordering and Petgomo relationships.
- No change to the current non-melodic text UI or hymn navigation during translation architecture study.
- Keep language-content translations separate from application interface localization.
