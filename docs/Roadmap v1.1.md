# SyriacPlatform

## Roadmap v1.1

Status: Official Development Plan  
Updated: 2026-07-17

# Purpose

This document defines the implementation phases of SyriacPlatform.

The roadmap focuses on building a stable foundation before expanding features.

---

# Phase 1 — Repository Foundation

Status: Completed

## Objectives

- Create project structure
- Create official documentation
- Establish development rules

## Completed

- Project folders created
- Platform Blueprint created
- Development Guide created
- Engineering Notebook created
- Roadmap created
- Project conventions introduced

---

# Phase 2 — Kotlin Multiplatform Foundation

Status: Partially Completed

## Objectives

Create the real Kotlin Multiplatform project.

## Completed

- KMP project created inside the platform module
- Android target configured
- Shared module created
- Android reference application created
- Android build process verified

## Remaining

- Configure and verify the iOS target when the project reaches the planned cross-platform stage

## Output

A working SyriacPlatform shared library with an active Android reference application.

---

# Phase 3 — Platform Common Foundation

Status: Completed

## Objectives

Implement shared basic components.

## Components

- `Result<T>`
- `PlatformError`
- `ErrorCode`
- `PlatformId`
- Typed IDs
- `RuntimeState`
- `Version`

## Output

First reusable platform layer.

---

# Phase 4 — Platform Kernel

Status: Initial Kernel Completed

## Objectives

Implement the central runtime layer.

## Completed

- `PlatformService`
- `ServiceMetadata`
- `ServiceRegistry`
- `PlatformKernel`
- Registration by `KClass`
- Resolution by `KClass`
- Kernel-managed service initialization
- Registry encapsulation maintained

## Planned Later

- Command dispatcher, only when a real use case requires it
- Event dispatcher, only when a real use case requires it
- Extended lifecycle behavior
- Failure handling across multiple service initializations

## Output

A working platform core capable of registering, initializing, and resolving services.

---

# Phase 5 — Minimal Running Platform

Status: MRP v0.1 Completed; MRP v0.2 In Progress

## MRP v0.1 — First Visible Content Flow

Status: Completed

### Includes

- Content Service
- Demo Qolo data
- Reference Android application
- Shared tests
- Visible Compose output

### Success Criteria

The reference application can load and display a Qolo through the platform.

### Result

Completed.

---

## MRP v0.2 — Reusable Startup and First Navigation State

Status: In Progress

### Completed

- Reusable `PlatformBootstrap`
- Kernel-managed service initialization
- Central runtime startup flow
- Application decoupled from service construction
- Bootstrap test
- Kernel lifecycle test
- Shared checks passed
- Android debug build passed

### Remaining

- Define the first navigation state
- Decide ownership of navigation state
- Introduce the minimum navigation contract or service
- Connect navigation state to the reference application
- Add navigation tests

### Success Criteria

The reference application starts through the bootstrap and can represent and change its first navigation state without placing platform construction logic in the UI.

---

# Phase 6 — Audio Integration

Status: Planned

## Objectives

Connect Audio Domain.

## Includes

- Playback service
- Play/Pause
- Seek
- Playback state
- Verse synchronization

---

# Phase 7 — Content Domain

Status: Initial Slice Completed; Full Domain Planned

## Objectives

Implement real liturgical content handling.

## Initial Slice Completed

- `ContentService` contract
- `Qolo` model
- `QoloId`
- `DefaultContentService`
- Demo content loading

## Planned Expansion

- Books
- Occasions
- Prayers
- Qolos
- Verses
- Melodies
- Relations
- Prepared runtime packages

---

# Phase 8 — Search Domain

Status: Planned

## Objectives

Implement content discovery.

## Includes

- Text search
- Indexed search
- Relationship search

---

# Phase 9 — User Domain

Status: Planned

## Objectives

Implement user features.

## Includes

- Preferences
- Language
- Font scaling
- Favorites
- Notes

---

# Phase 10 — BuildTools

Status: Planned

## Objectives

Connect the author database workflow.

## Includes

- Import
- Validation
- Transformation
- Indexing
- Package generation

---

# Phase 11 — First Real Application

Status: Planned

## Objectives

Build the first production application using SyriacPlatform.

## Possible Target

Liturgical prayer application.

---

# Development Principle

The roadmap follows:

```text
Foundation
↓
Kernel
↓
Domains
↓
Applications
```

New features are added only when the foundation supports them.

The platform avoids speculative infrastructure. Components such as dispatchers or additional services are introduced only when a concrete platform flow requires them.

---

End of Roadmap v1.1
