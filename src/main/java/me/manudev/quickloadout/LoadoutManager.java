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
			placed[h] = ItemMatcher.matches(inventory.getStack(h), wanted[h]);
		}

		for (int h = 0; h < HOTBAR; h++) {
			if (wanted[h].isEmpty() || placed[h]) {
				continue;
			}
			int source = findSource(inventory, wanted[h], placed, h);
			if (source < 0) {
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

		for (int h = 0; h < HOTBAR; h++) {
			if (placed[h]) {
				topUp(client, handler, inventory, h, wanted[h].getCount());
			}
		}

		List<ItemStack> missing = new ArrayList<>();
		List<Integer> counts = new ArrayList<>();
		for (int h = 0; h < HOTBAR; h++) {
			if (wanted[h].isEmpty()) {
				continue;
			}
			ItemStack current = inventory.getStack(h);
			int have = ItemMatcher.matches(current, wanted[h]) ? current.getCount() : 0;
			if (have < wanted[h].getCount()) {
				addMissing(missing, counts, wanted[h], wanted[h].getCount() - have);
			}
		}

		if (missing.isEmpty()) {
			Messages.actionBar(client, Text.translatable("quickloadout.recalled", loadout + 1));
			return;
		}
		MutableText list = Text.empty();
		for (int i = 0; i < missing.size(); i++) {
			if (i > 0) {
				list.append(", ");
			}
			list.append(counts.get(i) + "× ").append(missing.get(i).getName());
		}
		Messages.actionBar(client, Text.translatable("quickloadout.recalled_partial", loadout + 1, list));
	}

	// Prefers an identical stack, then the main inventory over other hotbar slots, then the biggest stack.
	private static int findSource(PlayerInventory inventory, ItemStack wanted, boolean[] placed, int target) {
		int best = -1;
		int bestScore = -1;
		for (int i = 0; i <= OFFHAND; i++) {
			if (i == target || i < HOTBAR && placed[i] || i >= MAIN_END && i != OFFHAND) {
				continue;
			}
			ItemStack stack = inventory.getStack(i);
			if (ItemMatcher.matches(stack, wanted) && scoreOf(stack, wanted, i) > bestScore) {
				best = i;
				bestScore = scoreOf(stack, wanted, i);
			}
		}
		return best;
	}

	private static int scoreOf(ItemStack stack, ItemStack wanted, int index) {
		int score = Math.min(stack.getCount(), 99);
		if (index >= HOTBAR && index < MAIN_END) {
			score += 100;
		}
		if (ItemMatcher.exact(stack, wanted)) {
			score += 1000;
		}
		return score;
	}

	// Merges identical stacks from the main inventory until the saved amount is reached.
	private static void topUp(MinecraftClient client, PlayerScreenHandler handler, PlayerInventory inventory, int h, int amount) {
		for (int guard = 0; guard < MAIN_END; guard++) {
			ItemStack current = inventory.getStack(h);
			if (current.getCount() >= amount || current.getCount() >= current.getMaxCount()) {
				return;
			}
			int source = -1;
			for (int i = HOTBAR; i < MAIN_END; i++) {
				if (ItemMatcher.exact(inventory.getStack(i), current)) {
					source = i;
					break;
				}
			}
			if (source < 0) {
				return;
			}
			click(client, handler, source, 0, SlotActionType.PICKUP);
			click(client, handler, HOTBAR_SLOT + h, 0, SlotActionType.PICKUP);
			if (!handler.getCursorStack().isEmpty()) {
				click(client, handler, source, 0, SlotActionType.PICKUP);
			}
		}
	}

	private static void addMissing(List<ItemStack> missing, List<Integer> counts, ItemStack wanted, int count) {
		for (int i = 0; i < missing.size(); i++) {
			if (ItemMatcher.matches(missing.get(i), wanted)) {
				counts.set(i, counts.get(i) + count);
				return;
			}
		}
		missing.add(wanted);
		counts.add(count);
	}

	private static void click(MinecraftClient client, PlayerScreenHandler handler, int slot, int button, SlotActionType action) {
		client.interactionManager.clickSlot(handler.syncId, slot, button, action, client.player);
	}
}
