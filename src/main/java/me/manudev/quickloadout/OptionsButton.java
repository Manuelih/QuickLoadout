package me.manudev.quickloadout;

import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.text.Text;

import java.util.List;

/**
 * Adds "QuickLoadout Settings..." to the first free cell of the Options button grid. When the window is
 * too small for another row, the Done button is narrowed and the new button goes next to it.
 */
public final class OptionsButton {
	private static final int WIDTH = 150;
	private static final int HEIGHT = 20;
	private static final int ROW = 24;

	private OptionsButton() {
	}

	public static void add(Screen screen) {
		List<ClickableWidget> buttons = Screens.getButtons(screen);
		int left = Integer.MAX_VALUE;
		int right = Integer.MIN_VALUE;
		int lastRow = Integer.MIN_VALUE;
		for (ClickableWidget widget : buttons) {
			if (widget.getWidth() == WIDTH) {
				left = Math.min(left, widget.getX());
				right = Math.max(right, widget.getX());
				lastRow = Math.max(lastRow, widget.getY());
			}
		}
		if (lastRow == Integer.MIN_VALUE) {
			return;
		}
		ClickableWidget done = null;
		for (ClickableWidget widget : buttons) {
			if (widget.getY() > lastRow && (done == null || widget.getY() > done.getY())) {
				done = widget;
			}
		}
		int footer = done == null ? screen.height : done.getY();
		for (int y = lastRow; y + HEIGHT <= footer - 4; y += ROW) {
			for (int x : new int[] {left, right}) {
				if (isFree(buttons, x, y)) {
					buttons.add(create(screen, x, y));
					return;
				}
			}
		}
		if (done != null) {
			int center = done.getX() + done.getWidth() / 2;
			done.setWidth(WIDTH);
			done.setX(center - WIDTH - 4);
			buttons.add(create(screen, center + 4, done.getY()));
		}
	}

	private static ButtonWidget create(Screen screen, int x, int y) {
		return ButtonWidget.builder(Text.translatable("quickloadout.settings.button"),
			button -> MinecraftClient.getInstance().setScreen(new LoadoutKeysScreen(screen))).dimensions(x, y, WIDTH, HEIGHT).build();
	}

	private static boolean isFree(List<ClickableWidget> buttons, int x, int y) {
		for (ClickableWidget widget : buttons) {
			if (widget.visible && x < widget.getX() + widget.getWidth() && widget.getX() < x + WIDTH
				&& y < widget.getY() + widget.getHeight() && widget.getY() < y + HEIGHT) {
				return false;
			}
		}
		return true;
	}
}
