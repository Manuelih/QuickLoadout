package me.manudev.quickloadout.mixin;

import me.manudev.quickloadout.LoadoutKeys;
import me.manudev.quickloadout.LoadoutKeysScreen;
import net.minecraft.client.gui.screen.option.KeybindsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBinding;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// "Reset Keys" and its enabled state only look at the QuickLoadout keys on the QuickLoadout screen.
@Mixin(KeybindsScreen.class)
public abstract class KeybindsScreenMixin {
	@Redirect(method = "*", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/GameOptions;allKeys:[Lnet/minecraft/client/option/KeyBinding;", opcode = Opcodes.GETFIELD))
	private KeyBinding[] quickloadout$onlyOwnKeys(GameOptions options) {
		return (Object) this instanceof LoadoutKeysScreen ? LoadoutKeys.all() : options.allKeys;
	}
}
