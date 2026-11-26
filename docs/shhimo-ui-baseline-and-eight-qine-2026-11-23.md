# SyriacPlatform — Shhimo and shared hymn UI baseline (2026-11-23)

Branch: `feature/notation-media-selection`
Last verified GitHub UI commit: `339b091ebfa3e8acfc41c6ce56287e084f64ba15`.

## Status confirmed by device testing
The user confirmed that both apps work well and approved the current shared hymn page, parchment background, 50% translucent top toolbar, no visible border, and subtle shadow. This is the approved visual baseline. Preserve all currently working playback, notation viewing, zoom/pan, rotation/session handling, and navigation.

## Shhimo data pipeline
- Access database source is authoritative. `modSchemaExporter.bas` includes `ExportShhimoPackageData`, which runs `ExportBookData 1` then `ExportMediaData`. The user installed an Access button and confirmed both exports succeed; two separate success messages are acceptable.
- `author-database/exports/book-1/Days.csv` contains Syriac day names from Access, not hardcoded labels.
- Build command from `platform`:
  ```powershell
  .\gradlew :buildtools:buildShhimoDailyPackage `
      -PbookId=1 `
      -PsourceDir="D:/SyriacPlatform/author-database/exports/book-1" `
      -PoutputDir="D:/SyriacPlatform/author-database/preview/shhimo-daily-updated" `
      -PincludeMedia=true `
      --rerun-tasks
  ```
- Successful build: 7 days, 78 prayer sequences, 431 liturgical items, 2524 text occurrences, 12 performance recordings, 73 melody media links, 85 packaged media files.
- Seven day labels from Access are confirmed correct and fully vocalized. Day prayer counts: 11, 11, 11, 11, 11, 12, 11.
- Updated debug package assets under `platform/shhimoApp/src/debug/assets/shhimo`.

## Shhimo navigation UI — approved
- Home: book title, language and font-scale controls (persist after restart), centered red Syriac day names.
- Day prayers: no redundant book title; single top header with Back and centered day name; thin subtle horizontal divider below.
- Prayer items: no redundant book title; single top header with Back and centered prayer name; divider below; tappable text rows without button backgrounds.
- Both lists use `Arrangement.spacedBy(6.dp)` and row vertical padding `10.dp`. First page remains unchanged.

## Shared hymn UI — approved
- Both Occasions and Shhimo use the same `HymnDetailsScreen`.
- Parchment image is stored once at `platform/shared/src/androidMain/assets/shhimo-parchment.jpg` (local image asset; the binary is not necessarily committed to GitHub). The app code loads it via `ParchmentHymnBackground` in `platform/shared/src/androidMain/kotlin/org/syriacplatform/ParchmentHymnBackground.kt`.
- Occasions enables the wrapper only on `AppDestination.HYMN_DETAILS`; Shhimo enables it on its hymn detail page.
- `LocalParchmentHymnBackground` controls transparent background of the hymn page and both text-verse rendering paths. Keep controls legible.
- Top toolbar: `MaterialTheme.colorScheme.surface.copy(alpha = 0.50f)`, `border = null`, `tonalElevation = 0.dp`, `shadowElevation = 1.dp`. User tested and approved.
- Do not accidentally apply parchment to navigation pages or alter existing audio/notation behavior.

## Next work: eight qine / melodic variants
Investigate a database indication that one liturgical item may use any of eight melodies depending on the selected `qinto` (ܩܝܢܬܐ). The next task is to trace the full pipeline from Access relationships and exported tables through buildtools packaging, runtime models, melody selection UI, media and notation resolution. Confirm whether all eight alternatives are exported and address any missing import/association, without duplicating the liturgical text or breaking existing single-melody items.

No root cause or fix for the eight-melody issue has yet been established. Inspect actual Access schema/exports and code before implementing. Prefer incremental changes and explicit build/device tests.


---

## 2026-11-26 — Shhimo beta, weekly selection, prayer playback and notation lifecycle

**Branch:** `feature/notation-media-selection`. **Code checkpoint:** `410d8c9` (before documentation updates). Supersedes earlier pending eight-qine and selector milestones; see EngineeringNotebook decisions 079–080 for the already archived authoring and membership contracts.

- Weekly qinto selector replaced temporary eight buttons with a bottom sheet (9a8aee2); selection persists and selector is limited to navigation screens (2db31ca). Header divider, neutral clickable text and balanced Back/qinto placement respect interface direction (bfdb625, a6d5b384, 865cbc5, 1842c65). Preserve fixed qine and authored occurrence identity; weekly selection is not text duplication.
- Shhimo non-melodic titles centered independently of playback button (f5a3448). The user explicitly confirmed on 2026-11-26 that the corresponding Occasion non-melodic title alignment is also satisfactory; this regression is closed by device observation.
- Shhimo prayer playback: shared queue builder accepts ordered item IDs (62ed5a1); controls on prayer list, qinto-aware queue retained across rotation, localized progress/navigation and compact player integration (b80d4e6, 5b65902, f45c25e, cb33053); cancellation and loading failures guarded (c861f6b). Preserve contextual PERFORMANCE semantics and authored order.
- First Shhimo beta version and launcher icons committed (33d50a8 through ca0e373); XML declaration fixes (f2fbcc8, fd75cad, ece3bd1); package backups moved outside APK assets (c131c4f). Do not infer store publication or successful release build from these commits.
- Notation navigation bug: after closing notation on one occurrence and navigating to another, especially one without notation, stale requests could survive. Reset requests on item navigation (550fc8e). User reported the defect disappeared. Rotation then visibly reloaded the image; session metadata/image reuse and bounded cache added (bd20c4c, e30f953, 6e56a1e, 7ee0a88), and Shhimo composition preserved during rotation (459e970). User reported rotation acceptable in both apps. Keep cache bounded and occurrence-specific state reset; do not equate ordinary rotation with process-death restoration.
- Latest code commit 410d8c9 fits notation to available landscape width with bounded panning. The user explicitly confirmed on 2026-11-26 that landscape width fitting and bounded panning work well. Broader device and automated regression coverage remains separate.

**Restart priority:** both previously pending UI checks are now user-verified and closed. Agree on the next functional milestone before changing code. Retain the approved shared hymn UI and weekly membership policy. Focused regression tests for membership, qinto titles, playback and notation lifecycle remain advisable; no fresh full Gradle suite, iOS or process-death verification is established here. Local assets and package contents were not independently inspected. This checkpoint reconciles GitHub commit history and available prior conversation excerpts, not an exhaustive transcript.

**User verification addendum (2026-11-26):** Windows fast-forward synchronization through `af162bc` succeeded; tracked files were clean and the branch matched origin. Existing untracked local assets, previews and exporter backups were reported and left untouched. No new full Gradle suite was reported.
