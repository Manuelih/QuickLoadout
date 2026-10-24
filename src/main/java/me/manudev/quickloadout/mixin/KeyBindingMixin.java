package me.manudev.quickloadout.mixin;

import me.manudev.quickloadout.LoadoutKeys;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {
	@Shadow
	@Final
	@SuppressWarnings("rawtypes")
	private static Map KEY_TO_BINDINGS;

	@Shadow
	@Final
	private static Map<String, KeyBinding> KEYS_BY_ID;

	@Inject(method = "updateKeysByCode", at = @At("TAIL"))
	private static void quickloadout$giveKeysBack(CallbackInfo ci) {
		LoadoutKeys.restoreVanilla(KEY_TO_BINDINGS, KEYS_BY_ID);
	}

	@Inject(method = "equals(Lnet/minecraft/client/option/KeyBinding;)Z", at = @At("HEAD"), cancellable = true)
	private void quickloadout$hideIntendedOverlaps(KeyBinding other, CallbackInfoReturnable<Boolean> cir) {
		if (LoadoutKeys.isIntendedOverlap((KeyBinding) (Object) this, other)) {
			cir.setReturnValue(false);
		}
	}
}
