package me.manudev.quickloadout.mixin;

import me.manudev.quickloadout.LoadoutKeys;
import me.manudev.quickloadout.LoadoutKeysScreen;
import net.minecraft.client.gui.screen.option.ControlsListWidget;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ControlsListWidget.class)
public abstract class ControlsListWidgetMixin {
	@Redirect(method = "<init>", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;allKeys:[Lnet/minecraft/client/option/KeyBinding;", opcode = Opcodes.GETFIELD))
	private KeyBinding[] quickloadout$onlyOwnKeys(GameOptions options, KeybindsScreen parent) {
		return parent instanceof LoadoutKeysScreen ? LoadoutKeys.all() : options.allKeys;
	}
}
