package dev.decl.flowtiers.client;

import java.lang.reflect.Field;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class RankedMatchDetector {
	private static final long CACHE_MS = 2000L;
	private static volatile boolean cachedResult = false;
	private static volatile long cachedAt = 0L;

	private RankedMatchDetector() {
	}

	public static boolean isInRankedMatch() {
		long now = System.currentTimeMillis();
		if (now - cachedAt < CACHE_MS) return cachedResult;
		cachedResult = detect();
		cachedAt = now;
		return cachedResult;
	}

	public static boolean nameAlreadyHasTierInfo(Component text) {
		if (text == null) return false;
		String value = stripFormatCodes(text.getString()).trim();
		if (value.isEmpty()) return false;
		return hasTierToken(value) || value.matches("(?i).*\\b\\d{2,5}\\s+(SR|ELO)\\b.*");
	}

	private static boolean detect() {
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.level == null) return false;

		try {
			Object overlay = client.gui.getTabList();
			for (Field field : overlay.getClass().getDeclaredFields()) {
				if (field.getType() != Component.class) continue;
				field.setAccessible(true);
				Component text = (Component) field.get(overlay);
				if (text != null && isRankedKeyword(text.getString().toLowerCase())) return true;
			}
		} catch (Throwable ignored) {
		}

		return false;
	}

	private static boolean isRankedKeyword(String lower) {
		return lower.contains("ranked") || lower.contains("duel")
				|| lower.contains("elo") || lower.contains("matchmaking")
				|| lower.contains("1v1") || lower.contains("flowpvp")
				|| lower.contains("flow pvp");
	}

	private static String stripFormatCodes(String value) {
		return value.replaceAll("\u00A7[0-9a-fk-orA-FK-OR]", "");
	}

	private static boolean hasTierToken(String value) {
		return value.matches("(?i).*\\b[LMH]T[1-5]\\b.*")
				|| value.matches("(?i).*\\b(Coal|Copper|Iron|Gold|Emerald|Diamond|Netherite|Grandmaster)(\\s+(I|II|III|IV|V))?\\b.*");
	}
}
