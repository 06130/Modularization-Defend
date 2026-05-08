# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

- **Build**: `./gradlew build`
- **Run client**: `./gradlew runClient`
- **Run server**: `./gradlew runServer`
- **Run data generator**: `./gradlew runData`
- **Run gametests**: `./gradlew runGameTestServer`

Requires JDK 21. No test suite exists yet (`src/test` is empty).

## Architecture Overview

This is a **Minecraft NeoForge 1.21.1** mod (modid: `modularization_defend`) that adds a modular turret defense system. It depends on GeckoLib (animations), LDLib2 (GUI framework), Curios (accessory slot), GuideME (docs), and Photon (particle effects).

### Modular Turret Architecture

The turret system follows a **composition-over-inheritance** pattern with an explicit abstraction hierarchy:

- **`BaseTurretBlockEntity`** — Abstract BE defining the turret lifecycle. Holds a 3-slot inventory (core, guidance control, target filter), owns a `TurretFireSystem` instance, and exposes two abstract methods subclasses must implement:
  - `acquireTarget()` → returns an `EntityTracker`
  - `onFire(Entity target)` → executes an attack on the locked target
  - Tick flow: `tick()` → `fireSystem.tick()` (search + shoot) → `onCustomTick()` (optional hook for particles/animations)

- **`TurretFireSystem`** — Encapsulates all fire-control logic: target search cooldown, lock-on, re-acquisition after target loss, and fire-rate gating. Does NOT know about specific attack mechanics — it delegates to `BaseTurretBlockEntity.performFire()`.

- **`BaseTurretBlock`** — Abstract block that handles right-click interaction to install/replace turret cores, guidance controls, and target filters into BEs. Provides `dismantleStructure()` for structure teardown.

- **Concrete implementations** (e.g., `BasicBulletTurretV1BlockEntity`) wire up config values (fire rate, range, search height) into the firing system and implement `acquireTarget()` / `onFire()` using the entity tracking framework and `TurretCoreItem.executeAttack()`.

### Multiblock System

Turrets can be multi-block structures. `AffiliateBlock` / `AffiliateBlockEntity` are "proxy" blocks that:
1. Store a reference to their main `BaseTurretBlock` position via NBT
2. Redirect `ItemHandler` capability lookups to the main block entity (with one-shot caching)
3. Right-clicks on affiliate blocks are forwarded to `BaseTurretBlock.handleInteraction()`

Structures are defined by `getStructureWidth()` / `getStructureHeight()` on the block class. A `TurretRemovalTool` item handles controlled teardown.

### Entity Tracking Framework (`entity/EntityTrace/`)

A standalone system for finding and tracking entities:

- **`EntitySearchUtil`** — Static utility with performance-optimized entity queries (float-based distance calcs, AABB pre-filtering). Supports "find nearest" and "find all" modes, plus a "fast mode" that caps to 10 candidates.
- **`EntityTracker`** — Stateful tracker that locks onto a UUID, caches the entity reference, and reports status via `TrackingState` enum (UNLOCKED → TRACKING → LOST → INVALID). Supports re-locking and target-lost callbacks.
- **`IEntitySearch`** / **`IEntityTracker`** — Interfaces providing static factory methods and the tracker contract.
- **`EntityFilter`** — `Predicate<Entity>` functional interface with factory methods in `EntityFilters` for common categories (hostile/neutral/friendly mobs, players, by entity ID).

### Data Component System (`DataComponents/`)

Three data component types use NeoForge's modern data component API (Codec-based, persistent + network-synchronized):

| Component | Key Fields | Used By |
|---|---|---|
| `TurretCoreData` | harmLevel, energyLevel, turretType, energy/maxEnergy | Turret core items |
| `DefendCoreData` | harmLevel, energyExpendLevel, energyMax/Current, shieldCapacity/Active, 5 upgrade levels | Player-worn DefendCore curio |
| `TargetFilterData` | filterType (HOSTILE/NEUTRAL/FRIENDLY/PLAYER/ENTITY_ID), optional entityId | Target filter items |

All use `RecordCodecBuilder` for serialization, supporting both `CODEC` (disk) and `STREAM_CODEC` (network via `ByteBufCodecs.fromCodec`).

### Projectile System (`entity/Projectile.java`)

Abstract base extending Minecraft's `Projectile` with:
- Smooth client interpolation (manages `deltaMovementOld` to prevent first-frame flicker)
- Optional homing guidance via `handleHoming()` (linear interpolation steering)
- Collision detection via `ProjectileUtil.getHitResultOnMoveVector`
- Subclass hooks: `onHitEntity()`, `onHitBlock()`, `onClientTick()`, `onServerTick()`

### Config System (`config/`)

Three modules loaded as separate TOML files under `config/modularization_defend/`:
- **`GeneralConfig`** — max connection distance, heartbeat interval, turret core upgrade caps
- **`TurretConfig`** — per-turret-type stats (e.g., `BasicBulletV1Stats`: fireRate, damage, range, energyCapacity, searchHeight)
- **`DebugConfig`** — enable flag + log level (TRACE/DEBUG/INFO/WARN/ERROR)

Config values are accessed via static getters on the `Config` class.

### Network Layer (`network/`)

Uses NeoForge's `PayloadRegistrar` (protocol version "1"). Currently has a single packet: `OpenDefendCoreGUIMessage` (client→server, play phase).

### UI (`ldlibUI/`)

DefendCore GUI is built with LDLib2's `ModularUI` system. `DefendCoreContainerMenu` is the server-side container, `DefendCoreGUI` builds the `ModularUI` widget tree, and `NewUIScreen` wraps it in an `AbstractContainerScreen`. The GUI is registered to open via keybind (default: G).

### Registration & Events

All registrations use NeoForge's `DeferredRegister` system, declared in `ModularizationDefend`'s constructor. Event handlers are split:
- **Mod bus** (`@EventBusSubscriber(bus = Mod)`) — `Config` (config load/reload, player join welcome message)
- **Forge bus** — `RightClickItemHandler`, `ClientKeyInputHandler`, `EntityIdFilter`
- **Client setup** — `ClientModEvents` inner class in `ModularizationDefend` (renderers, screens)

### Key Package Reference

| Package | Purpose |
|---|---|
| `Register/` | All deferred registries (blocks, items, BEs, entities, menus, data components, creative tabs, keybindings, capabilities) |
| `Blocks/MultiblockFrame/` | Abstract turret framework (base BE, base block, affiliate block, fire system) |
| `Blocks/Multiblock/` | Concrete turret implementations |
| `Items/TurretCore/` | Turret core items (abstract `TurretCoreItem` + concrete types) |
| `Items/TargetFilter/` | Target filter items (abstract `BaseTargetFilter` + per-type concrete filters) |
| `Items/GuidanceControl/` | Guidance control components |
| `entity/EntityTrace/` | Entity search and tracking framework |
| `entity/` | Projectile entities (abstract `Projectile`, `LaserProjectile`) |
| `config/` | Config spec classes (NeoForge `ModConfigSpec`) |
| `network/` | Network packet registration and message handlers |
| `ldlibUI/` | LDLib2-based GUI screens and containers |
| `util/` | Debug logger (sharded output), debug command, energy formatting, time helpers |