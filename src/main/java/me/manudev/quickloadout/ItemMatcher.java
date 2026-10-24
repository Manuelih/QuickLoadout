package me.manudev.quickloadout;

import net.minecraft.component.ComponentType;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;

import java.util.List;

/**
 * How close a stack is to a saved loadout item, ignoring the amount:
 * <ul>
 *   <li>{@link #EXACT}: same item and same components</li>
 *   <li>{@link #STRICT}: the same, except for what changes while the item is used: durability, anvil
 *   repair cost, shulker box and bundle contents, a crossbow's loaded projectile</li>
 *   <li>{@link #SAME_NAME}: same item and same display name, anything else may differ (lore, enchantments,
 *   ammo or serial numbers written by a server plugin...)</li>
 *   <li>{@link #NAME_ONLY}: a custom-named item (a server's "Beretta 92FS") whose base item changed but whose
 *   display name did not</li>
 * </ul>
 * Lower levels are only used when nothing closer is in the inventory. A diamond renamed "diamante" never
 * matches a plain diamond, at any level.
 */
public final class ItemMatcher {
	public static final int NONE = 0;
	public static final int NAME_ONLY = 1;
	public static final int SAME_NAME = 2;
	public static final int STRICT = 3;
	public static final int EXACT = 4;

	private static final List<ComponentType<?>> VOLATILE = List.of(
		DataComponentTypes.DAMAGE,
		DataComponentTypes.REPAIR_COST,
		DataComponentTypes.CONTAINER,
		DataComponentTypes.BUNDLE_CONTENTS,
		DataComponentTypes.CHARGED_PROJECTILES
	);

	private ItemMatcher() {
	}

	public static int level(ItemStack stack, ItemStack wanted) {
		if (stack.isEmpty() || wanted.isEmpty() || !stack.getName().getString().equals(wanted.getName().getString())) {
			return NONE;
		}
		if (stack.getItem() != wanted.getItem()) {
			return hasCustomName(wanted) ? NAME_ONLY : NONE;
		}
		if (ItemStack.areItemsAndComponentsEqual(stack, wanted)) {
			return EXACT;
		}
		return ItemStack.areItemsAndComponentsEqual(stripped(stack), stripped(wanted)) ? STRICT : SAME_NAME;
	}

	public static boolean matches(ItemStack stack, ItemStack wanted) {
		return level(stack, wanted) != NONE;
	}

	private static boolean hasCustomName(ItemStack stack) {
		return stack.contains(DataComponentTypes.CUSTOM_NAME) || stack.getComponentChanges().get(DataComponentTypes.ITEM_NAME) != null;
	}

	private static ItemStack stripped(ItemStack stack) {
		ItemStack copy = stack.copy();
		for (ComponentType<?> type : VOLATILE) {
			copy.remove(type);
		}
		return copy;
	}
}
