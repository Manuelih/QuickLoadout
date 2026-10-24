package me.manudev.quickloadout;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public final class LoadoutKeys {
	public static final int LOADOUTS = 4;
	private static final String CATEGORY = "key.category.quickloadout.hotbar";

	public static KeyBinding saveModifier;
	public static KeyBinding recallModifier;
	public static final KeyBinding[] SAVE = new KeyBinding[LOADOUTS];
	public static final KeyBinding[] RECALL = new KeyBinding[LOADOUTS];

	private static Object category;

	private LoadoutKeys() {
	}

	static void register() {
		saveModifier = create("save", GLFW.GLFW_KEY_L);
		for (int i = 0; i < LOADOUTS; i++) {
			SAVE[i] = create("save." + (i + 1), GLFW.GLFW_KEY_1 + i);
		}
		recallModifier = create("recall", GLFW.GLFW_KEY_L);
		for (int i = 0; i < LOADOUTS; i++) {
			RECALL[i] = create("recall." + (i + 1), GLFW.GLFW_KEY_F1 + i);
		}
	}

	/** Every QuickLoadout key, in the order shown by the settings screen. */
	public static KeyBinding[] all() {
		KeyBinding[] keys = new KeyBinding[LOADOUTS * 2 + 2];
		keys[0] = saveModifier;
		keys[LOADOUTS + 1] = recallModifier;
		System.arraycopy(SAVE, 0, keys, 1, LOADOUTS);
		System.arraycopy(RECALL, 0, keys, LOADOUTS + 2, LOADOUTS);
		return keys;
	}

	public static boolean isOwn(KeyBinding binding) {
		if (binding == saveModifier || binding == recallModifier) {
			return true;
		}
		for (int i = 0; i < LOADOUTS; i++) {
			if (binding == SAVE[i] || binding == RECALL[i]) {
				return true;
			}
		}
		return false;
	}

	private static KeyBinding modifierOf(KeyBinding binding) {
		for (int i = 0; i < LOADOUTS; i++) {
			if (binding == SAVE[i]) {
				return saveModifier;
			}
			if (binding == RECALL[i]) {
				return recallModifier;
			}
		}
		return null;
	}

	/**
	 * Overlaps that are part of the design are not real conflicts: a loadout key only fires while its
	 * modifier is held, both modifiers may share a key, and a tap of the modifier still reaches the
	 * advancements key it shares by default.
	 */
	public static boolean isIntendedOverlap(KeyBinding a, KeyBinding b) {
		for (KeyBinding own : new KeyBinding[] {a, b}) {
			KeyBinding other = own == a ? b : a;
			if (!isOwn(own)) {
				continue;
			}
			KeyBinding modifier = modifierOf(own);
			if (modifier != null && !isUnbound(modifier) && !boundKey(modifier).equals(boundKey(own))) {
				return true;
			}
			if (modifier == null && (other == saveModifier || other == recallModifier
				|| MinecraftClient.getInstance().options != null && other == MinecraftClient.getInstance().options.advancementsKey)) {
				return true;
			}
		}
		return false;
	}

	public static InputUtil.Key boundKey(KeyBinding binding) {
		return KeyBindingHelper.getBoundKeyOf(binding);
	}

	public static boolean isUnbound(KeyBinding binding) {
		return boundKey(binding).equals(InputUtil.UNKNOWN_KEY);
	}

	// Up to 1.21.8 every key maps to a single binding, so a combo key such as "1" or "L" would
	// steal the press from the vanilla hotbar or advancements binding. The mod reads its keys
	// straight from GLFW, so the vanilla binding gets the key back.
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void restoreVanilla(Map keyToBindings, Map<String, KeyBinding> keysById) {
		Collection<KeyBinding> all = keysById.values();
		for (Object entry : List.copyOf(keyToBindings.entrySet())) {
			Map.Entry e = (Map.Entry) entry;
			if (!(e.getValue() instanceof KeyBinding current) || !isOwn(current)) {
				continue;
			}
			for (KeyBinding other : all) {
				if (!isOwn(other) && boundKey(other).equals(e.getKey())) {
					keyToBindings.put(e.getKey(), other);
					break;
				}
			}
		}
	}

	// 1.21.8 takes the category as a translation key, 1.21.9+ as a KeyBinding.Category record.
	private static KeyBinding create(String name, int code) {
		String id = "key.quickloadout." + name;
		try {
			for (Constructor<?> constructor : KeyBinding.class.getConstructors()) {
				Class<?>[] params = constructor.getParameterTypes();
				if (params.length == 4 && params[0] == String.class && params[1] == InputUtil.Type.class && params[2] == int.class) {
					Object cat = params[3] == String.class ? CATEGORY : category(params[3]);
					return KeyBindingHelper.registerKeyBinding((KeyBinding) constructor.newInstance(id, InputUtil.Type.KEYSYM, code, cat));
				}
			}
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Could not create key binding " + id, e);
		}
		throw new IllegalStateException("No supported KeyBinding constructor for " + id);
	}

	private static Object category(Class<?> type) throws ReflectiveOperationException {
		if (category == null) {
			for (Method method : type.getMethods()) {
				if (Modifier.isStatic(method.getModifiers()) && method.getReturnType() == type
					&& method.getParameterCount() == 1 && method.getParameterTypes()[0] == Identifier.class) {
					category = method.invoke(null, Identifier.of(QuickLoadout.MOD_ID, "hotbar"));
					break;
				}
			}
			if (category == null) {
				throw new NoSuchMethodException("KeyBinding.Category.create(Identifier)");
			}
		}
		return category;
	}
}
