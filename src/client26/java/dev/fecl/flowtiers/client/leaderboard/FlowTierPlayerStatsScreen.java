package dev.fecl.flowtiers.client.leaderboard;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import dev.fecl.flowtiers.client.FlowTierClientConfig;
import dev.fecl.flowtiers.client.FlowTierFormatter;
import dev.fecl.flowtiers.client.FlowTierSeasonArchive;
import dev.fecl.flowtiers.client.FlowTierStats;
import dev.fecl.flowtiers.client.FlowTierStatsClipboard;
import dev.fecl.flowtiers.client.FlowTiersClientState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FlowTierPlayerStatsScreen extends Screen {
    private static final DateTimeFormatter GRAPH_DATE = DateTimeFormatter.ofPattern("MMM d");

    // ── palette ────────────────────────────────────────────────────────────
    private static final int BG_BASE      = 0xF0080C14;
    private static final int BG_PANEL     = 0xCC0D1424;
    private static final int BG_HEADER    = 0xDD111E35;
    private static final int BG_ROW_ALT   = 0x18FFFFFF;
    private static final int BG_ROW_HOVER = 0x443B82F6;
    private static final int BG_GRAPH     = 0x33000000;
    private static final int ACCENT_BLUE  = 0xFF3B82F6;
    private static final int ACCENT_DIM   = 0x663B82F6;
    private static final int BORDER       = 0x441E3A5C;
    private static final int TEXT_TITLE   = 0xFF7C9EC4;
    private static final int TEXT_HEADER  = 0xFFB0C8E8;
    private static final int TEXT_DIM     = 0xFF526882;
    private static final int TEXT_WHITE   = 0xFFECF0FF;
    private static final int AXIS_LINE    = 0x33FFFFFF;

    private final Screen parent;
    private final UUID uuid;
    private final String fallbackName;
    private boolean loaded;
    private boolean failed;
    private int selectedSeason = -1;

    private String selectedLadder = null;
    private List<FlowTierLeaderboardClient.HistoryPoint> historyPoints = null;
    private boolean historyLoading = false;
    private int[] graphXPositions = null;
    private int[] graphYPositions = null;

    // ── layout helpers ─────────────────────────────────────────────────────
    private int panelLeft()    { return Math.max(16, width / 2 - 270); }
    private int panelRight()   { return Math.min(width - 16, width / 2 + 270); }
    private int panelWidth()   { return panelRight() - panelLeft(); }
    private int headerTop()    { return 8; }
    private int headerBottom() { return 56; }
    private int tableTop()     { return headerBottom() + 4; }
    private int tableBottom()  { return height - 32; }
    private int rowH()         { return 17; }

    public FlowTierPlayerStatsScreen(Screen parent, String uuid, String fallbackName, String autoOpenLadder) {
        super(Component.literal("FlowTiers Player Stats"));
        this.parent = parent;
        this.uuid = UUID.fromString(uuid);
        this.fallbackName = fallbackName;
        this.selectedLadder = autoOpenLadder;
        FlowTierClientConfig.recordPlayerVisit(this.uuid.toString(), fallbackName);
        if (autoOpenLadder != null) this.historyLoading = true;
    }

    public FlowTierPlayerStatsScreen(Screen parent, String uuid, String fallbackName) {
        this(parent, uuid, fallbackName, null);
    }

    @Override
    protected void init() {
        clearWidgets();

        String backLabel = selectedLadder != null ? "Back" : "Close";
        addRenderableWidget(Button.builder(Component.literal(backLabel), btn -> {
            if (selectedLadder != null) {
                selectedLadder = null;
                historyPoints = null;
                graphXPositions = null;
                graphYPositions = null;
                init();
            } else {
                if (minecraft != null) minecraft.setScreen(parent);
            }
        }).bounds(panelLeft(), height - 26, 78, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Copy profile link"), btn -> copyProfileLink())
                .bounds(panelLeft() + 84, height - 26, 104, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Copy stats"), btn -> copyStats())
                .bounds(panelLeft() + 194, height - 26, 82, 18).build());
        addRenderableWidget(Button.builder(Component.literal(FlowTierClientConfig.isFavorite(uuid.toString()) ? "Remove favorite" : "Add favorite"), btn -> {
            FlowTierClientConfig.toggleFavorite(uuid.toString(), fallbackName);
            init();
        }).bounds(panelLeft() + 282, height - 26, 104, 18).build());
        List<FlowTierSeasonArchive.Season> seasons = FlowTierSeasonArchive.seasonsFor(uuid);
        if (!seasons.isEmpty()) {
            addRenderableWidget(Button.builder(Component.literal(seasonButtonLabel(seasons)), btn -> {
                selectedLadder = null;
                selectedSeason++;
                if (selectedSeason >= seasons.size()) selectedSeason = -1;
                init();
            }).bounds(panelLeft() + 392, height - 26, 132, 18).build());
        }

        if (selectedLadder == null && selectedSeason < 0) {
            var cached = FlowTiersClientState.cache().getIfFresh(uuid);
            if (cached.isPresent()) addLadderButtons(cached.get());
        }

        FlowTiersClientState.cache().fetch(uuid).thenAccept(stats -> {
            if (!loaded) {
                loaded = true;
                failed = stats == null;
                if (minecraft != null && selectedLadder == null) minecraft.execute(this::init);
            }
        });

        if (selectedLadder != null && historyPoints == null && historyLoading) {
            FlowTiersClientState.leaderboardClient()
                    .fetchHistory(uuid.toString(), selectedLadder)
                    .thenAccept(pts -> {
                        if (minecraft != null) minecraft.execute(() -> {
                            historyPoints = pts;
                            historyLoading = false;
                        });
                    });
        }
    }

    private void addLadderButtons(FlowTierStats stats) {
        List<FlowTierStats.LadderStats> ladders = sortedLadders(stats);
        int pl = panelLeft() + 2;
        int pr = panelRight() - 2;
        int y = tableTop() + rowH() + 4;

        for (FlowTierStats.LadderStats l : ladders) {
            if (!l.ladder().equals("GLOBAL")) {
                final String id = l.ladder();
                final int fy = y;
                Button btn = Button.builder(Component.empty(), b -> openLadder(id))
                        .bounds(pl, fy, pr - pl, rowH()).build();
                btn.setAlpha(0f);
                addRenderableWidget(btn);
            }
            y += rowH();
        }
    }

    private void openLadder(String id) {
        selectedLadder = id;
        historyPoints = null;
        graphXPositions = null;
        graphYPositions = null;
        historyLoading = true;
        init();
        FlowTiersClientState.leaderboardClient()
                .fetchHistory(uuid.toString(), id)
                .thenAccept(pts -> {
                    if (minecraft != null) minecraft.execute(() -> {
                        historyPoints = pts;
                        historyLoading = false;
                    });
                });
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int mx, int my, float delta) {
        int pl = panelLeft(), pr = panelRight();
        int ht = headerTop(), hb = headerBottom();
        int tt = tableTop(), tb = tableBottom();

        ctx.fill(0, 0, width, height, BG_BASE);
        ctx.fill(0, 0, width / 4, height, 0x08FFFFFF);

        ctx.fill(pl, tt, pr, tb, BG_PANEL);
        ctx.fill(pl,     tt,     pr,     tt + 1, BORDER);
        ctx.fill(pl,     tb - 1, pr,     tb,     BORDER);
        ctx.fill(pl,     tt,     pl + 1, tb,     BORDER);
        ctx.fill(pr - 1, tt,     pr,     tb,     BORDER);

        ctx.fill(pl, ht, pr, hb, BG_HEADER);
        ctx.fill(pl, hb - 1, pr, hb, ACCENT_DIM);

        ctx.centeredText(font, Component.literal("FLOWPVP  STATS"), width / 2, ht + 6, TEXT_TITLE);
        ctx.centeredText(font, Component.literal(fallbackName), width / 2, ht + 20, TEXT_WHITE);
        ctx.centeredText(font, Component.literal(selectedSeasonName()), width / 2, ht + 34, TEXT_DIM);

        super.extractRenderState(ctx, mx, my, delta);

        var statsOpt = displayedStats();
        if (statsOpt.isEmpty()) {
            String msg = selectedSeason >= 0 ? "No archived stats found for this season." : (loaded || failed ? "No ranked stats found for this player." : "Loading...");
            int col = loaded || failed ? 0xFFFFD166 : TEXT_DIM;
            ctx.centeredText(font, Component.literal(msg), width / 2, tt + 40, col);
            return;
        }

        FlowTierStats playerStats = statsOpt.get();

        if (selectedLadder != null) {
            renderGraph(ctx, playerStats, pl, pr, tt, tb, mx, my);
        } else {
            renderTable(ctx, playerStats, pl, pr, tt, tb, mx, my);
        }
    }

    private void renderTable(GuiGraphicsExtractor ctx, FlowTierStats stats,
                             int pl, int pr, int tt, int tb, int mx, int my) {
        int pw = pr - pl;
        int hy = tt + 4;
        ctx.fill(pl + 2, hy, pr - 2, hy + rowH() - 2, BG_HEADER);
        ctx.fill(pl + 2, hy + rowH() - 2, pr - 2, hy + rowH() - 1, ACCENT_DIM);

        ctx.text(font, "LADDER", pl + col(pw, 0), hy + 4, TEXT_HEADER, true);
        ctx.text(font, "TIER",   pl + col(pw, 1), hy + 4, TEXT_HEADER, true);
        ctx.text(font, "SR",     pl + col(pw, 2), hy + 4, TEXT_HEADER, true);
        ctx.text(font, "RANK",   pl + col(pw, 3), hy + 4, TEXT_HEADER, true);
        ctx.text(font, "W / L",  pl + col(pw, 4), hy + 4, TEXT_HEADER, true);
        ctx.text(font, "STREAK", pl + col(pw, 5), hy + 4, TEXT_HEADER, true);

        List<FlowTierStats.LadderStats> ladders = sortedLadders(stats);
        if (ladders.isEmpty()) {
            ctx.centeredText(font, Component.literal("No ranked data."), width / 2, tt + 50, TEXT_DIM);
            return;
        }

        int y = tt + rowH() + 4;
        for (int i = 0; i < ladders.size(); i++) {
            FlowTierStats.LadderStats l = ladders.get(i);
            boolean isGlobal = l.ladder().equals("GLOBAL");
            boolean hovered = !isGlobal && mx >= pl + 2 && mx <= pr - 2
                    && my >= y && my < y + rowH();

            if (hovered)         ctx.fill(pl + 2, y, pr - 2, y + rowH(), BG_ROW_HOVER);
            else if (i % 2 == 0) ctx.fill(pl + 2, y, pr - 2, y + rowH(), BG_ROW_ALT);

            if (isGlobal) ctx.fill(pl + 2, y, pl + 4, y + rowH(), ACCENT_BLUE);

            ctx.text(font, FlowTierFormatter.icon(l.ladder()),        pl + col(pw, 0),      y + 4, TEXT_WHITE, true);
            ctx.text(font, FlowTierFormatter.displayName(l.ladder()), pl + col(pw, 0) + 12, y + 4, TEXT_WHITE, true);
            ctx.text(font, l.tierLabel(), pl + col(pw, 1), y + 4, tierColor(l.tierLabel(), l.position()), true);
            ctx.text(font, l.totalRating() + " SR", pl + col(pw, 2), y + 4, eloColor(l.totalRating()), true);
            String rankStr = l.hasPosition() ? "#" + l.position() : "-";
            ctx.text(font, rankStr, pl + col(pw, 3), y + 4, l.hasPosition() ? 0xFFFFD700 : TEXT_DIM, true);
            ctx.text(font, l.wins() + " / " + l.losses(), pl + col(pw, 4), y + 4, wlColor(l.wins(), l.losses()), true);
            ctx.text(font, streakStr(l.currentStreak()), pl + col(pw, 5), y + 4, streakColor(l.currentStreak()), true);
            if (hovered) ctx.text(font, "->", pr - 14, y + 4, ACCENT_BLUE, true);
            y += rowH();
        }

        final int finalY = y;
        stats.bestLadder().ifPresent(best -> {
            int fy = Math.max(tb - 16, finalY + 6);
            ctx.text(font, "Best: " + FlowTierFormatter.displayName(best.ladder()) + "  " + best.tierLabel(),
                    pl + 10, fy, 0xFFFFD700, true);
        });
    }

    private static int col(int pw, int col) {
        return switch (col) {
            case 0 -> 10;
            case 1 -> pw * 32 / 100;
            case 2 -> pw * 48 / 100;
            case 3 -> pw * 62 / 100;
            case 4 -> pw * 74 / 100;
            case 5 -> pw * 89 / 100;
            default -> 10;
        };
    }

    private void renderGraph(GuiGraphicsExtractor ctx, FlowTierStats stats,
                             int pl, int pr, int tt, int tb, int mx, int my) {
        FlowTierStats.LadderStats ladder = stats.ladders().get(selectedLadder);

        ctx.centeredText(font, Component.literal(
                        FlowTierFormatter.displayName(selectedLadder) + "  .  SR History"),
                width / 2, tt + 5, TEXT_HEADER);

        if (ladder != null) {
            String summary = ladder.tierLabel()
                    + "   " + ladder.totalRating() + " SR"
                    + "   " + ladder.wins() + "W / " + ladder.losses() + "L";
            ctx.centeredText(font, Component.literal(summary), width / 2, tt + 17,
                    tierColor(ladder.tierLabel(), ladder.position()));
        }

        int gl = pl + 44, gr = pr - 14;
        int gt = tt + 32, gbt = tb - 18;
        int gw = gr - gl, gh = gbt - gt;

        ctx.fill(gl, gt, gr, gbt, BG_GRAPH);
        ctx.fill(gl - 1, gt, gl, gbt + 1, 0x884C7BA7);
        ctx.fill(gl, gbt, gr, gbt + 1, 0x884C7BA7);

        if (historyLoading) {
            ctx.centeredText(font, Component.literal("Loading history..."), width / 2, gt + gh / 2 - 4, TEXT_DIM);
            return;
        }
        if (historyPoints == null || historyPoints.isEmpty()) {
            ctx.centeredText(font, Component.literal("No history data."), width / 2, gt + gh / 2 - 4, TEXT_DIM);
            return;
        }

        int n = historyPoints.size();
        int minElo = historyPoints.stream().mapToInt(FlowTierLeaderboardClient.HistoryPoint::elo).min().orElse(0);
        int maxElo = historyPoints.stream().mapToInt(FlowTierLeaderboardClient.HistoryPoint::elo).max().orElse(1);
        int pad = Math.max(15, (maxElo - minElo) / 8);
        minElo -= pad; maxElo += pad;
        int eloRange = Math.max(1, maxElo - minElo);

        for (int i = 0; i <= 4; i++) {
            int gridElo = minElo + eloRange * i / 4;
            int gy = gbt - (gridElo - minElo) * gh / eloRange;
            ctx.fill(gl, gy, gr, gy + 1, i == 0 ? 0x448EA7D2 : AXIS_LINE);
            String label = Integer.toString(gridElo);
            ctx.text(font, label, gl - font.width(label) - 3, gy - 4, TEXT_DIM, true);
        }

        if (graphXPositions == null || graphXPositions.length != n) {
            graphXPositions = new int[n];
            graphYPositions = new int[n];
        }
        for (int i = 0; i < n; i++) {
            graphXPositions[i] = gl + (n == 1 ? gw / 2 : i * gw / (n - 1));
            graphYPositions[i] = gbt - (historyPoints.get(i).elo() - minElo) * gh / eloRange;
        }

        // Tier color fill
        int tierRaw = ladder != null
                ? dev.fecl.flowtiers.client.FlowTierRankSystem.tierColor(ladder.tierLabel(), ladder.position())
                : 0x3B82F6;
        int fillColor = (0x22 << 24) | (tierRaw & 0xFFFFFF);

        for (int i = 1; i < n; i++) {
            fillTrapezoid(ctx, graphXPositions[i-1], graphYPositions[i-1],
                    graphXPositions[i], graphYPositions[i], gbt, fillColor);
        }

        // Green/red line segments
        for (int i = 1; i < n; i++) {
            boolean up = historyPoints.get(i).elo() >= historyPoints.get(i-1).elo();
            drawThickLine(ctx, graphXPositions[i-1], graphYPositions[i-1],
                    graphXPositions[i], graphYPositions[i],
                    up ? 0xCC4ADE80 : 0xCCF87171);
        }

        // Dots
        for (int i = 0; i < n; i++) {
            int x = graphXPositions[i], y = graphYPositions[i];
            boolean up = i == 0 || historyPoints.get(i).elo() >= historyPoints.get(i-1).elo();
            ctx.fill(x - 2, y - 2, x + 3, y + 3, 0xFF000000);
            ctx.fill(x - 1, y - 1, x + 2, y + 2, up ? 0xFF4ADE80 : 0xFFF87171);
        }

        // Date labels
        ctx.text(font, dateLabel(historyPoints.get(0).timestamp()), gl, gbt + 4, TEXT_DIM, true);
        String lastDate = dateLabel(historyPoints.get(n - 1).timestamp());
        ctx.text(font, lastDate, gr - font.width(lastDate), gbt + 4, TEXT_DIM, true);

        // Tooltip
        if (graphXPositions != null && my >= gt && my <= gbt) {
            int closest = -1, bestDist = 10;
            for (int i = 0; i < n; i++) {
                int d = Math.max(Math.abs(mx - graphXPositions[i]), Math.abs(my - graphYPositions[i]));
                if (d < bestDist) { bestDist = d; closest = i; }
            }
            if (closest >= 0) renderTooltip(ctx, closest, gl, gr, gt, gbt);
        }
    }

    private void renderTooltip(GuiGraphicsExtractor ctx, int idx, int gl, int gr, int gt, int gbt) {
        int elo   = historyPoints.get(idx).elo();
        int prev  = idx > 0 ? historyPoints.get(idx - 1).elo() : elo;
        int delta = elo - prev;
        String deltaStr  = idx == 0 ? "start" : (delta >= 0 ? "+" + delta : Integer.toString(delta));
        String date      = dateLabel(historyPoints.get(idx).timestamp());
        int deltaColor   = idx == 0 ? TEXT_DIM : (delta >= 0 ? 0xFF4ADE80 : 0xFFF87171);

        ctx.fill(graphXPositions[idx], gt, graphXPositions[idx] + 1, gbt, 0x553B82F6);

        int lines = date.isEmpty() ? 2 : 3;
        int tw = Math.max(68, font.width(date) + 12);
        int th = 8 + lines * 10;
        int tx = Math.min(graphXPositions[idx] + 6, gr - tw - 2);
        int ty = Math.max(gt + 2, graphYPositions[idx] - th - 6);

        ctx.fill(tx - 2, ty - 2, tx + tw + 2, ty + th + 2, 0xF0050810);
        ctx.fill(tx - 2, ty - 2, tx + tw + 2, ty - 1, ACCENT_BLUE);
        ctx.fill(tx - 2, ty - 2, tx - 1, ty + th + 2, ACCENT_DIM);

        ctx.text(font, elo + " SR", tx + 2, ty + 2, eloColor(elo), true);
        ctx.text(font, deltaStr, tx + 2, ty + 12, deltaColor, true);
        if (!date.isEmpty()) ctx.text(font, date, tx + 2, ty + 22, TEXT_HEADER, true);

        ctx.fill(graphXPositions[idx] - 3, graphYPositions[idx] - 3,
                graphXPositions[idx] + 4, graphYPositions[idx] + 4, 0xFFFFFFFF);
        ctx.fill(graphXPositions[idx] - 2, graphYPositions[idx] - 2,
                graphXPositions[idx] + 3, graphYPositions[idx] + 3, ACCENT_BLUE);
    }

    private static void drawThickLine(GuiGraphicsExtractor ctx, int x1, int y1, int x2, int y2, int color) {
        int steps = Math.max(Math.abs(x2 - x1), Math.abs(y2 - y1));
        if (steps == 0) { ctx.fill(x1 - 1, y1 - 1, x1 + 2, y1 + 2, color); return; }
        for (int i = 0; i <= steps; i++) {
            int x = x1 + (x2 - x1) * i / steps;
            int y = y1 + (y2 - y1) * i / steps;
            ctx.fill(x - 1, y - 1, x + 2, y + 2, color);
        }
    }

    private static void fillTrapezoid(GuiGraphicsExtractor ctx, int x1, int y1, int x2, int y2, int baseline, int color) {
        if (x2 <= x1) return;
        for (int x = x1; x < x2; x++) {
            int lineY = y1 + (y2 - y1) * (x - x1) / Math.max(1, x2 - x1);
            if (lineY < baseline) ctx.fill(x, lineY, x + 1, baseline, color);
        }
    }

    private static List<FlowTierStats.LadderStats> sortedLadders(FlowTierStats stats) {
        return stats.ladders().values().stream()
                .filter(l -> l.wins() > 0 || l.losses() > 0)
                .sorted(Comparator
                        .comparingInt((FlowTierStats.LadderStats l) -> l.ladder().equals("GLOBAL") ? 0 : 1)
                        .thenComparing(Comparator.comparingInt(FlowTierStats.LadderStats::totalRating).reversed()))
                .toList();
    }

    private static String streakStr(int s) {
        if (s > 0) return "+" + s;
        if (s < 0) return Integer.toString(s);
        return "-";
    }

    private static int wlColor(int w, int l) {
        int t = w + l;
        if (t == 0) return TEXT_DIM;
        double r = (double) w / t;
        if (r >= 0.55) return 0xFF4ADE80;
        if (r >= 0.45) return 0xFFB0B8CC;
        return 0xFFF87171;
    }

    private static int streakColor(int s) {
        if (s > 2)  return 0xFF4ADE80;
        if (s > 0)  return 0xFF86EFAC;
        if (s < -2) return 0xFFF87171;
        if (s < 0)  return 0xFFFCA5A5;
        return TEXT_DIM;
    }

    private static int tierColor(String tier, int pos) {
        int c = dev.fecl.flowtiers.client.FlowTierRankSystem.tierColor(tier, pos);
        return c == 0xFFFFFF ? 0xFFB0B8CC : 0xFF000000 | c;
    }

    private static int eloColor(int elo) {
        return 0xFF000000 | dev.fecl.flowtiers.client.FlowTierRankSystem.ratingColor(elo);
    }

    private static String dateLabel(long ts) {
        if (ts <= 0) return "";
        long ms = ts < 10_000_000_000L ? ts * 1000L : ts;
        return GRAPH_DATE.format(Instant.ofEpochMilli(ms).atZone(ZoneId.systemDefault()));
    }

    private void copyProfileLink() {
        if (minecraft != null) minecraft.keyboardHandler.setClipboard("https://flowpvp.gg/user/" + fallbackName);
    }

    private void copyStats() {
        if (minecraft == null) return;
        minecraft.keyboardHandler.setClipboard(FlowTierStatsClipboard.format(fallbackName, displayedStats().orElse(null)));
    }

    private java.util.Optional<FlowTierStats> displayedStats() {
        List<FlowTierSeasonArchive.Season> seasons = FlowTierSeasonArchive.seasonsFor(uuid);
        if (selectedSeason >= 0 && selectedSeason < seasons.size()) {
            return FlowTierSeasonArchive.stats(uuid, seasons.get(selectedSeason));
        }
        return FlowTiersClientState.cache().getIfFresh(uuid);
    }

    private String selectedSeasonName() {
        List<FlowTierSeasonArchive.Season> seasons = FlowTierSeasonArchive.seasonsFor(uuid);
        if (selectedSeason >= 0 && selectedSeason < seasons.size()) return seasons.get(selectedSeason).name();
        return FlowTierSeasonArchive.currentSeasonName();
    }

    private String seasonButtonLabel(List<FlowTierSeasonArchive.Season> seasons) {
        if (selectedSeason >= 0 && selectedSeason < seasons.size()) return "Season: " + seasons.get(selectedSeason).name();
        return "Season: Current";
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
