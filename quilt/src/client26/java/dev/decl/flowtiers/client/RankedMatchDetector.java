package dev.decl.flowtiers.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

public final class RankedMatchDetector {
    private static final long CACHE_MS = 2000L;
    private static volatile boolean cachedResult = false;
    private static volatile long cachedAt = 0L;

    private RankedMatchDetector() {}

    public static boolean isInRankedMatch() {
        long now = System.currentTimeMillis();
        if (now - cachedAt < CACHE_MS) return cachedResult;
        cachedResult = detect();
        cachedAt = now;
        return cachedResult;
    }

    public static boolean nameAlreadyHasTierInfo(Component text) {
        if (text == null) return false;
        String s = stripFormatCodes(text.getString()).trim();
        if (s.isEmpty()) return false;
        return hasTierToken(s) || s.matches("(?i).*\\b\\d{2,5}\\s+(SR|ELO)\\b.*");
    }

    private static boolean detect() {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return false;

        try {
            var overlay = mc.gui.getTabList();
            for (java.lang.reflect.Field f : overlay.getClass().getDeclaredFields()) {
                if (f.getType() == Component.class) {
                    f.setAccessible(true);
                    Component t = (Component) f.get(overlay);
                    if (t != null && isRankedKeyword(t.getString().toLowerCase())) return true;
                }
            }
        } catch (Throwable ignored) {}

        return false;
    }

    private static boolean isRankedKeyword(String lower) {
        return lower.contains("ranked") || lower.contains("duel")
                || lower.contains("elo") || lower.contains("matchmaking")
                || lower.contains("1v1") || lower.contains("flowpvp")
                || lower.contains("flow pvp");
    }

    private static String stripFormatCodes(String s) {
        return s.replaceAll("\u00A7[0-9a-fk-orA-FK-OR]", "");
    }

    private static boolean hasTierToken(String s) {
        return s.matches("(?i).*\\b[LMH]T[1-5]\\b.*")
                || s.matches("(?i).*\\b(Coal|Copper|Iron|Gold|Emerald|Diamond|Netherite|Grandmaster)(\\s+(I|II|III|IV|V))?\\b.*");
    }
}
