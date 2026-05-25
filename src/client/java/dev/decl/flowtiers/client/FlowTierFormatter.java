package dev.decl.flowtiers.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Formatting;

public final class FlowTierFormatter {
	private static final int SEPARATOR_COLOR = 0xABABAB;

	private FlowTierFormatter() {
	}

	public static Text compact(FlowTierStats stats) {
		FlowTierStats.LadderStats ladder = stats.displayLadder().orElse(null);

		if (ladder == null || !ladder.hasPlayedRanked()) {
			return Text.literal("Unranked").formatted(Formatting.GRAY);
		}

		return decorated(ladder);
	}

	public static Text previewCompact() {
		net.minecraft.client.MinecraftClient client = net.minecraft.client.MinecraftClient.getInstance();
		if (client != null && client.player != null) {
			dev.decl.flowtiers.client.FlowTierStats real =
					dev.decl.flowtiers.client.FlowTiersClientState.cache()
							.getIfFresh(client.player.getUuid()).orElse(null);
			if (real != null) return compact(real);
		}
		FlowTierStats.LadderStats fake = new FlowTierStats.LadderStats(
				FlowTierClientConfig.preferredLadder,
				800, 10, 5, 2, 10, "MT4", 123
		);
		return decorated(fake);
	}

	public static Text hud(FlowTierStats stats) {
		FlowTierStats.LadderStats ladder = stats.displayLadder().orElse(null);

		if (ladder == null) {
			return Text.literal("FlowPvP: Unranked").formatted(Formatting.GRAY);
		}

		return Text.literal("FlowPvP: ").formatted(Formatting.GRAY)
				.append(Text.literal(stats.name()).formatted(Formatting.WHITE))
				.append(Text.literal(" "))
				.append(decorated(ladder));
	}

	public static Text details(FlowTierStats stats) {
		return Text.literal("FlowPvP stats for ").formatted(Formatting.GRAY)
				.append(Text.literal(stats.name()).formatted(Formatting.WHITE))
				.append(Text.literal(":").formatted(Formatting.GRAY));
	}

	public static List<Text> ladderDetails(FlowTierStats stats) {
		List<Text> lines = new ArrayList<>();
		stats.ladder("GLOBAL").ifPresent(global -> lines.add(ladderDetailLine(global)));
		stats.ladders().values().stream()
				.filter(FlowTierStats.LadderStats::hasPlayedRanked)
				.filter(ladder -> !ladder.ladder().equals("GLOBAL"))
				.sorted(Comparator.comparingInt(FlowTierStats.LadderStats::totalRating).reversed())
				.forEach(ladder -> lines.add(ladderDetailLine(ladder)));
		return lines;
	}

	private static Text ladderDetailLine(FlowTierStats.LadderStats ladder) {
		return Text.literal("  ")
				.append(icon(ladder.ladder()))
				.append(Text.literal(" "))
				.append(Text.literal(displayName(ladder.ladder())).formatted(Formatting.AQUA))
				.append(Text.literal(": ").formatted(Formatting.GRAY))
				.append(Text.literal(ladder.tierLabel()).formatted(Formatting.GOLD))
				.append(separator(" | "))
				.append(Text.literal(ratingText(ladder.totalRating())).setStyle(Style.EMPTY.withColor(ratingColor(ladder.totalRating()))))
				.append(separator(" | "))
				.append(Text.literal(ladder.wins() + "W/" + ladder.losses() + "L").formatted(Formatting.WHITE))
				.append(positionDetails(ladder));
	}

	private static Text positionDetails(FlowTierStats.LadderStats ladder) {
		if (!ladder.hasPosition()) {
			return Text.empty();
		}

		return separator(" | #")
				.append(Text.literal(Integer.toString(ladder.position())).formatted(Formatting.WHITE));
	}

	private static Text decorated(FlowTierStats.LadderStats ladder) {
		MutableText text = Text.empty();
		boolean wrotePart = false;

		for (int componentIndex = 0; componentIndex < FlowTierClientConfig.nametagOrder.size(); componentIndex++) {
			FlowTierClientConfig.NametagComponent component = FlowTierClientConfig.nametagOrder.get(componentIndex);
			switch (component) {
				case GAMEMODE_ICON -> {
					if (!FlowTierClientConfig.gamemodeIconEnabled) continue;
					if (wrotePart) text.append(Text.literal(" "));
					text.append(icon(ladder.ladder()));
					wrotePart = true;
				}
				case TIER -> {
					if (!FlowTierClientConfig.tierEnabled) continue;
					if (wrotePart) text.append(Text.literal(" "));
					if (FlowTierClientConfig.coloredTier) {
						text.append(Text.literal(tierLabel(ladder)).setStyle(Style.EMPTY.withColor(tierColor(ladder.tierLabel(), ladder.position()))));
					} else {
						text.append(Text.literal(tierLabel(ladder)).formatted(Formatting.WHITE));
					}
					wrotePart = true;
				}
				case SEPARATOR -> {
					if (!FlowTierClientConfig.separatorEnabled) continue;
					if (!wrotePart) continue; // no leading separator
					if (!hasFollowingNametagPart(ladder, componentIndex + 1)) continue;
					if (!endsWithSeparator(text)) text.append(separator(" | "));
				}
				case ELO -> {
					if (!FlowTierClientConfig.eloEnabled) continue;
					if (wrotePart && !endsWithSeparator(text)) text.append(Text.literal(" "));
					Style eloStyle = Style.EMPTY.withColor(FlowTierClientConfig.coloredElo ? ratingColor(ladder.totalRating()) : 0xFFFFFF);
					text.append(Text.literal(Integer.toString(ladder.totalRating())).setStyle(eloStyle));
					if (FlowTierClientConfig.eloLabelEnabled)
						text.append(Text.literal(" " + FlowTierRankSystem.RATING_LABEL).setStyle(eloStyle));
					wrotePart = true;
				}
				case POSITION -> {
					if (!FlowTierClientConfig.positionEnabled || !ladder.hasPosition()) continue;
					if (wrotePart && !endsWithSeparator(text)) text.append(Text.literal(" "));
					int posColor = FlowTierClientConfig.coloredPosition ? positionColor(ladder.tierLabel(), ladder.position()) : 0xFFFFFF;
					if (FlowTierClientConfig.positionLabelEnabled)
						text.append(Text.literal("#").setStyle(Style.EMPTY.withColor(posColor)));
					text.append(Text.literal(Integer.toString(ladder.position())).setStyle(Style.EMPTY.withColor(posColor)));
					wrotePart = true;
				}
			}
		}
		return text;
	}

	private static boolean endsWithSeparator(Text text) {
		String value = text.getString();
		return value.endsWith(" | ") || value.endsWith(" |");
	}

	private static MutableText separator(String value) {
		return Text.literal(value).setStyle(Style.EMPTY.withColor(SEPARATOR_COLOR));
	}

	private static boolean hasFollowingNametagPart(FlowTierStats.LadderStats ladder, int startIndex) {
		for (int i = startIndex; i < FlowTierClientConfig.nametagOrder.size(); i++) {
			switch (FlowTierClientConfig.nametagOrder.get(i)) {
				case GAMEMODE_ICON -> {
					if (FlowTierClientConfig.gamemodeIconEnabled) return true;
				}
				case TIER -> {
					if (FlowTierClientConfig.tierEnabled) return true;
				}
				case ELO -> {
					if (FlowTierClientConfig.eloEnabled) return true;
				}
				case POSITION -> {
					if (FlowTierClientConfig.positionEnabled && ladder.hasPosition()) return true;
				}
				case SEPARATOR -> {
				}
			}
		}
		return false;
	}

	private static String tierLabel(FlowTierStats.LadderStats ladder) {
		if (!FlowTierClientConfig.shortTierNames) {
			return ladder.tierLabel();
		}

		String tag = ladder.tierLabel();
		if (tag.matches("(?i)[LMH]T[1-5]")) {
			return tag.toUpperCase();
		}

		String[] words = tag.split("\\s+");
		if (words.length < 2) {
			return tag;
		}

		return words[0].substring(0, 1).toUpperCase() + shortDivision(words[1]);
	}

	private static String shortDivision(String division) {
		return switch (division.toUpperCase()) {
			case "I" -> "1";
			case "II" -> "2";
			case "III" -> "3";
			case "IV" -> "4";
			case "V" -> "5";
			default -> division;
		};
	}

	public static Text icon(String ladder) {
		return Text.literal(String.valueOf(iconGlyph(ladder)))
				.setStyle(FlowTierMinecraftCompat.fontStyle(Identifier.of("flowtiers", "default"))
						.withColor(0xFFFFFF));
	}

	public static String ratingText(int rating) {
		return rating + " " + FlowTierRankSystem.RATING_LABEL;
	}

	public static int ratingColor(int rating) {
		return FlowTierRankSystem.ratingColor(rating);
	}

	private static char iconGlyph(String ladder) {
		return switch (FlowTierClientConfig.normalizeLadder(ladder)) {
			case "GLOBAL" -> '\uE00A';
			case "SWORD" -> '\uE001';
			case "AXE" -> '\uE002';
			case "VANILLA", "CRYSTAL" -> '\uE003';
			case "UHC" -> '\uE004';
			case "MACE", "SPEAR_MACE", "SPEAR" -> '\uE005';
			case "NETHERITE_OP", "NETHERITE_POT" -> '\uE006';
			case "DIAMOND_POT", "POT" -> '\uE007';
			case "SMP", "NETHERITE_SMP" -> '\uE008';
			case "DIAMOND_SMP" -> '\uE009';
			case "CART" -> '\uE00A';
			default -> '\uE00A';
		};
	}

	public static String displayName(String ladder) {
		return switch (FlowTierClientConfig.normalizeLadder(ladder)) {
			case "GLOBAL" -> "Global";
			case "SWORD" -> "Sword";
			case "AXE" -> "Axe";
			case "UHC" -> "UHC";
			case "VANILLA", "CRYSTAL" -> "Vanilla";
			case "MACE" -> "Mace";
			case "SPEAR_MACE", "SPEAR" -> "Spear Mace";
			case "CART" -> "Cart";
			case "DIAMOND_POT" -> "Pot";
			case "NETHERITE_OP" -> "NethOP";
			case "SMP", "NETHERITE_SMP" -> "SMP";
			case "DIAMOND_SMP" -> "DiamondSMP";
			default -> FlowTierClientConfig.normalizeLadder(ladder);
		};
	}

	@Deprecated
	private static Text oldDetailsLine(FlowTierStats.LadderStats ladder) {
		return Text.literal("")
				.append(Text.literal(ladder.ladder()).formatted(Formatting.AQUA))
				.append(Text.literal(" "))
				.append(Text.literal(ladder.tierLabel()).formatted(Formatting.GOLD))
				.append(Text.literal(", ").formatted(Formatting.GRAY))
				.append(Text.literal(ratingText(ladder.totalRating())).formatted(Formatting.GREEN))
				.append(Text.literal(", ").formatted(Formatting.GRAY))
				.append(Text.literal(ladder.wins() + "W/" + ladder.losses() + "L").formatted(Formatting.WHITE))
				.append(Text.literal(", #").formatted(Formatting.GRAY))
				.append(Text.literal(Integer.toString(ladder.position())).formatted(Formatting.WHITE));
	}

	public static Optional<FlowTierStats.LadderStats> bestLadder(Map<String, FlowTierStats.LadderStats> ladders) {
		return ladders.values().stream()
				.filter(FlowTierStats.LadderStats::hasPlayedRanked)
				.filter(ladder -> !ladder.ladder().equals("GLOBAL"))
				.max(Comparator.comparingInt(FlowTierStats.LadderStats::totalRating));
	}

	private static int tierColor(String tier, int position) {
		return FlowTierRankSystem.tierColor(tier, position);
	}

	private static int positionColor(String tier, int position) {
		return FlowTierRankSystem.tierColor(tier, position);
	}
}
