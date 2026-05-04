package dev.decl.flowtiers.client.leaderboard;

import java.util.List;

import dev.decl.flowtiers.client.FlowTierFormatter;
import dev.decl.flowtiers.client.FlowTierStats;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FlowTierLeaderboardScreen extends Screen {
	private static final String[] LADDERS = {
			"GLOBAL", "SWORD", "AXE", "UHC", "VANILLA",
			"MACE", "DIAMOND_POT", "NETHERITE_OP", "SMP", "DIAMOND_SMP"
	};

	private final FlowTierLeaderboardClient leaderboardClient;
	private String ladder = "GLOBAL";
	private int scrollOffset;

	public FlowTierLeaderboardScreen(FlowTierLeaderboardClient leaderboardClient) {
		super(Minecraft.getInstance(), Minecraft.getInstance().font, Component.literal("FlowTiers Leaderboard"));
		this.leaderboardClient = leaderboardClient;
	}

	@Override
	protected void init() {
		leaderboardClient.load(ladder);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
		context.fill(0, 0, width, height, 0xE0101420);
		super.extractRenderState(context, mouseX, mouseY, delta);

		FlowTierLeaderboardClient.PageState state = leaderboardClient.state(ladder);
		List<FlowTierLeaderboardClient.Entry> entries = state.entries();
		int panelLeft = Math.max(20, width / 2 - 190);
		int panelRight = Math.min(width - 20, width / 2 + 190);
		int top = 58;
		int bottom = height - 28;
		int rowHeight = 16;

		context.centeredText(font, Component.literal("FlowPvP Leaderboard - " + FlowTierFormatter.displayName(ladder)), width / 2, 16, 0xFFFFFFFF);
		context.centeredText(font, Component.literal("Press 1-9/0 to switch ladders. Scroll to load more. Esc closes."), width / 2, 30, 0xFF9CA3AF);
		context.fill(panelLeft, top - 18, panelRight, bottom, 0xAA080B12);
		context.fill(panelLeft, top - 18, panelRight, top - 2, 0xCC111827);
		context.text(font, "#", panelLeft + 10, top - 14, 0xFFB5C7E8, true);
		context.text(font, "Player", panelLeft + 46, top - 14, 0xFFB5C7E8, true);
		context.text(font, "Tier", panelRight - 132, top - 14, 0xFFB5C7E8, true);
		context.text(font, "ELO", panelRight - 54, top - 14, 0xFFB5C7E8, true);

		if (entries.isEmpty()) {
			String message = state.error() != null ? state.error() : state.loading() ? "Loading..." : "No leaderboard data.";
			context.centeredText(font, Component.literal(message), width / 2, top + 28, 0xFFAAAAAA);
			return;
		}

		int maxScroll = Math.max(0, entries.size() * rowHeight - (bottom - top));
		scrollOffset = Math.min(scrollOffset, maxScroll);

		context.enableScissor(panelLeft, top, panelRight, bottom);
		for (int i = 0; i < entries.size(); i++) {
			FlowTierLeaderboardClient.Entry entry = entries.get(i);
			int y = top + i * rowHeight - scrollOffset;
			if (y + rowHeight < top || y > bottom) {
				continue;
			}

			if (i % 2 == 0) {
				context.fill(panelLeft + 2, y - 1, panelRight - 2, y + rowHeight - 1, 0x22000000);
			}

			String tier = tierFor(entry);
			context.text(font, Integer.toString(entry.position()), panelLeft + 10, y + 3, rankColor(entry.position()), true);
			context.text(font, trim(entry.name(), 18), panelLeft + 46, y + 3, nameColor(entry.position()), true);
			context.text(font, trim(tier, 12), panelRight - 132, y + 3, tierColor(tier, entry.position()), true);
			context.text(font, entry.elo() + " ELO", panelRight - 54, y + 3, eloColor(entry.elo()), true);
		}
		context.disableScissor();

		if (state.loading()) {
			context.centeredText(font, Component.literal("Loading more..."), width / 2, height - 18, 0xFF7C8BA1);
		} else {
			context.text(font, entries.size() + " players | page " + Math.max(1, state.page()), panelLeft, height - 18, 0xFF7C8BA1, true);
		}
	}

	@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
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
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		scrollOffset -= (int) (verticalAmount * 18);
		scrollOffset = Math.max(0, scrollOffset);
		FlowTierLeaderboardClient.PageState state = leaderboardClient.state(ladder);
		int visibleRows = Math.max(1, (height - 86) / 16);
		if (scrollOffset > Math.max(0, state.entries().size() - visibleRows - 4) * 16) {
			leaderboardClient.loadMore(ladder);
		}
		return true;
	}

	@Override
	public boolean isPauseScreen() {
		return false;
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
