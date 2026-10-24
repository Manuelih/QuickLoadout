package me.manudev.quickloadout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryWrapper;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loadouts live in config/quickloadout.json, shared by every world and server. Each slot is the item
 * stack encoded with the vanilla codec, so names, enchantments and every other component survive.
 */
public final class LoadoutStore {
	public static final int SLOTS = 9;
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static final JsonElement[][] LOADOUTS = new JsonElement[LoadoutKeys.LOADOUTS][];

	private LoadoutStore() {
	}

	private static Path file() {
		return FabricLoader.getInstance().getConfigDir().resolve("quickloadout.json");
	}

	static void load() {
		Path file = file();
		if (!Files.exists(file)) {
			return;
		}
		try (Reader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
			JsonArray loadouts = JsonParser.parseReader(reader).getAsJsonObject().getAsJsonArray("loadouts");
			for (int i = 0; i < Math.min(loadouts.size(), LoadoutKeys.LOADOUTS); i++) {
				if (!loadouts.get(i).isJsonArray()) {
					continue;
				}
				JsonArray slots = loadouts.get(i).getAsJsonArray();
				LOADOUTS[i] = new JsonElement[SLOTS];
				for (int s = 0; s < SLOTS; s++) {
					LOADOUTS[i][s] = s < slots.size() ? slots.get(s) : JsonNull.INSTANCE;
				}
			}
		} catch (Exception e) {
			QuickLoadout.LOGGER.warn("Could not read quickloadout.json: {}", e.getMessage());
		}
	}

	private static void write() {
		JsonObject root = new JsonObject();
		JsonArray loadouts = new JsonArray();
		for (JsonElement[] loadout : LOADOUTS) {
			if (loadout == null) {
				loadouts.add(JsonNull.INSTANCE);
				continue;
			}
			JsonArray slots = new JsonArray();
			for (JsonElement slot : loadout) {
				slots.add(slot);
			}
			loadouts.add(slots);
		}
		root.add("loadouts", loadouts);
		Path file = file();
		try {
			Files.createDirectories(file.getParent());
			try (Writer writer = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
				GSON.toJson(root, writer);
			}
		} catch (Exception e) {
			QuickLoadout.LOGGER.warn("Could not save quickloadout.json: {}", e.getMessage());
		}
	}

	public static void save(int loadout, ItemStack[] hotbar, RegistryWrapper.WrapperLookup registries) {
		DynamicOps<JsonElement> ops = registries.getOps(JsonOps.INSTANCE);
		JsonElement[] slots = new JsonElement[SLOTS];
		for (int s = 0; s < SLOTS; s++) {
			ItemStack stack = hotbar[s];
			if (stack.isEmpty()) {
				slots[s] = JsonNull.INSTANCE;
				continue;
			}
			slots[s] = ItemStack.CODEC.encodeStart(ops, stack).resultOrPartial(QuickLoadout.LOGGER::warn).orElse(JsonNull.INSTANCE);
		}
		LOADOUTS[loadout] = slots;
		write();
	}

	/** Returns the saved stacks, EMPTY for empty slots, or null when the loadout was never saved. */
	public static ItemStack[] get(int loadout, RegistryWrapper.WrapperLookup registries) {
		JsonElement[] slots = LOADOUTS[loadout];
		if (slots == null) {
			return null;
		}
		ItemStack[] stacks = new ItemStack[SLOTS];
		DynamicOps<JsonElement> ops = registries.getOps(JsonOps.INSTANCE);
		for (int s = 0; s < SLOTS; s++) {
			stacks[s] = slots[s].isJsonNull() ? ItemStack.EMPTY
				: ItemStack.CODEC.parse(ops, slots[s]).resultOrPartial(QuickLoadout.LOGGER::warn).orElse(ItemStack.EMPTY);
		}
		return stacks;
	}
}
