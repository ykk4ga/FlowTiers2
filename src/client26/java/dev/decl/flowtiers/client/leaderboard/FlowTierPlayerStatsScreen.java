package dev.decl.flowtiers.client.leaderboard;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import dev.decl.flowtiers.client.FlowTierFormatter;
import dev.decl.flowtiers.client.FlowTierStats;
import dev.decl.flowtiers.client.FlowTiersClientState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FlowTierPlayerStatsScreen extends Screen {
    private final Screen parent;
    private final UUID uuid;
    private final String fallbackName;
    private boolean loaded;
    private boolean failed;

    public FlowTierPlayerStatsScreen(Screen parent, String uuid, String fallbackName) {
        super(Component.literal("FlowTiers Player Stats"));
        this.parent = parent;
        this.uuid = UUID.fromString(uuid);
        this.fallbackName = fallbackName;
    }

    @Override
    protected void init() {
        clearWidgets();
        addRenderableWidget(Button.builder(Component.literal("Back"), button -> {
            if (minecraft != null) minecraft.setScreen(parent);
        }).bounds(width / 2 - 42, height - 24, 84, 18).build());
        FlowTiersClientState.cache().fetch(uuid).thenAccept(stats -> {
            loaded = true;
            failed = stats == null;
        });
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xE0101420);
        super.extractRenderState(context, mouseX, mouseY, delta);

        int panelLeft = Math.max(20, width / 2 - 260);
        int panelRight = Math.min(width - 20, width / 2 + 260);
        int top = 78;
        int rowHeight = 16;

        context.centeredText(font, Component.literal("FlowPvP player stats"), width / 2, 12, 0xFF7C8BA1);
        context.centeredText(font, Component.literal(fallbackName), width / 2, 32, 0xFFFFFFFF);
        context.fill(panelLeft, top - 18, panelRight, height - 42, 0xAA080B12);
        context.fill(panelLeft, top - 18, panelRight, top - 2, 0xCC111827);

        var stats = FlowTiersClientState.cache().getIfFresh(uuid);
        if (stats.isEmpty()) {
            String message = loaded || failed ? "No FlowPvP ranked stats found for this player." : "Loading...";
            context.centeredText(font, Component.literal(message), width / 2, top + 34, loaded || failed ? 0xFFFFD166 : 0xFFAAAAAA);
            return;
        }

        FlowTierStats playerStats = stats.get();
        context.text(font, "Ladder",  panelLeft + 12,  top - 14, 0xFFB5C7E8, true);
        context.text(font, "Tier",    panelLeft + 110, top - 14, 0xFFB5C7E8, true);
        context.text(font, "ELO",     panelLeft + 210, top - 14, 0xFFB5C7E8, true);
        context.text(font, "Pos",     panelLeft + 275, top - 14, 0xFFB5C7E8, true);
        context.text(font, "W/L",     panelLeft + 320, top - 14, 0xFFB5C7E8, true);
        context.text(font, "Streak",  panelLeft + 390, top - 14, 0xFFB5C7E8, true);

        List<FlowTierStats.LadderStats> ladders = playerStats.ladders().values().stream()
                .filter(FlowTierStats.LadderStats::hasPlayedRanked)
                .sorted(Comparator.comparing((FlowTierStats.LadderStats ladder) -> ladder.ladder().equals("GLOBAL") ? 0 : 1)
                        .thenComparing(Comparator.comparingInt(FlowTierStats.LadderStats::totalRating).reversed()))
                .toList();

        if (ladders.isEmpty()) {
            context.centeredText(font, Component.literal("No ranked stats found."), width / 2, top + 34, 0xFFAAAAAA);
            return;
        }

        int y = top;
        for (int i = 0; i < ladders.size(); i++) {
            FlowTierStats.LadderStats ladder = ladders.get(i);
            if (i % 2 == 0) {
                context.fill(panelLeft + 2, y - 1, panelRight - 2, y + rowHeight - 1, 0x22000000);
            }
            context.text(font, FlowTierFormatter.icon(ladder.ladder()),               panelLeft + 12,  y + 3, 0xFFFFFFFF, true);
            context.text(font, FlowTierFormatter.displayName(ladder.ladder()),        panelLeft + 24,  y + 3, 0xFFFFFFFF, true);
            context.text(font, ladder.tierLabel(),                                    panelLeft + 110, y + 3, tierColor(ladder.tierLabel(), ladder.position()), true);
            context.text(font, ladder.totalRating() + " ELO",                        panelLeft + 210, y + 3, eloColor(ladder.totalRating()), true);
            context.text(font, ladder.hasPosition() ? "#" + ladder.position() : "-", panelLeft + 275, y + 3, ladder.hasPosition() ? 0xFFFFD700 : 0xFF7C8BA1, true);
            context.text(font, ladder.wins() + "/" + ladder.losses(),                panelLeft + 320, y + 3, winLossColor(ladder.wins(), ladder.losses()), true);
            context.text(font, streak(ladder.currentStreak()),                        panelLeft + 390, y + 3, streakColor(ladder.currentStreak()), true);
            y += rowHeight;
        }

        playerStats.bestLadder().ifPresent(best -> context.text(font,
                "Highest: " + FlowTierFormatter.displayName(best.ladder()) + " " + best.tierLabel(),
                panelLeft + 12, height - 34, 0xFFFFD700, true));
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private static String streak(int streak) {
        if (streak > 0) return "+" + streak;
        if (streak < 0) return Integer.toString(streak);
        return "-";
    }

    private static int winLossColor(int wins, int losses) {
        int total = wins + losses;
        if (total == 0) return 0xFF7C8BA1;
        double rate = (double) wins / total;
        if (rate >= 0.55) return 0xFF55FF55;
        if (rate >= 0.45) return 0xFFAAAAAA;
        return 0xFFFF5555;
    }

    private static int streakColor(int streak) {
        if (streak > 0) return 0xFF55FF55;
        if (streak < 0) return 0xFFFF5555;
        return 0xFF7C8BA1;
    }

    private static int tierColor(String tier, int position) {
        if (position == 1 || tier.equals("Grandmaster")) return 0xFFFF55FF;
        if (tier.startsWith("Netherite")) return 0xFF8B5CF6;
        if (tier.startsWith("Diamond")) return 0xFF55FFFF;
        if (tier.startsWith("Emerald")) return 0xFF50C878;
        if (tier.startsWith("Gold")) return 0xFFFFD700;
        if (tier.startsWith("Iron")) return 0xFFC0C0C0;
        if (tier.startsWith("Copper")) return 0xFFCD7F32;
        return 0xFFAAAAAA;
    }

    private static int eloColor(int elo) {
        if (elo >= 2175) return 0xFF8B5CF6;
        if (elo >= 1650) return 0xFF55FFFF;
        if (elo >= 1275) return 0xFF50C878;
        if (elo >= 900) return 0xFFFFD700;
        if (elo >= 600) return 0xFFC0C0C0;
        if (elo >= 300) return 0xFFCD7F32;
        return 0xFFAAAAAA;
    }
}