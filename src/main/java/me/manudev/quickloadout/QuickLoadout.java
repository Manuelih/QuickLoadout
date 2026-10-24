package me.manudev.quickloadout;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.option.KeyBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuickLoadout implements ClientModInitializer {
	public static final String MOD_ID = "quickloadout";
	public static final Logger LOGGER = LoggerFactory.getLogger("QuickLoadout");

	@Override
	public void onInitializeClient() {
		LoadoutKeys.register();
		LoadoutStore.load();
		ClientLifecycleEvents.CLIENT_STARTED.register(client -> {
			KeyBinding.updateKeysByCode();
			LoadoutInput.install(client);
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			LoadoutInput.tick(client);
			LoadoutManager.tick(client);
		});
		ScreenEvents.AFTER_INIT.register((client, screen, width, height) -> {
			if (screen instanceof OptionsScreen) {
				OptionsButton.add(screen);
			}
		});
	}
}
