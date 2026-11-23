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
