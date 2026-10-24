# 🎒 QuickLoadout

**Save up to 4 hotbar layouts and get them back with a single key.**

Building, fighting, farming, exploring: each needs a different hotbar. Set it up once, save it, and from then on QuickLoadout pulls the right items out of your inventory and puts every one of them back in its slot. If something is missing, it tells you exactly what.

## ✨ Features

- 💾 **4 loadouts:** save the current hotbar with **L + 1 / 2 / 3 / 4**
- ⚡ **One key recall:** **L + F1 / F2 / F3 / F4** rebuilds the saved hotbar from your inventory (main inventory, other hotbar slots and offhand)
- 📦 **Fills the stacks:** saved 64 cobblestone but you have 30 + 34 in two stacks? They are merged back into one full stack
- 📝 **Clear missing list:** anything you don't have is shown above the hotbar with its real name and amount, e.g. *» ʟᴏᴀᴅᴏᴜᴛ 1 ʀᴇᴄᴀʟʟᴇᴅ, ᴍɪꜱꜱɪɴɢ: 2× ᴅɪᴀᴍᴀɴᴛᴇ, 3× ɢᴏʟᴅᴇɴ ᴀᴘᴘʟᴇ*. Renamed items show their custom name
- 🎨 **Clean messages:** small caps text. Saving writes one line in chat, switching loadouts never spams the chat: it only uses the line above the hotbar
- 🔍 **Smart matching:** a worn sword is still your sword, but a sword with a different name or enchantments is not
- 🌐 **Works on any server:** it only uses normal inventory clicks, the same a player makes by hand. Nothing to install on the server
- ⌨️ **Every key can be changed** in *Options → Controls → Key Binds → Hotbar Settings*
- 🇬🇧 English and 🇮🇹 Italian: messages follow your game language

## 🎮 How to use

| Action | Default keys |
|---|---|
| Save the hotbar to loadout 1-4 | hold **L**, press **1 / 2 / 3 / 4** |
| Recall loadout 1-4 | hold **L**, press **F1 / F2 / F3 / F4** |

1. Arrange your hotbar the way you like it
2. Hold **L** and press **1**: *Hotbar saved to loadout 1*
3. Play, mess up your inventory, put things away
4. Hold **L** and press **F1**: everything goes back to its place

## ⌨️ About the keys

Vanilla already uses some of these keys, so QuickLoadout takes care not to break them:

- **F1 (hide HUD) and F3 (debug screen) keep working.** The F keys only recall a loadout while you hold the modifier (L). When they do, the press never reaches vanilla, so the HUD doesn't disappear and the debug screen doesn't open
- **Tapping L still opens Advancements.** L is only used as a modifier: if you release it without pressing a loadout key, the game gets the L press as usual
- **1-4 still select the hotbar slots**, unless L is held
- If you prefer **F1-F4 alone**, set *Recall: Modifier key (hold)* to *Not bound* (press Esc while changing it). F1-F4 will then always recall loadouts and no longer hide the HUD or open the debug screen
- Shift, Ctrl and Alt work fine as modifiers too and are never held back

## 🔍 How items are matched

An item counts as the saved one when it is the **same item with the same components**: custom name, enchantments, potion effects, trims, custom data and so on. Things that change while you use an item are ignored: **durability, anvil repair cost, shulker box / bundle contents and a loaded crossbow's arrow**.

So a damaged *Diamond Sword* still matches the sword you saved, while a diamond renamed *"diamante"* is a different item from a plain *Diamond*. When there are several candidates, an identical stack is preferred, then the main inventory over other hotbar slots, then the biggest stack.

## 📌 Good to know

- Loadouts are saved in `config/quickloadout.json` and shared by all worlds and servers
- Empty slots in a loadout are left as they are, so a recall never throws your items away
- Recall only works with no screen open (not while typing in chat or with the inventory open)
- Client-side only. Works in singleplayer and on any server (vanilla, Paper, Spigot...)

## 📋 Requirements

- Minecraft **1.21.8 - 1.21.11** (one jar for all)
- Fabric Loader and **Fabric API**
