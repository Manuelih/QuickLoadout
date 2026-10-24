package me.manudev.quickloadout;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Chat (saving): "ǫᴜɪᴄᴋʟᴏᴀᴅᴏᴜᴛ » message" with an electric green prefix. Action bar (recalling): "» message".
 * Every message is written in small caps after translation, so it works in any language.
 */
public final class Messages {
	private static final int PREFIX_COLOR = 0x39FF14;
	private static final String PREFIX = smallCaps("QuickLoadout");
	private static final String PLAIN = "abcdefghijklmnopqrstuvwxyzàáèéìíòóùú";
	private static final String SMALL = "ᴀʙᴄᴅᴇꜰɢʜɪᴊᴋʟᴍɴᴏᴘǫʀꜱᴛᴜᴠᴡxʏᴢᴀᴀᴇᴇɪɪᴏᴏᴜᴜ";

	private Messages() {
	}

	public static String smallCaps(String text) {
		StringBuilder out = new StringBuilder(text.length());
		for (char c : text.toCharArray()) {
			int index = PLAIN.indexOf(Character.toLowerCase(c));
			out.append(index >= 0 ? SMALL.charAt(index) : c);
		}
		return out.toString();
	}

	private static MutableText body(Text message) {
		return Text.literal("» ").formatted(Formatting.DARK_GRAY)
			.append(Text.literal(smallCaps(message.getString())).formatted(Formatting.GRAY));
	}

	public static void chat(MinecraftClient client, Text message) {
		Text line = Text.literal(PREFIX).styled(style -> style.withColor(PREFIX_COLOR))
			.append(Text.literal(" "))
			.append(body(message));
		client.inGameHud.getChatHud().addMessage(line);
	}

	public static void actionBar(MinecraftClient client, Text message) {
		client.inGameHud.setOverlayMessage(body(message), false);
	}
}
