# Application Package Examples and Reference

Version 1.0

---

# 1. Introduction

## 1.1 Purpose

This document provides practical examples and implementation references for the SyriacPlatform Application Package Specification Version 1.0.

Unlike the specification, which defines the rules governing an Application Package, this document demonstrates how those rules are applied in real package structures and JSON documents.

The examples presented throughout this document are intended to serve as implementation references for developers, Build Tools, validation utilities, and future applications.

---

## 1.2 Relationship to Other Documents

This document complements, but does not replace, the following official specifications:

- Domain Model v1.0
- Application Content Model v1.0
- Application Package Specification v1.0

Whenever a conflict exists between this document and the Application Package Specification, the specification shall take precedence.

---

## 1.3 Scope

This document illustrates the canonical representation of an Application Package through a complete, internally consistent example.

The examples include:

- complete package structure
- package manifest
- canonical content collections
- entity relationships
- media references
- runtime loading sequence

The examples are intentionally simplified to improve readability while remaining fully compliant with Version 1.0 of the specification.

---

## 1.4 Example Philosophy

The examples contained in this document represent a fictional demonstration package.

They are not intended to model the complete liturgical library.

Instead, they illustrate how canonical entities reference one another and how an application consumes package content through the Core Engine.

Every identifier, filename, and relationship has been selected to demonstrate the architectural principles defined by the SyriacPlatform specifications.

---

# 2. Example Package

## 2.1 Overview

The remainder of this document uses a single demonstration package.

The package represents a simplified Occasions application containing:

- one occasion
- two prayers
- one prayer sequence
- several liturgical items
- one hymn
- one melody
- one text
- associated media resources

All JSON files reference one another using stable identifiers.

Together, these files form one complete Application Package.

---

## 2.2 Example Package Structure

The demonstration package has the following directory layout.

```text
OccasionsDemo.syrpkg

├── manifest.json
│
├── content
│   ├── entry-points.json
│   ├── occasions.json
│   ├── prayers.json
│   ├── prayer-sequences.json
│   ├── liturgical-items.json
│   ├── qolos.json
│   ├── melodies.json
│   ├── texts.json
│   └── media-assets.json
│
├── indexes
│   └── search-index.json
│
└── media
    ├── audio
    ├── notation
    ├── images
    └── documents
```

This package will be used consistently throughout the remainder of this document.

Each subsequent section explains one file from this package and demonstrates how it relates to the others.

# 3. Example manifest.json

## 3.1 Purpose

The manifest is the entry point of every Application Package.

It provides package identity, version information, compatibility requirements, profile declaration, and build metadata.

Every Application Package SHALL contain exactly one manifest file located at the package root.

The following example demonstrates a complete manifest for the demonstration package introduced in this document.

---

## 3.2 Example

```json
{
  "packageId": "demo.occasions",
  "packageName": "Occasions Demo Package",

  "schemaVersion": "1.0",
  "packageVersion": "1.0.0",
  "contentVersion": "1.0.0",

  "application": {
    "id": "occasions",
    "name": "Occasions",
    "platform": "SyriacPlatform",
    "defaultLanguage": "en"
  },

  "profile": "Occasion",

  "build": {
    "generatedAt": "2026-08-10T14:30:00Z",
    "buildTool": "SyriacPlatform Build Tools",
    "buildVersion": "1.0.0",
    "buildRevision": "20260810-1430-001"
  },

  "compatibility": {
    "minimumCoreVersion": "1.0.0",
    "targetSchemaVersion": "1.0",
    "supportedFeatures": [
      "canonical-content",
      "media-assets",
      "search-index"
    ]
  }
}
```

---

## 3.3 Discussion

This example intentionally contains every major section defined by the Application Package Specification.

The values themselves are illustrative and are not intended to represent production packages.

The package declares:

- its identity
- its package versions
- the target application
- the package profile
- build information
- compatibility requirements

Every subsequent example in this document assumes this manifest.

---

## 3.4 Notes

Several observations can be made from this example.

### Package Identity

The package is uniquely identified by the value:

```text
demo.occasions
```

This identifier remains constant for the lifetime of the package.

---

### Package Profile

The declared profile is:

```text
Occasion
```

This informs the Core Engine which collections are expected to exist.

---

### Build Metadata

The build section records the tool responsible for generating the package together with the build version and the unique build revision.

This information is intended for diagnostics and traceability.

---

### Compatibility

The compatibility section allows applications to verify that the package can be safely interpreted before any content is loaded.

Applications should validate compatibility before reading any canonical collection.

# 4. Example entry-points.json

## 4.1 Purpose

The `entry-points.json` file defines the logical entry points into an Application Package.

Rather than scanning the entire package for available content, the Core Engine begins by loading this collection.

Each entry point represents a user-visible starting location within the application.

Different Package Profiles may define different types of entry points while preserving the same overall structure.

---

## 4.2 Example

```json
{
  "items": [
    {
      "id": "ENTRY-001",
      "name": "Nativity",
      "type": "occasion",
      "targetId": "OCC-001",
      "default": true
    }
  ]
}
```

---

## 4.3 Discussion

This demonstration package contains a single entry point.

The entry point represents the **Nativity** occasion and directs the Core Engine to the corresponding Occasion entity.

The value:

```text
targetId = OCC-001
```

indicates that loading this entry point should continue by loading the Occasion whose identifier is `OCC-001`.

---

## 4.4 Runtime Sequence

When the package is opened, the runtime sequence is as follows.

```text
Application
      │
      ▼
manifest.json
      │
      ▼
entry-points.json
      │
      ▼
ENTRY-001
      │
      ▼
OCC-001
```

The Core Engine does not yet load prayers, hymns, texts, or media.

Those objects are discovered progressively as additional entities are resolved.

---

## 4.5 Notes

The following observations apply to this example.

### Stable Identifier

The entry point itself has its own stable identifier.

```text
ENTRY-001
```

This identifier uniquely identifies the entry point and is independent of the target entity.

---

### Target Entity

The target is referenced only by identifier.

```text
targetId
```

No embedded Occasion object appears inside the entry point.

---

### Entry Point Type

The value:

```text
occasion
```

identifies the category of content referenced by the entry point.

Future Package Profiles may define additional entry point types while preserving the same structure.

---

### Default Entry Point

The property:

```text
"default": true
```

indicates that this is the preferred starting point when the package is opened.

Packages containing multiple entry points may designate one default entry point.

---

## 4.6 Relationship to Other Collections

The `entry-points.json` collection references the following collection.

```text
occasions.json
```

The next example demonstrates the referenced Occasion entity and shows how package navigation continues beyond the entry point.

# 5. Example occasions.json

## 5.1 Purpose

The `occasions.json` collection defines the liturgical occasions contained within an Application Package.

An Occasion represents a logical celebration or event that may contain one or more prayers, each represented by its own Prayer Sequence.

The Occasion itself does not contain liturgical content directly.

Instead, it provides the entry point into the appropriate prayer sequences.

---

## 5.2 Example

```json
{
  "items": [
    {
      "id": "OCC-001",
      "name": "Nativity",
      "description": "Demonstration occasion used throughout this reference document.",
      "prayerSequenceIds": [
        "PS-001",
        "PS-002"
      ]
    }
  ]
}
```

---

## 5.3 Discussion

This demonstration package contains a single Occasion.

The Occasion represents the Nativity celebration and references two Prayer Sequences.

Rather than embedding prayers directly inside the Occasion object, the package references the corresponding Prayer Sequence identifiers.

This separation allows the same Prayer Sequence to be reused whenever appropriate.

---

## 5.4 Runtime Sequence

After resolving the entry point, the Core Engine continues as follows.

```text
ENTRY-001
      │
      ▼
OCC-001
      │
      ▼
PS-001

PS-002
```

At this stage, no liturgical items have been loaded.

Only the available Prayer Sequences have been identified.

---

## 5.5 Notes

### Occasion Identity

Each Occasion possesses its own stable identifier.

```text
OCC-001
```

This identifier is used throughout the package wherever the Occasion is referenced.

---

### Human-Readable Name

The `name` property provides the primary display name of the Occasion.

Applications may display this value directly or replace it with localized resources if supported.

---

### Description

The optional `description` property provides explanatory information intended for developers or users.

Its presence does not affect runtime behavior.

---

### Prayer Sequence References

The property

```text
prayerSequenceIds
```

contains only identifiers.

No Prayer Sequence objects are embedded inside the Occasion.

This preserves the independence of canonical entities.

---

## 5.6 Relationship to Other Collections

This collection is referenced by:

```text
entry-points.json
```

and references:

```text
prayer-sequences.json
```

The next example introduces the Prayer Sequence collection and demonstrates how the runtime continues from the selected Occasion to the ordered sequence of liturgical items.

# 6. Example prayers.json

## 6.1 Purpose

The `prayers.json` collection defines the stable identities of the prayers available within an Application Package.

A Prayer identifies a recognizable liturgical prayer or office.

It does not define the ordered liturgical content used in a particular occasion, day, or application context.

That contextual realization is represented separately by a Prayer Sequence.

---

## 6.2 Example

```json
{
  "items": [
    {
      "id": "PR-001",
      "name": "Evening Prayer",
      "description": "The evening prayer associated with the demonstration occasion."
    },
    {
      "id": "PR-002",
      "name": "Morning Prayer",
      "description": "The morning prayer associated with the demonstration occasion."
    }
  ]
}
```

---

## 6.3 Discussion

The demonstration package contains two Prayer entities:

```text
PR-001 — Evening Prayer

PR-002 — Morning Prayer
```

These entities establish only the identity and display information of each prayer.

They do not contain:

- liturgical items
- sequence positions
- occasion-specific content
- effective melodies
- effective qintos
- media references

Those details belong to other canonical entities.

---

## 6.4 Prayer Identity

A Prayer remains conceptually stable even when its content differs according to occasion, day, season, or application context.

For example, the identity:

```text
PR-001
```

always represents the Evening Prayer.

However, its realization for the Nativity occasion is represented by a separate Prayer Sequence.

```text
PR-001
    │
    ▼
PS-001
```

Another occasion could provide a different Prayer Sequence for the same Prayer identity.

```text
PR-001
    ├──► PS-001 — Nativity realization
    │
    └──► PS-101 — Another occasion realization
```

The demonstration package contains only the first realization.

---

## 6.5 Notes

### Stable Identifier

Each Prayer has an identifier originating from the Author Database and preserved by the Build Tools.

In this reference example, readable identifiers are used to make relationships easier to follow.

```text
PR-001

PR-002
```

Runtime applications SHALL treat these identifiers as opaque values.

---

### Name

The `name` property provides the primary human-readable name of the Prayer.

It identifies the prayer independently of any particular Occasion or Prayer Sequence.

---

### Description

The optional `description` property provides explanatory information.

It does not participate in prayer resolution or runtime ordering.

---

### No Embedded Content

A Prayer object does not embed its liturgical items.

The following structure would therefore be incorrect:

```json
{
  "id": "PR-001",
  "name": "Evening Prayer",
  "items": [
    {
      "id": "LI-001"
    }
  ]
}
```

The ordered items belong to a Prayer Sequence and are stored separately.

---

## 6.6 Relationship to Other Collections

The `prayers.json` collection is referenced by:

```text
prayer-sequences.json
```

The current relationship chain is now:

```text
entry-points.json
        │
        ▼
occasions.json
        │
        ▼
prayer-sequences.json
        │
        ▼
prayers.json
```

The next example defines `prayer-sequences.json` and shows how each contextual sequence connects a Prayer identity to its ordered liturgical items.

# 7. Example prayer-sequences.json

## 7.1 Purpose

The `prayer-sequences.json` collection defines the contextual realization of a Prayer.

A Prayer Sequence connects a Prayer identity to an ordered collection of Liturgical Items within a particular application context.

Unlike the Prayer entity, which represents a stable liturgical identity, a Prayer Sequence represents one specific realization of that Prayer.

Different occasions, seasons, or applications may define different Prayer Sequences for the same Prayer.

---

## 7.2 Example

```json
{
  "items": [
    {
      "id": "PS-001",
      "prayerId": "PR-001",
      "liturgicalItemIds": [
        "LI-001",
        "LI-002",
        "LI-003"
      ]
    },
    {
      "id": "PS-002",
      "prayerId": "PR-002",
      "liturgicalItemIds": [
        "LI-004",
        "LI-005"
      ]
    }
  ]
}
```

---

## 7.3 Discussion

The demonstration package defines two Prayer Sequences.

The first sequence realizes the Evening Prayer.

```text
PS-001
        │
        ▼
PR-001
```

The second sequence realizes the Morning Prayer.

```text
PS-002
        │
        ▼
PR-002
```

Each Prayer Sequence references an ordered list of Liturgical Items.

The sequence itself contains no embedded liturgical content.

---

## 7.4 Ordered Content

The property

```text
liturgicalItemIds
```

defines the execution order of the prayer.

For example,

```text
LI-001

LI-002

LI-003
```

means that the Core Engine presents the items in exactly this order.

Applications SHALL preserve the order defined by the Prayer Sequence.

---

## 7.5 Runtime Sequence

The runtime flow now continues as follows.

```text
ENTRY-001
        │
        ▼
OCC-001
        │
        ▼
PS-001
        │
        ▼
LI-001
        │
        ▼
LI-002
        │
        ▼
LI-003
```

Only after resolving each Liturgical Item does the Core Engine discover whether it represents:

- a Text
- a Qolo
- a Reading
- a Petgomo
- another supported liturgical entity

---

## 7.6 Notes

### Prayer Reference

Every Prayer Sequence references exactly one Prayer.

```text
prayerId
```

This relationship identifies the liturgical identity realized by the sequence.

---

### Ordered References

The Prayer Sequence references Liturgical Items only by identifier.

No Liturgical Item objects are embedded.

This preserves canonical independence between entities.

---

### Sequence Identity

The identifier

```text
PS-001
```

identifies one specific realization of a Prayer.

It does not identify the Prayer itself.

Multiple Prayer Sequences may reference the same Prayer whenever different contextual realizations are required.

---

### Context Independence

The Prayer entity remains reusable.

Only the Prayer Sequence changes according to:

- occasion
- season
- application profile
- future extensions

---

## 7.7 Relationship to Other Collections

This collection references:

```text
prayers.json

liturgical-items.json
```

and is referenced by:

```text
occasions.json
```

The relationship chain is now:

```text
entry-points.json
        │
        ▼
occasions.json
        │
        ▼
prayer-sequences.json
        │
        ├──► prayers.json
        │
        ▼
liturgical-items.json
```

The next example introduces the `liturgical-items.json` collection, where the runtime begins resolving the actual liturgical content presented to the user.

# 8. Example liturgical-items.json

## 8.1 Purpose

The `liturgical-items.json` collection defines the ordered building blocks of a Prayer Sequence.

Each Liturgical Item represents one logical step within the liturgical flow.

A Liturgical Item does not contain the canonical content itself.

Instead, it references the appropriate canonical entity, such as a Text, Qolo, Reading, or Petgomo.

This design allows canonical entities to remain reusable across multiple Prayer Sequences.

---

## 8.2 Example

```json
{
  "items": [
    {
      "id": "LI-001",
      "type": "text",
      "targetId": "TXT-001"
    },
    {
      "id": "LI-002",
      "type": "qolo",
      "targetId": "QOL-001",
      "effectiveMelodyId": "MEL-001"
    },
    {
      "id": "LI-003",
      "type": "text",
      "targetId": "TXT-002"
    },
    {
      "id": "LI-004",
      "type": "text",
      "targetId": "TXT-003"
    },
    {
      "id": "LI-005",
      "type": "qolo",
      "targetId": "QOL-002",
      "effectiveMelodyId": "MEL-002"
    }
  ]
}
```

---

## 8.3 Discussion

This demonstration package contains five Liturgical Items.

Together they define the ordered content presented during the two Prayer Sequences.

Each Liturgical Item references one canonical entity through its `targetId`.

No canonical content is embedded within the Liturgical Item itself.

---

## 8.4 Item Types

Version 1.0 supports multiple liturgical item types.

This example demonstrates two of them.

### Text Item

```text
LI-001
        │
        ▼
TXT-001
```

The referenced Text entity provides the canonical textual content.

---

### Qolo Item

```text
LI-002
        │
        ▼
QOL-001
        │
        ▼
MEL-001
```

The Liturgical Item references the Qolo identity.

The melody to be used at runtime is provided separately through the `effectiveMelodyId`.

---

## 8.5 Effective Melody Resolution

The demonstration package explicitly declares:

```text
effectiveMelodyId
```

This value represents the melody that shall be used for this occurrence of the Qolo.

The Build Tools are responsible for resolving this value during package generation.

Runtime applications do not calculate or infer the effective melody.

---

## 8.6 Runtime Resolution

When the Core Engine encounters:

```text
LI-002
```

the following resolution sequence occurs.

```text
LI-002
        │
        ▼
QOL-001
        │
        ▼
MEL-001
```

The runtime then retrieves the corresponding canonical entities from their respective collections.

---

## 8.7 Notes

### Stable Identifier

Each Liturgical Item has its own stable identifier.

```text
LI-001

LI-002

LI-003
```

The identifier represents the occurrence of the item within the package rather than the canonical content itself.

---

### Item Type

The property

```text
type
```

determines how the Core Engine interprets the referenced entity.

Applications SHALL use this value to select the appropriate rendering strategy.

---

### Target Reference

The property

```text
targetId
```

always references one canonical entity.

The referenced entity depends on the value of `type`.

---

### Effective Properties

Properties beginning with the prefix:

```text
effective
```

represent values already resolved by the Build Tools.

Applications SHALL use these values directly without attempting additional editorial resolution.

---

## 8.8 Relationship to Other Collections

This collection is referenced by:

```text
prayer-sequences.json
```

and references:

```text
texts.json

qolos.json

melodies.json
```

The runtime relationship chain has now expanded to:

```text
entry-points.json
        │
        ▼
occasions.json
        │
        ▼
prayer-sequences.json
        │
        ▼
liturgical-items.json
        │
        ├──► texts.json
        │
        └──► qolos.json
                   │
                   ▼
            melodies.json
```

The next examples define the canonical Text, Qolo, and Melody collections referenced by these Liturgical Items.

# 9. Example texts.json

## 9.1 Purpose

The `texts.json` collection defines the canonical textual content contained within an Application Package.

Each Text represents one independent textual entity.

Texts are canonical objects and may be referenced by multiple Liturgical Items throughout the package.

A Text contains only the textual content itself.

It does not contain ordering information, melody information, or contextual placement.

---

## 9.2 Example

```json
{
  "items": [
    {
      "id": "TXT-001",
      "title": "Opening Prayer",
      "language": "en",
      "content": "In the name of the Father, and of the Son, and of the Holy Spirit."
    },
    {
      "id": "TXT-002",
      "title": "Closing Prayer",
      "language": "en",
      "content": "May peace remain with us forever."
    },
    {
      "id": "TXT-003",
      "title": "Morning Introduction",
      "language": "en",
      "content": "Let us begin the morning prayer with thanksgiving."
    }
  ]
}
```

---

## 9.3 Discussion

The demonstration package contains three canonical Text entities.

Each Text is an independent reusable object.

Multiple Liturgical Items may reference the same Text whenever identical wording is required.

No duplication of canonical textual content is necessary.

---

## 9.4 Runtime Resolution

When the Core Engine encounters:

```text
LI-001
```

it resolves:

```text
LI-001
        │
        ▼
TXT-001
```

The Text entity supplies the textual content presented to the user.

The Liturgical Item itself contains no textual information.

---

## 9.5 Notes

### Stable Identifier

Each Text possesses its own canonical identifier.

```text
TXT-001

TXT-002

TXT-003
```

Applications use these identifiers only for entity resolution.

---

### Title

The `title` property provides a short descriptive label for the Text.

The title is intended for identification and development purposes.

Applications may choose whether or not to display it.

---

### Language

The `language` property identifies the language of the canonical text.

Example:

```text
en
```

Future packages may contain texts in additional languages.

---

### Content

The `content` property contains the canonical textual content.

Formatting conventions, typography, or platform-specific rendering remain the responsibility of the runtime application.

---

### Canonical Reuse

A Text entity may be referenced by multiple Liturgical Items.

For example:

```text
LI-001
        │
        ▼
TXT-001

LI-010
        │
        ▼
TXT-001
```

This avoids duplication while preserving a single canonical source.

---

## 9.6 Relationship to Other Collections

This collection is referenced by:

```text
liturgical-items.json
```

The current relationship chain becomes:

```text
entry-points.json
        │
        ▼
occasions.json
        │
        ▼
prayer-sequences.json
        │
        ▼
liturgical-items.json
        │
        ▼
texts.json
```

The next example introduces the `qolos.json` collection and demonstrates how hymn identities are represented independently from their melodies and textual content.

# 10. Example qolos.json

## 10.1 Purpose

The `qolos.json` collection defines the canonical hymn identities contained within an Application Package.

A Qolo represents the identity of a liturgical hymn.

It does not define a fixed melody, textual realization, or contextual performance.

Those aspects are resolved separately by the Build Tools and represented by the appropriate canonical entities.

This separation allows the same Qolo to appear in multiple contexts while preserving a single canonical identity.

---

## 10.2 Example

```json
{
  "items": [
    {
      "id": "QOL-001",
      "name": "Hymn of the Nativity",
      "textId": "TXT-001"
    },
    {
      "id": "QOL-002",
      "name": "Morning Hymn",
      "textId": "TXT-003"
    }
  ]
}
```

---

## 10.3 Discussion

The demonstration package contains two canonical Qolo entities.

Each Qolo defines only the hymn identity together with its canonical text.

No melody information appears inside the Qolo.

Likewise, no runtime performance information is stored within the hymn definition.

---

## 10.4 Runtime Resolution

When the Core Engine resolves:

```text
LI-002
```

the following sequence occurs.

```text
LI-002
        │
        ▼
QOL-001
        │
        ▼
TXT-001
```

The melody is **not** obtained from the Qolo itself.

Instead, it is provided separately through the Liturgical Item.

```text
LI-002
        │
        ├──► QOL-001
        │
        └──► MEL-001
```

---

## 10.5 Notes

### Stable Identifier

Each Qolo has one canonical identifier.

```text
QOL-001

QOL-002
```

The identifier represents the hymn identity and remains stable throughout the package.

---

### Canonical Text

The property

```text
textId
```

references the canonical hymn text.

The Qolo does not embed the textual content directly.

---

### Melody Independence

A Qolo does not define its melody.

Different Liturgical Items may reference the same Qolo while specifying different effective melodies.

For example:

```text
LI-002
        │
        ├──► QOL-001
        └──► MEL-001

LI-025
        │
        ├──► QOL-001
        └──► MEL-008
```

This allows one hymn identity to be performed using different melodies in different liturgical contexts.

---

### Canonical Reuse

The same Qolo may appear in multiple Prayer Sequences without duplication.

Only the contextual information changes.

The canonical hymn identity remains unchanged.

---

## 10.6 Relationship to Other Collections

This collection is referenced by:

```text
liturgical-items.json
```

and references:

```text
texts.json
```

The melody relationship is established separately through:

```text
effectiveMelodyId
```

contained within the Liturgical Item.

The next example introduces the `melodies.json` collection and demonstrates how canonical melodies are represented independently from hymn identities.

# 11. Example melodies.json

## 11.1 Purpose

The `melodies.json` collection defines the canonical melody identities contained within an Application Package.

A Melody represents a musical composition or tune.

It is independent of any particular hymn, prayer, or liturgical context.

Melodies are canonical entities that may be referenced by multiple Liturgical Items throughout the package.

Media resources associated with a Melody are referenced separately through Media Assets.

---

## 11.2 Example

```json
{
  "items": [
    {
      "id": "MEL-001",
      "name": "Nativity Melody",
      "audioAssetId": "MED-001",
      "notationAssetId": "MED-002"
    },
    {
      "id": "MEL-002",
      "name": "Morning Melody",
      "audioAssetId": "MED-003",
      "notationAssetId": "MED-004"
    }
  ]
}
```

---

## 11.3 Discussion

The demonstration package contains two canonical Melody entities.

Each Melody defines the musical identity independently of any hymn or prayer.

The melody does not contain:

- hymn information
- prayer information
- occasion information
- runtime ordering

Instead, it references the Media Assets required to represent the melody.

---

## 11.4 Runtime Resolution

When the Core Engine encounters:

```text
effectiveMelodyId = MEL-001
```

it resolves:

```text
MEL-001
        │
        ├──► MED-001 (Audio)
        │
        └──► MED-002 (Notation)
```

The actual media files are obtained through the Media Asset collection.

---

## 11.5 Notes

### Stable Identifier

Each Melody has its own canonical identifier.

```text
MEL-001

MEL-002
```

Applications use these identifiers only for canonical resolution.

---

### Melody Identity

The `name` property identifies the melody independently of the hymn using it.

The same Melody may therefore be referenced by multiple Liturgical Items.

---

### Media References

The Melody references media indirectly through Media Assets.

For example:

```text
audioAssetId

notationAssetId
```

No file paths are stored directly inside the Melody.

---

### Canonical Reuse

One Melody may accompany many different hymns.

Likewise, one hymn may be performed using different melodies depending on context.

The canonical entities remain independent.

---

## 11.6 Relationship to Other Collections

This collection is referenced by:

```text
liturgical-items.json
```

through:

```text
effectiveMelodyId
```

It also references:

```text
media-assets.json
```

through:

```text
audioAssetId

notationAssetId
```

The current relationship chain becomes:

```text
liturgical-items.json
        │
        ▼
melodies.json
        │
        ▼
media-assets.json
```

The next example introduces the `media-assets.json` collection and demonstrates how physical media resources are represented independently from canonical musical entities.

# 12. Example media-assets.json

## 12.1 Purpose

The `media-assets.json` collection defines the physical media resources contained within an Application Package.

A Media Asset represents a single physical resource stored within the package.

Canonical entities, such as Melodies, reference Media Assets rather than physical file paths.

This separation isolates canonical content from the physical organization of package resources.

---

## 12.2 Example

```json
{
  "items": [
    {
      "id": "MED-001",
      "type": "audio",
      "file": "audio/nativity-melody.mp3",
      "format": "mp3"
    },
    {
      "id": "MED-002",
      "type": "notation",
      "file": "notation/nativity-melody.pdf",
      "format": "pdf"
    },
    {
      "id": "MED-003",
      "type": "audio",
      "file": "audio/morning-melody.mp3",
      "format": "mp3"
    },
    {
      "id": "MED-004",
      "type": "notation",
      "file": "notation/morning-melody.pdf",
      "format": "pdf"
    }
  ]
}
```

---

## 12.3 Discussion

The demonstration package contains four Media Assets.

Each Media Asset represents one physical file contained within the package.

The asset itself carries no liturgical meaning.

Its purpose is simply to identify and locate a physical resource.

---

## 12.4 Runtime Resolution

When the Core Engine resolves:

```text
MEL-001
```

it retrieves the associated Media Assets.

```text
MEL-001
        │
        ├──► MED-001
        │          │
        │          ▼
        │   audio/nativity-melody.mp3
        │
        └──► MED-002
                   │
                   ▼
          notation/nativity-melody.pdf
```

The runtime accesses the media file using the package-relative path stored in the Media Asset.

---

## 12.5 Notes

### Stable Identifier

Each Media Asset possesses its own canonical identifier.

```text
MED-001

MED-002

MED-003

MED-004
```

Applications resolve media exclusively through these identifiers.

---

### Asset Type

The `type` property identifies the category of the media resource.

Examples include:

```text
audio

notation
```

Future schema versions may define additional media categories.

---

### File Path

The `file` property specifies the package-relative location of the resource.

Examples:

```text
audio/nativity-melody.mp3

notation/nativity-melody.pdf
```

Absolute paths SHALL NOT be used.

---

### File Format

The `format` property specifies the physical file format.

Examples:

```text
mp3

pdf
```

Applications may use this information to determine the appropriate rendering or playback mechanism.

---

## 12.6 Relationship to Other Collections

This collection is referenced by:

```text
melodies.json
```

through:

```text
audioAssetId

notationAssetId
```

The complete runtime chain is now:

```text
entry-points.json
        │
        ▼
occasions.json
        │
        ▼
prayer-sequences.json
        │
        ▼
liturgical-items.json
        │
        ├──► texts.json
        │
        └──► qolos.json
                   │
                   ▼
            melodies.json
                   │
                   ▼
          media-assets.json
                   │
                   ▼
         Physical Media Files
```

This completes the canonical runtime resolution chain demonstrated throughout this reference package.

# 13. Example search-index.json

## 13.1 Purpose

The `search-index.json` file provides a package-local index for fast runtime search.

It is generated by the Build Tools from the canonical entities contained in the package.

The search index does not define canonical content.

Instead, each search entry points back to an entity stored in one of the collections under `content/`.

Applications use the index to locate matching entity identifiers and then retrieve the complete entity from its canonical collection.

---

## 13.2 Example

```json
{
  "items": [
    {
      "id": "SEARCH-001",
      "entityType": "occasion",
      "entityId": "OCC-001",
      "label": "Nativity",
      "terms": [
        "Nativity"
      ],
      "normalizedTerms": [
        "nativity"
      ]
    },
    {
      "id": "SEARCH-002",
      "entityType": "prayer",
      "entityId": "PR-001",
      "label": "Evening Prayer",
      "terms": [
        "Evening Prayer",
        "Evening"
      ],
      "normalizedTerms": [
        "evening prayer",
        "evening"
      ]
    },
    {
      "id": "SEARCH-003",
      "entityType": "prayer",
      "entityId": "PR-002",
      "label": "Morning Prayer",
      "terms": [
        "Morning Prayer",
        "Morning"
      ],
      "normalizedTerms": [
        "morning prayer",
        "morning"
      ]
    },
    {
      "id": "SEARCH-004",
      "entityType": "text",
      "entityId": "TXT-001",
      "label": "Opening Prayer",
      "terms": [
        "Opening Prayer",
        "Father",
        "Son",
        "Holy Spirit"
      ],
      "normalizedTerms": [
        "opening prayer",
        "father",
        "son",
        "holy spirit"
      ]
    },
    {
      "id": "SEARCH-005",
      "entityType": "text",
      "entityId": "TXT-002",
      "label": "Closing Prayer",
      "terms": [
        "Closing Prayer",
        "Peace"
      ],
      "normalizedTerms": [
        "closing prayer",
        "peace"
      ]
    },
    {
      "id": "SEARCH-006",
      "entityType": "text",
      "entityId": "TXT-003",
      "label": "Morning Introduction",
      "terms": [
        "Morning Introduction",
        "Thanksgiving"
      ],
      "normalizedTerms": [
        "morning introduction",
        "thanksgiving"
      ]
    },
    {
      "id": "SEARCH-007",
      "entityType": "qolo",
      "entityId": "QOL-001",
      "label": "Hymn of the Nativity",
      "terms": [
        "Hymn of the Nativity",
        "Nativity Hymn"
      ],
      "normalizedTerms": [
        "hymn of the nativity",
        "nativity hymn"
      ]
    },
    {
      "id": "SEARCH-008",
      "entityType": "qolo",
      "entityId": "QOL-002",
      "label": "Morning Hymn",
      "terms": [
        "Morning Hymn",
        "Hymn"
      ],
      "normalizedTerms": [
        "morning hymn",
        "hymn"
      ]
    },
    {
      "id": "SEARCH-009",
      "entityType": "melody",
      "entityId": "MEL-001",
      "label": "Nativity Melody",
      "terms": [
        "Nativity Melody",
        "Nativity"
      ],
      "normalizedTerms": [
        "nativity melody",
        "nativity"
      ]
    },
    {
      "id": "SEARCH-010",
      "entityType": "melody",
      "entityId": "MEL-002",
      "label": "Morning Melody",
      "terms": [
        "Morning Melody",
        "Morning"
      ],
      "normalizedTerms": [
        "morning melody",
        "morning"
      ]
    }
  ]
}
```

---

## 13.3 Discussion

The demonstration search index contains entries for several searchable entity types:

```text
occasion

prayer

text

qolo

melody
```

Each search entry identifies one canonical entity through the combination of:

```text
entityType

entityId
```

The index may contain multiple searchable terms for the same entity.

This allows users to locate an entity using its primary label or other relevant words generated during the build process.

---

## 13.4 Search Entry Identity

Each index entry has its own identifier.

Example:

```text
SEARCH-001
```

This identifier represents the search entry itself.

It does not replace the identifier of the canonical entity.

For example:

```text
SEARCH-001
        │
        ▼
OCC-001
```

`SEARCH-001` identifies the index record.

`OCC-001` identifies the canonical Occasion.

---

## 13.5 Entity Type

The `entityType` property identifies the canonical collection containing the referenced entity.

For example:

```json
{
  "entityType": "occasion",
  "entityId": "OCC-001"
}
```

The Core Engine resolves this result through:

```text
content/occasions.json
```

Another example:

```json
{
  "entityType": "qolo",
  "entityId": "QOL-001"
}
```

The Core Engine resolves this result through:

```text
content/qolos.json
```

The `entityType` value therefore determines which canonical collection shall be queried.

---

## 13.6 Entity Identifier

The `entityId` property references the canonical entity represented by the search entry.

It must correspond to an existing identifier in the appropriate content collection.

For example:

```text
entityType = melody

entityId = MEL-001
```

must resolve to:

```text
content/melodies.json
        │
        ▼
MEL-001
```

A search entry with a missing or incompatible entity reference is invalid.

---

## 13.7 Display Label

The `label` property provides the short value displayed in search results.

Example:

```text
Hymn of the Nativity
```

The label allows the application to present a useful result before loading the complete canonical entity.

It is a derived display value and does not become the canonical name of the referenced entity.

The canonical collection remains the authoritative source.

---

## 13.8 Search Terms

The `terms` property contains searchable words or phrases associated with the entity.

Example:

```json
"terms": [
  "Hymn of the Nativity",
  "Nativity Hymn"
]
```

Search terms may be generated from:

- canonical names
- titles
- selected textual words
- approved alternate expressions
- other searchable metadata

The Build Tools determine which terms are included according to the package generation rules.

---

## 13.9 Normalized Terms

The `normalizedTerms` property contains search-ready forms of the values stored in `terms`.

Example:

```json
"terms": [
  "Nativity Melody"
],
"normalizedTerms": [
  "nativity melody"
]
```

Normalization allows the runtime application to compare user input against consistent search values.

In this demonstration package, normalization converts text to lowercase.

Real packages may apply additional language-specific normalization defined by the Build Tools.

Runtime applications shall not reinterpret or editorially modify canonical content while performing this normalization.

---

## 13.10 Runtime Search Flow

When the user searches for:

```text
Nativity
```

the application normalizes the input:

```text
nativity
```

It then compares the value against the entries in:

```text
normalizedTerms
```

The search index may return several matches:

```text
SEARCH-001
        │
        ▼
OCC-001

SEARCH-007
        │
        ▼
QOL-001

SEARCH-009
        │
        ▼
MEL-001
```

The application may display their labels and entity types:

```text
Nativity
Occasion

Hymn of the Nativity
Qolo

Nativity Melody
Melody
```

When the user selects one result, the Core Engine loads the corresponding canonical entity.

For example:

```text
Search Result
        │
        ▼
entityType = qolo
entityId   = QOL-001
        │
        ▼
content/qolos.json
        │
        ▼
QOL-001
```

---

## 13.11 Derived Nature of the Index

The search index is generated from canonical package content.

It is not maintained as an independent editorial source.

The generation flow is:

```text
Author Database
        │
        ▼
Build Tools
        │
        ├──► Canonical Content Collections
        │
        └──► search-index.json
```

If the index is removed, it can be generated again from the canonical content and the applicable build rules.

Changes to canonical content must therefore be made in the Author Database rather than directly inside the generated search index.

---

## 13.12 Canonical Content Remains Authoritative

The search index may repeat limited derived values, such as:

```text
label

terms

normalizedTerms
```

These values exist only to support search behavior.

The index shall not be treated as the authoritative source for:

- entity names
- descriptions
- textual content
- prayer order
- liturgical relationships
- melody relationships
- media references

After selecting a search result, the application retrieves the canonical entity from the appropriate content collection.

---

## 13.13 Full Content Shall Not Be Duplicated

The search index should contain only the information required for locating and presenting search results.

It should not duplicate complete canonical entities.

For example, the following structure is incorrect:

```json
{
  "entityType": "text",
  "entityId": "TXT-001",
  "title": "Opening Prayer",
  "language": "en",
  "content": "In the name of the Father, and of the Son, and of the Holy Spirit."
}
```

This incorrectly turns the search index into a second source for the Text entity.

The correct search entry contains only derived search information:

```json
{
  "id": "SEARCH-004",
  "entityType": "text",
  "entityId": "TXT-001",
  "label": "Opening Prayer",
  "terms": [
    "Opening Prayer",
    "Father",
    "Son",
    "Holy Spirit"
  ],
  "normalizedTerms": [
    "opening prayer",
    "father",
    "son",
    "holy spirit"
  ]
}
```

The complete Text remains stored in:

```text
content/texts.json
```

---

## 13.14 Reference Validation

During package generation, the Build Tools shall validate every search entry.

Validation includes confirming that:

```text
entityType
```

identifies a supported canonical entity type, and that:

```text
entityId
```

exists in the corresponding collection.

For example:

```json
{
  "entityType": "occasion",
  "entityId": "MEL-001"
}
```

is invalid because `MEL-001` identifies a Melody rather than an Occasion.

The valid form is:

```json
{
  "entityType": "melody",
  "entityId": "MEL-001"
}
```

---

## 13.15 Relationship to Package Content

The search index references canonical entities stored under:

```text
content/
```

The relationship can be represented as:

```text
search-index.json
        │
        ├──► occasions.json
        │
        ├──► prayers.json
        │
        ├──► texts.json
        │
        ├──► qolos.json
        │
        └──► melodies.json
```

The search index does not participate in the canonical liturgical sequence.

It provides an alternative discovery path into the same package content.

The normal navigation path is:

```text
Entry Point
        │
        ▼
Occasion
        │
        ▼
Prayer Sequence
        │
        ▼
Liturgical Item
```

The search path is:

```text
User Query
        │
        ▼
Search Index
        │
        ▼
Canonical Entity
```

Both paths ultimately resolve entities from the canonical collections under `content/`.

# 14. Complete Runtime Walkthrough

## 14.1 Purpose

This section demonstrates how the Core Engine resolves one complete runtime path through the example package.

The walkthrough begins when the application opens the package and ends when it reaches the physical media files associated with a selected Melody.

The purpose is to connect the previous file examples into one continuous resolution flow.

---

## 14.2 Starting the Package

The application begins by opening:

```text
manifest.json
```

The manifest identifies the package and confirms that it is compatible with the Core Engine.

Relevant values include:

```text
packageId

schemaVersion

application.id

profile

compatibility
```

The runtime validates these values before loading package content.

---

## 14.3 Loading the Entry Point

After validating the manifest, the application loads:

```text
content/entry-points.json
```

The example package contains:

```text
ENTRY-001
```

with:

```text
type     = occasion

targetId = OCC-001

default  = true
```

Because this entry is marked as the default, the application may open it automatically.

The first runtime transition is:

```text
manifest.json
        │
        ▼
entry-points.json
        │
        ▼
ENTRY-001
```

---

## 14.4 Resolving the Occasion

The entry point declares:

```text
entity type = occasion

targetId    = OCC-001
```

The Core Engine therefore loads:

```text
content/occasions.json
```

and retrieves:

```text
OCC-001
```

The Occasion references two Prayer Sequences:

```text
PS-001

PS-002
```

The runtime flow becomes:

```text
ENTRY-001
        │
        ▼
OCC-001
        │
        ├──► PS-001
        └──► PS-002
```

---

## 14.5 Resolving the First Prayer Sequence

The application selects:

```text
PS-001
```

from:

```text
content/prayer-sequences.json
```

The sequence references:

```text
prayerId = PR-001
```

and the following ordered Liturgical Items:

```text
LI-001

LI-002

LI-003
```

The runtime may use `PR-001` to retrieve the stable Prayer identity from:

```text
content/prayers.json
```

The ordered content, however, is defined by the Liturgical Item references.

```text
PS-001
        │
        ├──► PR-001
        │
        ├──► LI-001
        ├──► LI-002
        └──► LI-003
```

---

## 14.6 Preserving Liturgical Order

The Liturgical Item identifiers appear in this order:

```json
[
  "LI-001",
  "LI-002",
  "LI-003"
]
```

The Core Engine preserves this order exactly.

It shall not:

- sort the identifiers alphabetically
- group the items by type
- move hymns before or after texts
- infer a new sequence from entity metadata

The order declared by the Prayer Sequence is the runtime order.

---

## 14.7 Resolving the First Text Item

The first Liturgical Item is:

```text
LI-001
```

The Core Engine retrieves it from:

```text
content/liturgical-items.json
```

Its values are:

```text
type     = text

targetId = TXT-001
```

The engine uses the type to select the appropriate canonical collection:

```text
content/texts.json
```

It then retrieves:

```text
TXT-001
```

The complete resolution path is:

```text
PS-001
        │
        ▼
LI-001
        │
        ▼
TXT-001
        │
        ▼
Opening Prayer
```

The Text entity supplies the canonical textual content displayed by the application.

---

## 14.8 Resolving the Qolo Item

The second Liturgical Item is:

```text
LI-002
```

Its values are:

```text
type              = qolo

targetId          = QOL-001

effectiveMelodyId = MEL-001
```

The Liturgical Item therefore establishes two contextual references:

```text
LI-002
        │
        ├──► QOL-001
        └──► MEL-001
```

The Core Engine does not ask the Qolo to determine which Melody shall be used.

The effective Melody has already been resolved by the Build Tools.

---

## 14.9 Resolving the Qolo Identity

Because the Liturgical Item type is:

```text
qolo
```

the Core Engine loads:

```text
content/qolos.json
```

and retrieves:

```text
QOL-001
```

The Qolo identifies the hymn and references:

```text
textId = TXT-001
```

The resulting branch is:

```text
LI-002
        │
        ▼
QOL-001
        │
        ▼
TXT-001
```

The Qolo supplies the hymn identity.

The referenced Text supplies the canonical wording.

---

## 14.10 Resolving the Effective Melody

The same Liturgical Item declares:

```text
effectiveMelodyId = MEL-001
```

The Core Engine loads:

```text
content/melodies.json
```

and retrieves:

```text
MEL-001
```

The Melody references:

```text
audioAssetId    = MED-001

notationAssetId = MED-002
```

The Melody branch is therefore:

```text
LI-002
        │
        ▼
MEL-001
        │
        ├──► MED-001
        └──► MED-002
```

---

## 14.11 Resolving the Audio Asset

The Core Engine retrieves:

```text
MED-001
```

from:

```text
content/media-assets.json
```

The asset declares:

```text
type   = audio

file   = audio/nativity-melody.mp3

format = mp3
```

The runtime combines this package-relative value with the package media root:

```text
media/
```

The resulting physical resource is:

```text
media/audio/nativity-melody.mp3
```

The complete audio path is:

```text
LI-002
        │
        ▼
MEL-001
        │
        ▼
MED-001
        │
        ▼
media/audio/nativity-melody.mp3
```

The application may now provide audio playback.

---

## 14.12 Resolving the Notation Asset

The same Melody references:

```text
MED-002
```

The asset declares:

```text
type   = notation

file   = notation/nativity-melody.pdf

format = pdf
```

The physical resource is:

```text
media/notation/nativity-melody.pdf
```

The complete notation path is:

```text
LI-002
        │
        ▼
MEL-001
        │
        ▼
MED-002
        │
        ▼
media/notation/nativity-melody.pdf
```

The application may now display or open the notation document.

---

## 14.13 Resolving the Final Text Item

After completing `LI-002`, the Core Engine continues with the next identifier in the Prayer Sequence:

```text
LI-003
```

The item declares:

```text
type     = text

targetId = TXT-002
```

The engine retrieves:

```text
TXT-002
```

from:

```text
content/texts.json
```

The complete first Prayer Sequence is now:

```text
PS-001
        │
        ├──► LI-001
        │        │
        │        ▼
        │     TXT-001
        │
        ├──► LI-002
        │        │
        │        ├──► QOL-001
        │        │        │
        │        │        ▼
        │        │     TXT-001
        │        │
        │        └──► MEL-001
        │                 │
        │                 ├──► MED-001
        │                 └──► MED-002
        │
        └──► LI-003
                 │
                 ▼
              TXT-002
```

---

## 14.14 Resolving the Second Prayer Sequence

The Occasion also references:

```text
PS-002
```

This sequence points to:

```text
prayerId = PR-002
```

and contains:

```text
LI-004

LI-005
```

The runtime path is:

```text
OCC-001
        │
        ▼
PS-002
        │
        ├──► PR-002
        ├──► LI-004
        └──► LI-005
```

`LI-004` resolves to:

```text
TXT-003
```

`LI-005` resolves to:

```text
QOL-002
```

with:

```text
effectiveMelodyId = MEL-002
```

The complete second sequence is:

```text
PS-002
        │
        ├──► LI-004
        │        │
        │        ▼
        │     TXT-003
        │
        └──► LI-005
                 │
                 ├──► QOL-002
                 │        │
                 │        ▼
                 │     TXT-003
                 │
                 └──► MEL-002
                          │
                          ├──► MED-003
                          └──► MED-004
```

---

## 14.15 Complete Package Resolution

The complete example package may now be represented as one runtime graph:

```text
manifest.json
        │
        ▼
ENTRY-001
        │
        ▼
OCC-001
        │
        ├──► PS-001
        │        │
        │        ├──► PR-001
        │        ├──► LI-001 ───► TXT-001
        │        ├──► LI-002
        │        │        ├──► QOL-001 ───► TXT-001
        │        │        └──► MEL-001
        │        │                 ├──► MED-001
        │        │                 └──► MED-002
        │        └──► LI-003 ───► TXT-002
        │
        └──► PS-002
                 │
                 ├──► PR-002
                 ├──► LI-004 ───► TXT-003
                 └──► LI-005
                          ├──► QOL-002 ───► TXT-003
                          └──► MEL-002
                                   ├──► MED-003
                                   └──► MED-004
```

The physical media resources are:

```text
MED-001
        │
        ▼
media/audio/nativity-melody.mp3

MED-002
        │
        ▼
media/notation/nativity-melody.pdf

MED-003
        │
        ▼
media/audio/morning-melody.mp3

MED-004
        │
        ▼
media/notation/morning-melody.pdf
```

---

## 14.16 Search-Based Resolution

The package may also be entered through the search index.

For example, a search for:

```text
Morning Hymn
```

may return:

```text
entityType = qolo

entityId   = QOL-002
```

The engine then retrieves:

```text
content/qolos.json
        │
        ▼
QOL-002
```

This search result provides direct access to the canonical entity.

It does not change the liturgical order of any Prayer Sequence.

Search is therefore a discovery mechanism rather than a replacement for the canonical navigation structure.

---

## 14.17 Runtime Responsibilities

During this walkthrough, the Core Engine is responsible for:

- validating package compatibility
- loading package collections
- resolving typed identifiers
- preserving declared sequence order
- retrieving canonical entities
- locating Media Assets
- resolving package-relative files
- exposing the resulting content to the application layer

The Core Engine is not responsible for:

- choosing the effective Melody
- editing canonical content
- generating search terms
- repairing invalid references
- inventing missing sequence items
- replacing missing assets with unrelated resources

These responsibilities belong to the Author Database, Build Tools, or package validation process.

---

## 14.18 Application Responsibilities

The application layer determines how resolved content is presented.

For example, the application may:

- display the Occasion name
- show the available Prayer Sequences
- render the Prayer identity
- display Text content
- present a Qolo title
- play the Melody audio
- display the notation file
- provide search controls

The application consumes resolved package content but does not redefine its canonical relationships.

---

## 14.19 Architectural Summary

The example demonstrates four separate responsibilities:

```text
Author Database
        │
        ▼
Defines and maintains canonical editorial content

Build Tools
        │
        ▼
Generate validated runtime packages and resolve effective values

Core Engine
        │
        ▼
Loads, validates, and resolves package relationships

Application
        │
        ▼
Presents the resolved content to the user
```

The complete operational flow is:

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
Application Interface
```

This separation allows multiple applications to use the same platform architecture while presenting different subsets of content and different user experiences.

# 15. Reference Integrity Examples

## 15.1 Purpose

One of the primary responsibilities of the Build Tools is to ensure that every reference inside an Application Package is valid.

A package should never contain unresolved identifiers, incompatible entity types, or broken relationships.

This section presents common examples of valid and invalid references.

The examples are intended to illustrate the integrity rules applied during package generation.

---

## 15.2 Valid Reference

The following Liturgical Item references an existing Qolo.

```json
{
  "id": "LI-002",
  "type": "qolo",
  "targetId": "QOL-001",
  "effectiveMelodyId": "MEL-001"
}
```

The referenced Qolo exists:

```text
content/qolos.json
        │
        ▼
QOL-001
```

This reference is valid.

---

## 15.3 Missing Target Entity

The following Liturgical Item references a Qolo that does not exist.

```json
{
  "id": "LI-002",
  "type": "qolo",
  "targetId": "QOL-999"
}
```

The package contains no entity with identifier:

```text
QOL-999
```

The Build Tools report the missing reference and reject the package.

---

## 15.4 Wrong Entity Type

The following reference declares:

```json
{
  "type": "qolo",
  "targetId": "TXT-001"
}
```

Although `TXT-001` exists, it identifies a Text rather than a Qolo.

```text
Expected

Qolo

Received

Text
```

The identifier exists, but it belongs to the wrong canonical collection.

This reference is invalid.

---

## 15.5 Missing Effective Melody

A Liturgical Item referencing a Qolo should also provide a valid effective Melody.

Example:

```json
{
  "type": "qolo",
  "targetId": "QOL-001",
  "effectiveMelodyId": "MEL-999"
}
```

Because `MEL-999` does not exist, the package contains an unresolved contextual reference.

The Build Tools reject the package.

---

## 15.6 Invalid Search Reference

The following Search entry is inconsistent.

```json
{
  "entityType": "occasion",
  "entityId": "MEL-001"
}
```

`MEL-001` identifies a Melody.

It does not identify an Occasion.

The correct form is:

```json
{
  "entityType": "melody",
  "entityId": "MEL-001"
}
```

---

## 15.7 Missing Media Asset

The following Melody references a Media Asset that does not exist.

```json
{
  "id": "MEL-001",
  "audioAssetId": "MED-999"
}
```

Since the package contains no matching Media Asset, the runtime would be unable to locate the audio resource.

The Build Tools detect the missing asset during validation.

---

## 15.8 Missing Physical File

The Media Asset exists.

```json
{
  "id": "MED-001",
  "type": "audio",
  "file": "audio/nativity-melody.mp3"
}
```

However, the physical file is absent from the package.

```text
media/
    audio/
        ✗ nativity-melody.mp3
```

Although the reference chain is internally correct, the package is incomplete.

Package validation should report the missing resource before distribution.

---

## 15.9 Duplicate Identifier

Identifiers must be unique within their canonical collection.

The following example is invalid.

```json
[
  {
    "id": "QOL-001"
  },
  {
    "id": "QOL-001"
  }
]
```

The Build Tools reject duplicate identifiers because the referenced entity would become ambiguous.

---

## 15.10 Circular References

Canonical entities should not form circular dependency chains.

For example:

```text
Entity A
        │
        ▼
Entity B
        │
        ▼
Entity A
```

Circular references complicate runtime resolution and should be detected during package validation.

---

## 15.11 Orphaned Entities

A canonical entity may exist without being referenced by another entity.

For example:

```text
QOL-010
```

exists inside:

```text
content/qolos.json
```

but is never referenced by any Liturgical Item.

Such entities are considered orphaned.

Depending on the package profile, orphaned entities may generate:

- a warning
- an informational report
- or no message at all

Unused content does not necessarily invalidate a package.

---

## 15.12 Validation Summary

During package generation, the Build Tools verify:

- referenced identifiers exist
- referenced entity types are compatible
- effective values resolve correctly
- media assets exist
- physical files are present
- identifiers are unique
- references are internally consistent
- circular dependencies are absent

Only packages passing these integrity checks should be distributed to runtime applications.

---

## 15.13 Integrity Philosophy

Reference integrity is established before the package reaches the Core Engine.

The responsibilities are therefore divided as follows:

```text
Author Database
        │
        ▼
Editorial consistency

Build Tools
        │
        ▼
Reference validation
Package integrity

Core Engine
        │
        ▼
Runtime resolution
```

The Core Engine assumes that the package has already passed integrity validation.

It resolves references but does not attempt to repair broken relationships or infer missing entities.

# 16. Complete Package Checklist

## 16.1 Purpose

This checklist summarizes the complete Application Package presented throughout this document.

It is intended as a practical verification guide before distributing a package.

The checklist does not replace formal schema validation performed by the Build Tools.

Instead, it provides a convenient reference for confirming that all required package components are present and internally consistent.

---

## 16.2 Package Structure

Verify that the package contains the expected directory structure.

```text
ApplicationPackage/

├── manifest.json
│
├── content/
│   ├── entry-points.json
│   ├── occasions.json
│   ├── prayers.json
│   ├── prayer-sequences.json
│   ├── liturgical-items.json
│   ├── texts.json
│   ├── qolos.json
│   ├── melodies.json
│   └── media-assets.json
│
├── indexes/
│   └── search-index.json
│
└── media/
    ├── audio/
    └── notation/
```

All required files should be present.

---

## 16.3 Manifest

Verify that:

- `packageId` is unique.
- `schemaVersion` is supported.
- compatibility information is correct.
- application metadata is complete.
- package profile is defined.

---

## 16.4 Canonical Collections

Verify that every required collection exists.

```text
✓ entry-points

✓ occasions

✓ prayers

✓ prayer-sequences

✓ liturgical-items

✓ texts

✓ qolos

✓ melodies

✓ media-assets
```

Each collection should contain valid canonical identifiers.

---

## 16.5 Identifier Integrity

Verify that:

- identifiers are unique
- identifiers are stable
- identifiers follow the expected naming convention
- duplicate identifiers are absent

Examples:

```text
ENTRY-001

OCC-001

PR-001

PS-001

LI-001

TXT-001

QOL-001

MEL-001

MED-001
```

---

## 16.6 Reference Integrity

Verify that every reference resolves successfully.

Examples include:

```text
Entry Point
        ▼
Occasion

Occasion
        ▼
Prayer Sequence

Prayer Sequence
        ▼
Liturgical Item

Liturgical Item
        ▼
Text

Liturgical Item
        ▼
Qolo

Liturgical Item
        ▼
Effective Melody

Melody
        ▼
Media Asset
```

No unresolved identifiers should remain.

---

## 16.7 Search Index

Verify that:

- every search entry references an existing entity
- `entityType` matches the referenced collection
- `entityId` exists
- labels are appropriate
- normalized terms are generated correctly

The search index should be generated from canonical content rather than edited manually.

---

## 16.8 Media Assets

Verify that every Media Asset:

- has a unique identifier
- declares the correct media type
- references a valid package-relative file
- specifies the correct file format

Example:

```text
MED-001
        │
        ▼
audio/nativity-melody.mp3
```

---

## 16.9 Physical Resources

Verify that every referenced resource actually exists.

Examples include:

```text
media/audio/

media/notation/
```

Every referenced file should be present.

No unused or missing files should remain unintentionally.

---

## 16.10 Runtime Resolution

Perform a complete runtime verification.

Confirm that the following path resolves successfully.

```text
manifest.json
        │
        ▼
Entry Point
        │
        ▼
Occasion
        │
        ▼
Prayer Sequence
        │
        ▼
Liturgical Item
        │
        ├──► Text
        │
        └──► Qolo
                  │
                  ▼
              Melody
                  │
                  ▼
            Media Asset
                  │
                  ▼
           Physical File
```

The complete resolution chain should execute without errors.

---

## 16.11 Search Verification

Verify that representative searches locate the expected entities.

Example searches:

```text
Nativity

Morning

Opening Prayer

Morning Hymn
```

Each result should resolve to the correct canonical entity.

---

## 16.12 Package Validation

Before distribution, confirm that the Build Tools report:

```text
✓ Schema validation passed

✓ Reference validation passed

✓ Package integrity verified

✓ Media validation passed

✓ Search index generated

✓ Package successfully built
```

Packages should not be distributed until all validation stages complete successfully.

---

## 16.13 Deployment Readiness

A package is considered ready for deployment when:

- package structure is complete
- canonical collections are valid
- references resolve correctly
- media resources exist
- search index has been generated
- runtime walkthrough succeeds
- validation completes successfully

Only after these conditions are satisfied should the package be released for use by runtime applications.

---

## 16.14 Final Summary

The example package presented throughout this document demonstrates the complete lifecycle of an Application Package.

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

The package separates:

- editorial content
- canonical entities
- runtime relationships
- search optimization
- physical media resources

Each layer has a clearly defined responsibility.

This separation allows multiple applications to share the same platform architecture while presenting different content, interfaces, and user experiences without modifying the underlying canonical model.

The examples contained in this document are intended as a practical reference for developers implementing or validating SyriacPlatform Application Packages.

# Appendix A — Complete Example Package

## A.1 Purpose

This appendix provides a consolidated overview of the complete example package presented throughout this document.

No new information is introduced here.

Instead, this appendix gathers the package structure, canonical collections, identifiers, and runtime relationships into a single reference.

Developers may use this appendix as a quick reference while implementing or validating Application Packages.

---

## A.2 Complete Package Structure

```text
ApplicationPackage/

├── manifest.json
│
├── content/
│   ├── entry-points.json
│   ├── occasions.json
│   ├── prayers.json
│   ├── prayer-sequences.json
│   ├── liturgical-items.json
│   ├── texts.json
│   ├── qolos.json
│   ├── melodies.json
│   └── media-assets.json
│
├── indexes/
│   └── search-index.json
│
└── media/
    ├── audio/
    │   ├── nativity-melody.mp3
    │   └── morning-melody.mp3
    │
    └── notation/
        ├── nativity-melody.pdf
        └── morning-melody.pdf
```

---

## A.3 Canonical Collections

```text
Entry Points
────────────
ENTRY-001

Occasions
─────────
OCC-001

Prayers
────────
PR-001
PR-002

Prayer Sequences
────────────────
PS-001
PS-002

Liturgical Items
────────────────
LI-001
LI-002
LI-003
LI-004
LI-005

Texts
─────
TXT-001
TXT-002
TXT-003

Qolos
─────
QOL-001
QOL-002

Melodies
────────
MEL-001
MEL-002

Media Assets
────────────
MED-001
MED-002
MED-003
MED-004
```

---

## A.4 Canonical Relationships

The complete canonical relationships of the example package are summarized below.

```text
ENTRY-001
        │
        ▼
OCC-001
        │
        ├──► PS-001
        │        │
        │        ├──► PR-001
        │        │
        │        ├──► LI-001
        │        │        ▼
        │        │     TXT-001
        │        │
        │        ├──► LI-002
        │        │        │
        │        │        ├──► QOL-001
        │        │        │        ▼
        │        │        │     TXT-001
        │        │        │
        │        │        └──► MEL-001
        │        │                 │
        │        │                 ├──► MED-001
        │        │                 └──► MED-002
        │        │
        │        └──► LI-003
        │                 ▼
        │              TXT-002
        │
        └──► PS-002
                 │
                 ├──► PR-002
                 │
                 ├──► LI-004
                 │        ▼
                 │     TXT-003
                 │
                 └──► LI-005
                          │
                          ├──► QOL-002
                          │        ▼
                          │     TXT-003
                          │
                          └──► MEL-002
                                   │
                                   ├──► MED-003
                                   └──► MED-004
```

---

## A.5 Runtime Resolution

The runtime resolution path demonstrated throughout this document is:

```text
manifest.json
        │
        ▼
entry-points.json
        │
        ▼
occasions.json
        │
        ▼
prayer-sequences.json
        │
        ▼
liturgical-items.json
        │
        ├──► texts.json
        │
        └──► qolos.json
                  │
                  ▼
             melodies.json
                  │
                  ▼
          media-assets.json
                  │
                  ▼
           media/audio/

           media/notation/
```

---

## A.6 Search Resolution

The package also supports direct discovery through the generated search index.

```text
User Search
        │
        ▼
search-index.json
        │
        ▼
entityType

entityId
        │
        ▼
Canonical Collection
        │
        ▼
Canonical Entity
```

Search provides an alternative entry into the package while preserving the canonical content model.

---

## A.7 Package Generation

The complete package generation process is:

```text
Author Database
        │
        ▼
Build Tools
        │
        ├──► manifest.json
        ├──► content/
        ├──► indexes/
        └──► media/
                │
                ▼
        Application Package
```

Every generated package should pass integrity validation before distribution.

---

## A.8 Architectural Layers

The complete SyriacPlatform package architecture is summarized below.

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

Each layer has a clearly defined responsibility.

Editorial decisions remain in the Author Database.

Package generation belongs to the Build Tools.

Relationship resolution belongs to the Core Engine.

Presentation belongs to the Application.

---

## A.9 Closing Remarks

The example package presented in this document is intentionally small.

Its purpose is not to represent a complete liturgical library, but to demonstrate the complete structure, relationships, and runtime behavior of a SyriacPlatform Application Package.

The same architecture scales from a small demonstration package to large production packages containing thousands of canonical entities while preserving the same principles of separation, consistency, validation, and runtime resolution.
