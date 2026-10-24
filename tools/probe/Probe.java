package probe;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import me.manudev.quickloadout.LoadoutKeys;
import me.manudev.quickloadout.LoadoutStore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class Probe implements ClientModInitializer {
	private static final Logger LOG = LoggerFactory.getLogger("PROBE");
	private static int t;
	private static final boolean shots = true;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(Probe::tick);
	}

	private static void cmd(MinecraftServer server, String command) {
		server.execute(() -> server.getCommandManager().CMD(server.getCommandSource(), command));
	}

	private static void key(MinecraftClient c, int key, int action) {
		long handle = c.getWindow().getHandle();
		GLFWKeyCallback cb = GLFW.glfwSetKeyCallback(handle, null);
		GLFW.glfwSetKeyCallback(handle, cb);
		cb.invoke(handle, key, GLFW.glfwGetKeyScancode(key), action, 0);
	}

	private static ItemStack named(ItemStack stack, String name) {
		stack.set(DataComponentTypes.CUSTOM_NAME, Text.literal(name));
		return stack;
	}

	private static void server(MinecraftServer server, java.util.function.Consumer<ServerPlayerEntity> action) {
		server.execute(() -> action.accept(server.getPlayerManager().getPlayerList().get(0)));
	}

	private static String dump(PlayerInventory inv, int from, int to) {
		StringBuilder sb = new StringBuilder();
		for (int i = from; i < to; i++) {
			ItemStack s = inv.getStack(i);
			if (!s.isEmpty()) {
				sb.append(i).append('=').append(s.getCount()).append('x').append(s.getName().getString());
				if (s.getDamage() > 0) {
					sb.append("(dmg").append(s.getDamage()).append(')');
				}
				sb.append(' ');
			}
		}
		return sb.toString().trim();
	}

	private static void tick(MinecraftClient c) {
		if (c.player == null || c.world == null) {
			return;
		}
		t++;
		MinecraftServer server = c.getServer();
		switch (t) {
			case 1 -> {
				cmd(server, "gamemode survival @a");
				cmd(server, "difficulty peaceful");
				cmd(server, "time set 6000");
				cmd(server, "weather clear");
				cmd(server, "gamerule doDaylightCycle false");
				server(server, p -> {
					PlayerInventory inv = p.getInventory();
					inv.clear();
					inv.setStack(0, new ItemStack(Items.DIAMOND_SWORD));
					inv.setStack(1, named(new ItemStack(Items.DIAMOND, 5), "diamante"));
					inv.setStack(2, new ItemStack(Items.COBBLESTONE, 64));
					inv.setStack(4, new ItemStack(Items.GOLDEN_APPLE, 3));
					inv.setStack(5, new ItemStack(Items.BOW));
					inv.setStack(6, new ItemStack(Items.WATER_BUCKET));
					inv.setStack(8, new ItemStack(Items.TORCH, 32));
					inv.setStack(20, new ItemStack(Items.ARROW, 64));
					inv.setSelectedSlot(0);
				});
				c.player.getInventory().setSelectedSlot(0);
			}
			case 40 -> {
				LOG.info("PROBE before save: hotbar [{}] selected={}", dump(c.player.getInventory(), 0, 9), c.player.getInventory().getSelectedSlot());
				capture(c, "gallery-1-hotbar.png");
			}
			case 41 -> c.inGameHud.getChatHud().clear(false);
			case 42 -> key(c, GLFW.GLFW_KEY_L, GLFW.GLFW_PRESS);
			case 44 -> key(c, GLFW.GLFW_KEY_1, GLFW.GLFW_PRESS);
			case 45 -> key(c, GLFW.GLFW_KEY_1, GLFW.GLFW_RELEASE);
			case 47 -> key(c, GLFW.GLFW_KEY_L, GLFW.GLFW_RELEASE);
			case 50 -> LOG.info("PROBE after L+1: selected={} (must be 0) screen={} (must be null)", c.player.getInventory().getSelectedSlot(), c.currentScreen);
			case 52 -> capture(c, "gallery-2-saved.png");
			case 56 -> c.inGameHud.getChatHud().clear(false);
			case 60 -> server(server, p -> {
				PlayerInventory inv = p.getInventory();
				inv.clear();
				ItemStack sword = new ItemStack(Items.DIAMOND_SWORD);
				sword.setDamage(300);
				inv.setStack(27, sword);
				inv.setStack(10, new ItemStack(Items.DIAMOND, 10));
				inv.setStack(33, named(new ItemStack(Items.DIAMOND, 3), "diamante"));
				inv.setStack(12, new ItemStack(Items.COBBLESTONE, 30));
				inv.setStack(30, new ItemStack(Items.COBBLESTONE, 34));
				inv.setStack(15, new ItemStack(Items.COBBLESTONE, 20));
				inv.setStack(0, new ItemStack(Items.DIRT, 16));
				inv.setStack(5, new ItemStack(Items.OAK_PLANKS, 40));
				inv.setStack(3, new ItemStack(Items.BOW));
				inv.setStack(40, new ItemStack(Items.TORCH, 32));
				inv.setStack(22, new ItemStack(Items.WATER_BUCKET));
				inv.setSelectedSlot(0);
			});
			case 70 -> c.setScreen(new InventoryScreen(c.player));
			case 74 -> capture(c, "gallery-3b-scrambled-inventory.png");
			case 77 -> c.setScreen(null);
			case 80 -> {
				LOG.info("PROBE scrambled: hotbar [{}] main [{}] offhand [{}]", dump(c.player.getInventory(), 0, 9), dump(c.player.getInventory(), 9, 36), dump(c.player.getInventory(), 40, 41));
				capture(c, "gallery-3-scrambled.png");
			}
			case 82 -> key(c, GLFW.GLFW_KEY_L, GLFW.GLFW_PRESS);
			case 84 -> key(c, GLFW.GLFW_KEY_F1, GLFW.GLFW_PRESS);
			case 85 -> key(c, GLFW.GLFW_KEY_F1, GLFW.GLFW_RELEASE);
			case 87 -> key(c, GLFW.GLFW_KEY_L, GLFW.GLFW_RELEASE);
			case 95 -> {
				LOG.info("PROBE after L+F1 client: hotbar [{}] main [{}] offhand [{}] hudHidden={} (must be false) screen={}", dump(c.player.getInventory(), 0, 9), dump(c.player.getInventory(), 9, 36), dump(c.player.getInventory(), 40, 41), c.options.hudHidden, c.currentScreen);
				server(server, p -> LOG.info("PROBE after L+F1 server: hotbar [{}] main [{}]", dump(p.getInventory(), 0, 9), dump(p.getInventory(), 9, 36)));
			}
			case 97 -> capture(c, "gallery-4-recalled.png");
			case 100 -> c.setScreen(new InventoryScreen(c.player));
			case 104 -> capture(c, "gallery-5-inventory.png");
			case 106 -> c.setScreen(null);
			case 110 -> key(c, GLFW.GLFW_KEY_3, GLFW.GLFW_PRESS);
			case 111 -> key(c, GLFW.GLFW_KEY_3, GLFW.GLFW_RELEASE);
			case 114 -> LOG.info("PROBE vanilla key 3 alone: selected={} (must be 2)", c.player.getInventory().getSelectedSlot());
			case 116 -> key(c, GLFW.GLFW_KEY_L, GLFW.GLFW_PRESS);
			case 118 -> key(c, GLFW.GLFW_KEY_L, GLFW.GLFW_RELEASE);
			case 122 -> {
				LOG.info("PROBE tap L alone: screen={} (must be advancements)", c.currentScreen == null ? null : c.currentScreen.getClass().getSimpleName());
				c.setScreen(null);
			}
			case 126 -> key(c, GLFW.GLFW_KEY_F1, GLFW.GLFW_PRESS);
			case 127 -> key(c, GLFW.GLFW_KEY_F1, GLFW.GLFW_RELEASE);
			case 130 -> {
				LOG.info("PROBE F1 alone: hudHidden={} (must be true, vanilla)", c.options.hudHidden);
				c.options.hudHidden = false;
			}
			case 132 -> key(c, GLFW.GLFW_KEY_L, GLFW.GLFW_PRESS);
			case 134 -> key(c, GLFW.GLFW_KEY_F2, GLFW.GLFW_PRESS);
			case 135 -> key(c, GLFW.GLFW_KEY_F2, GLFW.GLFW_RELEASE);
			case 137 -> key(c, GLFW.GLFW_KEY_L, GLFW.GLFW_RELEASE);
			case 140 -> {
				c.setScreen(new KeybindsScreen(null, c.options));
			}
			case 145 -> {
				for (var child : c.currentScreen.children()) {
					if (child instanceof ControlsListWidget list) {
						list.setScrollY(list.getMaxScrollY());
					}
				}
			}
			case 150 -> capture(c, "gallery-6-controls.png");
			case 152 -> {
				c.setScreen(null);
				c.options.language = "it_it";
				c.getLanguageManager().setLanguage("it_it");
				c.reloadResources();
			}
			case 260 -> {
				key(c, GLFW.GLFW_KEY_L, GLFW.GLFW_PRESS);
			}
			case 262 -> key(c, GLFW.GLFW_KEY_F1, GLFW.GLFW_PRESS);
			case 263 -> key(c, GLFW.GLFW_KEY_F1, GLFW.GLFW_RELEASE);
			case 265 -> key(c, GLFW.GLFW_KEY_L, GLFW.GLFW_RELEASE);
			case 270 -> capture(c, "gallery-7-italian.png");
			case 272 -> {
				c.setScreen(new KeybindsScreen(null, c.options));
			}
			case 277 -> {
				for (var child : c.currentScreen.children()) {
					if (child instanceof ControlsListWidget list) {
						list.setScrollY(list.getMaxScrollY());
					}
				}
			}
			case 282 -> capture(c, "gallery-8-controls-it.png");
			case 284 -> {
				c.setScreen(null);
				c.options.language = "en_us";
				c.getLanguageManager().setLanguage("en_us");
				c.options.write();
			}
			case 290 -> {
				LoadoutKeys.recallModifier.setBoundKey(InputUtil.UNKNOWN_KEY);
				LoadoutKeys.SAVE[2].setBoundKey(InputUtil.Type.KEYSYM.createFromCode(GLFW.GLFW_KEY_K));
				KeyBinding.updateKeysByCode();
				server(server, p -> {
					PlayerInventory inv = p.getInventory();
					ItemStack hotbar0 = inv.getStack(0);
					inv.setStack(0, inv.getStack(14));
					inv.setStack(14, hotbar0);
				});
			}
			case 300 -> key(c, GLFW.GLFW_KEY_F1, GLFW.GLFW_PRESS);
			case 301 -> key(c, GLFW.GLFW_KEY_F1, GLFW.GLFW_RELEASE);
			case 305 -> LOG.info("PROBE rebound: F1 alone recall: hotbar0={} hudHidden={} (must be sword, false)", c.player.getInventory().getStack(0).getName().getString(), c.options.hudHidden);
			case 306 -> key(c, GLFW.GLFW_KEY_L, GLFW.GLFW_PRESS);
			case 307 -> key(c, GLFW.GLFW_KEY_K, GLFW.GLFW_PRESS);
			case 308 -> key(c, GLFW.GLFW_KEY_K, GLFW.GLFW_RELEASE);
			case 309 -> key(c, GLFW.GLFW_KEY_L, GLFW.GLFW_RELEASE);
			case 312 -> {
				LOG.info("PROBE rebound: L+K saved loadout 3 = {}", LoadoutStore.get(2, c.world.getRegistryManager()) != null);
				LoadoutKeys.recallModifier.setBoundKey(LoadoutKeys.recallModifier.getDefaultKey());
				LoadoutKeys.SAVE[2].setBoundKey(LoadoutKeys.SAVE[2].getDefaultKey());
				KeyBinding.updateKeysByCode();
				c.options.write();
			}
			case 315 -> c.scheduleStop();
			default -> {
			}
		}
	}

	private static void capture(MinecraftClient c, String name) {
		if (!shots) {
			return;
		}
		c.getToastManager().clear();
		File out = new File(c.runDirectory, name);
		ScreenshotRecorder.takeScreenshot(c.getFramebuffer(), image -> {
			try (image) {
				image.writeTo(out);
				LOG.info("PROBE captured {}", out.getAbsolutePath());
			} catch (Exception e) {
				LOG.error("PROBE capture failed", e);
			}
		});
	}
}
