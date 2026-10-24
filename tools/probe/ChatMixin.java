package probe.mixin;

import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Shadow;
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

	// Acts like a server gun plugin: cancels one kind of click when it touches the "Beretta 92FS" and resyncs.
	@Mixin(ServerPlayNetworkHandler.class)
	public static class Clicks {
		@Shadow
		public ServerPlayerEntity player;

		@Inject(method = "onClickSlot", at = @At("HEAD"), cancellable = true)
		private void probe$block(ClickSlotC2SPacket packet, CallbackInfo ci) {
			SlotActionType blocked = probe.Probe.blocked;
			if (blocked == null || packet.actionType() != blocked || !Thread.currentThread().getName().equals("Server thread")) {
				return;
			}
			ScreenHandler handler = player.currentScreenHandler;
			boolean gun = isGun(handler.getCursorStack());
			if (packet.slot() >= 0 && packet.slot() < handler.slots.size()) {
				gun |= isGun(handler.getSlot(packet.slot()).getStack());
			}
			if (blocked == SlotActionType.SWAP && packet.button() >= 0 && packet.button() < 9) {
				gun |= isGun(player.getInventory().getStack(packet.button()));
			}
			if (gun) {
				probe.Probe.cancelled++;
				ci.cancel();
				handler.syncState();
			}
		}

		private static boolean isGun(ItemStack stack) {
			return stack.getName().getString().equals("Beretta 92FS");
		}
	}
}
