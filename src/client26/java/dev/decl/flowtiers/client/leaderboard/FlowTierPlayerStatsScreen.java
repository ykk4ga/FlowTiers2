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

    private String selectedLadder = null;
    private List<FlowTierLeaderboardClient.HistoryPoint> historyPoints = null;
    private boolean historyLoading = false;
    private int[] graphXPositions = null;
    private int[] graphYPositions = null;

    public FlowTierPlayerStatsScreen(Screen parent, String uuid, String fallbackName, String autoOpenLadder) {
        super(Component.literal("FlowTiers Player Stats"));
        this.parent = parent;
        this.uuid = UUID.fromString(uuid);
        this.fallbackName = fallbackName;
        this.selectedLadder = autoOpenLadder;
        if (autoOpenLadder != null) {
            this.historyLoading = true;
        }
    }

    public FlowTierPlayerStatsScreen(Screen parent, String uuid, String fallbackName) {
        this(parent, uuid, fallbackName, null);
    }

    @Override
    protected void init() {
        clearWidgets();

        addRenderableWidget(Button.builder(Component.literal("Back"), button -> {
            if (selectedLadder != null) {
                selectedLadder = null;
                historyPoints = null;
                graphXPositions = null;
                graphYPositions = null;
                init();
            } else {
                if (minecraft != null) minecraft.setScreen(parent);
            }
        }).bounds(width / 2 - 42, height - 24, 84, 18).build());

        if (selectedLadder == null) {
            var cached = FlowTiersClientState.cache().getIfFresh(uuid);
            if (cached.isPresent()) {
                addLadderButtons(cached.get());
            }
        }

        FlowTiersClientState.cache().fetch(uuid).thenAccept(stats -> {
            if (!loaded) {
                loaded = true;
                failed = stats == null;
                if (minecraft != null && selectedLadder == null) {
                    minecraft.execute(this::init);
                }
            }
        });

        if (selectedLadder != null && historyPoints == null && historyLoading) {
            FlowTiersClientState.leaderboardClient()
                    .fetchHistory(uuid.toString(), selectedLadder)
                    .thenAccept(points -> {
                        if (minecraft != null) minecraft.execute(() -> {
                            historyPoints = points;
                            historyLoading = false;
                        });
                    });
        }
    }

    private void addLadderButtons(FlowTierStats playerStats) {
        int panelLeft = Math.max(20, width / 2 - 260);
        int panelRight = Math.min(width - 20, width / 2 + 260);
        int top = 78;
        int rowHeight = 16;

        List<FlowTierStats.LadderStats> ladders = playerStats.ladders().values().stream()
                .filter(FlowTierStats.LadderStats::hasPlayedRanked)
                .sorted(Comparator.comparing((FlowTierStats.LadderStats l) -> l.ladder().equals("GLOBAL") ? 0 : 1)
                        .thenComparing(Comparator.comparingInt(FlowTierStats.LadderStats::totalRating).reversed()))
                .toList();

        int y = top;
        for (FlowTierStats.LadderStats ladder : ladders) {
            if (!ladder.ladder().equals("GLOBAL")) {
                final String ladderId = ladder.ladder();
                final int buttonY = y;
                Button btn = Button.builder(Component.empty(), b -> {
                    selectedLadder = ladderId;
                    historyPoints = null;
                    graphXPositions = null;
                    graphYPositions = null;
                    historyLoading = true;
                    init();
                    FlowTiersClientState.leaderboardClient()
                            .fetchHistory(uuid.toString(), ladderId)
                            .thenAccept(points -> {
                                if (minecraft != null) minecraft.execute(() -> {
                                    historyPoints = points;
                                    historyLoading = false;
                                });
                            });
                }).bounds(panelLeft, buttonY - 1, panelRight - panelLeft, rowHeight).build();
                btn.setAlpha(0f);
                addRenderableWidget(btn);
            }
            y += rowHeight;
        }
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
        context.fill(panelLeft, top - 18, panelRight, height - 28, 0xAA080B12);
        context.fill(panelLeft, top - 18, panelRight, top - 2, 0xCC111827);

        var stats = FlowTiersClientState.cache().getIfFresh(uuid);
        if (stats.isEmpty()) {
            String message = loaded || failed ? "No FlowPvP ranked stats found for this player." : "Loading...";
            context.centeredText(font, Component.literal(message), width / 2, top + 34,
                    loaded || failed ? 0xFFFFD166 : 0xFFAAAAAA);
            return;
        }

        FlowTierStats playerStats = stats.get();

        if (selectedLadder != null) {
            renderGraph(context, playerStats, panelLeft, panelRight, top, mouseX, mouseY);
            return;
        }

        context.text(font, "Ladder",  panelLeft + 12,  top - 14, 0xFFB5C7E8, true);
        context.text(font, "Tier",    panelLeft + 100, top - 14, 0xFFB5C7E8, true);
        context.text(font, "ELO",     panelLeft + 185, top - 14, 0xFFB5C7E8, true);
        context.text(font, "Pos",     panelLeft + 245, top - 14, 0xFFB5C7E8, true);
        context.text(font, "W/L",     panelLeft + 290, top - 14, 0xFFB5C7E8, true);
        context.text(font, "Streak",  panelLeft + 355, top - 14, 0xFFB5C7E8, true);

        List<FlowTierStats.LadderStats> ladders = playerStats.ladders().values().stream()
                .filter(FlowTierStats.LadderStats::hasPlayedRanked)
                .sorted(Comparator.comparing((FlowTierStats.LadderStats l) -> l.ladder().equals("GLOBAL") ? 0 : 1)
                        .thenComparing(Comparator.comparingInt(FlowTierStats.LadderStats::totalRating).reversed()))
                .toList();

        if (ladders.isEmpty()) {
            context.centeredText(font, Component.literal("No ranked stats found."), width / 2, top + 34, 0xFFAAAAAA);
            return;
        }

        int y = top;
        for (int i = 0; i < ladders.size(); i++) {
            FlowTierStats.LadderStats ladder = ladders.get(i);
            boolean hovered = mouseX >= panelLeft && mouseX <= panelRight
                    && mouseY >= y - 1 && mouseY < y + rowHeight - 1
                    && !ladder.ladder().equals("GLOBAL");

            if (hovered) {
                context.fill(panelLeft + 2, y - 1, panelRight - 2, y + rowHeight - 1, 0x553B82F6);
            } else if (i % 2 == 0) {
                context.fill(panelLeft + 2, y - 1, panelRight - 2, y + rowHeight - 1, 0x22000000);
            }

            context.text(font, FlowTierFormatter.icon(ladder.ladder()),               panelLeft + 12,  y + 3, 0xFFFFFFFF, true);
            context.text(font, FlowTierFormatter.displayName(ladder.ladder()),        panelLeft + 24,  y + 3, 0xFFFFFFFF, true);
            context.text(font, ladder.tierLabel(),                                    panelLeft + 100, y + 3, tierColor(ladder.tierLabel(), ladder.position()), true);
            context.text(font, ladder.totalRating() + " ELO",                        panelLeft + 185, y + 3, eloColor(ladder.totalRating()), true);
            context.text(font, ladder.hasPosition() ? "#" + ladder.position() : "-", panelLeft + 245, y + 3, ladder.hasPosition() ? 0xFFFFD700 : 0xFF7C8BA1, true);
            context.text(font, ladder.wins() + "/" + ladder.losses(),                panelLeft + 290, y + 3, winLossColor(ladder.wins(), ladder.losses()), true);
            context.text(font, streak(ladder.currentStreak()),                        panelLeft + 355, y + 3, streakColor(ladder.currentStreak()), true);
            y += rowHeight;
        }

        int highestY = Math.max(height - 46, top + ladders.size() * rowHeight + 6);
        playerStats.bestLadder().ifPresent(best -> context.text(font,
                "Highest: " + FlowTierFormatter.displayName(best.ladder()) + " " + best.tierLabel(),
                panelLeft + 12, highestY, 0xFFFFD700, true));
    }

    private void renderGraph(GuiGraphicsExtractor context, FlowTierStats playerStats, int panelLeft, int panelRight, int top, int mouseX, int mouseY) {
        FlowTierStats.LadderStats ladder = playerStats.ladders().get(selectedLadder);

        context.centeredText(font, Component.literal(FlowTierFormatter.displayName(selectedLadder) + " - ELO History"),
                width / 2, top - 12, 0xFFB5C7E8);

        if (ladder != null) {
            context.text(font,
                    ladder.tierLabel() + "  " + ladder.totalRating() + " ELO  " + ladder.wins() + "W/" + ladder.losses() + "L",
                    panelLeft + 12, top + 2, tierColor(ladder.tierLabel(), ladder.position()), true);
        }

        int graphLeft = panelLeft + 40;
        int graphRight = panelRight - 12;
        int graphTop = top + 20;
        int graphBottom = height - 52;
        int graphW = graphRight - graphLeft;
        int graphH = graphBottom - graphTop;

        context.fill(graphLeft, graphTop, graphRight, graphBottom, 0x33000000);
        context.fill(graphLeft, graphTop, graphLeft + 1, graphBottom, 0x44FFFFFF);
        context.fill(graphLeft, graphBottom - 1, graphRight, graphBottom, 0x44FFFFFF);

        if (historyLoading) {
            context.centeredText(font, Component.literal("Loading history..."), width / 2, graphTop + graphH / 2, 0xFFAAAAAA);
            return;
        }

        if (historyPoints == null || historyPoints.isEmpty()) {
            context.centeredText(font, Component.literal("No history data available."), width / 2, graphTop + graphH / 2, 0xFFAAAAAA);
            return;
        }

        int minElo = historyPoints.stream().mapToInt(FlowTierLeaderboardClient.HistoryPoint::elo).min().orElse(0);
        int maxElo = historyPoints.stream().mapToInt(FlowTierLeaderboardClient.HistoryPoint::elo).max().orElse(1);
        int eloRange = Math.max(1, maxElo - minElo);
        int padding = Math.max(10, eloRange / 10);
        minElo -= padding;
        maxElo += padding;
        eloRange = maxElo - minElo;

        for (int i = 0; i <= 4; i++) {
            int gridElo = minElo + (eloRange * i / 4);
            int gridY = graphBottom - (gridElo - minElo) * graphH / eloRange;
            context.fill(graphLeft, gridY, graphRight, gridY + 1, 0x22FFFFFF);
            context.text(font, Integer.toString(gridElo), panelLeft + 12, gridY - 4, 0xFF7C8BA1, true);
        }

        int n = historyPoints.size();
        if (graphXPositions == null || graphXPositions.length != n) {
            graphXPositions = new int[n];
            graphYPositions = new int[n];
        }

        int prevX = -1, prevY = -1;
        for (int i = 0; i < n; i++) {
            int elo = historyPoints.get(i).elo();
            int x = graphLeft + (n == 1 ? graphW / 2 : i * graphW / (n - 1));
            int y = graphBottom - (elo - minElo) * graphH / eloRange;
            graphXPositions[i] = x;
            graphYPositions[i] = y;
            int dotColor = eloColor(elo);

            if (prevX >= 0) {
                int lineColor = dotColor & 0xAAFFFFFF;
                int minY = Math.min(prevY, y);
                int maxY = Math.max(prevY, y);
                context.fill(prevX, minY, prevX + 1, maxY + 1, lineColor);
                context.fill(Math.min(prevX, x), y, Math.max(prevX, x) + 1, y + 1, lineColor);
            }

            context.fill(x - 1, y - 1, x + 3, y + 3, dotColor);
            prevX = x;
            prevY = y;
        }

        context.text(font, Integer.toString(historyPoints.get(0).elo()),
                graphLeft + 2, graphTop + 2, 0xFF7C8BA1, true);
        context.text(font, Integer.toString(historyPoints.get(n - 1).elo()),
                graphRight - 24, graphTop + 2, eloColor(historyPoints.get(n - 1).elo()), true);

        if (graphXPositions != null && mouseY >= graphTop && mouseY <= graphBottom) {
            int closestIdx = -1;
            int closestDist = 8;
            for (int i = 0; i < graphXPositions.length; i++) {
                int dist = Math.max(Math.abs(mouseX - graphXPositions[i]), Math.abs(mouseY - graphYPositions[i]));
                if (dist < closestDist) {
                    closestDist = dist;
                    closestIdx = i;
                }
            }
            if (closestIdx >= 0) {
                int elo = historyPoints.get(closestIdx).elo();
                int prev = closestIdx > 0 ? historyPoints.get(closestIdx - 1).elo() : elo;
                int delta = elo - prev;
                String deltaStr = closestIdx == 0 ? "start" : (delta >= 0 ? "+" + delta : Integer.toString(delta));
                int deltaColor = closestIdx == 0 ? 0xFF7C8BA1 : (delta >= 0 ? 0xFF55FF55 : 0xFFFF5555);

                int tipX = Math.min(graphXPositions[closestIdx] + 6, graphRight - 70);
                int tipY = Math.max(graphTop + 2, graphYPositions[closestIdx] - 26);
                context.fill(tipX - 3, tipY - 3, tipX + 68, tipY + 22, 0xEE000000);
                context.fill(tipX - 3, tipY - 3, tipX + 68, tipY - 2, 0xFF3B82F6);
                context.text(font, elo + " ELO", tipX, tipY + 2, eloColor(elo), true);
                context.text(font, deltaStr, tipX, tipY + 12, deltaColor, true);

                context.fill(graphXPositions[closestIdx] - 2, graphYPositions[closestIdx] - 2,
                        graphXPositions[closestIdx] + 4, graphYPositions[closestIdx] + 4, 0xFFFFFFFF);
            }
        }
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