# SyriacPlatform — Current State

Date: 2026-07-17  
Status: MRP v0.2 In Progress — Platform Startup Slice Completed

## Technology

- Kotlin Multiplatform
- Android target currently active
- Shared module: `platform/shared`
- Reference Android application: `platform/androidApp`
- Package: `org.syriacplatform`

## Completed Foundation

- `Result<T>`
- `PlatformError`
- `ErrorCode`
- `PlatformId`
- Typed IDs
- `RuntimeState`
- `Version`

## Completed Kernel

- `PlatformService`
- `ServiceMetadata`
- `ServiceRegistry`
- `PlatformKernel`
- Service registration by `KClass`
- Service resolution by `KClass`
- Kernel-managed initialization of registered services
- Registry-owned service iteration through an internal operation

## Completed Content Domain

- `ContentService` contract
- `Qolo` model
- `QoloId`
- `DefaultContentService`
- `loadQolo(qoloId)`
- Demo content data

## Completed Platform Startup

- `PlatformBootstrap` created as the central platform construction point
- `PlatformBootstrap.create()` creates the kernel
- Platform services are registered by the bootstrap
- `PlatformKernel.initialize()` initializes registered services
- The bootstrap returns a ready-to-use platform
- `App.kt` no longer creates, registers, or initializes platform services
- Compose UI consumes the platform through `PlatformBootstrap`

## Tests and Verification

- Content service behavior remains covered by shared tests
- `PlatformKernelTest` verifies registration, kernel initialization, resolution, and content loading
- `PlatformBootstrapTest` verifies that the bootstrap returns an initialized usable platform
- `:shared:allTests` passed
- `:shared:check` passed
- `:androidApp:assembleDebug` passed

## Visible MRP Result

The Android reference application successfully displays:

```text
SyriacPlatform

ܩܳܠܳܐ ܢܽܘܗܪܳܢܳܐ
```

## Current Runtime Flow

```text
Android App
→ App.kt
→ PlatformBootstrap.create()
→ PlatformKernel
→ ServiceRegistry
→ ContentService
→ DefaultContentService
→ PlatformKernel.initialize()
→ loadQolo(QoloId(1))
→ Result.Success<Qolo>
→ Compose UI
```

## Important Decisions

- Liturgical entities retain their original names: `Qolo`, `Qinto`, and `Petgomo`.
- Runtime models are immutable.
- Service contracts use explicit functions such as `loadQolo(qoloId)`.
- Services expose `RuntimeState` as read-only.
- Services modify their state internally.
- Tests belong under `commonTest`, not `commonMain`.
- The application UI must not construct or initialize platform services.
- `PlatformBootstrap` owns platform construction and service registration.
- `PlatformKernel` owns runtime service initialization.
- `ServiceRegistry` owns its internal service storage and does not expose it directly.

## Recent Commits

- `646cc44` — Let `PlatformKernel` initialize registered services
- `9ebab26` — Introduce platform bootstrap
- `d5804a3` — Move platform startup out of `App`

## Next Step

Complete MRP v0.2 by introducing the first navigation state.

Planned work:

- Define the minimal navigation state model
- Decide where navigation state is owned
- Add the first navigation contract or service only after the state model is clear
- Connect the reference application to the navigation state
- Add shared tests for the first navigation flow
- Verify the Android reference application again
