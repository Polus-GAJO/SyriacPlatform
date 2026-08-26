# SyriacPlatform

## Engineering Notebook v1.3

**Status:** Official Engineering Record\
**Updated:** 2026-08-18\
**Repository:** `SyriacPlatform`\
**Branch:** `main`\
**Implementation baseline:** `06d10ee`\
**Documentation correction follows:** `3ca4c6b`

------------------------------------------------------------------------

# Purpose

This document records important engineering decisions made during the
development of SyriacPlatform.

Each decision includes:

-   Decision
-   Reason
-   Impact

The Engineering Notebook records **why** important architectural
decisions were made.

It is intentionally different from:

-   `CurrentState.md`, which records the current implementation state;
-   `ApplicationPackageSpecification.md`, which defines the physical
    package contract;
-   `DomainModel.md`, which defines conceptual domain structure;
-   `Roadmap`, which records planned implementation stages.

Historical decisions are preserved even when later decisions supersede
or refine them.

------------------------------------------------------------------------

# Decision 001

## Project Vision

### Decision

SyriacPlatform is developed as a reusable platform, not as a single
application.

### Reason

The project will support multiple applications sharing the same
liturgical foundation.

Examples:

-   Prayer applications
-   Occasion applications
-   Complete library applications

### Impact

Applications consume the platform instead of rebuilding independent
systems.

------------------------------------------------------------------------

# Decision 002

## Kotlin Multiplatform Adoption

### Decision

SyriacPlatform uses Kotlin Multiplatform (KMP).

### Reason

The platform must support Android and iOS while sharing the majority of
business logic.

### Impact

Shared logic is implemented in:

``` text
commonMain
```

Platform-specific implementations remain isolated.

------------------------------------------------------------------------

# Decision 003

## Platform Kernel Architecture

### Decision

Platform services are coordinated through the Platform Kernel.

### Reason

This creates:

-   clear responsibilities;
-   independent platform domains;
-   centralized service lifecycle;
-   easier maintenance.

### Impact

Platform domains expose contracts and services rather than allowing
application code to construct internal infrastructure directly.

------------------------------------------------------------------------

# Decision 004

## Domain Architecture

### Decision

The platform is divided into independent domains.

Initial domains include:

-   Content
-   Navigation
-   Audio
-   Search
-   User

### Reason

Each responsibility should have a clear architectural location.

### Impact

Future expansion can add capabilities without restructuring the whole
platform.

------------------------------------------------------------------------

# Decision 005

## Content and User Data Separation

### Decision

Content data and user data are separated.

### Reason

Liturgical content is shared and maintained as platform/application
content, while user information belongs to each user.

### Impact

Content updates do not require mutation of user data.

User-specific state can evolve independently from canonical liturgical
content.

------------------------------------------------------------------------

# Decision 006

## Hybrid Data Architecture

### Historical Decision

The original architecture proposed:

Build format:

``` text
JSON
```

Runtime format:

``` text
SQLite
```

### Original Reason

JSON is suitable for building and transferring packages.

SQLite can be suitable for runtime access and search when large local
datasets require database-backed indexing.

### Status

**Superseded for the current runtime architecture by Decision 014.**

SQLite is not currently the canonical runtime representation used by the
implemented Core.

It remains a possible future optimization or storage implementation if
later requirements justify it.

### Impact

The historical decision is preserved because it may remain relevant to
future large-package or search-performance work, but current application
runtime behavior must follow the validated Application Package
architecture rather than assuming SQLite.

------------------------------------------------------------------------

# Decision 007

## Build Tools Separation

### Decision

Build Tools are a separate development system.

### Reason

Data preparation, editorial processing, package generation, and
authoring logic should not exist inside runtime applications.

### Impact

Applications remain focused on consuming validated content and
presenting it to users.

The Author Database and Build Tools may evolve independently from the
runtime Core.

------------------------------------------------------------------------

# Decision 008

## Reference Application

### Decision

A Reference Application is created to validate the platform.

### Reason

The platform needs a real application environment for end-to-end
testing.

### Impact

The Reference Application demonstrates integration between:

-   package loading;
-   validation;
-   runtime content;
-   navigation;
-   services;
-   presentation.

It is also used as a visual smoke test for architectural changes.

------------------------------------------------------------------------

# Decision 009

## Documentation as Project Source

### Decision

Official engineering decisions are stored inside project documentation.

### Reason

Conversation history is not a permanent technical reference.

### Impact

The project must remain understandable independently of the conversation
in which a decision was made.

Important architectural changes should therefore be synchronized with
the relevant documentation.

------------------------------------------------------------------------

# Decision 010

## Kernel-Owned Service Initialization

### Decision

`PlatformKernel` owns initialization of all registered platform
services.

### Reason

Service lifecycle is a platform responsibility.

Applications and tests should not need to initialize each service
manually.

The registry retains ownership of its internal storage and exposes
internal iteration rather than leaking the collection of service
instances.

### Impact

The runtime flow becomes:

``` text
Register services
        ↓
PlatformKernel.initialize()
        ↓
Each registered service initializes
```

This makes service startup consistent and prepares the platform for
multiple services.

Implemented in commit:

``` text
646cc44 — Let PlatformKernel initialize registered services
```

------------------------------------------------------------------------

# Decision 011

## Central Platform Bootstrap

### Decision

Platform construction is centralized in `PlatformBootstrap`.

`PlatformBootstrap.create()`:

1.  creates `PlatformKernel`;
2.  creates platform services;
3.  registers services;
4.  calls `PlatformKernel.initialize()`;
5.  returns a ready-to-use platform context.

### Reason

Platform construction must have one authoritative location.

Without a bootstrap, each application could assemble and initialize the
platform differently.

### Impact

Applications consume a ready platform and do not need to know concrete
service implementations or startup order.

Implemented in commit:

``` text
9ebab26 — Introduce platform bootstrap
```

------------------------------------------------------------------------

# Decision 012

## Presentation Does Not Own Platform Startup

### Decision

The application UI must not create, register, or initialize platform
services.

### Reason

Compose UI is responsible for presentation.

Platform construction and lifecycle are separate responsibilities.

### Impact

The UI consumes the platform through bootstrap/context contracts and no
longer depends on direct construction of `DefaultContentService`,
repositories, or `PlatformKernel`.

Implemented in commit:

``` text
d5804a3 — Move platform startup out of App
```

------------------------------------------------------------------------

# Decision 013

## PlatformContext as the Application Boundary

### Decision

`PlatformContext` is the official high-level interface exposed to
consuming applications.

Conceptually:

``` text
PlatformContext
├── content
└── navigation
```

### Reason

Applications should consume stable platform capabilities without
knowing:

-   service registry implementation;
-   kernel internals;
-   concrete repositories;
-   package-loading details.

### Impact

Application UI code interacts with platform services through
`PlatformContext`.

Infrastructure implementation remains hidden behind platform contracts.

------------------------------------------------------------------------

# Decision 014

## Application Package as the Canonical Runtime Content Source

### Decision

The validated Application Package is the canonical source of runtime
content.

The implemented runtime path is:

``` text
Package Source
        ↓
ApplicationPackageLoader
        ↓
DTO decoding
        ↓
Domain mapping
        ↓
ParsedApplicationPackage
        ↓
Package Validation
        ↓
Runtime Content
```

### Reason

Runtime applications must not depend on the Author Database or
reconstruct editorial relationships.

The package provides a portable, deterministic, offline-capable
representation of the content prepared by Build Tools.

### Impact

The previous direct-JSON repository path was removed.

Runtime content is no longer loaded by individual feature-specific JSON
readers.

The Core consumes the package through one controlled loading path.

This decision supersedes the assumption in Decision 006 that SQLite is
the immediate runtime representation.

------------------------------------------------------------------------

# Decision 015

## Package Validation Before Runtime Availability

### Decision

A package must successfully pass validation before its content becomes
available to runtime services.

### Reason

Runtime code should be able to rely on package invariants instead of
repeatedly defending against malformed package structure.

### Impact

The conceptual path is:

``` text
Physical Package
        ↓
Parsing
        ↓
ParsedApplicationPackage
        ↓
PackageValidator
        ↓
Validated Package
        ↓
RuntimeContentStore
```

A validation failure prevents the package from becoming runtime content.

Validation issues are collected into a report rather than stopping at
the first issue.

------------------------------------------------------------------------

# Decision 016

## Layered Package Validation

### Decision

Package validation is divided into explicit validation layers.

Current layers include:

``` text
PackageValidator
├── ManifestValidator
├── CompatibilityValidator
├── ProfileValidator
├── ReferenceValidator
├── IntegrityValidator
└── SemanticValidator
```

### Reason

Different validation questions are conceptually distinct.

Examples:

-   Is the manifest structurally valid?
-   Is the package compatible with the Core?
-   Does the profile contain the required physical collections?
-   Do references resolve?
-   Are canonical IDs structurally legal?
-   Are domain relationships semantically valid?

### Impact

Validation rules remain small, testable, and composable.

New package rules should be added to the correct validation layer rather
than placed in one monolithic validator.

------------------------------------------------------------------------

# Decision 017

## Physical Collection Presence Is Distinct from Empty Content

### Decision

The package model preserves whether a canonical collection file is
physically present.

`PackageCollectionPresence` distinguishes:

``` text
present + empty list
```

from:

``` text
absent + empty list
```

### Reason

Package Profiles define required and optional physical collections.

An empty required collection can be valid, while an absent required
collection is not.

### Impact

Profile validation must not infer physical presence only from collection
size.

Package loading records physical collection presence during loading.

------------------------------------------------------------------------

# Decision 018

## Package Profiles Do Not Force Content That Does Not Exist

### Decision

Profile requirements describe package structure, not artificial content
requirements.

Examples:

-   Required means the collection file must physically exist.
-   Required does not mean the collection must be non-empty.
-   Optional collections may be absent.
-   Qinto classification is not forced for melodies that do not belong
    to the eight-Qinto system.
-   Presence of `petgomos.json` does not require every hymn or text to
    have a Petgomo.

### Reason

The package must represent real liturgical data rather than satisfy
arbitrary non-empty requirements.

### Impact

Profiles can support different application categories without distorting
the authored content.

------------------------------------------------------------------------

# Decision 019

## Central Core Compatibility Defaults

### Decision

Default Core compatibility values are centralized in:

``` text
CoreCompatibilityDefaults.CURRENT
```

### Reason

Compatibility values must not be duplicated across loaders, validators,
tests, or applications.

### Impact

Schema/Core compatibility has one authoritative default definition.

Current compatibility validation includes:

-   supported schema version;
-   target schema compatibility;
-   minimum Core version;
-   semantic version parsing.

------------------------------------------------------------------------

# Decision 020

## RuntimeContentStore and RuntimeContentIndex

### Decision

A structured runtime layer exists above `ParsedApplicationPackage`.

The runtime store conceptually contains:

``` text
RuntimeContentStore
├── RuntimeContent
└── RuntimeContentIndex
```

### Reason

Package collections are useful as canonical ordered data, but repeated
runtime lookup should not require scanning lists.

### Impact

Canonical entities can be retrieved efficiently by typed ID.

Examples include indexes for:

-   EntryPoint
-   Occasion
-   Prayer
-   PrayerSequence
-   LiturgicalItem
-   TextContent
-   Petgomo
-   Qolo
-   Melody
-   Qinto

Ordered canonical collections remain available separately.

The index does not redefine semantic ordering.

------------------------------------------------------------------------

# Decision 021

## Runtime Resolver Owns Relationship Resolution

### Decision

`RuntimeContentResolver` resolves validated package references into
structures ready for application consumption.

### Reason

UI and feature code should not repeatedly implement traversal such as:

``` text
OccasionId
→ PrayerSequenceId
→ PrayerId
→ LiturgicalItemId
→ TextId / QoloId / MelodyId
```

### Impact

Runtime resolution produces high-level structures such as:

``` text
RuntimeEntryPoint
RuntimeOccasion
RuntimePrayerSequence
ResolvedLiturgicalItem
```

The UI consumes resolved content rather than manually traversing raw
package identifiers.

------------------------------------------------------------------------

# Decision 022

## Canonical Entity and Liturgical Occurrence Are Different Concepts

### Decision

Canonical content identity is separated from contextual liturgical
usage.

Examples:

``` text
TextContent
```

is a canonical reusable text, while:

``` text
LiturgicalItemTarget.Text
```

represents one contextual use of that text.

Likewise:

``` text
Qolo
```

is a canonical entity, while:

``` text
LiturgicalItemTarget.Qolo
```

represents one contextual liturgical occurrence of that Qolo.

### Reason

The same canonical entity can appear:

-   in multiple prayers;
-   in multiple occasions;
-   multiple times in one sequence;
-   with different contextual metadata.

### Impact

Contextual information must not be moved into the canonical entity
merely because one occurrence needs it.

This distinction is a core invariant of the Content Domain.

------------------------------------------------------------------------

# Decision 023

## Liturgical Repetition Is Legal and Must Be Preserved

### Decision

Repeated use of the same canonical entity or occurrence reference can be
liturgically meaningful.

Ordered runtime traversal preserves repetition.

### Reason

Liturgical structure may intentionally repeat:

-   the same `LiturgicalItem`;
-   the same `TextContent`;
-   the same Qolo occurrence reference.

Deduplicating such lists would alter the authored liturgical structure.

### Impact

Ordered liturgical lists must not be converted to sets as part of
ordinary traversal or presentation.

Canonical ID uniqueness applies to entity definitions, not to usage
frequency.

------------------------------------------------------------------------

# Decision 024

## Contextual Qolo Verses Belong to the Liturgical Occurrence

### Decision

A contextual Qolo occurrence carries its selected ordered verses.

The domain structure is:

``` text
LiturgicalItemTarget.Qolo
├── qoloId
├── effectiveMelodyId
└── verses: List<LiturgicalTextRef>
```

Each verse reference contains:

``` text
LiturgicalTextRef
├── textId
└── petgomoId?
```

### Reason

A canonical Qolo may be reused in different liturgical contexts.

The selection, order, repetition, and contextual metadata of the verses
belong to the particular liturgical occurrence, not permanently to the
canonical Qolo.

### Impact

The same canonical Qolo may appear with:

-   different selected verses;
-   different verse order;
-   repeated verses;
-   different contextual Petgomo associations;
-   a contextually effective Melody.

`Qolo` remains reusable canonical content.

------------------------------------------------------------------------

# Decision 025

## A Verse Reference Is an Occurrence Reference, Not a Canonical Entity

### Decision

`LiturgicalTextRef` does not receive its own canonical ID.

### Reason

It represents one occurrence of an already-canonical `TextContent`
inside an ordered hymn occurrence.

Its identity is contextual:

``` text
position in the ordered occurrence
+ textId
+ contextual metadata
```

### Impact

The same `textId` may legally appear multiple times in `verses`.

For example:

``` text
verses
├── Text 601 + Petgomo 701
└── Text 601 + no Petgomo
```

Both references point to the same canonical text but represent different
liturgical usages.

------------------------------------------------------------------------

# Decision 026

## Petgomo Belongs to the Text Occurrence

### Decision

A Petgomo associated with a text belongs to the contextual occurrence of
that text.

It is not an intrinsic property of canonical `TextContent`.

### Reason

The same canonical text may appear:

-   without a Petgomo;
-   with one Petgomo;
-   elsewhere with another Petgomo.

### Impact

For a standalone text occurrence:

``` text
LiturgicalItemTarget.Text
├── textId
└── petgomoId?
```

For a hymn verse occurrence:

``` text
LiturgicalTextRef
├── textId
└── petgomoId?
```

The top-level Qolo occurrence does not own one Petgomo for all verses.

------------------------------------------------------------------------

# Decision 027

## Author Database Is Authoritative for Liturgical Relationships

### Decision

The Core does not infer which texts belong in a hymn or how they should
be ordered.

Those relationships are established before package generation by the
authoritative content source and Build Tools.

### Reason

The platform runtime is not an editorial engine.

Poetic analysis, authorial classification, and verse selection do not
belong in application runtime logic.

### Impact

The Core:

-   validates relationships;
-   resolves relationships;
-   preserves relationships;
-   exposes relationships.

The Core does not:

-   select verses;
-   infer poetic compatibility;
-   reconstruct missing liturgical relationships;
-   reinterpret Author Database decisions.

------------------------------------------------------------------------

# Decision 028

## Hymn Runtime Resolution Includes Verses

### Decision

`ResolvedLiturgicalItemTarget.Qolo` resolves the complete contextual
hymn occurrence.

Conceptually:

``` text
ResolvedLiturgicalItemTarget.Qolo
├── qolo
├── effectiveMelody
└── verses
    ├── ResolvedLiturgicalText
    ├── ResolvedLiturgicalText
    └── ...
```

Each resolved verse contains:

``` text
ResolvedLiturgicalText
├── text
└── petgomo?
```

### Reason

Presentation code should receive usable domain objects rather than
resolve IDs itself.

### Impact

Hymn details can be displayed without additional manual lookup of:

-   Text IDs;
-   Petgomo IDs;
-   Melody IDs.

Runtime traversal preserves verse order and repetition.

------------------------------------------------------------------------

# Decision 029

## Contextual Hymn Navigation Uses LiturgicalItemId

### Decision

When navigating to a contextual hymn occurrence, the application uses
`LiturgicalItemId`, not only `QoloId`.

### Reason

`QoloId` identifies the canonical Qolo but does not identify the
complete liturgical occurrence.

The same Qolo may appear with different:

-   effective Melody;
-   selected verses;
-   verse ordering;
-   Petgomo associations.

### Impact

The contextual hymn-details flow is:

``` text
LiturgicalItemId
        ↓
ContentService.loadLiturgicalItem(...)
        ↓
RuntimeContentResolver.resolveLiturgicalItem(...)
        ↓
ResolvedLiturgicalItemTarget.Qolo
```

`QOLO_DETAILS` and contextual `HYMN_DETAILS` are therefore distinct
concepts.

------------------------------------------------------------------------

# Decision 030

## UI Navigation Mirrors Liturgical Hierarchy

### Decision

The Reference Application reveals one logical level of liturgical
structure at a time.

Current flow:

``` text
HOME
        ↓
OCCASION_DETAILS
        ↓
PRAYER_DETAILS
        ↓
HYMN_DETAILS
```

### Reason

Displaying all nested content at the prayer level collapses domain
boundaries and makes contextual structure unclear.

### Impact

The UI behaves as follows:

-   HOME lists Occasions.
-   OCCASION_DETAILS lists prayers/prayer sequences.
-   PRAYER_DETAILS displays ordered liturgical components.
-   HYMN_DETAILS displays the selected contextual hymn and its ordered
    verses.

A Qolo component on `PRAYER_DETAILS` does not expand all its verses
inline.

------------------------------------------------------------------------

# Decision 031

## Runtime Discovery and Traversal Are Separate Use Cases

### Decision

The Content API distinguishes discovery from resolved traversal.

Discovery examples:

``` text
loadEntryPoints()
loadOccasions()
```

Resolved traversal examples:

``` text
loadDefaultEntryPoint()
loadOccasion(id)
loadLiturgicalItem(id)
```

### Reason

Applications may need to list available starting points without
immediately resolving every nested relationship.

### Impact

The Core does not force one UI start behavior.

Different consuming applications may:

-   open a default EntryPoint directly;
-   display available Occasions;
-   choose another discovery flow.

------------------------------------------------------------------------

# Decision 032

## Sample Package Is a Runtime Smoke-Test Asset

### Decision

The embedded sample package is retained as a minimal development package
instead of being returned to completely empty collections after each
test.

### Reason

A small valid package provides a fast end-to-end verification path:

``` text
Package
→ Loading
→ Validation
→ Runtime
→ Repository
→ Service
→ Navigation
→ UI
```

### Impact

The sample package may contain intentionally simplified content used to
verify architecture.

It must not be confused with final production liturgical content.

------------------------------------------------------------------------

# Decision 033

## Schema Documentation Must Follow Structural Runtime Decisions

### Decision

A change to the physical Application Package structure is not complete
until the package specification is synchronized.

### Reason

Code and schema documentation must not become separate sources of truth.

The contextual Qolo `verses` change modified the physical structure of
`liturgical-items.json`.

### Impact

`ApplicationPackageSpecification.md` was updated to document:

-   contextual Qolo verses;
-   `LiturgicalTextRef` physical representation;
-   ordering;
-   legal repetition;
-   contextual Petgomo;
-   referential-integrity requirements;
-   responsibility boundaries between Author Database, Build Tools,
    Core, and applications.

Documentation synchronization is part of completing a schema-level
engineering change.

------------------------------------------------------------------------

# Current Architecture Snapshot

The current content path is:

``` text
Author Database
        ↓
Build Tools
        ↓
Application Package
        ↓
PackageSource
        ↓
ApplicationPackageLoader
        ↓
DTO decoding
        ↓
Domain mapping
        ↓
ParsedApplicationPackage
        ↓
PackageValidator
        ↓
RuntimeContentStore
        ↓
RuntimeContentIndex
        ↓
RuntimeContentResolver
        ↓
ApplicationPackageContentRepository
        ↓
DefaultContentService
        ↓
PlatformContext
        ↓
Reference Application
```

The current liturgical presentation path is:

``` text
Occasion
        ↓
PrayerSequence
        ↓
Prayer
        ↓
LiturgicalItem
        ↓
Contextual Qolo occurrence
        ↓
Ordered resolved verses
```

------------------------------------------------------------------------

# Current Engineering Status

Completed major engineering stages include:

-   Repository foundation
-   Kotlin Multiplatform Android foundation
-   Shared common foundation
-   Platform Kernel
-   Central Bootstrap
-   PlatformContext application boundary
-   Reactive Navigation
-   Canonical Content Domain
-   Application Package Specification v1
-   Package Profiles v1
-   Package Loading v1
-   Manifest Validation
-   Compatibility Validation v1
-   Profile Validation v1
-   Reference Validation v1
-   Integrity Validation v1
-   Semantic Validation v1
-   Package Validator integration
-   RuntimeContent
-   RuntimeContentIndex
-   RuntimeContentStore
-   RuntimeContentResolver
-   Runtime discovery API
-   Runtime traversal API
-   Contextual Qolo verse model
-   Contextual Petgomo handling
-   Hymn runtime resolution
-   Occasion → Prayer → Hymn UI flow
-   End-to-end Android emulator verification
-   Application Package specification synchronization

Current documentation baseline:

``` text
d3bf4b9
```

The next engineering stage should be selected only after reviewing the
Roadmap against this completed runtime/package milestone.

------------------------------------------------------------------------

# Verification Discipline

Architectural changes should continue to be verified with:

``` powershell
.\platform\gradlew.bat -p .\platform :shared:allTests
```

``` powershell
.\platform\gradlew.bat -p .\platform :shared:check
```

``` powershell
.\platform\gradlew.bat -p .\platform :androidApp:assembleDebug
```

UI-flow changes should additionally be verified in the Android emulator.

------------------------------------------------------------------------

# Restart Guidance

When a new development conversation or implementation phase begins:

1.  Read `CurrentState.md`.
2.  Read this Engineering Notebook when architectural reasoning is
    needed.
3.  Consult `ApplicationPackageSpecification.md` for package-format
    rules.
4.  Confirm `main` is at or beyond the documented baseline.
5.  Run the shared test suite before major changes.
6.  Extend existing architecture rather than creating parallel loading
    or runtime paths.

------------------------------------------------------------------------

End of Engineering Notebook v1.2

------------------------------------------------------------------------

# Decision 034

## Build Tools Must Target the Existing Schema-v1/Core Path

### Decision

The first Author Database integration is implemented as a separate Build
Tools pipeline whose output is the existing Schema-v1 Application
Package.

Build Tools must not create a second runtime content model or bypass the
existing loader, validator, runtime store, resolver, repository, or
service layers.

### Reason

The package/runtime architecture was already validated before Author
Database integration began. A parallel path would create two competing
definitions of runtime content and would undermine the Application
Package as the canonical runtime boundary.

### Impact

The implementation direction is:

``` text
Author Database export
        ↓
Build Tools
        ↓
Schema-v1 package
        ↓
existing Core
```

------------------------------------------------------------------------

# Decision 035

## Qolo Occurrence Identity Is Independent from Melody Resolution

### Decision

A contextual Qolo occurrence remains valid even when one effective Melody
has not been resolved.

### Reason

Legitimate source states include undetermined Qinto and multiple Melody
candidates. Removing the Qolo would destroy valid liturgical structure.

### Impact

`LiturgicalItemTarget.Qolo` now carries nullable `effectiveMelodyId`,
`melodyCandidateIds`, and ordered `verses`.

------------------------------------------------------------------------

# Decision 036

## Melody Resolution Has Three Explicit States

### Decision

The package/runtime preserve resolved, unresolved, and ambiguous states.

``` text
resolved   → effectiveMelodyId != null, candidates empty
unresolved → effectiveMelodyId = null, candidates empty
ambiguous  → effectiveMelodyId = null, candidates preserved
```

### Impact

Runtime exposes `effectiveMelody: Melody?` and
`melodyCandidates: List<Melody>` and never selects a candidate by an
arbitrary heuristic.

This refines Decision 028: verse resolution remains, while Melody
cardinality is now explicit.

------------------------------------------------------------------------

# Decision 037

## Schema v1 Preserves Unresolved and Ambiguous Melody State

### Decision

Qolo Liturgical Items support nullable `effectiveMelodyId` and
`melodyCandidateIds`.

### Impact

Every supplied effective/candidate Melody must resolve and belong to the
same Qolo. A missing effective Melody does not invalidate the occurrence.
Candidate order does not imply preference.

This corrects the temporary documentation assumption that every Qolo
required one non-null effective Melody.

------------------------------------------------------------------------

# Decision 038

## Occasion-1 Package Preserves All 52 Qolo Occurrences

### Decision

The generated Occasion-1 package contains every mapped Qolo occurrence,
not only the 20 with one resolved effective Melody.

### Impact

``` text
20 resolved
29 unresolved
 3 ambiguous
------------
52 total
```

`DevelopmentPreviewSlice` no longer means a resolved-only subset; its
name may be revisited when Build Tools are generalized.

------------------------------------------------------------------------

# Decision 039

## Generated Real Content Must Pass the Existing Core Unchanged

### Decision

Build Tools output must enter the existing loader, validator, runtime,
repository, service, PlatformContext, and Reference Application path.

### Impact

That full path has now been verified for Occasion 1, including the 52
Qolo occurrences and all three Melody-resolution states.

------------------------------------------------------------------------

# Decision 040

## Reference Application Remains a Visual Smoke-Test Surface

### Decision

Presentation work at this stage should primarily improve inspection of
real content, not begin final production UI design.

### Impact

Right alignment, readable Syriac fonts, and correct Petgomo-before-Text
presentation are useful verification aids. Final styling should later be
centralized in a presentation/theme layer rather than accumulated in
`App.kt`.

------------------------------------------------------------------------

# Current Architecture Snapshot

``` text
Author Database
        ↓
controlled export
        ↓
Build Tools
        ↓
Schema-v1 Application Package
        ↓
ApplicationPackageLoader / PackageValidator
        ↓
RuntimeContentStore / RuntimeContentResolver
        ↓
Repository / ContentService / PlatformContext
        ↓
Reference Application
```

The verified representative package contains all 52 Qolo occurrences and
preserves resolved, unresolved, and ambiguous Melody states.

------------------------------------------------------------------------

# Current Engineering Status

Phase 7 now includes controlled Author Database export, Build Tools
mapping, physical package generation, nullable/candidate Melody state,
Core integration tests, Runtime integration tests, Reference Application
synchronization, and Android emulator verification.

The next major stage is Build Tools generalization across multiple
Occasions.

------------------------------------------------------------------------

# Restart Guidance

1. Read `CurrentState.md`.
2. Consult this notebook for architectural reasoning.
3. Consult `ApplicationPackageSpecification.md` for physical package rules.
4. Consult `AuthorDatabaseMapping.md` for source interpretation.
5. Run relevant Build Tools/shared tests before major changes.
6. Extend the existing package/runtime path rather than creating a parallel path.

------------------------------------------------------------------------

End of Engineering Notebook v1.3


------------------------------------------------------------------------

# Engineering Update â€” 2026-08-20

## Baseline

Verified repository baseline:

```text
2eac9db
```

This update records the engineering work completed after the previous
notebook revision.

## Generalized Occasion preview workflow

The development preview workflow was generalized so that a real
Occasion can be selected by Gradle property instead of being fixed to a
single sample.

Verified command:

```powershell
.\gradlew.bat :buildtools:syncDevelopmentPreviewToReferenceApp -PoccasionId=<ID>
```

The workflow builds from:

```text
author-database/exports/occasion-<ID>
```

through the unified `OccasionPackageBuilder`, writes a generated preview,
and synchronizes it to the reference application.

The Gradle tasks were corrected so that Configuration Cache can be
stored and reused. Consecutive unchanged runs were verified as
successful and up-to-date.

## Long-text investigation

Real Occasion testing exposed truncated Syriac text.

The investigation separated the pipeline into stages:

```text
Access
-> exported Texts.csv
-> generated package
-> synchronized Compose resources
-> runtime/UI
```

The truncation was ultimately corrected at the export path. Full text
was then verified in `Texts.csv`, generated JSON, and the running
reference application.

This confirmed the value of diagnosing the earliest stage at which data
changes rather than assuming the runtime/UI is responsible.

## Unresolved Qolo design

Occasion 84 exposed a source row with `QoloN = 0`.

Initial mapping treated every Qolo row as a canonical Qolo and failed
because canonical fields such as `QoloSerch` were absent.

Author Database semantics clarified that `QoloN = 0` is deliberate: it
reserves a real liturgical position for a Qolo whose identity has not
yet been established.

The implementation was therefore extended with an explicit unresolved
Qolo occurrence rather than deleting the occurrence or creating fake
canonical content.

The support now crosses:

```text
Build Tools composition
-> preview package
-> package JSON mapping
-> package validation
-> runtime resolver
-> reference UI
```

The canonical Qolo collection does not gain Qolo `0`.

Relevant implementation commit:

```text
2eac9db Support unresolved Qolo occurrences across package runtime
```

## Real source-integrity failure

Holy Week testing exposed contextual Text IDs that were referenced by
`ExistsInText` but no longer existed in `Texts`.

The missing IDs were traced to stale relationship rows left after older
long texts had been divided into smaller texts and the old canonical
texts deleted.

The Author Database was cleaned by removing the invalid relationship
rows.

After cleanup, package generation successfully transported the real
content.

The existing failure:

```text
Not every contextual Text referenced by LiturgicalItems exists in canonical content.
```

is therefore retained as correct validation behavior.

## Real Occasion results

The generalized pipeline has now been exercised beyond the initial
sample Occasions.

Verified examples include Occasion 84 and Occasion 370.

Occasion 41 (Holy Week) also successfully exports and transports its
content after source cleanup.

The remaining Holy Week defect is structural presentation/composition:
items from the same prayer across several days are currently grouped
under one prayer because `DayN` is not yet represented in the
composition/navigation model.

## Engineering conclusion

The tests establish that the platform does not need a separate import
mechanism for complex multi-day Occasions.

The next task is to extend the existing composition model with the
missing day dimension.

Next engineering target:

```text
Day-aware Composition and Navigation
```

The design must preserve:

- one Author Database export pipeline;
- one Application Package pipeline;
- canonical entity versus liturgical occurrence separation;
- ordered/repeated liturgical usage;
- explicit validation rather than silent repair.


------------------------------------------------------------------------

# Audio Integration Architecture Update â€” 2026-08-21

<!-- AUDIO-INTEGRATION-ENGINEERING-2026-08-21 -->

## Baseline

Architecture review baseline:

``` text
eabb3c9
```

No Audio implementation change is implied by this documentation update.

The purpose of this update is to establish the media architecture before
modifying the Author Database, Application Package, Core, or Reference
Application.

# Decision 041

## MediaAsset Is a Reusable Platform Entity

### Decision

MediaAsset is an independent reusable platform entity.

It is not owned by Melody, Qolo, LiturgicalItem, or application UI.

### Reason

The same physical/logical media resource may legitimately be reused by
multiple content entities and multiple applications.

Embedding file paths or audio fields directly in content entities would
couple content identity to storage and distribution.

### Impact

Content-to-media association is represented through explicit
relationships.

Media binaries can evolve independently from canonical liturgical
entities.

# Decision 042

## Melody-to-Media Is Many-to-Many

### Decision

Melody and MediaAsset use an explicit many-to-many relationship.

Conceptually:

``` text
Melody
   *
   â”‚ MelodyMedia
   *
MediaAsset
```

### Reason

Several Melody identities may use the same actual recorded performance.

Duplicating the media file for each Melody would create unnecessary
storage duplication and inconsistent media identity.

### Impact

`MelodyMedia` owns relationship-specific properties such as `role` and
`sort`.

MediaAsset does not contain `MelodyId`.

# Decision 043

## Media Role Belongs to the Relationship

### Decision

The semantic purpose of a media resource in relation to content belongs
to the relationship rather than MediaAsset itself.

### Reason

Media type answers what kind of resource exists.

Relationship role answers why that resource is associated with this
content.

The same MediaAsset may potentially have different relationship roles in
different contexts.

### Impact

Concepts such as `RECORDING`, future teaching/reference roles, and
relationship ordering are modeled on media relationships.

# Decision 044

## Authoring Media Location Is Relative

### Decision

The Author Database stores a source-relative media path rather than an
absolute workstation path or deployment URL.

### Reason

Authoring media roots vary between computers and environments.

Deployment locations may also change independently from authored
content.

### Impact

Authoring resolution follows:

``` text
environment-specific media root
        +
SourceRelativePath
        =
authoring source file
```

Absolute local paths and cloud-provider URLs do not become canonical
media identity.

# Decision 045

## Media Distribution Is Independent from Authoring

### Decision

Local, embedded, remote, and hybrid media access are runtime/distribution
policies rather than Author Database relationships.

### Reason

Moving media between package storage, device cache, CDN, or another
cloud provider must not require rewriting content relationships.

### Impact

The same MediaAsset identity can be resolved through different physical
resource strategies.

A future hybrid resolver may prefer a valid local resource and fall back
to a remote source when required.

# Decision 046

## MediaAsset Identity Is Independent from Exact Binary Bytes

### Decision

MediaAsset identifies the logical media resource rather than one exact
binary encoding.

### Reason

Noise reduction, normalization, metadata correction, re-encoding, or
replacement of a damaged copy should not require rebuilding all content
relationships.

### Impact

The same MediaAssetId may remain while the physical file changes.

A materially different performance or resource receives a new
MediaAssetId.

Build-derived checksum metadata distinguishes physical binary revisions.

# Decision 047

## Checksums Detect Physical Media Changes

### Decision

Physical media changes are detected through build-derived cryptographic
checksums rather than by changing MediaAsset identity.

### Reason

A media file may be replaced under the same source-relative path while
remaining the same logical resource.

Installed or cached applications need a deterministic way to determine
whether their local binary is current.

### Impact

Conceptually:

``` text
same MediaAssetId
+
same checksum
    -> same physical revision

same MediaAssetId
+
different checksum
    -> updated physical revision
```

A manual media revision number is not required solely for binary-change
detection.

# Decision 048

## Contextual Media Is Separate from Melody Media

### Decision

Media associated with a reusable Melody is distinct from media associated
with one contextual LiturgicalItem.

### Reason

A teaching/reference recording of a Melody and a performance of one
specific liturgical occurrence represent different relationships even
when both are audio.

### Impact

The architecture distinguishes `MelodyMedia` from
`LiturgicalItemMedia`.

Both may reference reusable MediaAsset entities.

# Decision 049

## Media Timing Is Not a MediaAsset Property

### Decision

Verse/text playback timing does not belong to MediaAsset itself.

### Reason

Timing describes how structured content maps onto temporal regions of a
media resource.

The same MediaAsset may be used in several liturgical contexts.

### Impact

`startMs` and `endMs` are modeled through reusable timing structures
rather than as MediaAsset fields.

# Decision 050

## Timing Sets and Segments Are Reusable

### Decision

Temporal segmentation is represented through reusable MediaTimingSet and
MediaSegment entities.

Conceptually:

``` text
MediaAsset
    â†“
MediaTimingSet
    â†“
ordered MediaSegments
```

### Reason

The same recording and the same temporal divisions may be reused by
different liturgical occurrences.

Duplicating start/end values for every `ExistsIn` occurrence would create
redundant authoring data and synchronization risk.

### Impact

Multiple contextual media relationships may reference the same
MediaTimingSet.

Timing is authored once and reused where semantically valid.

# Decision 051

## Contextual Text Occurrences Map to Reusable Media Segments

### Decision

The association between a contextual text occurrence and a temporal
segment is represented explicitly.

Conceptually:

``` text
contextual Text occurrence
        â†“
LiturgicalTextMediaSegment
        â†“
MediaSegment
```

### Reason

Different contextual text occurrences may use the same canonical Text,
the same recording, and the same timing segment while remaining distinct
liturgical occurrences.

### Impact

Different `ExistsInText` identities can reference the same
MediaSegment.

This preserves contextual identity without duplicating authoritative
timing information.

# Decision 052

## AudioService Does Not Interpret Liturgical Structure

### Decision

The future AudioService is responsible for playback, not for resolving
liturgical relationships.

### Reason

Audio playback and content interpretation are separate platform
responsibilities.

### Impact

Content/runtime resolution determines a MediaAsset and optional
`startMs` / `endMs`.

AudioService performs operations such as play, pause, resume, seek, and
stop and exposes playback state.

The AudioService does not select Melody, Qolo, Text, Petgomo, or
MediaSegment relationships.

# Decision 053

## Hybrid Media Resolution Is a Supported Architectural Direction

### Decision

The media architecture must permit local and remote resource resolution,
including a hybrid strategy.

### Reason

Applications may require offline media while also benefiting from
cloud-hosted updates and selective downloads.

### Impact

A future runtime may conceptually resolve media as:

``` text
valid local resource
    -> use local

otherwise remote resource available
    -> retrieve/cache/use remote

otherwise
    -> unavailable
```

Content identity and media relationships remain unchanged regardless of
the physical resolution strategy.

# Decision 054

## Legacy RofMP3 Is a Migration Source, Not the Target Model

### Decision

The existing `Rof mp3.accdb` Attachment-based storage is treated as a
legacy migration source.

### Reason

Embedding audio binaries in an auxiliary Access database couples
authoring storage to Access and prevents clean local/cloud/hybrid media
distribution.

### Impact

Existing attachments will eventually be extracted into an external media
library and represented through MediaAsset and MelodyMedia records.

Migration must be verified before the legacy source is retired.

# Decision 055

## Day-Aware Composition Is Deferred to Broader Contextual Organization

### Decision

Day-aware Composition and Navigation is intentionally deferred.

Audio Integration becomes the active architecture stage.

### Reason

The `DayN` requirement remains valid, but it belongs to a broader
contextual-organization design rather than needing to block the now
well-defined Audio Integration work.

The real-content pipeline has already established the stable content
identities and contextual occurrence boundaries required by media.

### Impact

Audio design must not introduce Day-specific assumptions.

The existing `DayN` findings remain authoritative and must not be lost.

A later contextual-organization phase will address Day-aware composition
together with the broader contextual model.

## Audio Architecture Snapshot

``` text
Author Database
â”œâ”€â”€ MediaAsset
â”œâ”€â”€ MelodyMedia
â”œâ”€â”€ LiturgicalItemMedia
â”œâ”€â”€ MediaTimingSet
â”œâ”€â”€ MediaSegment
â””â”€â”€ LiturgicalTextMediaSegment
        â†“
Build Tools
        â†“
Application Package media representation
        â†“
Package Validation
        â†“
Runtime media resolution
        â†“
Content / Media relationship resolution
        â†“
AudioService
        â†“
platform playback backend
        â†“
Application UI
```

The physical Application Package representation remains a separate
schema decision and must be documented before implementation publishes
new canonical media collections.

------------------------------------------------------------------------

# Decision 056

## Canonical Audio Media Source

### Decision

For media-aware builds, `MelodyMedia` is the authoritative source of melody-recording relationships.

The legacy `Melody.Record` Boolean must not determine published recording availability when canonical Media data is available.

`SchemaV1Melody` exposes:

```text
hasRecording
recordingIds[]
```

where `recordingIds` reference packaged MediaAsset identities.

### Reason

A Boolean cannot represent multiple recordings, stable media identity, media type, ordering, shared recordings, or future media metadata.

### Impact

The authoritative media-aware path is:

```text
MediaAsset + MelodyMedia
        v
MediaSourceData
        v
SchemaV1CanonicalMedia
        v
package-specific media selection
        v
SchemaV1Melody.recordingIds[]
```

The legacy non-media path remains temporarily available for compatibility, but it is not authoritative for published audio media.

------------------------------------------------------------------------

# Decision 057

## Physical Media Root Is Build-Time State

### Decision

Physical source media remains outside the Git repository and is supplied through a configurable media-library root.

`MediaAsset.SourceRelativePath` is relative to that root.

Build Tools convert that source path into a package-relative `media/...` path.

Absolute workstation paths must never enter Application Package JSON.

### Reason

The authoring filesystem is a build concern. Application Packages must remain portable across Android, iOS, desktop, tests, and future distribution mechanisms.

### Impact

The currently verified development root is:

```text
D:\SyriacPlatformMedia
```

This path is not part of the package contract.

The root may be overridden by:

```text
-PmediaLibraryRoot=<path>
```

or:

```text
SYRIACPLATFORM_MEDIA_ROOT
```

------------------------------------------------------------------------

# Decision 058

## Media References, Metadata, and Files Are Emitted Together

### Decision

`recordingIds`, `media-assets.json`, and their selected physical media files must enter the package as one build operation.

Only MediaAssets required by the current package are copied.

### Reason

Publishing recording references without corresponding MediaAsset metadata and physical files would create dangling runtime references.

Copying the complete authoring media library would make packages unnecessarily large and would weaken package isolation.

### Impact

The verified package shape now includes:

```text
content/
|-- melodies.json
`-- media-assets.json

media/
`-- ...
```

A fresh Occasion 2 build produced 13 selected MediaAssets and 13 physical media files with no broken references or missing files.

------------------------------------------------------------------------

# Decision 059

## Initial Media Role Semantics

### Decision

The initial media-role semantics are:

```text
RECORDING
= recording of the melody itself

PERFORMANCE
= recording of the complete liturgical occurrence
```

### Reason

A melody recording and a complete liturgical performance are different domain relationships even when both are represented by audio or video files.

### Impact

The currently completed Build Tools vertical slice implements melody `RECORDING`.

Occurrence-level `PERFORMANCE` and timing/segment integration remain future work.
------------------------------------------------------------------------

<!-- AUDIO-RUNTIME-DECISIONS-060-065 -->

# Decision 060

## Packaged Media Is Canonical Runtime Content

### Decision

`MediaAsset` and Melody `recordingIds` are loaded, validated, indexed, and resolved through the existing Application Package and runtime content architecture.

Audio media must not be loaded through an application-specific side channel.

### Reason

The platform already has one authoritative path for canonical content:

```text
Application Package
    -> validation
    -> RuntimeContent
    -> RuntimeContentIndex
    -> RuntimeContentResolver
    -> repository/service APIs
```

Creating a second media-loading path would bypass package validation and separate media identity from the content relationships that select it.

### Impact

The Core now includes canonical MediaAsset ingestion and runtime lookup.

Melody recording resolution is available through runtime/repository/service APIs.

Applications request recordings through content identity rather than opening package JSON or selecting physical files directly.

------------------------------------------------------------------------

# Decision 061

## Media Resource Resolution Is Separate from Playback

### Decision

Canonical `MediaAsset` identity and package-relative path resolution are separated from native playback through `MediaResourceResolver`.

### Reason

A MediaAsset is a logical package resource.

A native playback engine requires a platform-usable resource reference such as a URI.

These are different responsibilities and may vary independently across embedded, local-cache, remote, or hybrid distribution strategies.

### Impact

The current embedded-package path is:

```text
MediaAsset.path
    -> ComposeResourceMediaResourceResolver
    -> Res.getUri(...)
    -> MediaResource.uri
```

`AudioPlayerBackend` consumes `MediaResource`.

Future iOS, remote, cached, or hybrid resolvers may be introduced without changing canonical MediaAsset identity.

------------------------------------------------------------------------

# Decision 062

## AudioService Owns Canonical Playback State; Native Backends Execute Playback

### Decision

`DefaultAudioService` owns command validation and canonical observable `PlaybackState`.

`AudioPlayerBackend` executes platform playback operations.

### Reason

Shared application logic must not depend on ExoPlayer, AVFoundation, or another platform-specific player API.

At the same time, native playback engines must retain control over actual device playback.

### Impact

The shared Audio domain exposes:

```text
AudioService
PlaybackState
PlaybackStatus
AudioPlayerBackend
```

Android-specific Media3 types remain outside `commonMain`.

The same service contract can later be backed by an iOS implementation.

------------------------------------------------------------------------

# Decision 063

## Native Playback Readiness Is Asynchronous

### Decision

A successful backend command does not itself prove that the native player has reached the requested playback state.

Native state transitions are reported through `AudioPlayerEvent`.

### Reason

Real playback engines prepare media asynchronously.

Treating `prepare()` return as immediate `Ready` would make Core state incorrect and would prevent reliable handling of duration, end-of-media, and asynchronous player errors.

### Impact

The state path is:

```text
load(MediaAsset)
    -> Loading
    -> backend prepare accepted
    -> AudioPlayerEvent.Ready(duration)
    -> Ready
```

Likewise, actual backend events drive:

```text
Playing
Paused
Ended
Error
```

A playback error changes `PlaybackStatus` without permanently destroying the AudioService lifecycle state.

------------------------------------------------------------------------

# Decision 064

## Android Playback Uses Media3 Behind the Platform Boundary

### Decision

The verified Android backend is implemented with AndroidX Media3 / ExoPlayer inside `androidApp`.

### Reason

Media3 provides the native Android playback lifecycle required by the platform while preserving a clean separation from shared Core contracts.

### Impact

`AndroidAudioPlayerBackend` is platform-specific.

Media3 is an Android dependency only.

No Android Media3 type is exposed by `AudioService`, `MediaResource`, or other common contracts.

The Android backend translates native callbacks into `AudioPlayerEvent`.

------------------------------------------------------------------------

# Decision 065

## Recording Selection Follows the Resolved Effective Melody

### Decision

Application playback for a contextual hymn derives recording availability from the hymn's resolved `effectiveMelody`.

The application must not hard-code a MediaAsset identity as the production selection path.

### Reason

The Author Database and Build Tools determine the canonical Melody relationships.

The runtime already resolves the effective Melody for the contextual hymn occurrence.

AudioService must play the MediaAsset selected by content/runtime resolution rather than interpreting liturgical relationships itself.

### Impact

The verified application path is:

```text
ResolvedLiturgicalItemTarget.Qolo
    -> effectiveMelody.id
    -> ContentService.loadMelodyRecordings(...)
    -> MediaAsset
    -> AudioService
```

A temporary hard-coded MediaAsset smoke test was used only to verify the first Android backend and was then removed.

Manual Android verification confirmed that the recording selected by this real-content path matches the Melody represented in the Author Database.

The current UI uses the first available recording as the initial verified behavior.

A future explicit policy is still required when more than one recording is available.

------------------------------------------------------------------------

## Runtime Audio Verification Baseline

The real-content Android playback milestone is:

```text
4e0e599
```

The supporting Android backend milestone is:

```text
f707940
```

The runtime audio architecture remains deliberately incomplete in the following areas:

- final AudioService registration/ownership through PlatformContext;
- continuous playback-position reporting;
- multiple-recording selection policy;
- queue / Play All;
- iOS backend;
- occurrence-level PERFORMANCE media;
- timing/segment-driven verse synchronization.

These are follow-on requirements and must extend the verified path rather than bypass it.
