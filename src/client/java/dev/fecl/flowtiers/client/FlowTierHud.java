package dev.fecl.flowtiers.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public final class FlowTierHud {
	private static final int PADDING = 3;
	private static final int LINE_HEIGHT = 10;
	private static final int BACKGROUND = 0x88000000;
	private static final int FLOW_BLUE = 0xFF00BFFF;
	private static final int WHITE = 0xFFFFFFFF;
	private static final int GRAY = 0xFFAAAAAA;

	private FlowTierHud() {}

	public static void register(FlowTierCache cache) {
		HudRenderCallback.EVENT.register((context, tickCounter) -> {
			if (!FlowTierClientConfig.hudEnabled) return;
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null || client.options.hudHidden) return;
			cache.fetch(client.player.getUuid());
			FlowTierStats stats = cache.getIfFresh(client.player.getUuid()).orElse(null);
			draw(context, client, stats);
		});
	}

	private static void draw(DrawContext context, MinecraftClient client, FlowTierStats stats) {
		TextRenderer textRenderer = client.textRenderer;
		Text header = Text.literal("FlowTiers");
		Text icon = null;
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
					if (FlowTierClientConfig.hudStreakEnabled) {
						recordLine += "  " + streak(ladder.currentStreak());
					}
				} else if (FlowTierClientConfig.hudStreakEnabled) {
					recordLine = streak(ladder.currentStreak());
				}
			}
		}

		int width = textRenderer.getWidth(header);
		if (icon != null) width += textRenderer.getWidth("  ") + textRenderer.getWidth(icon);
		width = Math.max(width, textRenderer.getWidth(tierLine));
		if (positionLine != null) width = Math.max(width, textRenderer.getWidth(positionLine));
		if (recordLine != null) width = Math.max(width, textRenderer.getWidth(recordLine));

		int lines = 2 + (positionLine != null ? 1 : 0) + (recordLine != null ? 1 : 0);
		int widgetWidth = width + PADDING * 2;
		int widgetHeight = lines * LINE_HEIGHT + PADDING * 2;
		float scale = FlowTierClientConfig.hudScale;
		int x = clamp(FlowTierClientConfig.hudX, 0, client.getWindow().getScaledWidth() - Math.round(widgetWidth * scale));
		int y = clamp(FlowTierClientConfig.hudY, 0, client.getWindow().getScaledHeight() - Math.round(widgetHeight * scale));
		FlowTierHudScaleTransform.push(context, x, y, scale);

		if (FlowTierClientConfig.hudBackground) {
			context.fill(0, 0, widgetWidth, widgetHeight, BACKGROUND);
		}

		int tx = PADDING;
		int ty = PADDING;
		context.drawTextWithShadow(textRenderer, header, tx, ty, FLOW_BLUE);
		if (icon != null) {
			context.drawTextWithShadow(textRenderer, icon, tx + textRenderer.getWidth(header) + textRenderer.getWidth("  "), ty, WHITE);
		}
		ty += LINE_HEIGHT;

		context.drawTextWithShadow(textRenderer, tierLine, tx, ty, tierColor);
		ty += LINE_HEIGHT;

		if (positionLine != null) {
			int posColor = FlowTierClientConfig.coloredPosition ? positionColor(positionTier, positionRank) : WHITE;
			context.drawTextWithShadow(textRenderer, positionLine, tx, ty, posColor);
			ty += LINE_HEIGHT;
		}
		if (recordLine != null) {
			context.drawTextWithShadow(textRenderer, recordLine, tx, ty, GRAY);
		}
		FlowTierHudScaleTransform.pop(context);
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
