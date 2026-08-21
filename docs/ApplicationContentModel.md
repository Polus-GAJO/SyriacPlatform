# Application Content Model
Version 1.0

---

# 1. Purpose

The Application Content Model defines how liturgical content is organized after it has been processed by the Build Tools and before it is consumed by the Core Engine.

Unlike the Domain Model, which describes the conceptual structure of the Syriac liturgical tradition, the Application Content Model describes a runtime-oriented representation optimized for applications.

Its purpose is to transform the rich conceptual relationships of the Domain Model into a compact, deterministic, read-only package that applications can consume without interpreting liturgical rules.

The Application Content Model is therefore the contract between the Build Tools and every application built on the platform.

---

# 2. Position inside the Platform

The platform architecture is divided into five major layers.

```text
Author Database
        │
        ▼
Build Tools
        │
        ▼
Application Content Package
        │
        ▼
Core Engine
        │
        ▼
Application UI
```

Each layer has a single responsibility.

The Author Database stores the complete editorial knowledge.

The Build Tools transform that knowledge into application-ready content.

The Application Content Package stores only the information required by runtime applications.

The Core Engine reads the package and exposes simple APIs to the user interface.

The Application UI is responsible only for presentation and user interaction.

No application should reconstruct the original editorial relationships stored inside the Author Database.

---

# 3. Relationship to the Domain Model

The Domain Model and the Application Content Model describe the same liturgical reality from two different perspectives.

The Domain Model is conceptual.

It defines the identity of liturgical entities and their relationships without considering runtime performance.

The Application Content Model is operational.

Its purpose is to organize those entities into a format that applications can load quickly and display efficiently.

Consequently, the Application Content Model may duplicate small amounts of derived information when doing so simplifies application logic.

For example, inherited Qinto assignments are resolved during package generation rather than during application execution.

---

# 4. Design Principles

The Application Content Model follows the following principles.

## 4.1 Read-only

Applications never modify package contents.

Every package is considered immutable after generation.

Any modification is performed inside the Author Database and exported again through the Build Tools.

---

## 4.2 Deterministic

Applications should never interpret liturgical rules.

Whenever multiple inheritance or resolution rules exist, the Build Tools calculate the final result before package generation.

The package always contains the final effective values required by the application.

---

## 4.3 Read Optimized

The package is optimized for reading rather than editing.

Data duplication is acceptable whenever it significantly simplifies runtime access.

---

## 4.4 Stable Identity

Every reusable entity possesses a permanent identifier.

Examples include:

- Occasion
- Prayer
- Qolo
- Melody
- Text
- Petgomo
- Qinto
- Location

These identifiers remain stable across package versions.

---

## 4.5 Separation of Identity and Context

One of the fundamental principles of the model is the separation between permanent identity and contextual appearance.

Examples include:

Prayer
    represents the identity of a liturgical prayer.

PrayerSequence
    represents one contextual realization of that prayer inside a specific liturgical context.

Likewise,

Qolo
    represents the permanent identity of a hymn.

LiturgicalItem
    represents one appearance of that hymn inside one specific prayer sequence.

This distinction allows the same liturgical entities to be reused without duplicating their conceptual definitions.

---

## 4.6 Minimal Runtime Logic

Applications should primarily perform two operations:

Read

↓

Render

Complex editorial logic must never be transferred to runtime applications.

The Build Tools remain solely responsible for editorial interpretation.

# 5. Core Content Entities

The Application Content Model is composed of reusable entities and contextual entities.

Reusable entities represent permanent liturgical concepts.

Contextual entities represent the appearance of those concepts inside a particular liturgical context.

This distinction is one of the most important architectural principles of the platform.

---

# 5.1 Reusable Entities

Reusable entities possess permanent identities.

They may be referenced by many different applications, occasions, prayers, or liturgical contexts without duplication.

The following entities belong to this category.

```
Application Content
│
├── Occasion
├── Day
├── Prayer
├── Qolo
├── Melody
├── Text
├── Petgomo
├── Location
├── Qinto
├── Group
└── MediaAsset
```

These entities describe liturgical knowledge.

They are never created dynamically by applications.

---

# 5.2 Contextual Entities

Contextual entities describe how reusable entities are assembled for one particular liturgical context.

Unlike reusable entities, contextual entities usually exist only inside one generated package.

```
Application Content
│
├── PrayerSequence
├── LiturgicalItem
├── TextOccurrence
├── OccasionPrayerReference
└── DayPrayerReference
```

These entities define the runtime structure presented to the user.

---

# 6. Content Hierarchy

Applications navigate the package using the following logical hierarchy.

```
Occasion
        │
        ▼
Prayer Sequence
        │
        ▼
Liturgical Items
        │
        ▼
Texts
```

For daily liturgical applications such as Shhima, the hierarchy becomes

```
Day
        │
        ▼
Prayer Sequence
        │
        ▼
Liturgical Items
        │
        ▼
Texts
```

This hierarchy represents navigation only.

It should not be interpreted as ownership of reusable entities.

For example,

Prayer remains an independent reusable entity,

while PrayerSequence represents one contextual realization of that prayer.

---

# 7. Entity Responsibilities

Every entity has one clearly defined responsibility.

Maintaining this separation is essential for long-term maintainability.

---

## Occasion

Represents one liturgical occasion.

Examples include:

- Nativity
- Resurrection
- Lent
- Pentecost

An Occasion defines:

- its identity
- its metadata
- its ordered prayer sequences
- default inherited values when applicable

It does not contain hymn content directly.

---

## Day

Represents one liturgical day.

This entity is primarily used by applications organized around the calendar rather than special occasions.

A Day contains:

- its identity
- its metadata
- its ordered prayer sequences

Like Occasion, it does not contain hymn content directly.

---

## Prayer

Prayer represents the permanent identity of a liturgical prayer.

Examples include:

- Evening Prayer
- Morning Prayer
- Ramsho
- Soutoro

Prayer contains only information intrinsic to the prayer itself.

It intentionally does not define which hymns appear inside it.

That responsibility belongs to PrayerSequence.

---

## PrayerSequence

PrayerSequence represents one realization of a Prayer inside a particular liturgical context.

Multiple PrayerSequences may reference the same Prayer.

Each PrayerSequence defines:

- the contextual ordering of liturgical items
- inherited liturgical values resolved by the Build Tools
- any contextual metadata required during runtime

This design allows one prayer to appear differently depending on the occasion or day without duplicating the prayer definition itself.

---

## LiturgicalItem

LiturgicalItem represents one liturgical appearance of a hymn inside one PrayerSequence.

It is the central runtime entity of the package.

Each LiturgicalItem references:

- one Qolo
- one Location
- one effective Melody
- one effective Qinto
- one or more TextOccurrences
- optional media resources

A LiturgicalItem never owns the texts themselves.

It only references reusable text entities.

---

## TextOccurrence

TextOccurrence represents one occurrence of a text inside a LiturgicalItem.

It determines:

- which Text is used
- its display order
- any associated Petgomo

The same Text may appear in many different LiturgicalItems.

---

## Qolo

Qolo represents the permanent liturgical identity of a hymn.

A Qolo is not defined by one melody nor by one collection of verses.

Instead,

it represents the stable hymn identity used throughout the liturgical tradition.

Different liturgical contexts may associate the same Qolo with:

- different melodies
- different selections of verses
- different Petgome
- different recordings

The contextual selection is performed by the LiturgicalItem.

---

## Melody

Melody represents one musical realization.

Multiple melodies may be associated with the same Qolo.

Applications never calculate these relationships.

They are resolved during package generation.

---

## Text

Text represents one reusable textual unit.

Texts are stored only once inside the package.

Every occurrence references the reusable Text entity through its identifier.

This eliminates unnecessary duplication.

---

## Petgomo

Petgomo represents one reusable refrain or associated textual component.

Like Text,

Petgomo exists independently and may appear in many different liturgical contexts.

---

## Location

Location represents one liturgical position inside a prayer.

Examples include:

- Before Sedra
- After Sedra
- Before Gospel

Locations remain reusable across the entire package.

---

## Qinto

Qinto represents one liturgical mode.

Applications always receive the effective Qinto already resolved by the Build Tools.

They never calculate inheritance at runtime.

---

## Group

Group provides optional organizational classification.

It exists primarily to simplify navigation and grouping inside applications.

It has no effect on liturgical behavior.

---

## MediaAsset

MediaAsset represents every reusable media resource.

Examples include:

- audio recordings
- musical notation
- images
- illustrations

Media resources are stored independently and referenced by identifier.

This avoids duplication and allows future expansion without modifying the data model.

# 8. Package-Level Structure

The complete runtime content is represented by one logical ApplicationContentPackage.

```text
ApplicationContentPackage
├── metadata
├── entryPoints[]
├── occasions[]
├── days[]
├── prayers[]
├── prayerSequences[]
├── liturgicalItems[]
├── locations[]
├── groups[]
├── qolos[]
├── qintos[]
├── melodies[]
├── texts[]
├── petgome[]
├── mediaAssets[]
└── indexes?
```

Not every application must include every collection.

A small application may contain only the entities required by its own content.

For example, an occasion-based application may omit `days`, while a Shhima application may depend primarily on `days`.

Optional collections must not force applications to implement features they do not use.

---

# 9. Package Metadata

Every package contains metadata describing its identity, structure, content version, and origin.

```text
PackageMetadata
├── packageId
├── schemaVersion
├── contentVersion
├── applicationId
├── title
├── defaultLanguage
├── supportedLanguages[]
├── createdAt
├── sourceRevision?
└── checksum?
```

## packageId

Identifies the generated content package.

The identifier must remain stable for successive versions of the same logical package.

Example:

```text
shhima-main
```

or:

```text
nativity-occasions
```

---

## schemaVersion

Identifies the structural version of the Application Content Model used by the package.

It changes only when the package structure or interpretation rules change.

A simple correction to a text or recording does not change `schemaVersion`.

---

## contentVersion

Identifies the version of the packaged content.

It changes whenever exported content changes, including:

- text corrections
- reordered prayers
- changed melodies
- new recordings
- modified translations
- updated notation files

---

## applicationId

Identifies the application for which the package was generated.

Different applications may use the same model while containing different selections of content.

---

## title

Provides a human-readable package title.

It is descriptive metadata and must not be used as a technical identifier.

---

## defaultLanguage

Defines the preferred interface or content language when no explicit language has been selected.

---

## supportedLanguages

Lists all languages included in the package.

The presence of a language in this list does not require every textual entity to contain a translation in that language.

---

## createdAt

Records when the package was generated by the Build Tools.

It describes package generation time, not the historical date of the liturgical content.

---

## sourceRevision

Optionally identifies the Author Database revision, export revision, or source control commit from which the package was generated.

This field improves traceability.

---

## checksum

Optionally provides a package-level integrity value.

The exact checksum algorithm and validation rules belong to the Application Package Specification rather than this document.

---

# 10. Entry Points

EntryPoint defines where an application may begin navigation.

Different applications may begin at different levels of the content hierarchy.

```text
EntryPoint
├── id
├── type
├── targetId
├── sort
└── metadata?
```

Supported conceptual entry-point types include:

```text
OCCASION_LIST
DAY_LIST
PRAYER
PRAYER_SEQUENCE
LITURGICAL_ITEM_SEQUENCE
```

The exact serialized values will be defined by the Application Package Specification.

---

## id

Uniquely identifies the entry point inside the package.

---

## type

Defines how the application should interpret the target.

---

## targetId

References the entity or collection represented by the entry point.

---

## sort

Defines the display order when an application exposes multiple entry points.

---

## metadata

May contain optional presentation information, such as:

- title override
- subtitle
- icon reference
- navigation hint

EntryPoint metadata must not contain liturgical logic.

---

# 11. OccasionContent

OccasionContent represents one liturgical occasion and its ordered PrayerSequences.

```text
OccasionContent
├── id
├── title
├── subtitle?
├── description?
├── defaultQintoId?
├── prayerSequenceRefs[]
└── metadata?
```

---

## id

Provides the permanent identity of the occasion.

---

## title

Contains the primary display title.

Localized alternatives may be provided through a multilingual value structure defined later in this document.

---

## subtitle

Provides optional secondary descriptive text.

---

## description

Provides optional explanatory content for the occasion.

---

## defaultQintoId

References the Qinto assigned at occasion level, when applicable.

This value records the contextual default before more specific overrides are applied.

Applications must not use it to calculate LiturgicalItem Qinto inheritance.

Every LiturgicalItem receives its own final `effectiveQintoId`.

---

## prayerSequenceRefs

Contains the ordered PrayerSequences belonging to the occasion.

```text
OccasionPrayerSequenceRef
├── prayerSequenceId
└── sort
```

The reference, rather than PrayerSequence itself, owns the order inside the occasion.

This allows the same structural concepts to be referenced in different ordered contexts without moving ordering responsibility into the reusable entity.

---

# 12. DayContent

DayContent represents one liturgical day and its ordered PrayerSequences.

```text
DayContent
├── id
├── title
├── subtitle?
├── description?
├── defaultQintoId?
├── prayerSequenceRefs[]
└── metadata?
```

DayContent follows the same contextual principles as OccasionContent.

```text
DayPrayerSequenceRef
├── prayerSequenceId
└── sort
```

A Day may contain multiple PrayerSequences.

The order is explicit and must never be inferred from identifiers, titles, or database insertion order.

---

# 13. PrayerContent

PrayerContent represents the permanent identity and general metadata of a prayer.

```text
PrayerContent
├── id
├── title
├── subtitle?
├── description?
└── metadata?
```

PrayerContent intentionally contains no LiturgicalItems.

The same Prayer may have different content depending on:

- occasion
- day
- season
- liturgical context
- local tradition

That contextual content belongs to PrayerSequenceContent.

---

## Identity versus content

The following distinction is mandatory:

```text
PrayerContent
    defines what the prayer is

PrayerSequenceContent
    defines how the prayer appears in one context
```

Applications must not assume that every PrayerSequence referencing the same Prayer contains the same LiturgicalItems.

---

# 14. PrayerSequenceContent

PrayerSequenceContent represents one contextual realization of a Prayer.

```text
PrayerSequenceContent
├── id
├── prayerId
├── effectiveQintoId?
├── liturgicalItemRefs[]
└── metadata?
```

---

## id

Uniquely identifies this contextual prayer sequence.

Its identity is distinct from the referenced Prayer.

---

## prayerId

References the reusable PrayerContent entity.

---

## effectiveQintoId

Contains the Qinto resolved for the PrayerSequence by the Build Tools.

It may originate from:

- the surrounding Occasion
- the surrounding Day
- the Prayer context
- a more specific editorial assignment

Applications do not calculate this value.

---

## liturgicalItemRefs

Contains the ordered LiturgicalItems belonging to the sequence.

```text
PrayerSequenceLiturgicalItemRef
├── liturgicalItemId
└── sort
```

The reference owns the ordering relationship.

This avoids placing context-specific ordering inside LiturgicalItem itself.

---

## Ordering rule

`sort` is the authoritative order of LiturgicalItems within the PrayerSequence.

Applications must not derive order from:

- entity identifiers
- Location identifiers
- Qolo numbers
- titles
- physical order inside serialized collections

The Build Tools must export an explicit and deterministic sort value.

---

# 15. LiturgicalItemContent

LiturgicalItemContent represents one contextual appearance of a Qolo inside a PrayerSequence.

```text
LiturgicalItemContent
├── id
├── locationId
├── qoloId
├── effectiveQintoId?
├── effectiveMelodyId?
├── textOccurrences[]
├── audioIds[]
├── notationIds[]
└── metadata?
```

LiturgicalItemContent is the primary runtime composition entity.

It gathers the references required to display and play one liturgical item without duplicating the reusable entities themselves.

---

## id

Uniquely identifies this contextual appearance.

Two LiturgicalItems may reference the same Qolo while remaining distinct contextual occurrences.

---

## locationId

References the liturgical Location where the item appears.

---

## qoloId

References the permanent Qolo identity.

The Qolo identifier remains the same wherever that Qolo appears in the liturgy.

The reference does not imply that every appearance uses the same verses or melody.

---

## effectiveQintoId

Contains the final Qinto resolved specifically for this LiturgicalItem.

The Build Tools must resolve inheritance before package generation.

A typical resolution hierarchy may include:

```text
LiturgicalItem assignment
        otherwise
PrayerSequence assignment
        otherwise
Occasion or Day assignment
```

The exact editorial rule belongs to the Build Tools and Domain Model.

The runtime package contains only the final result.

---

## effectiveMelodyId

References the Melody selected for this particular appearance.

The same Qolo may use different Melodies in different contexts.

The application must not select a Melody by searching Qolo relationships at runtime.

---

## textOccurrences

Contains the exact textual composition used in this appearance.

```text
TextOccurrenceContent
├── id
├── sort
├── textId
└── petgomoId?
```

A LiturgicalItem may use:

- one text
- several verses
- a subset of all texts associated with the Qolo
- a different ordering
- an optional Petgomo attached to a specific occurrence

This structure allows the same Qolo to appear repeatedly with small contextual variations without duplicating Text content.

---

## audioIds

References one or more MediaAssets containing recordings for this LiturgicalItem.

Each Qolo appearance may have its own independent recording.

The package does not require one long recording for the entire prayer.

This design supports:

- smaller media files
- direct playback of one item
- independent replacement of recordings
- faster loading
- simpler navigation
- selective offline download in future package versions

The ordering of `audioIds` may define the preferred recording order when more than one recording is supplied.

---

## notationIds

References one or more MediaAssets containing musical notation related to this LiturgicalItem.

Notation may depend on the selected Melody and contextual use.

The Build Tools are responsible for exporting the correct references.

---

# 16. TextOccurrenceContent

TextOccurrenceContent represents the use of one reusable Text inside one LiturgicalItem.

```text
TextOccurrenceContent
├── id
├── sort
├── textId
└── petgomoId?
```

---

## id

Provides stable identity for the occurrence inside the generated content model.

Although an occurrence could theoretically be identified by its parent and order, an explicit identifier supports future features such as:

- direct navigation
- annotations
- reading progress
- bookmarks
- corrections linked to a specific occurrence
- synchronization between package versions

---

## sort

Defines the display order inside the LiturgicalItem.

This order is contextual and must not be inferred from Text identity.

---

## textId

References one reusable TextContent entity.

The full textual content is not duplicated inside the occurrence.

---

## petgomoId

Optionally references one PetgomoContent entity associated with this occurrence.

A Petgomo is therefore attached to the contextual use of the text, not permanently embedded inside TextContent.

---

# 17. Reuse and Storage Efficiency

The Application Content Model avoids duplication by storing reusable content once and representing repeated appearances through identifiers.

For example:

```text
Qolo 250
├── related to multiple Melodies
└── related to hundreds of Texts
```

The Qolo may appear many times across the liturgy.

Each appearance is represented by a LiturgicalItem that contains only the contextual selection:

```text
LiturgicalItem A
├── qoloId: 250
├── effectiveMelodyId: melody-3
└── textOccurrences:
    ├── text-101
    ├── text-102
    └── text-103
```

Another appearance may use:

```text
LiturgicalItem B
├── qoloId: 250
├── effectiveMelodyId: melody-6
└── textOccurrences:
    ├── text-101
    ├── text-104
    └── text-105
```

The actual Text and Melody entities remain stored only once.

Only their identifiers and contextual ordering are repeated.

This provides substantial storage efficiency for large applications such as Shhima while preserving full liturgical accuracy.

---

# 18. Relationship Summary

The principal relationships are:

```text
Occasion
└── ordered references to PrayerSequence

Day
└── ordered references to PrayerSequence

PrayerSequence
├── references one Prayer
└── ordered references to LiturgicalItem

LiturgicalItem
├── references one Location
├── references one Qolo
├── references one effective Qinto
├── references one effective Melody
├── contains ordered TextOccurrences
└── references MediaAssets

TextOccurrence
├── references one Text
└── optionally references one Petgomo
```

The resulting navigation path is:

```text
Occasion or Day
        │
        ▼
PrayerSequence
        │
        ├── Prayer identity
        │
        ▼
LiturgicalItem
        ├── Qolo
        ├── Location
        ├── effective Qinto
        ├── effective Melody
        ├── ordered TextOccurrences
        └── MediaAssets
```

This hierarchy is explicit, deterministic, and ready for runtime consumption.

# 19. QoloContent

QoloContent represents the permanent identity of a liturgical hymn.

```text
QoloContent
├── id
├── title
├── subtitle?
├── groupId?
├── relatedTextIds[]
├── relatedMelodyIds[]
└── metadata?
```

A Qolo is a reusable liturgical identity.

It must not be interpreted as one fixed combination of:

- one melody
- one sequence of verses
- one Petgomo
- one recording
- one notation file

The actual contextual combination is always defined by LiturgicalItemContent.

---

## id

Provides the permanent technical identity of the Qolo.

This identifier must remain stable across package versions even when:

- titles are corrected
- new texts are associated with the Qolo
- new melodies are added
- recordings are replaced
- metadata is expanded

Applications must use the identifier rather than the title to establish identity.

---

## title

Provides the primary display title of the Qolo.

The title may be descriptive, traditional, numeric, or derived from the editorial source.

It must not be treated as a unique identifier.

---

## subtitle

Provides optional secondary information.

Examples may include:

- alternate traditional name
- short descriptive phrase
- opening words
- local classification

---

## groupId

Optionally references one GroupContent entity.

This relationship is intended for classification and navigation.

It does not affect liturgical behavior.

---

## relatedTextIds

Contains references to TextContent entities generally associated with the Qolo.

```text
Qolo
└── relatedTextIds[]
```

This collection describes the reusable textual repertoire connected to the Qolo.

It does not define which texts appear in any particular liturgical use.

The exact selection and ordering belong to LiturgicalItemContent through TextOccurrenceContent.

Therefore, an application must not display all `relatedTextIds` automatically when rendering one LiturgicalItem.

---

## relatedMelodyIds

Contains references to MelodyContent entities associated with the Qolo.

```text
Qolo
└── relatedMelodyIds[]
```

A single Qolo may be associated with several melodies.

This relationship records the available or historically related melodic repertoire.

It does not select the effective melody for a contextual appearance.

The selected melody is exported as `effectiveMelodyId` inside LiturgicalItemContent.

---

## Qolo identity rule

The following distinction is mandatory:

```text
QoloContent
    defines the reusable hymn identity

LiturgicalItemContent
    defines one contextual use of that hymn
```

The same Qolo may therefore appear in many LiturgicalItems with different:

- melodies
- texts
- Petgome
- Qinto values
- recordings
- notation resources
- liturgical locations

This variation does not create a new Qolo identity.

---

# 20. MelodyContent

MelodyContent represents one reusable musical realization associated with liturgical content.

```text
MelodyContent
├── id
├── qoloId?
├── title
├── subtitle?
├── mnemonicText?
├── qintoAssignments[]
├── recordingIds[]
├── notationIds[]
└── metadata?
```

A Melody is stored independently so that it may be referenced from many LiturgicalItems without duplication.

---

## id

Provides the permanent identity of the Melody.

This identifier must not change merely because:

- its title changes
- a recording is replaced
- notation is corrected
- additional metadata is added

---

## qoloId

Optionally references the principal Qolo associated with the Melody.

This field is optional because the long-term model may allow a Melody to be shared across more than one Qolo or classified independently.

When a many-to-many relationship is required, the package may use relationship collections or indexes instead of relying exclusively on this field.

The exact serialized strategy belongs to the Application Package Specification.

---

## title

Provides the primary display title of the Melody.

It may represent:

- a traditional melody name
- a mnemonic designation
- a local name
- an editorial label
- a numeric designation

The title must not be used as a technical identity.

---

## subtitle

Provides optional secondary information.

---

## mnemonicText

Optionally stores a short textual phrase traditionally used to identify or remember the Melody.

Mnemonic text is descriptive metadata.

It is not a replacement for the TextContent entities used in liturgical composition.

---

## qintoAssignments

Contains optional relationships between the Melody and one or more Qinto entities.

```text
MelodyQintoAssignment
├── qintoId
├── sort?
└── metadata?
```

These relationships describe the broader musical classification of the Melody.

They do not determine the effective Qinto of a LiturgicalItem at runtime.

The final contextual Qinto remains stored as `effectiveQintoId` on LiturgicalItemContent.

---

## recordingIds

References MediaAssets containing recordings of the Melody itself.

These recordings are distinct from contextual LiturgicalItem recordings.

A Melody recording may serve purposes such as:

- teaching the melody
- previewing the melody
- comparing melodic variants
- documenting the musical identity independently of a full liturgical occurrence

---

## notationIds

References MediaAssets containing notation associated with the Melody.

These notation resources may represent:

- full musical notation
- simplified notation
- teaching notation
- alternate versions
- historical versions

A LiturgicalItem may also reference contextual notation directly when its notation differs from the general Melody notation.

---

## Melody selection rule

Applications must not select an effective Melody merely by examining all melodies associated with a Qolo.

The Build Tools must export the selected contextual Melody through:

```text
LiturgicalItemContent.effectiveMelodyId
```

The general Qolo–Melody relationships remain available for browsing, study, or future application features.

---

# 21. TextContent

TextContent represents one reusable textual unit.

```text
TextContent
├── id
├── syriac
├── translations[]
└── metadata?
```

TextContent is stored once and referenced wherever it appears.

A Text may be reused across:

- multiple Qolos
- multiple LiturgicalItems
- multiple PrayerSequences
- multiple Occasions
- multiple Days
- multiple applications

---

## id

Provides the permanent identity of the textual unit.

The identifier must remain stable when editorial corrections preserve the identity of the same text.

A substantially different text must receive a different identity according to the editorial rules of the Author Database.

The Application Content Model does not determine whether two textual variants constitute the same Text or separate Text entities.

That decision belongs to the Author Database and Build Tools.

---

## syriac

Contains the primary Syriac text.

The package must preserve the authoritative spelling, punctuation, vocalization, and formatting exported by the Build Tools.

Applications must not normalize or alter the stored text for display.

Any search normalization must operate through a separate runtime index or derived search value.

---

## translations

Contains zero or more translations.

```text
TranslationContent
├── languageCode
└── text
```

A Text may have:

- no translation
- one translation
- several translations

Not every Text is required to support every language listed in package metadata.

---

## languageCode

Identifies the translation language.

The exact language-code standard will be defined in the Application Package Specification.

---

## text

Contains the translated text.

Translations must remain associated with the reusable Text identity rather than being duplicated inside LiturgicalItems.

---

## Text reuse rule

TextContent contains the reusable textual unit only.

It does not contain contextual information such as:

- order inside a hymn
- associated Petgomo
- selected Melody
- liturgical Location
- prayer context

That information belongs to TextOccurrenceContent or LiturgicalItemContent.

---

## Text variants

The model permits the Author Database to preserve related textual variants as separate Text entities.

For example:

```text
Text A
Text B
```

may differ only slightly while still remaining separate editorial records.

The Application Content Model must preserve this distinction and must not merge them automatically.

---

# 22. TranslationContent

TranslationContent represents one translation attached to a reusable textual entity.

```text
TranslationContent
├── languageCode
└── text
```

The same structure may be used by:

- TextContent
- PetgomoContent
- descriptive metadata when multilingual values are required

---

## Translation availability

Applications must treat translations as optional.

The absence of a translation must not prevent the Syriac source text from being displayed.

Applications may apply fallback behavior, but they must not fabricate or infer missing translations.

---

## Translation identity

TranslationContent does not require a permanent independent identifier in version 1.0.

Its identity is determined by:

```text
parent entity
+
languageCode
```

A future specification may add translation identifiers if required for:

- annotations
- correction tracking
- synchronization
- independent translation versioning

---

# 23. PetgomoContent

PetgomoContent represents one reusable Petgomo or refrain.

```text
PetgomoContent
├── id
├── syriac
├── translations[]
└── metadata?
```

A Petgomo is stored independently because the same Petgomo may be used in many contextual appearances.

---

## id

Provides the permanent identity of the Petgomo.

---

## syriac

Contains the authoritative Syriac text.

---

## translations

Contains zero or more translations using TranslationContent.

---

## Contextual association

PetgomoContent does not permanently belong to one TextContent.

Instead, the contextual association is expressed through:

```text
TextOccurrenceContent.petgomoId
```

This is essential because:

- the same Text may appear with different Petgome
- the same Petgomo may accompany several texts
- some occurrences may have no Petgomo
- Petgomo assignment may depend on the liturgical context

---

# 24. QintoContent

QintoContent represents one reusable liturgical mode identity.

```text
QintoContent
├── id
├── title
├── number?
├── subtitle?
└── metadata?
```

---

## id

Provides the permanent technical identity of the Qinto.

Applications must reference Qinto by identifier rather than by number or title.

---

## title

Provides the display name.

---

## number

Optionally provides the traditional or editorial Qinto number.

The number is descriptive and may support sorting or display.

It must not replace the permanent identifier.

---

## subtitle

Provides optional secondary information.

---

## Effective Qinto rule

QintoContent describes the reusable Qinto identity.

It does not contain inheritance logic.

The effective Qinto for a contextual appearance is exported by the Build Tools.

```text
PrayerSequenceContent.effectiveQintoId
LiturgicalItemContent.effectiveQintoId
```

Applications must not perform fallback calculations such as:

```text
LiturgicalItem
otherwise PrayerSequence
otherwise Occasion
otherwise Day
```

That resolution belongs entirely to package generation.

---

## Unresolved Qinto

When no effective Qinto applies, the Build Tools may omit `effectiveQintoId`.

Applications must treat absence as a valid state rather than attempting to infer a value.

---

# 25. LocationContent

LocationContent represents one reusable liturgical position or section.

```text
LocationContent
├── id
├── title
├── subtitle?
├── displayOrder?
└── metadata?
```

Examples may include positions such as:

- before Sedra
- after Sedra
- before the Gospel
- after the Gospel
- opening section
- concluding section

The exact list is determined by the Author Database.

---

## id

Provides permanent identity.

---

## title

Provides the primary display title.

---

## subtitle

Provides optional secondary information.

---

## displayOrder

May provide a general default ordering for navigation or classification.

It must not determine the actual order of LiturgicalItems inside a PrayerSequence.

Contextual ordering is always defined by:

```text
PrayerSequenceLiturgicalItemRef.sort
```

---

## Location behavior

LocationContent provides classification and display context.

It does not own LiturgicalItems.

The same Location may be referenced by many LiturgicalItems across different prayers and applications.

---

# 26. GroupContent

GroupContent represents an optional organizational classification.

```text
GroupContent
├── id
├── title
├── subtitle?
├── parentGroupId?
├── sort?
└── metadata?
```

Groups may be used to organize content by:

- liturgical family
- hymn classification
- editorial collection
- thematic category
- application navigation section

---

## id

Provides permanent identity.

---

## title

Provides the display title.

---

## parentGroupId

Optionally allows hierarchical grouping.

```text
Parent Group
└── Child Group
```

Group hierarchy must remain organizational.

It must not introduce hidden liturgical rules.

---

## sort

Provides optional default display order among sibling groups.

---

## Group rule

GroupContent may improve browsing and presentation, but it must not affect:

- Qinto resolution
- Melody selection
- Text selection
- prayer ordering
- LiturgicalItem ordering

Any relationship that changes liturgical behavior must be represented explicitly elsewhere.

---

# 27. MediaAsset

MediaAsset represents one reusable media resource referenced by content entities.

```text
MediaAsset
├── id
├── type
├── path
├── mimeType?
├── languageCode?
├── duration?
├── checksum?
├── size?
└── metadata?
```

MediaAsset stores descriptive and technical information about a resource.

The actual packaging rules for binary files belong to the Application Package Specification.

---

## id

Provides the stable identity of the media resource.

A new identifier may be required when a replacement represents a materially different resource.

The exact media identity policy will be defined by package-generation rules.

---

## type

Identifies the media category.

Conceptual types may include:

```text
AUDIO
NOTATION
IMAGE
DOCUMENT
VIDEO
```

The exact serialized values belong to the Application Package Specification.

---

## path

Provides the package-relative location of the resource.

Applications must not assume that media files are available through external URLs.

The package path must be sufficient for local runtime resolution.

---

## mimeType

Optionally identifies the media format.

Examples include:

```text
audio/mpeg
audio/mp4
image/png
image/jpeg
application/pdf
```

The Package Specification will define supported formats.

---

## languageCode

Optionally identifies the language of a spoken or textual media resource.

This may be useful for:

- translated audio
- explanatory recordings
- language-specific images
- multilingual documents

---

## duration

Optionally stores the media duration.

This field is primarily applicable to audio and video.

Applications may use it to display playback information without opening the complete media file.

---

## checksum

Optionally stores an integrity value for the individual resource.

Package-level validation rules and checksum algorithms belong to the Application Package Specification.

---

## size

Optionally stores the media size in bytes.

This supports:

- storage calculations
- download planning
- package diagnostics
- future selective content installation

---

# 28. Media Reference Strategies

Media may be referenced from different levels depending on its purpose.

---

## LiturgicalItem media

```text
LiturgicalItemContent
├── audioIds[]
└── notationIds[]
```

These resources belong to one contextual liturgical appearance.

Examples include:

- a recording of the exact selected verses
- notation adapted to that particular appearance
- a contextual performance recording

---

## Melody media

```text
MelodyContent
├── recordingIds[]
└── notationIds[]
```

These resources describe the reusable Melody itself.

Examples include:

- a teaching recording
- a general melodic demonstration
- a standard notation sheet

---

## Entity metadata media

Future package versions may allow entities such as Occasion or Qolo to reference additional images or documents.

Such references must remain optional and must not change the core liturgical hierarchy.

---

## Media independence

MediaAsset is an independent registry entity.

Deleting or replacing one media resource must not require duplicating or rewriting the related Text, Qolo, Melody, or LiturgicalItem definitions.

---

# 29. Metadata Extension

Several entities contain an optional `metadata` field.

```text
metadata?
```

Metadata exists to support non-core descriptive information without continuously expanding the fundamental model.

Possible uses include:

- alternate titles
- editorial notes
- display hints
- source references
- traditional classifications
- feature flags
- migration information

---

## Metadata limitations

Metadata must not contain information required to correctly interpret the liturgical structure.

Core behavior must always be represented through explicit model fields.

For example, the following must not be hidden inside metadata:

- PrayerSequence ordering
- LiturgicalItem ordering
- selected Melody
- effective Qinto
- Text selection
- Petgomo assignment
- entity identity

Applications must be able to render valid core content without interpreting arbitrary metadata.

---

## Metadata compatibility

Applications should ignore unknown metadata properties unless they explicitly support them.

This allows future package versions to add optional descriptive information without breaking older applications.

---

# 30. Reusable Entity Relationship Summary

The reusable entities form a network of stable references.

```text
Qolo
├── optionally belongs to Group
├── relates to Texts
└── relates to Melodies

Melody
├── may relate to Qolo
├── may relate to Qintos
└── references MediaAssets

Text
└── contains Translations

Petgomo
└── contains Translations

Location
└── is referenced by LiturgicalItems

Qinto
├── is referenced by PrayerSequences
├── is referenced by LiturgicalItems
└── may classify Melodies

MediaAsset
├── is referenced by LiturgicalItems
└── is referenced by Melodies
```

These reusable relationships describe general knowledge.

They do not replace contextual selection.

---

# 31. General Relationship versus Contextual Selection

The model distinguishes between two relationship categories.

## General relationship

A general relationship describes what may be associated in the reusable liturgical repertoire.

Examples:

```text
Qolo
└── relatedMelodyIds[]

Qolo
└── relatedTextIds[]

Melody
└── qintoAssignments[]
```

These relationships support:

- browsing
- study
- validation
- content exploration
- editorial traceability

---

## Contextual selection

A contextual selection defines what is actually used in one specific liturgical appearance.

Examples:

```text
LiturgicalItem
├── effectiveMelodyId
├── effectiveQintoId
└── textOccurrences[]
```

Applications rendering liturgical content must use contextual selections.

They must not reconstruct them from general relationships.

---

## Example

A Qolo may be generally related to:

```text
Melodies
├── Melody A
├── Melody B
└── Melody C

Texts
├── Text 1
├── Text 2
├── Text 3
├── Text 4
└── Text 5
```

One contextual appearance may select:

```text
LiturgicalItem X
├── effectiveMelodyId: Melody B
└── textOccurrences
    ├── Text 1
    ├── Text 3
    └── Text 5
```

Another appearance of the same Qolo may select:

```text
LiturgicalItem Y
├── effectiveMelodyId: Melody C
└── textOccurrences
    ├── Text 2
    └── Text 4
```

Neither appearance modifies the reusable Qolo definition.

This distinction is essential for accurately representing the liturgical tradition without duplicating content.

# 32. Content Indexes

The Application Content Package may contain optional indexes designed to accelerate common runtime operations.

```text
ContentIndexes
├── prayerSequencesByOccasion?
├── prayerSequencesByDay?
├── liturgicalItemsByPrayerSequence?
├── textsByQolo?
├── melodiesByQolo?
├── qolosByGroup?
├── mediaByEntity?
└── searchIndex?
```

Indexes are derived structures.

They do not introduce new liturgical knowledge and must never become the authoritative source of a relationship.

The canonical entities and their explicit references remain the source of truth.

---

## 32.1 Purpose of indexes

Indexes may improve:

- package loading
- entity lookup
- navigation
- filtering
- search
- grouping
- media resolution
- startup performance

They are particularly useful in large packages containing thousands of reusable entities and contextual occurrences.

---

## 32.2 Optional nature

Indexes are optional unless a future package profile explicitly requires them.

An application must be able to interpret the package from canonical entity collections and references even when no indexes are present.

The Core Engine may construct temporary runtime indexes after loading when the package does not provide them.

---

## 32.3 Derived-data rule

Every index must be reproducible from canonical package content.

For example:

```text
prayerSequencesByOccasion
```

must be derivable from:

```text
OccasionContent.prayerSequenceRefs[]
```

Likewise:

```text
textsByQolo
```

must be derivable from:

```text
QoloContent.relatedTextIds[]
```

An index must not contain relationships that are absent from the canonical model.

---

## 32.4 Index consistency

The Build Tools are responsible for ensuring that exported indexes match the canonical content.

If an index conflicts with canonical entity references, the canonical entity references take precedence.

Package validation should report such a conflict as an error.

---

## 32.5 Example indexes

### PrayerSequences by Occasion

```text
prayerSequencesByOccasion
└── occasionId
    └── prayerSequenceIds[]
```

This index may provide direct access to the sequences belonging to one occasion.

It does not replace the ordered references stored in OccasionContent.

When order is relevant, the canonical `sort` values must be preserved.

---

### LiturgicalItems by PrayerSequence

```text
liturgicalItemsByPrayerSequence
└── prayerSequenceId
    └── liturgicalItemIds[]
```

This index supports fast sequence loading.

The order must remain equivalent to:

```text
PrayerSequenceContent.liturgicalItemRefs[].sort
```

---

### Texts by Qolo

```text
textsByQolo
└── qoloId
    └── textIds[]
```

This index supports exploration of the textual repertoire generally associated with a Qolo.

It must not be used to determine the texts selected for one LiturgicalItem.

---

### Qolos by Group

```text
qolosByGroup
└── groupId
    └── qoloIds[]
```

This index supports classification and browsing.

It has no effect on liturgical composition.

---

# 33. Search Support

Search is a runtime feature that may require derived representations of content.

The canonical Syriac text must remain unchanged.

Search normalization must therefore be stored or generated separately.

```text
SearchIndex
├── entries[]
│   ├── entityType
│   ├── entityId
│   ├── normalizedValues[]
│   └── tokens?
└── metadata?
```

---

## 33.1 Search is not canonical content

Search values are derived from canonical fields such as:

- titles
- Syriac texts
- translations
- alternate names
- mnemonic texts
- descriptive metadata

They exist only to make retrieval efficient.

They must not replace the authoritative source text.

---

## 33.2 Syriac normalization

A search index may include normalized Syriac forms produced by the Build Tools.

Normalization may remove or transform elements such as:

- vocalization marks
- diacritical marks
- punctuation
- spacing differences
- selected orthographic distinctions

The precise normalization rules belong to the Build Tools and search specification.

Applications must not display normalized search values as authoritative text.

---

## 33.3 Search-result identity

Every search result must resolve to a canonical package entity through:

```text
entityType
+
entityId
```

Applications must not depend on duplicated search-result content when the canonical entity is available.

---

## 33.4 Search versioning

Because normalization rules may evolve independently from liturgical content, a search index may contain its own optional version metadata.

```text
SearchIndexMetadata
├── normalizationVersion?
├── tokenizerVersion?
└── generatedAt?
```

The exact serialized structure belongs to the Application Package Specification.

---

# 34. Multilingual Values

Some package fields may require more than one display language.

The model distinguishes between primary domain content and multilingual presentation values.

---

## 34.1 Translated textual entities

TextContent and PetgomoContent use explicit translation collections:

```text
translations[]
```

These translations belong to the textual entity itself.

---

## 34.2 Localized descriptive fields

Titles, subtitles, and descriptions may use a localized-value structure when an application requires multilingual presentation.

```text
LocalizedText
├── default
└── values?
    └── languageCode → text
```

Conceptually:

```text
title
├── default: "Primary title"
└── values
    ├── ar: "Arabic title"
    ├── en: "English title"
    └── fr: "French title"
```

The exact JSON representation will be defined by the Application Package Specification.

---

## 34.3 Default value

Every required localized field must contain one default value.

Applications may display the default value when:

- the requested language is unavailable
- the language is unsupported
- a translation is missing
- the user has not selected a language

---

## 34.4 Missing localization

Missing localized values are valid.

Applications must apply deterministic fallback behavior rather than treating incomplete localization as corrupt content.

---

## 34.5 Syriac source priority

When a field represents an authoritative Syriac liturgical text, the Syriac value remains primary.

Translations and localized descriptions must not replace or alter the Syriac source.

---

# 35. Entity Identifiers

Every reusable and contextual entity must have an identifier unique within its entity collection.

Examples include:

```text
occasionId
dayId
prayerId
prayerSequenceId
liturgicalItemId
qoloId
melodyId
textId
petgomoId
locationId
qintoId
groupId
mediaAssetId
```

---

## 35.1 Stability

Reusable entity identifiers must remain stable across content versions.

A correction to descriptive data must not create a new identity.

Examples of changes that normally preserve identity include:

- spelling correction
- title correction
- translation update
- metadata expansion
- replacement of a media file
- addition of a new related entity

---

## 35.2 New identity

A new identifier is required when the Author Database determines that the content represents a distinct entity rather than a revision of the existing one.

The editorial system, not the application, determines entity identity.

---

## 35.3 Identifier opacity

Applications must treat identifiers as opaque values.

They must not infer meaning from:

- numeric ranges
- prefixes
- formatting
- database table origins
- insertion order
- visible titles

For example, an application must not assume that a larger numeric identifier represents later liturgical order.

---

## 35.4 Cross-package identity

When the same reusable entity appears in multiple packages, it should retain the same identifier whenever those packages originate from the same platform identity system.

This supports:

- package comparison
- shared caches
- bookmarks
- synchronization
- content updates
- future cross-application links

---

## 35.5 Contextual entity identity

Contextual entities such as PrayerSequence and LiturgicalItem also require stable identifiers when possible.

Their identity should survive content revisions when they continue to represent the same contextual occurrence.

This supports future features such as:

- bookmarks
- playback progress
- annotations
- user preferences
- direct links
- migration between content versions

---

# 36. Explicit Ordering

Ordering is always represented explicitly on the relationship that owns the sequence.

The model does not rely on physical serialization order.

Examples include:

```text
OccasionPrayerSequenceRef.sort
DayPrayerSequenceRef.sort
PrayerSequenceLiturgicalItemRef.sort
TextOccurrenceContent.sort
EntryPoint.sort
```

---

## 36.1 Relationship-owned order

Order belongs to the contextual relationship, not to the reusable entity.

For example:

```text
PrayerSequence A
└── LiturgicalItem X at sort 3
```

does not imply that LiturgicalItem X must always be third wherever a related concept appears.

---

## 36.2 Sort-value requirements

Within one ordered collection, sort values must produce a deterministic sequence.

The Build Tools may use:

- consecutive integers
- spaced integers
- another sortable numeric strategy

The exact encoding belongs to the Application Package Specification.

---

## 36.3 Duplicate sort values

Duplicate sort values inside the same ordered relationship are invalid unless the specification explicitly defines a secondary deterministic ordering rule.

Version 1.0 should prefer unique sort values within every ordered sibling collection.

---

## 36.4 Missing order

When order is semantically required, a missing `sort` value is invalid.

Applications must not silently substitute:

- identifier order
- title order
- database order
- file order

---

# 37. Effective Values

Some values are inherited or selected through editorial rules.

These values must be resolved by the Build Tools before package generation.

Examples include:

```text
effectiveQintoId
effectiveMelodyId
```

---

## 37.1 Meaning of effective

The word `effective` indicates that the value is the final runtime result after all relevant editorial rules have been applied.

It does not describe the source from which the value originated.

---

## 37.2 Resolution responsibility

The Build Tools may consider:

- direct LiturgicalItem assignments
- PrayerSequence assignments
- Occasion defaults
- Day defaults
- seasonal rules
- editorial overrides
- local tradition settings
- source-specific exceptions

The Application Content Model does not reproduce this rule system.

It stores only the resolved result.

---

## 37.3 Runtime behavior

The Core Engine reads effective values directly.

It must not attempt to repeat the inheritance process.

Conceptually:

```text
effectiveQintoId exists
        │
        ▼
use it directly
```

not:

```text
check item
    otherwise check prayer
        otherwise check occasion
            otherwise infer
```

---

## 37.4 Optional effective values

An effective value may be absent when no valid assignment exists.

Absence is a meaningful state.

Applications must not fabricate a fallback unless the application specification explicitly defines one.

---

# 38. Build Tools Responsibilities

The Build Tools transform editorial knowledge into a valid Application Content Package.

Their responsibilities include more than simple data export.

---

## 38.1 Content selection

The Build Tools determine which entities are required by the target application.

A small application may include only a limited subset of the Author Database.

A larger application may include extensive reusable catalogs.

---

## 38.2 Relationship resolution

The Build Tools resolve all relationships required at runtime.

This includes:

- Occasion to PrayerSequence
- Day to PrayerSequence
- PrayerSequence to Prayer
- PrayerSequence to LiturgicalItem
- LiturgicalItem to Qolo
- LiturgicalItem to Location
- LiturgicalItem to TextOccurrence
- TextOccurrence to Text
- TextOccurrence to Petgomo
- entity to MediaAsset

---

## 38.3 Inheritance resolution

The Build Tools calculate final inherited values.

Applications receive effective values, not unresolved editorial rules.

---

## 38.4 Ordering

The Build Tools export explicit ordering for every contextual sequence.

---

## 38.5 Dependency inclusion

When an exported entity references another entity, the referenced entity must also be included unless the package profile explicitly permits an external dependency.

For example, exporting a LiturgicalItem requires inclusion of its referenced:

- Qolo
- Location
- effective Qinto, when present
- effective Melody, when present
- Texts
- Petgome, when present
- MediaAssets, when present

---

## 38.6 Deduplication

Reusable entities must be emitted once per package collection.

Repeated contextual appearances must reference the same reusable identity rather than duplicating its complete content.

---

## 38.7 Validation

Before package generation is considered successful, the Build Tools must validate:

- identifier uniqueness
- reference integrity
- ordering
- required fields
- effective-value resolution
- media availability
- package metadata
- language metadata
- index consistency, when indexes are present

---

## 38.8 Traceability

The Build Tools should preserve enough source information to trace generated content back to the Author Database.

This may include:

- source identifiers
- source revision
- export timestamp
- transformation version
- diagnostic reports

Such information may appear in package metadata or build reports.

It must not expose implementation-specific database logic to applications.

---

## 38.9 Reproducibility

Given the same:

- Author Database revision
- Build Tools version
- package configuration
- selected application profile

the Build Tools should produce logically equivalent content.

Where byte-for-byte reproducibility is required, the Application Package Specification must define deterministic serialization and archive rules.

---

# 39. Core Engine Responsibilities

The Core Engine provides a stable runtime interface between package content and applications.

It consumes the Application Content Model without interpreting the Author Database.

---

## 39.1 Package loading

The Core Engine loads and validates the package structure required by the application.

---

## 39.2 Entity lookup

The Core Engine provides efficient access to entities by identifier.

Examples include:

```text
getOccasion(id)
getPrayerSequence(id)
getLiturgicalItem(id)
getQolo(id)
getText(id)
getMediaAsset(id)
```

The actual programming interfaces belong to implementation documentation.

---

## 39.3 Relationship traversal

The Core Engine follows explicit references exported in the package.

For example:

```text
Occasion
    → PrayerSequence reference
    → PrayerSequence
    → LiturgicalItem reference
    → LiturgicalItem
    → TextOccurrence
    → Text
```

---

## 39.4 Ordering

The Core Engine respects exported sort values.

It does not invent or infer ordering.

---

## 39.5 Localization

The Core Engine may provide consistent language fallback behavior for localized descriptive fields and translations.

It must preserve the authoritative source content.

---

## 39.6 Media resolution

The Core Engine resolves MediaAsset identifiers to package-relative resources.

It does not require applications to know the package's internal physical layout.

---

## 39.7 Search

The Core Engine may expose search services based on:

- exported search indexes
- runtime-generated indexes
- normalized values
- entity metadata

Search results must resolve to canonical entities.

---

## 39.8 Compatibility checks

The Core Engine verifies whether it supports the package's `schemaVersion`.

It may reject packages using incompatible structural versions.

---

# 40. Application Responsibilities

Applications are responsible for presentation and user interaction.

They consume services exposed by the Core Engine.

---

## 40.1 Navigation

Applications present the navigation structure provided by EntryPoints, Occasions, Days, PrayerSequences, and LiturgicalItems.

---

## 40.2 Rendering

Applications render:

- titles
- Syriac texts
- translations
- contextual Petgome
- musical information
- media controls
- notation
- descriptive content

---

## 40.3 User preferences

Applications may manage user-specific state such as:

- selected language
- font size
- theme
- playback preferences
- bookmarks
- reading progress
- downloaded media

Such state is not part of the Application Content Package.

---

## 40.4 Prohibited responsibilities

Applications must not:

- query the Author Database
- reconstruct editorial relationships
- calculate Qinto inheritance
- choose contextual Melodies from general relationships
- infer Text selections
- derive liturgical order from identifiers
- modify package content
- treat metadata as hidden liturgical logic

---

# 41. Responsibility Boundary

The division of responsibility may be summarized as follows:

```text
Author Database
    stores editorial truth

Build Tools
    interpret and resolve editorial truth

Application Content Package
    stores the resolved runtime representation

Core Engine
    loads and exposes the representation

Application
    presents the content to the user
```

A violation occurs whenever responsibility moves backward across these boundaries.

For example:

```text
Application calculates Qinto inheritance
```

is invalid because it transfers Build Tools responsibility into the application.

Likewise:

```text
Build Tools permanently redefine liturgical identity
```

is invalid because identity belongs to the Author Database and Domain Model.

---

# 42. Referential Integrity

Every reference inside the package must resolve to an existing entity of the expected type.

Examples include:

```text
PrayerSequenceContent.prayerId
    → PrayerContent.id

LiturgicalItemContent.qoloId
    → QoloContent.id

LiturgicalItemContent.locationId
    → LocationContent.id

TextOccurrenceContent.textId
    → TextContent.id

TextOccurrenceContent.petgomoId
    → PetgomoContent.id
```

---

## 42.1 Missing required references

A missing required target makes the package invalid.

For example, a LiturgicalItem referencing a Qolo that is not included in the package is invalid.

---

## 42.2 Missing optional references

An optional field may be absent.

However, when an optional reference is present, its target must exist.

For example:

```text
petgomoId absent
```

is valid.

But:

```text
petgomoId references unknown entity
```

is invalid.

---

## 42.3 Type correctness

A reference must resolve to the correct entity collection.

An identifier existing in another collection does not satisfy the reference.

For example:

```text
qoloId
```

must resolve to QoloContent, even when the same textual identifier happens to exist as a TextContent identifier.

---

## 42.4 Orphan reusable entities

A package may contain reusable entities that are not referenced by contextual content when they are intentionally included for:

- browsing
- search
- study
- future navigation
- complete repertoire access

Such entities are not necessarily invalid.

The package profile determines whether unused entities are permitted.

---

## 42.5 Orphan contextual entities

Contextual entities such as PrayerSequence or LiturgicalItem should normally be reachable from an EntryPoint, Occasion, Day, or another contextual structure.

Unreachable contextual entities should produce a validation warning or error according to the package profile.

---

# 43. Package Profiles

The same Application Content Model may support different application profiles.

A profile defines which parts of the model are required for one category of application.

Conceptual examples include:

```text
Occasion Application Profile
Daily Prayer Profile
Shhima Profile
Full Library Profile
Study Profile
```

---

## 43.1 Occasion Application Profile

An occasion-oriented package may require:

```text
metadata
entryPoints
occasions
prayers
prayerSequences
liturgicalItems
locations
qolos
qintos
melodies
texts
petgome
mediaAssets
```

It may omit:

```text
days
```

---

## 43.2 Daily Prayer Profile

A daily prayer package may use:

```text
days
prayerSequences
prayers
liturgicalItems
```

while omitting occasion-specific collections.

---

## 43.3 Full Library Profile

A full-library package may include:

- complete Qolo catalogs
- complete Text catalogs
- Melody catalogs
- Group hierarchies
- search indexes
- broad media collections
- multiple entry points

---

## 43.4 Profile rules

Profiles define required and optional collections.

They must not redefine the meaning of the core entities.

A Qolo remains the same conceptual entity across every profile.

---

# 44. Package Completeness

A package is complete when it contains all content required to satisfy its declared application profile without consulting the Author Database.

Completeness does not require exporting the entire platform database.

It requires exporting the full dependency closure of the selected application content.

---

## 44.1 Dependency closure

Conceptually:

```text
Selected EntryPoints
        │
        ▼
Occasions or Days
        │
        ▼
PrayerSequences
        │
        ▼
Prayers and LiturgicalItems
        │
        ▼
Qolos, Locations, Qintos, Melodies
        │
        ▼
Texts, Petgome, MediaAssets
```

Every required referenced entity must be included.

---

## 44.2 Self-contained runtime behavior

A complete package allows the Core Engine to provide its intended functions offline unless the package profile explicitly declares external resources.

The default architectural expectation is self-contained content.

---

## 44.3 External dependencies

Future specifications may permit explicitly declared external media or supplemental packages.

Such dependencies must never be implicit.

The core model assumes that undeclared external lookup is invalid.

---

# 45. Immutability and Updates

An Application Content Package is immutable after generation.

Applications must treat it as a versioned snapshot.

---

## 45.1 Content correction

When content changes, the Build Tools generate a new package version.

Applications replace or install the new package according to update rules defined elsewhere.

---

## 45.2 User data separation

User-generated state must be stored outside the package.

Examples include:

- bookmarks
- notes
- favorites
- playback position
- reading progress
- downloaded-resource state

This separation prevents package updates from destroying user information.

---

## 45.3 Identity-based migration

Stable entity identifiers allow user data to be mapped from one content version to another.

For example:

```text
bookmark
└── liturgicalItemId
```

may remain valid after a text correction when the LiturgicalItem identity has not changed.

---

## 45.4 Removed entities

When an entity is removed from a newer package version, user data referencing it may become unresolved.

The Core Engine or application must handle this state safely.

It must not silently attach the user data to another entity based on title similarity.

---

# 46. Error Handling Principles

The package model distinguishes between structural errors, missing optional content, and unsupported features.

---

## 46.1 Structural errors

Examples include:

- duplicate identifiers
- unresolved required references
- invalid entity types
- missing required ordering
- incompatible schema version
- invalid package metadata

Structural errors may require package rejection.

---

## 46.2 Optional-content absence

Examples include:

- missing translation
- missing audio
- missing notation
- absent subtitle
- absent Petgomo
- absent effective Qinto when none applies

These are valid states and must not cause package failure.

---

## 46.3 Unsupported optional features

An older application may encounter:

- unknown metadata fields
- unsupported media types
- optional index structures
- additional localization values

It should ignore unsupported optional information when the core package remains interpretable.

---

## 46.4 No silent reconstruction

When required information is missing, applications must not attempt to reconstruct it from unrelated fields.

For example:

- missing `effectiveMelodyId` must not be replaced by the first Melody related to the Qolo
- missing `sort` must not be replaced by identifier order
- missing Text reference must not be replaced through title matching

Such reconstruction risks presenting liturgically incorrect content.

---

# 47. Validation Levels

Package validation may occur at several stages.

```text
Authoring Validation
        │
        ▼
Build Validation
        │
        ▼
Package Validation
        │
        ▼
Runtime Validation
```

---

## 47.1 Authoring validation

The Author Database validates editorial content and domain relationships.

---

## 47.2 Build validation

The Build Tools validate transformation rules, dependency resolution, and effective values.

---

## 47.3 Package validation

The generated package is checked for structural correctness, referential integrity, and completeness.

---

## 47.4 Runtime validation

The Core Engine verifies compatibility and essential integrity before exposing content to the application.

Runtime validation should not repeat the full editorial validation performed by the Build Tools.

---

# 48. Conceptual Validation Rules

A valid package should satisfy at least the following conceptual rules.

1. Every entity identifier is unique within its collection.

2. Every required reference resolves to an included entity of the correct type.

3. Every Occasion and Day contains deterministically ordered PrayerSequence references.

4. Every PrayerSequence references one valid Prayer.

5. Every PrayerSequence contains deterministically ordered LiturgicalItem references.

6. Every LiturgicalItem references one valid Qolo and one valid Location.

7. Every TextOccurrence references one valid Text.

8. Every present Petgomo reference resolves to a valid Petgomo.

9. Every present Qinto, Melody, or MediaAsset reference resolves correctly.

10. Effective values are already resolved and require no runtime inheritance.

11. Search indexes, when present, agree with canonical content.

12. The package declares a supported schema version.

13. The package includes the dependency closure required by its application profile.

The exact machine-readable validation schema belongs to the Application Package Specification.

---

# 49. Runtime Navigation Example

An application may begin with one Occasion entry point.

```text
EntryPoint
└── type: OCCASION_LIST
```

The application loads an Occasion:

```text
OccasionContent
├── id: occasion-nativity
└── prayerSequenceRefs
    ├── prayer-sequence-nativity-ramsho
    └── prayer-sequence-nativity-sapro
```

The first PrayerSequence references its Prayer identity:

```text
PrayerSequenceContent
├── id: prayer-sequence-nativity-ramsho
├── prayerId: prayer-ramsho
└── liturgicalItemRefs
    ├── item-nativity-ramsho-001
    ├── item-nativity-ramsho-002
    └── item-nativity-ramsho-003
```

One LiturgicalItem contains the contextual selection:

```text
LiturgicalItemContent
├── id: item-nativity-ramsho-001
├── locationId: location-opening
├── qoloId: qolo-250
├── effectiveQintoId: qinto-1
├── effectiveMelodyId: melody-250-03
├── textOccurrences
│   ├── text-101
│   ├── text-104
│   └── text-108
└── audioIds
    └── audio-nativity-ramsho-001
```

The Core Engine resolves the referenced reusable entities and presents a complete runtime view.

No Author Database query or liturgical inheritance calculation is required.

---

# 50. Runtime Composition View

Although package entities remain normalized and reusable, the Core Engine may expose a composed runtime object to simplify application rendering.

Conceptually:

```text
ResolvedLiturgicalItem
├── item
├── location
├── qolo
├── effectiveQinto?
├── effectiveMelody?
├── resolvedTextOccurrences[]
│   ├── occurrence
│   ├── text
│   └── petgomo?
├── audioAssets[]
└── notationAssets[]
```

This composed view is a runtime convenience.

It is not a separate canonical package entity.

---

## 50.1 Purpose

A resolved view allows the application to render one item without manually performing multiple identifier lookups.

---

## 50.2 No new interpretation

Composition resolves references only.

It must not introduce new editorial decisions.

For example, the Core Engine may load the Melody referenced by `effectiveMelodyId`, but it must not choose another Melody.

---

## 50.3 Caching

The Core Engine may cache resolved views for performance.

Cached data remains derived and may always be reconstructed from the package.

---

# 51. Model Boundaries

The Application Content Model defines runtime content structure.

It does not define every concern of the platform.

---

## 51.1 Outside the model

The following concerns are outside this document:

- Author Database table design
- Access queries and forms
- editorial workflow
- Build Tools user interface
- JSON serialization details
- archive format
- file naming conventions
- compression
- encryption
- digital signatures
- download protocol
- application user interface design
- user account management
- subscription logic
- copy-protection implementation
- analytics
- cloud synchronization

These concerns belong to other documents and system layers.

---

## 51.2 Application Package Specification

The following topics will be defined by the Application Package Specification:

- physical package layout
- manifest format
- JSON field names
- data types
- required and optional fields
- identifier encoding
- date and time format
- language-code standard
- media paths
- supported media formats
- checksums
- archive structure
- schema files
- validation procedure
- version compatibility
- package profiles
- deterministic serialization rules

---

## 51.3 Implementation documentation

Programming-language models, repository interfaces, loaders, caches, and platform-specific APIs belong to implementation documentation.

The conceptual model must remain independent from Kotlin, Swift, JavaScript, SQL, or any other implementation technology.

---

# 52. Architectural Invariants

The following invariants must remain true across every implementation of the Application Content Model.

## Invariant 1

The Author Database remains the editorial source of truth.

## Invariant 2

The Build Tools resolve editorial logic before package generation.

## Invariant 3

Applications do not reconstruct liturgical relationships.

## Invariant 4

Prayer identity is separate from PrayerSequence content.

## Invariant 5

Qolo identity is separate from LiturgicalItem appearance.

## Invariant 6

Reusable Texts, Petgome, Melodies, and MediaAssets are referenced rather than repeatedly duplicated.

## Invariant 7

Contextual ordering belongs to relationships.

## Invariant 8

Effective Qinto and Melody values are runtime-ready.

## Invariant 9

General repertoire relationships do not replace contextual selections.

## Invariant 10

Package content is immutable and versioned.

## Invariant 11

Stable identifiers are the basis of identity.

## Invariant 12

Indexes and runtime views are derived from canonical content.

Any future change that violates one of these invariants constitutes an architectural change rather than a minor implementation adjustment.

---

# 53. Summary

The Application Content Model transforms the conceptual Syriac liturgical domain into a deterministic, reusable, and runtime-oriented structure.

Its primary hierarchy is:

```text
EntryPoint
        │
        ▼
Occasion or Day
        │
        ▼
PrayerSequence
        │
        ├── Prayer identity
        │
        ▼
LiturgicalItem
        ├── Location
        ├── Qolo
        ├── effective Qinto
        ├── effective Melody
        ├── TextOccurrences
        └── MediaAssets
```

Reusable entities are stored once:

```text
Prayer
Qolo
Melody
Text
Petgomo
Qinto
Location
Group
MediaAsset
```

Contextual entities assemble them for a particular liturgical use:

```text
PrayerSequence
LiturgicalItem
TextOccurrence
Ordered References
```

The Build Tools perform all editorial interpretation.

The Core Engine loads, resolves, and exposes the resulting content.

Applications render the content without reconstructing the original database or liturgical rules.

This separation provides a stable foundation for:

- small occasion applications
- daily prayer applications
- Shhima
- a complete liturgical library
- future study and music applications

The Application Content Model therefore serves as the conceptual contract between the platform's editorial system and its runtime applications.



------------------------------------------------------------------------

# Audio and Media Architecture Alignment â€” 2026-08-21

<!-- AUDIO-ARCHITECTURE-ALIGNMENT-2026-08-21 -->

This section refines and supersedes earlier MediaAsset and media-reference
statements where they conflict with the rules below.

The purpose of this alignment is to establish the logical media model
before the physical Application Package representation and runtime audio
implementation are finalized.

## Media Architecture Principle

Media is a reusable platform capability.

Audio files, images, notation, documents, and future media resources must
not be modeled as application-specific properties or as direct filesystem
paths embedded in presentation code.

The logical flow is:

``` text
Content identity
        â†“
Media relationship
        â†“
MediaAsset
        â†“
Resource resolution
        â†“
Media service
```

Content entities describe what the content is.

MediaAsset describes the reusable media resource.

Relationship entities describe why and how a MediaAsset is associated
with content.

Runtime resource resolution determines where the physical resource is
obtained.

## MediaAsset Identity

MediaAsset represents one stable reusable media resource.

Conceptually:

``` text
MediaAsset
â”œâ”€â”€ id
â””â”€â”€ type
```

Authoring and package representations may contain additional fields
appropriate to their respective responsibilities.

MediaAsset identity is independent from:

- Melody identity;
- Qolo identity;
- LiturgicalItem identity;
- Text identity;
- filesystem location;
- cloud-provider URL;
- exact binary checksum.

A MediaAsset identifier remains stable when the physical representation
of the same logical media resource is corrected or improved.

Examples that normally preserve MediaAsset identity include:

- noise reduction;
- normalization;
- metadata correction;
- re-encoding;
- replacing a damaged copy with a corrected copy of the same recording.

A materially different performance or media resource receives a new
MediaAsset identity.

Therefore:

``` text
logical media identity
        â‰ 
exact file binary
```

The exact binary representation may be identified independently through
derived package metadata such as a checksum.

## MediaAsset Is Not Owned by Content

MediaAsset does not contain MelodyId, LiturgicalItemId, QoloId, or other
content ownership fields.

Relationships between media and content are represented explicitly.

This allows one MediaAsset to be reused by multiple content entities
without duplicating the underlying media resource.

## MelodyMedia

MelodyMedia represents a reusable many-to-many relationship between
Melody and MediaAsset.

Conceptually:

``` text
Melody
   *
   â”‚
   â”‚ MelodyMedia
   â”‚
   *
MediaAsset
```

The relationship contains contextual relationship information such as:

``` text
MelodyMedia
â”œâ”€â”€ melodyId
â”œâ”€â”€ mediaAssetId
â”œâ”€â”€ role
â””â”€â”€ sort
```

A single MediaAsset may therefore be associated with multiple Melodies.

This is required because distinct Melody identities may legitimately use
the same actual recorded performance.

Media must not be duplicated merely because several Melodies reference
the same recording.

### Role

`role` describes why the MediaAsset is related to the Melody.

It is distinct from MediaAsset type.

For example:

``` text
MediaAsset.type = AUDIO
MelodyMedia.role = RECORDING
```

Future roles may distinguish teaching, reference, performance, or other
uses when real requirements establish those distinctions.

### Sort

`sort` provides authored ordering when more than one MediaAsset has the
same applicable role.

Applications and Build Tools must not infer preference from MediaAsset
identifiers.

## LiturgicalItemMedia

LiturgicalItemMedia represents the relationship between one contextual
liturgical occurrence and a MediaAsset.

Conceptually:

``` text
LiturgicalItem
      *
      â”‚
      â”‚ LiturgicalItemMedia
      â”‚
      *
MediaAsset
```

This relationship is distinct from MelodyMedia.

MelodyMedia describes media associated with the reusable Melody itself.

LiturgicalItemMedia describes media associated with one contextual
liturgical use.

Examples include:

- a complete performance of the selected hymn occurrence;
- a recording containing the exact contextual verses;
- another contextual recording whose use depends on the liturgical
  occurrence.

The same MediaAsset may be reused by multiple LiturgicalItems.

A contextual media relationship may contain:

``` text
LiturgicalItemMedia
â”œâ”€â”€ liturgicalItemId
â”œâ”€â”€ mediaAssetId
â”œâ”€â”€ role
â”œâ”€â”€ sort
â””â”€â”€ timingSetId?
```

The exact physical representation belongs to the Application Package
Specification.

## Media Timing

Timing information does not belong to MediaAsset itself.

A MediaAsset identifies the reusable media resource.

Timing describes how portions of that resource correspond to structured
content.

Therefore fields such as:

``` text
startMs
endMs
```

must not be stored as intrinsic MediaAsset properties.

## MediaTimingSet

MediaTimingSet represents one reusable temporal segmentation of a
MediaAsset.

Conceptually:

``` text
MediaAsset
      â”‚
      â–¼
MediaTimingSet
      â”‚
      â”œâ”€â”€ MediaSegment
      â”œâ”€â”€ MediaSegment
      â””â”€â”€ MediaSegment
```

A timing set may be reused by more than one LiturgicalItemMedia
relationship when those contextual occurrences use the same recording
and the same temporal segmentation.

This prevents duplicate timing data when the same text and recording are
reused in different liturgical contexts.

## MediaSegment

MediaSegment represents one ordered temporal interval inside a
MediaTimingSet.

Conceptually:

``` text
MediaSegment
â”œâ”€â”€ id
â”œâ”€â”€ timingSetId
â”œâ”€â”€ sequence
â”œâ”€â”€ startMs
â””â”€â”€ endMs
```

Timing is represented numerically in milliseconds.

The following basic invariant applies:

``` text
0 <= startMs < endMs
```

When authoritative media duration is available:

``` text
endMs <= media duration
```

`sequence` identifies the ordered position of the segment within the
timing set.

MediaSegment does not permanently own a canonical Text or one
LiturgicalItem occurrence.

## LiturgicalTextMediaSegment

LiturgicalTextMediaSegment maps a contextual text occurrence to a
reusable MediaSegment.

Conceptually:

``` text
contextual Text occurrence
        â”‚
        â–¼
LiturgicalTextMediaSegment
        â”‚
        â–¼
MediaSegment
```

This additional relationship is required because different contextual
occurrences may use the same canonical Text, the same recording, and the
same timing segment while retaining distinct contextual identities.

For example:

``` text
Liturgical occurrence A
    Text occurrence A1 â”€â”€â–؛ Segment 1
    Text occurrence A2 â”€â”€â–؛ Segment 2

Liturgical occurrence B
    Text occurrence B1 â”€â”€â–؛ Segment 1
    Text occurrence B2 â”€â”€â–؛ Segment 2
```

The contextual Text occurrence identifiers differ.

The timing data is nevertheless stored only once.

This preserves both contextual occurrence identity and reusable timing
information.

## Verse Playback and Synchronization

The media model supports direct playback of a contextual text occurrence.

Runtime resolution may expose:

``` text
Resolved contextual text
â”œâ”€â”€ text
â””â”€â”€ audio segment
    â”œâ”€â”€ mediaAsset
    â”œâ”€â”€ startMs
    â””â”€â”€ endMs
```

Selecting the text may therefore request playback of the corresponding
interval without requiring application UI code to understand authoring
relationships.

The same timing information may also support reverse synchronization:

``` text
text selection
    â†’ seek to media segment

playback position
    â†’ identify active media segment
    â†’ identify contextual text occurrence
```

This enables future verse highlighting during continuous playback.

The Audio service itself does not need to understand Text, Qolo,
Melody, or liturgical structure.

It receives a resolved media resource and optional playback interval.

## Authoring Source versus Runtime Resource

The location used by the Author Database to find an authoring file is
not the runtime media location.

These are separate concepts:

``` text
Authoring source
        â‰ 
Application Package resource
        â‰ 
Runtime distribution location
```

The Author Database should identify a media source using a relative
authoring path.

Conceptually:

``` text
MediaSourceRoot
        +
SourceRelativePath
```

The root is environment-specific.

The relative path belongs to authoring data.

Build Tools resolve the source file and determine its published package
or distribution representation.

Absolute workstation paths must not become canonical package content.

## Local, Remote, and Hybrid Resolution

Media relationships are independent from the physical distribution
strategy.

The same MediaAsset may be resolved from an embedded/local package
resource, a local media cache, a remote/cloud media source, or through a
hybrid strategy.

A hybrid runtime may conceptually use:

``` text
if valid local resource exists
    use local resource
else if remote resource is available
    retrieve remote resource
    cache when appropriate
    use retrieved resource
else
    report media unavailable
```

The Author Database must not encode a cloud provider or deployment URL
as part of the logical content relationship.

Changing the media host must not require rewriting Melody, LiturgicalItem,
or MediaAsset relationships.

## Binary Change Detection

MediaAsset identity does not identify the exact binary bytes of a file.

Build Tools may derive technical metadata including:

``` text
mimeType
fileSize
durationMs
checksum
```

A cryptographic checksum may be used to determine whether a locally
available or cached resource represents the expected binary revision.

Therefore:

``` text
same MediaAssetId
+
different checksum
```

may legally represent an updated physical version of the same logical
media resource.

A materially different media resource receives a new MediaAssetId.

## Authoring versus Package MediaAsset

The Author Database and Application Package intentionally require
different MediaAsset representations.

The Author Database stores editorial/source facts.

Conceptually:

``` text
Author MediaAsset
â”œâ”€â”€ MediaAssetId
â”œâ”€â”€ MediaType
â””â”€â”€ SourceRelativePath
```

Build Tools inspect the actual source resource and derive physical
metadata.

The Application Package may expose runtime information such as:

``` text
Package MediaAsset
â”œâ”€â”€ id
â”œâ”€â”€ type
â”œâ”€â”€ resource location
â”œâ”€â”€ mimeType
â”œâ”€â”€ fileSize
â”œâ”€â”€ checksum
â””â”€â”€ durationMs?
```

The exact serialized fields and resource-location strategy are defined
only by the Application Package Specification.

Derived physical metadata should not become manually maintained Author
Database data unless an explicit authoring requirement later justifies
it.

## Media Relationship Summary

The logical media architecture is:

``` text
Melody
   *
   â”‚ MelodyMedia
   *
MediaAsset
   *
   â”‚ LiturgicalItemMedia
   *
LiturgicalItem

MediaAsset
   â”‚
   â””â”€â”€ MediaTimingSet
           â”‚
           â””â”€â”€ ordered MediaSegments

contextual Text occurrence
   â”‚
   â””â”€â”€ LiturgicalTextMediaSegment
           â”‚
           â””â”€â”€ MediaSegment
```

The model preserves the following distinctions:

``` text
MediaAsset
    = what media resource exists

Media relationship
    = why that resource is associated with content

MediaTimingSet
    = one reusable segmentation of that resource

MediaSegment
    = one temporal interval

LiturgicalTextMediaSegment
    = which contextual text occurrence uses that interval
```

## Media Design Invariants

The following rules are established:

1. MediaAsset is an independent reusable entity.
2. Melody and MediaAsset have a many-to-many relationship.
3. Melody media relationships are represented explicitly.
4. Relationship role and ordering belong to the relationship.
5. MediaAsset does not store a deployment URL as canonical identity.
6. Authoring uses a relative media-source path.
7. The authoring media root is environment-specific.
8. Build Tools transform authoring sources into runtime/package resources.
9. Local, cloud, and hybrid resolution are distribution/runtime policies.
10. MediaAsset identity survives physical improvements to the same
    logical resource.
11. A materially different media resource receives a new identity.
12. Binary checksums identify physical-resource changes independently
    from MediaAsset identity.
13. Replacing a physical file does not require changing MediaAssetId when
    logical media identity remains unchanged.
14. Manual media revision numbers are not required merely to detect
    binary changes when authoritative checksums are available.
15. Timing data belongs to media/content usage, not to MediaAsset itself.
16. Reusable timing data must not be duplicated merely because the same
    recording appears in multiple liturgical contexts.
17. Applications consume resolved media relationships rather than
    reconstructing Author Database relationships.
