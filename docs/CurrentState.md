# SyriacPlatform --- Current State

**Document:** CurrentState\
**Status:** Active implementation reference\
**Last updated:** 2026-09-02\
**Repository:** SyriacPlatform\
**Branch:** `main`\
**Current milestone:** Phase 9 Runtime Audio Integration --- Android reusable audio foundation, lifecycle ownership, performer metadata, and multiple-recording selection verified\
**Verified functional milestone:** `b56fdea`\
**Documentation correction follows:** `3ca4c6b`

------------------------------------------------------------------------

# 1. Purpose of This Document

This document records the current implementation state of
SyriacPlatform.

Its purpose is to provide a reliable restart point for future
development sessions and to prevent architectural decisions already
established during implementation from being accidentally reinterpreted
or lost.

This document describes:

-   what is currently implemented;
-   the current content architecture;
-   the Application Package loading path;
-   runtime content resolution;
-   the current navigation hierarchy;
-   important domain decisions established during implementation;
-   what has been verified by tests and runtime execution;
-   the recommended next implementation areas.

This is not a replacement for the Platform Blueprint or Application
Package Specification.

It records the current implementation state.

------------------------------------------------------------------------

# 2. Platform Goal

SyriacPlatform is being developed as a reusable platform capable of
powering multiple Syriac liturgical applications from a common
architecture.

The intended structure remains:

``` text
Texso / SyriacPlatform

├── Author Database
│
├── Build Tools
│
├── Core Engine
│
├── Shhima App
│
├── Occasions App
│
└── Full Library App
```

The platform is not responsible for authoring or inferring liturgical
relationships.

The Author Database is the authoritative source in which content
relationships are established.

The platform receives prepared Application Packages and is responsible
for:

-   loading them;
-   validating them;
-   resolving their relationships;
-   exposing them through stable runtime APIs;
-   presenting them to consuming applications.

------------------------------------------------------------------------

# 3. Architectural Principle: Content vs. Presentation

A fundamental platform principle remains:

> Content structure must remain independent from presentation.

The Author Database determines the canonical content and its established
relationships.

The Application Package transports that content.

The Core validates and resolves it.

The application UI consumes the resolved runtime representation.

The platform must not infer relationships that should have been
established by the Author Database.

------------------------------------------------------------------------

# 4. Current Package Pipeline

The implemented content pipeline is now approximately:

``` text
Application Package JSON
        │
        ▼
Package DTOs
        │
        ▼
Domain Mapping
        │
        ▼
ParsedApplicationPackage
        │
        ▼
Package Validation
        │
        ▼
ApplicationPackageLoader
        │
        ▼
RuntimeContentStore
        │
        ├── Parsed content
        │
        └── RuntimeContentIndex
                │
                ▼
        RuntimeContentResolver
                │
                ▼
        Resolved Runtime Content
                │
                ▼
        ContentRepository
                │
                ▼
        ContentService
                │
                ▼
        PlatformContext
                │
                ▼
              UI
```

The Application Package is loaded and validated before being exposed to
the consuming application.

The package is cached by `ApplicationPackageContentRepository` after
successful loading.

------------------------------------------------------------------------

# 5. Application Package

The platform now uses an Application Package rather than loading
isolated JSON collections directly from application code.

The current sample package contains content files including:

``` text
manifest.json

content/
├── entry-points.json
├── occasions.json
├── prayers.json
├── prayer-sequences.json
├── liturgical-items.json
├── texts.json
└── melodies.json
```

Additional canonical collections supported by the domain and validation
architecture may also be present as required by package content.

The package structure is expected to evolve only through explicit
specification/version changes.

------------------------------------------------------------------------

# 6. Compatibility

Core/package compatibility is represented explicitly.

The compatibility layer includes:

``` text
CoreCompatibility
CoreCompatibilityDefaults
```

`CoreCompatibilityDefaults` is the canonical location for the default
compatibility values used by the current Core.

Compatibility checks are part of the package loading/validation process.

Compatibility information must not be scattered as unrelated hard-coded
values throughout the platform.

------------------------------------------------------------------------

# 7. Runtime Content Layer

A dedicated runtime layer now exists above `ParsedApplicationPackage`.

The main runtime components include:

``` text
RuntimeContentStore
RuntimeContentIndex
RuntimeContentResolver
```

The purpose of this layer is to prevent consuming applications from
manually traversing raw package relationships.

The runtime layer provides indexed and resolved access to package
content.

------------------------------------------------------------------------

# 8. RuntimeContentStore

`RuntimeContentStore` represents the validated package prepared for
runtime use.

Conceptually:

``` text
RuntimeContentStore
├── content
└── index
```

The original ordered collections remain available through the parsed
content.

Fast identity-based lookup is provided by the runtime index.

This distinction is intentional.

Ordered source collections must not automatically be replaced by maps
because ordering can carry semantic meaning.

------------------------------------------------------------------------

# 9. RuntimeContentIndex

The runtime index provides identity-based lookup for canonical entities.

Examples include indexes for:

``` text
EntryPoint
Occasion
PrayerSequence
Prayer
LiturgicalItem
TextContent
Petgomo
Qolo
Melody
```

and other supported package entities.

The index is a runtime optimization and relationship-resolution
mechanism.

It does not redefine canonical ordering.

------------------------------------------------------------------------

# 10. Runtime Resolution

`RuntimeContentResolver` resolves package references into structures
that are directly usable by application code.

The implemented traversal includes the hierarchy:

``` text
EntryPoint
    │
    ▼
Occasion
    │
    ▼
PrayerSequence
    │
    ├── Prayer
    │
    └── ordered LiturgicalItems
```

The resolver deliberately traverses original lists when order and
repetition are meaningful.

It must not convert ordered liturgical usages into sets.

------------------------------------------------------------------------

# 11. Canonical Entity vs. Liturgical Occurrence

One of the most important architectural distinctions established during
implementation is the difference between:

``` text
canonical entity
```

and:

``` text
liturgical occurrence
```

For example, `TextContent` represents a canonical reusable text.

A liturgical occurrence determines where and how that text is used.

Likewise, `Qolo` is a canonical entity.

A particular occurrence of that Qolo inside a prayer contains contextual
information that does not belong permanently to the canonical Qolo
itself.

This distinction must be preserved throughout future implementation.

------------------------------------------------------------------------

# 12. Prayer and PrayerSequence

`Prayer` represents the persistent identity of a known prayer.

It does not itself own the ordered liturgical contents used in every
context.

`PrayerSequence` represents the contextual ordered realization of a
prayer.

Conceptually:

``` text
Prayer
└── canonical identity

PrayerSequence
├── prayerId
└── liturgicalItemIds[]
```

The ordered `liturgicalItemIds` determine the liturgical components
appearing in that sequence.

Order is significant.

Repeated usage is legal.

------------------------------------------------------------------------

# 13. LiturgicalItem

`LiturgicalItem` represents one liturgical occurrence inside a sequence.

It has its own identity:

``` text
LiturgicalItemId
```

This identity is important because two occurrences may reference the
same canonical content while carrying different contextual information.

Current supported targets include:

``` text
LiturgicalItemTarget.Text

LiturgicalItemTarget.Qolo
```

------------------------------------------------------------------------

# 14. Text Liturgical Occurrence

A text occurrence currently has the conceptual form:

``` text
LiturgicalItemTarget.Text
├── textId
└── petgomoId?
```

`textId` points to the canonical `TextContent`.

`petgomoId` belongs to the liturgical occurrence.

A Petgomo therefore must not be treated as a permanent property of
`TextContent`.

------------------------------------------------------------------------

# 15. Contextual Qolo / Hymn Model

The Qolo model was extended during the current milestone to represent a
complete contextual hymn occurrence.

The conceptual structure is now:

``` text
LiturgicalItemTarget.Qolo
├── qoloId
├── effectiveMelodyId: MelodyId?
├── melodyCandidateIds: List<MelodyId>
└── verses: List<LiturgicalTextRef>
```

This is an important architectural decision.

The canonical `Qolo` does not own the selected verses used in a
particular prayer.

The selected verses belong to the liturgical occurrence of that Qolo.

------------------------------------------------------------------------

# 16. LiturgicalTextRef

`LiturgicalTextRef` represents one contextual occurrence of a canonical
text inside a hymn.

Conceptually:

``` text
LiturgicalTextRef
├── textId
└── petgomoId?
```

Important rules:

1.  `textId` references canonical `TextContent`.

2.  The same `textId` may appear more than once.

3.  The order of the list is significant.

4.  Repetition must be preserved.

5.  `petgomoId` belongs to this occurrence of the text.

6.  The same canonical text may therefore appear multiple times with
    different contextual Petgomo relationships.

Example:

``` text
Qolo occurrence
│
├── verse occurrence 1
│   ├── Text 601
│   └── Petgomo 701
│
└── verse occurrence 2
    ├── Text 601
    └── no Petgomo
```

Both occurrences refer to the same canonical text.

They are nevertheless distinct liturgical usages.

------------------------------------------------------------------------

# 17. What the Platform Does Not Decide

The platform must not attempt to infer why a text belongs to a Qolo.

In particular, poetic-meter relationships are outside the responsibility
of this runtime organizational layer.

A text may be associated with one or multiple Qolos in the Author
Database for authorial or poetic reasons.

Those relationships are established before package generation.

The platform's responsibility here is narrower:

> Where and how was this text used liturgically?

The platform consumes the relationships already established by the
Author Database.

It does not choose verses or infer poetic compatibility.

------------------------------------------------------------------------

# 18. Package Representation of Hymn Verses

The package format now supports contextual verses inside a Qolo
liturgical item.

Conceptually:

``` json
{
  "id": 2,
  "type": "qolo",
  "targetId": 438,
  "effectiveMelodyId": 1,
  "petgomoId": null,
  "verses": [
    {
      "textId": 1,
      "petgomoId": null
    },
    {
      "textId": 1,
      "petgomoId": null
    }
  ]
}
```

The repeated `textId` is intentional and legal.

This structure represents repeated usage, not duplicated canonical
content.

------------------------------------------------------------------------

# 19. Package DTO and Mapping Support

The package-format layer now includes support for contextual hymn
verses.

Relevant structures include:

``` text
LiturgicalItemJsonDto
LiturgicalTextRefJsonDto
LiturgicalItemMapper
```

The mapper converts physical JSON representation into strongly typed
domain objects.

For Qolo targets it resolves the physical verse records into:

``` text
List<LiturgicalTextRef>
```

For Text targets, declaring Qolo verses is invalid.

For Qolo targets, top-level Petgomo usage remains invalid; contextual
Petgomo belongs to individual verse occurrences.

------------------------------------------------------------------------

# 20. Reference Validation

Reference validation was extended to understand references inside Qolo
verses.

The validation layer now checks not only direct
`LiturgicalItemTarget.Text` references but also contextual references
contained inside:

``` text
LiturgicalItemTarget.Qolo.verses
```

This includes validation of:

``` text
verse.textId
verse.petgomoId?
```

against the canonical package collections.

Missing references are fatal package-validation issues.

The validation architecture continues to use explicit rules coordinated
by `ReferenceValidator`.

------------------------------------------------------------------------

# 21. Repetition Is Legal

Repetition is a first-class supported behavior.

The same:

``` text
LiturgicalItemId
TextId
```

may legally appear repeatedly where the model permits repeated
liturgical usage.

The runtime must preserve these repetitions.

Code that transforms liturgical sequences or hymn verses into `Set`
structures would therefore be incorrect unless uniqueness is explicitly
required for a separate operation.

------------------------------------------------------------------------

# 22. ResolvedLiturgicalItem

The runtime representation of a liturgical item is:

``` text
ResolvedLiturgicalItem
├── item
└── target
```

The resolved target provides actual domain entities instead of forcing
the UI to manually resolve identifiers.

------------------------------------------------------------------------

# 23. Resolved Text Target

A resolved text occurrence has the conceptual form:

``` text
ResolvedLiturgicalItemTarget.Text
├── text: TextContent
└── petgomo: Petgomo?
```

This representation is directly consumable by the UI.

------------------------------------------------------------------------

# 24. Resolved Hymn Target

A resolved Qolo occurrence now has the conceptual form:

``` text
ResolvedLiturgicalItemTarget.Qolo
├── qolo
├── effectiveMelody: Melody?
├── melodyCandidates: List<Melody>
└── verses
    ├── ResolvedLiturgicalText
    ├── ResolvedLiturgicalText
    └── ...
```

Each `ResolvedLiturgicalText` contains:

``` text
ResolvedLiturgicalText
├── text: TextContent
└── petgomo: Petgomo?
```

This means the runtime UI does not need to resolve `TextId` or
`PetgomoId` manually.

------------------------------------------------------------------------

# 25. Qolo Name Collision

During implementation, Kotlin name shadowing occurred between:

``` text
org.syriacplatform.content.models.Qolo
```

and:

``` text
ResolvedLiturgicalItemTarget.Qolo
```

The resolved runtime model uses an explicit alias for the canonical
content type:

``` kotlin
import org.syriacplatform.content.models.Qolo as ContentQolo
```

This distinction should be preserved unless the runtime target is
renamed in a future deliberate refactoring.

------------------------------------------------------------------------

# 26. ContentRepository

`ContentRepository` is the abstraction used by the content service.

The current repository API has expanded beyond the original Qolo-only
proof of concept.

It now supports runtime access required by the current application flow,
including:

``` text
loadQolo(...)
loadAllQolos()

loadEntryPoints()
loadDefaultEntryPoint()

loadOccasions()
loadOccasion(...)

loadLiturgicalItem(...)
```

The exact API should continue to evolve only when required by stable
application use cases.

------------------------------------------------------------------------

# 27. ApplicationPackageContentRepository

`ApplicationPackageContentRepository` is the production repository
implementation backed by the Application Package.

Its responsibilities include:

1.  loading the Application Package;
2.  applying Core compatibility;
3.  receiving validated package data;
4.  constructing `RuntimeContentStore`;
5.  caching the store;
6.  serving content through runtime indexes and resolvers.

The repository must not bypass package validation.

------------------------------------------------------------------------

# 28. ContentService

`ContentService` provides the official service-level access to content.

`DefaultContentService` delegates content operations to
`ContentRepository`.

The service remains exposed through `PlatformContext`.

The UI should consume platform services rather than directly opening
package JSON files.

------------------------------------------------------------------------

# 29. PlatformContext

`PlatformContext` remains the official high-level interface exposed to
consuming applications.

Conceptually:

``` text
PlatformContext
├── content
└── navigation
```

It hides direct kernel/service-registry details from application UI
code.

This boundary should remain stable.

------------------------------------------------------------------------

# 30. Current Navigation Model

The current navigation destinations include:

``` text
HOME
OCCASION_DETAILS
PRAYER_DETAILS
HYMN_DETAILS
QOLO_DETAILS
```

The important distinction is that:

``` text
QOLO_DETAILS
```

represents information about the canonical Qolo entity,

while:

``` text
HYMN_DETAILS
```

represents one contextual liturgical occurrence of a Qolo.

These must not be treated as equivalent.

------------------------------------------------------------------------

# 31. Current UI Hierarchy

The current tested application flow is:

``` text
HOME
│
│  list of occasions
▼
OCCASION_DETAILS
│
│  list of prayers / prayer sequences
▼
PRAYER_DETAILS
│
│  ordered liturgical components
▼
HYMN_DETAILS
```

The screens have been manually verified in the Android emulator.

------------------------------------------------------------------------

# 32. HOME

The current HOME screen displays available occasions.

The sample package currently provides a minimal test occasion.

Selecting the occasion opens:

``` text
OCCASION_DETAILS
```

------------------------------------------------------------------------

# 33. OCCASION_DETAILS

The occasion screen displays the prayer sequences belonging to the
selected occasion.

Selecting a prayer opens:

``` text
PRAYER_DETAILS
```

The UI must not prematurely expand the contents of every prayer on the
occasion screen.

------------------------------------------------------------------------

# 34. PRAYER_DETAILS

The prayer-details screen displays the ordered liturgical components of
the selected prayer sequence.

For a Qolo/hymn occurrence, this screen displays the hymn as a
component.

It may display identifying information such as:

``` text
Qolo name
effective melody
```

but it must not expand the hymn verses inline.

This separation was explicitly verified during UI testing.

The correct conceptual behavior is:

``` text
Prayer
├── component
├── hymn
└── component
```

not:

``` text
Prayer
├── hymn title
├── hymn verse
├── hymn verse
└── ...
```

The hymn verses belong to the next navigation level.

------------------------------------------------------------------------

# 35. HYMN_DETAILS

`HYMN_DETAILS` displays one contextual hymn occurrence.

It receives the identity of the liturgical occurrence rather than
relying only on `QoloId`.

The current selection is represented by:

``` text
LiturgicalItemId
```

This is necessary because `QoloId` alone does not identify the complete
liturgical context.

For example, the same canonical Qolo may appear in different places
with:

``` text
different effective melody
different selected verses
different verse ordering
different Petgomo relationships
```

Therefore:

``` text
QoloId
```

is insufficient to identify a contextual hymn occurrence.

`LiturgicalItemId` identifies the occurrence that owns the contextual
data.

------------------------------------------------------------------------

# 36. Hymn Details Runtime Flow

The current flow is:

``` text
User selects hymn component
        │
        ▼
selectedLiturgicalItemId
        │
        ▼
HYMN_DETAILS
        │
        ▼
ContentService.loadLiturgicalItem(...)
        │
        ▼
ContentRepository
        │
        ▼
RuntimeContentResolver.resolveLiturgicalItem(...)
        │
        ▼
ResolvedLiturgicalItemTarget.Qolo
        │
        ├── Qolo
        ├── effective Melody?
        ├── Melody candidates
        └── resolved verses
```

This is the preferred runtime flow for contextual hymn presentation.

------------------------------------------------------------------------

# 37. Sample Package

The current sample package is intentionally minimal.

It exists to exercise the architecture end-to-end.

The sample hymn currently contains repeated use of the same canonical
text.

This is intentional.

It verifies that:

-   repeated verse occurrences survive JSON parsing;
-   mapping preserves them;
-   validation accepts legal repetition;
-   runtime resolution preserves order;
-   UI displays both occurrences.

The sample package should not be mistaken for production liturgical
content.

------------------------------------------------------------------------

# 38. Sample Prayer Cleanup

During UI testing, the sample prayer sequence temporarily contained:

``` text
[1, 2, 1]
```

which represented:

``` text
Text
Qolo
Text
```

This was useful for verifying ordering and repetition.

For the current visual hymn test, the sample sequence was simplified to:

``` text
[2]
```

so that the prayer contains only the test hymn occurrence.

This is sample-data cleanup, not a change in platform semantics.

Standalone Text liturgical items remain supported.

------------------------------------------------------------------------

# 39. UI Issue Discovered and Corrected

During visual testing, hymn verses initially appeared directly on
`PRAYER_DETAILS`.

This was incorrect because it collapsed two navigation levels into one.

The problem was not in runtime resolution.

The runtime had correctly resolved the hymn verses.

The issue was presentation logic in `ResolvedLiturgicalItemView`, where
`target.verses` were still being rendered inline.

That rendering was removed.

The resulting separation is now:

``` text
PRAYER_DETAILS
→ hymn component only

HYMN_DETAILS
→ hymn verses
```

This distinction should be preserved in future UI refactoring.

------------------------------------------------------------------------

# 40. Validation and Tests

The shared module test suite has been repeatedly executed throughout
this implementation phase.

The latest development cycle included successful execution after:

-   adding contextual verses;
-   updating DTO mapping;
-   updating reference validation;
-   updating runtime resolution;
-   updating repository/service access;
-   updating UI navigation.

The test count increased as the new functionality was added.

Tests cover areas including:

``` text
Package mapping
Package validation
Reference validation
Semantic validation
Allowed duplicates
Runtime content resolution
Repository behavior
Service behavior
Kernel/bootstrap behavior
Navigation behavior
```

Tests should continue to be treated as required before committing
architectural changes.

------------------------------------------------------------------------

# 41. Current Verification Workflow

The standard verification commands currently used are:

``` powershell
.\platform\gradlew.bat -p .\platform :shared:allTests
```

``` powershell
.\platform\gradlew.bat -p .\platform :shared:check
```

``` powershell
.\platform\gradlew.bat -p .\platform :androidApp:assembleDebug
```

Visual UI changes should additionally be verified in the Android
emulator.

------------------------------------------------------------------------

# 42. Git State

The latest milestone described by this document was pushed successfully
to:

``` text
main
```

at commit:

``` text
0872227
```

This milestone includes the contextual Qolo verse model and hymn
runtime/UI flow.

The commit represents a coherent vertical feature spanning:

``` text
Domain
→ Package DTO
→ Mapper
→ Validation
→ Runtime
→ Repository
→ Service
→ Navigation
→ UI
→ Tests
```

------------------------------------------------------------------------

# 43. Major Completed Milestone

The major completed milestone can be summarized as:

> Contextual Qolo verses and hymn runtime flow.

Implemented capabilities include:

-   `LiturgicalTextRef`;
-   contextual Qolo verses;
-   ordered verse preservation;
-   legal repeated text usage;
-   contextual Petgomo per verse occurrence;
-   package DTO support;
-   package mapping support;
-   text reference validation inside hymn verses;
-   Petgomo reference validation inside hymn verses;
-   runtime verse resolution;
-   `ResolvedLiturgicalText`;
-   resolved Qolo runtime target;
-   repository access to resolved LiturgicalItem;
-   service access to resolved LiturgicalItem;
-   `HYMN_DETAILS`;
-   LiturgicalItem-based hymn selection;
-   separation of prayer and hymn presentation;
-   end-to-end sample-package execution;
-   Android emulator visual verification.

------------------------------------------------------------------------

# 44. Important Invariants

The following invariants should be treated as established unless
deliberately changed through an architectural decision.

## 44.1 Canonical content is reusable

Canonical entities such as `TextContent`, `Qolo`, and `Petgomo` are not
duplicated merely because they are used more than once.

------------------------------------------------------------------------

## 44.2 Occurrences carry context

Contextual information belongs to the occurrence that uses canonical
content.

------------------------------------------------------------------------

## 44.3 Order matters

Prayer liturgical items and hymn verses are ordered lists.

Their order must be preserved.

------------------------------------------------------------------------

## 44.4 Repetition is allowed

Repeated identifiers in contextual ordered lists can be legal and
meaningful.

Do not automatically deduplicate them.

------------------------------------------------------------------------

## 44.5 Petgomo belongs to usage

Petgomo association belongs to the contextual text occurrence.

It is not an intrinsic property of canonical `TextContent`.

------------------------------------------------------------------------

## 44.6 Qolo does not own contextual verses

Canonical `Qolo` does not permanently contain the verses selected for
every liturgical use.

The contextual Qolo occurrence owns the ordered verse references.

------------------------------------------------------------------------

## 44.7 QoloId is not enough for hymn navigation

A contextual hymn must be identified through its liturgical occurrence
when contextual information is required.

The current implementation uses `LiturgicalItemId`.

------------------------------------------------------------------------

## 44.8 Runtime resolves; UI consumes

The UI should not manually reconstruct package relationships.

Runtime resolution should prepare structures suitable for application
consumption.

------------------------------------------------------------------------

## 44.9 Author DB decides content relationships

The platform consumes established relationships.

It does not infer poetic or liturgical relationships that belong to the
Author Database/build process.

------------------------------------------------------------------------

# 45. Architectural Direction

The current implementation has moved beyond the original Qolo-only proof
of concept.

The platform now has the beginnings of a real vertical content path:

``` text
Author-established relationships
        ↓
Application Package
        ↓
Validation
        ↓
Runtime indexes
        ↓
Runtime relationship resolution
        ↓
Service API
        ↓
Context-aware application navigation
        ↓
UI
```

Future work should continue extending this path rather than creating
parallel content-loading mechanisms.

------------------------------------------------------------------------

# 46. Documentation That May Require Synchronization

Because the Application Package representation has evolved, package
documentation should be reviewed before significantly expanding
implementation.

In particular, the formal package specification should be checked for
the current Qolo liturgical-item structure:

``` text
verses[]
├── textId
└── petgomoId?
```

If the existing Application Package specification does not yet describe
this structure, it should be updated.

The implementation and package specification must not be allowed to
diverge.

------------------------------------------------------------------------

# 47. Recommended Next Review

Before adding substantially more UI, review the current package-format
documentation against the implementation.

Priority review areas:

1.  `LiturgicalItemTarget.Qolo`
2.  `LiturgicalTextRef`
3.  Qolo `verses`
4.  contextual `petgomoId`
5.  legal repetition
6.  ordering guarantees
7.  package validation rules
8.  LiturgicalItem occurrence identity

If the specification already represents these decisions correctly,
implementation can continue without changing it.

If not, update the specification first.

------------------------------------------------------------------------

# 48. Likely Next Implementation Areas

After documentation synchronization, likely next areas include one or
more of:

``` text
A. Continue extending runtime content traversal

B. Expand real package content

C. Build additional liturgical component types

D. Improve application navigation state

E. Begin replacing temporary test UI with reusable presentation components

F. Prepare the package/build boundary for Author Database export
```

The exact next area should be chosen after reviewing the package
specification and current repository state.

------------------------------------------------------------------------

# 49. What Should Not Be Done Yet

Avoid prematurely:

-   building production UI styling;
-   embedding Author Database logic into runtime;
-   inferring poetic relationships in the Core;
-   bypassing package validation;
-   creating alternate JSON-loading paths;
-   flattening contextual occurrences into canonical entities;
-   deduplicating ordered liturgical usages;
-   treating Qolo and contextual Hymn occurrence as the same object;
-   coupling applications directly to physical JSON DTOs.

The current architecture should be extended rather than bypassed.

------------------------------------------------------------------------

# 50. Restart Point

When development resumes, use this sequence:

``` text
1. Read CurrentState.md.

2. Confirm repository main is at or beyond commit 0872227.

3. Run:
   :shared:allTests

4. Review the Application Package specification against
   contextual Qolo verses.

5. Synchronize documentation if necessary.

6. Select the next platform layer deliberately.

7. Continue from the Runtime / Package architecture,
   not from an isolated UI feature.
```

------------------------------------------------------------------------

# 51. Current State Summary

At the current milestone, SyriacPlatform has:

``` text
Platform Kernel
        │
        ▼
Platform Bootstrap
        │
        ▼
PlatformContext
        │
        ├── ContentService
        │
        └── NavigationService
                │
                ▼
Application Package Loader
        │
        ▼
Package Validation
        │
        ▼
RuntimeContentStore
        │
        ▼
RuntimeContentIndex
        │
        ▼
RuntimeContentResolver
        │
        ▼
Resolved Liturgical Content
        │
        ▼
Repository / Service API
        │
        ▼
HOME
        │
        ▼
OCCASION_DETAILS
        │
        ▼
PRAYER_DETAILS
        │
        ▼
HYMN_DETAILS
```

The platform now correctly distinguishes:

``` text
canonical content
```

from:

``` text
contextual liturgical usage
```

and has demonstrated that distinction end-to-end for contextual Qolo
verses.

This is the current implementation baseline for the next development
phase.

------------------------------------------------------------------------

# 52. Phase 7 --- Real Author Database Vertical Slice

Phase 7 has reached its first complete real-content milestone.

The verified path is:

``` text
Author Database
        ↓
controlled CSV export
        ↓
Build Tools
        ↓
Schema-v1 package
        ↓
ApplicationPackageLoader / PackageValidator
        ↓
RuntimeContentStore / RuntimeContentResolver
        ↓
Repository / ContentService / PlatformContext
        ↓
Reference Application
```

No parallel loading path is used.

## 52.1 Representative slice

For `OccN = 1`, all 52 Qolo occurrences are preserved:

``` text
20 resolved effective-Melody occurrences
29 unresolved occurrences
 3 ambiguous Melody occurrences
----------------------------------
52 total Qolo occurrences
```

Runtime states:

``` text
resolved   → effectiveMelody != null, melodyCandidates = []
unresolved → effectiveMelody = null, melodyCandidates = []
ambiguous  → effectiveMelody = null, melodyCandidates contains candidates
```

An unresolved or ambiguous Melody does not remove the Qolo occurrence.

## 52.2 Package and Core contract

`liturgical-items.json` contains all 52 real Qolo occurrences and may
carry:

``` text
effectiveMelodyId: Long?
melodyCandidateIds: List<Long>
verses: ordered contextual text references
```

Core validation checks every Melody reference actually supplied and
ensures effective/candidate Melodies belong to the referenced Qolo.
Runtime preserves unresolved and ambiguous states and does not choose a
candidate arbitrarily.

## 52.3 End-to-end verification

The generated package has successfully passed Build Tools tests, Core
Loader/Validator integration, Runtime integration, synchronization into
the Reference Application, Android debug build, and emulator visual
verification.

The emulator verifies the correct Occasion, ordered Prayers, all Qolo
occurrences, ordered contextual verses, and Petgomo displayed before its
associated Text.

------------------------------------------------------------------------

# 53. Current Restart Point

The Occasion-1 proof is complete. The immediate engineering objective is
to generalize Build Tools so generation is not tied to one representative
Occasion.

``` text
selected OccN / build configuration
        ↓
controlled export
        ↓
generic Build Tools mapping
        ↓
Schema-v1 package
        ↓
existing Core
        ↓
Reference Application
```

Next, deliberately selected additional Occasions should be used to expose
new real-domain cases before Audio, Search, or production UI work expands.

------------------------------------------------------------------------

# 54. Immediate Engineering Priorities

1. Generalize Occasion selection/configuration in Build Tools.
2. Preserve deterministic generation and stable source identities.
3. Run several representative real Occasions through the full pipeline.
4. Classify newly discovered cases at the correct architectural boundary.
5. Extend Content Domain/schema rules only when real requirements demand it.
6. Keep the Reference Application primarily as a visual smoke-test surface.


------------------------------------------------------------------------

# Implementation Alignment Update â€” 2026-08-20

This section records the verified implementation delta established after
the previous CurrentState milestone. It supplements the preceding
sections; it does not replace or reinterpret them.

## Verified baseline

The current verified repository baseline for this update is:

```text
2eac9db
```

This baseline includes the generalized real-Occasion build workflow and
end-to-end support for unresolved Qolo occurrences.

## Generalized real-Occasion build workflow

The Build Tools workflow is no longer tied to a single hard-coded
development Occasion.

A real Occasion export can be selected by `occasionId`, built into a
preview Application Package, and synchronized to the reference
application.

The verified development command is:

```powershell
.\gradlew.bat :buildtools:syncDevelopmentPreviewToReferenceApp -PoccasionId=<ID>
```

The build workflow uses the corresponding Author Database export
directory:

```text
author-database/exports/occasion-<ID>
```

and produces the generated preview under:

```text
buildtools/build/generated/occasion-<ID>-preview
```

The Gradle workflow was also verified with Configuration Cache reuse.
A repeated build with unchanged inputs can complete as up-to-date while
reusing the configuration cache.

The synchronized Compose resources may require Android Studio's
"Sync Project with Gradle Files" before an already-running development
environment reflects a newly selected Occasion. This is a development
workflow concern, not an Application Package semantic rule.

## Long text export

Long Syriac texts are now verified end-to-end without the earlier
truncation.

The failure was traced to the Author Database export path rather than to
the runtime text model. After correction, full text values were verified
in the exported `Texts.csv`, generated package JSON, and reference
application.

Long text content must therefore remain untruncated through:

```text
Author Database
    -> CSV export
    -> Build Tools mapping
    -> Application Package JSON
    -> Runtime
    -> UI
```

## Unresolved Qolo occurrences

A source occurrence with:

```text
QoloN = 0
```

has an explicit domain meaning.

It is not automatically corrupt data and it must not be silently
discarded.

It represents a real liturgical position reserved for a Qolo whose
canonical identity has not yet been established in the Author Database.
The occurrence may legitimately have no contextual texts yet.

The platform therefore supports an unresolved Qolo occurrence as a
distinct occurrence type.

Important invariants are:

1. `QoloN = 0` is a source sentinel for an unresolved Qolo occurrence.
2. It does not create canonical Qolo entity `0`.
3. It preserves the liturgical position and occurrence ordering.
4. It does not invent a Qolo name, text, melody, or canonical identity.
5. It remains distinguishable from an ordinary resolved Qolo.
6. Package validation, runtime resolution, and UI handling understand
   the unresolved occurrence explicitly.

This behavior is implemented across Build Tools, package representation,
mapping, validation, runtime resolution, and the reference UI.

## Source integrity and orphan references

Real-content testing exposed stale `ExistsInText` records whose
`TextID` values no longer existed in the Author Database.

These records resulted from an authoring-history operation in which old
long texts were deleted after being replaced by newly divided texts,
while corresponding linking records remained.

The package build correctly rejected this condition with the invariant
that every contextual Text referenced by a LiturgicalItem must exist in
canonical content.

The correct response is to repair the Author Database relationships.
Build Tools must not silently drop or synthesize missing canonical
content merely to make the package build succeed.

This failure therefore remains useful source-integrity detection.

## Verified real-content coverage

The generalized pipeline has been exercised against multiple real
Occasions with different content characteristics.

Verified results include:

- ordinary Occasion structures already used during the vertical-slice
  work;
- Occasion 84 after unresolved-Qolo support;
- Occasion 370;
- Occasion 41 after stale `ExistsInText` references were removed from
  the Author Database.

For Occasion 41, the platform successfully extracted and transported
the real content after source cleanup.

This establishes an important boundary:

> The remaining problem for Occasion 41 is not content extraction.

## Day-aware composition gap

Occasion 41 represents Holy Week and contains repeated prayer structures
across multiple days.

The current composition path groups occurrences by prayer without yet
preserving the required day-level separation represented by source
`DayN`.

The observed result is that, for example, the liturgical items belonging
to the same prayer across the week can appear together under one prayer
presentation.

The content itself is present; the missing semantic dimension is the
day-aware composition/navigation layer.

Therefore the next architectural task is:

```text
Day-aware Composition and Navigation
```

The implementation must determine how `DayN` is represented through the
generated package, runtime composition, and application navigation
without introducing a parallel content-loading path or encoding
presentation decisions into canonical content.

## Current restart point

At baseline `2eac9db`, the next development session should treat the
following as established:

```text
Author Database export       verified
Parameterized Occasion build verified
Long-text transport          verified
Unresolved Qolo occurrence   supported
Source orphan detection      verified
Real Occasion extraction     verified
Day-aware composition        next architectural task
```

The next work should extend the existing pipeline rather than replace it.


------------------------------------------------------------------------

# Architecture Planning Update â€” 2026-08-21

<!-- AUDIO-INTEGRATION-CURRENT-STATE-2026-08-21 -->

Architecture review baseline:

``` text
eabb3c9
```

Audio Integration is now the active design and implementation stage.

Day-aware Composition and Navigation remains a confirmed architectural
requirement, including the previously established `DayN` findings, but
is intentionally deferred to a later broader contextual-organization
phase.

At this documentation point no Audio implementation has yet been added
to the Core, Application Package, Build Tools, or Reference Application.

The approved Audio architecture is documented in:

- `ApplicationContentModel.md`;
- `AuthorDatabaseMapping.md`;
- `EngineeringNotebook v1.4.md`;
- `Roadmap v1.4.md`.

The physical Application Package media contract remains intentionally
open until the Author Database media schema and source mapping are
designed in detail.

------------------------------------------------------------------------

# 17. Verified Author Database -> Media Package Integration (2026-08-25)

The first complete audio-media build/package vertical slice is implemented and verified against the current Author Database.

## 17.1 Author Database and export

The Author Database now contains the physical media architecture and its indexes and relationships. For the initial audio model:

- `RECORDING` = recording of the melody itself.
- `PERFORMANCE` = recording of the complete liturgical occurrence.
- `MelodyMedia` is the authoritative melody-to-`MediaAsset` relationship for media-aware builds.
- the legacy `Melody.Record` field is no longer the recording source of truth in that path.

`modSchemaExporter.bas` now emits the official Media export under:

```text
author-database/exports/media/
â”œâ”€â”€ MediaAsset.csv
â”œâ”€â”€ MelodyMedia.csv
â”œâ”€â”€ ExistsInMedia.csv
â”œâ”€â”€ MediaTimingSet.csv
â”œâ”€â”€ MediaSegment.csv
â””â”€â”€ ExistsInTextMediaSegment.csv
```

The physical media library remains outside Git. The verified development root is `D:\SyriacPlatformMedia`; it is a build-time input only and is never serialized as an absolute package path.

## 17.2 Build Tools media path

The implemented path is:

```text
MediaSourceDataLoader
 -> MediaSourceMapper / MediaSourceData
 -> SchemaV1MediaMapper
 -> SchemaV1CanonicalMedia
 -> SchemaV1PackageMediaSelector
 -> SchemaV1CanonicalMapper
```

`SchemaV1Melody` now exposes `hasRecording` and `recordingIds[]`. In media-aware builds these are derived from canonical `MelodyMedia` relationships.

Only MediaAssets required by melodies selected for the package are retained.

## 17.3 Physical package output

An authoring relative path such as:

```text
audio/melodies/media-000217.mp3
```

becomes:

```text
media/audio/melodies/media-000217.mp3
```

The package writer emits `content/media-assets.json`, writes `recordingIds` in `content/melodies.json`, and copies only selected physical media under `media/`.

The media-aware build entry point is integrated with `:buildtools:buildOccasionPreview`. The media root can be overridden by `-PmediaLibraryRoot=<path>` or `SYRIACPLATFORM_MEDIA_ROOT`.

## 17.4 Real end-to-end verification

Occasion 2 was freshly exported from the current Microsoft Access Author Database using `ExportOccasionData 2`, then explicitly rebuilt with:

```text
.\gradlew.bat :buildtools:buildOccasionPreview -PoccasionId=2 --rerun-tasks
```

Verified result:

```text
Occasion 2 package built successfully.
Mode: media-aware
Packaged media assets: 13
Prayers: 6
Liturgical items: 51
BUILD SUCCESSFUL
```

Package integrity checks established:

- 13 packaged MediaAssets;
- 13 copied physical media files;
- every `recordingIds` reference resolved to a packaged MediaAsset;
- every packaged MediaAsset path resolved to an existing package file;
- no broken media references;
- no missing physical package files.

Thus the verified path is now:

```text
Current Author Database
 -> Occasion + Media Export
 -> Build Tools
 -> Canonical Media
 -> package-specific selection
 -> recordingIds + media-assets.json
 -> physical packaged media
```

## 17.5 Current boundary

The Build Tools / Application Package side of melody audio is implemented and verified. Core/runtime media ingestion, validation, lookup, resource resolution, reusable playback state, and Android playback are now also implemented and verified; the details are recorded in the following update.

Occurrence-level `PERFORMANCE` integration and `MediaTimingSet` / `MediaSegment` / `ExistsInTextMediaSegment` package/runtime behavior remain future work.
------------------------------------------------------------------------

<!-- RUNTIME-AUDIO-ANDROID-VERIFIED-2026-08-26 -->

# 18. Verified Core / Runtime / Android Audio Integration (2026-08-26)

Phase 9 has now crossed the Core/runtime boundary and reached verified real-content playback on Android.

The verified end-to-end path is:

```text
Author Database
        â†“
Occasion + Media Export
        â†“
Build Tools
        â†“
recordingIds + media-assets.json
        â†“
physical packaged media
        â†“
ApplicationPackageLoader
        â†“
MediaAsset + Melody.recordingIds
        â†“
Package Validation
        â†“
RuntimeContent / RuntimeContentIndex
        â†“
ContentRepository / RuntimeContentResolver
        â†“
ContentService.loadMelodyRecordings(...)
        â†“
effectiveMelody.id from the contextual hymn
        â†“
MediaAsset
        â†“
ComposeResourceMediaResourceResolver
        â†“
MediaResource URI
        â†“
DefaultAudioService
        â†“
AndroidAudioPlayerBackend
        â†“
Media3 / ExoPlayer
        â†“
audible playback
```

## 18.1 Core media ingestion

The Core now loads packaged media metadata as canonical runtime content.

Implemented support includes:

```text
MediaAsset
MediaAssetJsonDto
MediaAssetMapper
Melody.recordingIds
PackagePaths.MEDIA_ASSETS
ParsedApplicationPackage.mediaAssets
RuntimeContent.mediaAssets
RuntimeContentIndex media lookup
```

The package loader recognizes `content/media-assets.json` and preserves melody recording references through the Core.

The generated Occasion 2 package was used as the real integration fixture. It contains:

```text
51 LiturgicalItems
13 MediaAssets
13 physical media files
```

## 18.2 Media package validation

Media validation is part of normal package validation.

The implemented validation rules cover:

- MediaAsset canonical-ID uniqueness;
- melody recording references;
- consistency between recording declarations and referenced MediaAssets;
- package-relative media paths;
- invalid or unavailable media-reference states detectable at package-validation time.

Media validation is not delegated to the playback backend.

The playback layer may still report physical runtime failures, but structurally invalid package media must be rejected earlier.

## 18.3 Runtime media lookup

Runtime media lookup is exposed through the content architecture.

The repository/runtime APIs include:

```text
loadMediaAsset(MediaAssetId)
loadMelodyRecordings(MelodyId)

resolveMediaAsset(MediaAssetId)
resolveMelodyRecordings(MelodyId)
```

`ContentService` now exposes:

```text
loadMelodyRecordings(MelodyId)
```

for application-facing content traversal.

This preserves the responsibility boundary:

```text
Content/runtime
    -> decides which MediaAsset belongs to the selected content

AudioService
    -> plays the MediaAsset it receives
```

AudioService does not interpret Qolo, Melody, LiturgicalItem, Text, or Petgomo relationships.

## 18.4 Audio domain contracts

The shared Core now contains reusable audio contracts and state.

Implemented structures include:

```text
AudioService
MediaResourceResolver
AudioPlayerBackend

MediaResource
PlaybackState
PlaybackStatus
AudioPlayerEvent
```

`DefaultAudioService` is platform-neutral.

It owns command validation and canonical observable playback state.

The native backend performs actual playback operations.

## 18.5 Asynchronous player state

Native player readiness is asynchronous.

Therefore:

```text
load(MediaAsset)
        â†“
PlaybackStatus.Loading
        â†“
backend prepare
        â†“
AudioPlayerEvent.Ready(durationMs)
        â†“
PlaybackStatus.Ready
```

Likewise, actual native-player events drive:

```text
Playing
Paused
Ended
Error
```

A successful command call means that the backend accepted the command; it does not fabricate a native playback state before the player reports it.

The service lifecycle remains recoverable after a playback failure:

```text
RuntimeState.Ready
```

can coexist with:

```text
PlaybackStatus.Error
```

so another media resource may subsequently be loaded.

## 18.6 Media resource resolution

Canonical package media paths remain independent from playback-engine details.

`ComposeResourceMediaResourceResolver` maps:

```text
MediaAsset.path
    = media/audio/...

to:

files/media/audio/...
    â†“
Res.getUri(...)
    â†“
MediaResource.uri
```

The resolver does not load complete audio binaries into Core memory.

Resource resolution and playback are separate responsibilities.

This allows future Android, iOS, desktop, local, remote, or hybrid strategies without changing canonical media identity.

## 18.7 Android playback backend

Android now has a real platform implementation:

```text
AndroidAudioPlayerBackend
```

implemented with:

```text
AndroidX Media3 / ExoPlayer
```

The dependency is isolated to `androidApp`.

No Android type is introduced into `shared/commonMain`.

The backend translates player callbacks into Core events including:

```text
Ready(durationMs)
Playing
Paused
Ended
Error
```

It also provides Android-side player release behavior.

The verified Android backend milestone is:

```text
f707940
```

## 18.8 Real-content application playback

The temporary hard-coded audio smoke harness was removed after verification.

The Reference Application now derives playback from the real contextual hymn:

```text
ResolvedLiturgicalItemTarget.Qolo
        â†“
effectiveMelody
        â†“
effectiveMelody.id
        â†“
ContentService.loadMelodyRecordings(...)
        â†“
first available MediaAsset
        â†“
AudioService.load(...)
        â†“
AudioService.play()
```

The UI does not contain a hard-coded MediaAsset identity for the verified production path.

Manual verification established all of the following:

1. the original Reference Application navigation was restored;
2. the hymn-details screen displayed `Play recording`;
3. playback started successfully;
4. the played recording matched the effective Melody represented in the Author Database.

The verified real-content playback milestone is:

```text
4e0e599
```

## 18.9 Verification

The completed path passed:

```text
:shared:allTests
:shared:check
:androidApp:assembleDebug
```

as well as manual Android execution and audible playback verification.

The real Occasion 2 media package was synchronized into the local Reference Application resources for runtime testing.

Those generated package/media files are development build output and were intentionally kept separate from the code commits.

## 18.10 Current Phase 9 boundary

The following are now implemented and verified:

```text
Author Database media relationships
Build Tools media export and packaging
physical selected-media packaging
Core MediaAsset ingestion
media package validation
runtime media lookup
Melody -> recording MediaAsset resolution
Compose resource URI resolution
platform-neutral AudioService contracts
observable playback state
asynchronous backend event bridge
Android Media3 / ExoPlayer backend
real contextual Melody -> recording playback
```

Phase 9 remains active.

Important remaining work includes:

- stabilize application-facing Play / Pause / Stop controls;
- expose useful continuous playback-position updates;
- define behavior when a Melody has more than one recording;
- decide the final AudioService ownership / bootstrap integration point;
- strengthen lifecycle handling beyond the current Android activity owner;
- add additional focused runtime/player integration tests where practical;
- implement an iOS backend during the planned cross-platform phase;
- later implement occurrence-level `PERFORMANCE` media;
- later integrate authoritative timing sets, segments, and verse synchronization;
- later implement queue / Play All behavior from application requirements.

The current Android wiring deliberately does not yet register AudioService in `PlatformContext`.

`MainActivity` constructs the Android backend and passes an initialized `AudioService` into the shared `App` entry point.

This is an explicit intermediate boundary while the playback contract is stabilized, not a final statement that Audio must remain outside the platform service context.

## 18.11 Representative implementation commits

The runtime audio implementation was developed through small verified commits, including:

```text
4298f01  Integrate media assets into Core runtime ingestion
fad9df7  Fix UTF-8 encoding in Core media files
556a41f  Add Core media package validation
92dd2a0  Add runtime media lookup APIs
e789c9a  Add Core audio domain contracts
e70cd6c  Add default audio service state machine
bb3e7d9  Add audio player backend boundary
16526ad  Add Compose resource media resolver
f67d438  Add asynchronous audio backend events
f707940  Add Android Media3 audio backend
4e0e599  Wire runtime melody recordings to Android audio playback
```

These commits build on the previously verified Author Database / Build Tools media milestone:

```text
40acabd  Integrate audio media into Author Database and Build Tools
```

## 18.12 Restart point

The next development session should treat this as established:

```text
real package media              verified
Core media ingestion            verified
Core media validation           verified
runtime media lookup            verified
content-driven recording lookup verified
Android native playback         verified
real Melody recording match     verified
```

The next work should improve reusable playback behavior rather than create a second audio-loading path.

------------------------------------------------------------------------

<!-- PHASE-9-AUDIO-PAUSE-CHECKPOINT-2026-08-27 -->

# 19. Phase 9 Audio Stabilization and Multiple-Recording Checkpoint (2026-08-27)

This section is the authoritative restart checkpoint for the current development pause.

Verified implementation baseline:

```text
eda3274
```

The earlier real-content Android playback path remains intact and has now been extended through application-facing playback stabilization and explicit multiple-recording selection.

## 19.1 Playback command semantics

`DefaultAudioService` now has verified stable command behavior:

```text
play while Playing  -> success without duplicate backend play
pause while Paused  -> success without duplicate backend pause
stop while Idle     -> success without backend stop
play after Ended    -> seek(0) then play
```

Focused service tests, `:shared:allTests`, and `:shared:check` passed after this stabilization.

Representative implementation milestone:

```text
b376a09  Stabilize audio playback command semantics
```

## 19.2 Continuous Android playback position

`AndroidAudioPlayerBackend` now reports playback position continuously while Playing.

Verified behavior:

```text
Playing -> immediate position + periodic updates
Pause   -> final position, polling stops
Buffer  -> polling stops until Playing resumes
Seek    -> current native position emitted immediately
Ended   -> final position, polling stops
Error   -> polling stops
Stop    -> polling stops
Release -> polling stops
```

The current Android polling interval is 250 ms.

The Reference Application displays live `PlaybackState.positionMs` and the value was manually verified to increase during playback, stop during Pause, resume during Play, and return to 0 after Stop.

Representative milestones:

```text
10738f7  Add continuous Android audio position reporting
e0a8b92  Show live audio playback position
```

## 19.3 Application-facing playback controls

`HYMN_DETAILS` now provides state-aware Play / Pause / Stop controls.

Verified behavior:

```text
Idle / Error -> Play loads the selected recording and auto-plays
Loading      -> disabled Loading state
Ready        -> Play
Playing      -> Pause + Stop
Paused       -> Play + Stop
Ended        -> replay is available; obsolete Stop is not retained
```

Manual Android verification confirmed:

- Pause freezes playback position;
- Play resumes from the paused position;
- Stop returns position to 0;
- replay after Stop starts correctly.

Representative milestones:

```text
1eb943c  Add audio playback controls to hymn details
d00d8cc  Fix audio controls formatting
```

## 19.4 Seek bar

`HYMN_DETAILS` now exposes a seek slider driven by canonical `PlaybackState`.

The slider uses local drag state while the user is dragging so native position updates do not fight the thumb.

When dragging finishes, `seekTo(...)` is issued once.

Manual Android verification confirmed seeking while both Playing and Paused:

- Playing -> audio jumps to the selected position and continues;
- Paused  -> position changes while playback remains paused;
- the slider direction is left-to-right;
- end-of-recording control state was verified and cleaned up.

Representative milestones:

```text
43a9040  Add audio seek bar to hymn details
1e2590e  Clean up audio seek bar formatting
```

## 19.5 Performer metadata

A real multiple-recording case was selected from Occasion 107.

The Author Database `MediaAsset` source now carries optional descriptive performer metadata.

The verified source values are:

```text
MediaAsset 370 -> روفو عطالله
MediaAsset 371 -> ياسر عطالله
```

Both recordings belong to:

```text
Melody 1067
Qolo 46
```

The two physical package resources are:

```text
370 -> media/audio/melodies/media-000370.mp3
371 -> media/audio/melodies/media-000371.m4a
```

`performer` is optional metadata. It does not participate in MediaAsset identity.

The verified transport path is:

```text
Author Database MediaAsset.Performer
    -> MediaAsset.csv
    -> MediaAssetSource
    -> Schema-v1 canonical/package media
    -> content/media-assets.json
    -> MediaAssetJsonDto
    -> MediaAsset
    -> runtime/service/UI
```

UTF-8 Arabic performer names were verified directly in generated `media-assets.json`.

## 19.6 Multiple recordings for one Melody

The existing content API already supported:

```text
ContentService.loadMelodyRecordings(MelodyId)
    -> List<MediaAsset>
```

No new service contract was required.

A regression fixture verifies that Melody 1067 returns both recordings in authored order:

```text
370 -> روفو عطالله
371 -> ياسر عطالله
```

The service/runtime path preserves all recordings and performer metadata.

## 19.7 Explicit recording selection in HYMN_DETAILS

The earlier temporary policy:

```text
first available recording
```

has been replaced for multi-recording Melodies by explicit user selection.

Current behavior:

- one recording -> the existing playback flow remains simple;
- multiple recordings -> performer choices are displayed;
- the first recording is the initial selection;
- selecting another performer changes the active `MediaAsset`;
- previous playback is stopped before switching;
- selection itself does not auto-play;
- Play / Pause / Stop / Seek operate on the selected recording.

Manual Android verification with Occasion 107 and Melody 1067 confirmed that switching between the two performer recordings is smooth and that all playback controls continue to work correctly.

## 19.8 Verification status at pause

Immediately before this checkpoint, the implementation passed:

```text
:shared:allTests
:shared:check
:androidApp:assembleDebug
```

Manual Android verification also passed for:

```text
Occasion 107
Melody 1067
two recordings
two performer names
recording switching
Play
Pause
Stop
continuous position
Seek
replay
```

Occasion 2 was rebuilt when the generated integration-test baseline had become stale; its package/runtime integration tests returned to green. That failure was unrelated to `performer`.

## 19.9 Current architectural boundary

The following Phase 9 items are now considered implemented and verified:

```text
real contextual Melody -> recording playback
stable Play / Pause / Stop semantics
continuous Android position reporting
live position presentation
seek bar
optional performer metadata
multiple recordings per Melody
explicit performer-based recording selection
```

The next deliberate Phase 9 task after the pause is:

> Finalize AudioService lifecycle / ownership integration.

The current intermediate ownership remains:

```text
MainActivity
    -> constructs AndroidAudioPlayerBackend
    -> constructs/initializes AudioService
    -> passes AudioService into shared App
```

This should be reviewed before expanding into queue / Play All, iOS playback, PERFORMANCE media, or timing/segment synchronization.

## 19.10 Restart procedure

When development resumes:

```text
1. Read this section of CurrentState.md.
2. Confirm main is at or beyond eda3274.
3. Run :shared:allTests.
4. Run :shared:check.
5. Run :androidApp:assembleDebug.
6. If a visual smoke test is desired, synchronize Occasion 107.
7. Verify Melody 1067 still exposes both performer recordings.
8. Resume with AudioService lifecycle / ownership integration.
```

Do not redesign the recording-selection contract on restart unless a new real requirement demands it. The current multiple-recording path is already verified end-to-end.

<!-- PHASE-9-LIFECYCLE-AUTHORDB-CHECKPOINT-2026-09-01 -->

------------------------------------------------------------------------

# Phase 9 Completion Checkpoint --- 2026-09-01

This checkpoint supersedes earlier restart statements where they conflict
with the verified implementation state recorded below.

## Verified repository baseline

```text
b56fdeacce12c98068e623610e55c3cf9a66c40e
```

The two latest functional milestones are:

```text
336be6776870e0ae041ba45d79e59fa4bf48e515
Integrate audio service lifecycle with platform bootstrap

b56fdeacce12c98068e623610e55c3cf9a66c40e
Sync performer metadata in author database schema
```

Both commits are present on `main` and were pushed to `origin/main`.

## AudioService lifecycle and ownership

The temporary Activity-owned AudioService wiring has been replaced by
platform-owned service lifecycle management.

The verified ownership path is now:

```text
MainActivity
    |
    -> AndroidAudioPlayerBackend
            |
            v
       PlatformDependencies
            |
            v
       DefaultPlatformServices
            |
            +-> ContentService
            +-> NavigationService
            +-> AudioService
                    |
                    v
              PlatformBootstrap
                    |
                    v
               PlatformKernel
             initialize / shutdown
                    |
                    v
              PlatformContext
                    |
                    v
                   App
```

Established rules:

1. Android creates the platform-specific `AndroidAudioPlayerBackend`.
2. The backend is injected through `PlatformDependencies`.
3. `DefaultPlatformServices` creates `DefaultAudioService` only when an
   audio backend is supplied.
4. `PlatformBootstrap` registers AudioService with the Platform Kernel.
5. `PlatformContext` exposes the optional AudioService to the
   application.
6. `App` consumes the supplied PlatformContext and does not construct a
   second platform instance internally.
7. `PlatformContext.shutdown()` delegates to `PlatformKernel.shutdown()`.
8. `DefaultAudioService.shutdown()` detaches the backend listener,
   releases the backend, resets playback state, and returns the service
   runtime state to `NotInitialized`.
9. Shutdown is idempotent.
10. Platforms with no audio backend continue to construct a valid
    PlatformContext with `audio = null`.

The verified platform shutdown path is:

```text
PlatformContext.shutdown()
    -> PlatformKernel.shutdown()
    -> AudioService.shutdown()
    -> AudioPlayerBackend.release()
    -> ExoPlayer.release()
```

## Audio behavior currently verified

The reusable Android audio path now includes:

```text
content-driven recording resolution
Play / Pause / Stop command semantics
continuous position reporting
seek interaction
Ended-state replay behavior
performer metadata
multiple recordings per Melody
explicit recording selection
platform-owned lifecycle
backend release through platform shutdown
```

For multiple recordings:

- canonical `Melody.recordingIds` order is preserved;
- `ContentService.loadMelodyRecordings(...)` returns every recording in
  that order;
- the first recording is the initial selection;
- performer metadata is used as the human-readable selector label when
  available;
- selecting another recording stops previous playback;
- selection itself does not auto-play;
- Play / Pause / Stop / Seek operate on the selected recording.

The first real multiple-recording verification case remains:

```text
Occasion 107
Melody 1067

MediaAsset 370 -> روفو عطالله
MediaAsset 371 -> ياسر عطالله
```

Manual Android verification confirmed smooth switching and correct
playback controls for both recordings.

## Author Database performer synchronization

The authoritative Microsoft Access Author Database now contains:

```text
MediaAsset.Performer
Short Text
size 255
nullable
```

The official media export now selects:

```text
MediaAssetID, MediaType, SourceRelativePath, Performer
```

`Performer` is preserved through:

```text
Author Database
    -> MediaAsset.csv
    -> Build Tools source model
    -> Schema-v1 canonical/package model
    -> media-assets.json
    -> Core MediaAsset
    -> ContentService
    -> Reference Application
```

The version-controlled Author Database schema snapshot was regenerated
from the same authoritative database.

Two consecutive executions of `ExportAuthorDatabaseSchema()` produced
identical SHA-256 values for:

```text
tables.json
relationships.json
indexes.json
```

This established that the current generated schema snapshot is
deterministic for the current database state.

The committed snapshot also records the current Access relationship and
index names and the current `AllowZeroLength` state of existing fields.
These are schema-state observations, not effects of adding content rows
to Occasion 107.

## Verification

After lifecycle integration, the following completed successfully:

```text
:shared:allTests
:shared:check
:androidApp:assembleDebug
```

The Android Reference Application was then manually tested and retained
the previously verified behavior.

The multiple-recording Occasion 107 path had already passed the same
shared/build verification and manual Android execution before lifecycle
ownership was changed.

## Local generated fixture policy

The working tree may intentionally contain generated Occasion 107
Compose resources under:

```text
platform/shared/src/commonMain/composeResources/files/
```

These generated development resources are not part of the functional
source commits described above and must not be staged accidentally.

Do not use `git add .` while this fixture is present.

## Current restart point

When development resumes:

```text
1. Read CurrentState.md.

2. Confirm main is at or beyond:
   b56fdea

3. Preserve the generated Occasion 107 fixture unless deliberately
   replacing or removing it.

4. Run:
   :shared:allTests
   :shared:check
   :androidApp:assembleDebug
   before the next architectural commit.

5. Treat the following as complete:
   - stable playback commands
   - continuous position reporting
   - seek behavior
   - performer metadata transport
   - multiple-recording selection
   - AudioService lifecycle / ownership integration
   - Author Database performer/schema synchronization

6. Do not re-open Day-aware Composition, iOS playback,
   PERFORMANCE/timing/segment integration, verse synchronization,
   queue/Play All, or multiple-Melody-per-Qinto behavior unless the
   next roadmap decision explicitly selects that work.
```

## Next engineering step

The immediate task is no longer lifecycle ownership.

The next session should select the next implementation milestone from
the remaining roadmap items deliberately, using the now-stable Phase 9
Android audio foundation as the baseline.

No parallel playback path or application-specific media architecture
should be introduced.

------------------------------------------------------------------------

<!-- MULTIPLE-MELODY-SELECTION-CHECKPOINT-2026-09-02 -->

------------------------------------------------------------------------

# Phase 9 Multiple-Melody Selection Checkpoint --- 2026-09-02

This checkpoint extends the verified Phase 9 Android audio foundation.

## Verified repository baseline

```text
03014abccd705d38d646637d20523d03797e5be3
```

The two implementation milestones covered by this checkpoint are:

```text
88d0efc  Scope occasion melodies by Qinto
03014ab  Support multiple melody selection in hymn details
```

Both commits are present on `main` and pushed to `origin/main`.

## Occasion Melody export scoping

The Author Database exporter now scopes Melody export by the contextual
Qinto relationship carried by `ExistsIn`.

Established export behavior:

```text
if ExistsIn.QintoN > 0:
    include Melody rows where:
        Melody.QoloN  = ExistsIn.QoloN
        Melody.QintoN = ExistsIn.QintoN

if ExistsIn.QintoN = 0 or Null:
    include all Melody rows where:
        Melody.QoloN = ExistsIn.QoloN
```

`Melody.QintoN = 0` is not automatically added when a concrete
`ExistsIn.QintoN > 0` is present.

This prevents unrelated Melody variants of the same Qolo from leaking
into an Occasion package while preserving the deliberately unresolved
Qinto case.

## Runtime multiple-Melody representation

The existing contextual hymn model already carries:

```text
effectiveMelodyId: MelodyId?
melodyCandidateIds: List<MelodyId>
```

and runtime resolution exposes:

```text
effectiveMelody: Melody?
melodyCandidates: List<Melody>
```

The runtime preserves candidate order and does not choose among multiple
candidates arbitrarily.

## Application selection behavior

`HYMN_DETAILS` now supports the ambiguous real-content case where a Qolo
has more than one valid Melody candidate.

Established behavior:

```text
effective Melody exists
    -> use it directly

no effective Melody + exactly one candidate
    -> use the unique candidate

no effective Melody + multiple candidates
    -> require explicit user selection
```

For multiple candidates:

- the application presents the Melody choices;
- no candidate is selected arbitrarily;
- selecting a Melody stops prior playback;
- recording selection is reset;
- seek state is reset;
- selection itself does not auto-play;
- the existing recording-selection and Play / Pause / Stop / Seek flow
  operates on the selected Melody.

## Real verification case

The first verified real multiple-Melody case is:

```text
Occasion 64
Qolo 224
Qinto 2

Melody 294
Melody 295
```

The generated package preserves both candidates in
`melodyCandidateIds`.

Manual Android verification confirmed:

- both Melody names are displayed;
- neither is chosen automatically;
- either Melody can be selected;
- playback controls work after selection;
- switching Melody preserves the previously established playback
  semantics.

Verification also passed:

```text
:shared:allTests
:shared:check
:androidApp:assembleDebug
```

## Current Phase 9 boundary

The reusable Android audio/content-selection path now includes:

```text
contextual Melody resolution
multiple Melody candidates
explicit Melody selection
multiple recordings per Melody
explicit recording selection
Play / Pause / Stop
continuous position reporting
Seek
platform-owned AudioService lifecycle
Android Media3 / ExoPlayer backend
```

The next selected Phase 9 milestone is:

```text
Playback Queue / Play All
```

This work must extend the existing content-driven playback path rather
than introduce a second application-specific audio architecture.

Occurrence-level PERFORMANCE media, timing/segment synchronization,
verse synchronization, iOS playback, and Day-aware Composition remain
deferred unless explicitly selected by a later roadmap decision.

------------------------------------------------------------------------

------------------------------------------------------------------------

## Prayer Play All Checkpoint — 2026-09-02

**Verified implementation commit:** `4fc1e44`
**Source cleanup commit:** `fb78b17`

Prayer-scoped continuous playback is now implemented and verified in the Android Reference Application.

Runtime path:

``` text
RuntimePrayerSequence
        ↓
ordered ResolvedLiturgicalItems
        ↓
PrayerPlaybackQueueBuilder
        ↓
ordered PlaybackQueueEntry list
        ↓
PlaybackQueueController
        ↓
existing AudioService
        ↓
AndroidAudioPlayerBackend
```

Established behavior:

- Play All operates on one Prayer, not on an entire Occasion.
- Liturgical item order is preserved.
- An effective Melody is queued when present.
- Without an effective Melody, all author-valid Melody candidates are queued in runtime order.
- Exactly one recording is queued per Melody.
- A previously selected recording is preferred when applicable; otherwise the first available recording is used.
- Melodies without recordings are skipped.
- `AudioService` remains a single-`MediaAsset`, content-agnostic playback service.
- Pause All, Resume All, Stop All, automatic advancement, and completion are implemented.
- Opening an individual hymn or leaving the Prayer stops the active queue.

Verification completed:

- `:shared:allTests`
- `:shared:check`
- `:androidApp:assembleDebug`
- manual Android playback across consecutive recordings
- manual transition across the two valid Melodies of Qolo 224
- manual skipping of Melodies without recordings
- manual Pause All / Resume All / Stop All behavior
- automatic completion after the final queue entry

Focused automated tests cover ordering, multiple Melody candidates, recording preference/fallback, missing recordings, pause/resume, stop, completion, and unrelated playback-state filtering.

The Prayer Play All foundation is complete for the current Android runtime scope.

------------------------------------------------------------------------

# 2026-09-02 Update --- Generated Content Separation and Reference App Development Content

## Generated Content Is No Longer a Source Resource

Generated Application Package content is no longer stored under:

``` text
platform/shared/src/commonMain/composeResources/files/
```

The source tree contains software and stable presentation resources, while
generated package data is produced outside tracked source resources.

The development pipeline is now:

``` text
Author Database
        ↓
Build Tools / Exporter
        ↓
Generated Occasion Package
        ↓
Build-time Compose Resource Staging
        ↓
Reference Application
```

This preserves the architectural boundary between software and generated
content.

Generated package files and copied media are build inputs/outputs, not
canonical source-code resources and not version-controlled application
content.

## Development Content Configuration

Local Reference Application development uses:

``` text
development-content.properties
```

at the repository root.

The local file is intentionally ignored by Git. A tracked template is
provided as:

``` text
development-content.properties.example
```

The current configuration supports:

``` properties
occasionId=<positive Occasion ID>
mediaLibraryRoot=<local media library path>
```

A command-line Gradle property remains an explicit override when needed.

For normal Android Studio development, changing `occasionId` in the local
configuration selects the Occasion to build and run without copying its
generated package into the source tree.

## Software-Only Builds Remain Independent

The presence of local development configuration does not make ordinary
shared-module tests depend on Author Database exports or generated
content.

Verified:

``` text
:shared:clean :shared:allTests
```

completed successfully without executing:

``` text
:buildtools:buildOccasionPreview
:shared:prepareDevelopmentContentResources
```

This is an intentional property of the development architecture.

## Android Reference App Build

When an Android Reference Application build is requested, the local
development Occasion configuration activates the generated-content path.

The verified build path includes:

``` text
:buildtools:buildOccasionPreview
        ↓
:shared:prepareDevelopmentContentResources
        ↓
Compose resource preparation/accessor generation
        ↓
:androidApp:assembleDebug
```

Occasion 64 was used for the verified development run on 2026-09-02.

## Static Resources and Generated Package Staging

Static software resources remain tracked under:

``` text
platform/shared/src/commonMain/composeResources/
├── font/
└── drawable/
```

Generated package content remains separate.

During an activated Reference Application build,
`prepareDevelopmentContentResources` creates a build-only staging resource
tree that combines the static Compose resources with the generated package:

``` text
shared/build/generated/developmentComposeResources/
├── font/
├── drawable/
└── files/
    ├── manifest.json
    ├── content/
    └── media/
```

The two resource categories are therefore separate in their authoritative
source locations and meet only in generated build output.

This staging rule is important because Compose resource accessor generation
must continue to see static resources such as the Syriac font while the
generated package is attached for the development build.

## Verification

The separation and development workflow have been verified through:

- byte-for-byte comparison of the previous source package and generated
  staging package before removal of the source copy;
- successful shared software-only tests without generated-content tasks;
- successful generated Occasion package preparation;
- successful Android application build with automatically selected local
  development content;
- successful Android Studio emulator smoke test;
- verification that the Reference Application content, navigation, and
  previously working behavior continue to operate after the separation.

The Application Package Schema-v1 contract was not changed by this work.
The change concerns package supply, development configuration, and build
resource staging.
