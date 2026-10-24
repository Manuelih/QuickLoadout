package me.manudev.quickloadout;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Rebuilds a hotbar with the same clicks a player would make in the inventory screen (swap with a
 * hotbar key, pick up and drop on a stack), so it works on any server without server-side code.
 */
public final class LoadoutManager {
	private static final int HOTBAR = LoadoutStore.SLOTS;
	private static final int MAIN_END = 36;
	private static final int OFFHAND = 40;
	// PlayerScreenHandler slot ids: main inventory 9-35 keeps its index, hotbar 0-8 is 36-44
	private static final int HOTBAR_SLOT = 36;
	private static final int OFFHAND_SWAP_BUTTON = 40;

	private LoadoutManager() {
	}

	public static void save(int loadout) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if (player == null || client.world == null) {
			return;
		}
		PlayerInventory inventory = player.getInventory();
		ItemStack[] hotbar = new ItemStack[HOTBAR];
		int items = 0;
		for (int i = 0; i < HOTBAR; i++) {
			hotbar[i] = inventory.getStack(i).copy();
			if (!hotbar[i].isEmpty()) {
				items++;
			}
		}
		LoadoutStore.save(loadout, hotbar, client.world.getRegistryManager());
		Messages.chat(client, Text.translatable("quickloadout.saved", loadout + 1, items));
	}

	public static void recall(int loadout) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if (player == null || client.world == null || client.interactionManager == null) {
			return;
		}
		ItemStack[] wanted = LoadoutStore.get(loadout, client.world.getRegistryManager());
		if (wanted == null) {
			Messages.actionBar(client, Text.translatable("quickloadout.not_saved", loadout + 1));
			return;
		}
		PlayerScreenHandler handler = player.playerScreenHandler;
		if (!handler.getCursorStack().isEmpty()) {
			return;
		}
		PlayerInventory inventory = player.getInventory();

		boolean[] placed = new boolean[HOTBAR];
		for (int h = 0; h < HOTBAR; h++) {
			placed[h] = ItemMatcher.level(inventory.getStack(h), wanted[h]) >= ItemMatcher.STRICT;
		}

		for (int h = 0; h < HOTBAR; h++) {
			if (wanted[h].isEmpty() || placed[h]) {
				continue;
			}
			int source = findSource(inventory, wanted[h], placed, h);
			int current = ItemMatcher.level(inventory.getStack(h), wanted[h]);
			if (source < 0 || ItemMatcher.level(inventory.getStack(source), wanted[h]) <= current) {
				placed[h] = current != ItemMatcher.NONE;
				continue;
			}
			if (source < HOTBAR) {
				click(client, handler, HOTBAR_SLOT + source, h, SlotActionType.SWAP);
			} else if (source == OFFHAND) {
				click(client, handler, HOTBAR_SLOT + h, OFFHAND_SWAP_BUTTON, SlotActionType.SWAP);
			} else {
				click(client, handler, source, h, SlotActionType.SWAP);
			}
			placed[h] = true;
		}

		// Slots that were empty when the loadout was saved are emptied into the main inventory.
		boolean full = false;
		for (int h = 0; h < HOTBAR; h++) {
			if (!wanted[h].isEmpty() || inventory.getStack(h).isEmpty()) {
				continue;
			}
			int free = -1;
			for (int i = HOTBAR; i < MAIN_END && free < 0; i++) {
				if (inventory.getStack(i).isEmpty()) {
					free = i;
				}
			}
			if (free < 0) {
				full = true;
				continue;
			}
			click(client, handler, free, h, SlotActionType.SWAP);
		}

		// Only items that are not in the inventory at all count as missing: a smaller stack is fine.
		List<ItemStack> missing = new ArrayList<>();
		for (int h = 0; h < HOTBAR; h++) {
			if (!wanted[h].isEmpty() && !ItemMatcher.matches(inventory.getStack(h), wanted[h])) {
				addMissing(missing, wanted[h]);
			}
		}

		if (missing.isEmpty()) {
			Messages.actionBar(client, Text.translatable(full ? "quickloadout.recalled_full" : "quickloadout.recalled", loadout + 1));
			return;
		}
		MutableText list = Text.empty();
		for (int i = 0; i < missing.size(); i++) {
			if (i > 0) {
				list.append(", ");
			}
			list.append(missing.get(i).getName());
		}
		Messages.actionBar(client, Text.translatable("quickloadout.recalled_partial", loadout + 1, list));
	}

	// Prefers the closest match, then the main inventory over other hotbar slots, then the biggest stack.
	private static int findSource(PlayerInventory inventory, ItemStack wanted, boolean[] placed, int target) {
		int best = -1;
		int bestScore = -1;
		for (int i = 0; i <= OFFHAND; i++) {
			if (i == target || i < HOTBAR && placed[i] || i >= MAIN_END && i != OFFHAND) {
				continue;
			}
			ItemStack stack = inventory.getStack(i);
			int level = ItemMatcher.level(stack, wanted);
			if (level == ItemMatcher.NONE) {
				continue;
			}
			int score = level * 1000 + (i >= HOTBAR && i < MAIN_END ? 100 : 0) + Math.min(stack.getCount(), 99);
			if (score > bestScore) {
				best = i;
				bestScore = score;
			}
		}
		return best;
	}

	private static void addMissing(List<ItemStack> missing, ItemStack wanted) {
		for (ItemStack entry : missing) {
			if (ItemMatcher.matches(entry, wanted)) {
				return;
			}
		}
		missing.add(wanted);
	}

	private static void click(MinecraftClient client, PlayerScreenHandler handler, int slot, int button, SlotActionType action) {
		client.interactionManager.clickSlot(handler.syncId, slot, button, action, client.player);
	}
}
