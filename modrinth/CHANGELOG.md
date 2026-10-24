## QuickLoadout 1.0.1

- Fixed items (for example server guns) going back to their old slot right after a recall on servers whose plugins block hotbar-key swaps: items are now moved with normal pick-up-and-drop clicks
- If the server still undoes a move, QuickLoadout notices it half a second later and tries again once with the other kind of click
- Anything left on the cursor by a blocked click is put back into the inventory

## QuickLoadout 1.0.0

First release, for Minecraft 1.21.8 - 1.21.11 (one jar).

- 4 hotbar loadouts: save with L + 1-4, recall with L + F1-F4
- Recall pulls items from the main inventory, other hotbar slots and the offhand, whatever the amount
- Slots saved empty are cleared into the inventory
- Items missing entirely are listed above the hotbar with their display name
- Small caps messages: chat only when saving, action bar when recalling
- Items match by type and components, ignoring durability and similar, with a fallback on the display name for server items (guns, custom tools...)
- Works on any server: only normal inventory clicks, nothing needed on the server
- F1, F3, the number keys and the Advancements key keep their vanilla behaviour
- QuickLoadout Settings button in the Options screen; keys also in Controls → Key Binds → Hotbar Settings
- English and Italian translations
