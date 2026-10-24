package me.manudev.quickloadout;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * Two stacks are "the same loadout item" when they share the item type and every component (custom
 * name, enchantments, potion, trims, custom data...), except the ones that change while the item is
 * used: durability, anvil repair cost, the contents of shulker boxes and bundles, and a crossbow's
 * loaded projectile. A worn sword still counts as the saved sword, a sword renamed differently does not.
 */
public final class ItemMatcher {
	private static final List<ComponentType<?>> IGNORED = List.of(
		DataComponentTypes.DAMAGE,
		DataComponentTypes.REPAIR_COST,
		DataComponentTypes.CONTAINER,
		DataComponentTypes.BUNDLE_CONTENTS,
		DataComponentTypes.CHARGED_PROJECTILES
	);

	private ItemMatcher() {
	}

	public static boolean matches(ItemStack stack, ItemStack wanted) {
		if (stack.isEmpty() || wanted.isEmpty() || stack.getItem() != wanted.getItem()) {
			return false;
		}
		if (ItemStack.areItemsAndComponentsEqual(stack, wanted)) {
			return true;
		}
		return ItemStack.areItemsAndComponentsEqual(stripped(stack), stripped(wanted));
	}

	public static boolean exact(ItemStack stack, ItemStack wanted) {
		return !stack.isEmpty() && ItemStack.areItemsAndComponentsEqual(stack, wanted);
	}

	private static ItemStack stripped(ItemStack stack) {
		ItemStack copy = stack.copy();
		for (ComponentType<?> type : IGNORED) {
			copy.remove(type);
		}
		return copy;
	}
}
