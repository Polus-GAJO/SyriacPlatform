# Core Engine Architecture

**Version:** 1.0  
**Status:** Draft  
**Project:** SyriacPlatform

---

# 1. Introduction

## 1.1 Purpose

This document defines the architecture of the SyriacPlatform Core Engine.

The Core Engine is the shared runtime layer responsible for loading, validating, resolving, and exposing Application Package content to platform applications.

It provides the common behavior required by all applications built on SyriacPlatform, regardless of their specific content scope, interface design, or target platform.

The purpose of this document is to define:

- the architectural role of the Core Engine
- its responsibilities and boundaries
- its internal components
- the relationships between those components
- the runtime flow of Application Package content
- the public services exposed to applications
- the architectural constraints that implementation must preserve

This document describes the architecture of the Core Engine rather than a specific implementation.

Implementation details may evolve, provided that they preserve the responsibilities, boundaries, and behavior defined here.

---

## 1.2 Relationship to Other Documents

This document belongs to the SyriacPlatform architectural documentation set.

It should be read together with the following documents:

```text
Platform Blueprint
Development Guide
Engineering Notebook
Roadmap
Application Package Specification
Application Package Examples and Reference
```

Each document has a distinct responsibility.

```text
Platform Blueprint
        │
        ▼
Defines the overall platform vision and major components

Development Guide
        │
        ▼
Defines development practices and implementation guidance

Application Package Specification
        │
        ▼
Defines the formal structure and rules of Application Packages

Application Package Examples and Reference
        │
        ▼
Demonstrates practical package examples and runtime relationships

Core Engine Architecture
        │
        ▼
Defines the runtime system that consumes Application Packages
```

The Application Package documents define the data supplied to the Core Engine.

This document defines how the Core Engine processes and exposes that data.

---

## 1.3 Architectural Context

SyriacPlatform separates content authoring, package generation, runtime processing, and application presentation.

```text
Author Database
        │
        ▼
Build Tools
        │
        ▼
Application Package
        │
        ▼
Core Engine
        │
        ▼
Application
        │
        ▼
User
```

The Author Database maintains editorial and canonical source content.

The Build Tools transform that source content into validated Application Packages.

The Core Engine loads and resolves those packages at runtime.

Applications use the Core Engine to present package content to users.

This separation ensures that:

- editorial decisions remain outside runtime applications
- package generation remains independent from presentation
- multiple applications can share the same runtime architecture
- the same Core Engine can process different package profiles
- platform behavior remains consistent across supported targets

---

## 1.4 Core Engine Definition

The Core Engine is a shared, platform-independent runtime component.

It is not a complete application.

It does not define the final user interface.

It does not author or edit canonical content.

It does not replace the Build Tools.

Instead, it provides reusable runtime capabilities such as:

```text
Package Loading
Package Validation
Content Access
Reference Resolution
Navigation
Search
Resource Resolution
Media Resolution
Localization
Runtime State
Diagnostics
```

Applications depend on these capabilities but remain responsible for presentation and user interaction.

---

## 1.5 Architectural Objective

The primary architectural objective of the Core Engine is to provide one consistent runtime model for all SyriacPlatform applications.

For example:

```text
Shhima App
        │
        ┐
Occasions App
        │
        ├──► Core Engine
        │
Full Library App
        │
        ┘
```

Each application may:

- load a different Application Package
- expose different entry points
- present different navigation structures
- support different features
- use different visual designs

However, all applications should rely on the same Core Engine behavior for:

- package interpretation
- entity identity
- relationship resolution
- sequence ordering
- search result resolution
- media location
- error handling

This prevents each application from developing its own incompatible interpretation of platform content.

---

## 1.6 Design Scope

This document covers the architecture of the runtime Core Engine.

It includes:

- package lifecycle
- runtime content representation
- service responsibilities
- component dependencies
- reference resolution
- navigation and search
- resource access
- platform abstraction
- application-facing APIs
- error and diagnostic behavior
- testing and extensibility

The document does not define:

- Author Database schema
- editorial workflows
- Build Tools implementation
- package authoring interfaces
- final application UI design
- platform-specific visual components
- application-specific business presentation

These concerns belong to other platform layers.

---

## 1.7 Intended Audience

This document is intended for:

- Core Engine developers
- application developers
- Build Tools developers
- platform architects
- maintainers
- test engineers
- future contributors to SyriacPlatform

A developer implementing an application should be able to use this document to understand which responsibilities belong to the application and which belong to the Core Engine.

A developer implementing the Core Engine should be able to use it to understand the required components, dependencies, and runtime behavior.

---

## 1.8 Architectural Stability

The Core Engine is expected to evolve over time.

New package entity types, services, or application capabilities may be introduced in future platform versions.

However, architectural evolution should preserve the following properties:

- clear separation of responsibilities
- stable canonical identifiers
- deterministic runtime resolution
- package compatibility control
- platform independence
- testable services
- explicit error handling
- application independence
- backward-compatible extension where practical

Changes that alter these properties require explicit architectural review.

---

## 1.9 Normative Language

This document uses the following terms:

- **shall** indicates an architectural requirement
- **shall not** indicates an architectural prohibition
- **should** indicates a recommended architectural practice
- **may** indicates an optional capability
- **can** describes a possible behavior or implementation

Where this document conflicts with the formal Application Package Specification regarding package structure, the Application Package Specification remains authoritative.

This document remains authoritative regarding Core Engine responsibilities and architecture.

---

## 1.10 Summary

The Core Engine is the shared runtime foundation of SyriacPlatform applications.

Its role is summarized as follows:

```text
Application Package
        │
        ▼
Core Engine
        │
        ├──► Validate
        ├──► Load
        ├──► Resolve
        ├──► Navigate
        ├──► Search
        ├──► Locate Resources
        └──► Expose Runtime Services
                │
                ▼
            Application
```

The following sections define this architecture in detail.

# 2. Architectural Role of the Core Engine

## 2.1 Purpose

This section defines the architectural role of the Core Engine within SyriacPlatform.

The Core Engine occupies the runtime boundary between Application Packages and applications.

Its role is to convert validated package content into a stable runtime model that applications can use without directly interpreting package files or reconstructing package relationships.

---

## 2.2 Position Within the Platform

The Core Engine is located after package generation and before application presentation.

```text
Author Database
        │
        ▼
Build Tools
        │
        ▼
Application Package
        │
        ▼
Core Engine
        │
        ▼
Application
```

Each layer produces or consumes a distinct form of information.

```text
Author Database
        │
        ▼
Editorial source content

Build Tools
        │
        ▼
Validated runtime package

Core Engine
        │
        ▼
Resolved runtime services and models

Application
        │
        ▼
User-facing presentation and interaction
```

The Core Engine shall not bypass this architectural order.

---

## 2.3 Primary Runtime Role

The primary role of the Core Engine is to interpret an Application Package according to the platform architecture and package specification.

This includes:

- opening the package
- reading the manifest
- checking runtime compatibility
- loading canonical collections
- validating runtime-level assumptions
- building efficient content access structures
- resolving typed references
- preserving declared sequence order
- exposing navigation and search capabilities
- resolving media and resource locations
- reporting structured runtime errors

The Core Engine transforms package data into application-consumable runtime behavior.

---

## 2.4 Stable Interpretation Layer

Applications shall not independently interpret package structure.

Without a shared runtime layer, each application could develop different behavior for the same package.

For example, one application might:

- preserve Prayer Sequence order

while another might:

- sort Liturgical Items by identifier

One application might interpret:

```text
effectiveMelodyId
```

as authoritative, while another might attempt to derive the Melody again.

Such differences would create incompatible runtime behavior.

The Core Engine therefore acts as the single interpretation layer for Application Packages.

```text
Application Package
        │
        ▼
One Core Engine interpretation
        │
        ├──► Shhima App
        ├──► Occasions App
        └──► Full Library App
```

---

## 2.5 Separation From Build Tools

The Build Tools and Core Engine operate at different stages.

The Build Tools are responsible for producing the package.

The Core Engine is responsible for consuming the package.

```text
Build Tools
        │
        ├──► extract source content
        ├──► transform editorial data
        ├──► resolve effective values
        ├──► generate indexes
        ├──► validate references
        ├──► copy media resources
        └──► produce Application Package

Core Engine
        │
        ├──► open package
        ├──► verify compatibility
        ├──► load package content
        ├──► resolve package relationships
        ├──► expose runtime services
        └──► report runtime failures
```

The Core Engine shall not repeat editorial or build-time decisions.

For example, it shall not:

- select an effective Melody from editorial rules
- generate canonical identifiers
- normalize source content for publication
- generate the search index
- repair broken source relationships
- modify package content

These operations belong to the Build Tools or Author Database.

---

## 2.6 Separation From Applications

The application and Core Engine also have distinct responsibilities.

The Core Engine determines what the package means at runtime.

The application determines how that resolved content is presented.

```text
Core Engine
        │
        ├──► package access
        ├──► content resolution
        ├──► navigation state
        ├──► search results
        ├──► resource locations
        └──► structured errors

Application
        │
        ├──► screens
        ├──► visual layout
        ├──► typography
        ├──► controls
        ├──► animations
        ├──► user interaction
        └──► application-specific presentation
```

The Core Engine shall not depend on a specific application interface.

Applications shall not reproduce Core Engine responsibilities inside UI code.

---

## 2.7 Separation From the Author Database

The Author Database is an editorial environment.

It may contain:

- incomplete records
- editorial alternatives
- unresolved choices
- internal notes
- historical data
- source relationships not intended for runtime
- authoring-specific structures

The Core Engine shall not consume the Author Database directly.

It shall consume only Application Packages generated by the Build Tools.

```text
Author Database
        │
        ▼
Build Tools transformation boundary
        │
        ▼
Application Package
        │
        ▼
Core Engine
```

This boundary protects runtime applications from editorial complexity and database-specific implementation details.

---

## 2.8 Runtime Authority

At runtime, the Application Package is the content authority available to the Core Engine.

The Core Engine shall not silently supplement package content from:

- the Author Database
- an external editorial source
- hard-coded application content
- inferred relationships
- an unrelated package

If a required entity is missing, the Core Engine shall report the condition rather than invent or recover content from an undeclared source.

This preserves deterministic runtime behavior.

---

## 2.9 Deterministic Behavior

Given the same:

- Core Engine version
- Application Package
- platform-compatible environment
- runtime configuration

the Core Engine should produce the same content resolution results.

For example:

```text
PS-001
        │
        ▼
[
  LI-001,
  LI-002,
  LI-003
]
```

shall always preserve the same declared order.

Similarly:

```text
LI-002
        │
        ├──► QOL-001
        └──► MEL-001
```

shall resolve to the same canonical entities unless the package itself changes.

Deterministic behavior is essential for:

- reproducibility
- testing
- cross-platform consistency
- debugging
- package validation
- long-term maintenance

---

## 2.10 Shared Runtime Foundation

The Core Engine serves as the shared runtime foundation for multiple applications.

```text
                    ┌──► Shhima App
                    │
Application Package ├──► Occasions App
        │           │
        ▼           └──► Full Library App
   Core Engine
```

Applications may use different subsets of Core Engine services.

For example:

```text
Shhima App
        │
        ├──► navigation
        ├──► prayer sequences
        ├──► texts
        └──► melodies

Occasions App
        │
        ├──► occasions
        ├──► search
        ├──► texts
        └──► media

Full Library App
        │
        ├──► all canonical collections
        ├──► advanced search
        ├──► cross-reference navigation
        └──► complete media access
```

The underlying package interpretation remains consistent.

---

## 2.11 Application Independence

The Core Engine shall remain independent from application-specific assumptions.

It shall not assume:

- a particular screen structure
- a fixed navigation bar
- one application name
- one package profile
- one visual theme
- one content subset
- one device type

Instead, it shall expose general runtime capabilities that applications compose according to their needs.

---

## 2.12 Platform Independence

The Core Engine should contain as much platform-independent logic as practical.

Its core behavior should remain consistent across:

- Android
- iOS
- desktop targets
- future supported platforms

Platform-specific behavior should be isolated behind explicit abstractions.

Examples include:

- file access
- package storage
- audio playback integration
- opening notation documents
- platform logging
- locale discovery

The architectural role of the engine remains the same regardless of the target platform.

---

## 2.13 Core Engine as a Service Boundary

Applications should interact with the Core Engine through explicit services or public interfaces.

They should not depend directly on:

- internal storage maps
- parser implementations
- private validation structures
- package file locations
- internal caches
- concrete platform adapters

The intended relationship is:

```text
Application
        │
        ▼
Core Engine Public API
        │
        ▼
Internal Engine Components
        │
        ▼
Application Package
```

This boundary allows internal implementation to evolve without forcing application code to change unnecessarily.

---

## 2.14 Failure Boundary

The Core Engine is also the primary runtime failure boundary for package-related errors.

It should convert low-level failures into structured engine errors.

For example:

```text
File system failure
        │
        ▼
PackageReadError

Invalid JSON
        │
        ▼
PackageParseError

Unsupported schema version
        │
        ▼
CompatibilityError

Missing entity
        │
        ▼
ReferenceResolutionError

Missing media file
        │
        ▼
ResourceNotFoundError
```

Applications receive meaningful runtime errors without needing to interpret low-level package implementation details.

---

## 2.15 Architectural Role Summary

The Core Engine has four principal architectural roles.

```text
1. Package Consumer
        │
        ▼
Loads and checks Application Packages

2. Runtime Interpreter
        │
        ▼
Applies one consistent meaning to package content

3. Shared Service Layer
        │
        ▼
Provides navigation, search, resolution, and resource services

4. Application Boundary
        │
        ▼
Separates application presentation from package internals
```

Its complete position may be summarized as follows:

```text
Build Tools
        │
        ▼
Validated Application Package
        │
        ▼
Core Engine
        │
        ├──► Load
        ├──► Interpret
        ├──► Resolve
        ├──► Expose
        └──► Diagnose
                │
                ▼
            Applications
```

The Core Engine is therefore not merely a collection of utility functions.

It is the authoritative shared runtime layer of SyriacPlatform.

# 3. Architectural Principles

## 3.1 Purpose

This section defines the architectural principles that guide the design and implementation of the Core Engine.

These principles are not implementation details.

They are long-term constraints intended to preserve consistency, maintainability, portability, and correctness as the Core Engine evolves.

Any new component, service, or feature introduced into the Core Engine should be evaluated against these principles.

---

## 3.2 Separation of Responsibilities

Each Core Engine component shall have one clearly defined responsibility.

For example:

```text
Package Loader
        │
        ▼
Reads package files

Package Validator
        │
        ▼
Verifies package correctness

Reference Resolver
        │
        ▼
Resolves canonical relationships

Navigation Service
        │
        ▼
Manages runtime navigation behavior

Search Service
        │
        ▼
Queries generated search indexes
```

A component should not silently assume responsibilities belonging to another component.

For example, the Package Loader shall not:

- select effective Melodies
- repair invalid references
- generate search terms
- manage application screens
- perform editorial transformations

Clear separation reduces hidden coupling and makes each component easier to test and replace.

---

## 3.3 Content and Presentation Separation

The Core Engine shall operate independently from visual presentation.

It may expose:

- canonical entities
- ordered sequences
- navigation state
- search results
- resource references
- structured errors

It shall not define:

- colors
- typography
- screen layout
- animations
- button placement
- platform-specific widgets

The architectural relationship is:

```text
Core Engine
        │
        ▼
Resolved content and state
        │
        ▼
Application presentation
```

This allows multiple applications to present the same content differently without changing the runtime interpretation.

---

## 3.4 Build-Time and Runtime Separation

Editorial and transformational work shall remain outside the Core Engine.

Build-time responsibilities include:

- generating canonical package files
- selecting package content
- resolving effective values
- generating indexes
- validating complete reference graphs
- copying media resources
- producing release artifacts

Runtime responsibilities include:

- opening the package
- verifying compatibility
- loading generated content
- resolving declared references
- exposing runtime services

```text
Build Time
        │
        ▼
Produces resolved package data

Runtime
        │
        ▼
Consumes resolved package data
```

The Core Engine shall not reconstruct decisions that were already finalized by the Build Tools.

---

## 3.5 Canonical Identity

Every canonical entity shall be addressed through a stable identifier.

Examples include:

```text
OCC-001

PR-001

PS-001

LI-001

TXT-001

QOL-001

MEL-001

MED-001
```

The Core Engine shall use identifiers rather than display labels as the basis of relationships.

Labels may:

- change
- be translated
- contain duplicate wording
- vary by context

Identifiers shall remain the authoritative identity mechanism.

---

## 3.6 Typed Reference Resolution

A reference is defined by both its identifier and its expected entity type.

For example:

```text
type     = qolo

targetId = QOL-001
```

The Core Engine shall not treat identifier existence alone as sufficient.

The referenced identifier must belong to the expected canonical collection.

```text
QOL-001 in qolos.json
        │
        ▼
Valid

QOL-001 in texts.json
        │
        ▼
Invalid type relationship
```

Typed resolution prevents accidental cross-collection interpretation.

---

## 3.7 Deterministic Resolution

Runtime resolution shall be deterministic.

Given the same package and runtime configuration, the Core Engine shall produce the same resolved entities and ordering.

The engine shall not make undocumented choices based on:

- file loading order
- map iteration order
- localized labels
- platform behavior
- arbitrary fallback rules
- application-specific assumptions

For example:

```json
[
  "LI-001",
  "LI-002",
  "LI-003"
]
```

shall always resolve in that exact order.

Determinism supports testing, reproducibility, and cross-platform consistency.

---

## 3.8 Declared Order Is Authoritative

Whenever an Application Package declares an ordered collection, that order shall be preserved.

Examples include:

- Prayer Sequence items
- Occasion sequence references
- ordered navigation elements
- ordered search suggestions where explicitly defined

The Core Engine shall not automatically:

- alphabetize the collection
- sort by identifier
- group by entity type
- reorder by file location
- infer liturgical precedence

Any alternative presentation order shall be an explicit application-level operation and shall not alter the canonical runtime order.

---

## 3.9 Immutability of Package Content

Loaded package content should be treated as immutable.

The Core Engine shall not edit canonical entities inside the package.

It shall not:

- rewrite identifiers
- modify text content
- replace references
- add missing entities
- change sequence order
- write runtime state into package files

Runtime state shall remain separate from canonical package data.

```text
Application Package
        │
        ▼
Immutable canonical content

Runtime State
        │
        ▼
Mutable session information
```

This separation prevents accidental corruption and supports predictable reload behavior.

---

## 3.10 Explicit State

Runtime state shall be represented explicitly.

Examples include:

- currently selected Entry Point
- active Occasion
- active Prayer Sequence
- current Liturgical Item
- search query
- selected search result
- media playback position
- navigation history

State shall not be hidden inside unrelated services or inferred repeatedly from UI components.

An explicit state model improves:

- traceability
- testing
- restoration
- debugging
- platform consistency

---

## 3.11 Explicit Dependencies

Core Engine components should receive their dependencies through explicit interfaces.

For example:

```text
PackageService
        │
        ├──► PackageSource
        ├──► JsonDecoder
        ├──► CompatibilityChecker
        └──► DiagnosticReporter
```

Components should not depend on invisible global objects or directly create platform-specific implementations internally.

Explicit dependencies make behavior easier to test and allow alternative implementations where necessary.

---

## 3.12 Interface-Driven Architecture

Application-facing and platform-specific behavior shall be exposed through interfaces or stable contracts.

Examples include:

```text
PackageSource

ContentRepository

ReferenceResolver

SearchService

ResourceResolver

DiagnosticReporter
```

Applications should depend on contracts rather than internal concrete classes.

Platform-specific implementations may vary while preserving the same contract.

---

## 3.13 Platform Independence

Core domain and runtime logic should remain platform-independent wherever practical.

Shared logic includes:

- canonical models
- package validation rules
- reference resolution
- navigation state
- search behavior
- error classification
- compatibility decisions

Platform-specific implementations should be limited to capabilities such as:

- reading files
- locating application storage
- opening external documents
- platform logging
- connecting to media playback
- discovering system locale

```text
Shared Core Logic
        │
        ▼
Platform Abstraction
        │
        ├──► Android implementation
        ├──► iOS implementation
        └──► future platform implementation
```

---

## 3.14 Fail Explicitly

The Core Engine shall not silently ignore failures that affect correctness.

Examples include:

- unreadable manifest
- malformed JSON
- unsupported schema version
- missing required collection
- unresolved reference
- wrong entity type
- missing required resource

Such conditions shall produce explicit structured errors or diagnostics.

The engine shall not silently:

- skip required entities
- replace missing references
- use unrelated defaults
- continue with ambiguous state

Explicit failure makes defects discoverable and prevents incorrect content presentation.

---

## 3.15 Graceful Degradation

Explicit failure does not require every non-critical condition to stop the entire application.

The Core Engine should distinguish between:

```text
Fatal Error
        │
        ▼
Package cannot be safely used

Recoverable Error
        │
        ▼
A specific operation cannot complete

Warning
        │
        ▼
Package remains usable, but a condition should be reported

Information
        │
        ▼
Diagnostic context only
```

For example:

- an unsupported manifest version may be fatal
- one missing optional notation file may be recoverable
- an orphaned entity may produce a warning
- cache creation may produce informational diagnostics

The response shall be proportional to the severity of the condition.

---

## 3.16 No Silent Repair

The Core Engine shall not alter package meaning in an attempt to repair invalid content.

For example, when `MEL-999` is missing, the engine shall not:

- choose the first available Melody
- reuse a Melody from another Qolo
- derive a Melody from labels
- load content from another package

Any fallback behavior shall be explicit, documented, and limited to cases permitted by the package architecture.

Package repair belongs to authoring or Build Tools workflows.

---

## 3.17 Minimal Knowledge of Storage Format

Applications shall not need to know where or how package entities are physically stored.

An application should request:

```text
QOL-001
```

through a Core Engine service.

It should not need to know that the entity currently resides in:

```text
content/qolos.json
```

The engine may understand the storage format internally, but that knowledge shall not leak unnecessarily through the public API.

This allows future changes to internal storage without rewriting application logic.

---

## 3.18 Derived Data Is Not Canonical

Indexes, caches, and lookup maps are derived runtime or build-time structures.

Examples include:

- search indexes
- identifier maps
- reverse-reference indexes
- navigation caches
- resource lookup caches

They shall not replace canonical entities as the source of truth.

```text
Canonical Collection
        │
        ▼
Authoritative content

Derived Index
        │
        ▼
Optimized access only
```

If derived data conflicts with canonical content, the condition shall be reported rather than silently redefining the canonical entity.

---

## 3.19 Lazy Work Where Appropriate

The Core Engine may defer expensive operations until they are required.

Examples include:

- loading large media metadata
- opening notation documents
- constructing optional reverse indexes
- decoding content used only by advanced search
- preparing platform media integration

However, lazy behavior shall not create inconsistent results.

Required package compatibility and structural checks should occur early enough to prevent an invalid package from appearing successfully loaded.

---

## 3.20 Predictable Performance

The architecture should avoid repeated full-package scans during normal runtime operations.

Frequently used operations should rely on efficient lookup structures.

For example:

```text
Entity identifier
        │
        ▼
Indexed lookup
        │
        ▼
Canonical entity
```

rather than:

```text
Entity identifier
        │
        ▼
Scan every collection on every request
```

Performance optimizations shall preserve canonical behavior and shall remain hidden behind stable service contracts.

---

## 3.21 Testability

Every major Core Engine responsibility shall be independently testable.

This includes:

- manifest compatibility
- package loading
- collection parsing
- reference validation
- typed resolution
- navigation transitions
- search resolution
- resource lookup
- error mapping
- platform adapters

Components should support controlled test inputs without requiring a complete application interface.

---

## 3.22 Observability

The Core Engine should expose sufficient diagnostics to understand runtime behavior.

Diagnostics may include:

- package load stages
- compatibility decisions
- validation results
- failed reference paths
- missing resource details
- navigation transitions
- search operation failures

Observability shall not require applications to inspect private engine state.

Sensitive or excessive content should not be logged unnecessarily.

---

## 3.23 Compatibility Before Capability

The Core Engine shall determine whether it can safely interpret a package before exposing normal runtime operations.

The intended order is:

```text
Open package
        │
        ▼
Read manifest
        │
        ▼
Check compatibility
        │
        ▼
Load required content
        │
        ▼
Expose runtime services
```

The engine shall not partially interpret an unsupported package and then allow applications to continue as though compatibility had been established.

---

## 3.24 Backward-Compatible Evolution

New engine capabilities should be introduced without unnecessarily breaking older compatible packages or applications.

Evolution should prefer:

- additive fields
- optional services
- explicit version checks
- stable public contracts
- controlled deprecation
- migration paths

Breaking changes shall be explicit and associated with appropriate version boundaries.

---

## 3.25 Application Neutrality

The Core Engine shall not contain business rules that belong to one specific application unless they are generalized as platform capabilities.

For example, the engine may support:

```text
Resolve an Occasion
```

but should not contain logic such as:

```text
Open the Nativity screen with a specific visual layout
```

Application-specific features should be built above the Core Engine.

---

## 3.26 Architectural Simplicity

The Core Engine should remain no more complex than necessary.

New abstractions should solve a concrete architectural problem.

The architecture should avoid:

- duplicate service layers
- unnecessary inheritance
- hidden service locators
- premature distributed systems
- runtime reflection without need
- generalized extension mechanisms without real use cases

Simplicity supports reliability and long-term maintainability.

---

## 3.27 Principle Precedence

Some principles may appear to compete in particular situations.

For example:

- lazy loading versus early validation
- graceful degradation versus explicit failure
- performance versus implementation simplicity
- backward compatibility versus architectural correction

When resolving such conflicts, the following priorities should guide decisions:

```text
1. Content correctness
2. Deterministic behavior
3. Clear responsibility boundaries
4. Compatibility and stability
5. Testability
6. Performance
7. Implementation convenience
```

Implementation convenience shall not override correctness or architectural integrity.

---

## 3.28 Architectural Principles Summary

The Core Engine architecture is founded on the following central principles:

```text
Clear responsibilities

Content and presentation separation

Build-time and runtime separation

Stable canonical identity

Typed reference resolution

Deterministic behavior

Declared order preservation

Immutable package content

Explicit runtime state

Explicit dependencies

Interface-driven components

Platform independence

Structured failure

No silent repair

Canonical content authority

Efficient indexed access

Testability

Observability

Controlled evolution

Application neutrality
```

These principles form the decision framework for all subsequent sections of this document.

When an implementation choice is uncertain, it should be evaluated by asking:

```text
Does this preserve deterministic content behavior?

Does this respect component boundaries?

Does this keep package content canonical and immutable?

Does this remain testable and platform-independent?

Does this expose failures explicitly?

Does this avoid application-specific coupling?
```

A design that consistently satisfies these questions is aligned with the Core Engine architecture.

# 4. Core Engine Boundaries

## 4.1 Purpose

This section defines the architectural boundaries of the Core Engine.

A clear boundary is essential to ensure that responsibilities remain well separated across the SyriacPlatform architecture.

The Core Engine shall neither assume responsibilities belonging to other platform layers nor expose its internal implementation unnecessarily to applications.

The purpose of this section is to define:

- what the Core Engine owns
- what it consumes
- what it produces
- what it exposes
- what remains outside its responsibility

---

## 4.2 Architectural Boundary

The Core Engine exists entirely between the Application Package and the Application.

```text
                Outside
────────────────────────────────────────────

Author Database

Build Tools

────────────────────────────────────────────
            Core Engine Boundary
────────────────────────────────────────────

Package Loader

Package Validator

Content Repository

Reference Resolver

Navigation Services

Search Services

Resource Resolver

Media Resolver

Localization Services

Runtime Context

Public API

────────────────────────────────────────────
                Outside
────────────────────────────────────────────

Application

User Interface

Platform UI Components
```

Everything inside this boundary belongs to the Core Engine.

Everything outside belongs to another architectural layer.

---

## 4.3 Inputs

The Core Engine consumes information produced by other platform components.

Its primary inputs are:

```text
Application Package

Platform Services

Runtime Configuration
```

More specifically:

```text
Application Package
        │
        ├── manifest.json
        ├── content/
        ├── indexes/
        └── media/
```

Platform services may include:

- file access
- package storage
- locale discovery
- logging
- media integration

The Core Engine shall not consume:

- the Author Database
- editorial metadata
- Build Tool internals
- package generation rules

---

## 4.4 Outputs

The Core Engine exposes runtime capabilities rather than raw package files.

Its outputs include:

- resolved canonical entities
- navigation structures
- search results
- runtime state
- resource locations
- diagnostics
- structured errors

Applications interact with these runtime services instead of interpreting package files directly.

```text
Application
        │
        ▼
Core Engine Services
        │
        ▼
Resolved Runtime Model
```

---

## 4.5 Internal Responsibilities

The Core Engine owns the following responsibilities.

```text
Package Loading

Compatibility Verification

Runtime Validation

Content Repository

Reference Resolution

Navigation

Search

Resource Resolution

Localization

Runtime Context

Diagnostics
```

Each responsibility shall remain inside the engine unless explicitly delegated to a platform abstraction.

---

## 4.6 Responsibilities Outside the Boundary

The following responsibilities do not belong to the Core Engine.

### Author Database

Responsible for:

- canonical editorial content
- editing workflows
- historical information
- authoring relationships
- content preparation

---

### Build Tools

Responsible for:

- package generation
- schema validation
- effective value computation
- search index generation
- package assembly
- release artifacts

---

### Application

Responsible for:

- user interface
- presentation
- interaction
- visual themes
- platform experience
- user preferences

---

### Platform Layer

Responsible for:

- filesystem access
- operating system integration
- audio playback
- document opening
- platform lifecycle
- hardware integration

The Core Engine coordinates these services but does not replace them.

---

## 4.7 Ownership Rules

Every runtime concern should have a single architectural owner.

For example:

```text
Selecting effective Melody
        ▼
Build Tools

Resolving effective Melody
        ▼
Core Engine

Displaying Melody
        ▼
Application
```

Similarly:

```text
Creating Search Index
        ▼
Build Tools

Executing Search
        ▼
Core Engine

Presenting Search Results
        ▼
Application
```

Ownership shall not overlap unnecessarily.

---

## 4.8 Canonical Content Boundary

Canonical entities remain inside the Application Package.

The Core Engine loads them into runtime memory but does not become their author.

```text
Application Package
        │
        ▼
Canonical Entity

Core Engine
        │
        ▼
Runtime Representation
```

The runtime representation may optimize access, but it shall preserve canonical meaning.

---

## 4.9 Runtime State Boundary

Canonical content and runtime state are separate architectural concerns.

```text
Canonical Content

TXT-001

QOL-001

MEL-001

──────────────

Runtime State

Current Entry Point

Current Occasion

Current Prayer Sequence

Navigation History

Current Search Query
```

The Core Engine owns runtime state.

It does not write runtime state back into canonical package content.

---

## 4.10 Public Boundary

Applications interact only through the Core Engine's public contracts.

```text
Application
        │
        ▼
Public API
        │
──────────────────────────
Internal Boundary
──────────────────────────
        │
        ▼
Internal Components
```

Applications shall not depend on:

- internal caches
- lookup maps
- parser classes
- JSON structures
- storage layout
- internal service wiring

This allows internal implementation to evolve independently.

---

## 4.11 Platform Boundary

Platform-specific services are accessed through explicit abstractions.

```text
Core Engine
        │
        ▼
Platform Interface
        │
        ├── Android
        ├── iOS
        └── Future Platforms
```

Examples include:

- reading files
- opening media
- logging
- discovering locale
- scheduling platform callbacks

Business logic remains inside the Core Engine.

Platform behavior remains outside.

---

## 4.12 Failure Boundary

Package-related failures terminate inside the Core Engine.

Applications receive structured engine errors instead of low-level implementation failures.

```text
Filesystem Error
        │
        ▼
Core Engine
        │
        ▼
PackageReadError
```

Likewise:

```text
Invalid JSON
        │
        ▼
Core Engine
        │
        ▼
PackageParseError
```

The Core Engine shields applications from package implementation details.

---

## 4.13 Extension Boundary

Future platform extensions should integrate by adding services inside defined architectural boundaries rather than bypassing them.

For example:

```text
Future Feature
        │
        ▼
Public API

Reference Resolver

Navigation

Search

Resource Services
```

New functionality should extend existing architectural layers instead of introducing parallel runtime paths.

---

## 4.14 Dependency Direction

Dependencies always flow inward toward the Core Engine.

```text
Application
        │
        ▼
Core Engine
        │
        ▼
Platform Services
```

The reverse dependency shall not exist.

For example:

- the Core Engine shall not depend on application screens
- the Package Loader shall not depend on UI components
- the Reference Resolver shall not depend on Android Activities
- the Search Service shall not depend on SwiftUI views

This preserves architectural independence.

---

## 4.15 Boundary Summary

The Core Engine owns the interpretation of Application Packages.

It does not own:

- editorial authoring
- package generation
- user presentation
- operating system behavior

Its architectural responsibility is summarized below.

```text
Application Package
        │
        ▼
────────────────────────────
      Core Engine
────────────────────────────

Load

Validate

Resolve

Navigate

Search

Locate Resources

Manage Runtime State

Expose Public Services

────────────────────────────
        │
        ▼
Application
```

Every responsibility outside these boundaries belongs to another layer of the SyriacPlatform architecture.

Maintaining these boundaries is essential to preserving a modular, testable, and platform-independent Core Engine.

# 5. High-Level Architecture

## 5.1 Purpose

This section defines the high-level internal architecture of the Core Engine.

It identifies the principal components of the engine, their responsibilities, their dependency relationships, and the main runtime flow between them.

The purpose of this section is not to define implementation classes or source-code packages in detail.

Instead, it establishes the architectural component model that later implementation shall preserve.

---

## 5.2 Architectural Overview

The Core Engine is composed of a set of cooperating runtime components.

```text
Application
        │
        ▼
Public API
        │
        ▼
Runtime Context
        │
        ├──► Navigation Service
        ├──► Search Service
        ├──► Reference Resolver
        ├──► Resource Resolver
        ├──► Media Resolver
        └──► Localization Service
                │
                ▼
        Runtime Content Store
                │
                ▼
        Loaded Application Package
```

Package initialization is handled by a separate loading and validation path.

```text
Application Package
        │
        ▼
Package Source
        │
        ▼
Package Loader
        │
        ▼
Compatibility Checker
        │
        ▼
Package Validator
        │
        ▼
Runtime Content Store
        │
        ▼
Runtime Context
        │
        ▼
Public API
```

These two views represent:

- the initialization path
- the normal runtime service path

---

## 5.3 Principal Components

The Core Engine contains the following principal architectural components:

```text
Core Engine

├── Public API
├── Engine Bootstrap
├── Package Source
├── Package Loader
├── Compatibility Checker
├── Package Validator
├── Runtime Content Store
├── Reference Resolver
├── Navigation Service
├── Search Service
├── Resource Resolver
├── Media Resolver
├── Localization Service
├── Runtime Context
├── Diagnostic System
├── Error Model
└── Platform Abstractions
```

Each component owns a distinct part of the engine lifecycle or runtime behavior.

---

## 5.4 Public API

The Public API is the application-facing boundary of the Core Engine.

It exposes stable operations and services required by applications.

Examples include:

```text
initialize engine

open package

retrieve Entry Points

resolve Occasion

open Prayer Sequence

navigate to Liturgical Item

search content

resolve media resource

read diagnostics

observe runtime state
```

The Public API shall:

- expose engine capabilities through stable contracts
- hide internal storage and parsing details
- return structured results or errors
- remain independent from application UI frameworks
- prevent direct access to mutable internal engine state

Applications shall communicate with the engine through this boundary.

---

## 5.5 Engine Bootstrap

The Engine Bootstrap coordinates Core Engine initialization.

Its responsibilities include:

- receiving runtime configuration
- receiving platform implementations
- creating internal services
- loading the selected Application Package
- verifying compatibility
- validating required package content
- constructing the Runtime Content Store
- initializing the Runtime Context
- exposing the ready Public API

The Bootstrap does not perform all initialization work itself.

It coordinates specialized components.

```text
Engine Bootstrap
        │
        ├──► Package Loader
        ├──► Compatibility Checker
        ├──► Package Validator
        ├──► Runtime Content Store
        ├──► Service Construction
        └──► Runtime Context
```

A successfully initialized engine shall expose only services backed by a usable runtime state.

---

## 5.6 Package Source

The Package Source abstracts the physical origin of an Application Package.

A package may be stored as:

- bundled application resources
- a local directory
- an extracted archive
- an application-managed download
- a test fixture
- another future supported source

The Core Engine shall not require higher-level services to know the physical storage mechanism.

```text
Package Loader
        │
        ▼
Package Source Interface
        │
        ├──► Bundled Package Source
        ├──► Local Package Source
        ├──► Downloaded Package Source
        └──► Test Package Source
```

The Package Source is responsible only for providing access to package files and resources.

It shall not interpret package meaning.

---

## 5.7 Package Loader

The Package Loader reads package files and converts them into parsed package models.

Its responsibilities include:

- reading `manifest.json`
- locating declared package collections
- reading JSON content files
- decoding package records
- reporting unreadable or malformed files
- supplying parsed content to later initialization stages

The Package Loader shall not:

- repair malformed package data
- resolve editorial decisions
- reorder canonical collections
- create missing entities
- expose package files directly to applications

Its output is a parsed package representation, not yet a fully usable runtime content model.

```text
Package Source
        │
        ▼
Package Loader
        │
        ▼
Parsed Package Data
```

---

## 5.8 Compatibility Checker

The Compatibility Checker determines whether the Core Engine can safely interpret the package.

It evaluates information such as:

- package format version
- schema version
- minimum engine version
- required capabilities
- package profile
- unsupported mandatory features

The compatibility decision shall occur before normal runtime services become available.

```text
Parsed Manifest
        │
        ▼
Compatibility Checker
        │
        ├──► Compatible
        ├──► Compatible with warnings
        └──► Incompatible
```

An incompatible package shall not proceed into normal runtime initialization.

---

## 5.9 Package Validator

The Package Validator verifies runtime-relevant package correctness.

Its responsibilities include checking:

- required files
- required collections
- unique identifiers
- required fields
- valid entity types
- valid references
- required media declarations
- structural assumptions
- index consistency where applicable

The Build Tools should already produce a valid package.

However, the Core Engine shall not assume that every received package is valid.

Runtime validation protects the application from:

- damaged package files
- incomplete downloads
- manual package modification
- version mismatch
- build defects
- storage corruption

Validation results shall be structured and classified by severity.

---

## 5.10 Runtime Content Store

The Runtime Content Store is the authoritative in-memory representation of loaded canonical package content.

It stores canonical entities in efficient lookup structures.

For example:

```text
Entry Points by ID

Occasions by ID

Prayers by ID

Prayer Sequences by ID

Liturgical Items by ID

Texts by ID

Qolos by ID

Melodies by ID

Media Assets by ID
```

Conceptually:

```text
Runtime Content Store

├── EntryPointStore
├── OccasionStore
├── PrayerStore
├── PrayerSequenceStore
├── LiturgicalItemStore
├── TextStore
├── QoloStore
├── MelodyStore
└── MediaAssetStore
```

The Runtime Content Store shall:

- preserve canonical package meaning
- provide efficient identifier lookup
- preserve declared ordering
- expose read-only access
- remain independent from UI state
- avoid leaking physical JSON structure

It shall not contain application presentation models.

---

## 5.11 Reference Resolver

The Reference Resolver resolves typed relationships between canonical entities.

Examples include:

```text
Entry Point
        │
        ▼
Occasion

Occasion
        │
        ▼
Prayer Sequence

Prayer Sequence
        │
        ▼
Liturgical Items

Liturgical Item
        │
        ├──► Text
        ├──► Qolo
        └──► Effective Melody

Qolo
        │
        ▼
Text

Melody
        │
        ▼
Media Assets
```

The Reference Resolver uses the Runtime Content Store.

It shall:

- require the expected entity type
- report missing targets
- preserve declared order
- avoid guessing relationships
- avoid silently substituting entities
- return structured resolution results

It is the authoritative runtime component for canonical relationship traversal.

---

## 5.12 Navigation Service

The Navigation Service manages navigation through package-defined runtime content.

It may support operations such as:

- selecting an Entry Point
- opening an Occasion
- selecting a Prayer
- opening a Prayer Sequence
- moving between Liturgical Items
- maintaining navigation history
- restoring a previous runtime position

The Navigation Service shall operate on canonical identifiers and resolved runtime entities.

It shall not define visual screens or platform navigation widgets.

```text
User Action
        │
        ▼
Application
        │
        ▼
Navigation Service
        │
        ▼
Updated Runtime Navigation State
        │
        ▼
Application Presentation
```

The application chooses how to display the resulting navigation state.

---

## 5.13 Search Service

The Search Service executes queries against the package-generated search index.

Its responsibilities may include:

- normalizing runtime search input
- querying indexed terms
- ranking or preserving generated result order
- resolving search references
- filtering by supported entity type
- returning application-consumable search results

The Search Service shall treat the search index as derived data.

A search result becomes meaningful only after its canonical target has been resolved.

```text
Search Query
        │
        ▼
Search Index
        │
        ▼
entityType + entityId
        │
        ▼
Reference Resolver
        │
        ▼
Canonical Entity
```

The Search Service shall not redefine canonical content.

---

## 5.14 Resource Resolver

The Resource Resolver translates logical package resource references into runtime-accessible resource locations.

It may resolve:

- text-related files
- notation documents
- images
- package-local documents
- static supplemental resources

For example:

```text
MED-003
        │
        ▼
media/notation/example.pdf
        │
        ▼
Platform-accessible resource handle
```

The application shall not construct package paths manually.

The Resource Resolver shall account for the active Package Source and platform storage abstraction.

---

## 5.15 Media Resolver

The Media Resolver specializes in resolving media-related entities and their physical resources.

Its responsibilities may include:

- resolving Melody media references
- resolving audio assets
- resolving notation assets
- choosing declared media by type
- verifying resource availability
- producing media descriptors usable by platform integrations

The Media Resolver does not necessarily perform playback.

```text
Melody
        │
        ▼
Media Resolver
        │
        ▼
Media Descriptor
        │
        ▼
Platform Media Integration
```

Actual playback or document presentation may remain a platform or application responsibility.

---

## 5.16 Localization Service

The Localization Service provides runtime access to localized package content where supported.

Its responsibilities may include:

- selecting the active language
- resolving localized labels
- applying defined fallback rules
- reporting missing required localization
- exposing available package languages

The Localization Service shall not translate content dynamically.

It consumes localization data already present in the Application Package.

```text
Canonical Entity
        │
        ▼
Localization Service
        │
        ▼
Localized Runtime Representation
```

Canonical identity remains independent from language.

---

## 5.17 Runtime Context

The Runtime Context represents the active engine session.

It connects loaded content, runtime services, and mutable runtime state.

It may contain:

- loaded package identity
- active package version
- selected language
- selected Entry Point
- active Occasion
- active Prayer Sequence
- current Liturgical Item
- navigation history
- search state
- service references
- runtime configuration

The Runtime Context shall not replace the Runtime Content Store.

The distinction is:

```text
Runtime Content Store
        │
        ▼
What content exists

Runtime Context
        │
        ▼
What the user or application is currently doing
```

One loaded package may support changing runtime state without altering canonical content.

---

## 5.18 Diagnostic System

The Diagnostic System records and exposes meaningful information about engine behavior.

It may receive diagnostics from:

- Bootstrap
- Package Loader
- Compatibility Checker
- Package Validator
- Reference Resolver
- Search Service
- Resource Resolver
- Platform Adapters

Diagnostic entries should include sufficient context, such as:

- severity
- category
- error code
- package identifier
- file or entity identifier
- operation
- human-readable message
- underlying technical cause where appropriate

Diagnostics shall remain separate from normal canonical content.

---

## 5.19 Error Model

The Error Model provides a common representation for failures across Core Engine components.

Typical error categories include:

```text
InitializationError

PackageReadError

PackageParseError

CompatibilityError

ValidationError

ReferenceResolutionError

NavigationError

SearchError

ResourceNotFoundError

MediaResolutionError

PlatformError
```

Components shall not expose arbitrary low-level exceptions as the primary application-facing failure contract.

Errors shall be mapped into stable engine-level categories.

---

## 5.20 Platform Abstractions

Platform Abstractions isolate operating-system and target-specific behavior.

They may include contracts for:

```text
File Access

Package Storage

Resource Handles

Locale Discovery

Logging

Clock or Time

Media Integration

External Document Opening
```

Shared engine components depend on these abstractions rather than on Android, iOS, or desktop APIs directly.

```text
Shared Component
        │
        ▼
Platform Contract
        │
        ├──► Android Adapter
        ├──► iOS Adapter
        └──► Test Adapter
```

This preserves platform-independent engine logic.

---

## 5.21 Initialization Flow

The complete high-level initialization flow is:

```text
1. Application creates engine configuration
        │
        ▼
2. Application supplies platform implementations
        │
        ▼
3. Engine Bootstrap receives Package Source
        │
        ▼
4. Package Loader reads manifest
        │
        ▼
5. Compatibility Checker evaluates package
        │
        ▼
6. Package Loader reads required collections
        │
        ▼
7. Package Validator verifies package integrity
        │
        ▼
8. Runtime Content Store is constructed
        │
        ▼
9. Runtime services are constructed
        │
        ▼
10. Runtime Context is initialized
        │
        ▼
11. Public API becomes ready
```

No normal runtime operation should be exposed before the required initialization stages have completed successfully.

---

## 5.22 Normal Runtime Flow

After initialization, the normal runtime flow is:

```text
Application Request
        │
        ▼
Public API
        │
        ▼
Relevant Core Engine Service
        │
        ▼
Runtime Content Store or Runtime Context
        │
        ▼
Reference or Resource Resolution
        │
        ▼
Structured Runtime Result
        │
        ▼
Application Presentation
```

For example:

```text
Open Entry Point ENTRY-001
        │
        ▼
Navigation Service
        │
        ▼
Reference Resolver
        │
        ▼
OCC-001
        │
        ▼
Runtime Context updated
        │
        ▼
Application receives resolved Occasion
```

---

## 5.23 Component Dependency Rules

The architecture shall maintain controlled dependency directions.

A simplified dependency model is:

```text
Public API
        │
        ▼
Runtime Services
        │
        ├──► Runtime Context
        ├──► Reference Resolver
        ├──► Runtime Content Store
        └──► Platform Abstractions
```

Initialization components follow a separate direction:

```text
Engine Bootstrap
        │
        ├──► Package Loader
        ├──► Compatibility Checker
        ├──► Package Validator
        └──► Service Construction
```

The following dependency rules shall apply:

- the Runtime Content Store shall not depend on navigation state
- canonical models shall not depend on UI models
- the Reference Resolver shall not depend on application screens
- the Package Loader shall not depend on the Search Service
- platform adapters shall implement shared contracts
- applications shall not depend on internal engine components
- diagnostics may receive events from components but shall not control canonical behavior

---

## 5.24 Read and Write Responsibilities

Most Core Engine components perform read-only operations against canonical content.

```text
Component                 Canonical Content Access

Package Loader            Creates parsed representation

Package Validator         Reads and verifies

Runtime Content Store     Stores read-only canonical models

Reference Resolver        Reads and resolves

Navigation Service        Reads content, writes runtime state

Search Service            Reads index and canonical targets

Resource Resolver         Reads resource declarations

Localization Service      Reads localized content

Runtime Context           Writes session state only
```

Canonical package content shall not be modified after successful loading.

Only runtime state is mutable.

---

## 5.25 Service Coordination

Some application operations require coordination between multiple components.

For example, opening a Liturgical Item may require:

```text
Navigation Service
        │
        ▼
Reference Resolver
        │
        ├──► resolve target entity
        ├──► resolve effective Melody
        └──► resolve related Text
                │
                ▼
Media Resolver
        │
        ▼
Resource Resolver
```

This coordination should occur through defined service relationships.

It shall not require the application to manually reconstruct the complete reference chain.

---

## 5.26 Architectural Component Model

The complete high-level component model may be represented as follows:

```text
┌─────────────────────────────────────────────┐
│                 Application                 │
└──────────────────────┬──────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────┐
│                 Public API                  │
└──────────────────────┬──────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
        ▼              ▼              ▼
 Navigation       Search         Localization
 Service          Service        Service
        │              │              │
        └──────────────┼──────────────┘
                       │
                       ▼
               Reference Resolver
                       │
                       ▼
              Runtime Content Store
                       │
             ┌─────────┴─────────┐
             │                   │
             ▼                   ▼
      Resource Resolver     Runtime Context
             │
             ▼
       Media Resolver
             │
             ▼
      Platform Abstractions
```

Initialization surrounds this runtime model:

```text
Package Source
        │
        ▼
Package Loader
        │
        ▼
Compatibility Checker
        │
        ▼
Package Validator
        │
        ▼
Runtime Content Store
        │
        ▼
Runtime Services
```

The Diagnostic System and Error Model operate across all layers.

---

## 5.27 Architectural Variability

Not every application must use every Core Engine service.

A simple application may use only:

- package loading
- navigation
- text resolution
- media resolution

A larger application may additionally use:

- full search
- multiple Entry Points
- cross-entity navigation
- multiple languages
- advanced diagnostics
- downloadable packages

Services may therefore be composed according to application capability.

However, optional service use shall not produce conflicting interpretations of package content.

---

## 5.28 High-Level Architecture Summary

The Core Engine is organized around two major flows.

### Initialization Flow

```text
Package Source
        │
        ▼
Load
        │
        ▼
Check Compatibility
        │
        ▼
Validate
        │
        ▼
Build Runtime Content Store
        │
        ▼
Initialize Services and Context
```

### Runtime Flow

```text
Application
        │
        ▼
Public API
        │
        ▼
Runtime Service
        │
        ▼
Canonical Content and Runtime State
        │
        ▼
Resolved Result
```

The principal architectural distinction is:

```text
Initialization Components
        │
        ▼
Prepare a safe runtime environment

Runtime Components
        │
        ▼
Provide stable application capabilities
```

Together, these components form one shared, deterministic, platform-independent runtime architecture for all SyriacPlatform applications.

# 6. Runtime Model

## 6.1 Purpose

This section defines the runtime model of the Core Engine.

The runtime model describes how the engine behaves from the moment initialization begins until the engine is shut down or replaced.

It defines:

- the engine lifecycle
- the principal runtime states
- the transitions between those states
- the conditions required for runtime readiness
- the relationship between canonical content and mutable runtime state
- the behavior of runtime services during an active session
- failure, recovery, reload, and shutdown behavior

The runtime model complements the high-level architecture.

The high-level architecture defines which components exist.

The runtime model defines how those components cooperate over time.

```text
High-Level Architecture
        │
        ▼
What components exist

Runtime Model
        │
        ▼
How the engine behaves during execution
```

---

## 6.2 Runtime Lifecycle

The Core Engine follows an explicit lifecycle.

A typical lifecycle is:

```text
Created
        │
        ▼
Initializing
        │
        ▼
Loading Package
        │
        ▼
Checking Compatibility
        │
        ▼
Validating Package
        │
        ▼
Constructing Runtime
        │
        ▼
Ready
        │
        ▼
Active Runtime Operations
        │
        ▼
Shutting Down
        │
        ▼
Stopped
```

A failure may occur during any initialization or runtime stage.

```text
Initialization Stage
        │
        ├──► Success
        │        │
        │        ▼
        │      Next Stage
        │
        └──► Failure
                 │
                 ▼
              Failed
```

The lifecycle shall be explicit and observable.

Applications shall not infer engine readiness from the existence of individual objects or partially initialized services.

---

## 6.3 Engine State Model

The Core Engine shall expose or internally maintain a defined state model.

The principal states are:

```text
Created

Initializing

Loading

CheckingCompatibility

Validating

ConstructingRuntime

Ready

Reloading

ShuttingDown

Stopped

Failed
```

A concrete implementation may represent some initialization stages as sub-states of `Initializing`.

However, the architectural meaning of each stage shall remain distinguishable for diagnostics, testing, and failure reporting.

---

## 6.4 Created State

The `Created` state represents an engine instance that has been constructed but not yet initialized.

At this stage, the engine may have received:

- runtime configuration
- platform abstractions
- diagnostic services
- a Package Source
- optional startup parameters

It shall not yet expose normal package-dependent runtime operations.

```text
Engine instance exists
        │
        ▼
No package loaded

No Runtime Content Store

No active Runtime Context

No package-dependent services ready
```

Operations such as the following shall not succeed in the `Created` state:

- retrieving Entry Points
- resolving canonical entities
- executing package search
- opening a Prayer Sequence
- resolving media resources

The engine may expose lifecycle operations such as:

```text
initialize

read current engine state

subscribe to diagnostics

cancel initialization where supported
```

---

## 6.5 Initializing State

The `Initializing` state represents the overall startup process.

Initialization coordinates the following stages:

```text
Prepare Runtime Environment
        │
        ▼
Open Package Source
        │
        ▼
Load Manifest
        │
        ▼
Check Compatibility
        │
        ▼
Load Package Collections
        │
        ▼
Validate Package
        │
        ▼
Construct Runtime Content Store
        │
        ▼
Construct Services
        │
        ▼
Create Runtime Context
        │
        ▼
Enter Ready State
```

Initialization shall be treated as one controlled lifecycle operation.

The engine shall not become partially available merely because some internal services have been constructed.

---

## 6.6 Initialization Inputs

Initialization requires a complete set of required inputs.

These may include:

```text
Package Source

Engine Configuration

Platform Abstractions

Supported Version Information

Diagnostic Reporter

Optional Locale Configuration

Optional Feature Configuration
```

Before package loading begins, the Engine Bootstrap should verify that required runtime dependencies are available.

Examples of missing initialization dependencies include:

- no Package Source
- no file-access abstraction
- invalid engine configuration
- unsupported platform capability required by configuration
- unavailable required decoder

Such failures belong to engine initialization rather than package validation.

---

## 6.7 Initialization Atomicity

Engine initialization should be atomic from the application's perspective.

This means the application observes either:

```text
Initialization completed successfully
        │
        ▼
Engine is Ready
```

or:

```text
Initialization failed
        │
        ▼
Engine is Failed
```

The application shall not receive a runtime API that appears ready while required components remain unavailable.

Internally, initialization occurs in stages.

Externally, package-dependent runtime capability becomes available only after all mandatory stages have completed successfully.

---

## 6.8 Package Loading Phase

The package loading phase begins when the Package Source is opened.

The intended sequence is:

```text
Open Package Source
        │
        ▼
Read manifest.json
        │
        ▼
Decode Manifest
        │
        ▼
Retain Manifest for Compatibility Check
```

The manifest shall be loaded before the complete package collections because it defines the package identity, structure, and compatibility information required for later decisions.

Failures at this stage may include:

```text
Package source unavailable

manifest.json missing

manifest.json unreadable

manifest JSON malformed

required manifest fields missing
```

A failure to load a usable manifest shall prevent further package initialization.

---

## 6.9 Manifest-First Rule

The Core Engine shall apply a manifest-first initialization rule.

```text
Package Source
        │
        ▼
Manifest
        │
        ▼
Compatibility Decision
        │
        ▼
Remaining Package Content
```

The engine shall not load and interpret all canonical collections before determining that the package format is supported.

This avoids:

- unnecessary work
- misleading parse errors from unsupported schemas
- partial interpretation of incompatible packages
- accidental use of unsupported package capabilities

Limited physical package inspection may occur before the compatibility decision, but semantic interpretation shall begin with the manifest.

---

## 6.10 Compatibility Phase

After the manifest has been parsed, the engine enters the compatibility-checking phase.

The Compatibility Checker evaluates whether the current engine can safely consume the package.

The decision may consider:

- package format version
- schema version
- minimum supported engine version
- required feature declarations
- mandatory package capabilities
- application profile constraints
- platform capability requirements where declared

The result shall be one of the following:

```text
Compatible

Compatible with Warnings

Incompatible
```

### Compatible

The package may proceed to loading and validation.

### Compatible with Warnings

The package may proceed, but diagnostics shall record non-fatal compatibility conditions.

### Incompatible

Initialization shall stop.

The engine shall enter the `Failed` state with a structured `CompatibilityError`.

---

## 6.11 Collection Loading Phase

After compatibility is established, the Package Loader reads the required package collections.

A typical loading sequence may include:

```text
entry-points.json

occasions.json

prayers.json

prayer-sequences.json

liturgical-items.json

texts.json

qolos.json

melodies.json

media-assets.json

search-index.json
```

The exact physical order may vary where no dependency requires a specific order.

However, the resulting runtime meaning shall remain deterministic.

The loader shall produce parsed package models without yet treating every reference as successfully resolved.

```text
Package Files
        │
        ▼
Decoded Package Models
        │
        ▼
Package Validation
```

---

## 6.12 Required and Optional Package Content

The runtime model shall distinguish between required and optional package content.

Required content is necessary for the declared package profile or capabilities.

Optional content may be absent without making the package unusable.

For example:

```text
Required canonical collection missing
        │
        ▼
Fatal validation failure
```

while:

```text
Optional notation resource absent
        │
        ▼
Recoverable condition or warning
```

Requiredness shall be determined by:

- the Application Package Specification
- the package manifest
- the declared package profile
- enabled mandatory capabilities

The Core Engine shall not invent requiredness based on one application's UI assumptions.

---

## 6.13 Validation Phase

After package collections are parsed, the engine enters the validation phase.

Runtime validation occurs before the Runtime Content Store becomes authoritative.

The validator checks conditions such as:

```text
Required collections exist

Required fields are present

Identifiers are unique

Identifier formats are acceptable

Declared entity types are valid

References point to existing entities

References point to the correct entity types

Ordered collections contain valid identifiers

Required media declarations exist

Declared resource paths are acceptable

Search references resolve to canonical targets
```

The complete build-time validation may be more extensive.

Runtime validation is intended to establish that the package is safe and coherent enough for runtime consumption.

---

## 6.14 Validation Result Model

Validation shall produce a structured result.

A validation result may contain:

```text
Validation Status

Fatal Errors

Recoverable Errors

Warnings

Informational Diagnostics
```

Conceptually:

```text
PackageValidationResult

├── isUsable
├── fatalIssues
├── recoverableIssues
├── warnings
└── information
```

The engine shall not reduce validation to one unstructured text message.

Structured results allow:

- precise diagnostics
- automated tests
- future validation tools
- application-level error presentation
- clear distinction between blocking and non-blocking conditions

---

## 6.15 Validation Failure Behavior

If validation identifies a fatal issue, initialization shall stop.

Examples include:

- duplicate canonical identifier
- unresolved required reference
- wrong entity type for a required relationship
- missing required collection
- invalid required sequence structure
- unsupported mandatory entity type
- malformed canonical record preventing safe interpretation

The transition is:

```text
Validating
        │
        ▼
Fatal Validation Issue
        │
        ▼
Failed
```

The engine shall not construct a normal Runtime Content Store from package data known to be structurally unsafe.

---

## 6.16 Recoverable Validation Conditions

Some package conditions may be recoverable.

Examples may include:

- missing optional media asset
- missing optional localized label
- unused orphaned entity
- unavailable optional notation resource
- non-critical derived-index inconsistency

A recoverable condition shall not silently disappear.

The engine shall:

- classify it
- record a diagnostic
- preserve enough context to identify the affected entity or resource
- restrict only the affected capability where practical

For example:

```text
Package remains usable
        │
        ├──► Text display available
        ├──► Navigation available
        └──► One optional notation resource unavailable
```

Graceful degradation shall not change canonical meaning.

---

## 6.17 Runtime Construction Phase

After successful validation, the engine constructs the runtime environment.

This phase includes:

```text
Create identifier lookup maps

Preserve ordered references

Construct Runtime Content Store

Create Reference Resolver

Create Search Service

Create Navigation Service

Create Resource Resolver

Create Media Resolver

Create Localization Service

Create Runtime Context

Connect diagnostics

Expose Public API implementation
```

The construction phase transforms validated package models into efficient runtime structures.

---

## 6.18 Runtime Content Store Construction

The Runtime Content Store shall be constructed from validated canonical package data.

For each canonical collection, the engine may create indexed lookup structures.

For example:

```text
List of Qolos
        │
        ▼
Map<QoloId, Qolo>
```

Likewise:

```text
List of Texts
        │
        ▼
Map<TextId, Text>
```

The store may preserve both:

- efficient identifier lookup
- canonical declared ordering where meaningful

The store shall expose read-only content after construction.

---

## 6.19 Derived Runtime Structures

During runtime construction, the engine may build derived structures for efficient access.

Examples include:

- identifier maps
- reverse-reference indexes
- resolved sequence caches
- search lookup tables
- media-type indexes
- localization lookup maps

These structures shall remain derived.

```text
Canonical Package Content
        │
        ▼
Derived Runtime Structures
        │
        ▼
Faster Access
```

They shall not become independent sources of canonical meaning.

If a derived structure cannot be built consistently from validated canonical data, runtime construction shall report the condition.

---

## 6.20 Service Construction

Runtime services shall be constructed only after their mandatory dependencies are available.

A simplified dependency order is:

```text
Runtime Content Store
        │
        ▼
Reference Resolver
        │
        ├──► Search Service
        ├──► Navigation Service
        ├──► Media Resolver
        └──► Localization-related resolution
```

Resource and platform-dependent services may additionally require:

```text
Package Source

Platform Resource Abstraction

Platform Logging

Locale Provider
```

No service shall be exposed as ready while a mandatory dependency remains absent.

---

## 6.21 Runtime Context Initialization

The Runtime Context is created after canonical content and required services are available.

Its initial state may include:

```text
Loaded package identity

Active package version

Selected language

Available Entry Points

No selected Entry Point

No active Occasion

No active Prayer Sequence

No current Liturgical Item

Empty navigation history

Empty search state
```

The Runtime Context shall begin in a valid neutral state.

The engine shall not automatically choose content unless the package architecture or explicit initialization configuration defines a default.

---

## 6.22 Default Runtime Selection

The package or application configuration may define a default Entry Point or startup target.

Where such a default is explicitly declared, initialization may resolve it before entering the `Ready` state.

For example:

```text
Manifest declares default Entry Point
        │
        ▼
Resolve ENTRY-001
        │
        ▼
Initialize selected Entry Point state
```

If no default is declared, the engine should enter the `Ready` state without an active content selection.

The Core Engine shall not choose the first entity merely because it appears first in physical file storage unless that ordering is explicitly defined as authoritative.

---

## 6.23 Ready State

The engine enters the `Ready` state after all mandatory initialization stages have succeeded.

The `Ready` state means:

- the package is compatible
- required package content has loaded
- blocking validation has passed
- the Runtime Content Store exists
- mandatory services are available
- the Runtime Context is valid
- the Public API may accept normal runtime operations

```text
Initialization complete
        │
        ▼
EngineState.Ready
```

`Ready` does not mean that every optional resource has already been opened or every lazy structure has been computed.

It means that the engine can safely serve its declared runtime capabilities.

---

## 6.24 Runtime Capability Availability

The engine may expose a capability model describing which services are available.

For example:

```text
Navigation Available

Search Available

Audio Media Available

Notation Available

Localization Available

Multiple Packages Unsupported

Advanced Search Unavailable
```

Capability availability may depend on:

- package declarations
- engine version
- platform support
- optional package content
- application configuration

A ready engine may therefore have some optional capabilities unavailable.

Such absence shall be explicit rather than discovered through arbitrary runtime failures.

---

## 6.25 Runtime Session

A runtime session begins when the engine enters the `Ready` state and continues until reload or shutdown.

The session combines:

```text
Immutable Canonical Content
        │
        +
Mutable Runtime Context
        │
        +
Runtime Services
        │
        ▼
Active Engine Session
```

Canonical content remains stable throughout the session.

Runtime state may change in response to application operations.

---

## 6.26 Runtime Operation Model

A normal runtime operation follows this pattern:

```text
Application Request
        │
        ▼
Public API
        │
        ▼
Verify Engine State
        │
        ▼
Validate Operation Input
        │
        ▼
Invoke Runtime Service
        │
        ▼
Resolve Canonical Content
        │
        ▼
Update Runtime Context where required
        │
        ▼
Return Structured Result
```

Every package-dependent operation shall first require an engine state that permits the operation.

---

## 6.27 State Guarding

Runtime operations shall be guarded by engine state.

For example:

```text
Operation requested while Ready
        │
        ▼
Operation may proceed
```

```text
Operation requested while Initializing
        │
        ▼
Return EngineNotReady error
```

```text
Operation requested while Failed
        │
        ▼
Return EngineFailed error
```

```text
Operation requested while Stopped
        │
        ▼
Return EngineStopped error
```

Applications shall not receive undefined behavior merely because an operation was called during an invalid lifecycle stage.

---

## 6.28 Read Operations

Read operations do not alter canonical content or runtime navigation state.

Examples include:

- retrieve available Entry Points
- retrieve an Occasion by identifier
- retrieve a Text by identifier
- inspect available languages
- inspect package metadata
- resolve a Media Asset descriptor
- read current diagnostics

A read operation may populate internal caches, but such caching shall not change observable canonical results.

---

## 6.29 State-Changing Operations

State-changing runtime operations modify the Runtime Context only.

Examples include:

- select an Entry Point
- open an Occasion
- select a Prayer Sequence
- move to the next Liturgical Item
- return to a previous navigation position
- change the selected language
- update the current search query
- select a search result

The relationship is:

```text
Canonical Content
        │
        └──► remains unchanged

Runtime Context
        │
        └──► updated
```

State changes shall be explicit and deterministic.

---

## 6.30 Navigation Runtime Flow

A navigation operation may follow this sequence:

```text
Application selects ENTRY-001
        │
        ▼
Navigation Service validates request
        │
        ▼
Reference Resolver resolves target
        │
        ▼
OCC-001 retrieved from Runtime Content Store
        │
        ▼
Runtime Context updated
        │
        ▼
Navigation result returned
```

Opening a Prayer Sequence may continue:

```text
OCC-001
        │
        ▼
Resolve PS-001
        │
        ▼
Resolve ordered Liturgical Item IDs
        │
        ▼
[
  LI-001,
  LI-002,
  LI-003
]
        │
        ▼
Runtime Context stores active sequence and position
```

The declared order shall remain authoritative.

---

## 6.31 Search Runtime Flow

A search operation follows a separate but related flow.

```text
Application submits query
        │
        ▼
Search Service normalizes query
        │
        ▼
Search Index queried
        │
        ▼
Search entries returned
        │
        ▼
entityType + entityId validated
        │
        ▼
Reference Resolver resolves canonical entities
        │
        ▼
Search results returned
```

Executing a search need not change navigation state.

Selecting a search result may produce a separate navigation operation.

This distinction prevents search activity from silently altering the user's active content position.

---

## 6.32 Resource Resolution Runtime Flow

A resource operation may follow this sequence:

```text
Application requests media for MEL-001
        │
        ▼
Media Resolver loads Melody
        │
        ▼
Resolve declared Media Asset IDs
        │
        ▼
Resource Resolver maps package file references
        │
        ▼
Platform-accessible resource descriptors returned
```

The engine may verify resource existence eagerly or lazily depending on the resource type and validation policy.

Missing resources shall produce structured results.

---

## 6.33 Runtime Result Model

Runtime operations should return structured outcomes.

Conceptually:

```text
RuntimeResult<T>

├── Success<T>
└── Failure<EngineError>
```

A successful result contains the requested value.

A failed result contains a stable engine-level error.

Optional operations may also represent absence explicitly where absence is valid.

For example:

```text
Found canonical entity

Entity not found

Operation invalid in current state

Resource unavailable

Unsupported capability
```

The engine shall distinguish valid absence from operational failure.

---

## 6.34 Runtime Error Isolation

A recoverable failure during one runtime operation should not automatically invalidate the entire engine session.

For example:

```text
Notation file missing
        │
        ▼
Notation operation fails
        │
        ▼
Engine remains Ready
```

Similarly:

```text
Search query invalid
        │
        ▼
Search request fails
        │
        ▼
Navigation remains usable
```

The engine should enter the global `Failed` state only when a failure makes continued runtime interpretation unsafe or impossible.

---

## 6.35 Fatal Runtime Failure

A fatal runtime failure is a condition that invalidates the active engine session.

Examples may include:

- Runtime Content Store corruption
- unrecoverable internal invariant violation
- package source becoming unusable when essential lazy content remains required
- mandatory service failure preventing safe operation
- impossible runtime state transition indicating engine inconsistency

The transition is:

```text
Ready
        │
        ▼
Fatal Runtime Failure
        │
        ▼
Failed
```

The engine shall record diagnostics and reject further normal runtime operations.

---

## 6.36 Runtime Invariants

The Core Engine shall preserve runtime invariants throughout the active session.

Examples include:

```text
Every active entity identifier resolves to the expected type

The current Liturgical Item belongs to the active Prayer Sequence

The active sequence position is within valid bounds

Navigation history contains valid runtime locations

Selected language belongs to available package languages

Canonical content remains unchanged

The loaded package identity remains stable during the session
```

State-changing operations shall validate these invariants before committing a new Runtime Context.

---

## 6.37 Atomic Runtime State Transitions

A runtime state transition should be atomic from the application perspective.

For example, opening a Prayer Sequence should not expose an intermediate state in which:

- the active sequence changed
- but the current Liturgical Item still belongs to the previous sequence

The intended behavior is:

```text
Previous valid Runtime Context
        │
        ▼
Compute proposed transition
        │
        ▼
Validate complete new state
        │
        ▼
Commit new Runtime Context
```

If transition validation fails, the previous valid state should remain active.

---

## 6.38 Runtime State Observation

Applications may need to observe Runtime Context changes.

The engine may therefore expose a state observation contract.

Examples include:

```text
Current engine lifecycle state

Current navigation state

Selected language

Active package metadata

Current search state
```

State observation shall expose stable public models.

It shall not expose mutable internal objects or require applications to poll private service state.

---

## 6.39 Concurrency Model

The Core Engine shall define a consistent concurrency policy.

The exact implementation may vary by target platform, but the architectural behavior shall remain predictable.

The concurrency model should ensure that:

- initialization runs as one coordinated operation
- duplicate initialization attempts are controlled
- runtime state transitions do not race
- canonical stores are safe for concurrent reads where supported
- mutable Runtime Context updates are serialized or otherwise protected
- cancellation does not leave partial committed state
- diagnostics preserve meaningful operation context

Applications shall not be required to solve internal engine race conditions.

---

## 6.40 Duplicate Initialization

An engine instance shall not perform two uncontrolled initialization processes simultaneously.

If initialization is requested while already initializing, the engine shall apply one defined policy.

Possible supported policies include:

```text
Return existing initialization result

Reject duplicate request

Cancel and restart only through explicit reload operation
```

The policy shall be deterministic and documented.

The engine shall not create competing Runtime Content Stores within one active instance.

---

## 6.41 Cancellation During Initialization

Initialization may support cancellation.

If cancellation occurs, the engine shall:

- stop further initialization work where practical
- avoid exposing partial runtime services
- release temporary resources
- retain structured diagnostic context
- enter a defined non-ready state

Depending on the implementation contract, the resulting state may be:

```text
Created

Stopped

Failed with cancellation classification
```

Cancellation shall not be reported as package corruption unless the package itself caused the failure.

---

## 6.42 Runtime Session Identity

Each successfully initialized runtime session should have a distinct session identity or equivalent diagnostic context.

This supports:

- distinguishing old and new package loads
- preventing stale asynchronous results from updating a new session
- correlating diagnostics
- testing reload behavior
- identifying resource ownership

Conceptually:

```text
Engine Instance
        │
        ├──► Runtime Session A
        │
        └──► Runtime Session B after reload
```

Results produced for Session A shall not mutate Session B.

---

## 6.43 Reloading

Reloading replaces the current runtime session with a newly initialized session.

Reload may be required when:

- a new Application Package version becomes available
- the active package changes
- package resources are replaced
- configuration requiring reconstruction changes
- the application explicitly requests a fresh runtime

The high-level flow is:

```text
Ready
        │
        ▼
Reload Requested
        │
        ▼
Reloading
        │
        ├──► Load replacement package
        ├──► Check compatibility
        ├──► Validate
        ├──► Construct new runtime
        └──► Prepare new Runtime Context
                │
                ▼
          Commit replacement session
                │
                ▼
              Ready
```

---

## 6.44 Reload Atomicity

Reloading should preserve the current valid session until the replacement session is ready, where platform resources and memory constraints permit.

The preferred model is:

```text
Current Ready Session
        │
        ├──► remains usable or safely suspended
        │
        ▼
Construct Candidate Session
        │
        ├──► Candidate succeeds
        │        │
        │        ▼
        │   Replace current session
        │
        └──► Candidate fails
                 │
                 ▼
        Retain previous valid session
```

This prevents a failed package update from unnecessarily destroying a usable runtime.

An implementation that cannot retain both sessions temporarily shall still ensure that reload failure behavior is explicit.

---

## 6.45 Runtime State Restoration After Reload

A reload may attempt to restore compatible runtime state.

Examples include:

- selected Entry Point
- active Occasion
- selected language
- active Prayer Sequence
- current Liturgical Item

Restoration shall occur only when the referenced canonical identifiers still exist and remain valid in the replacement package.

```text
Previous active ID exists and remains compatible
        │
        ▼
State may be restored
```

```text
Previous active ID missing or incompatible
        │
        ▼
Fallback to defined neutral or default state
```

The engine shall not restore state by label matching or arbitrary positional similarity.

---

## 6.46 Package Switching

Switching from one package to another is a specialized form of reload.

A package switch shall create a new runtime session.

```text
Package A Session
        │
        ▼
Switch Request
        │
        ▼
Package B Initialization
        │
        ▼
Package B Session
```

Canonical entities from Package A shall not remain resolvable inside Package B unless multi-package behavior is explicitly supported by a future architecture.

Runtime state tied to Package A shall be discarded or migrated only through explicit rules.

---

## 6.47 Shutdown

Shutdown terminates the active runtime session and releases engine-owned runtime resources.

The shutdown sequence may include:

```text
Reject new runtime operations

Cancel engine-owned pending work

Stop state observation streams where required

Release package resource handles

Clear runtime caches

Dispose platform adapters where owned

Clear Runtime Context

Clear Runtime Content Store references

Record shutdown diagnostics
```

The engine then enters the `Stopped` state.

---

## 6.48 Shutdown Ownership

The Core Engine shall release only resources it owns.

For example, if the application supplies a shared platform logging service, the engine shall not necessarily destroy that service.

Ownership shall be explicit for:

- file handles
- package archives
- media descriptors
- asynchronous tasks
- caches
- platform adapters
- diagnostic sinks

Ambiguous ownership creates resource leaks or premature disposal and shall be avoided.

---

## 6.49 Operations During Shutdown

Once shutdown begins, new normal runtime operations shall be rejected.

```text
ShuttingDown
        │
        ▼
New navigation request
        │
        ▼
EngineShuttingDown error
```

Operations already in progress may:

- complete
- be cancelled
- be allowed to finish within a controlled boundary

The chosen behavior may depend on operation type, but it shall not allow state updates after the engine has entered `Stopped`.

---

## 6.50 Stopped State

The `Stopped` state represents an engine instance with no active runtime session.

In this state:

- canonical content is unavailable
- Runtime Context is unavailable
- package-dependent services reject operations
- engine-owned resources have been released

Whether a stopped engine instance may be initialized again is an implementation contract.

The architecture may support either:

```text
Stopped instance may be reinitialized
```

or:

```text
Stopped instance is terminal and a new engine instance is required
```

The selected policy shall be consistent and explicit.

---

## 6.51 Failed State

The `Failed` state represents an engine that cannot safely provide normal runtime services.

The failure may originate during:

- initialization
- compatibility checking
- package validation
- runtime construction
- active runtime operation
- reload

The Failed state shall retain:

- the primary structured error
- relevant diagnostics
- lifecycle stage
- package identity where known
- underlying technical cause where appropriate

Normal runtime operations shall be rejected.

Recovery shall require an explicit supported action such as:

```text
retry initialization

reload package

switch package

create new engine instance
```

---

## 6.52 Retry Behavior

A retry shall not be an invisible repetition of a failed operation.

The engine should distinguish:

- retrying one recoverable runtime operation
- retrying initialization
- reloading the same package
- opening a replacement package

For initialization retry, temporary state from the previous attempt shall not contaminate the new attempt.

```text
Failed Initialization
        │
        ▼
Clear incomplete construction state
        │
        ▼
Begin new initialization attempt
```

Diagnostics should preserve attempt boundaries.

---

## 6.53 Lifecycle Transition Rules

The allowed high-level transitions are:

```text
Created
   │
   ▼
Initializing
   │
   ├──► Ready
   │
   └──► Failed

Ready
   │
   ├──► Reloading
   │       ├──► Ready
   │       └──► Ready or Failed
   │
   ├──► ShuttingDown
   │       ▼
   │     Stopped
   │
   └──► Failed

Failed
   │
   ├──► Initializing or Reloading through explicit recovery
   └──► ShuttingDown
           ▼
         Stopped
```

Invalid transitions shall be rejected.

Examples include:

```text
Created directly to Ready
        ▼
Invalid

Stopped directly to Ready without initialization
        ▼
Invalid

Failed performing normal navigation
        ▼
Invalid
```

---

## 6.54 Lifecycle State Table

```text
State                  Package Available   Runtime Services   State Changes

Created                No                  No                 Initialization only

Initializing           Partial/internal    No                 Initialization-controlled

Loading                Partial/internal    No                 Loader-controlled

CheckingCompatibility  Manifest only       No                 Checker-controlled

Validating             Parsed package      No                 Validator-controlled

ConstructingRuntime    Validated package   No                 Bootstrap-controlled

Ready                  Yes                 Yes                Runtime operations allowed

Reloading              Current/candidate   Limited/defined    Reload-controlled

ShuttingDown           Being released      No new operations  Shutdown-controlled

Stopped                No                  No                 None or reinitialize

Failed                 Unsafe/unavailable  No normal use      Recovery or shutdown
```

This table describes architectural availability rather than concrete thread-level behavior.

---

## 6.55 Runtime Consistency Across Platforms

The same lifecycle and state-transition semantics shall apply across supported platforms.

Android and iOS implementations may differ in:

- file APIs
- coroutine or concurrency mechanisms
- resource handles
- lifecycle integration
- error wrapping

They shall not differ in:

- when a package is considered ready
- compatibility behavior
- canonical reference resolution
- sequence ordering
- runtime state invariants
- failure classification
- allowed lifecycle transitions

Platform implementation shall not redefine engine semantics.

---

## 6.56 Application Lifecycle Integration

The Core Engine lifecycle and application lifecycle are related but distinct.

For example:

```text
Application enters background
        │
        ▼
Engine may remain Ready
```

```text
Application screen closes
        │
        ▼
Engine session may remain active
```

```text
Application process terminates
        │
        ▼
Engine runtime ends
```

The Core Engine shall not assume that every UI lifecycle event requires package reload or shutdown.

Applications decide how long to retain an engine instance based on their architecture.

The engine exposes explicit lifecycle operations rather than binding itself directly to a specific UI framework lifecycle.

---

## 6.57 Runtime Persistence Boundary

The Runtime Context represents active session state.

Persistence of that state across application restarts may be supported by another layer.

For example:

```text
Core Engine
        │
        ▼
Exports stable restorable state identifiers

Application or Persistence Service
        │
        ▼
Stores user session information
```

The Core Engine may define serializable state descriptors, but it shall not necessarily own long-term user preference or database persistence.

Any restored state shall be validated against the currently loaded package before activation.

---

## 6.58 Runtime Model and Testing

The runtime lifecycle shall be directly testable.

Tests should cover:

```text
Created state restrictions

Successful initialization

Manifest loading failure

Compatibility rejection

Validation failure

Successful Ready transition

Runtime operation before Ready

Valid navigation state transition

Failed state behavior

Recoverable operation failure

Reload success

Reload failure

State restoration after reload

Shutdown behavior

Invalid lifecycle transitions

Concurrent initialization protection
```

The test architecture should verify state transitions as explicit outcomes rather than inferring them from incidental side effects.

---

## 6.59 Runtime Model Summary

The Core Engine runtime model is based on an explicit lifecycle.

```text
Created
        │
        ▼
Initialize
        │
        ├──► Load
        ├──► Check Compatibility
        ├──► Validate
        ├──► Construct Runtime
        └──► Create Runtime Context
                │
                ▼
              Ready
                │
                ├──► Navigate
                ├──► Search
                ├──► Resolve
                ├──► Update Runtime State
                ├──► Reload
                └──► Shutdown
```

The central runtime rules are:

```text
No package-dependent operation before Ready

Manifest compatibility is checked before full interpretation

Blocking validation occurs before runtime exposure

Canonical content remains immutable

Runtime Context contains mutable session state

State transitions are explicit and atomic

Recoverable failures remain isolated

Fatal failures move the engine to Failed

Reload creates a new runtime session

Shutdown releases engine-owned resources

Platform implementations preserve identical runtime semantics
```

The runtime model ensures that applications interact with a predictable engine whose state, readiness, failures, and transitions are always explicit.

# 7. Package Loading

## 7.1 Purpose

This section defines the architectural model for loading an Application Package into the Core Engine.

Package Loading is responsible for transforming a physical package into a validated runtime representation that can be consumed by the remainder of the engine.

The loading process establishes the foundation upon which all subsequent runtime services depend.

Its responsibilities include:

- discovering package contents
- loading package metadata
- reading canonical collections
- decoding package files
- constructing intermediate package models
- reporting loading failures
- preparing package data for runtime validation

Package Loading does not interpret canonical meaning beyond what is required to read the package structure.

---

## 7.2 Architectural Role

Package Loading is the first major operational stage of the Core Engine.

It bridges the gap between physical package storage and the engine's internal runtime model.

```text
Physical Package
        │
        ▼
Package Source
        │
        ▼
Package Loading
        │
        ▼
Parsed Package Model
        │
        ▼
Package Validation
```

The Package Loader shall remain independent from application logic, navigation, search, and presentation.

Its responsibility ends when a complete parsed package representation has been produced.

---

## 7.3 Loading Responsibilities

The Package Loading subsystem is responsible for:

- locating the package
- opening the package source
- discovering required package files
- loading the manifest
- reading canonical collections
- decoding package resources
- producing parsed package models
- reporting loading diagnostics
- preserving package identity
- maintaining deterministic loading behavior

It shall not:

- resolve canonical references
- validate editorial correctness
- construct runtime services
- build navigation structures
- execute searches
- infer missing package data
- repair malformed content

These responsibilities belong to later runtime stages.

---

## 7.4 Package Source

Package Loading operates through the Package Source abstraction.

The Package Source represents the physical origin of the package while hiding storage-specific implementation details.

Possible package sources include:

```text
Bundled Package

Local Directory

Extracted Archive

Downloaded Package

Test Package

Future Package Source
```

The loader interacts only with the Package Source contract.

```text
Package Loader
        │
        ▼
Package Source Interface
        │
        ├──► Android Assets
        ├──► iOS Bundle
        ├──► Local Filesystem
        ├──► Download Cache
        └──► Test Fixtures
```

The Package Loader shall not require knowledge of the underlying storage mechanism.

---

## 7.5 Package Discovery

Before loading begins, the Package Loader identifies the package structure.

Discovery includes determining:

- package root
- manifest location
- required directories
- declared collections
- available resources

Conceptually:

```text
Package Root
        │
        ├── manifest.json
        ├── content/
        ├── indexes/
        └── media/
```

Discovery does not imply that every file is immediately read.

It establishes the physical structure that subsequent loading stages will use.

---

## 7.6 Manifest Loading

The manifest is the first package file interpreted by the Core Engine.

The loader shall:

```text
Locate manifest.json
        │
        ▼
Read file
        │
        ▼
Decode Manifest
        │
        ▼
Provide Manifest Model
```

The Manifest provides essential package metadata, including:

- package identity
- package version
- schema version
- compatibility information
- declared collections
- capabilities
- package profile

The loading process shall not continue to semantic interpretation before the Manifest has been successfully decoded.

---

## 7.7 Manifest Integrity

The Manifest shall satisfy the minimum structural requirements necessary for further package loading.

Examples include:

- file exists
- valid JSON
- required fields present
- supported encoding
- structurally readable

Failure to obtain a usable Manifest shall terminate Package Loading.

The resulting failure shall be reported as a structured loading error.

---

## 7.8 Loading Strategy

Package Loading follows a deterministic strategy.

The high-level sequence is:

```text
Open Package Source
        │
        ▼
Load Manifest
        │
        ▼
Determine Package Structure
        │
        ▼
Load Declared Collections
        │
        ▼
Decode Package Models
        │
        ▼
Produce Parsed Package Representation
```

The loading strategy shall remain independent of:

- application type
- user interface
- target platform
- package content volume

Given the same package, the resulting parsed representation shall be equivalent.

---

## 7.9 Collection Discovery

After the Manifest has been successfully loaded, the Package Loader discovers the package collections declared by the package.

Typical collections include:

```text
Entry Points

Occasions

Prayers

Prayer Sequences

Liturgical Items

Texts

Qolos

Melodies

Media Assets

Search Index
```

The Package Loader shall rely on package declarations rather than hard-coded assumptions wherever practical.

---

## 7.10 Collection Loading

Each declared collection is loaded independently.

Conceptually:

```text
Collection File
        │
        ▼
Read File
        │
        ▼
Decode JSON
        │
        ▼
Parsed Collection
```

Each collection remains isolated during loading.

Cross-collection relationships are not interpreted during this stage.

For example:

```text
PrayerSequence
        │
        ▼
List of IDs
```

The loader preserves those identifiers without attempting to resolve them.

---

## 7.11 Loading Order

Some package elements have architectural dependencies that influence loading order.

A typical sequence is:

```text
Manifest

↓

Entry Points

↓

Occasions

↓

Prayers

↓

Prayer Sequences

↓

Liturgical Items

↓

Texts

↓

Qolos

↓

Melodies

↓

Media Assets

↓

Search Index
```

The exact physical order is an implementation detail provided that:

- deterministic behavior is preserved
- required dependencies are respected
- the parsed package representation remains equivalent

---

## 7.12 Parsing

Parsing converts physical package files into structured runtime models.

```text
JSON
        │
        ▼
Parser
        │
        ▼
Package Model
```

Parsing responsibilities include:

- decoding values
- creating typed models
- preserving declared ordering
- preserving canonical identifiers
- preserving declared relationships as identifiers

Parsing does not:

- resolve references
- validate semantic correctness
- compute derived values

---

## 7.13 Parsed Package Model

The output of Package Loading is the Parsed Package Model.

Conceptually:

```text
Parsed Package

├── Manifest
├── Entry Points
├── Occasions
├── Prayers
├── Prayer Sequences
├── Liturgical Items
├── Texts
├── Qolos
├── Melodies
├── Media Assets
└── Search Index
```

The Parsed Package Model represents the complete package in a decoded form.

It is not yet the Runtime Content Store.

---

## 7.14 Canonical Preservation

Package Loading shall preserve canonical package information exactly as represented by the package.

Specifically, it shall preserve:

- identifiers
- ordering
- declared relationships
- entity types
- metadata
- package structure

The Package Loader shall not:

- normalize identifiers
- reorder canonical collections
- replace missing values
- invent relationships
- modify editorial content

Canonical meaning remains unchanged throughout Package Loading.

---

## 7.15 Loading Isolation

Each package loading operation shall be isolated from previous runtime sessions.

For example:

```text
Package A Loading
```

shall not reuse parsed objects originating from:

```text
Package B
```

unless explicitly defined by a future caching architecture.

Isolation prevents stale runtime information from contaminating a newly loaded package.

---

## 7.16 Lazy and Eager Loading

The architecture permits both eager and lazy loading strategies.

### Eager Loading

```text
Read all required collections
        │
        ▼
Construct complete Parsed Package
```

### Lazy Loading

```text
Load Manifest
        │
        ▼
Load collection only when first required
```

Regardless of strategy:

- observable behavior shall remain identical
- canonical meaning shall remain unchanged
- runtime services shall not observe inconsistent loading state

The chosen strategy is an implementation decision.

---

## 7.17 Streaming Considerations

Future implementations may support streaming or incremental package loading.

For example:

```text
Large Package
        │
        ▼
Read Incrementally
        │
        ▼
Decode Sections
```

Streaming shall not change:

- canonical interpretation
- identifier preservation
- deterministic behavior
- validation requirements

The runtime architecture shall remain independent from the physical reading strategy.

---

## 7.18 Resource Discovery

In addition to canonical collections, the Package Loader discovers package resources.

Examples include:

```text
Images

Audio Files

Notation Files

Documents

Supplementary Resources
```

Discovery records resource declarations without requiring every resource to be opened immediately.

Actual resource resolution belongs to the Resource Resolver.

---

## 7.19 Loading Diagnostics

Throughout the loading process, the Package Loader shall emit structured diagnostics.

Diagnostic information may include:

- loading stage
- package identifier
- file name
- operation
- severity
- diagnostic code
- descriptive message

Diagnostics support:

- debugging
- automated testing
- runtime inspection
- failure analysis

---

## 7.20 Loading Errors

Package Loading may encounter failures such as:

```text
PackageNotFound

ManifestMissing

ManifestUnreadable

InvalidJson

CollectionMissing

CollectionUnreadable

ParserFailure

UnsupportedEncoding

UnexpectedPackageStructure
```

Loading errors prevent successful creation of a Parsed Package Model.

Errors shall be reported through the Core Engine Error Model.

---

## 7.21 Partial Loading

The Package Loader may temporarily hold partially loaded information internally.

However, partially loaded package data shall not be exposed through the Public API.

From the application's perspective, loading remains atomic.

Either:

```text
Parsed Package successfully produced
```

or:

```text
Loading failed
```

No intermediate package state shall become externally observable.

---

## 7.22 Deterministic Behavior

Given the same package and the same package format, Package Loading shall always produce an equivalent Parsed Package Model.

Observable behavior shall not depend on:

- filesystem ordering
- operating system
- platform implementation
- processor architecture
- thread scheduling

Deterministic loading ensures consistent runtime interpretation across all supported platforms.

---

## 7.23 Relationship to Validation

Package Loading and Package Validation are separate architectural stages.

```text
Package Loading
        │
        ▼
Can this package be read?
```

```text
Package Validation
        │
        ▼
Is this package structurally and semantically valid for runtime?
```

A successfully loaded package is not necessarily a valid package.

Likewise, validation assumes that loading has already completed successfully.

---

## 7.24 Relationship to Runtime Construction

After successful loading and validation, the Parsed Package Model becomes the input for runtime construction.

```text
Package Loading
        │
        ▼
Parsed Package Model
        │
        ▼
Package Validation
        │
        ▼
Runtime Construction
        │
        ▼
Runtime Content Store
```

This separation preserves a clear distinction between:

- physical package reading
- package correctness
- runtime representation

---

## 7.25 Package Loading Summary

Package Loading transforms a physical Application Package into a complete Parsed Package Model.

Its responsibilities are summarized below.

```text
Application Package
        │
        ▼
Package Source
        │
        ▼
Discover Package
        │
        ▼
Load Manifest
        │
        ▼
Discover Collections
        │
        ▼
Load Collections
        │
        ▼
Parse Package Files
        │
        ▼
Parsed Package Model
        │
        ▼
Package Validation
```

The Package Loader is responsible only for reading and decoding package content.

It does not determine canonical correctness, resolve references, construct runtime services, or interpret application behavior.

By maintaining this separation of responsibilities, Package Loading provides a deterministic, platform-independent, and implementation-neutral foundation for all subsequent Core Engine operations.

# 8. Package Validation

## 8.1 Purpose

This section defines the architectural model for validating an Application Package after it has been successfully loaded by the Package Loader.

The purpose of Package Validation is to determine whether the loaded package can be interpreted safely and consistently by the Core Engine.

Package Validation establishes runtime confidence before canonical content becomes available to runtime services.

Its responsibilities include:

- verifying package integrity
- validating runtime assumptions
- detecting structural inconsistencies
- identifying invalid references
- classifying validation issues
- preventing unsafe runtime interpretation

Package Validation does not repair package content.

It determines whether the package is acceptable for runtime use.

---

## 8.2 Architectural Role

Package Validation follows Package Loading and precedes Runtime Construction.

```text
Application Package
        │
        ▼
Package Loading
        │
        ▼
Parsed Package Model
        │
        ▼
Package Validation
        │
        ▼
Validated Package
        │
        ▼
Runtime Construction
```

Its architectural responsibility is to ensure that only coherent package data proceeds into the Runtime Content Store.

---

## 8.3 Validation Responsibilities

Package Validation is responsible for:

- validating package structure
- validating required collections
- validating identifiers
- validating references
- validating resource declarations
- validating collection consistency
- validating package assumptions required for runtime
- reporting structured validation diagnostics
- determining whether the package is usable

It shall not:

- modify package content
- repair invalid data
- reorder canonical collections
- infer missing entities
- substitute missing references
- perform editorial corrections
- regenerate derived data

Those responsibilities belong to Build Tools.

---

## 8.4 Validation Scope

Runtime validation focuses only on conditions that affect safe runtime behavior.

Typical validation scope includes:

```text
Package Structure

Canonical Collections

Identifiers

References

Resources

Derived Runtime Assumptions
```

It intentionally excludes editorial concerns such as:

- theological correctness
- liturgical correctness
- historical consistency
- authoring conventions
- stylistic recommendations

Those belong to the authoring process.

---

## 8.5 Validation Principles

Package Validation follows several architectural principles.

Validation shall be:

- deterministic
- non-destructive
- implementation-independent
- reproducible
- explicit
- observable

Validation shall never silently modify canonical package content.

The same package shall always produce the same validation result.

---

## 8.6 Validation Stages

Validation is performed in multiple stages.

A typical sequence is:

```text
Package Structure

↓

Schema Validation

↓

Collection Validation

↓

Identifier Validation

↓

Reference Validation

↓

Resource Validation

↓

Search Index Validation

↓

Validation Result
```

Each stage contributes additional information to the overall validation result.

---

## 8.7 Structural Validation

Structural Validation verifies that the package satisfies the required physical organization.

Typical checks include:

- required directories exist
- required collections exist
- required files exist
- package organization is recognizable
- required manifest declarations are available

Example:

```text
content/

indexes/

media/
```

Structural Validation does not inspect canonical relationships.

---

## 8.8 Schema Validation

Schema Validation verifies that package data conforms to the expected package schema.

Typical checks include:

- required properties
- supported property types
- required arrays
- supported object structures
- mandatory fields

For example:

```text
PrayerSequence

id

liturgicalItemIds
```

A malformed entity shall not proceed to runtime construction.

Schema Validation verifies structure rather than meaning.

---

## 8.9 Collection Validation

Collection Validation verifies each canonical collection independently.

Examples include:

```text
Entry Points

Occasions

Prayers

Prayer Sequences

Liturgical Items

Texts

Qolos

Melodies

Media Assets
```

Validation checks may include:

- collection exists
- collection readable
- entity count acceptable
- duplicate entities absent
- collection format correct

Cross-collection relationships belong to Reference Validation.

---

## 8.10 Identifier Validation

Identifiers are the foundation of canonical runtime references.

Validation shall verify:

- identifier exists
- identifier format is valid
- identifier uniqueness
- identifier stability assumptions
- identifier type consistency

Examples:

```text
TXT-001

QOL-002

MEL-004

PR-015
```

Duplicate identifiers shall be treated as fatal validation errors.

---

## 8.11 Reference Validation

Reference Validation verifies that canonical relationships are internally consistent.

Typical relationships include:

```text
Entry Point

↓

Occasion

↓

Prayer Sequence

↓

Liturgical Item

↓

Text

↓

Qolo

↓

Melody

↓

Media Asset
```

Validation verifies:

- referenced entity exists
- referenced type is correct
- required references are present
- reference direction is valid
- canonical relationship constraints are satisfied

Reference Validation shall not attempt automatic correction.

---

## 8.12 Ordered Collection Validation

Some collections depend upon canonical ordering.

Examples include:

```text
Prayer Sequence

Liturgical Items
```

Validation verifies:

- identifiers are valid
- ordering is preserved
- duplicate sequence entries are handled according to package rules
- sequence integrity is maintained

Validation shall not reorder canonical sequences.

---

## 8.13 Resource Validation

Resource Validation verifies declared package resources.

Typical resources include:

```text
Images

Audio

Notation

Documents

Other Media
```

Validation may verify:

- declared resource exists
- resource path format
- supported resource type
- declared resource identifier

Opening or rendering the resource is not required during validation.

---

## 8.14 Search Index Validation

The Search Index is derived package data.

Validation verifies that search entries remain consistent with canonical content.

Typical checks include:

- valid entity type
- valid entity identifier
- referenced entity exists
- duplicate search entries where prohibited
- supported index structure

The Core Engine does not regenerate the Search Index.

It only verifies that the supplied index remains usable.

---

## 8.15 Cross-Collection Consistency

Validation verifies that related collections remain mutually consistent.

Examples include:

```text
Prayer Sequence

↓

Liturgical Item IDs

↓

Existing Liturgical Items
```

Likewise:

```text
Melody

↓

Media Asset IDs

↓

Existing Media Assets
```

Cross-collection consistency ensures that independent collections form one coherent package.

---

## 8.16 Validation Severity Model

Every validation issue shall be assigned a severity.

The architectural severity levels are:

```text
Fatal

Recoverable

Warning

Information
```

### Fatal

The package cannot safely enter runtime.

### Recoverable

A limited capability is unavailable, but the package remains usable.

### Warning

The package is usable, but attention is recommended.

### Information

Diagnostic information only.

Severity classification shall be deterministic.

---

## 8.17 Fatal Validation Conditions

Typical fatal conditions include:

- duplicate canonical identifiers
- missing required collection
- malformed required entity
- unresolved required reference
- unsupported required entity type
- corrupted canonical structure
- invalid required package declaration

Fatal validation terminates initialization.

```text
Validation

↓

Fatal Issue

↓

Failed
```

---

## 8.18 Recoverable Validation Conditions

Recoverable conditions affect only optional functionality.

Examples include:

- optional notation missing
- optional media unavailable
- optional localized resource absent
- unused optional entity

The engine shall:

- report the issue
- preserve canonical interpretation
- continue where safe

Graceful degradation shall not alter canonical meaning.

---

## 8.19 Warning Conditions

Warnings indicate package conditions that deserve attention but do not prevent normal runtime.

Examples may include:

- unused optional entities
- optional metadata omissions
- recommended fields absent
- deprecated package declarations

Warnings should assist package authors without changing runtime behavior.

---

## 8.20 Validation Result Model

Validation produces a structured result.

Conceptually:

```text
PackageValidationResult

├── isValid
├── severity
├── fatalIssues
├── recoverableIssues
├── warnings
├── information
└── diagnostics
```

Applications and diagnostic tools should be able to inspect validation outcomes programmatically.

---

## 8.21 Validation Diagnostics

Every validation stage may emit diagnostics.

Diagnostic entries may contain:

- validation stage
- package identifier
- entity identifier
- resource identifier
- severity
- validation code
- descriptive message

Diagnostics support:

- testing
- debugging
- package inspection
- quality assurance

Validation diagnostics shall not replace the Validation Result.

---

## 8.22 Validation Errors

Validation failures are represented through the Core Engine Error Model.

Typical errors include:

```text
ValidationError

MissingCollection

DuplicateIdentifier

InvalidReference

InvalidEntityType

InvalidSchema

ResourceDeclarationError

SearchIndexError
```

The Package Validator shall not expose parser exceptions directly to applications.

---

## 8.23 Validation Atomicity

Validation is architecturally atomic.

Applications observe only:

```text
Validation succeeded
```

or

```text
Validation failed
```

Internally, multiple validation stages may execute.

Externally, the package becomes either:

```text
Validated
```

or

```text
Rejected
```

No partially validated package shall become available through the Public API.

---

## 8.24 Deterministic Validation

The same package shall always produce the same validation outcome.

Validation shall not depend upon:

- platform
- operating system
- filesystem ordering
- thread scheduling
- processor architecture

Deterministic validation is essential for reproducible runtime behavior.

---

## 8.25 Relationship to Runtime Construction

Only a successfully validated package may enter Runtime Construction.

```text
Parsed Package

↓

Package Validation

↓

Validated Package

↓

Runtime Construction
```

Runtime Construction assumes that:

- required collections exist
- identifiers are unique
- required references are valid
- runtime assumptions are satisfied

It shall not repeat the complete validation process.

---

## 8.26 Validation Ownership

Validation responsibilities are distributed across the platform architecture.

```text
Author Database
        │
Editorial correctness

↓

Build Tools
        │
Comprehensive package validation

↓

Core Engine
        │
Runtime safety validation
```

Each layer validates the package for its own architectural purpose.

The Core Engine validates only what is necessary for safe runtime execution.

---

## 8.27 Validation Extensibility

Future package versions may introduce additional validation rules.

New validation capabilities shall:

- preserve backward compatibility where possible
- classify issues consistently
- integrate with the existing severity model
- avoid changing canonical meaning

Validation rules should evolve without breaking established architectural contracts.

---

## 8.28 Package Validation Summary

Package Validation determines whether a loaded package is safe for runtime interpretation.

Its workflow is summarized below.

```text
Parsed Package Model
        │
        ▼
Structural Validation
        │
        ▼
Schema Validation
        │
        ▼
Collection Validation
        │
        ▼
Identifier Validation
        │
        ▼
Reference Validation
        │
        ▼
Resource Validation
        │
        ▼
Search Index Validation
        │
        ▼
Validation Result
        │
        ├──► Validated Package
        │
        └──► Validation Failure
```

Package Validation does not modify canonical content.

It verifies that the package satisfies the runtime requirements of the Core Engine and provides a deterministic, observable, and platform-independent decision regarding package usability.

Only packages that successfully complete this stage may proceed to Runtime Construction.

# 9. Runtime Content Store

## 9.1 Purpose

This section defines the architectural model of the Runtime Content Store.

The Runtime Content Store is the Core Engine's authoritative in-memory representation of canonical package content.

It is created after successful Package Loading and Package Validation and serves as the foundation for all runtime services.

Its primary responsibilities are:

- storing canonical entities
- providing efficient access to runtime content
- preserving canonical identity
- preserving canonical ordering
- supporting runtime services
- isolating applications from package storage details

The Runtime Content Store does not represent application state.

It represents immutable canonical content that has been accepted by the Core Engine.

---

## 9.2 Architectural Role

The Runtime Content Store separates package interpretation from runtime behavior.

```text
Application Package
        │
        ▼
Package Loading
        │
        ▼
Package Validation
        │
        ▼
Runtime Content Store
        │
        ▼
Runtime Services
```

Every runtime service operates on the Runtime Content Store rather than directly reading package files.

The Runtime Content Store therefore becomes the single source of canonical runtime content.

---

## 9.3 Design Principles

The Runtime Content Store follows several architectural principles.

It shall be:

- immutable
- deterministic
- identifier-based
- read-only
- platform-independent
- implementation-neutral
- optimized for lookup
- independent from application state

The store shall preserve canonical meaning exactly as defined by the Application Package.

---

## 9.4 Canonical Runtime Representation

The Runtime Content Store contains decoded canonical entities.

For example:

```text
Application Package

TXT-001

QOL-001

MEL-001
```

becomes

```text
Runtime Content Store

Text

Qolo

Melody
```

The transformation changes only the runtime representation.

Canonical identity remains unchanged.

---

## 9.5 Store Organization

The Runtime Content Store is composed of specialized entity stores.

Conceptually:

```text
Runtime Content Store

├── Entry Point Store
├── Occasion Store
├── Prayer Store
├── Prayer Sequence Store
├── Liturgical Item Store
├── Text Store
├── Qolo Store
├── Melody Store
├── Media Asset Store
└── Search Index Store
```

Each store owns one canonical entity type.

The Runtime Content Store coordinates these stores into one coherent runtime model.

---

## 9.6 Entity Stores

Each Entity Store manages a single canonical entity type.

For example:

```text
Text Store

TXT-001

TXT-002

TXT-003
```

or

```text
Melody Store

MEL-001

MEL-002

MEL-003
```

Entity Stores shall not own cross-entity interpretation.

They provide storage and retrieval only.

---

## 9.7 Identifier-Based Access

Canonical identifiers are the primary access mechanism.

For example:

```text
TXT-001
```

retrieves

```text
Text
```

Likewise:

```text
QOL-002
```

retrieves

```text
Qolo
```

The Runtime Content Store shall not depend upon:

- array position
- loading order
- object identity
- memory address

Canonical identifiers remain authoritative.

---

## 9.8 Ordered Collections

Some canonical collections possess defined ordering.

Examples include:

- Prayer Sequences
- Liturgical Items
- Entry Point ordering where declared

The Runtime Content Store shall preserve this ordering.

For example:

```text
Prayer Sequence

LI-001

LI-002

LI-003
```

shall remain exactly as declared by the package.

The store shall not reorder canonical sequences.

---

## 9.9 Read-Only Model

The Runtime Content Store exposes canonical content through read-only access.

Applications and runtime services may retrieve entities but shall not modify them.

Conceptually:

```text
Read

✓ Allowed
```

```text
Modify

✗ Not Allowed
```

If derived runtime information is required, it belongs outside the Runtime Content Store.

---

## 9.10 Derived Runtime Structures

To improve runtime performance, the store may construct derived structures.

Examples include:

```text
Identifier Maps

Reverse Lookup Maps

Ordered Indexes

Entity Type Indexes

Search Maps
```

Derived structures shall:

- originate from canonical content
- remain internally consistent
- never replace canonical entities

If a derived structure is discarded and rebuilt, the resulting canonical interpretation shall remain identical.

---

## 9.11 Cross-Entity Relationships

The Runtime Content Store preserves declared relationships but does not interpret them.

For example:

```text
Prayer Sequence

↓

Liturgical Item IDs
```

or

```text
Melody

↓

Media Asset IDs
```

Relationship resolution belongs to the Reference Resolver.

The store merely preserves canonical data.

---

## 9.12 Runtime Indexes

The Runtime Content Store may maintain internal indexes to improve lookup efficiency.

Examples include:

```text
Identifier Index

Entity-Type Index

Media-Type Index

Search Entry Index
```

Indexes exist solely to optimize runtime behavior.

They shall not become authoritative sources of canonical meaning.

---

## 9.13 Store Lifetime

The Runtime Content Store exists for the lifetime of a runtime session.

Its lifecycle is:

```text
Runtime Construction

↓

Runtime Content Store Created

↓

Runtime Session

↓

Shutdown

↓

Store Released
```

A new runtime session creates a new Runtime Content Store.

The store shall not survive beyond the session that owns it.

---

## 9.14 Memory Ownership

The Runtime Content Store owns the canonical runtime representation.

Applications do not own store entities.

Likewise:

- Navigation Service
- Search Service
- Reference Resolver
- Media Resolver

consume store content without becoming its owner.

Ownership remains centralized.

---

## 9.15 Immutability

Canonical entities become immutable after successful construction.

For example:

```text
Text

Qolo

Melody

Prayer

Occasion
```

shall not be modified during the runtime session.

Application-specific state belongs elsewhere.

Immutability guarantees deterministic runtime behavior.

---

## 9.16 Lookup Performance

The Runtime Content Store should optimize common lookup operations.

Typical operations include:

- entity by identifier
- ordered sequence retrieval
- collection iteration
- entity existence checks

Lookup performance shall not change observable runtime behavior.

Optimizations remain implementation details.

---

## 9.17 Store Consistency

The Runtime Content Store shall remain internally consistent throughout the runtime session.

Examples include:

- unique identifiers
- valid entity ownership
- preserved ordering
- stable canonical identity

Runtime services shall not leave the store in a partially modified state because the store itself is immutable.

---

## 9.18 Store Isolation

Each runtime session owns its own Runtime Content Store.

For example:

```text
Session A

↓

Store A
```

and

```text
Session B

↓

Store B
```

The two stores remain independent.

Canonical entities from one session shall not appear in another unless explicitly supported by a future multi-package architecture.

---

## 9.19 Runtime Construction

The Runtime Content Store is constructed from a successfully validated package.

The high-level flow is:

```text
Validated Package

↓

Decode Runtime Entities

↓

Construct Entity Stores

↓

Build Runtime Indexes

↓

Freeze Store

↓

Runtime Content Store Ready
```

After construction completes, runtime services may begin consuming canonical content.

---

## 9.20 Relationship to Runtime Context

The Runtime Content Store and Runtime Context represent different architectural concepts.

The Runtime Content Store answers:

```text
What content exists?
```

The Runtime Context answers:

```text
What content is currently active?
```

For example:

```text
Runtime Content Store

contains

TXT-001

TXT-002

TXT-003
```

while

```text
Runtime Context

Current Text

TXT-002
```

Changing the Runtime Context does not modify the Runtime Content Store.

---

## 9.21 Relationship to Reference Resolver

The Reference Resolver depends upon the Runtime Content Store.

Conceptually:

```text
Reference Resolver

↓

Runtime Content Store

↓

Canonical Entity
```

The Runtime Content Store does not resolve references itself.

Its responsibility ends with providing efficient access to canonical entities.

---

## 9.22 Diagnostics

The Runtime Content Store may provide diagnostic information regarding:

- construction
- entity counts
- index creation
- consistency checks
- memory statistics where supported

Diagnostics remain informational.

They do not alter canonical runtime behavior.

---

## 9.23 Extensibility

Future package versions may introduce additional canonical entity types.

The Runtime Content Store shall accommodate such extensions without changing the architectural model.

New entity stores shall:

- preserve identifier-based access
- preserve immutability
- integrate with existing lookup mechanisms
- remain compatible with runtime services

The architecture favors extension through additional entity stores rather than modification of existing canonical behavior.

---

## 9.24 Runtime Content Store Summary

The Runtime Content Store is the Core Engine's authoritative repository of canonical runtime content.

Its lifecycle is summarized below.

```text
Validated Package
        │
        ▼
Runtime Construction
        │
        ▼
Runtime Content Store
        │
        ├── Entry Point Store
        ├── Occasion Store
        ├── Prayer Store
        ├── Prayer Sequence Store
        ├── Liturgical Item Store
        ├── Text Store
        ├── Qolo Store
        ├── Melody Store
        ├── Media Asset Store
        └── Search Index Store
        │
        ▼
Runtime Services
```

The Runtime Content Store preserves canonical content exactly as defined by the Application Package.

It provides a deterministic, immutable, identifier-based, and platform-independent foundation for every runtime service within the Core Engine.

By separating canonical content from runtime state, it enables predictable behavior, efficient lookup, and long-term architectural stability across all SyriacPlatform applications.

# 10. Reference Resolution

## 10.1 Purpose

This section defines the architectural model for resolving canonical references within the Runtime Content Store.

Reference Resolution transforms canonical identifiers into the corresponding runtime entities, allowing runtime services to navigate relationships without depending on package-specific storage details.

Its responsibilities include:

- resolving canonical identifiers
- locating referenced entities
- preserving canonical relationships
- providing consistent entity access
- reporting resolution failures
- supporting runtime navigation

Reference Resolution does not modify canonical content.

It provides a deterministic mechanism for interpreting relationships already defined by the Application Package.

---

## 10.2 Architectural Role

Reference Resolution is a core runtime service that operates on the Runtime Content Store.

Its position within the runtime architecture is:

```text
Runtime Content Store
        │
        ▼
Reference Resolver
        │
        ▼
Resolved Runtime Entities
        │
        ▼
Runtime Services
```

Every runtime service that traverses relationships relies on the Reference Resolver rather than directly interpreting identifiers.

---

## 10.3 Design Principles

Reference Resolution shall be:

- deterministic
- identifier-based
- read-only
- stateless
- platform-independent
- implementation-neutral
- observable

Reference Resolution shall never modify canonical entities.

The same identifier shall always resolve to the same runtime entity within the same runtime session.

---

## 10.4 Canonical References

Canonical relationships are represented using canonical identifiers.

Examples include:

```text
Prayer Sequence

↓

LI-001

LI-002

LI-003
```

or

```text
Liturgical Item

↓

textId = TXT-001

effectiveMelodyId = MEL-001
```

The Runtime Content Store preserves these identifiers.

Reference Resolution transforms them into runtime entities.

---

## 10.5 Reference Types

The Core Engine supports references between canonical entity types.

Examples include:

```text
Entry Point

↓

Occasion
```

```text
Occasion

↓

Prayer
```

```text
Prayer

↓

Prayer Sequence
```

```text
Prayer Sequence

↓

Liturgical Item
```

```text
Liturgical Item

↓

Text
```

```text
Liturgical Item

↓

Melody
```

```text
Melody

↓

Media Asset
```

The Reference Resolver treats every relationship consistently regardless of entity type.

---

## 10.6 Resolution Process

Reference Resolution follows a predictable sequence.

```text
Identifier

↓

Locate Entity Store

↓

Lookup Entity

↓

Return Runtime Entity
```

If the identifier cannot be resolved, the appropriate resolution result is produced.

---

## 10.7 Entity Lookup

Entity lookup is performed using canonical identifiers.

For example:

```text
TXT-001
```

becomes

```text
Text
```

Likewise:

```text
MEL-002
```

becomes

```text
Melody
```

Lookup operations shall not depend upon:

- collection ordering
- memory address
- object identity
- loading sequence

Canonical identifiers remain authoritative.

---

## 10.8 Cross-Entity Navigation

Many runtime operations require traversing multiple entity types.

For example:

```text
Entry Point

↓

Occasion

↓

Prayer

↓

Prayer Sequence

↓

Liturgical Item

↓

Text
```

Each step is resolved independently through the Reference Resolver.

The Runtime Content Store supplies the entities.

The Reference Resolver supplies the relationships.

---

## 10.9 Resolution Ownership

Reference Resolution owns relationship interpretation.

The Runtime Content Store owns canonical content.

For example:

```text
Runtime Content Store

contains

TXT-001
```

The Reference Resolver determines:

```text
TXT-001

↓

Text
```

This separation keeps storage independent from navigation logic.

---

## 10.10 Required References

Some canonical relationships are mandatory.

Examples include:

- Prayer → Prayer Sequence
- Liturgical Item → Text
- Entry Point → Occasion

Failure to resolve a required reference indicates a package inconsistency.

Such failures should normally have been detected during Package Validation.

If encountered during runtime, they shall be reported through the Core Engine Error Model.

---

## 10.11 Optional References

Some relationships are optional.

Examples may include:

- optional melody
- optional notation
- optional media asset
- optional localized resource

When an optional reference is absent, the Reference Resolver shall indicate that no referenced entity exists.

The absence of an optional reference shall not be treated as a runtime failure.

---

## 10.12 Missing References

If a required identifier cannot be resolved during runtime, the Reference Resolver shall report a structured resolution failure.

Conceptually:

```text
Identifier

↓

No Matching Entity

↓

Resolution Failure
```

The resolver shall not invent replacement entities.

---

## 10.13 Circular References

Canonical package design should avoid circular references.

If a circular relationship is encountered, the Reference Resolver shall avoid entering an infinite resolution cycle.

Possible outcomes include:

- reporting a resolution failure
- terminating the current resolution
- returning a structured diagnostic

Handling of circular references shall remain deterministic.

---

## 10.14 Resolution Result Model

Every resolution produces a structured result.

Conceptually:

```text
Resolution Result

├── Success
├── Referenced Entity
├── Failure
└── Diagnostics
```

Applications should not interpret raw identifiers directly.

They consume resolved runtime entities.

---

## 10.15 Resolution Errors

Resolution failures are represented using the Core Engine Error Model.

Typical errors include:

```text
EntityNotFound

InvalidReference

UnexpectedEntityType

CircularReference

ResolutionFailure
```

Errors shall contain sufficient diagnostic information for debugging and testing.

---

## 10.16 Resolution Performance

Reference Resolution is expected to support frequent runtime access.

Typical operations include:

- identifier lookup
- sequence traversal
- navigation between entities
- media lookup

Performance optimizations shall not change observable behavior.

---

## 10.17 Resolution Caching

Implementations may cache previously resolved references.

For example:

```text
TXT-001

↓

Cached Text
```

Caching is an implementation optimization.

It shall not:

- change canonical meaning
- expose mutable entities
- affect deterministic behavior

The architecture does not require caching.

---

## 10.18 Relationship to Runtime Content Store

The Runtime Content Store owns canonical runtime entities.

The Reference Resolver provides access to those entities through canonical relationships.

Conceptually:

```text
Runtime Content Store

↓

Canonical Entity
```

```text
Reference Resolver

↓

Relationship Interpretation
```

Neither component replaces the other.

They operate together.

---

## 10.19 Relationship to Navigation Service

The Navigation Service depends upon the Reference Resolver.

For example:

```text
Current Prayer

↓

Prayer Sequence

↓

Liturgical Items

↓

Text
```

The Navigation Service determines *where* to navigate.

The Reference Resolver determines *what each identifier represents*.

---

## 10.20 Relationship to Search Service

Search results contain canonical references.

For example:

```text
Search Result

↓

TXT-001
```

The Search Service requests the Reference Resolver to obtain:

```text
Text
```

Search remains independent from entity storage.

---

## 10.21 Diagnostics

The Reference Resolver may emit diagnostics including:

- requested identifier
- entity type
- lookup stage
- resolution outcome
- diagnostic code
- descriptive message

Diagnostics support:

- debugging
- automated testing
- runtime inspection

Diagnostics shall not alter runtime behavior.

---

## 10.22 Extensibility

Future package versions may introduce additional canonical entity types.

The Reference Resolver shall support new entity relationships without changing the architectural model.

Extensions shall:

- preserve identifier-based resolution
- preserve deterministic behavior
- integrate with existing entity stores
- remain compatible with existing runtime services

The architecture favors extensibility through additional entity types rather than changes to the resolution mechanism.

---

## 10.23 Reference Resolution Summary

Reference Resolution transforms canonical identifiers into runtime entities.

Its overall workflow is summarized below.

```text
Canonical Identifier
        │
        ▼
Reference Resolver
        │
        ▼
Locate Entity Store
        │
        ▼
Resolve Entity
        │
        ▼
Runtime Entity
        │
        ▼
Navigation
Search
Media
Localization
Other Runtime Services
```

The Reference Resolver does not own canonical content and does not modify runtime entities.

Its responsibility is to provide deterministic, efficient, and platform-independent interpretation of canonical relationships, enabling every runtime service to operate on resolved entities rather than raw identifiers.

By separating relationship resolution from storage and application logic, the Core Engine maintains a clean architectural boundary between canonical content, runtime interpretation, and application behavior.

# 11. Navigation Service

## 11.1 Purpose

This section defines the architectural model of the Navigation Service.

The Navigation Service provides a consistent mechanism for moving through canonical content during runtime.

Rather than exposing low-level entity relationships, it presents navigation operations that allow applications to traverse the package in a predictable and implementation-independent manner.

Its responsibilities include:

- navigating canonical content
- maintaining navigation consistency
- resolving navigation targets
- preserving canonical ordering
- supporting sequential and hierarchical navigation
- coordinating with the Runtime Context

The Navigation Service does not own canonical content.

It provides controlled movement through content already represented by the Runtime Content Store.

---

## 11.2 Architectural Role

The Navigation Service operates above the Reference Resolver.

Its position within the runtime architecture is:

```text
Runtime Content Store
        │
        ▼
Reference Resolver
        │
        ▼
Navigation Service
        │
        ▼
Application
```

Applications interact with navigation operations rather than manually resolving identifiers.

---

## 11.3 Design Principles

The Navigation Service shall be:

- deterministic
- state-aware
- read-only with respect to canonical content
- platform-independent
- implementation-neutral
- predictable
- observable

Navigation operations shall never modify canonical entities.

---

## 11.4 Navigation Model

Navigation represents movement between canonical runtime entities.

Conceptually:

```text
Current Entity

↓

Navigation Operation

↓

Next Entity
```

Navigation always follows relationships already defined by the Application Package.

It never invents additional paths.

---

## 11.5 Navigation Scope

The Navigation Service supports navigation across the canonical hierarchy.

Typical navigation includes:

```text
Entry Point

↓

Occasion

↓

Prayer

↓

Prayer Sequence

↓

Liturgical Item

↓

Text
```

Navigation may also include movement toward related entities such as:

```text
Liturgical Item

↓

Melody

↓

Media Asset
```

The supported navigation paths are determined by canonical package relationships.

---

## 11.6 Navigation Context

Every navigation operation executes within the current Runtime Context.

For example:

```text
Current Occasion

Current Prayer

Current Liturgical Item
```

Navigation decisions are based upon this active context.

Changing the Runtime Context changes subsequent navigation behavior.

The Navigation Service itself does not own the Runtime Context.

---

## 11.7 Navigation Operations

Typical navigation operations include:

```text
Open

Next

Previous

Parent

Child

First

Last

Return
```

The exact public API may evolve.

The architectural model remains independent from API naming.

---

## 11.8 Sequential Navigation

Sequential navigation follows canonical ordering.

For example:

```text
LI-001

↓

LI-002

↓

LI-003
```

The Navigation Service shall preserve package-defined ordering.

It shall not reorder canonical sequences.

---

## 11.9 Hierarchical Navigation

Hierarchical navigation moves between different levels of the canonical model.

For example:

```text
Occasion

↓

Prayer

↓

Prayer Sequence

↓

Liturgical Item
```

Likewise:

```text
Liturgical Item

↑

Prayer Sequence
```

Hierarchical navigation follows canonical ownership relationships.

---

## 11.10 Entry Point Navigation

Navigation normally begins at an Entry Point.

Conceptually:

```text
Entry Point

↓

Occasion

↓

Prayer

↓

Prayer Sequence
```

Entry Points define the initial navigation position for an application.

Applications should not require knowledge of internal package organization before navigation begins.

---

## 11.11 Navigation State

Navigation is state-dependent.

A navigation operation always begins from a current position.

For example:

```text
Current Liturgical Item

↓

Next

↓

Next Liturgical Item
```

The current navigation position is maintained by the Runtime Context.

The Navigation Service consumes that state.

---

## 11.12 Navigation Results

Every successful navigation operation produces a new navigation target.

Conceptually:

```text
Current Entity

↓

Navigate

↓

Target Entity
```

The Navigation Service may also return supplementary information describing the completed navigation.

---

## 11.13 Navigation Boundaries

Canonical navigation has natural boundaries.

Examples include:

```text
First Item
```

or

```text
Last Prayer
```

When a navigation boundary is reached, the Navigation Service shall behave deterministically.

It shall not move beyond the canonical limits of the package.

---

## 11.14 Invalid Navigation

Some requested navigation operations may not be valid.

Examples include:

- moving to a parent that does not exist
- moving beyond the last entity
- navigating outside the current hierarchy
- requesting an unavailable child

Invalid navigation shall not modify the Runtime Context.

The Navigation Service shall return an appropriate navigation result or error.

---

## 11.15 Navigation Result Model

Navigation operations produce structured results.

Conceptually:

```text
Navigation Result

├── Success
├── Current Entity
├── Target Entity
├── Navigation State
└── Diagnostics
```

Applications should rely upon navigation results rather than directly manipulating canonical relationships.

---

## 11.16 Navigation Errors

Typical navigation errors include:

```text
NavigationBoundaryReached

NavigationTargetUnavailable

InvalidNavigationOperation

NavigationContextUnavailable

NavigationFailure
```

Navigation errors shall integrate with the Core Engine Error Model.

---

## 11.17 Navigation Performance

Navigation operations are expected to execute frequently.

Typical operations include:

- opening prayers
- moving between liturgical items
- traversing sequences
- returning to parent entities

Performance optimizations shall remain implementation details.

Observable behavior shall remain unchanged.

---

## 11.18 Relationship to Runtime Context

The Runtime Context owns the current navigation position.

The Navigation Service reads and updates that position through defined runtime operations.

Conceptually:

```text
Runtime Context

↓

Current Position

↓

Navigation Service

↓

Updated Position
```

Canonical content remains unchanged.

Only the active runtime position changes.

---

## 11.19 Relationship to Reference Resolver

The Navigation Service depends upon the Reference Resolver.

For example:

```text
Current Identifier

↓

Reference Resolver

↓

Runtime Entity

↓

Navigation Decision
```

Navigation never resolves identifiers directly.

It delegates entity resolution to the Reference Resolver.

---

## 11.20 Relationship to Search Service

Navigation and Search are complementary runtime services.

Search answers:

```text
Where is the requested content?
```

Navigation answers:

```text
How do I move to that content?
```

A Search result may become the starting point for subsequent navigation.

---

## 11.21 Diagnostics

The Navigation Service may emit diagnostics including:

- current navigation position
- requested operation
- target entity
- navigation outcome
- diagnostic code
- descriptive message

Diagnostics support:

- debugging
- runtime inspection
- automated testing

Diagnostics shall not alter navigation behavior.

---

## 11.22 Extensibility

Future package versions may introduce additional navigation paths.

The Navigation Service shall accommodate new canonical relationships without changing the architectural model.

Extensions shall:

- preserve deterministic behavior
- preserve canonical ordering
- integrate with the Runtime Context
- remain compatible with existing runtime services

The navigation architecture favors extension through additional navigation paths rather than modification of existing behavior.

---

## 11.23 Navigation Service Summary

The Navigation Service provides deterministic movement through canonical runtime content.

Its overall workflow is summarized below.

```text
Runtime Context
        │
        ▼
Current Position
        │
        ▼
Navigation Service
        │
        ▼
Reference Resolver
        │
        ▼
Runtime Content Store
        │
        ▼
Resolved Target Entity
        │
        ▼
Updated Runtime Context
        │
        ▼
Application
```

The Navigation Service does not own canonical content, nor does it interpret package structure beyond established canonical relationships.

Its responsibility is to provide predictable, state-aware, and platform-independent navigation, allowing applications to traverse the Application Package through high-level navigation operations instead of low-level identifier management.

By separating navigation from storage, reference resolution, and application logic, the Core Engine maintains a clear architectural model that is extensible, deterministic, and consistent across all supported SyriacPlatform applications.

# 12. Search Service

## 12.1 Purpose

This section defines the architectural model of the Search Service.

The Search Service provides a consistent mechanism for locating canonical content based on user queries rather than navigation position.

It enables applications to discover entities throughout the Application Package without requiring prior knowledge of the package hierarchy.

Its responsibilities include:

- processing search queries
- locating matching search entries
- resolving search results
- preserving deterministic search behavior
- supporting efficient content discovery

The Search Service does not interpret editorial meaning.

It provides runtime access to content that has already been indexed within the Application Package.

---

## 12.2 Architectural Role

The Search Service operates as a runtime service above the Runtime Content Store.

Its position within the runtime architecture is:

```text
Runtime Content Store
        │
        ▼
Search Index
        │
        ▼
Search Service
        │
        ▼
Reference Resolver
        │
        ▼
Application
```

Applications interact with search operations rather than directly inspecting package collections.

---

## 12.3 Design Principles

The Search Service shall be:

- deterministic
- query-driven
- read-only
- platform-independent
- implementation-neutral
- observable
- efficient

Search operations shall never modify canonical content.

The same query executed against the same package shall always produce equivalent results.

---

## 12.4 Search Model

The Search Service processes a search query and produces a collection of matching results.

Conceptually:

```text
Search Query

↓

Search Service

↓

Matching Search Entries

↓

Resolved Runtime Entities

↓

Search Results
```

Search operations are independent from the user's current navigation position.

---

## 12.5 Search Scope

The Search Service may search any canonical entity type represented within the Search Index.

Typical searchable entities include:

```text
Texts

Qolos

Melodies

Occasions

Prayers

Liturgical Items
```

The supported search scope is determined by the package definition.

Applications should not assume that every entity type is searchable.

---

## 12.6 Search Sources

The Search Service operates on the package's Search Index.

Conceptually:

```text
Application Package

↓

Search Index

↓

Search Service
```

The Runtime Content Store remains the authoritative repository of runtime entities.

The Search Index serves as the searchable representation.

---

## 12.7 Search Queries

A search operation begins with a query.

Examples include:

```text
Word

Phrase

Identifier

Canonical Name

Localized Text
```

The internal representation of queries is implementation-independent.

Applications shall not depend upon package-specific indexing mechanisms.

---

## 12.8 Search Process

A typical search follows this sequence.

```text
Search Query

↓

Search Index Lookup

↓

Matching Entries

↓

Reference Resolution

↓

Runtime Entities

↓

Search Results
```

Each stage shall preserve deterministic behavior.

---

## 12.9 Search Results

A search operation produces a collection of results.

Conceptually:

```text
Search Results

├── Result 1

├── Result 2

├── Result 3
```

Each result represents one canonical entity.

Applications should consume search results rather than raw search index entries.

---

## 12.10 Result Ordering

Search results shall follow a deterministic ordering.

The ordering strategy may depend upon:

- package-defined ranking
- canonical ordering
- relevance rules defined by the package
- implementation-independent search policies

The same query against the same package shall produce the same result ordering.

---

## 12.11 Search Filters

Search operations may support filtering.

Examples include:

```text
Entity Type

Language

Occasion

Prayer

Media Availability
```

Filtering narrows the result set.

Filters shall never modify canonical content.

---

## 12.12 Search Context

Search operations are generally independent of the current Runtime Context.

However, future implementations may optionally use contextual information to improve user experience.

Examples include:

- current language
- active application profile
- active package capabilities

Context-aware search shall never change canonical search correctness.

---

## 12.13 Empty Results

A search query may produce no matching results.

Conceptually:

```text
Search Query

↓

No Matches

↓

Empty Result Set
```

An empty result set is a valid search outcome.

It shall not be treated as a runtime failure.

---

## 12.14 Search Errors

Search failures are represented using the Core Engine Error Model.

Typical errors include:

```text
SearchUnavailable

InvalidQuery

SearchIndexUnavailable

SearchFailure
```

Errors shall distinguish operational failures from valid empty results.

---

## 12.15 Search Performance

Search operations are expected to execute frequently.

Typical operations include:

- full-text search
- identifier lookup
- localized search
- repeated incremental queries

Performance optimizations remain implementation details.

Observable search behavior shall remain unchanged.

---

## 12.16 Relationship to Runtime Content Store

The Runtime Content Store owns canonical runtime entities.

The Search Service does not search entity collections directly.

Instead, it searches the Search Index and obtains runtime entities through the Reference Resolver.

Conceptually:

```text
Search Index

↓

Reference Resolver

↓

Runtime Content Store

↓

Runtime Entity
```

This separation preserves architectural independence.

---

## 12.17 Relationship to Search Index

The Search Index is supplied by the Application Package.

The Search Service consumes the Search Index.

The Search Service shall not:

- regenerate the index
- modify the index
- reorder canonical index entries

Index generation belongs to Build Tools.

---

## 12.18 Relationship to Reference Resolver

Search results contain canonical identifiers.

The Reference Resolver transforms those identifiers into runtime entities.

Conceptually:

```text
Search Entry

↓

Entity Identifier

↓

Reference Resolver

↓

Runtime Entity
```

The Search Service does not resolve identifiers directly.

---

## 12.19 Relationship to Navigation Service

Search and Navigation provide complementary capabilities.

Search answers:

```text
What matches the query?
```

Navigation answers:

```text
How does the application move to that entity?
```

Applications may select a search result and continue navigation from that location.

The Navigation Service remains responsible for subsequent movement through canonical content.

---

## 12.20 Diagnostics

The Search Service may emit diagnostics including:

- search query
- search duration
- number of matches
- applied filters
- diagnostic code
- descriptive message

Diagnostics support:

- debugging
- performance analysis
- runtime inspection
- automated testing

Diagnostics shall not alter search behavior.

---

## 12.21 Extensibility

Future package versions may introduce additional searchable entity types or indexing capabilities.

The Search Service shall accommodate such extensions without changing the architectural model.

Extensions shall:

- preserve deterministic behavior
- preserve compatibility with the Search Index
- integrate with the Reference Resolver
- remain independent from application-specific behavior

The architecture favors extensibility through new searchable entities and indexing strategies rather than changes to the search model itself.

---

## 12.22 Search Service Summary

The Search Service provides deterministic discovery of canonical runtime content based on user queries.

Its overall workflow is summarized below.

```text
Search Query
        │
        ▼
Search Service
        │
        ▼
Search Index
        │
        ▼
Matching Search Entries
        │
        ▼
Reference Resolver
        │
        ▼
Runtime Content Store
        │
        ▼
Resolved Runtime Entities
        │
        ▼
Search Results
        │
        ▼
Application
```

The Search Service does not own canonical content, does not generate search indexes, and does not modify runtime entities.

Its responsibility is to locate canonical content efficiently and deterministically, using the Search Index as the source of searchable information and the Reference Resolver to obtain runtime entities.

By separating searching from navigation, storage, and package construction, the Core Engine maintains a clear and extensible architecture that provides consistent content discovery across all SyriacPlatform applications.

# 13. Resource and Media Resolution

## 13.1 Purpose

This section defines the architectural model of the Resource and Media Resolution service.

The Resource and Media Resolution service provides a consistent mechanism for locating and accessing physical resources associated with canonical runtime entities.

Its primary purpose is to transform a Media Asset entity into an accessible runtime resource without exposing storage-specific implementation details to applications.

Its responsibilities include:

- resolving media assets
- locating physical resources
- verifying resource availability
- providing runtime resource access
- supporting multiple resource types
- reporting resource access failures

The service does not own canonical content.

It provides runtime access to resources that are already defined by the Application Package.

---

## 13.2 Architectural Role

The Resource and Media Resolution service operates after canonical entity resolution.

Its position within the runtime architecture is:

```text
Runtime Content Store
        │
        ▼
Reference Resolver
        │
        ▼
Media Asset
        │
        ▼
Resource and Media Resolver
        │
        ▼
Runtime Resource
        │
        ▼
Application
```

Applications interact with runtime resources rather than physical package paths.

---

## 13.3 Design Principles

The Resource and Media Resolution service shall be:

- deterministic
- read-only
- platform-independent
- implementation-neutral
- resource-oriented
- observable

Resource resolution shall never modify:

- canonical entities
- package resources
- runtime content

---

## 13.4 Resource Model

The architecture distinguishes between canonical entities and physical resources.

Conceptually:

```text
Melody

↓

Media Asset

↓

Physical Resource
```

Likewise:

```text
Text

↓

Media Asset

↓

Illustration
```

Canonical entities describe resources.

The Resource and Media Resolution service locates them.

---

## 13.5 Media Asset Model

A Media Asset represents a canonical description of one runtime resource.

Examples include:

```text
Audio Recording

Image

Notation

Document

Video
```

The Media Asset contains descriptive metadata.

It is not the physical resource itself.

---

## 13.6 Resolution Process

Resource resolution follows a deterministic sequence.

```text
Media Asset

↓

Resource Resolver

↓

Locate Resource

↓

Verify Availability

↓

Runtime Resource
```

The resulting runtime resource is returned to the requesting application.

---

## 13.7 Resource Types

The architecture supports multiple resource categories.

Typical examples include:

```text
Audio

Images

Notation

Documents

Video
```

Future package versions may introduce additional resource types.

The resolution mechanism remains unchanged.

---

## 13.8 Resource Locations

The physical location of a resource is hidden behind the Resource Resolver.

Possible locations include:

```text
Application Package

Platform Assets

Local Storage

Downloaded Content

Future Storage Providers
```

Applications shall not depend upon resource storage location.

---

## 13.9 Resource Availability

Before providing access, the Resource Resolver verifies resource availability.

Possible outcomes include:

```text
Available

Unavailable

Unsupported

Missing
```

Availability verification shall be deterministic.

---

## 13.10 Local and Remote Resources

The architecture permits both local and remote resources.

Conceptually:

```text
Media Asset

↓

Local Resource
```

or

```text
Media Asset

↓

Remote Resource
```

Regardless of storage location, the observable behavior shall remain consistent.

Applications interact only with runtime resources.

---

## 13.11 Resource Access

Applications access resources through the Resource and Media Resolution service.

Typical workflow:

```text
Media Asset

↓

Resource Resolver

↓

Runtime Resource

↓

Application
```

Applications shall not access package paths directly.

---

## 13.12 Media Variants

A single Media Asset may provide multiple resource variants.

Examples include:

```text
High Quality Audio

Compressed Audio
```

or

```text
Light Theme Image

Dark Theme Image
```

The selection policy is determined by package capabilities and runtime configuration.

The architectural resolution process remains identical.

---

## 13.13 Resource Errors

Resource access failures are represented through the Core Engine Error Model.

Typical errors include:

```text
ResourceNotFound

ResourceUnavailable

UnsupportedResourceType

ResourceAccessFailure

ResourceCorrupted
```

Resource failures shall not modify canonical runtime content.

---

## 13.14 Resource Caching

Implementations may cache resolved resources.

For example:

```text
Media Asset

↓

Cached Runtime Resource
```

Caching is optional.

It shall not alter:

- canonical meaning
- observable behavior
- deterministic resolution

Caching remains an implementation optimization.

---

## 13.15 Resource Performance

Resource resolution may involve:

- storage access
- decoding
- platform APIs
- streaming

Performance optimizations shall remain implementation details.

Observable runtime behavior shall remain unchanged.

---

## 13.16 Relationship to Runtime Content Store

The Runtime Content Store owns canonical Media Asset entities.

The Resource and Media Resolution service consumes those entities.

Conceptually:

```text
Runtime Content Store

↓

Media Asset

↓

Resource Resolver

↓

Runtime Resource
```

The Runtime Content Store does not access physical resources.

---

## 13.17 Relationship to Reference Resolver

The Reference Resolver resolves canonical identifiers into Media Asset entities.

The Resource and Media Resolution service transforms Media Assets into runtime resources.

Conceptually:

```text
Media Asset ID

↓

Reference Resolver

↓

Media Asset

↓

Resource Resolver

↓

Runtime Resource
```

The two services have distinct responsibilities.

---

## 13.18 Relationship to Applications

Applications consume runtime resources.

They do not:

- interpret package paths
- locate resource files
- manage storage providers

Applications request resources from the Core Engine.

The Resource and Media Resolution service provides the appropriate runtime representation.

---

## 13.19 Diagnostics

The Resource and Media Resolution service may emit diagnostics including:

- resource identifier
- resource type
- storage provider
- resolution outcome
- diagnostic code
- descriptive message

Diagnostics support:

- debugging
- runtime inspection
- automated testing
- performance analysis

Diagnostics shall not alter resource resolution.

---

## 13.20 Extensibility

Future platform versions may introduce:

- additional resource types
- new storage providers
- streaming capabilities
- adaptive media selection
- alternative resource representations

The Resource and Media Resolution architecture shall support these extensions without changing the architectural contract.

New capabilities shall preserve:

- deterministic behavior
- platform independence
- compatibility with existing Media Assets

---

## 13.21 Resource and Media Resolution Summary

The Resource and Media Resolution service transforms canonical Media Asset entities into runtime resources that applications can consume.

Its overall workflow is summarized below.

```text
Media Asset Identifier
        │
        ▼
Reference Resolver
        │
        ▼
Media Asset
        │
        ▼
Resource and Media Resolver
        │
        ▼
Locate Physical Resource
        │
        ▼
Verify Availability
        │
        ▼
Runtime Resource
        │
        ▼
Application
```

The Resource and Media Resolution service does not own canonical content, does not modify package resources, and does not expose storage-specific implementation details.

Its responsibility is to provide deterministic, platform-independent access to runtime resources while preserving a clear architectural separation between canonical entities, physical resources, and application behavior.

By isolating resource access from storage implementation, the Core Engine ensures consistent media handling across all supported SyriacPlatform applications.

# 14. Localization Service

## 14.1 Purpose

This section defines the architectural model of the Localization Service.

The Localization Service provides a consistent mechanism for presenting canonical content in the language selected by the application or runtime environment.

Its primary purpose is to resolve localized representations of canonical entities while preserving the integrity of the underlying runtime model.

Its responsibilities include:

- resolving localized content
- selecting the active language
- providing localized labels and text
- applying language fallback rules
- preserving canonical identity across all languages

The Localization Service does not translate content.

It provides access only to localized representations that are included within the Application Package.

---

## 14.2 Architectural Role

The Localization Service operates as a runtime service above the Runtime Content Store.

Its position within the runtime architecture is:

```text
Runtime Content Store
        │
        ▼
Localization Service
        │
        ▼
Localized Representation
        │
        ▼
Application
```

Applications request localized content through the Localization Service rather than accessing localized values directly.

---

## 14.3 Design Principles

The Localization Service shall be:

- deterministic
- language-aware
- read-only
- platform-independent
- implementation-neutral
- observable

Localization operations shall never modify:

- canonical entities
- identifiers
- relationships
- package content

Changing the active language shall affect only the presentation of content.

---

## 14.4 Localization Model

The Localization Service separates canonical identity from localized presentation.

Conceptually:

```text
Canonical Entity
        │
        ▼
Localization Service
        │
        ▼
Localized Representation
```

The canonical entity remains unchanged regardless of the selected language.

Only its localized representation may differ.

---

## 14.5 Supported Languages

The set of supported languages is defined by the Application Package.

Examples include:

```text
Syriac

Arabic

English

French
```

Applications shall not assume that every package supports the same language set.

---

## 14.6 Localized Content

Localization may be provided for various presentation elements.

Examples include:

```text
Titles

Names

Descriptions

Labels

User Interface Text

Metadata
```

Localized content supplements canonical entities without changing their meaning.

---

## 14.7 Localization Resolution

Localization follows a deterministic resolution process.

```text
Canonical Entity

↓

Requested Language

↓

Localization Service

↓

Localized Representation
```

If a localized representation exists, it shall be returned to the application.

---

## 14.8 Language Selection

The active language is determined by the Runtime Context or the requesting application.

Conceptually:

```text
Runtime Context

↓

Active Language

↓

Localization Service
```

Applications may request localization using any language supported by the package.

---

## 14.9 Fallback Strategy

If the requested language is unavailable, the Localization Service shall apply the package-defined fallback strategy.

Conceptually:

```text
Requested Language

↓

Unavailable

↓

Fallback Language

↓

Localized Representation
```

Fallback behavior shall be deterministic.

Applications shall receive predictable localization results.

---

## 14.10 Canonical Identity

Localization shall never affect canonical identity.

The following remain unchanged regardless of language:

- identifiers
- entity relationships
- package structure
- navigation paths
- search references

Localization changes presentation only.

---

## 14.11 Localized Resources

Certain media resources may also provide localized variants.

Examples include:

```text
Localized Images

Localized Documents

Localized Audio

Localized Video
```

The Localization Service determines the appropriate localized representation.

Resource access remains the responsibility of the Resource and Media Resolution service.

---

## 14.12 Localization Errors

Localization failures are represented using the Core Engine Error Model.

Typical errors include:

```text
UnsupportedLanguage

LocalizationUnavailable

LocalizationFailure
```

A missing localization shall not invalidate the canonical entity.

Fallback behavior should be attempted whenever possible.

---

## 14.13 Localization Performance

Localization operations are expected to occur frequently throughout application execution.

Implementations may optimize:

- localized lookup
- language caching
- resource reuse

Performance optimizations remain implementation details.

Observable localization behavior shall remain unchanged.

---

## 14.14 Relationship to Runtime Context

The Runtime Context maintains the active language configuration.

The Localization Service consumes this information when resolving localized content.

Conceptually:

```text
Runtime Context

↓

Active Language

↓

Localization Service
```

The Runtime Context owns language selection.

The Localization Service performs language resolution.

---

## 14.15 Relationship to Runtime Content Store

The Runtime Content Store owns canonical entities.

The Localization Service consumes canonical entities and returns localized representations.

Conceptually:

```text
Runtime Content Store

↓

Canonical Entity

↓

Localization Service

↓

Localized Representation
```

The Runtime Content Store remains language-independent.

---

## 14.16 Relationship to Resource and Media Resolution

The Localization Service determines which localized resource representation should be used.

The Resource and Media Resolution service locates and provides the corresponding runtime resource.

Conceptually:

```text
Localization Service

↓

Localized Media Asset

↓

Resource and Media Resolution

↓

Runtime Resource
```

The two services provide complementary responsibilities.

---

## 14.17 Relationship to Applications

Applications consume localized representations through the Localization Service.

Applications should not:

- implement package-specific localization logic
- access localized package structures directly
- duplicate fallback rules

The Core Engine provides a consistent localization interface across all applications.

---

## 14.18 Diagnostics

The Localization Service may emit diagnostics including:

- requested language
- resolved language
- fallback usage
- localization status
- diagnostic code
- descriptive message

Diagnostics support:

- debugging
- runtime inspection
- localization verification
- automated testing

Diagnostics shall not alter localization behavior.

---

## 14.19 Extensibility

Future platform versions may introduce:

- additional languages
- regional language variants
- script variations
- advanced localization metadata
- dynamic localization capabilities

The Localization Service shall support these extensions without changing its architectural responsibilities.

All extensions shall preserve:

- canonical identity
- deterministic behavior
- compatibility with existing Application Packages

---

## 14.20 Localization Service Summary

The Localization Service provides deterministic access to localized representations of canonical runtime entities.

Its overall workflow is summarized below.

```text
Canonical Entity
        │
        ▼
Runtime Context
        │
        ▼
Active Language
        │
        ▼
Localization Service
        │
        ▼
Localized Representation
        │
        ▼
(Optional)
Localized Media Asset
        │
        ▼
Resource and Media Resolution
        │
        ▼
Runtime Resource
        │
        ▼
Application
```

The Localization Service does not modify canonical entities, identifiers, relationships, or package structure.

Its responsibility is to resolve localized representations while preserving the integrity and stability of the canonical runtime model.

By separating localization from content ownership, navigation, search, and resource access, the Core Engine provides a consistent multilingual architecture that can support diverse applications without compromising canonical content.

# 15. Runtime Context

## 15.1 Purpose

This section defines the architectural model of the Runtime Context.

The Runtime Context maintains the mutable execution state of the Core Engine during an application session.

Its primary purpose is to preserve runtime information that changes while the application is executing, without affecting the immutable canonical content stored within the Runtime Content Store.

Its responsibilities include:

- maintaining the current runtime state
- tracking the current navigation position
- storing active runtime configuration
- providing shared execution context for runtime services
- coordinating runtime state across the Core Engine

The Runtime Context does not own canonical content.

It stores only runtime state that exists during the lifetime of an active runtime session.

---

## 15.2 Architectural Role

The Runtime Context provides shared execution state for runtime services.

Its position within the runtime architecture is:

```text
Runtime Content Store
        │
        ▼
Runtime Context
        │
        ├──────────────┐
        ▼              ▼
Navigation      Localization
   Service          Service
        │              │
        └──────┬───────┘
               ▼
         Other Runtime Services
               │
               ▼
          Application
```

The Runtime Context acts as the shared source of mutable runtime information.

---

## 15.3 Design Principles

The Runtime Context shall be:

- mutable
- deterministic
- session-scoped
- platform-independent
- observable
- implementation-neutral

The Runtime Context shall never modify:

- canonical entities
- package structure
- runtime content
- canonical identifiers

Changes to the Runtime Context affect only runtime behavior.

---

## 15.4 Runtime Context Model

The architecture separates immutable content from mutable runtime state.

Conceptually:

```text
Runtime Content Store

(Immutable)

        │

        ▼

Runtime Context

(Mutable)
```

Canonical content remains constant throughout the runtime session.

The Runtime Context changes as the application executes.

---

## 15.5 Context Lifetime

A Runtime Context is created during runtime initialization.

Its lifecycle is:

```text
Runtime Initialization

↓

Runtime Context Created

↓

Application Execution

↓

Runtime Shutdown

↓

Context Destroyed
```

Each runtime session owns exactly one Runtime Context.

---

## 15.6 Runtime State

The Runtime Context may maintain runtime information such as:

- current navigation position
- active language
- selected application profile
- active package configuration
- temporary runtime flags

Only information required during execution belongs in the Runtime Context.

---

## 15.7 Navigation State

The Runtime Context owns the application's current navigation position.

Examples include:

```text
Current Entry Point

Current Occasion

Current Prayer

Current Prayer Sequence

Current Liturgical Item
```

Navigation Services update this information.

Other services may consume it.

---

## 15.8 Language State

The Runtime Context maintains the currently active language.

Conceptually:

```text
Active Language

↓

Localization Service

↓

Localized Representation
```

Changing the active language affects localization only.

Canonical content remains unchanged.

---

## 15.9 Session State

The Runtime Context maintains execution state that exists only during the current runtime session.

Examples include:

- current selections
- active runtime options
- temporary application state

Session state is discarded when the runtime session ends.

---

## 15.10 State Updates

Runtime state changes occur through explicit operations.

Typical updates include:

```text
Navigation

↓

Context Updated
```

or

```text
Language Change

↓

Context Updated
```

State changes shall be deterministic and observable.

---

## 15.11 Context Consistency

The Runtime Context shall always remain internally consistent.

Updates shall never produce:

- invalid navigation state
- unsupported language selection
- inconsistent runtime configuration

Invalid updates shall fail explicitly.

---

## 15.12 Context Isolation

Each Runtime Context belongs to exactly one runtime session.

Multiple runtime sessions shall never share mutable runtime state.

Conceptually:

```text
Session A

↓

Runtime Context A


Session B

↓

Runtime Context B
```

Session isolation preserves deterministic behavior.

---

## 15.13 Context Errors

Runtime Context failures are represented using the Core Engine Error Model.

Typical errors include:

```text
ContextUnavailable

InvalidRuntimeState

ContextInitializationFailure

ContextUpdateFailure
```

Context failures shall never corrupt canonical runtime content.

---

## 15.14 Runtime Performance

The Runtime Context is expected to be accessed frequently.

Implementations may optimize:

- state lookup
- state updates
- synchronization
- caching

Performance optimizations remain implementation details.

Observable runtime behavior shall remain unchanged.

---

## 15.15 Relationship to Runtime Content Store

The Runtime Content Store owns immutable canonical content.

The Runtime Context owns mutable runtime state.

Conceptually:

```text
Runtime Content Store

↓

Canonical Content


Runtime Context

↓

Runtime State
```

The two components have complementary responsibilities.

---

## 15.16 Relationship to Navigation Service

The Navigation Service reads and updates the Runtime Context.

Conceptually:

```text
Runtime Context

↓

Navigation Service

↓

Updated Runtime Context
```

Navigation does not own runtime state.

The Runtime Context remains the authoritative source.

---

## 15.17 Relationship to Localization Service

The Localization Service consumes the active language maintained by the Runtime Context.

Conceptually:

```text
Runtime Context

↓

Active Language

↓

Localization Service
```

Language selection belongs to the Runtime Context.

Language resolution belongs to the Localization Service.

---

## 15.18 Relationship to Applications

Applications may query or modify runtime state only through the Core Engine.

Applications shall not manipulate Runtime Context internals directly.

The Runtime Context provides a stable abstraction independent of application implementation.

---

## 15.19 Diagnostics

The Runtime Context may emit diagnostics including:

- current navigation state
- active language
- runtime configuration
- context update operations
- diagnostic code
- descriptive message

Diagnostics support:

- debugging
- runtime inspection
- automated testing
- state verification

Diagnostics shall not alter runtime behavior.

---

## 15.20 Extensibility

Future platform versions may introduce additional runtime state including:

- user preferences
- accessibility settings
- synchronization state
- application-specific runtime extensions

The Runtime Context shall support these extensions without changing its architectural responsibilities.

All extensions shall preserve:

- deterministic behavior
- session isolation
- compatibility with existing runtime services

---

## 15.21 Runtime Context Summary

The Runtime Context provides the mutable execution state shared by all Core Engine runtime services.

Its overall architecture is summarized below.

```text
Runtime Content Store
        │
        │ (Immutable)
        ▼
Runtime Context
        │
        ├──────────────┐
        ▼              ▼
Navigation      Localization
   Service          Service
        │              │
        └──────┬───────┘
               ▼
      Other Runtime Services
               │
               ▼
          Application
```

The Runtime Context does not own canonical entities, package content, or physical resources.

Its responsibility is to maintain consistent, session-scoped runtime state while preserving a strict separation between immutable canonical content and mutable execution state.

By isolating runtime state from canonical content, the Core Engine ensures predictable behavior, deterministic execution, and a clear architectural boundary between content management and application interaction.

# 16. Public API

## 16.1 Purpose

This section defines the architectural model of the Core Engine Public API.

The Public API provides the official interface through which applications interact with the Core Engine.

Its primary purpose is to expose the capabilities of the Core Engine without revealing its internal implementation.

Its responsibilities include:

- providing a stable application interface
- exposing runtime services
- coordinating engine operations
- preserving architectural boundaries
- ensuring implementation independence

Applications shall communicate with the Core Engine exclusively through the Public API.

---

## 16.2 Architectural Role

The Public API represents the external boundary of the Core Engine.

Its position within the architecture is:

```text
Application
        │
        ▼
Public API
        │
        ▼
Core Engine Services
        │
        ▼
Runtime Components
```

Applications remain isolated from internal engine implementation.

---

## 16.3 Design Principles

The Public API shall be:

- stable
- deterministic
- implementation-independent
- platform-independent
- observable
- minimal
- consistent

The Public API shall expose capabilities rather than implementation details.

Internal engine components shall remain inaccessible to applications.

---

## 16.4 API Model

Applications communicate with the Core Engine exclusively through the Public API.

Conceptually:

```text
Application

↓

Public API

↓

Core Engine
```

The Public API acts as the architectural gateway to all runtime capabilities.

---

## 16.5 API Responsibilities

The Public API provides access to Core Engine services including:

- runtime initialization
- runtime shutdown
- navigation
- search
- localization
- resource access
- runtime context operations

The Public API coordinates these services without owning their implementation.

---

## 16.6 Service Exposure

The Public API exposes runtime capabilities rather than individual internal components.

Conceptually:

```text
Public API

├── Runtime

├── Navigation

├── Search

├── Localization

├── Resources

└── Runtime Context
```

Applications interact with services through well-defined interfaces.

---

## 16.7 Initialization Interface

The Public API provides engine initialization.

Typical sequence:

```text
Application

↓

Initialize

↓

Core Engine Ready
```

Initialization completes only after successful package loading, validation, and runtime construction.

---

## 16.8 Runtime Operations

The Public API provides runtime operations throughout the lifetime of the application.

Typical operations include:

```text
Navigate

Search

Resolve Resources

Localize

Query Runtime Context
```

All operations shall preserve deterministic behavior.

---

## 16.9 Result Model

Public API operations return explicit results.

A result may represent:

```text
Success

Failure
```

Successful operations return the requested runtime information.

Failed operations return explicit runtime errors.

---

## 16.10 Error Handling

The Public API exposes failures using the Core Engine Error Model.

Typical errors include:

```text
InitializationFailure

ValidationFailure

NavigationFailure

SearchFailure

ResourceFailure

LocalizationFailure
```

Errors shall never expose internal implementation details.

---

## 16.11 API Consistency

All Public API operations shall follow consistent behavioral principles.

Applications shall observe:

- predictable behavior
- explicit outcomes
- deterministic execution
- stable interfaces

Equivalent operations shall produce equivalent observable results.

---

## 16.12 Thread Safety

The architectural contract does not require a specific concurrency model.

Implementations may support:

- single-threaded execution
- multi-threaded execution
- asynchronous execution

Regardless of implementation, observable API behavior shall remain deterministic.

---

## 16.13 Performance

Implementations may optimize:

- service dispatch
- caching
- resource reuse
- asynchronous execution

Performance optimizations shall not change the observable behavior of the Public API.

---

## 16.14 Relationship to Runtime Services

The Public API coordinates runtime services.

Conceptually:

```text
Public API
        │
        ├──────────────┐
        ▼              ▼
Navigation      Search
   Service        Service
        │              │
        ├──────────────┤
        ▼              ▼
Localization   Resource Resolution
   Service           Service
        │              │
        └──────┬───────┘
               ▼
        Runtime Context
```

Each runtime service remains responsible for its own architectural domain.

The Public API provides unified access.

---

## 16.15 Relationship to Applications

Applications communicate only with the Public API.

Applications shall not directly access:

- Runtime Content Store
- Reference Resolver
- Navigation Service internals
- Search Index
- Runtime Context internals
- Resource providers

This preserves clear architectural separation between applications and engine implementation.

---

## 16.16 API Stability

The Public API represents the long-term architectural contract of the Core Engine.

Future platform versions may extend the API.

Existing behavior shall remain compatible whenever practical.

Breaking changes shall be avoided.

---

## 16.17 Diagnostics

The Public API may emit diagnostics including:

- invoked operation
- execution outcome
- execution duration
- diagnostic code
- descriptive message

Diagnostics support:

- debugging
- runtime inspection
- automated testing
- performance analysis

Diagnostics shall not affect observable API behavior.

---

## 16.18 Extensibility

Future platform versions may introduce:

- additional runtime services
- expanded API capabilities
- new asynchronous operations
- additional platform integrations

Extensions shall preserve:

- architectural compatibility
- deterministic behavior
- implementation independence
- application compatibility

---

## 16.19 Public API Summary

The Public API provides the official architectural interface between applications and the Core Engine.

Its overall architecture is summarized below.

```text
                 Application
                       │
                       ▼
                 Public API
                       │
        ┌──────────────┼──────────────┐
        ▼              ▼              ▼
 Navigation        Search      Localization
   Service         Service         Service
        │              │              │
        ├──────────────┼──────────────┤
        ▼              ▼              ▼
 Resource Resolution   Runtime Context
        │              │
        └──────────────┘
               │
               ▼
      Runtime Content Store
               │
               ▼
       Application Package
```

The Public API is the only architectural entry point into the Core Engine.

It hides internal implementation details while providing stable, deterministic, and platform-independent access to all runtime capabilities.

By establishing a single, well-defined interface between applications and runtime services, the Public API preserves architectural separation, simplifies application development, and ensures long-term compatibility across all SyriacPlatform applications.
