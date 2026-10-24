package probe.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.text.Text;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class ChatMixin {
	@Mixin(ChatHud.class)
	public static class Chat {
		@Inject(method = "addMessage(Lnet/minecraft/text/Text;)V", at = @At("HEAD"))
		private void probe$log(Text text, CallbackInfo ci) {
			LoggerFactory.getLogger("PROBE").info("PROBE chat: {}", text.getString());
		}
	}

	@Mixin(InGameHud.class)
	public static class Overlay {
		@Inject(method = "setOverlayMessage", at = @At("HEAD"))
		private void probe$log(Text text, boolean tinted, CallbackInfo ci) {
			LoggerFactory.getLogger("PROBE").info("PROBE overlay: {}", text.getString());
		}
	}
}
