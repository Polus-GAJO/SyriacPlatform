# SyriacPlatform

## Roadmap v1.3

**Status:** Official Development Plan\
**Updated:** 2026-08-18\
**Verified implementation baseline:** `06d10ee`\
**Documentation correction follows:** `3ca4c6b`

------------------------------------------------------------------------

# Purpose

This document defines the implementation phases of SyriacPlatform.

The roadmap focuses on building a stable reusable platform before
expanding production features.

It records completed implementation phases, active engineering stages,
deferred work, and the intended order of future development.

Detailed implementation state belongs in `CurrentState.md`.
Architectural reasoning belongs in `EngineeringNotebook`. Physical
package rules belong in `ApplicationPackageSpecification.md`.

------------------------------------------------------------------------

# Phase 1 --- Repository Foundation

**Status:** Completed

## Objectives

-   Create project structure
-   Create official documentation
-   Establish development rules

## Completed

-   Project folders created
-   Platform Blueprint created
-   Development Guide created
-   Engineering Notebook created
-   Roadmap created
-   Project conventions introduced
-   Documentation stored with the repository
-   Documentation update discipline established

## Output

A version-controlled project foundation with official engineering
documentation.

------------------------------------------------------------------------

# Phase 2 --- Kotlin Multiplatform Foundation

**Status:** Android Foundation Completed; iOS Deferred

## Objectives

Create the real Kotlin Multiplatform project.

## Completed

-   KMP project created inside the platform module
-   Android target configured
-   Shared module created
-   Android reference application created
-   Android build process verified
-   Shared test execution verified

## Deferred

-   Configure and verify the iOS target when the project reaches the
    planned cross-platform stage

## Output

A working SyriacPlatform shared library with an active Android reference
application.

------------------------------------------------------------------------

# Phase 3 --- Platform Common Foundation

**Status:** Completed

## Objectives

Implement shared basic components.

## Completed Components

-   `Result<T>`
-   `PlatformError`
-   `ErrorCode`
-   `PlatformId`
-   Typed IDs
-   `RuntimeState`
-   `Version`

## Output

The reusable common foundation used by platform domains and runtime
infrastructure.

------------------------------------------------------------------------

# Phase 4 --- Platform Kernel and Startup

**Status:** Foundation Completed

## Objectives

Implement the central runtime and application startup boundary.

## Completed

-   `PlatformService`
-   `ServiceMetadata`
-   `ServiceRegistry`
-   `PlatformKernel`
-   Registration and resolution by `KClass`
-   Kernel-managed service initialization
-   Registry encapsulation
-   `PlatformBootstrap`
-   Centralized platform construction
-   `PlatformContext`
-   UI decoupled from service construction

## Deferred Until Required

-   Command dispatcher
-   Event dispatcher
-   Extended lifecycle behavior
-   Failure orchestration across multiple service initializations

## Output

A reusable platform startup architecture in which applications consume a
ready `PlatformContext`.

------------------------------------------------------------------------

# Phase 5 --- Navigation and Reference Application Foundation

**Status:** Completed for Current Runtime Scope

## Objectives

Create the first reusable navigation architecture and connect it to the
Reference Application.

## Completed

-   `AppDestination`
-   `NavigationState`
-   `NavigationController`
-   Navigation service contract
-   Reactive navigation state
-   Navigation tests
-   Navigation exposed through `PlatformContext`
-   Reference Application connected to platform navigation
-   Contextual destinations added as required by real content traversal

Current verified flow includes:

``` text
HOME
    ↓
OCCASION_DETAILS
    ↓
PRAYER_DETAILS
    ↓
HYMN_DETAILS
```

The earlier minimal Qolo flow was a development milestone and no longer
defines the current application architecture.

## Output

A working navigation layer capable of representing real liturgical
hierarchy without placing platform construction logic in the UI.

------------------------------------------------------------------------

# Phase 6 --- Content Domain and Application Package Runtime

**Status:** Core Runtime Foundation Completed

## Objectives

Implement real liturgical content handling using a validated Application
Package.

## Completed --- Canonical Content Domain

The implemented content foundation includes canonical entities and typed
identifiers required by the current package/runtime flow, including:

-   Entry Points
-   Occasions
-   Prayers
-   Prayer Sequences
-   Liturgical Items
-   Text Content
-   Petgomos
-   Qolos
-   Melodies
-   Qinto-related content used by the current schema

## Completed --- Application Package

-   Application Package Specification v1
-   Canonical collection format
-   Package manifest
-   Package profiles
-   Package source abstraction
-   DTO decoding
-   Domain mapping
-   Parsed package representation
-   Physical collection-presence tracking

## Completed --- Package Validation

-   Manifest validation
-   Compatibility validation
-   Profile validation
-   Reference validation
-   Integrity validation
-   Semantic validation
-   Validation-report aggregation

## Completed --- Runtime Layer

-   `RuntimeContent`
-   `RuntimeContentIndex`
-   `RuntimeContentStore`
-   `RuntimeContentResolver`
-   Resolved Entry Point
-   Resolved Occasion
-   Resolved Prayer Sequence
-   Resolved Liturgical Item
-   Runtime discovery API
-   Runtime traversal API

## Completed --- Contextual Hymn Model

-   Canonical Qolo separated from contextual Qolo occurrence
-   `LiturgicalItemTarget.Qolo`
-   Contextual `effectiveMelodyId`
-   Ordered `verses`
-   `LiturgicalTextRef`
-   Contextual `petgomoId`
-   Legal repeated verse references
-   Runtime resolution of contextual hymn verses
-   Reference validation for verse Text and Petgomo references
-   Package specification synchronized with the implementation

## Completed --- End-to-End Sample Flow

The embedded sample package verifies:

``` text
Application Package
        ↓
Loading
        ↓
Validation
        ↓
Runtime Store
        ↓
Runtime Resolver
        ↓
Repository
        ↓
Content Service
        ↓
PlatformContext
        ↓
Reference Application
```

The Android Reference Application has been visually verified through:

``` text
Occasion
    ↓
Prayer
    ↓
Liturgical components
    ↓
Hymn
    ↓
Ordered verses
```

## Remaining Before Production Content

-   Define additional content relationships only when driven by real
    application requirements
-   Expand package/runtime support as production content exposes missing
    domain cases
-   Replace or complement sample package data with Build Tools-generated
    packages when Build Tools reaches integration stage
-   Add performance-oriented runtime mechanisms only when package size
    and measurements justify them

## Output

A validated package-to-runtime content architecture capable of
representing and displaying contextual liturgical hierarchy.

------------------------------------------------------------------------

# Phase 7 --- Build Tools and Author Database Integration

**Status:** Completed --- First Real End-to-End Content Slice Verified

## Completed

- controlled Author Database export;
- Build Tools source loading and Schema-v1 mapping;
- contextual Qolo verses and Petgomo mapping;
- physical package generation;
- nullable effective-Melody state;
- `melodyCandidateIds` preservation;
- Loader/Validator and Runtime integration;
- Repository/Service/PlatformContext integration;
- Android Reference Application verification.

Representative `OccN = 1`:

``` text
20 resolved
29 unresolved
 3 ambiguous
------------
52 total Qolo occurrences
```

All 52 remain in authored liturgical order.

## Success Criteria

Achieved: Author Database → Build Tools → Schema v1 → existing Core →
Reference Application, with no manual package rewriting or arbitrary
Melody selection.

------------------------------------------------------------------------

# Phase 8 --- Generalize Build Tools and Broaden Real-Content Coverage

**Status:** Next Major Engineering Stage

## Objectives

Turn the Occasion-1 proof into a reusable workflow for arbitrary selected
Occasions.

## Planned Work

- remove `OccN = 1` assumptions from generation;
- define explicit Occasion/build-configuration input;
- preserve stable IDs, ordering, and all Melody-resolution states;
- produce actionable diagnostics;
- run 3--5 deliberately different representative Occasions end-to-end;
- classify new cases at the Author Database, Build Tools, Schema, Core,
  or application boundary;
- extend Content Domain only when real cases require it.

## Success Criteria

The same generation workflow can build and validate multiple Occasion
packages without Occasion-specific mapper code or manual JSON editing.

------------------------------------------------------------------------

# Phase 9 --- Audio Integration

**Status:** Planned

## Objectives

Connect the Audio Domain to resolved runtime content.

## Planned Work

-   Playback service
-   Play/Pause
-   Seek
-   Playback state
-   Content-to-audio relationships required by the real application
-   Verse synchronization where authoritative timing data exists

## Entry Condition

Audio work should begin only after the real content slice establishes
the content identities and relationships that audio must reference.

## Output

Reusable audio playback integrated with platform content rather than
embedded directly in one application.

------------------------------------------------------------------------

# Phase 10 --- Search Domain

**Status:** Planned

## Objectives

Implement content discovery across real package content.

## Planned Work

-   Text search
-   Syriac-aware search behavior
-   Indexed search
-   Relationship search
-   Search result navigation into runtime content

## Entry Condition

Search architecture should be designed against representative real
content volume rather than the small sample package.

## Output

A reusable search capability driven by measured content requirements.

------------------------------------------------------------------------

# Phase 11 --- User Domain

**Status:** Planned

## Objectives

Implement user-specific application capabilities while keeping them
separate from canonical content.

## Planned Work

-   Preferences
-   Language
-   Font scaling
-   Favorites
-   Notes

## Output

A user-data layer that can evolve independently from Application Package
content.

------------------------------------------------------------------------

# Phase 12 --- First Production Application

**Status:** Planned

## Objectives

Build the first production application using SyriacPlatform rather than
implementing its own independent content engine.

## Candidate Direction

A liturgical prayer/occasion application based on the content and
runtime capabilities validated in earlier phases.

The exact production target should be confirmed when the real content
slice and required platform domains are sufficiently mature.

## Success Criteria

The production application consumes platform services and generated
packages without duplicating Core content logic.

------------------------------------------------------------------------

# Phase 13 --- iOS and Cross-Platform Verification

**Status:** Planned / Deferred Until Platform Maturity

## Objectives

Verify that the shared platform architecture works across the second
primary mobile target.

## Planned Work

-   Configure and verify the iOS target
-   Resolve platform-specific integration requirements
-   Run shared tests against the supported target configuration
-   Verify package loading
-   Verify navigation integration
-   Verify content presentation
-   Verify audio integration when available

## Reason for Timing

Cross-platform verification is intentionally delayed until the shared
runtime and content architecture are stable enough that the work
validates a meaningful platform rather than an early prototype.

## Output

A verified shared SyriacPlatform foundation for Android and iOS.

------------------------------------------------------------------------

# Deferred / Requirement-Driven Infrastructure

The following capabilities are intentionally not assigned an early
implementation phase.

They should be introduced only when a concrete requirement justifies
them:

-   Command dispatcher
-   Event dispatcher
-   Advanced service lifecycle orchestration
-   Runtime SQLite materialization
-   Additional caching layers
-   Large-package optimization
-   Background update infrastructure
-   Additional platform services

This prevents speculative infrastructure from becoming a dependency
before its actual requirements are known.

------------------------------------------------------------------------

# Development Principle

The roadmap follows:

``` text
Foundation
    ↓
Kernel and startup
    ↓
Validated content package
    ↓
Runtime resolution
    ↓
Build Tools integration
    ↓
Real production content
    ↓
Requirement-driven domains
    ↓
Production applications
    ↓
Cross-platform expansion
```

New features are added only when the previous layer provides a real
requirement and a stable contract.

The platform does not infer editorial or liturgical relationships that
belong to the Author Database.

Build Tools prepares authoritative content.

The Application Package transports it.

The Core validates, resolves, and exposes it.

Applications consume it.

------------------------------------------------------------------------

# Immediate Next Milestone

``` text
selected OccN / build configuration
        ↓
controlled Author Database export
        ↓
generic Build Tools mapping
        ↓
Schema-v1 package
        ↓
existing Core
        ↓
Reference Application
```

Use several deliberately chosen Occasions to broaden real-content
coverage. Production UI styling is not the immediate milestone; the
Reference Application remains a visual smoke-test surface.

------------------------------------------------------------------------

End of Roadmap v1.3

------------------------------------------------------------------------

# Roadmap Update â€” 2026-08-20

This section supersedes earlier roadmap statements only where they
conflict with the verified implementation state below. All other roadmap
content remains in force.

## Current verified baseline

```text
2eac9db
```

The real-content pipeline is now verified for:

- parameterized Occasion package generation;
- synchronization to the reference application;
- complete long-text transport;
- resolved Qolo occurrences;
- unresolved Qolo occurrences originating from `QoloN = 0`;
- strict detection of missing canonical Text references;
- multiple real Occasions, including complex content extraction from
  Holy Week after Author Database cleanup.

## Completed milestone: generalized real-Occasion pipeline

The earlier single-Occasion vertical slice has become a reusable
development workflow.

This milestone is considered complete for the current phase.

It does not imply that every liturgical composition dimension is already
modeled.

## Next milestone: Day-aware Composition and Navigation

### Goal

Represent the day dimension required by multi-day Occasions without
creating a parallel import or package-loading path.

### Proven need

Occasion 41 contains prayer structures repeated across several days.

The existing pipeline retrieves the content correctly, but composition
currently groups occurrences sharing the same prayer identity without
preserving the required `DayN` separation.

### Required design work

The next implementation should determine:

1. the semantic role and identity rules for a day occurrence;
2. how source `DayN` enters Build Tools composition;
3. how day grouping is represented in Schema v1 or an explicitly
   versioned compatible extension;
4. how runtime resolution exposes day-aware structure;
5. how navigation traverses Occasion -> Day -> Prayer where applicable;
6. how ordinary single-day Occasions remain simple and compatible;
7. how ordering and repetition are preserved;
8. what validation rules are required for day references/grouping.

### Constraints

The implementation must not:

- introduce a second content-import pipeline for Holy Week;
- infer liturgical day relationships that the Author Database has not
  supplied;
- merge distinct day occurrences merely because they use the same
  canonical Prayer;
- move presentation-only decisions into canonical content entities;
- weaken existing canonical-reference validation.

### Acceptance criteria

The milestone is complete when:

- ordinary previously verified Occasions still build and run;
- unresolved Qolo behavior remains intact;
- a multi-day Occasion such as Occasion 41 preserves separate day
  groupings;
- prayers and liturgical items appear under the correct day;
- content remains complete and ordered;
- package validation and runtime tests cover the new day-aware path;
- the reference application can navigate the resulting structure without
  a special-case Holy Week loader.

## Subsequent work

After day-aware composition is stable, continue the existing roadmap for
broader application behavior and UI development.

The reference UI should remain primarily a verification surface until
the underlying content/composition contracts are stable.
