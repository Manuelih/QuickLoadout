package me.manudev.quickloadout;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import java.util.HashSet;
import java.util.Set;

/**
 * Sits in front of the vanilla GLFW key callback. Combos used by the mod are swallowed, so F1/F3 or the
 * number keys never reach vanilla while they save or recall a loadout. A non-modifier key used as the
 * combo modifier (L by default) is held back until release and only replayed when no combo was used,
 * so a plain tap of L still opens the advancements screen.
 */
public final class LoadoutInput {
	private static GLFWKeyCallbackI hook;
	private static GLFWKeyCallback previous;
	private static final Set<Integer> downKeys = new HashSet<>();
	private static final Set<Integer> downScancodes = new HashSet<>();
	private static final Set<Integer> swallowed = new HashSet<>();
	private static final boolean[] mouseWasDown = new boolean[LoadoutKeys.LOADOUTS * 2];
	private static boolean held;
	private static int heldKey;
	private static int heldMods;
	private static boolean comboUsed;
	private static boolean focused = true;

	private LoadoutInput() {
	}

	static void install(MinecraftClient client) {
		hook = (window, key, scancode, action, mods) -> onKey(client, window, key, scancode, action, mods);
		previous = GLFW.glfwSetKeyCallback(client.getWindow().getHandle(), hook);
	}

	private static void forward(long window, int key, int scancode, int action, int mods) {
		if (previous != null) {
			previous.invoke(window, key, scancode, action, mods);
		}
	}

	private static void onKey(MinecraftClient client, long window, int key, int scancode, int action, int mods) {
		if (action == GLFW.GLFW_PRESS) {
			downKeys.add(key);
			downScancodes.add(scancode);
		} else if (action == GLFW.GLFW_RELEASE) {
			downKeys.remove(key);
			downScancodes.remove(scancode);
		}

		if (action != GLFW.GLFW_PRESS) {
			if (swallowed.contains(key)) {
				if (action == GLFW.GLFW_RELEASE) {
					swallowed.remove(key);
				}
				return;
			}
			if (held && key == heldKey) {
				if (action == GLFW.GLFW_RELEASE) {
					held = false;
					if (!comboUsed && client.currentScreen == null) {
						forward(window, key, scancode, GLFW.GLFW_PRESS, heldMods);
					}
					forward(window, key, scancode, action, mods);
				}
				return;
			}
			forward(window, key, scancode, action, mods);
			return;
		}

		if (canAct(client)) {
			Runnable combo = combo(key, scancode);
			if (combo != null) {
				swallowed.add(key);
				comboUsed = true;
				client.execute(combo);
				return;
			}
			if (!held && isHoldableModifier(key, scancode)) {
				held = true;
				heldKey = key;
				heldMods = mods;
				comboUsed = false;
				return;
			}
		}
		forward(window, key, scancode, action, mods);
	}

	private static Runnable combo(int key, int scancode) {
		for (int i = 0; i < LoadoutKeys.LOADOUTS; i++) {
			int index = i;
			if (matches(LoadoutKeys.SAVE[i], key, scancode) && isDown(LoadoutKeys.saveModifier)) {
				return () -> LoadoutManager.save(index);
			}
			if (matches(LoadoutKeys.RECALL[i], key, scancode) && isDown(LoadoutKeys.recallModifier)) {
				return () -> LoadoutManager.recall(index);
			}
		}
		return null;
	}

	static void tick(MinecraftClient client) {
		boolean nowFocused = client.isWindowFocused();
		if (focused && !nowFocused) {
			downKeys.clear();
			downScancodes.clear();
			swallowed.clear();
			held = false;
		}
		focused = nowFocused;

		long handle = client.getWindow().getHandle();
		for (int i = 0; i < LoadoutKeys.LOADOUTS * 2; i++) {
			boolean save = i < LoadoutKeys.LOADOUTS;
			int index = i % LoadoutKeys.LOADOUTS;
			KeyBinding binding = save ? LoadoutKeys.SAVE[index] : LoadoutKeys.RECALL[index];
			InputUtil.Key bound = LoadoutKeys.boundKey(binding);
			boolean down = bound.getCategory() == InputUtil.Type.MOUSE && GLFW.glfwGetMouseButton(handle, bound.getCode()) == GLFW.GLFW_PRESS;
			if (down && !mouseWasDown[i] && canAct(client) && isDown(save ? LoadoutKeys.saveModifier : LoadoutKeys.recallModifier)) {
				if (save) {
					LoadoutManager.save(index);
				} else {
					LoadoutManager.recall(index);
				}
			}
			mouseWasDown[i] = down;
		}
	}

	private static boolean canAct(MinecraftClient client) {
		return client.currentScreen == null && client.getOverlay() == null && client.player != null
			&& client.interactionManager != null && !client.player.isSpectator();
	}

	private static boolean matches(KeyBinding binding, int key, int scancode) {
		InputUtil.Key bound = LoadoutKeys.boundKey(binding);
		if (bound.getCategory() == InputUtil.Type.KEYSYM) {
			return key != GLFW.GLFW_KEY_UNKNOWN && bound.getCode() == key;
		}
		return bound.getCategory() == InputUtil.Type.SCANCODE && bound.getCode() == scancode;
	}

	private static boolean isDown(KeyBinding modifier) {
		InputUtil.Key bound = LoadoutKeys.boundKey(modifier);
		if (bound.equals(InputUtil.UNKNOWN_KEY)) {
			return true;
		}
		long handle = MinecraftClient.getInstance().getWindow().getHandle();
		return switch (bound.getCategory()) {
			case KEYSYM -> downKeys.contains(bound.getCode()) || GLFW.glfwGetKey(handle, bound.getCode()) == GLFW.GLFW_PRESS;
			case SCANCODE -> downScancodes.contains(bound.getCode());
			case MOUSE -> GLFW.glfwGetMouseButton(handle, bound.getCode()) == GLFW.GLFW_PRESS;
		};
	}

	// Shift, Ctrl, Alt and Super keep working normally when used as the combo modifier.
	private static boolean isHoldableModifier(int key, int scancode) {
		if (key >= GLFW.GLFW_KEY_LEFT_SHIFT && key <= GLFW.GLFW_KEY_RIGHT_SUPER) {
			return false;
		}
		return (!LoadoutKeys.isUnbound(LoadoutKeys.saveModifier) && matches(LoadoutKeys.saveModifier, key, scancode))
			|| (!LoadoutKeys.isUnbound(LoadoutKeys.recallModifier) && matches(LoadoutKeys.recallModifier, key, scancode));
	}
}
