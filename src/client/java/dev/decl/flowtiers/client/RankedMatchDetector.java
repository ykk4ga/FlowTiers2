package dev.decl.flowtiers.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.text.Text;


// does not work IDK why
public final class RankedMatchDetector {

    private static final long CACHE_MS = 2000L;
    private static volatile boolean cachedResult = false;
    private static volatile long cachedAt = 0L;

    private RankedMatchDetector() {}

    public static boolean isInRankedMatch() {
        long now = System.currentTimeMillis();
        if (now - cachedAt < CACHE_MS) return cachedResult;
        boolean result = detect();
        cachedResult = result;
        cachedAt = now;
        return result;
    }

    public static boolean nameAlreadyHasTierInfo(Text text) {
        if (text == null) return false;
        String s = text.getString();
        if (s == null || s.isEmpty()) return false;
        String stripped = stripFormatCodes(s).trim();
        if (stripped.isEmpty()) return false;
        String lower = stripped.toLowerCase();
        if (lower.contains("elo")) return true;
        if (stripped.matches("^\\d{2,5}[\\s|·•].*[A-Za-z_].*")) return true;
        return false;
    }

    private static boolean detect() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.world == null) return false;

        // 1) Sidebar scoreboard title keyword
        // 2) Tab-list LIST slot has active objective
        try {
            Scoreboard sb = mc.world.getScoreboard();
            if (sb != null) {
                ScoreboardObjective sidebar = getObjectiveForSlot(sb, 1, "SIDEBAR");
                if (sidebar != null) {
                    String title = sidebar.getDisplayName().getString().toLowerCase();
                    if (isRankedKeyword(title)) return true;
                }
                if (getObjectiveForSlot(sb, 0, "LIST") != null) return true;
            }
        } catch (Throwable ignored) {}

        // 3) Tab list header/footer keyword
        try {
            var hud = mc.inGameHud;
            if (hud != null && hud.getPlayerListHud() != null) {
                Text header = getField(hud.getPlayerListHud(), "header", "field_2153");
                Text footer = getField(hud.getPlayerListHud(), "footer", "field_2152");
                if (textContainsRankedHint(header) || textContainsRankedHint(footer)) return true;
            }
        } catch (Throwable ignored) {}

        // 4) Scan up to 16 tab entries for ELO-prefixed names
        try {
            ClientPlayNetworkHandler net = mc.getNetworkHandler();
            if (net != null) {
                int checked = 0;
                for (PlayerListEntry e : net.getPlayerList()) {
                    if (e == null) continue;
                    Text disp = e.getDisplayName();
                    if (disp != null && nameAlreadyHasTierInfo(disp)) return true;
                    if (++checked >= 16) break;
                }
            }
        } catch (Throwable ignored) {}

        return false;
    }

    private static boolean textContainsRankedHint(Text t) {
        if (t == null) return false;
        return isRankedKeyword(t.getString().toLowerCase());
    }

    private static boolean isRankedKeyword(String lower) {
        if (lower == null || lower.isEmpty()) return false;
        return lower.contains("ranked")
                || lower.contains("duel")
                || lower.contains("elo")
                || lower.contains("matchmaking")
                || lower.contains("1v1")
                || lower.contains("flowpvp")
                || lower.contains("flow pvp");
    }

    private static String stripFormatCodes(String s) {
        return s.replaceAll("\u00A7[0-9a-fk-orA-FK-OR]", "");
    }

    private static ScoreboardObjective getObjectiveForSlot(Scoreboard sb, int legacySlot, String enumName) {
        try {
            java.lang.reflect.Method m = sb.getClass().getMethod("getObjectiveForSlot", int.class);
            Object r = m.invoke(sb, legacySlot);
            if (r instanceof ScoreboardObjective o) return o;
        } catch (Throwable ignored) {}
        try {
            for (java.lang.reflect.Method m : sb.getClass().getMethods()) {
                if (m.getName().equals("getObjectiveForSlot") && m.getParameterCount() == 1) {
                    Class<?> p = m.getParameterTypes()[0];
                    if (p.isEnum()) {
                        for (Object c : p.getEnumConstants()) {
                            if (c.toString().equalsIgnoreCase(enumName)) {
                                Object r = m.invoke(sb, c);
                                if (r instanceof ScoreboardObjective o) return o;
                            }
                        }
                    }
                }
            }
        } catch (Throwable ignored) {}
        return null;
    }

    @SuppressWarnings("unchecked")
    private static <T> T getField(Object target, String... names) {
        for (String n : names) {
            try {
                java.lang.reflect.Field f = target.getClass().getDeclaredField(n);
                f.setAccessible(true);
                return (T) f.get(target);
            } catch (Throwable ignored) {}
        }
        return null;
    }
}