# SyriacPlatform

## Engineering Notebook v1.1

Status: Official Engineering Record  
Updated: 2026-07-17

# Purpose

This document records important engineering decisions made during the development of SyriacPlatform.

Each decision includes:

- Decision
- Reason
- Impact

---

# Decision 001

## Project Vision

### Decision

SyriacPlatform is developed as a reusable platform, not as a single application.

### Reason

The project will support multiple applications sharing the same liturgical foundation.

Examples:

- Prayer applications
- Occasion applications
- Complete library applications

### Impact

Applications consume the platform instead of rebuilding independent systems.

---

# Decision 002

## Kotlin Multiplatform Adoption

### Decision

SyriacPlatform uses Kotlin Multiplatform (KMP).

### Reason

The platform must support Android and iOS while sharing the majority of business logic.

### Impact

Shared logic is implemented in:

```text
commonMain
```

Platform-specific implementations remain isolated.

---

# Decision 003

## Platform Kernel Architecture

### Decision

All platform domains communicate through Platform Kernel.

### Reason

This creates:

- Clear responsibilities
- Independent domains
- Easier maintenance

### Impact

Domains do not directly depend on each other.

---

# Decision 004

## Domain Architecture

### Decision

The platform is divided into independent domains.

Initial domains:

- Content
- Navigation
- Audio
- Search
- User

### Reason

Each responsibility should have a clear location.

### Impact

Future expansion can add capabilities without restructuring the platform.

---

# Decision 005

## Content and User Data Separation

### Decision

Content data and user data are separated.

### Reason

Liturgical content is shared and maintained by developers, while user information belongs to each user.

### Impact

Updates to content do not affect user data.

---

# Decision 006

## Hybrid Data Architecture

### Decision

The platform uses:

Build format:

```text
JSON
```

Runtime format:

```text
SQLite
```

### Reason

JSON is suitable for building and transferring packages. SQLite is suitable for application runtime access and search.

### Impact

Applications receive prepared data packages.

---

# Decision 007

## BuildTools Separation

### Decision

BuildTools is a separate development system.

### Reason

Data preparation should not exist inside applications.

### Impact

The application remains lightweight and focused on user experience.

---

# Decision 008

## Reference Application

### Decision

A Reference Application is created to validate the platform.

### Reason

The platform needs a real application environment for testing.

### Impact

Reference Application demonstrates integration between all domains.

---

# Decision 009

## Documentation as Project Source

### Decision

Official decisions are stored inside the project documentation.

### Reason

The conversation is not a permanent technical reference.

### Impact

The project remains understandable independently of conversation history.

---

# Decision 010

## Kernel-Owned Service Initialization

### Decision

`PlatformKernel` owns initialization of all registered platform services.

### Reason

Service lifecycle is a platform responsibility. Applications and tests should not need to initialize each service manually.

The registry retains ownership of its internal storage. It provides an internal iteration operation rather than exposing the collection of service instances.

### Impact

The runtime flow becomes:

```text
Register services
→ PlatformKernel.initialize()
→ Each registered service initializes
```

This makes service startup consistent and prepares the platform for multiple services.

Implemented in commit:

```text
646cc44 — Let PlatformKernel initialize registered services
```

---

# Decision 011

## Central Platform Bootstrap

### Decision

Platform construction is centralized in `PlatformBootstrap`.

`PlatformBootstrap.create()`:

1. Creates `PlatformKernel`
2. Creates platform services
3. Registers the services
4. Calls `PlatformKernel.initialize()`
5. Returns a ready-to-use platform

### Reason

Platform construction must have one authoritative location. Without a bootstrap, each application could assemble and initialize the platform differently.

### Impact

Applications consume a ready platform and do not need to know concrete service implementations or startup order.

Implemented in commit:

```text
9ebab26 — Introduce platform bootstrap
```

---

# Decision 012

## Presentation Does Not Own Platform Startup

### Decision

The application UI must not create, register, or initialize platform services.

### Reason

Compose UI is responsible for presentation. Platform construction and lifecycle are separate responsibilities.

### Impact

`App.kt` now starts from:

```kotlin
val kernel = PlatformBootstrap.create()
```

The UI remains a consumer of platform contracts and no longer depends on `DefaultContentService` or direct `PlatformKernel` construction.

Implemented in commit:

```text
d5804a3 — Move platform startup out of App
```

---

# Current Status

Completed engineering stages:

- Repository foundation
- Kotlin Multiplatform Android foundation
- Shared common foundation
- Initial Platform Kernel
- Initial Content Domain
- MRP v0.1 visible content flow
- Kernel-managed service initialization
- Central platform bootstrap
- Application startup decoupling
- Shared test and Android debug build verification

Current engineering stage:

```text
MRP v0.2 — First Navigation State
```

---

End of Engineering Notebook v1.1
