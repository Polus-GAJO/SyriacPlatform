# Application Package Specification
Version 1.0

---

# 1. Introduction

## 1.1 Purpose

This document defines the official physical specification of the SyriacPlatform Application Package.

While the Domain Model defines the conceptual liturgical domain and the Application Content Model defines the logical runtime content, this specification defines how that content is physically represented, stored, validated, distributed, and consumed by applications.

This document serves as the authoritative contract between:

- Build Tools
- Package Validators
- Core Engine
- Runtime Applications

Every package produced for the SyriacPlatform SHALL conform to this specification unless another package specification explicitly supersedes it.

---

## 1.2 Scope

This specification defines:

- package architecture
- directory structure
- file organization
- manifest structure
- JSON encoding rules
- identifier rules
- media organization
- versioning
- validation requirements
- compatibility rules
- package profiles
- deterministic package generation

This specification intentionally does **not** define:

- the conceptual liturgical domain
- editorial rules
- Author Database implementation
- Build Tools implementation
- Core Engine implementation
- application user interface
- programming language APIs
- database schemas

These subjects are defined by separate platform documents.

---

## 1.3 Relationship to Other Specifications

The SyriacPlatform architecture is organized into several complementary specifications.

The documents are intended to be read in the following order:

```text
Platform Blueprint
        │
        ▼
Domain Model
        │
        ▼
Application Content Model
        │
        ▼
Application Package Specification
        │
        ▼
Implementation Documentation
```

Each document builds upon the previous one.

The present specification assumes that the concepts defined in the Domain Model and the Application Content Model are already understood and therefore does not redefine them.

---

## 1.4 Goals

The Application Package Specification has the following primary goals.

### Standardization

Every application package shall follow the same physical structure regardless of:

- application type
- application size
- supported content
- target platform

This allows all platform components to interpret packages consistently.

---

### Portability

Packages shall be independent of:

- operating system
- programming language
- application framework
- database technology

A valid package must be consumable by any compliant Core Engine implementation.

---

### Determinism

Given identical:

- Author Database revision
- Build Tools version
- package configuration

the Build Tools should produce logically equivalent packages.

Where deterministic serialization is enabled, byte-for-byte identical packages should be produced.

---

### Extensibility

The specification shall allow future expansion without breaking existing compliant implementations whenever possible.

Optional capabilities shall therefore be introduced through additive mechanisms rather than incompatible structural changes.

---

### Simplicity

The package format should remain:

- human-readable
- machine-readable
- easy to validate
- easy to debug
- easy to compare under version control

---

## 1.5 Design Principles

The package format is based on the following architectural principles.

### Principle 1 — Canonical Representation

The Application Package is the canonical runtime representation of application content.

Applications shall consume package content rather than reconstructing information from the Author Database.

---

### Principle 2 — Separation of Responsibilities

Each architectural layer has one clearly defined responsibility.

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

Responsibility must not move backward across these layers.

---

### Principle 3 — Stable Identity

Every reusable entity shall maintain a stable identifier across package versions whenever its conceptual identity remains unchanged.

Applications must rely on identifiers rather than display titles.

---

### Principle 4 — Explicit Structure

Every relationship required for runtime interpretation shall be represented explicitly.

Applications must not infer:

- ordering
- inheritance
- contextual selections
- liturgical relationships

from unrelated data.

---

### Principle 5 — Self-Contained Packages

Unless a package profile explicitly declares external dependencies, every package shall contain all information required for offline runtime operation.

---

### Principle 6 — Immutable Content

Generated packages are immutable.

Applications shall treat every package as a versioned snapshot rather than editable runtime content.

---

# 2. Conformance

## 2.1 Normative Language

The key words:

- **MUST**
- **MUST NOT**
- **REQUIRED**
- **SHALL**
- **SHALL NOT**
- **SHOULD**
- **SHOULD NOT**
- **RECOMMENDED**
- **MAY**
- **OPTIONAL**

are to be interpreted as normative requirements within this specification.

These terms indicate implementation obligations rather than recommendations.

---

## 2.2 Conforming Package

A package conforms to this specification when it satisfies all mandatory requirements defined herein.

In particular, a conforming package shall:

- follow the required directory layout
- contain all required files
- use the defined encoding rules
- satisfy identifier requirements
- preserve referential integrity
- declare supported schema versions
- pass structural validation

A package that violates any mandatory requirement is not considered conforming.

---

## 2.3 Conforming Build Tools

A Build Tool implementation conforms to this specification when it produces packages that satisfy every mandatory requirement defined by this document.

Conforming Build Tools are responsible for:

- generating valid package structures
- resolving editorial logic
- producing deterministic output
- validating package integrity before publication

---

## 2.4 Conforming Core Engine

A Core Engine conforms to this specification when it correctly interprets every conforming package without requiring knowledge of the Author Database.

A conforming Core Engine shall:

- load package metadata
- validate structural integrity
- resolve references
- expose runtime content
- reject unsupported schema versions
- ignore unsupported optional extensions unless otherwise specified

---

## 2.5 Conforming Applications

Applications conform to this specification when they consume package content exclusively through the Application Package and the Core Engine.

Applications shall not:

- access the Author Database
- reconstruct editorial inheritance
- reinterpret package structure
- modify canonical package content

Applications remain free to implement any user interface or presentation model provided the canonical package semantics remain unchanged.

---

# 3. Terminology

The following terminology is used consistently throughout this specification.

---

## Application Package

A structured collection of files representing the canonical runtime content of one application.

An Application Package consists of:

- metadata
- structured content
- optional indexes
- media resources

---

## Package Root

The top-level directory of an Application Package.

Every package contains exactly one Package Root.

---

## Manifest

The package metadata document describing:

- package identity
- schema version
- content version
- application identity
- compatibility information

The manifest is the entry point of every package.

---

## Canonical Content

The authoritative runtime representation generated by the Build Tools.

Canonical content shall not be modified by applications.

---

## Derived Content

Content generated from canonical data for performance or convenience.

Examples include:

- indexes
- caches
- search structures

Derived content must always be reproducible from canonical content.

---

## Entity Collection

A JSON document containing entities of one specific type.

Examples include:

```text
texts.json
qolos.json
prayers.json
```

Each entity collection contains only one category of entities.

---

## Media Resource

A binary file referenced by one or more package entities.

Examples include:

- audio
- notation
- images
- documents
- video

---

## Schema Version

The version of the package structure defined by this specification.

Schema Version determines structural compatibility between packages and Core Engine implementations.

---

## Content Version

The editorial revision of the package content.

Content Version changes independently from Schema Version.

---

## Package Profile

A predefined subset of the specification defining which collections are required for a particular application category.

Examples include:

- Occasion Profile
- Daily Prayer Profile
- Full Library Profile

---

## Deterministic Build

A package generation process that produces identical logical output from identical source content and configuration.

---

## Normative Requirement

A rule identified by terms such as:

- MUST
- SHALL
- REQUIRED

Violation of a normative requirement results in a non-conforming implementation.

---

## Informative Example

Examples included throughout this specification are provided for illustration only.

Unless explicitly stated otherwise, examples are not normative.

The normative requirements are defined exclusively by the surrounding specification text.

---

# 4. Specification Structure

This specification is organized into four major sections.

```text
Part I
Foundations
    • Architecture
    • Package format
    • Terminology

Part II
Physical Representation
    • Directory layout
    • Manifest
    • JSON files
    • Media

Part III
Validation and Compatibility
    • Versioning
    • Validation
    • Profiles
    • Compatibility

Part IV
Appendices
    • Examples
    • Recommendations
    • Future extensions
```

Each section builds upon the previous sections.

Implementations should interpret this document as a complete specification rather than a collection of independent rules.

# 5. Package Architecture

## 5.1 Overview

An Application Package is the canonical physical representation of one application's runtime content.

Every package SHALL contain all information required by its declared Package Profile unless explicit external dependencies are declared by a future specification.

The package architecture separates:

- package metadata
- canonical content
- derived content
- binary resources

Each category has a clearly defined responsibility.

---

## 5.2 Architectural Layers

Conceptually, every package consists of four logical layers.

```text
Application Package
│
├── Metadata
│
├── Canonical Content
│
├── Derived Content (Optional)
│
└── Binary Resources
```

Each layer is independent.

No layer shall duplicate the responsibility of another.

---

## 5.3 Metadata Layer

The Metadata Layer describes the package itself.

Typical information includes:

- package identity
- schema version
- content version
- application identity
- compatibility requirements

Metadata does not contain liturgical content.

---

## 5.4 Canonical Content Layer

The Canonical Content Layer contains the complete structured runtime model defined by the Application Content Model.

Canonical content represents the authoritative runtime data exported by the Build Tools.

Applications SHALL interpret canonical content exactly as exported.

---

## 5.5 Derived Content Layer

Derived content consists of optional structures generated from canonical content.

Examples include:

- search indexes
- lookup indexes
- optimized navigation structures

Derived content SHALL NOT introduce new semantic information.

Every derived structure MUST be reproducible from canonical content.

---

## 5.6 Binary Resource Layer

Binary resources contain non-JSON assets required by the application.

Examples include:

- audio recordings
- musical notation
- images
- PDF documents
- video

Binary resources are referenced through MediaAsset entities.

Applications SHALL access binary resources through MediaAsset references rather than direct directory assumptions.

---

# 6. Package Representation

## 6.1 Working Representation

During package generation, the Build Tools SHALL produce a normal directory structure.

Conceptually:

```text
Package Folder
├── manifest.json
├── content/
├── indexes/
└── media/
```

The working representation exists primarily for:

- package generation
- validation
- debugging
- testing
- version control
- development

The directory representation is considered the canonical physical layout.

---

## 6.2 Distribution Representation

For publication and installation, packages SHOULD be distributed as a single archive.

The standard package extension is:

```text
.syrpkg
```

Conceptually:

```text
Occasions.syrpkg
Shhima.syrpkg
FullLibrary.syrpkg
```

The archive contains exactly the same directory structure as the working representation.

Packaging SHALL NOT alter logical package content.

---

## 6.3 Archive Format

Version 1.0 uses a ZIP-compatible archive with the custom extension:

```text
.syrpkg
```

The custom extension identifies the archive as a SyriacPlatform Application Package.

Applications SHALL identify packages by both:

- extension
- manifest validation

A valid file extension alone is not sufficient to establish package validity.

---

## 6.4 Installation

A typical installation process consists of:

```text
Read archive
        │
        ▼
Validate manifest
        │
        ▼
Validate package structure
        │
        ▼
Extract package
        │
        ▼
Install locally
        │
        ▼
Load through Core Engine
```

Future implementations may read directly from archives.

Such optimizations shall not change the logical package model.

---

# 7. Package Root

## 7.1 Definition

Every Application Package SHALL contain exactly one Package Root.

The Package Root is the top-level directory of the package.

No package may contain multiple independent roots.

---

## 7.2 Root Contents

Version 1.0 defines the following top-level directories.

```text
Package Root
│
├── manifest.json
├── content/
├── indexes/
└── media/
```

Additional top-level directories SHALL NOT be introduced unless defined by a future schema version.

---

## 7.3 Required Elements

The following elements are mandatory.

```text
manifest.json
content/
media/
```

The following element is optional.

```text
indexes/
```

When omitted, applications may generate runtime indexes internally.

---

## 7.4 Unknown Directories

Applications SHOULD ignore unknown directories that are explicitly permitted by future schema versions.

Applications MUST NOT reinterpret unknown directories as canonical content.

---

# 8. Physical Directory Layout

A package conforming to Schema v1 SHALL use the following canonical
directory structure:

```text
/
├── manifest.json
└── content/
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

The files listed above constitute the canonical content collections
defined by Schema v1.

Whether a collection file is required or optional depends on the
declared package profile.

A required collection SHALL be physically present in the package even
when it contains no items.

An optional collection MAY be absent.

Collections reserved for future schema versions are not part of the
active Schema v1 package layout.

---

# 9. Canonical Directory Names

## 9.1 Fixed Directory Names

The following directory names are reserved.

```text
content
indexes
media
```

Implementations SHALL use these exact names.

Case variations are not permitted.

---

## 9.2 Fixed File Names

Schema v1 defines the following canonical file names:

| Collection | Canonical file name |
|---|---|
| Entry points | `entry-points.json` |
| Occasions | `occasions.json` |
| Prayers | `prayers.json` |
| Prayer sequences | `prayer-sequences.json` |
| Liturgical items | `liturgical-items.json` |
| Texts | `texts.json` |
| Petgomos | `petgomos.json` |
| Qolos | `qolos.json` |
| Melodies | `melodies.json` |
| Qintos | `qintos.json` |
| Melody-Qinto assignments | `melody-qinto-assignments.json` |

These names are canonical and SHALL be used exactly as specified.

Collection file names are lowercase, plural where applicable, and use
kebab-case for compound names.

Implementations SHALL NOT infer alternative file names for canonical
Schema v1 collections.

In particular, `petgomos.json` is the canonical Schema v1 file name for
the Petgomo collection.

---

## 9.3 Uniformity

All applications using this specification SHALL use identical filenames.

For example:

```text
Occasions App

content/
    prayers.json
```

and

```text
Shhima App

content/
    prayers.json
```

use the same filename.

The difference lies only in the contained entities.

---

## 9.4 Missing Collections

Package Profiles determine which collections are required.

Collections that are optional for a given profile may be absent.

Applications SHALL interpret missing optional collections as absent content rather than package corruption.

---

# 10. Package Components

Each package component has one clearly defined responsibility.

---

## manifest.json

Contains package metadata.

Responsibilities include:

- package identity
- version information
- compatibility declaration

It does not contain application content.

---

## content/

Contains canonical runtime entities.

Each JSON document stores exactly one entity category.

Canonical content is the authoritative runtime source.

---

## indexes/

Contains optional derived structures.

Indexes improve performance.

Indexes SHALL NOT introduce new semantic information.

Applications may ignore indexes and still interpret canonical content correctly.

---

## media/

Contains binary resources referenced by MediaAsset entities.

Applications SHOULD access media through MediaAsset identifiers rather than constructing filesystem paths manually.

---

# 11. Canonical Content Collections

Canonical content is divided into independent entity collections.

Each collection contains one entity type only.

For example:

```text
texts.json
```

contains only:

```text
TextContent
```

Likewise:

```text
melodies.json
```

contains only:

```text
MelodyContent
```

Mixed entity collections are not permitted.

---

## 11.1 Collection Independence

Each collection is independently readable.

Applications may load only the collections required for a specific operation.

For example:

A search operation may initially load:

```text
texts.json
search-index.json
```

without loading every entity collection.

---

## 11.2 Entity Ownership

Each entity belongs to exactly one canonical collection.

For example:

```text
QoloContent
```

belongs only to:

```text
qolos.json
```

It SHALL NOT appear duplicated inside another entity collection.

Relationships are expressed through identifiers.

---

## 11.3 Entity References

Relationships between collections SHALL always use identifiers.

Example:

```text
LiturgicalItem
└── qoloId
```

The complete Qolo definition remains stored in:

```text
qolos.json
```

This preserves normalization and eliminates duplication.

---

# 12. Collection File Format

## 12.1 General Structure

Every canonical entity collection SHALL use the following outer structure.

```json
{
  "items": [
  ]
}
```

This wrapper is mandatory.

Applications SHALL NOT expect a root-level JSON array.

---

## 12.2 Purpose of Wrapper

The wrapper allows future schema versions to introduce additional collection metadata without breaking compatibility.

Possible future additions include:

- entity type
- collection revision
- statistics
- diagnostics
- generation metadata

Applications conforming to Version 1.0 shall ignore unknown wrapper properties unless otherwise specified.

---

## 12.3 Empty Collections

An empty collection is valid.

Example:

```json
{
  "items": []
}
```

Applications shall distinguish between:

- an empty collection
- a missing optional collection
- a missing required collection

These represent different semantic states.

---

## 12.4 Root Properties

Version 1.0 defines one required root property.

```text
items
```

Additional root properties are optional.

Unknown properties SHALL be ignored unless explicitly defined by a future schema version.

---

# 13. Canonical Representation

Every entity stored inside canonical collections represents authoritative runtime content.

Applications SHALL NOT modify canonical entities during normal operation.

Instead, applications may construct runtime objects such as:

- resolved entities
- caches
- lookup tables
- search structures

These runtime objects are implementation details.

They are not part of the package itself.

---

## 13.1 Runtime Objects

Conceptually:

```text
Canonical Package
        │
        ▼
Core Engine
        │
        ▼
Resolved Runtime Objects
```

Runtime objects exist only while the application is running.

They SHALL NOT replace or redefine canonical package content.

---

## 13.2 Canonical Priority

Whenever canonical content and derived runtime data disagree, canonical content is authoritative.

Derived data shall be regenerated rather than modifying canonical package content.

---

# 14. Package Invariants

Every conforming Application Package SHALL satisfy the following structural invariants.

### Invariant 1

Exactly one Package Root exists.

---

### Invariant 2

Exactly one manifest exists.

---

### Invariant 3

Canonical entity collections use fixed filenames.

---

### Invariant 4

Each entity belongs to exactly one collection.

---

### Invariant 5

Relationships are expressed through stable identifiers.

---

### Invariant 6

Canonical content is immutable.

---

### Invariant 7

Derived content never replaces canonical content.

---

### Invariant 8

Binary resources are referenced through MediaAsset entities.

---

### Invariant 9

Working and distribution representations contain identical logical content.

---

### Invariant 10

Every package conforms to exactly one declared schema version.

These invariants form the structural foundation of the Application Package Specification and remain valid throughout Version 1.0.

# 15. Package Manifest

## 15.1 Purpose

Every Application Package SHALL contain exactly one manifest file.

The manifest describes the package itself rather than its content.

It serves as the primary entry point for package discovery, validation, compatibility checking, and loading.

Applications SHALL read and validate the manifest before accessing any other package component.

---

## 15.2 Location

The manifest file SHALL be located in the Package Root.

Its filename is fixed.

```text
manifest.json
```

Alternative filenames are not permitted.

---

## 15.3 Responsibilities

The manifest is responsible for describing:

- package identity
- package name
- application identity
- schema version
- content version
- package version
- build information
- compatibility information
- package profile

The manifest SHALL NOT contain canonical application content.

---

## 15.4 Manifest Structure

The manifest is represented as a JSON object.

Version 1.0 defines the following high-level structure.

```json
{
    "packageId": "...",
    "packageName": "...",

    "schemaVersion": "...",
    "packageVersion": "...",
    "contentVersion": "...",

    "application": {
        ...
    },

    "profile": "...",

    "build": {
        ...
    },

    "compatibility": {
        ...
    }
}
```

Additional properties MAY be introduced by future schema versions.

Applications SHALL ignore unknown properties unless otherwise specified.

---

# 16. Manifest Fields

## 16.1 packageId

A globally unique identifier for the package.

Example:

```json
"packageId": "com.syriacplatform.shhima"
```

Requirements:

- MUST be unique.
- MUST remain stable across package updates.
- MUST NOT change between content revisions.

---

## 16.2 packageName

Human-readable package name.

Example:

```json
"packageName": "Shhima"
```

Requirements:

- Intended for display only.
- SHALL NOT be used for internal references.

---

## 16.3 schemaVersion

Defines which version of the Application Package Specification the package follows.

Example:

```json
"schemaVersion": "1.0"
```

Applications SHALL use this field to determine structural compatibility.

---

## 16.4 packageVersion

Identifies the package release.

Example:

```json
"packageVersion": "1.2.0"
```

Package Version changes whenever a new distributable package is published.

It is independent of the editorial content revision.

---

## 16.5 contentVersion

Identifies the editorial revision represented by the package.

Example:

```json
"contentVersion": "2026.08"
```

Changes to texts, melodies, prayers, or liturgical data SHALL update the Content Version.

---

# 17. Application Information

Application-specific information is stored in the application object.

Example:

```json
{
    "application": {
        "id": "...",
        "name": "...",
        "platform": "...",
        "defaultLanguage": "..."
    }
}
```

---

## 17.1 id

Application identifier.

Example:

```json
"id": "shhima"
```

The identifier SHALL remain stable throughout the application's lifetime.

---

## 17.2 name

Human-readable application name.

Example:

```json
"name": "Shhima"
```

Used for presentation purposes only.

---

## 17.3 platform

Identifies the platform family for which the package was generated.

Typical examples include:

```text
generic
mobile
desktop
web
```

Version 1.0 RECOMMENDS using:

```text
generic
```

Platform-specific packages SHOULD remain structurally identical whenever possible.

---

## 17.4 defaultLanguage

Defines the primary language used by the package metadata.

Example:

```json
"defaultLanguage": "en"
```

This field does not restrict application localization.

---

# 18. Package Profile

Every package SHALL declare exactly one Package Profile.

Example:

```json
"profile": "Occasion"
```

or

```json
"profile": "Shhima"
```

or

```json
"profile": "FullLibrary"
```

Package Profiles determine:

- required collections
- optional collections
- expected content

Package Profiles are defined later in this specification.

---

# 19. Build Information

Build metadata describes how the package was generated.

Example:

```json
{
    "build": {
        ...
    }
}
```

Build information is informative.

It does not affect runtime semantics.

---

## 19.1 generatedAt

Timestamp indicating package generation.

Example:

```json
"generatedAt": "2026-08-04T18:25:13Z"
```

This field is optional.

---

## 19.2 buildTool

Name of the Build Tool.

Example:

```json
"buildTool": "SyriacPlatform Build Tools"
```

---

## 19.3 buildVersion

Version of the Build Tools used.

Example:

```json
"buildVersion": "1.0.0"
```

---

## 19.4 buildConfiguration

Optional description of the build configuration.

Example:

```json
"buildConfiguration": "Release"
```

Future Build Tools may define additional metadata.

---

# 20. Compatibility

Compatibility information allows applications to determine whether a package can be loaded safely.

Example:

```json
{
    "compatibility": {
        ...
    }
}
```

---

## 20.1 minimumCoreVersion

Defines the oldest compatible Core Engine version.

Example:

```json
"minimumCoreVersion": "1.0"
```

Applications SHALL reject packages requiring newer functionality.

---

## 20.2 targetSchemaVersion

Defines the schema version expected by the package.

Normally this equals:

```text
schemaVersion
```

The field exists to support future migration scenarios.

---

## 20.3 supportedFeatures

Optional list of package capabilities.

Example:

```json
"supportedFeatures": [
    "SearchIndex",
    "Audio",
    "Notation"
]
```

Unknown features SHALL be ignored unless explicitly required.

---

# 21. JSON Encoding Rules

## 21.1 Character Encoding

All JSON files SHALL use:

```text
UTF-8
```

without a Byte Order Mark (BOM).

---

## 21.2 Unicode

Unicode characters SHALL be stored directly.

Example:

```json
"caption": "ܩܠܐ ܩܕܡܝܐ"
```

Escape sequences SHOULD only be used when required by the JSON standard.

---

## 21.3 Line Endings

Canonical package generation SHALL use:

```text
LF
```

line endings.

Applications SHOULD accept any valid line ending.

---

## 21.4 Comments

JSON comments are not permitted.

Example:

```json
// Invalid
```

Packages containing comments are not conforming.

---

## 21.5 Trailing Commas

Trailing commas are prohibited.

Invalid:

```json
{
    "name": "Example",
}
```

---

## 21.6 Number Representation

Numbers SHALL use standard JSON number syntax.

Localized formatting is prohibited.

Invalid:

```text
12,5
```

Valid:

```text
12.5
```

---

## 21.7 Boolean Values

Boolean values SHALL use:

```json
true
false
```

String equivalents are not permitted.

---

## 21.8 Null Values

Null values SHOULD be avoided unless the distinction between:

- missing
- empty
- unknown

is semantically significant.

---

# 22. Deterministic Serialization

## 22.1 General Principle

Official package generation SHOULD produce deterministic JSON serialization.

This ensures that identical source content produces identical package output.

---

## 22.2 Property Ordering

Build Tools SHOULD serialize object properties using a consistent ordering.

The recommended order is:

- identifiers
- names
- structural properties
- references
- optional metadata

---

## 22.3 Entity Ordering

Entity collections SHOULD use deterministic ordering.

The preferred order is ascending identifier order.

Example:

```text
PrayerID

1
2
3
4
...
```

rather than insertion order.

---

## 22.4 Collection Ordering

Canonical collection filenames SHALL remain fixed.

Applications SHALL NOT rely on filesystem ordering.

---

## 22.5 Binary Resources

Binary resources SHALL preserve their original content.

Packaging SHALL NOT modify:

- audio quality
- image resolution
- document content

unless explicitly requested during package generation.

---

# 23. Canonical Naming Conventions

Version 1.0 adopts the following naming conventions.

---

## JSON filenames

```text
lowercase
kebab-case
plural
```

Examples:

```text
prayers.json
liturgical-items.json
media-assets.json
```

---

## JSON Properties

Properties SHALL use:

```text
camelCase
```

Example:

```json
effectiveMelodyId
```

---

## Directory Names

Directory names SHALL use:

```text
lowercase
```

Examples:

```text
content
indexes
media
```

---

## Identifier Properties

Entity reference properties SHOULD end with:

```text
Id
```

Examples:

```text
textId
qoloId
melodyId
locationId
```

Collection properties SHOULD end with:

```text
Ids
```

Example:

```text
textIds
```

This naming convention improves readability and consistency throughout the specification.

# 24. Entity Identifiers

## 24.1 Purpose

Identifiers are the foundation of the SyriacPlatform data model.

Every relationship within an Application Package is established through stable entity identifiers.

Identifiers represent entity identity rather than presentation or ordering.

Applications SHALL use identifiers as the primary mechanism for locating and relating entities.

---

## 24.2 Identifier Source

All canonical entity identifiers originate from the Author Database.

The Build Tools SHALL preserve these identifiers during package generation.

Applications SHALL NOT generate replacement identifiers for canonical entities.

---

## 24.3 Identifier Stability

An identifier represents the conceptual identity of an entity.

As long as an entity represents the same conceptual object, its identifier SHALL remain unchanged across all package versions.

Examples include:

- Prayer
- Qolo
- Melody
- Text
- Location
- Occasion
- Day

Editing an entity does not create a new identifier.

---

## 24.4 Identifier Uniqueness

Identifiers SHALL be unique within their corresponding entity type.

No two entities of the same type may share the same identifier.

Applications may therefore safely use identifiers as primary lookup keys.

---

## 24.5 Identifier References

Relationships between entities SHALL always reference identifiers.

Example:

```text
LiturgicalItem
    └── qoloId
```

The referenced Qolo entity is retrieved from:

```text
content/qolos.json
```

Applications SHALL NOT duplicate referenced entities.

---

## 24.6 Identifier Semantics

Applications SHALL treat identifiers as opaque values.

Applications MUST NOT derive:

- ordering
- category
- chronology
- hierarchy

from identifier values.

The meaning of an entity is defined exclusively by its associated data.

---

## 24.7 Identifier Persistence

Identifiers remain stable across:

- package rebuilds
- content revisions
- application updates

unless the conceptual identity itself changes.

Deleting and recreating an entity intentionally creates a different identifier.

---

# 25. Entity References

## 25.1 General Principle

Relationships between canonical entities are represented exclusively through identifiers.

The package SHALL NOT embed complete entity definitions inside other entities.

---

## 25.2 Single References

A single relationship uses one identifier.

Example:

```json
{
    "melodyId": 42
}
```

---

## 25.3 Multiple References

A collection relationship uses an ordered identifier array.

Example:

```json
{
    "textIds": [
        15,
        28,
        41
    ]
}
```

The order of identifiers is significant whenever ordering is defined by the Application Content Model.

---

## 25.4 Missing References

Every referenced identifier SHALL exist within the package.

Dangling references are not permitted.

Packages containing unresolved references are not conforming.

---

## 25.5 Circular References

Circular references SHOULD be avoided whenever possible.

Where unavoidable, the Core Engine SHALL resolve them without modifying canonical content.

---

# 26. Media Resources

## 26.1 Purpose

Media resources provide binary assets referenced by canonical entities.

Media resources are not canonical entities themselves.

Instead, they are represented through MediaAsset entities stored in:

```text
content/media-assets.json
```

---

## 26.2 Media Categories

Version 1.0 defines the following standard categories.

```text
audio/
notation/
images/
documents/
video/
```

Additional categories MAY be introduced by future schema versions.

---

## 26.3 Resource Identity

Each binary resource SHALL be represented by exactly one MediaAsset entity.

The MediaAsset defines:

- identifier
- type
- filename
- relative path
- optional metadata

Applications SHALL access media through the MediaAsset definition.

---

## 26.4 Relative Paths

Media resources SHALL use package-relative paths.

Absolute filesystem paths are prohibited.

Example:

```text
audio/qolo001.mp3
```

---

## 26.5 File Names

File names SHOULD remain stable whenever the underlying media does not change.

Renaming media unnecessarily is discouraged because it complicates package comparison and caching.

---

# 27. Index Files

## 27.1 Purpose

Indexes improve runtime performance.

Indexes SHALL NOT modify the meaning of canonical content.

---

## 27.2 Optional Nature

All index files are optional.

Applications SHALL remain capable of interpreting the package without them.

---

## 27.3 Regeneration

Indexes are considered derived content.

Applications MAY regenerate indexes locally whenever necessary.

Regenerated indexes SHALL produce equivalent lookup behavior.

---

## 27.4 Search Index

Version 1.0 recommends:

```text
indexes/search-index.json
```

The internal structure of search indexes is implementation-dependent.

Future specifications may standardize additional index formats.

---

# 28. Referential Integrity

## 28.1 General Rule

Every identifier reference SHALL resolve successfully.

Packages SHALL preserve complete referential integrity.

---

## 28.2 Validation

Package validation SHALL verify:

- missing identifiers
- duplicate identifiers
- invalid references
- invalid media references

Packages failing referential integrity validation are not conforming.

---

## 28.3 Consistency

Canonical content SHALL remain internally consistent.

Applications SHALL assume validated packages satisfy all referential integrity requirements.

---

# 29. Package Validation

## 29.1 Validation Levels

Package validation consists of four logical stages.

```text
Level 1
Package Structure

↓

Level 2
Manifest Validation

↓

Level 3
Canonical Content Validation

↓

Level 4
Referential Integrity Validation
```

Every published package SHOULD successfully pass all validation levels.

---

## 29.2 Structural Validation

Structural validation verifies:

- required directories
- required files
- canonical filenames
- JSON validity

---

## 29.3 Manifest Validation

Manifest validation verifies:

- required properties
- schema version
- package profile
- compatibility declarations

---

## 29.4 Content Validation

Content validation verifies:

- entity structure
- required properties
- property types
- collection format

---

## 29.5 Integrity Validation

Integrity validation verifies:

- identifier uniqueness
- valid references
- media consistency
- profile compliance

---

# 30. Error Handling

## 30.1 Invalid Packages

Applications SHALL reject packages that violate mandatory structural requirements.

Examples include:

- invalid JSON
- missing manifest
- duplicate identifiers
- unresolved references
- unsupported schema version

---

## 30.2 Recoverable Conditions

Applications MAY recover from non-critical conditions such as:

- missing optional indexes
- unknown optional properties
- unsupported optional features

Recovery SHALL NOT alter canonical package content.

---

## 30.3 Diagnostic Reporting

Build Tools SHOULD produce human-readable validation reports.

Diagnostic reports SHOULD identify:

- validation level
- affected file
- affected entity
- error description
- suggested correction

This facilitates efficient debugging and package maintenance.

# 31. Versioning Strategy

## 31.1 Overview

The SyriacPlatform separates different kinds of version information.

Each version describes a different aspect of the package.

Version numbers SHALL NOT be used interchangeably.

Version 1.0 defines the following version categories.

| Version | Purpose |
|----------|---------|
| Schema Version | Defines the package structure |
| Package Version | Identifies a published package release |
| Content Version | Identifies the editorial content revision |
| Build Version | Identifies the Build Tools release |
| Build Revision | Identifies one specific build execution |

---

## 31.2 Schema Version

The Schema Version defines the structure of the Application Package.

Changes to the schema occur only when the package specification itself changes.

Examples include:

- introducing new required directories
- changing JSON structures
- modifying validation rules

Applications SHALL reject unsupported Schema Versions.

---

## 31.3 Package Version

The Package Version identifies the published package release.

Typical reasons for increasing the Package Version include:

- new application release
- corrected package
- regenerated distribution package

Package Version does not necessarily imply editorial changes.

---

## 31.4 Content Version

The Content Version identifies the editorial state of the package.

Content Version SHALL change whenever canonical application content changes.

Examples include:

- new prayers
- corrected texts
- updated melodies
- modified liturgical structure

---

## 31.5 Build Version

The Build Version identifies the version of the Build Tools used to generate the package.

Example:

```text
1.0.0
```

This value assists diagnostics and reproducibility.

---

## 31.6 Build Revision

The Build Revision uniquely identifies a single package generation process.

Unlike the Build Version, which identifies the software release, the Build Revision identifies one execution of that software.

Example:

```text
20260805-1842-7F3A
```

Build Revision is intended for:

- diagnostics
- traceability
- build auditing
- reproducibility

Changing the Build Revision alone does not represent a new package release.

---

# 32. Compatibility Strategy

## 32.1 Forward Compatibility

Applications SHOULD ignore unknown optional properties whenever possible.

Future schema versions should introduce new capabilities using additive changes.

---

## 32.2 Backward Compatibility

Whenever practical, future schema versions SHOULD preserve compatibility with previous versions.

Breaking changes SHOULD be introduced only when absolutely necessary.

---

## 32.3 Unsupported Schemas

Applications SHALL reject packages requiring unsupported Schema Versions.

Partial interpretation of unsupported package structures is not permitted.

---

## 32.4 Optional Features

Optional features SHALL NOT prevent applications from loading packages unless explicitly required by the Package Profile.

Unknown optional features SHALL be ignored.

---

# 33. Package Profiles

## 33.1 Purpose

Not every application requires every entity collection defined by this specification.

Package Profiles define which collections are required for a particular application category.

Profiles simplify package generation while preserving structural consistency.

---

## 33.2 Profile Principle

Every Application Package SHALL declare exactly one Package Profile.

The Package Profile determines:

- required collections
- optional collections
- expected runtime capabilities

The directory structure itself remains identical across all profiles.

---

## 33.3 Standard Profiles

Version 1.0 defines three standard profiles.

### Occasion Profile

Contains only the data required by the Occasions application.

Typical characteristics include:

- selected occasions
- associated prayers
- required hymns
- related media

---

### Shhima Profile

Contains only the data required by the Shhima application.

Typical characteristics include:

- daily offices
- daily prayer sequences
- associated hymns
- required media

---

### Full Library Profile

Contains the complete liturgical library.

This profile represents the largest canonical package defined by Version 1.0.

---

## 33.4 Profile Collection Matrix

Schema v1 defines the following collection-presence requirements:

| Collection | OCCASION | SHHIMA | FULL_LIBRARY |
|---|---|---|---|
| `entry-points.json` | Required | Required | Required |
| `occasions.json` | Required | Optional | Required |
| `prayers.json` | Required | Required | Required |
| `prayer-sequences.json` | Required | Required | Required |
| `liturgical-items.json` | Required | Required | Required |
| `texts.json` | Required | Required | Required |
| `qolos.json` | Optional | Optional | Required |
| `melodies.json` | Optional | Optional | Required |
| `qintos.json` | Optional | Optional | Required |
| `petgomos.json` | Optional | Optional | Required |
| `melody-qinto-assignments.json` | Optional | Optional | Required |

For the purposes of Profile Validation:

- **Required** means that the collection file SHALL be physically
  present in the package.
- A required collection MAY contain an empty `items` array.
- **Optional** means that the collection file MAY be absent.
- If an optional collection is present, it SHALL satisfy all applicable
  structural, reference, integrity, and semantic validation rules.
- An empty collection and an absent collection are distinct states.
- The presence of a collection does not imply that every entity in the
  package must participate in that collection's relationships.

In particular, the presence of `qintos.json` or
`melody-qinto-assignments.json` does not require every Melody to belong
to the eight-Qinto system.

Likewise, the presence of `petgomos.json` does not require every Qolo,
Melody, Text, or LiturgicalItem to have a Petgomo.
---

## 33.5 Future Profiles

Future schema versions MAY introduce additional Package Profiles.

Examples might include:

- Educational Profile
- Choir Profile
- Audio Library Profile

Applications SHALL determine profile behavior through the declared Profile rather than package naming.

---

# 34. Future Extensions

## 34.1 General Principle

Future extensions SHALL preserve the architectural principles defined by this specification.

Extensions SHOULD be additive whenever possible.

---

## 34.2 Reserved Areas

The following areas are intentionally designed for future expansion:

- additional manifest properties
- additional entity collections
- additional media categories
- additional index formats
- additional Package Profiles
- additional validation rules

---

## 34.3 Unknown Properties

Applications SHOULD ignore unknown properties unless they are explicitly marked as required by a future Schema Version.

---

## 34.4 Deprecated Features

Future specifications MAY deprecate individual properties or structures.

Deprecated features SHOULD remain supported during an appropriate transition period whenever feasible.

---

# 35. Architectural Principles

The following principles summarize the entire Application Package Specification.

---

## Principle 1

The Application Package is the canonical runtime representation of application content.

---

## Principle 2

The Author Database is never accessed by runtime applications.

---

## Principle 3

The Build Tools are solely responsible for transforming editorial data into canonical runtime content.

---

## Principle 4

Applications consume package content through the Core Engine.

---

## Principle 5

Canonical entities are immutable.

---

## Principle 6

Relationships are represented through stable identifiers.

---

## Principle 7

Derived content never replaces canonical content.

---

## Principle 8

Every package conforms to one declared Schema Version.

---

## Principle 9

Packages remain platform-independent.

---

## Principle 10

The physical package structure is identical across all applications.

Only the contained data differs.

---

# 36. Conclusion

The Application Package Specification defines the official physical representation of application content within the SyriacPlatform architecture.

Together with the Domain Model and the Application Content Model, it establishes a complete separation between:

- editorial content creation
- package generation
- runtime interpretation
- application presentation

This separation enables multiple applications, implemented on different platforms and technologies, to consume identical canonical content while maintaining consistent behavior.

The Application Package is therefore the central exchange format of the SyriacPlatform ecosystem and serves as the authoritative contract between the Build Tools, the Core Engine, and every compliant application.

Future versions of this specification may extend the package format while preserving the architectural principles established by Version 1.0.

# Appendix A — Reserved Names

## A.1 Purpose

This appendix defines the names reserved by the Application Package Specification.

Reserved names are part of the package standard and SHALL NOT be modified by implementations.

---

## A.2 Reserved Top-Level Files

The following filenames are reserved.

```text
manifest.json
```

No alternative filename is permitted.

---

## A.3 Reserved Top-Level Directories

The following directory names are reserved.

```text
content
indexes
media
```

These names SHALL remain identical across all conforming packages.

---

### A.4 Reserved Canonical Collection Files

The following collection file names are reserved for future schema
versions and are not active canonical collections in Schema v1:

```text
days.json
locations.json
groups.json
media-assets.json

These names are reserved so that future schema versions may introduce
the corresponding collections without conflicting with extension or
vendor-defined file names.

Schema v1 loaders SHALL NOT require these files.

Their absence SHALL NOT cause Profile Validation failure in Schema v1.

If support for any of these collections is introduced in a future
schema version, that schema version SHALL define:

its DTO and canonical data model,
its mapping rules,
its collection-presence requirements,
its reference and integrity rules where applicable,
and its Profile Matrix requirements.

The canonical Schema v1 collection files are defined in Section 9.2.

---

# Appendix B — Reserved Naming Conventions

## B.1 Purpose

This appendix defines the standard naming conventions used throughout the Application Package.

Implementations SHOULD follow these conventions consistently.

---

## B.2 Directory Names

Directory names use:

```text
lowercase
```

Examples:

```text
content
indexes
media
```

---

## B.3 Collection Files

Collection filenames use:

- lowercase
- plural nouns
- kebab-case

Examples:

```text
liturgical-items.json

media-assets.json

prayer-sequences.json
```

---

## B.4 JSON Properties

JSON property names use:

```text
camelCase
```

Examples:

```text
effectiveMelodyId

defaultLanguage

packageVersion

schemaVersion
```

---

## B.5 Identifier Properties

Single identifier references SHOULD end with:

```text
Id
```

Examples:

```text
textId

melodyId

qoloId

locationId
```

Identifier collections SHOULD end with:

```text
Ids
```

Example:

```text
textIds
```

---

# Appendix C — Standard Media Types

## C.1 Purpose

This appendix defines the standard media categories recognized by Version 1.0.

---

## C.2 Standard Directories

```text
audio/

notation/

images/

documents/

video/
```

Applications SHOULD preserve these directory names.

---

## C.3 Recommended Formats

### Audio

Recommended:

```text
mp3
```

Supported by future implementations:

```text
wav

flac
```

---

### Musical Notation

Recommended:

```text
pdf
```

Optional:

```text
svg
```

---

### Images

Recommended:

```text
jpg

png
```

Optional:

```text
svg

webp
```

---

### Documents

Recommended:

```text
pdf
```

Optional:

```text
txt

html
```

---

### Video

Recommended:

```text
mp4
```

Future schema versions may define additional recommendations.

---

# Appendix D — Recommended Serialization Order

## D.1 Purpose

Although JSON object ordering has no semantic meaning, deterministic property ordering improves:

- readability
- debugging
- version comparison
- source control integration

---

## D.2 Recommended Property Order

Where applicable, Build Tools SHOULD serialize entity properties using the following order.

```text
Identifier

Primary Name

Display Information

Core Properties

Relationships

Optional Metadata
```

For example:

```json
{
    "id": ...,

    "name": ...,

    "title": ...,

    "description": ...,

    "textId": ...,

    "melodyId": ...,

    "metadata": ...
}
```

This ordering is recommended but not mandatory.

---

# Appendix E — Validation Checklist

## E.1 Purpose

Every published Application Package SHOULD pass the following validation stages before distribution.

---

## E.2 Structural Validation

Verify:

- required directories
- required files
- canonical filenames
- package layout

---

## E.3 Manifest Validation

Verify:

- package identity
- schema version
- package profile
- compatibility declarations

---

## E.4 JSON Validation

Verify:

- UTF-8 encoding
- valid JSON syntax
- collection wrapper structure
- required properties

---

## E.5 Identifier Validation

Verify:

- identifier uniqueness
- identifier consistency
- valid identifier types

---

## E.6 Referential Integrity Validation

Verify:

- entity references
- media references
- collection references

No unresolved references shall remain.

---

## E.7 Profile Validation

Verify that the package satisfies all requirements of its declared Package Profile.

---

## E.8 Compatibility Validation

Verify:

- supported schema version
- supported Core Engine version
- supported package features

---

## E.9 Publication Validation

A package SHOULD be published only after successfully completing every validation stage defined by this specification.

---

# Appendix F — Guiding Principles

The following principles summarize the intent of this specification.

1. The Application Package is the canonical runtime representation.

2. Runtime applications never access the Author Database.

3. Build Tools perform all editorial transformations.

4. The Core Engine interprets canonical package content.

5. Canonical entities are immutable.

6. Stable identifiers define all relationships.

7. Derived content never replaces canonical content.

8. Applications remain platform-independent.

9. Packages remain deterministic whenever possible.

10. Structural consistency is more important than implementation convenience.

These principles should guide future revisions of this specification.

