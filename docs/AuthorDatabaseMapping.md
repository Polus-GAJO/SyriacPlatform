# SyriacPlatform --- Author Database Mapping

**Status:** Engineering Reference --- Phase 7\
**Version:** 1.1-draft\
**Baseline:** current Phase-7 Build Tools working baseline ---
2026-08-18\
**Scope:** Author Database → Build Tools → Application Package Schema v1

------------------------------------------------------------------------

## 1. Purpose

This document defines the authoritative mapping boundary between the
existing Microsoft Access Author Database and SyriacPlatform Application
Package Schema v1.

The Author Database remains the authoritative editorial source. Build
Tools interpret its tables and relationships, validate source facts,
resolve build-time rules, and emit application-ready packages. Runtime
code must not depend on Access table names, query order, relationship
traversal, authoring-only tables, or temporary placeholder values.

This document is a mapping contract, not a replacement schema for the
Author Database.

------------------------------------------------------------------------

## 2. Architectural Boundary

``` text
Author Database
        ↓
Controlled Export
        ↓
Build Tools
        ↓
Application Package Schema v1
        ↓
Existing Core validation/runtime
        ↓
Application
```

Rules:

1.  Stable source identifiers are preserved whenever the source entity
    and package entity have the same conceptual identity.
2.  Editorial relationships are interpreted in Build Tools, not
    reconstructed by applications.
3.  Authored ordering and legal repetition are preserved.
4.  Temporary authoring placeholders must not become published domain
    categories.
5.  Build Tools must not invent missing editorial facts merely to make a
    package valid.
6.  Schema v1 is the output contract. No parallel runtime content path
    is introduced.

------------------------------------------------------------------------

## 3. Author Database Snapshot

The version-controlled structural snapshot is stored under:

``` text
author-database/schema/
├── tables.json
├── relationships.json
└── indexes.json
```

The exporter source is stored under:

``` text
author-database/tools/modSchemaExporter.bas
```

Representative real data used during Phase 7 mapping analysis is stored
under:

``` text
author-database/samples/mapping-analysis/
```

The current representative slice is `OccN = 1`.

------------------------------------------------------------------------

## 4. Source Table Classification

### 4.1 Canonical content sources

  Author Database table   Role in Schema v1
  ----------------------- ----------------------------------------------------------
  `Occasion`              Canonical Occasion source
  `Prayers`               Canonical Prayer source
  `Qolos`                 Canonical Qolo source
  `Texts`                 Canonical TextContent source
  `Petgomo`               Canonical Petgomo source
  `Melody`                Canonical Melody source
  `Qinto`                 Canonical Qinto source, subject to placeholder filtering

### 4.2 Contextual and composition sources

  -----------------------------------------------------------------------
  Table                               Meaning for Build Tools
  ----------------------------------- -----------------------------------
  `ExistsIn`                          Reusable liturgical placement /
                                      contextual LiturgicalItem source

  `OccaExis`                          Connects an Occasion to `ExistsIn`
                                      placements

  `ExistsInText`                      Assigns ordered Text occurrences to
                                      an `ExistsIn` placement

  `PetExis`                           Assigns contextual Petgomo
                                      information to a Text occurrence

  `TextMelody`                        Authoring relationship between Text
                                      and Qolo; not the authoritative
                                      contextual verse-selection source
  -----------------------------------------------------------------------

### 4.3 Supporting authoring/domain data

  -----------------------------------------------------------------------
  Table                               Current treatment
  ----------------------------------- -----------------------------------
  `Groups`                            Qolo classification support; no
                                      required Schema-v1 collection

  `Location`                          Liturgical descriptor support; no
                                      required Schema-v1 collection

  `Days`                              Future day-based entry-context
                                      source; `days.json` is reserved,
                                      not required in Schema v1

  `Books`                             Authoring/reference support

  `Months`                            Occasion metadata support

  `Notes`                             Authoring metadata

  `Subjects`                          Authoring classification

  `TextSubjects`                      Authoring classification
                                      relationship

  `MusicNotes`                        Future notation/media source

  `MNMelody`                          Future notation/media relationship

  `RofMP3`                            Future audio/media source
  -----------------------------------------------------------------------

### 4.4 Derived, temporary, operational, or excluded data

  -----------------------------------------------------------------------
  Table                               Treatment
  ----------------------------------- -----------------------------------
  `TextIndex`                         Derived/search data; not canonical
                                      package content

  `SimilarTextsTemp`                  Temporary authoring data; exclude

  `tblTempCopyPrayer`                 Temporary authoring data; exclude

  `tblTempResults`                    Temporary authoring data; exclude

  `Paste Errors`                      Operational/error data; exclude

  `Users`                             Authoring/user-administration data;
                                      exclude completely from content
                                      packages
  -----------------------------------------------------------------------

------------------------------------------------------------------------

## 5. Canonical Entity Mapping

### 5.1 Occasion

  Schema v1 property   Author Database source
  -------------------- ------------------------
  `Occasion.id`        `Occasion.OccN`
  `Occasion.name`      `Occasion.Occasion`

`OccN` is preserved as the stable Occasion identifier.

### 5.2 Prayer

  Schema v1 property   Author Database source
  -------------------- ------------------------
  `Prayer.id`          `Prayers.PrayerN`
  `Prayer.name`        `Prayers.Prayer`

`PrayerN` is preserved as the stable Prayer identifier.

### 5.3 TextContent

  Schema v1 property     Author Database source
  ---------------------- ------------------------
  `TextContent.id`       `Texts.TextID`
  `TextContent.syriac`   `Texts.TheText`

Additional translation/content fields shall be mapped only where the
current Schema-v1 DTO/domain model defines corresponding properties.

### 5.4 Petgomo

  Schema v1 property   Author Database source
  -------------------- ------------------------
  `Petgomo.id`         `Petgomo.PetN`
  `Petgomo.syriac`     `Petgomo.Petgomo`

### 5.5 Qolo

  Schema v1 property   Author Database source
  -------------------- ------------------------
  `Qolo.id`            `Qolos.QoloN`
  `Qolo.groupId`       `Qolos.GroupN`
  `Qolo.sort`          `Qolos.Sort`
  `Qolo.name`          `Qolos.Qolo`
  `Qolo.searchName`    `Qolos.QoloSerch`
  `Qolo.poeticMeter`   `Qolos.Poetic`

Canonical Qolo does not own the selected contextual verses for a
liturgical occurrence.

### 5.6 Melody

  Schema v1 property      Author Database source
  ----------------------- ------------------------
  `Melody.id`             `Melody.MelodyN`
  `Melody.qoloId`         `Melody.QoloN`
  `Melody.name`           `Melody.Melody`
  `Melody.searchName`     `Melody.MelodySerch`
  `Melody.hasRecording`   `Melody.Record`

Any Qinto relationship carried by `Melody` participates in build-time
melody resolution as described in Section 10.

### 5.7 Qinto

  Schema v1 property   Author Database source
  -------------------- ------------------------
  `Qinto.id`           `Qinto.QintoN`
  `Qinto.name`         `Qinto.Qinto`

`QintoN = 0` is an authoring placeholder meaning that the Qinto has not
yet been determined. It MUST NOT be treated as a resolved canonical
Qinto in a published package.

A `NULL` Qinto likewise means that no explicit Qinto is currently
supplied at that source position. It must not be converted into an
arbitrary default.

------------------------------------------------------------------------

## 6. Occasion Composition

The source relationship path is:

``` text
Occasion
    ↓
OccaExis
    ↓
ExistsIn
```

`OccaExis` associates an Occasion with reusable `ExistsIn` placements.

For an Occasion package slice, Build Tools select the `ExistsIn` records
referenced through `OccaExis` for that Occasion and use those records to
construct application-ready prayer/liturgical structures.

The application must never reconstruct this Access relationship path at
runtime.

------------------------------------------------------------------------

## 7. LiturgicalItem Mapping

`ExistsIn` is the authoritative contextual placement source for the
current prayer-oriented mapping.

Primary mapping:

  Package concept                 Source
  ------------------------------- ----------------------
  `LiturgicalItem.id`             `ExistsIn.ID`
  Prayer association              `ExistsIn.PrayerN`
  contextual Qolo target          `ExistsIn.QoloN`
  explicit/current Qinto source   `ExistsIn.QintoN`
  liturgical Location source      `ExistsIn.LocationN`
  source order                    `ExistsIn.Sort`

Other `ExistsIn` fields such as `BookN`, `NoteN`, and `DayN` remain
available to Build Tools but shall enter Schema v1 only where an
explicit package/domain rule requires them.

`ExistsIn.ID` is preserved as the contextual occurrence identity; Build
Tools must not replace it with a newly generated identifier without a
future explicit migration rule.

------------------------------------------------------------------------

## 8. PrayerSequence Projection

The Author Database currently has no dedicated `PrayerSequence` entity
or sequence identifier.

For the current Phase-7 mapping, Build Tools construct the package
`PrayerSequence` as a projection required by the existing Core.

### 8.0 PrayerSequence identity

`PrayerSequence` has no direct Author Database identity. Its Schema-v1
identifier is therefore a deterministic Build Tools projection derived
from the source identities that define the sequence context.

For the current Occasion-oriented Phase-7 mapping:

``` text
PrayerSequence.id =
    (Occasion.id << 32) | Prayer.id
```

Both source identifiers must be positive 32-bit Author Database
identifiers.

This projection guarantees that:

the same (Occasion, Prayer) pair always produces the same
PrayerSequence.id; the same Prayer used in different Occasions produces
distinct PrayerSequence identities; no query order, row order, localized
name, or generated sequence counter participates in identity; Prayer and
Occasion source identifiers remain unchanged.

The numeric value of the projected identifier carries no liturgical
ordering or precedence. It is an engineering identity only.

A future explicit Author Database PrayerSequence identity may replace
this projection through a documented migration rule.

### 8.1 Prayer ordering

At the current source baseline, prayers are presented in their
established natural order by `PrayerN`. Until the Author Database gains
an explicit prayer-order field, Build Tools may use `PrayerN` to
construct the ordered prayer list for the applicable entry context.

This is a temporary source-ordering rule. `PrayerN` remains primarily an
identifier; future explicit authoring order shall replace this rule
without changing Prayer identity.

### 8.2 LiturgicalItem ordering inside a Prayer

For each Prayer, the relevant `ExistsIn` records are ordered by authored
`ExistsIn.Sort`.

The resulting ordered identifiers become the
`PrayerSequence.liturgicalItemIds` representation required by Schema
v1/Core.

Legal repetition must be preserved.

### 8.3 Equal sort values

Equal authored sort values are legal in the Author Database and may
intentionally express equality at an authoring level. Therefore Build
Tools MUST NOT automatically classify duplicate `Sort`/`SortInPra`
values as corrupt data solely because they are equal.

Where Schema v1 requires a deterministic linear array, Build Tools must
apply an explicit deterministic tie policy that preserves the authored
equality rather than pretending the source supplied a strict order. The
final tie policy is an implementation rule to be fixed before production
package generation; source record identity/order may be used only if
documented as serialization determinism, not as additional liturgical
meaning.

------------------------------------------------------------------------

## 9. Contextual Text and Petgomo Mapping

### 9.1 Verse source

For a Qolo occurrence represented by `ExistsIn.ID`, contextual verses
are selected from:

``` text
ExistsIn.ID
    ↓
ExistsInText.ExistsInID
```

Mapping:

  Schema-v1 contextual verse property   Source
  ------------------------------------- --------------------------
  `verses[].textId`                     `ExistsInText.TextID`
  authored verse order                  `ExistsInText.SortInPra`

`ExistsInText.ID` is the identity of the source occurrence/link. It does
not become a new canonical Text identifier.

A Qolo may exist in the source while its contextual text list is
incomplete or temporarily empty during authoring. Build Tools must
distinguish incomplete authoring data from nonexistent canonical Qolo
identity.

### 9.2 Petgomo context

The source path is:

``` text
ExistsInText
    ↓
PetExis
    ↓
Petgomo
```

`PetExis.ExistInTextID` identifies the contextual text occurrence to
which the Petgomo assignment belongs.

`PetExis.PetN` maps to:

``` text
verses[].petgomoId
```

The Petgomo is contextual to the text occurrence. It is not a permanent
property of canonical `TextContent`.

Where `PetExis.TextID` is present, Build Tools should validate that it
agrees with the `TextID` of the referenced `ExistsInText` record.

### 9.3 TextMelody boundary

`TextMelody` is an authoring convenience/relationship connecting Texts
and Qolos. It is not the authoritative source for contextual `verses[]`
in Schema v1.

Contextual verse selection comes from `ExistsInText`.

Build Tools may use `TextMelody` for validation or future authoring
logic only when a specific rule requires it.

------------------------------------------------------------------------

## 10. Qinto and Melody Resolution

### 10.1 General rule

The source model permits more than one Melody for the same Qolo and
Qinto. Therefore this rule is invalid in the general case:

``` text
Qolo + Qinto → exactly one Melody
```

Build Tools must resolve the contextual result explicitly before
emitting an `effectiveMelodyId` required by a package occurrence.

### 10.2 Resolved single-Melody case

If:

1.  the contextual Qinto is explicitly resolved (`QintoN > 0`); and
2.  exactly one Melody is applicable to the `(QoloN, QintoN)`
    combination;

then Build Tools may set:

``` text
effectiveMelodyId = Melody.MelodyN
```

### 10.3 Ambiguous case

If more than one Melody is applicable to the same contextual
`(QoloN, QintoN)` combination, Build Tools MUST NOT choose a Melody by:

-   lowest/highest `MelodyN`;
-   row order;
-   insertion order;
-   first query result;
-   arbitrary default.

The source occurrence is classified as unresolved melody ambiguity until
an explicit authoring rule identifies the intended result.

A future Author Database design may need to represent either:

-   one explicitly preferred/effective Melody; or
-   multiple intended Melodies when the liturgical meaning genuinely
    requires more than one.

These two cases must not be conflated.

### 10.4 Undetermined Qinto

`QintoN = 0` means that the author has not yet determined the Qinto. It
is not a canonical Qinto value.

A null Qinto likewise does not authorize arbitrary melody selection.

For such cases Build Tools must preserve the unresolved state at the
source-validation stage and must not fabricate `effectiveMelodyId`.

### 10.5 Representative-data finding

The Phase-7 representative slice confirmed real cases where one
`(QoloN, QintoN)` combination has multiple Melodies. Therefore melody
ambiguity is a real source-domain case, not merely a theoretical future
possibility.

The representative slice also contains Qolo 319 (the Psalm) with many
occurrences whose Qinto remains intentionally undetermined during
authoring. This is expected source state and must not be normalized into
a false Qinto.

------------------------------------------------------------------------

## 11. EntryPoint Projection

`EntryPoint.id` is not an Author Database field and is not expected to
become one merely to satisfy Schema v1.

EntryPoint is an application/package projection.

Each application chooses its entry point according to the highest
relevant node of its navigation/content hierarchy. Examples include:

``` text
Occasions application → Occasion list/root
Shhima application    → Day list/root
Single-rite package   → selected Prayer or ordered liturgical content
```

Build Tools/package configuration is responsible for producing the
Schema-v1 EntryPoint representation required by the target application
profile.

EntryPoint identity must be deterministic and stable for the same
package/application configuration, but it is not a canonical Author
Database content identity.

### 11.1 EntryPoint identity

For the current single-Occasion package projection, Build Tools derive
the EntryPoint identifier deterministically from the target Occasion:

``` text
EntryPoint.id = Occasion.id
```

This does not mean that EntryPoint and Occasion are the same domain
entity. Their identifiers belong to distinct typed identifier
namespaces.

The rule is valid for the current Occasion-oriented projection only.
Other application profiles may define their own deterministic EntryPoint
identity policy when their navigation root differs.

EntryPoint identity must never depend on display names, query order, or
arbitrary generated counters.

------------------------------------------------------------------------

## 12. Incomplete Authoring Data

The Author Database is a living authoring system. During editorial work
it is valid for some Qolo occurrences to have incomplete text
assignments, for only part of a prayer to have been entered, or for a
Qinto to remain undetermined.

Therefore the controlled export and Build Tools must distinguish at
least:

``` text
valid complete source data
valid but incomplete authoring data
invalid/inconsistent source data
resolved build-time data
unresolved package-blocking data
```

Incomplete authoring data must not be silently repaired by inventing
Texts, Qintos, Melodies, Petgomos, or relationships.

Whether an incomplete source condition blocks package generation depends
on the package profile and whether the missing fact is mandatory for the
emitted Schema-v1 structure.

------------------------------------------------------------------------

## 13. Validation Rules

Build Tools shall perform source validation before or during mapping and
shall then pass the generated package through the existing Schema-v1
validation pipeline.

### 13.1 Identifier validation

-   Preserve canonical source identifiers.
-   Reject conflicting duplicate canonical identifiers.
-   Do not generate replacement IDs merely because a source identifier
    is non-sequential.

### 13.2 Referential validation

Validate, where applicable:

-   `OccaExis.ExistInID` → `ExistsIn.ID`;
-   `ExistsIn.PrayerN` → `Prayers.PrayerN`;
-   `ExistsIn.QoloN` → `Qolos.QoloN`;
-   resolved `ExistsIn.QintoN` → `Qinto.QintoN`;
-   `ExistsInText.ExistsInID` → `ExistsIn.ID`;
-   `ExistsInText.TextID` → `Texts.TextID`;
-   `PetExis.ExistInTextID` → `ExistsInText.ID`;
-   `PetExis.PetN` → `Petgomo.PetN`;
-   contextual effective Melody → canonical `Melody.MelodyN`.

### 13.3 Contextual consistency

Where `PetExis.TextID` is populated, verify that it matches the Text
referenced by its `ExistsInText` occurrence.

### 13.4 Ordering

-   Preserve authored `ExistsIn.Sort` ordering.
-   Preserve authored `ExistsInText.SortInPra` ordering.
-   Preserve legal repetition.
-   Equal source sort values are not automatically errors; deterministic
    package serialization must not falsely assign additional liturgical
    meaning.

### 13.5 Placeholder handling

-   `QintoN = 0` is unresolved authoring state and must not become a
    published canonical Qinto.
-   Null values must not be converted into arbitrary defaults.
-   Other source placeholders discovered later must be documented here
    before Build Tools publish them as domain values.

### 13.6 Melody validation

-   Exactly one valid Melody for a resolved Qolo/Qinto may be resolved
    automatically.
-   Multiple valid Melodies require an explicit future authoring/domain
    rule.
-   Zero valid Melodies for a context that requires `effectiveMelodyId`
    is package-blocking unless the applicable profile/schema rule
    permits omission.

### 13.7 Completeness diagnostics

Build Tools should produce actionable diagnostics for incomplete source
content rather than hiding it. Diagnostics should identify source IDs
sufficiently to return to Access and correct/complete the authoring
data.

------------------------------------------------------------------------

## 14. Deterministic Generation

Build Tools should produce deterministic package output where practical.

Determinism includes:

-   stable source-to-package identifiers;
-   explicit ordering rules;
-   stable collection ordering;
-   deterministic serialization/property ordering;
-   no dependence on unspecified Access query order;
-   no arbitrary selection among ambiguous Melodies.

Deterministic serialization is an engineering property and must not be
confused with liturgical meaning. In particular, a serialization
tie-break for equal authored sort values does not mean one tied record
is liturgically earlier than another.

------------------------------------------------------------------------

## 15. Schema-v1 Collections and Current Source Responsibilities

  -----------------------------------------------------------------------
  Schema-v1 collection/concept        Current Author Database source
  ----------------------------------- -----------------------------------
  `occasions.json`                    `Occasion`

  `prayers.json`                      `Prayers`

  `prayer-sequences.json`             Build-time projection from source
                                      prayer ordering + ordered
                                      `ExistsIn`

  `liturgical-items.json`             `ExistsIn` + `ExistsInText` +
                                      contextual resolution

  contextual Qolo `verses`            `ExistsInText` + `PetExis`

  `texts.json`                        `Texts`

  `petgomos.json`                     `Petgomo`

  `qolos.json`                        `Qolos`

  `melodies.json`                     `Melody`

  Qinto content required by current   `Qinto`, excluding unresolved
  schema                              placeholder `0`

  Entry Points                        Build Tools/package configuration
                                      projection

  manifest/profile metadata           Build Tools/package configuration
  -----------------------------------------------------------------------

Reserved future collections such as `days.json`, `locations.json`,
`groups.json`, and `media-assets.json` are not introduced merely because
corresponding source data exists. Schema v1 does not require those
reserved collections.

------------------------------------------------------------------------

## 16. Representative Slice Findings --- Occasion 1

The representative export for `OccN = 1` was selected because it
contains multiple prayers and intentionally incomplete authoring data,
making it useful for validating the mapping boundary.

The analysis confirmed:

1.  `Occasion → OccaExis → ExistsIn` is a viable source path for the
    Occasion composition.
2.  `ExistsIn.Sort` provides authored LiturgicalItem ordering inside
    prayers.
3.  `ExistsInText` provides contextual Text selection and authored verse
    ordering.
4.  `PetExis` correctly expresses contextual Petgomo assignment to Text
    occurrences.
5.  Some Qolo occurrences intentionally have no Text assignments yet
    because authoring is incomplete.
6.  Equal `SortInPra` values can occur and may express equality rather
    than corruption; they require deterministic serialization without
    invented liturgical precedence.
7.  `QintoN = 0` represents an undetermined Qinto and must remain
    unresolved during source mapping.
8.  Real `(Qolo, Qinto)` combinations exist with more than one Melody,
    proving that automatic first-row/ID-based Melody selection would be
    incorrect.
9.  Qolo 319 (Psalm) commonly appears with Qinto left undetermined,
    which is expected authoring state rather than a mapping defect.

These findings are treated as source-domain facts for Phase 7 and must
be reflected in Build Tools tests.

------------------------------------------------------------------------

## 17. Open Mapping Rules

The following matters remain intentionally open and must not be solved
implicitly inside implementation code.

### 17.1 Explicit prayer order

Current source order uses `PrayerN`. A future explicit prayer-order
field is planned. When introduced, Build Tools shall prefer the explicit
order while preserving existing Prayer IDs.

### 17.2 Equal-order serialization policy

The Author Database permits equal ordering values. Before production
generation, Build Tools must define and test a deterministic
serialization tie policy that preserves equality semantically.

### 17.3 Melody authoring rule

The Author Database requires a future explicit mechanism for contexts
where `(Qolo, Qinto)` does not identify one intended Melody.

The future design must distinguish:

-   selecting one intended Melody from alternatives; and
-   intentionally requiring more than one Melody.

### 17.4 Qinto inheritance/calculation

The wider domain allows Qinto precedence/inheritance and future
calculated Qinto behavior. Phase 7 shall implement only rules that are
supported by authoritative current source facts and current package
requirements. It must not invent the future Qinto calculation system.

### 17.5 Media

Audio, notation, and other media relationships remain outside the first
Phase-7 content-generation slice unless required to validate a current
Schema-v1 profile. They will be mapped in their appropriate later phase.

------------------------------------------------------------------------

## 18. Build Tools Implementation Contract

The first Build Tools implementation shall follow this pipeline:

``` text
Controlled Author Database export
        ↓
Source DTO/model parsing
        ↓
Source validation
        ↓
AuthorDatabase mapping rules
        ↓
Schema-v1 package models
        ↓
Deterministic serialization
        ↓
Existing ApplicationPackageLoader
        ↓
Existing PackageValidator
        ↓
Existing RuntimeContentResolver
```

Build Tools MUST NOT create a second content model that bypasses Schema
v1 or the existing Core loading/validation/runtime path.

The first implementation slice should prioritize:

1.  parsing the controlled export;
2.  canonical entity mapping;
3.  Occasion/Prayer/LiturgicalItem composition;
4.  contextual Qolo verses and Petgomo;
5.  source diagnostics;
6.  Melody resolution where unambiguous;
7.  Schema-v1 package generation;
8.  end-to-end loading through the existing Core.

------------------------------------------------------------------------

## 19. Change Discipline

This document is the engineering reference for Author Database →
Schema-v1 mapping.

When implementation discovers a new source-domain case:

1.  inspect the authoritative Author Database evidence;
2.  determine whether the case belongs to authoring, Build Tools,
    package schema, Core, or application behavior;
3.  update this mapping contract if the source interpretation changes;
4.  update `ApplicationPackageSpecification.md` if the physical package
    contract changes;
5.  update implementation and tests together;
6.  do not hide unresolved domain decisions in ad-hoc mapper code.

------------------------------------------------------------------------

## 20. Current Phase-7 Implementation Status

The schema exporter and representative-data exporter established the
controlled, versioned Author Database boundary, and the first Build
Tools implementation now exists against that boundary.

The implemented development path includes:

``` text
Controlled Author Database export
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
physical Schema-v1 package
```

For the representative `OccN = 1` data currently used by the Build Tools
tests:

-   the full source composition contains both resolvable and unresolved
    authoring occurrences;
-   20 Qolo occurrences are currently resolvable and are retained in the
    development preview;
-   32 source occurrences remain package-blocking in the full
    composition;
-   `DevelopmentPreviewSlice` deliberately excludes those blocked
    occurrences without changing the identity or authored relative order
    of the included occurrences;
-   the preview can therefore generate a physical Schema-v1 Occasion
    package while the full representative source still preserves its
    diagnostics.

This development-preview mechanism is not an editorial repair mechanism
and is not permission to silently discard unresolved content in a
production package. It exists to prove the real Author Database → Build
Tools → Schema-v1 generation path using the subset that already
satisfies the current package contract.

The current Schema-v1 contract is unchanged: a published Qolo Liturgical
Item requires a valid `effectiveMelodyId`. Ambiguous or undetermined
Melody/Qinto source cases remain Build Tools concerns until an explicit
authoring/domain rule resolves them or a future schema revision
deliberately changes the package contract.

The next Phase-7 objective is to connect the generated
development-preview package to the existing Core
loading/validation/runtime path and verify the result end-to-end in the
Reference Application, while continuing to preserve the 32 unresolved
source occurrences as actionable authoring diagnostics.
