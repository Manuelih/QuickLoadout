package me.manudev.quickloadout;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.text.Text;

/**
 * The vanilla Key Binds screen, showing only the QuickLoadout keys (see ControlsListWidgetMixin and
 * KeybindsScreenMixin). Reusing it keeps key capture identical to vanilla on every supported version.
 */
public class LoadoutKeysScreen extends KeybindsScreen {
	public static final Text TITLE = Text.translatable("quickloadout.settings.title");

	public LoadoutKeysScreen(Screen parent) {
		super(parent, MinecraftClient.getInstance().options);
	}

	@Override
	public Text getTitle() {
		return TITLE;
	}

	@Override
	protected void initHeader() {
		this.layout.addHeader(TITLE, this.textRenderer);
	}
}
