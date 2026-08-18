# SyriacPlatform

## Roadmap v1.3

**Status:** Official Development Plan\
**Updated:** 2026-08-18\
**Repository baseline:** Phase 7 Build Tools development-preview
generation

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

**Status:** In Progress --- First Physical Development Preview Generated

## Objectives

Connect the authoritative Author Database workflow to the existing
Application Package specification and Core runtime without introducing a
parallel content path.

## Completed

### Controlled Author Database Boundary

-   Version-controlled schema snapshot
-   Controlled representative export
-   Representative slice: `OccN = 1`
-   Source table classification
-   Stable source-identity rules
-   Explicit mapping contract in `AuthorDatabaseMapping.md`

### Build Tools Source and Mapping Layer

-   `AuthorSourceDataLoader`
-   canonical Schema-v1 mapping
-   Occasion/Prayer/LiturgicalItem composition mapping
-   contextual Qolo verse mapping
-   contextual Petgomo mapping
-   deterministic PrayerSequence projection
-   deterministic EntryPoint projection
-   source diagnostics for unresolved authoring data

### Development Preview

The full representative composition currently contains:

``` text
20 resolved Qolo occurrences
32 package-blocking source occurrences
```

A dedicated `DevelopmentPreviewSlice` selects only the 20
already-resolved occurrences for the current non-production proof while
preserving their occurrence identities and authored relative ordering.

The 32 blocked source occurrences remain explicit diagnostics in the
full composition.

### Physical Schema-v1 Generation

-   `SchemaV1PreviewPackageAssembler`
-   `SchemaV1PackageWriter`
-   manifest generation
-   canonical collection generation
-   physical package directory generation
-   Occasion-profile development preview
-   20 generated Qolo Liturgical Items
-   Build Tools tests for preview selection, ordering, navigation, and
    physical package output

## Current Contract

The generated preview conforms to the existing Schema-v1 rule that a
Qolo Liturgical Item has a valid `effectiveMelodyId`.

Build Tools must not invent Melody/Qinto decisions for unresolved source
occurrences. Ambiguous or undetermined cases remain outside the preview
package until resolved authoritatively.

## Remaining Work

-   Load the Build Tools-generated package through the existing
    `ApplicationPackageLoader`
-   Pass the existing package validation pipeline
-   Resolve the generated real content through `RuntimeContentResolver`
-   Expose it through the existing repository/service/`PlatformContext`
    path
-   Verify the generated preview in the Android Reference Application
-   Keep unresolved source diagnostics traceable to Author Database
    records
-   Decide future authoring rules for Melody ambiguity only from
    authoritative domain requirements

## Success Criteria

The current Phase-7 milestone is complete when:

``` text
Author Database
        ↓
controlled export
        ↓
Build Tools
        ↓
generated Schema-v1 package
        ↓
existing Core validation/runtime
        ↓
Reference Application
```

works without manual package rewriting, parallel loading logic, or
invented editorial data.

## Output

A proven Author Database → Build Tools → Schema-v1 → existing Core
integration path using real representative content.

------------------------------------------------------------------------

# Phase 8 --- First Production Content Slice

**Status:** Partially Entered Through Phase-7 Development Preview

## Objectives

Move from the controlled development preview to a production-meaningful
real content slice after the generated package has completed end-to-end
Core integration.

## Planned Work

-   Select one representative production content slice
-   Generate it through Build Tools
-   Verify canonical identity and contextual relationships
-   Verify occasion/prayer/hymn hierarchy
-   Verify verse ordering and repetition
-   Verify Melody and Petgomo context
-   Verify Syriac presentation requirements
-   Identify domain cases not covered by the sample package

## Success Criteria

The Reference Application renders a real liturgical content slice
without application-specific data reconstruction.

## Output

A real-content proof that the platform architecture works beyond
synthetic sample data.

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

The immediate objective is no longer to define the first Build Tools
output. That output now exists as the physical Occasion-1 development
preview.

The next milestone is:

``` text
generated Occasion-1 preview package
        ↓
ApplicationPackageLoader
        ↓
PackageValidator
        ↓
RuntimeContentStore
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

This must use the existing Core path unchanged.

The unresolved 32 source occurrences remain a separate authoring/build
concern and must not be hidden by weakening Schema v1.

------------------------------------------------------------------------

End of Roadmap v1.3
