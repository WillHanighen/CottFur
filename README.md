# 🐾 CottFur

A Minecraft Fabric mod that allows players to replace their default player model with anthro/furry character models.

![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21.11-green)
![Fabric Loader](https://img.shields.io/badge/Fabric%20Loader-0.18.4-blue)
![License](https://img.shields.io/badge/License-CC--BY--SA--4.0-orange)

## Features

- **Multiple Species**: Choose from Protogen, Canine (K9), Feline, or a basic Anthro base model
- **Customization Screen**: Press `G` to open the model customization UI
- **Color Customization**: Primary, secondary, and accent color options
- **Pattern Support**: Stripes, spots, gradients, and more (WIP)
- **Custom Texture Import**: Upload your own textures (WIP)
- **Multiplayer Support**: Other players see your anthro model when the server has the mod installed

## Current Status

### ✅ Implemented

- Mod loads and initializes without crashes
- 4 placeholder GeckoLib models with proper bone structure
- Basic textures for all species (64x64)
- Animation files (idle, walk, run, sneak, attack)
- Customization UI with model selection and color pickers
- Client-server networking for model synchronization
- Player model data persistence
- Keybind registration (G key for customization screen)

### 🚧 Needs Work

- **GeckoLib Rendering**: The actual visual rendering of anthro models needs implementation
- **Blockbench Models**: Placeholder geometry needs to be replaced with detailed models
- **First-Person Arms**: Custom paw rendering in first-person view
- **Texture Generation**: Procedural pattern/color application to textures

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.18.4+
- Fabric API 0.140.2+1.21.11
- Fabric Language Kotlin 1.13.8+kotlin.2.3.0
- GeckoLib 5.4+

## Building

```bash
./gradlew build
```

The built jar will be in `build/libs/`.

## Development

### Project Structure

```bash
src/
├── main/
│   ├── kotlin/xyz/cottageindustries/cottfur/
│   │   ├── Cottfur.kt              # Main mod entrypoint
│   │   ├── CottfurConstants.kt     # Shared constants
│   │   ├── data/                   # Player data management
│   │   └── network/                # Server-side networking
│   └── resources/
│       └── assets/cottfur/
│           ├── geo/                # GeckoLib model files (.geo.json)
│           ├── textures/entity/    # Model textures
│           ├── animations/         # Animation files
│           └── lang/               # Translations
├── client/
│   ├── kotlin/xyz/cottageindustries/cottfur/client/
│   │   ├── CottfurClient.kt        # Client entrypoint
│   │   ├── CottfurKeybinds.kt      # Keybind registration
│   │   ├── model/                  # Model types and registry
│   │   ├── render/                 # GeckoLib renderers
│   │   ├── ui/                     # Customization screens
│   │   ├── animation/              # Animation controllers
│   │   ├── customization/          # Texture/pattern generation
│   │   └── network/                # Client networking
│   └── java/.../mixin/client/      # Client-side mixins
```

### Key Files for Rendering Implementation

If you're looking to implement the actual model rendering:

1. **`AnthroGeoModel.kt`** - GeckoLib model class providing resource locations
2. **`AnthroGeoRenderer.kt`** - Stub renderer that needs full implementation
3. **`PlayerEntityRendererMixin.java`** - Hooks into vanilla player rendering
4. **`ModelRegistry.kt`** - Manages model instances and animatables

### Technical Notes

- Uses GeckoLib 5.4 which has significant API changes from 4.x
- Minecraft 1.21.11 uses `OrderedRenderCommandQueue` instead of `VertexConsumerProvider`
- Player rendering uses a two-phase approach: `updateRenderState` then `render`
- The render method is inherited from `LivingEntityRenderer`, not on `PlayerEntityRenderer` directly

## Contributing

Contributions welcome! Particularly looking for help with:

- GeckoLib 5.x rendering expertise
- Blockbench model creation
- Texture artists for species designs

## License

This project is licensed under [CC-BY-SA-4.0](https://creativecommons.org/licenses/by-sa/4.0/).

## Credits

- Built with [Fabric](https://fabricmc.net/)
- Animations powered by [GeckoLib](https://github.com/bernie-g/geckolib)
