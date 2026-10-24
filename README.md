# QuickLoadout

Client-side Fabric mod for Minecraft 1.21.8 - 1.21.11: save up to 4 hotbar layouts and recall them with one key.

- **L + 1/2/3/4** saves the hotbar to loadout 1-4
- **L + F1/F2/F3/F4** recalls it: items are moved from the inventory with normal inventory clicks, so it works on any server
- Missing items are listed above the hotbar with their display name and amount; the chat is only used when saving
- All keys are in *Options → Controls → Key Binds → Hotbar Settings*

## Building

Requires JDK 21 or newer.

```
gradlew build
```

The jar is written to `build/libs/`. `gradlew runClient` starts a development client.

## How it works

- `LoadoutInput` wraps the GLFW key callback: key combos used by the mod are swallowed before vanilla sees them (so F1/F3 do nothing else while recalling), and the modifier key is replayed on release when no combo was used (so a tap of L still opens Advancements).
- `LoadoutManager` rebuilds the hotbar with `SWAP` and `PICKUP` clicks on the player inventory screen handler.
- `ItemMatcher` compares item type and components, ignoring durability, repair cost, container contents and charged projectiles.
- `LoadoutKeys` creates the key bindings by reflection, because the category argument is a `String` up to 1.21.8 and a `KeyBinding.Category` from 1.21.9.

## License

MIT
