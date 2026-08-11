# SyriacPlatform — Current State

Date: 2026-08-12

Status: Package Loading v1 and Package Validation v1 Completed — Runtime Content Indexing Next

---

## Technology

- Kotlin Multiplatform
- Android target currently active
- Shared module: `platform/shared`
- Reference Android application: `platform/androidApp`
- Package namespace: `org.syriacplatform`

---

## Completed Foundation

- `Result<T>`
- `PlatformError`
- `ErrorCode`
- `PlatformId`
- Typed IDs
- `RuntimeState`
- `Version`
  - semantic parsing
  - numeric comparison
  - validation of version components

---

## Completed Kernel

- `PlatformService`
- `ServiceMetadata`
- `ServiceRegistry`
- `PlatformKernel`
- Service registration by `KClass`
- Service resolution by `KClass`
- Kernel-managed initialization of registered services
- Registry-owned service iteration
- `PlatformContext`

---

## Completed Platform Startup

- `PlatformBootstrap` is the central platform construction point
- `PlatformBootstrap.create()` creates and prepares the platform
- Platform services are registered through bootstrap-owned dependencies
- `PlatformKernel.initialize()` initializes registered services
- `App.kt` does not construct platform services directly
- Compose UI consumes the platform through the bootstrap layer
- Platform startup remains independent from platform-specific content-loading details

---

## Completed Navigation

- `AppDestination`
- `NavigationState`
- `NavigationController`
- `NavigationService`
- Reactive navigation using `StateFlow`
- Compose observes navigation state
- HOME ↔ QOLO_DETAILS navigation implemented
- Navigation covered by shared tests

---

## Completed Content Domain

Canonical runtime/domain models currently include:

- `EntryPoint`
- `Occasion`
- `Prayer`
- `PrayerSequence`
- `LiturgicalItem`
- `TextContent`
- `Petgomo`
- `Qolo`
- `Melody`
- `Qinto`
- `MelodyQintoAssignment`

Important content decisions:

- Canonical IDs identify one legal entity inside their entity type.
- Repeated use of the same entity is legal.
- Repeated definitions of the same canonical ID are not legal.
- Liturgical repetition is preserved and is not treated as duplicate data.
- `LiturgicalItemTarget.Text` may optionally carry a `PetgomoId`.
- `LiturgicalItemTarget.Qolo` carries:
  - `qoloId`
  - `effectiveMelodyId`
- Current v1 model defines:
  - one `Qolo` → many `Melody`
- The same musical tune may be represented by separate Melody records when used with different Qolos.
- Qinto classification is optional where the melody is not part of the eight-Qinto system.

---

## Application Package Specification v1

The package specification has been aligned with the implemented runtime model.

Canonical Schema v1 content files are:

```text
content/
├── entry-points.json
├── occasions.json
├── prayers.json
├── prayer-sequences.json
├── liturgical-items.json
├── texts.json
├── petgomos.json
├── qolos.json
├── melodies.json
├── qintos.json
└── melody-qinto-assignments.json

Reserved for future schema versions:

days.json
locations.json
groups.json
media-assets.json
Package Profiles v1

Supported profiles:

OCCASION
SHHIMA
FULL_LIBRARY

Profile Collection Matrix:

Collection	OCCASION	SHHIMA	FULL_LIBRARY
entry-points.json	Required	Required	Required
occasions.json	Required	Optional	Required
prayers.json	Required	Required	Required
prayer-sequences.json	Required	Required	Required
liturgical-items.json	Required	Required	Required
texts.json	Required	Required	Required
qolos.json	Optional	Optional	Required
melodies.json	Optional	Optional	Required
qintos.json	Optional	Optional	Required
petgomos.json	Optional	Optional	Required
melody-qinto-assignments.json	Optional	Optional	Required

Profile rules:

Required means the collection file must physically exist.
Required does not mean non-empty.
Optional collections may be absent.
Present-but-empty and absent are distinct states.
A Melody is not required to belong to the eight-Qinto system.
Presence of petgomos.json does not require every Qolo, Melody, Text, or LiturgicalItem to have a Petgomo.
Parsed Application Package

ParsedApplicationPackage is now the canonical package representation after JSON decoding and DTO-to-Domain mapping, and before Runtime Content construction.

It contains:

PackageManifest
PackageCollectionPresence
all 11 canonical collections

PackageCollectionPresence preserves the difference between:

present + empty list

and:

absent + empty list

This distinction is required by Profile Validation.

Package Validation v1

The package-validation pipeline is implemented and integrated.

Current validation layers:

PackageValidator
├── ManifestValidator
├── CompatibilityValidator
├── ProfileValidator
├── ReferenceValidator
├── IntegrityValidator
└── SemanticValidator

All validation issues are collected into:

ValidationReport

The validator does not stop at the first fatal issue.

Manifest Validation

Implemented validation includes required manifest metadata and basic manifest consistency.

Compatibility Validation v1

Implemented:

SchemaCompatibilityRule
MinimumCoreVersionRule

Current compatibility behavior:

Unsupported schema version → FATAL
targetSchemaVersion mismatch in Schema v1 → FATAL
Core older than minimumCoreVersion → FATAL
Invalid semantic Core version → FATAL

Core compatibility defaults are centralized in:

CoreCompatibilityDefaults.CURRENT

Current values:

Core version: 1.0.0
Supported schema versions: 1.0
Reference Validation v1

Implemented reference rules:

EntryPointReferenceRule
OccasionReferenceRule
PrayerSequenceReferenceRule
PrayerSequenceLiturgicalItemReferenceRule
LiturgicalItemTextReferenceRule
LiturgicalItemPetgomoReferenceRule
LiturgicalItemQoloReferenceRule
MelodyQoloReferenceRule
MelodyQintoAssignmentMelodyReferenceRule
MelodyQintoAssignmentQintoReferenceRule

Reference Validation answers:

Does the referenced canonical entity exist?
Integrity Validation v1

Implemented:

CanonicalIdUniquenessRule
DefaultEntryPointUniquenessRule
MelodyQintoAssignmentUniquenessRule

Explicitly allowed:

repeated LiturgicalItem references
repeated Text references
repeated Qolo references
duplicate Qolo sort values

Integrity Validation answers:

Is the internal package structure legally consistent?
Semantic Validation v1

Implemented:

LiturgicalItemEffectiveMelodyRule

Current semantic rule:

If a LiturgicalItemTarget.Qolo declares:

qoloId = X
effectiveMelodyId = Y

then Melody Y, when present, must belong to Qolo X.

Missing Melody references remain the responsibility of Reference Validation.

Package Loading v1

The full package-loading path is implemented.

Key abstractions:

PackageSource
ResourcePackageSource
PackagePaths
PackageStructure
PackageLoadResult
ApplicationPackageLoader

ApplicationPackageLoader:

reads canonical files
detects physical collection presence
decodes JSON into DTOs
maps DTOs to Domain models
builds ParsedApplicationPackage
runs PackageValidator
returns:
Success
ValidationFailed
Failure

Canonical files are read once during the main load() path.

Runtime Package Integration

The runtime no longer reads qolos.json directly.

The legacy:

JsonContentRepository

has been removed.

Current runtime content path:

Compose Resources
    ↓
ResourcePackageSource
    ↓
ApplicationPackageLoader
    ↓
PackageCollectionPresence
    ↓
JSON DTO decoding
    ↓
DTO → Domain mapping
    ↓
ParsedApplicationPackage
    ↓
PackageValidator
    ↓
ApplicationPackageContentRepository
    ↓
DefaultContentService
    ↓
Runtime

Only a successfully parsed and validated package is cached by the runtime repository.

A package that fails validation does not become Runtime Content.

Embedded Development Package

The reference runtime currently includes a minimal embedded OCCASION package.

It contains:

manifest.json
required empty OCCASION collections
the existing qolos.json

This embedded package is used to verify the production loading path.

Content Repository

Current runtime repository:

ApplicationPackageContentRepository

Current public content access remains intentionally small:

loadQolo(qoloId)
loadAllQolos()

The repository caches the validated:

ParsedApplicationPackage

rather than caching Qolos separately.

This prepares the runtime for broader canonical content access.

Tests and Verification

The following continue to pass:

:shared:allTests
:shared:check
:androidApp:assembleDebug

Tests now cover:

kernel
bootstrap
navigation
content service
package DTO mapping
manifest parsing
canonical paths
package structure discovery
collection presence
package loading
malformed JSON
missing required collections
package validation
compatibility validation
profile validation
reference validation
integrity validation
semantic validation
runtime package content repository
Major Architectural Decisions
Author data and Runtime data remain separate.
Application Package is the canonical runtime content representation.
Runtime never depends directly on Author Database structure.
DTOs are separated from Domain models.
Package validation happens before content becomes available to Runtime.
Missing and empty collections are distinct states.
Core compatibility is centrally defined.
Canonical IDs are unique definitions, not unique usages.
Liturgical repetition is legal.
Optional musical classification is not forced.
Qinto membership is not required for all Melodies.
PackageSource abstracts physical storage.
Runtime content loading is no longer tied directly to Compose Resources.
ApplicationPackageLoader owns package reading/parsing/mapping/validation orchestration.
ApplicationPackageContentRepository owns access to validated package content.
UI does not construct infrastructure dependencies directly.
Current Git Milestone

Latest completed milestone:

7bd578e — Centralize core compatibility defaults

Previous major milestone:

b3d6bb7 — Add validated application package runtime loading
Current Status

Completed:

Platform Foundation                 ✓
Kernel                              ✓
Bootstrap                           ✓
Reactive Navigation                 ✓
Canonical Content Models            ✓
Application Package Specification   ✓ v1
Canonical Collections               ✓ v1
Package Profiles                    ✓ v1
Manifest Validation                 ✓
Compatibility Validation            ✓ v1
Profile Validation                  ✓ v1
Reference Validation                ✓ v1
Integrity Validation                ✓ v1
Semantic Validation                 ✓ v1
Package Validator                   ✓
Package Loading                     ✓ v1
Runtime Package Integration         ✓
Legacy Direct JSON Repository       ✓ removed
Next Phase
Runtime Content Access v1

The next architectural step is to build a structured runtime/index layer above ParsedApplicationPackage.

Planned direction:

Validated ParsedApplicationPackage
        ↓
RuntimeContent / RuntimeContentIndex
        ↓
Indexed canonical entities
        ↓
ContentRepository
        ↓
ContentService

The runtime index should provide efficient canonical lookup without repeatedly scanning lists.

Initial indexed entities are expected to include:

EntryPoints
Occasions
Prayers
PrayerSequences
LiturgicalItems
Texts
Petgomos
Qolos
Melodies
Qintos
MelodyQintoAssignments

The first real runtime traversal should then support:

Occasion
→ PrayerSequence
→ LiturgicalItems
→ Text / Qolo

This phase should be completed before introducing broad Search, Audio, or higher-level application features.