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
		// fake ladder stats for preview
		FlowTierStats.LadderStats fake = new FlowTierStats.LadderStats(
				FlowTierClientConfig.preferredLadder,
				800, 10, 5, 2, 10, "IRON_III", 123
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
				.append(Text.literal(" | ").formatted(Formatting.DARK_GRAY))
				.append(Text.literal(ladder.totalRating() + " ELO").setStyle(Style.EMPTY.withColor(eloColor(ladder.totalRating()))))
				.append(Text.literal(" | ").formatted(Formatting.DARK_GRAY))
				.append(Text.literal(ladder.wins() + "W/" + ladder.losses() + "L").formatted(Formatting.WHITE))
				.append(positionDetails(ladder));
	}

	private static Text positionDetails(FlowTierStats.LadderStats ladder) {
		if (!ladder.hasPosition()) {
			return Text.empty();
		}

		return Text.literal(" | #").formatted(Formatting.GRAY)
				.append(Text.literal(Integer.toString(ladder.position())).formatted(Formatting.WHITE));
	}

	private static Text decorated(FlowTierStats.LadderStats ladder) {
		MutableText text = Text.empty();
		boolean wrotePart = false;

		for (FlowTierClientConfig.NametagComponent component : FlowTierClientConfig.nametagOrder) {
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
					text.append(Text.literal(tierLabel(ladder)).formatted(Formatting.GOLD));
					wrotePart = true;
				}
				case ELO -> {
					if (!FlowTierClientConfig.eloEnabled) continue;
					if (wrotePart) text.append(Text.literal(" | ").formatted(Formatting.DARK_GRAY));
					Style eloStyle = Style.EMPTY.withColor(FlowTierClientConfig.coloredElo ? eloColor(ladder.totalRating()) : 0x55FF55);
					text.append(Text.literal(Integer.toString(ladder.totalRating())).setStyle(eloStyle));
					if (FlowTierClientConfig.eloLabelEnabled)
						text.append(Text.literal(" ELO").setStyle(eloStyle));
					wrotePart = true;
				}
				case POSITION -> {
					if (!FlowTierClientConfig.positionEnabled || !ladder.hasPosition()) continue;
					if (wrotePart) text.append(Text.literal(" | ").formatted(Formatting.DARK_GRAY));
					if (FlowTierClientConfig.positionLabelEnabled)
						text.append(Text.literal("#").formatted(Formatting.GRAY));
					text.append(Text.literal(Integer.toString(ladder.position())).formatted(Formatting.WHITE));
					wrotePart = true;
				}
			}
		}
		return text;
	}

	private static String tierLabel(FlowTierStats.LadderStats ladder) {
		if (!FlowTierClientConfig.shortTierNames) {
			return ladder.tierLabel();
		}

		String[] words = ladder.tierLabel().split("\\s+");
		if (words.length < 2) {
			return ladder.tierLabel();
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
				.setStyle(FlowTierMinecraftCompat.fontStyle(Identifier.of("flowtiers", "default")).withColor(0xFFFFFF));
	}

	private static int eloColor(int elo) {
		if (elo >= 2175) return 0x8B5CF6;
		if (elo >= 1650) return 0x55FFFF;
		if (elo >= 1275) return 0x50C878;
		if (elo >= 900) return 0xFFD700;
		if (elo >= 600) return 0xC0C0C0;
		if (elo >= 300) return 0xCD7F32;
		return 0xAAAAAA;
	}

	private static char iconGlyph(String ladder) {
		return switch (FlowTierClientConfig.normalizeLadder(ladder)) {
			case "GLOBAL" -> '\uE00A';
			case "SWORD" -> '\uE001';
			case "AXE" -> '\uE002';
			case "VANILLA", "CRYSTAL" -> '\uE003';
			case "UHC" -> '\uE004';
			case "MACE" -> '\uE005';
			case "NETHERITE_OP", "NETHERITE_POT" -> '\uE006';
			case "DIAMOND_POT", "POT" -> '\uE007';
			case "SMP", "NETHERITE_SMP" -> '\uE008';
			case "DIAMOND_SMP" -> '\uE009';
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
				.append(Text.literal(ladder.totalRating() + " ELO").formatted(Formatting.GREEN))
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
}
