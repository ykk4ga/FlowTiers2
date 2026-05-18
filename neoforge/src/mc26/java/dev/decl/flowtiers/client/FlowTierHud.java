package dev.decl.flowtiers.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public final class FlowTierHud {
	private static final int PADDING = 3;
	private static final int LINE_HEIGHT = 10;
	private static final int BACKGROUND = 0x88000000;
	private static final int FLOW_BLUE = 0xFF00BFFF;
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GRAY = 0xFFAAAAAA;

	private FlowTierHud() {
	}

	public static void render(RenderGuiEvent.Post event, FlowTierCache cache) {
		if (!FlowTierClientConfig.hudEnabled) return;
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.options.hideGui) return;

		cache.fetch(client.player.getUUID());
		FlowTierStats stats = cache.getIfFresh(client.player.getUUID()).orElse(null);
		draw(event.getGuiGraphics(), client, stats);
	}

	private static void draw(GuiGraphicsExtractor graphics, Minecraft client, FlowTierStats stats) {
		Font font = client.font;
		Component header = Component.literal("FlowTiers");
		Component icon = null;
		String tierLine = "Loading...";
		int tierColor = GRAY;
		String positionLine = null;
		int positionRank = 0;
		String positionTier = "";
		String recordLine = null;

		if (stats != null) {
			FlowTierStats.LadderStats ladder = stats.displayLadder().orElse(null);
			if (ladder == null) {
				tierLine = "No ranked stats";
			} else {
				icon = FlowTierFormatter.icon(ladder.ladder());
				tierLine = tierLine(ladder);
				tierColor = tierColor(ladder.tierLabel(), ladder.position());
				if (FlowTierClientConfig.positionEnabled && ladder.hasPosition()) {
					positionLine = "#" + ladder.position() + " " + FlowTierFormatter.displayName(ladder.ladder());
					positionRank = ladder.position();
					positionTier = ladder.tierLabel();
				}
				if (FlowTierClientConfig.hudRecordEnabled) {
					recordLine = ladder.wins() + "W " + ladder.losses() + "L";
					if (FlowTierClientConfig.hudStreakEnabled) recordLine += "  " + streak(ladder.currentStreak());
				} else if (FlowTierClientConfig.hudStreakEnabled) {
					recordLine = streak(ladder.currentStreak());
				}
			}
		}

		int width = font.width(header);
		if (icon != null) width += font.width("  ") + font.width(icon);
		width = Math.max(width, font.width(tierLine));
		if (positionLine != null) width = Math.max(width, font.width(positionLine));
		if (recordLine != null) width = Math.max(width, font.width(recordLine));

		int lines = 2 + (positionLine != null ? 1 : 0) + (recordLine != null ? 1 : 0);
		int widgetWidth = width + PADDING * 2;
		int widgetHeight = lines * LINE_HEIGHT + PADDING * 2;
		int x = clamp(FlowTierClientConfig.hudX, 0, client.getWindow().getGuiScaledWidth() - widgetWidth);
		int y = clamp(FlowTierClientConfig.hudY, 0, client.getWindow().getGuiScaledHeight() - widgetHeight);

		if (FlowTierClientConfig.hudBackground) {
			graphics.fill(x, y, x + widgetWidth, y + widgetHeight, BACKGROUND);
		}

		int tx = x + PADDING;
		int ty = y + PADDING;
		graphics.text(font, header, tx, ty, FLOW_BLUE, true);
		if (icon != null) {
			graphics.text(font, icon, tx + font.width(header) + font.width("  "), ty, WHITE, true);
		}
		ty += LINE_HEIGHT;

		graphics.text(font, tierLine, tx, ty, tierColor, true);
		ty += LINE_HEIGHT;

		if (positionLine != null) {
			int posColor = FlowTierClientConfig.coloredPosition ? positionColor(positionTier, positionRank) : WHITE;
			graphics.text(font, positionLine, tx, ty, posColor, true);
			ty += LINE_HEIGHT;
		}
		if (recordLine != null) {
			graphics.text(font, recordLine, tx, ty, GRAY, true);
		}
	}

	private static String tierLine(FlowTierStats.LadderStats ladder) {
		StringBuilder line = new StringBuilder();
		if (FlowTierClientConfig.tierEnabled) line.append(ladder.tierLabel());
		if (FlowTierClientConfig.eloEnabled) {
			if (!line.isEmpty()) line.append("  ");
			line.append(ladder.totalRating());
			if (FlowTierClientConfig.eloLabelEnabled) line.append(" ").append(FlowTierRankSystem.RATING_LABEL);
		}
		return line.isEmpty() ? FlowTierFormatter.displayName(ladder.ladder()) : line.toString();
	}

	private static int clamp(int value, int min, int max) {
		return Math.max(min, Math.min(value, Math.max(min, max)));
	}

	private static String streak(int streak) {
		if (streak > 0) return "+" + streak + " streak";
		if (streak < 0) return streak + " streak";
		return "0 streak";
	}

	private static int tierColor(String tier, int position) {
		int color = FlowTierRankSystem.tierColor(tier, position);
		return color == 0xFFFFFF ? GRAY : 0xFF000000 | color;
	}

	private static int positionColor(String tier, int position) {
		int color = FlowTierRankSystem.tierColor(tier, position);
		return color == 0xFFFFFF ? GRAY : 0xFF000000 | color;
	}
}
