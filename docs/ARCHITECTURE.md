# CottFur Architecture Documentation

This document provides detailed technical documentation for the CottFur mod codebase.

## Table of Contents

- [Module Overview](#module-overview)
- [Main Module](#main-module)
- [Client Module](#client-module)
- [Networking](#networking)
- [Model System](#model-system)
- [Rendering Pipeline](#rendering-pipeline)
- [Animation System](#animation-system)
- [Customization System](#customization-system)
- [Mixins](#mixins)
- [Technical Notes](#technical-notes)

---

## Module Overview

CottFur uses Fabric's split source sets for clean client/server separation:

```bash
src/
├── main/           # Server & common code (runs on both client and server)
└── client/         # Client-only code (rendering, UI, keybinds)
```

---

## Main Module

Located in `src/main/kotlin/xyz/cottageindustries/cottfur/`

### Cottfur.kt

The main mod entrypoint implementing `ModInitializer`.

**Responsibilities:**

- Initialize server-side networking
- Log mod startup

**Lifecycle:** Called once when the mod loads on server or client.

### CottfurConstants.kt

Shared constants and utilities used across the mod.

**Contents:**

- `MOD_ID` - The mod identifier ("cottfur")
- `MOD_NAME` - Display name ("CottFur")
- `LOGGER` - SLF4J logger instance
- `id(path)` - Helper to create `Identifier` objects for this mod

### data/PlayerModelData.kt

Player configuration storage system.

**PlayerModelConfig** - Data class containing:

| Field | Type | Default | Description |
| ------- | ------ | --------- | ------------- |
| `modelTypeId` | String | "none" | The selected model type ID |
| `customTextureId` | String? | null | Custom texture identifier |
| `primaryColor` | Int | 0xFFFFFF | Primary fur/body color |
| `secondaryColor` | Int | 0x888888 | Secondary marking color |
| `accentColor` | Int | 0xFF0000 | Accent/highlight color |
| `patternId` | String? | null | Selected pattern type |

**PlayerModelDataManager** - Thread-safe singleton for storing player configs:

- Uses `ConcurrentHashMap` for thread safety
- Accessed by both networking and rendering code

### network/CottfurNetworking.kt

Server-side networking implementation.

**Packets Registered:**

| Packet | Direction | Purpose |
| -------- | ----------- | --------- |
| `UpdateModelPayload` | C→S | Client sends model changes |
| `SyncAllModelsPayload` | S→C | Server sends all configs on join |
| `SyncSingleModelPayload` | S→C | Server broadcasts single update |

**Flow:**

1. Client sends `UpdateModelPayload` when player changes model
2. Server stores in `PlayerModelDataManager`
3. Server broadcasts `SyncSingleModelPayload` to all other players

---

## Client Module

Located in `src/client/kotlin/xyz/cottageindustries/cottfur/client/`

### CottfurClient.kt

Client entrypoint implementing `ClientModInitializer`.

**Initialization Order:**

1. `ModelRegistry.initialize()` - Register all model types
2. `CottfurClientNetworking.registerClientPackets()` - Set up packet handlers
3. `CottfurKeybinds.register()` - Register keybindings

### CottfurKeybinds.kt

Keybind registration and handling.

**Registered Keys:**

| Key | Default | Action |
| ----- | --------- | -------- |
| `open_customization` | G | Opens `AnthroCustomizationScreen` |

Uses `ClientTickEvents.END_CLIENT_TICK` to poll key state.

### CottfurDataGenerator.kt

Fabric data generation entrypoint. Currently empty - reserved for future language file generation.

---

## Model System

Located in `src/client/kotlin/xyz/cottageindustries/cottfur/client/model/`

### AnthroModelType.kt

Enum defining available species:

| Type | ID | Display Name |
| ------ | ---- | -------------- |
| `NONE` | "none" | None (Default Player) |
| `PROTOGEN` | "protogen" | Protogen |
| `K9` | "k9" | Canine (K9) |
| `FELINE` | "feline" | Feline |
| `ANTHRO_BASE` | "anthro_base" | Basic Anthro |

**Helper Methods:**

- `getModelLocation()` - Returns path to `.geo.json` file
- `getDefaultTextureLocation()` - Returns path to texture PNG
- `getAnimationLocation()` - Returns path to `.animation.json`
- `isAnthroModel()` - Returns true if not NONE
- `fromId(id)` - Static lookup by string ID

### AnthroModel.kt

Abstract base class for species model definitions.

**Bone Constants:**

```kotlin
BONE_HEAD, BONE_BODY, BONE_LEFT_ARM, BONE_RIGHT_ARM,
BONE_LEFT_LEG, BONE_RIGHT_LEG, BONE_TAIL, BONE_LEFT_EAR, BONE_RIGHT_EAR
```

**AnthroAnimatable Interface:**
Extends `GeoAnimatable` with:

- `getCustomTexture()` - Optional custom texture override
- `getModelType()` - The species type

### AnthroGeoModel.kt

GeckoLib `GeoModel` implementation providing resource locations.

**GeckoLib 5.4 Note:** Uses `GeoRenderState` parameter for resource lookups instead of entity references (API change from 4.x).

### AnthroRenderState.kt

Data carrier between animatable and renderer.

**Fields:**

- Model type and custom texture
- Head pitch/yaw for look-at animation
- Body yaw rotation
- Limb swing values for walking
- State flags: `isSprinting`, `isSneaking`, `isSwimming`

### ModelRegistry.kt

Singleton managing model instances.

**AnthroPlayerAnimatable:**
Implementation of `AnthroAnimatable` for player entities:

- Wraps model type and custom texture
- Creates animation instance cache via `GeckoLibUtil.createInstanceCache()`
- Registers a main animation controller with idle animation

### models/ Subdirectory

Species-specific model implementations:

| File | Species | Extra Bones |
| ------ | --------- | ------------- |
| `ProtogenModel.kt` | Protogen | `visor`, `left_antenna`, `right_antenna` |
| `K9Model.kt` | Canine | `muzzle`, `jaw` |
| `FelineModel.kt` | Feline | `whiskers` |
| `AnthroBaseModel.kt` | Basic | None |

---

## Rendering Pipeline

Located in `src/client/kotlin/xyz/cottageindustries/cottfur/client/render/`

### AnthroGeoRenderer.kt

Main GeckoLib renderer (currently a stub).

**Current Behavior:**

- Checks if player has anthro model configured
- Logs once that rendering was triggered
- Returns `false` to fall back to vanilla rendering

**TODO:** Implement actual GeckoLib 5.4 rendering.

### AnthroPlayerRenderer.kt

Player-specific rendering wrapper.

**Responsibilities:**

- Sets up `AnthroPlayerAnimatable` with player data
- Populates `AnthroRenderState` from player entity
- Manages renderer cache per model type

**Static Methods:**

- `shouldRenderAnthro(player)` - Check if player uses anthro model
- `getPlayerModelType(player)` - Get player's current model type
- `getRenderer(modelType)` - Get/create cached renderer

### AnthroArmRenderer.kt

First-person arm rendering handler.

**Arm Textures:** Maps model types to arm texture paths.

**Methods:**

- `shouldRenderAnthroArms()` - Check if local player has anthro model
- `getLocalPlayerModelType()` - Get local player's model type
- `renderFirstPersonArm()` - Render custom arm (currently stub)

### FirstPersonArmRenderer.kt

Utilities for first-person arm transformations.

**applyFirstPersonArmTransform():**
Applies vanilla-style positioning with swing animation math.

**applyPawTransform():**
Species-specific adjustments:

- Protogen: Angular/robotic positioning
- K9: Wider spread for paws
- Feline: Graceful inward positioning

**getFirstPersonBones():**
Returns bone names needed for arm rendering per species.

---

## Animation System

Located in `src/client/kotlin/xyz/cottageindustries/cottfur/client/animation/`

### AnthroAnimationController.kt

Animation management and emote definitions.

**Animation Constants:**

```kotlin
ANIM_IDLE = "animation.anthro.idle"
ANIM_WALK = "animation.anthro.walk"
ANIM_RUN = "animation.anthro.run"
ANIM_TAIL_WAG = "animation.anthro.tail_wag"
ANIM_EAR_FLICK = "animation.anthro.ear_flick"
```

**Controllers:**

- `createMainController()` - Movement-based (idle/walk) switching
- `createSecondaryController()` - Overlay animations (tail, ears)

**AnthroEmote Enum:**

| Emote | Animation | Duration |
| ------- | ----------- | ---------- |
| WAVE | wave | 1.5s |
| SIT | sit | Until cancelled |
| DANCE | dance | 3.0s |
| NOD | nod | 0.5s |
| SHAKE | shake | 0.5s |
| BOW | bow | 1.0s |

**EmoteRegistry:**
Singleton for emote lookup by name.

---

## Customization System

Located in `src/client/kotlin/xyz/cottageindustries/cottfur/client/customization/`

### TextureManager.kt

Custom texture import and storage.

**Storage Location:** `config/cottfur/textures/`

**Constraints:**

- Max file size: 1MB
- Format: PNG only
- Expected dimensions: 64×64

**TextureResult Sealed Class:**

- `Success(textureId, identifier)` - Import succeeded
- `Error(message)` - Import failed with reason

### PatternGenerator.kt

Pattern definitions and color utilities.

**PatternType Enum:**

```none
NONE, STRIPES, SPOTS, GRADIENT, TWO_TONE,
TIGER, TABBY, HUSKY, CALICO
```

**PatternConfig Data Class:**

- Pattern type selection
- Three colors (primary, secondary, accent)
- Intensity (0.0-1.0)
- Scale multiplier

**Utility Methods:**

- `blendColors()` - Linear interpolation between colors
- `parseColor()` - Hex string to int
- `colorToHex()` - Int to hex string

### BlockbenchImporter.kt

Imports Blockbench `.bbmodel` files and converts to GeckoLib format.

**Storage Location:** `config/cottfur/models/`

**Required Bones:**
Models must have: `head`, `body`, `left_arm`, `right_arm`, `left_leg`, `right_leg`

**Process:**

1. Validate file exists and is `.bbmodel`
2. Parse JSON structure
3. Validate bone hierarchy
4. Convert to GeckoLib geo.json format
5. Save both original and converted files

**ImportResult Sealed Class:**

- `Success(modelId, modelName, boneCount)`
- `Error(message)`

---

## UI System

Located in `src/client/kotlin/xyz/cottageindustries/cottfur/client/ui/`

### AnthroCustomizationScreen.kt

Main customization GUI extending `Screen`.

**Tabs:**

| Tab | Content |
| ----- | --------- |
| MODEL_SELECT | Species selection buttons |
| COLORS | Color pickers (primary, secondary, accent) |
| PATTERNS | Pattern selection grid |
| IMPORT | Texture/model import buttons |

**Flow:**

1. Load current config from `PlayerModelDataManager`
2. User makes selections
3. "Apply" button saves to local manager and sends network packet
4. Screen closes

---

## Networking

### Client-Side (CottfurClientNetworking.kt)

**Packet Handlers:**

- `SYNC_ALL_MODELS` - Clears local cache, loads all received configs
- `SYNC_SINGLE_MODEL` - Updates single player's config

**Sending:**

- `sendModelUpdate(config)` - Sends player's config to server
- `isServerSupported()` - Checks if server has CottFur

### Server-Side (CottfurNetworking.kt)

**Packet Handler:**

- `UPDATE_MODEL` - Stores config, broadcasts to other players

---

## Mixins

Located in `src/client/java/xyz/cottageindustries/cottfur/mixin/client/`

### PlayerEntityRendererMixin.java

Hooks into `PlayerEntityRenderer.updateRenderState()`.

**Current Behavior:**

- Checks if player has anthro model
- If yes, overrides `skinTextures` with test texture

**Target:** Player model replacement (WIP).

### HeldItemRendererMixin.java

Placeholder for first-person arm rendering.

**Target:** `HeldItemRenderer` for custom paw rendering.

**Status:** Not yet implemented - method signatures for 1.21.11 need identification.

---

## Technical Notes

### GeckoLib 5.4 API Changes

From GeckoLib 4.x to 5.x:

| Change | Old API | New API |
| -------- | --------- | --------- |
| GeoModel resources | Entity parameter | GeoRenderState parameter |
| AnimationController | `(animatable, name, ticks, handler)` | `(name, ticks, handler)` |
| Handler lambda | Has animatable param | No animatable param |

### Minecraft 1.21.11 Rendering

**Two-Phase Rendering:**

1. `updateRenderState()` - Prepare state from entity
2. `render()` - Render using prepared state

**Key Classes:**

- `SkinTextures` - Player texture management
- `PlayerEntityRenderState` - Render state for players
- Render method on `LivingEntityRenderer`, not `PlayerEntityRenderer`

### Standard Bone Hierarchy

```bash
root
├── body
│   ├── head
│   │   ├── left_ear
│   │   └── right_ear
│   ├── left_arm
│   │   └── left_arm_lower (+ hand/paw/fingers)
│   ├── right_arm
│   │   └── right_arm_lower (+ hand/paw/fingers)
│   ├── left_leg
│   │   └── left_leg_lower
│   ├── right_leg
│   │   └── right_leg_lower
│   └── tail
```

### Thread Safety

`PlayerModelDataManager` uses `ConcurrentHashMap` because:

- Server networking runs on Netty threads
- Client rendering runs on render thread
- UI runs on client main thread

All access must be thread-safe.

---

## File Locations

### Assets

```bash
src/main/resources/assets/cottfur/
├── geo/                    # GeckoLib model geometry
│   ├── protogen.geo.json
│   ├── k9.geo.json
│   ├── feline.geo.json
│   └── anthro_base.geo.json
├── animations/             # Animation definitions
│   ├── protogen.animation.json
│   ├── k9.animation.json
│   ├── feline.animation.json
│   └── anthro_base.animation.json
├── textures/entity/        # Model textures
│   ├── protogen.png
│   ├── k9.png
│   ├── feline.png
│   ├── anthro_base.png
│   └── test.png
└── lang/
    └── en_us.json          # English translations
```

### Runtime Directories

```bash
config/cottfur/
├── textures/    # User-imported custom textures
└── models/      # User-imported Blockbench models
```
