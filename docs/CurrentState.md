# SyriacPlatform --- Current State

**Document:** CurrentState\
**Status:** Active implementation reference\
**Last updated:** 2026-08-18\
**Repository:** SyriacPlatform\
**Branch:** `main`\
**Current milestone:** Phase 7 --- Build Tools development-preview
package generation

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
├── effectiveMelodyId
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
├── effectiveMelody
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
        ├── effective Melody
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

# 52. Phase 7 --- Build Tools Implementation Update

The implementation has moved beyond the package/runtime foundation
described earlier in this document. A separate Build Tools module now
consumes the controlled Author Database export and maps real
representative data toward Schema v1.

The current Build Tools path is:

``` text
Author Database
        ↓
controlled export
        ↓
AuthorSourceDataLoader
        ↓
SchemaV1CanonicalMapper
        ↓
SchemaV1CompositionMapper
        ↓
DevelopmentPreviewSlice
        ↓
SchemaV1NavigationMapper
        ↓
SchemaV1PreviewPackageAssembler
        ↓
SchemaV1PackageWriter
        ↓
Schema-v1 package directory
```

The Build Tools do not create a parallel runtime model. Their output
remains the same Schema-v1 Application Package consumed by the existing
Core.

## 52.1 Representative real-content slice

The active representative source slice remains:

``` text
OccN = 1
```

The full mapped composition currently exposes two distinct states:

``` text
20 resolved Qolo occurrences
32 package-blocking source occurrences
```

The 32 blocked occurrences are retained as diagnostics in the full
composition. They are not silently repaired, assigned arbitrary
Melodies, or treated as valid package content.

`DevelopmentPreviewSlice` creates a controlled non-production preview
containing the 20 already-resolved occurrences. It preserves their
source occurrence identity and their authored relative ordering.

## 52.2 Physical package generation

`SchemaV1PackageWriter` now writes a physical development-preview
package with the standard package layout, including:

``` text
manifest.json

content/
├── entry-points.json
├── occasions.json
├── prayers.json
├── prayer-sequences.json
├── liturgical-items.json
├── texts.json
├── qolos.json
└── melodies.json

media/
```

The current writer test verifies that the preview contains 20 Qolo
Liturgical Items and that the expected Schema-v1 files and manifest
metadata are written.

`qintos.json` is deliberately absent from the current Occasion
development preview because the Occasion profile does not require that
collection and the included preview occurrences already have their
effective Melody resolved.

## 52.3 Melody boundary remains strict

The current Schema-v1/Core contract still requires a Qolo Liturgical
Item to carry a valid `effectiveMelodyId`.

Therefore:

-   a uniquely resolvable source Melody may enter the preview/package;
-   ambiguous Melody selection must not be resolved by row order, lowest
    ID, or an arbitrary default;
-   an undetermined Qinto must not be converted into a false Qinto;
-   unresolved source occurrences remain Build Tools diagnostics rather
    than partially valid runtime content.

No `melodyCandidateIds` package field has been established by the
current implementation baseline.

## 52.4 Current verified Build Tools behaviors

The current Build Tools tests establish that:

-   controlled source data can be loaded;
-   canonical entities can be mapped;
-   contextual Qolo verses and Petgomo references can be mapped;
-   deterministic PrayerSequence and EntryPoint projections can be
    produced;
-   unresolved full-composition data blocks normal navigation/package
    assembly;
-   the development preview contains only resolved occurrences;
-   preview occurrence identity and relative source ordering are
    preserved;
-   a physical Schema-v1 development-preview package can be written.

------------------------------------------------------------------------

# 53. Current Restart Point

Development should now resume from the Build Tools integration boundary
rather than from the older sample-package milestone.

The immediate path is:

``` text
generated Occasion-1 development-preview package
        ↓
existing ApplicationPackageLoader
        ↓
existing PackageValidator
        ↓
RuntimeContentStore / RuntimeContentResolver
        ↓
ApplicationPackageContentRepository
        ↓
DefaultContentService
        ↓
PlatformContext
        ↓
Reference Application
```

The next proof should establish that the package physically generated by
Build Tools can enter the existing Core unchanged.

The 32 unresolved source occurrences should remain visible as
authoring/build diagnostics and should not be hidden merely to complete
that proof.

------------------------------------------------------------------------

# 54. Current State Summary --- 2026-08-18

SyriacPlatform now has both sides of the package boundary under active
implementation:

``` text
AUTHORING / BUILD SIDE

Author Database
        ↓
controlled export
        ↓
Build Tools source loading
        ↓
canonical mapping
        ↓
composition mapping
        ↓
development preview
        ↓
Schema-v1 physical package generation


RUNTIME SIDE

Schema-v1 Application Package
        ↓
loading
        ↓
validation
        ↓
runtime store/index/resolution
        ↓
repository/service
        ↓
PlatformContext
        ↓
Reference Application
```

The remaining near-term task is to join these two already-existing sides
end-to-end with the generated real-content preview package, without
introducing a second content path or weakening Schema-v1 validation.
