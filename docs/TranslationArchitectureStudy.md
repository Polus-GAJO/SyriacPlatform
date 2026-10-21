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


## Design decision — 2026-10-16: shared translations without legacy table migration

**User-approved direction:** Use one canonical `Translations` table and a separate `TranslationLinks` association table. Do **not** merge or restructure existing Access `Texts` and `Petgomo` tables or their downstream queries, reports, forms and VBA. Roughly 20 Petgomo records also occur as text; identical Syriac content should share a translation regardless of liturgical role.

Proposed relationship fields: `TranslationLinks(LinkID, TranslationID, TextID NULL, PetgomoID NULL)`. Each link row must refer to **exactly one** of TextID or PetgomoID (exclusive OR); the same TranslationID can have multiple link rows. Add referential integrity and duplicate-link prevention, plus a save/publish validation that a translation has at least one link. Translation identity must not be inferred from `ExistsIn` or placement.

A single translation can therefore be linked to a `Texts` record and to a `Petgomo` record without duplicating its content. Preserve separate canonical entities in package Schema v1; Build Tools distribute the shared authored translation to each entity's existing `translations` array. No app/runtime schema migration is necessary for this relationship model.

**Pending before implementation:** confirm whether an existing `Languages` table exists; choose language-code policy, multiple translations per language, review/publication selection, and Access constraints/forms. Identical Syriac text should be reviewed before sharing translation links; normalization may find candidates but must not silently equate diacritic or editorial variants. No Access schema or executable code was changed by this documentation update.


## Design decision — 2026-10-16: retain alternatives, select one published translation

**User-approved editorial rule:** For one Syriac source and one language, retain multiple translations from different translators/sources in the Author Database, but select **one** for publication in the app.

Proposed authoring implementation:

- `Translations` retains every translation, language, source/translator and an editorial review status independent of publication.
- `TranslationLinks` links a translation to exactly one `TextID` or `PetgomoID` per row; add `IsPublished` (Yes/No) on the link, **not** globally on `Translations`, because the same translation may be selected for one source entity and not another.
- Publishing invariant: at most one `IsPublished = True` link for each (`TextID`, language) and each (`PetgomoID`, language). Validate this across the joined translation language in Access save/publish workflows and again in Build Tools; ordinary Access indexes on `TranslationLinks` alone cannot enforce this cross-table conditional uniqueness.
- Unselected translations remain in the authoring database; the initial application package should include only the selected translation for each source entity and language. A missing selected translation leaves the language unavailable, without auto-translation.
- Switching a published selection must atomically clear the prior selection for that source entity/language to avoid inconsistent intermediate states.
- This is an approved **design direction**, not an implemented Access migration. Review the exact Access schema and validation workflow before adding tables/forms.


---

## Implementation checkpoint — 2026-10-22 (supersedes earlier proposals)

The preceding proposal/pending language reflects the original study. The **implemented and user-verified Access design** is now:

| Table | Fields / role |
| --- | --- |
| `Languages` | `LanguageID` (PK), `LanguageCode`, `LanguageName`; initial `ar`, `en`, `fr` |
| `Translations` | `TranslationID` (PK), `LanguageID` (FK), `Translation` (Long Text), `Translator`, `Source`, `ReviewStatus` (`Draft`, `Reviewed`, `Approved`) |
| `TranslationLinks` | `LinkID` (PK), `TranslationID` (FK), nullable `TextID` and `PetN`, `IsPublished` (Yes/No); exactly one of `TextID`/`PetN` per row |

`PetN` is the **actual** Petgomo primary key (not the study's proposed `PetgomoID`). Existing `Texts` and `Petgomo` tables were not restructured. The same translation may link to many canonical sources, and editing it changes its content everywhere it is linked. Alternate translations can coexist in Access; publication is per **link**, not per canonical translation. `ReviewStatus` and `IsPublished` are independent; current export selects on `IsPublished=True` (not `ReviewStatus='Approved'`). Publishing workflow selects at most one per source/language.

The user enabled cascading update and delete on `Translations -> TranslationLinks`. `Remove link` deletes a single link without deleting the translation; `Delete permanently` warns about affected Text/Petgomo/published links and deletes the canonical translation, with Access cascading to its links.

User-tested authoring UI: `frmTextTranslations` accessed from `TextsChoose` and `TextsForQolo`; inline editing, new translation, linking existing, publishing/unpublishing, unlinking and permanent deletion. No separate detail editor is used.

### Export contract (Access CSV, verified)

`modSchemaExporter.bas` / `ExportOccasionDataToFolder` writes `Languages.csv`, `Translations.csv`, and `TranslationLinks.csv` into each occasion export directory. Only published links referencing that occasion's exported Texts/Petgomo are included, with their canonical Translations and Languages. Unpublished alternatives remain **only** in the Author Database. Shared Translations are deduplicated while eligible links remain distinct. User verified the generated CSVs and counts; **Build Tools and application consumption are still pending**.

### Required downstream mapping

Build Tools should join `TranslationLinks.TranslationID -> Translations.TranslationID -> Languages.LanguageID` and attach published translations to canonical `TextID` or `PetN` (not to an occurrence or liturgical position). Reuse existing Schema-v1 `translations: [{language, content}]` for Text and Petgomo where supported; retain authored Syriac and occurrence identity. Validate missing references and more than one published translation for a source/language, and preserve content-language choice separately from interface localization. No runtime or UI completion is claimed by this checkpoint.
