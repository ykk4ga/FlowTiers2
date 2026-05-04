package dev.decl.flowtiers.client;

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
	private static final int GOLD = 0xFFFFD700;

	private FlowTierHud() {
	}

	public static void register(FlowTierCache cache) {
		HudRenderCallback.EVENT.register((context, tickCounter) -> {
			if (!FlowTierClientConfig.hudEnabled) {
				return;
			}

			MinecraftClient client = MinecraftClient.getInstance();
			if (client.player == null || client.options.hudHidden) {
				return;
			}

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
		Integer tierColor = GRAY;
		String positionLine = null;
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
		if (icon != null) {
			width += textRenderer.getWidth("  ") + textRenderer.getWidth(icon);
		}
		width = Math.max(width, textRenderer.getWidth(tierLine));
		if (positionLine != null) {
			width = Math.max(width, textRenderer.getWidth(positionLine));
		}
		if (recordLine != null) {
			width = Math.max(width, textRenderer.getWidth(recordLine));
		}

		int lines = 2 + (positionLine != null ? 1 : 0) + (recordLine != null ? 1 : 0);
		int widgetWidth = width + PADDING * 2;
		int widgetHeight = lines * LINE_HEIGHT + PADDING * 2;
		int x = clamp(FlowTierClientConfig.hudX, 0, client.getWindow().getScaledWidth() - widgetWidth);
		int y = clamp(FlowTierClientConfig.hudY, 0, client.getWindow().getScaledHeight() - widgetHeight);

		if (FlowTierClientConfig.hudBackground) {
			context.fill(x, y, x + widgetWidth, y + widgetHeight, BACKGROUND);
		}

		int tx = x + PADDING;
		int ty = y + PADDING;
		context.drawTextWithShadow(textRenderer, header, tx, ty, FLOW_BLUE);
		if (icon != null) {
			context.drawTextWithShadow(textRenderer, icon, tx + textRenderer.getWidth(header) + textRenderer.getWidth("  "), ty, WHITE);
		}
		ty += LINE_HEIGHT;

		context.drawTextWithShadow(textRenderer, tierLine, tx, ty, tierColor);
		ty += LINE_HEIGHT;

		if (positionLine != null) {
			context.drawTextWithShadow(textRenderer, positionLine, tx, ty, GOLD);
			ty += LINE_HEIGHT;
		}
		if (recordLine != null) {
			context.drawTextWithShadow(textRenderer, recordLine, tx, ty, GRAY);
		}
	}

	private static String tierLine(FlowTierStats.LadderStats ladder) {
		StringBuilder line = new StringBuilder();
		if (FlowTierClientConfig.tierEnabled) {
			line.append(ladder.tierLabel());
		}
		if (FlowTierClientConfig.eloEnabled) {
			if (!line.isEmpty()) {
				line.append("  ");
			}
			line.append(ladder.totalRating());
			if (FlowTierClientConfig.eloLabelEnabled) {
				line.append(" ELO");
			}
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
		if (position == 1 || tier.equals("Grandmaster")) return 0xFFFF55FF;
		if (tier.startsWith("Netherite")) return 0xFF8B5CF6;
		if (tier.startsWith("Diamond")) return 0xFF55FFFF;
		if (tier.startsWith("Emerald")) return 0xFF50C878;
		if (tier.startsWith("Gold")) return 0xFFFFD700;
		if (tier.startsWith("Iron")) return 0xFFC0C0C0;
		if (tier.startsWith("Copper")) return 0xFFCD7F32;
		return GRAY;
	}
}
