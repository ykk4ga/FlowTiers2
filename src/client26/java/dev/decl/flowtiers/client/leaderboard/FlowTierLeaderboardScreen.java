package dev.decl.flowtiers.client.leaderboard;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import dev.decl.flowtiers.client.FlowTierClientConfig;
import dev.decl.flowtiers.client.FlowTierFormatter;
import dev.decl.flowtiers.client.FlowTierStats;
import dev.decl.flowtiers.client.FlowTiersClientState;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public final class FlowTierLeaderboardScreen extends Screen {
    private static final String[] LADDERS = {
            "GLOBAL", "SWORD", "AXE", "UHC", "VANILLA",
            "MACE", "DIAMOND_POT", "NETHERITE_OP", "SMP", "DIAMOND_SMP"
    };

    private final FlowTierLeaderboardClient leaderboardClient;
    private String ladder = initialLadder();
    private int scrollOffset;
    private EditBox searchField;
    private String searchQuery = "";
    private String searchStatus = "";
    private FlowTierLeaderboardClient.Entry resolvedSearchEntry;
    private String pendingResolveName = "";

    public FlowTierLeaderboardScreen(FlowTierLeaderboardClient leaderboardClient) {
        super(Component.literal("FlowTiers Leaderboard"));
        this.leaderboardClient = leaderboardClient;
    }

    @Override
    protected void init() {
        clearWidgets();
        int tabWidth = 68;
        int tabHeight = 18;
        int gap = 6;
        int panelLeft = panelLeft();
        int startX = panelLeft + 8;

        for (int i = 0; i < LADDERS.length; i++) {
            String tabLadder = LADDERS[i];
            int row = i / 5;
            int col = i % 5;
            int x = startX + col * (tabWidth + gap);
            int y = 22 + row * (tabHeight + 4);
            Button tab = Button.builder(Component.literal((tabLadder.equals(ladder) ? "> " : "") + tabButtonLabel(tabLadder)), button -> {
                ladder = tabLadder;
                scrollOffset = 0;
                leaderboardClient.load(ladder);
                init();
            }).bounds(x, y, tabWidth, tabHeight).build();
            tab.active = !tabLadder.equals(ladder);
            addRenderableWidget(tab);
        }

        searchField = new EditBox(font, panelLeft + 8, 72, 220, 18, Component.literal("Search player"));
        searchField.setMaxLength(32);
        searchField.setHint(Component.literal("Search player..."));
        searchField.setValue(searchQuery);
        searchField.setResponder(value -> {
            searchQuery = value.trim();
            searchStatus = "";
            resolvedSearchEntry = null;
            resolveSearchIfNeeded(searchQuery);
        });
        addRenderableWidget(searchField);
        addRenderableWidget(Button.builder(Component.literal("Search"), button -> searchPlayer())
                .bounds(panelLeft + 234, 72, 68, 18)
                .build());
        leaderboardClient.load(ladder);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xE0101420);
        super.extractRenderState(context, mouseX, mouseY, delta);

        FlowTierLeaderboardClient.PageState state = leaderboardClient.state(ladder);
        List<FlowTierLeaderboardClient.Entry> entries = state.entries();
        List<FlowTierLeaderboardClient.Entry> visibleEntries = visibleEntries(entries);
        int panelLeft = panelLeft();
        int panelRight = panelRight();
        int top = tableTop();
        int bottom = height - 28;
        int rowHeight = 16;

        context.fill(panelLeft, top - 18, panelRight, bottom, 0xAA080B12);
        context.fill(panelLeft, top - 18, panelRight, top - 2, 0xCC111827);
        context.text(font, "#", panelLeft + 10, top - 14, 0xFFB5C7E8, true);
        context.text(font, "Player", panelLeft + 46, top - 14, 0xFFB5C7E8, true);
        context.text(font, "Tier", panelRight - 132, top - 14, 0xFFB5C7E8, true);
        context.text(font, "ELO", panelRight - 54, top - 14, 0xFFB5C7E8, true);

        if (resolvedSearchEntry != null) {
            context.text(font, "Found: " + resolvedSearchEntry.name(), panelLeft + 310, 77, 0xFF55FF55, true);
        } else if (searchStatus != null && !searchStatus.isBlank()) {
            context.text(font, searchStatus, panelLeft + 310, 77, 0xFF7C8BA1, true);
        }

        if (visibleEntries.isEmpty()) {
            String message = state.error() != null ? state.error() : state.loading() ? "Loading..." : "No leaderboard data.";
            if (!entries.isEmpty() && !searchText().isBlank()) {
                message = resolvedSearchEntry == null ? "No loaded rows match. Resolving player..." : "Press Search to open found player.";
            }
            context.centeredText(font, Component.literal(message), width / 2, top + 28, 0xFFAAAAAA);
            return;
        }

        int maxScroll = Math.max(0, visibleEntries.size() * rowHeight - (bottom - top));
        scrollOffset = Math.min(scrollOffset, maxScroll);

        context.enableScissor(panelLeft, top, panelRight, bottom);
        for (int i = 0; i < visibleEntries.size(); i++) {
            FlowTierLeaderboardClient.Entry entry = visibleEntries.get(i);
            int y = top + i * rowHeight - scrollOffset;
            if (y + rowHeight < top || y > bottom) continue;

            boolean hovered = mouseX >= panelLeft && mouseX <= panelRight && mouseY >= y && mouseY < y + rowHeight;
            if (hovered) {
                context.fill(panelLeft + 2, y - 1, panelRight - 2, y + rowHeight - 1, 0x553B82F6);
            } else if (i % 2 == 0) {
                context.fill(panelLeft + 2, y - 1, panelRight - 2, y + rowHeight - 1, 0x22000000);
            }

            String tier = tierFor(entry);
            context.text(font, entry.position() > 0 ? Integer.toString(entry.position()) : "-", panelLeft + 10, y + 3, rankColor(entry.position()), true);
            context.text(font, trim(entry.name(), 18), panelLeft + 46, y + 3, nameColor(entry.position()), true);
            context.text(font, trim(tier, 12), panelRight - 132, y + 3, tierColor(tier, entry.position()), true);
            context.text(font, entry.elo() + " ELO", panelRight - 54, y + 3, eloColor(entry.elo()), true);
        }
        context.disableScissor();

        if (state.loading()) {
            context.centeredText(font, Component.literal("Loading more..."), width / 2, height - 18, 0xFF7C8BA1);
        } else {
            context.text(font, entries.size() + " players | page " + Math.max(1, state.page()), panelLeft, height - 18, 0xFF7C8BA1, true);
            context.text(font, "Scroll down to load more. Esc closes.", panelRight - 208, height - 18, 0xFF7C8BA1, true);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (searchField != null && searchField.isFocused()) return super.keyPressed(event);
        int index = switch (event.key()) {
            case org.lwjgl.glfw.GLFW.GLFW_KEY_1 -> 0;
            case org.lwjgl.glfw.GLFW.GLFW_KEY_2 -> 1;
            case org.lwjgl.glfw.GLFW.GLFW_KEY_3 -> 2;
            case org.lwjgl.glfw.GLFW.GLFW_KEY_4 -> 3;
            case org.lwjgl.glfw.GLFW.GLFW_KEY_5 -> 4;
            case org.lwjgl.glfw.GLFW.GLFW_KEY_6 -> 5;
            case org.lwjgl.glfw.GLFW.GLFW_KEY_7 -> 6;
            case org.lwjgl.glfw.GLFW.GLFW_KEY_8 -> 7;
            case org.lwjgl.glfw.GLFW.GLFW_KEY_9 -> 8;
            case org.lwjgl.glfw.GLFW.GLFW_KEY_0 -> 9;
            default -> -1;
        };
        if (index >= 0 && index < LADDERS.length) {
            ladder = LADDERS[index];
            scrollOffset = 0;
            leaderboardClient.load(ladder);
            init();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        scrollOffset -= (int) (verticalAmount * 18);
        scrollOffset = Math.max(0, scrollOffset);
        FlowTierLeaderboardClient.PageState state = leaderboardClient.state(ladder);
        int visibleRows = Math.max(1, (height - 110) / 16);
        if (searchText().isBlank() && scrollOffset > Math.max(0, state.entries().size() - visibleRows - 4) * 16) {
            leaderboardClient.loadMore(ladder);
        }
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean focused) {
        if (event.button() == 0) {
            FlowTierLeaderboardClient.Entry entry = rowAt(event.x(), event.y());
            if (entry != null && minecraft != null) {
                String autoLadder = ladder.equals("GLOBAL") ? null : ladder;
                minecraft.setScreen(new FlowTierPlayerStatsScreen(this, entry.uuid(), entry.name(), autoLadder));
                return true;
            }
        }
        return super.mouseClicked(event, focused);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static String initialLadder() {
        return "GLOBAL";
    }

    private FlowTierLeaderboardClient.Entry rowAt(double mouseX, double mouseY) {
        int panelLeft = panelLeft();
        int panelRight = panelRight();
        int top = tableTop();
        int bottom = height - 28;
        int rowHeight = 16;
        if (mouseX < panelLeft || mouseX > panelRight || mouseY < top || mouseY > bottom) return null;
        int index = ((int) mouseY - top + scrollOffset) / rowHeight;
        List<FlowTierLeaderboardClient.Entry> entries = visibleEntries(leaderboardClient.state(ladder).entries());
        return index >= 0 && index < entries.size() ? entries.get(index) : null;
    }

    private int panelLeft() {
        return Math.max(20, width / 2 - 190);
    }

    private int panelRight() {
        return Math.min(width - 20, width / 2 + 190);
    }

    private int tableTop() {
        return 124;
    }

    private List<FlowTierLeaderboardClient.Entry> visibleEntries(List<FlowTierLeaderboardClient.Entry> entries) {
        String query = searchText();
        if (query.isBlank()) return entries;
        String lowerQuery = query.toLowerCase(Locale.ROOT);
        List<FlowTierLeaderboardClient.Entry> filtered = entries.stream()
                .filter(entry -> entry.name().toLowerCase(Locale.ROOT).contains(lowerQuery))
                .toList();
        if (!filtered.isEmpty() || resolvedSearchEntry == null) return filtered;
        return List.of(resolvedSearchEntry);
    }

    private String searchText() {
        return searchField == null ? searchQuery : searchField.getValue().trim();
    }

    private void searchPlayer() {
        String query = searchText();
        if (query.isBlank() || minecraft == null) return;
        String autoLadder = ladder.equals("GLOBAL") ? null : ladder;

        if (resolvedSearchEntry != null && resolvedSearchEntry.name().equalsIgnoreCase(query)) {
            minecraft.setScreen(new FlowTierPlayerStatsScreen(this, resolvedSearchEntry.uuid(), resolvedSearchEntry.name(), autoLadder));
            return;
        }

        for (FlowTierLeaderboardClient.Entry entry : leaderboardClient.state(ladder).entries()) {
            if (entry.name().equalsIgnoreCase(query)) {
                minecraft.setScreen(new FlowTierPlayerStatsScreen(this, entry.uuid(), entry.name(), autoLadder));
                return;
            }
        }

        searchStatus = "Searching...";
        try {
            UUID uuid = parseUuid(query);
            minecraft.setScreen(new FlowTierPlayerStatsScreen(this, uuid.toString(), query, autoLadder));
            return;
        } catch (IllegalArgumentException ignored) {
        }

        FlowTiersClientState.profileResolver().resolve(query).thenAccept(result -> {
            if (minecraft == null) return;
            minecraft.execute(() -> {
                if (result.status() == dev.decl.flowtiers.client.MojangProfileResolver.Status.FOUND) {
                    minecraft.setScreen(new FlowTierPlayerStatsScreen(this, result.profile().uuid().toString(), result.profile().name(), autoLadder));
                } else if (result.status() == dev.decl.flowtiers.client.MojangProfileResolver.Status.NOT_FOUND) {
                    searchStatus = "Player not found.";
                } else {
                    searchStatus = "Search failed.";
                }
            });
        });
    }

    private void resolveSearchIfNeeded(String query) {
        if (query.length() < 3 || query.equalsIgnoreCase(pendingResolveName)) return;

        for (FlowTierLeaderboardClient.Entry entry : leaderboardClient.state(ladder).entries()) {
            if (entry.name().equalsIgnoreCase(query)) {
                resolvedSearchEntry = entry;
                searchStatus = "";
                return;
            }
        }

        pendingResolveName = query;
        searchStatus = "Resolving...";
        FlowTiersClientState.profileResolver().resolve(query).thenAccept(result -> {
            if (minecraft == null) return;
            minecraft.execute(() -> {
                if (!query.equals(searchText())) return;
                if (result.status() == dev.decl.flowtiers.client.MojangProfileResolver.Status.FOUND) {
                    searchStatus = "Fetching stats...";
                    FlowTiersClientState.cache().fetch(result.profile().uuid()).thenAccept(stats -> {
                        if (minecraft == null) return;
                        minecraft.execute(() -> {
                            if (!query.equals(searchText())) return;
                            if (stats == null) {
                                resolvedSearchEntry = null;
                                searchStatus = "Player has not played FlowPvP ranked.";
                                return;
                            }
                            FlowTierStats.LadderStats ladderStats = stats.ladder(ladder)
                                    .or(() -> stats.displayLadder())
                                    .orElse(null);
                            int elo = ladderStats == null ? 0 : ladderStats.totalRating();
                            int position = ladderStats == null ? 0 : ladderStats.position();
                            resolvedSearchEntry = new FlowTierLeaderboardClient.Entry(position, result.profile().uuid().toString(), result.profile().name(), elo);
                            searchStatus = "";
                        });
                    });
                } else if (result.status() == dev.decl.flowtiers.client.MojangProfileResolver.Status.NOT_FOUND) {
                    searchStatus = "Player not found.";
                } else {
                    searchStatus = "Search failed.";
                }
            });
        });
    }

    private static UUID parseUuid(String value) {
        if (value.length() != 32) return UUID.fromString(value);
        return UUID.fromString(value.replaceFirst(
                "([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{12})",
                "$1-$2-$3-$4-$5"
        ));
    }

    private static String tabButtonLabel(String ladder) {
        return switch (ladder) {
            case "GLOBAL" -> "Global";
            case "DIAMOND_POT" -> "Pot";
            case "NETHERITE_OP" -> "NethOP";
            case "DIAMOND_SMP" -> "D.SMP";
            default -> FlowTierFormatter.displayName(ladder);
        };
    }

    private static String tierFor(FlowTierLeaderboardClient.Entry entry) {
        return new FlowTierStats.LadderStats("LEADERBOARD", entry.elo(), 1, 0, 0, 1, null, entry.position()).tierLabel();
    }

    private static String trim(String value, int max) {
        return value.length() <= max ? value : value.substring(0, max - 1) + "...";
    }

    private static int rankColor(int rank) {
        if (rank == 1) return 0xFFFFD700;
        if (rank == 2) return 0xFFC0C0C0;
        if (rank == 3) return 0xFFCD7F32;
        if (rank <= 10) return 0xFFFFFF88;
        return 0xFF9CA3AF;
    }

    private static int nameColor(int rank) {
        if (rank <= 3) return rankColor(rank);
        return 0xFFFFFFFF;
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