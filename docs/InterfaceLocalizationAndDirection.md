# Interface Localization and Layout Direction

**Status:** Android implementation and user-facing verification completed for the current reference application  
**Documented:** 2026-11-19  
**Repository:** SyriacPlatform  
**Implementation branch:** `feature/notation-media-selection`

## Purpose

Record the interface-language and layout-direction decisions implemented in the reference application. This document is a focused implementation checkpoint, not a replacement for `CurrentState.md`, `Roadmap v1.4.md`, or the platform architecture documents.

## Fundamental separation of concerns

Three independent concepts must not be conflated:

1. **Interface language:** language of menus, buttons, settings, and UI error/status messages.
2. **Liturgical content language and direction:** supplied by the authoritative content source; changing the UI language must not translate, reorder, or reinterpret Syriac liturgical material.
3. **Translation language:** selection of available translations of liturgical text; independent of interface language and not automatically displayed merely because a preferred translation language exists.

Font scaling is a separate user preference, not a language setting.

## Implemented interface languages

- Arabic (`ar`): right-to-left interface layout.
- English (`en`), French (`fr`), German (`de`): left-to-right interface layout.
- With no explicit interface-language preference, the application retains the device's layout direction and uses the device locale for resource selection.

The resource files are:

- `platform/shared/src/commonMain/composeResources/values/strings.xml` (English default)
- `platform/shared/src/commonMain/composeResources/values-ar/strings.xml`
- `platform/shared/src/commonMain/composeResources/values-fr/strings.xml`
- `platform/shared/src/commonMain/composeResources/values-de/strings.xml`

UI text should use `stringResource(Res.string.*)` rather than hard-coded user-facing labels. Do not localize content names or descriptions originating from the Author Database as though they were interface labels.

## Locale, preferences, and direction

Key files:

- `platform/shared/src/commonMain/kotlin/org/syriacplatform/App.kt`
- `platform/shared/src/commonMain/kotlin/org/syriacplatform/presentation/locale/LocalAppLocale.kt`
- `platform/shared/src/androidMain/kotlin/org/syriacplatform/presentation/locale/LocalAppLocale.android.kt`
- `platform/shared/src/commonMain/kotlin/org/syriacplatform/presentation/audio/CompactAudioPlayer.kt`

`App.kt` reads the persisted `interfaceLanguage` preference, supplies `LocalAppLocale`, and supplies `LocalLayoutDirection` at the application composition boundary. Arabic selects `LayoutDirection.Rtl`; English, French and German select `LayoutDirection.Ltr`. A null or unrecognized override retains the incoming device layout direction.

The Android `LocalAppLocale` actual implementation adjusts the Compose configuration/locale used for localized resources. This implementation is Android-specific; **equivalent iOS behavior is not established by this checkpoint**.

The existing `translationLanguage` preference and text-display selection are distinct from `interfaceLanguage`. Global font scaling is applied through `LocalDensity`, based on the user's font-scale preference.

## Direction rules and deliberate exceptions

**General navigation**

- Back navigation uses logical `Arrangement.Start`, giving a right-side Back button in Arabic and a left-side Back button in Latin-script interfaces.
- Avoid replacing logical `Start`/`End` with physical left/right alignment for general interface navigation without a documented reason.

**Prayer content screen / playback queue**

- Queue-control order is explicitly adjusted according to the current interface direction, preserving the tested Arabic arrangement and mirroring the order for Latin-script languages.
- Previous/next glyphs are also mirrored according to interface direction. **Visual order, icon meaning, and click handler must agree**; do not change only one of these.
- The previous and next actions continue to call `queueController.previous()` and `queueController.next()` respectively.

**Hymn screen toolbar**

- The Back, notation and translation controls occupy their tested logical positions.
- The translation menu is anchored to the translation button using its own `Box`.
- The notation menu is anchored to the centered notation button using its own `Box`, rather than the full toolbar container.
- The list of available recordings and the melody-listening control retain their existing fixed arrangement by design; there is no requirement to mirror every content-specific control.

**Compact audio player**

- The player follows the current interface direction.
- The seek-bar tap calculation accounts for the direction: RTL and LTR map the physical tap position differently.
- Check both visual progression and actual seek behavior when modifying this component.

**Liturgical text and translations**

- Existing text-specific direction and alignment overrides must be retained.
- Syriac text remains governed by liturgical typography and its own direction, not by the interface language.
- Arabic translations must retain Arabic text direction even in a Latin-language interface; non-Arabic translations must not be forced RTL simply because the interface is Arabic.
- Several media/content-specific rows deliberately use explicit local `LayoutDirection.Ltr`; audit each such exception individually rather than removing them wholesale.

## Important Compose implementation constraint

`stringResource()` is a `@Composable` function. It cannot be invoked inside ordinary click callbacks or coroutine bodies. Read a localized fallback label during composition, then capture the resulting `String` for use in playback callbacks. This was corrected after Android compilation errors in the text-playback failure paths.

## Verification and scope

The user reported successful Android builds and visual/functional checks for:

- Four-language interface labels across the home, occasion, prayer and hymn screens.
- Language switching and previously implemented font scaling/navigation behavior.
- Arabic vs Latin-script navigation/button placement.
- Prayer playback queue order and direction-aware previous/next icons.
- Compact audio player direction and seek-bar behavior.
- Translation and notation dropdown placement under their corresponding toolbar buttons.
- Preservation of the intentionally fixed recording/melody controls.

The last direction/icon adjustment was committed as `22b69084e516d4fdf3d301e3f503afb1292d064e`; the user subsequently accepted the direction work as complete. This is a record of user-reported Android testing, **not a claim that a CI run or iOS build was executed during documentation**.

## Regression checklist for future UI changes

1. Build and test Android: `cd platform` then `.\gradlew.bat :shared:testAndroidHostTest :androidApp:assembleDebug`.
2. Compare Arabic with at least one Latin-script UI language on Home, Occasion, Prayer and Hymn screens.
3. Verify Back appears on the right for Arabic and on the left for Latin-script languages.
4. Start a multi-recording prayer queue; verify previous/next order, glyph direction and actual actions.
5. Start audio and seek near both ends of the progress bar in RTL and LTR.
6. Open translation and notation menus; verify they appear beneath their own buttons.
7. Verify Syriac text, Arabic translations, notation images, and content-specific controls retain their intended presentation.
8. Verify changing interface language does not reset the current navigation destination or automatically enable translations.
9. Verify preferences persist after app restart, including language and font scale.
10. Revisit device-default locale behavior and iOS parity when those targets are explicitly tested.

## Future work / boundaries

- Continue a separate audit for any remaining hard-coded UI labels outside the already localized screens.
- Do not claim iOS locale/layout parity without an iOS implementation and verification.
- Revisit the device-default locale lifecycle if Android configuration changes expose issues; the current Android implementation uses a captured original locale and resource configuration updates.
- Keep content-specific RTL decisions explicit and covered by regression tests where feasible.
