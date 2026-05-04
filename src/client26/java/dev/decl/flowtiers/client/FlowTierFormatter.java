package dev.decl.flowtiers.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public final class FlowTierFormatter {
	private FlowTierFormatter() {
	}

	public static Component compact(FlowTierStats stats) {
		FlowTierStats.LadderStats ladder = stats.displayLadder().orElse(null);

		if (ladder == null || !ladder.hasPlayedRanked()) {
			return Component.literal("Unranked").withStyle(ChatFormatting.GRAY);
		}

		return decorated(ladder);
	}

	public static Component previewCompact() {
		return Component.literal("Player").withStyle(ChatFormatting.WHITE)
				.append(Component.literal(" "))
				.append(icon("SWORD"))
				.append(Component.literal(" "))
				.append(Component.literal("Iron III").withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))
				.append(Component.literal("800 ELO").withStyle(style -> style.withColor(eloColor(800))))
				.append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))
				.append(Component.literal("#123").withStyle(ChatFormatting.WHITE));
	}

	public static Component details(FlowTierStats stats) {
		return Component.literal("FlowPvP stats for ").withStyle(ChatFormatting.GRAY)
				.append(Component.literal(stats.name()).withStyle(ChatFormatting.WHITE))
				.append(Component.literal(":").withStyle(ChatFormatting.GRAY));
	}

	public static List<Component> ladderDetails(FlowTierStats stats) {
		List<Component> lines = new ArrayList<>();
		stats.ladder("GLOBAL").ifPresent(global -> lines.add(ladderDetailLine(global)));
		stats.ladders().values().stream()
				.filter(FlowTierStats.LadderStats::hasPlayedRanked)
				.filter(ladder -> !ladder.ladder().equals("GLOBAL"))
				.sorted(Comparator.comparingInt(FlowTierStats.LadderStats::totalRating).reversed())
				.forEach(ladder -> lines.add(ladderDetailLine(ladder)));
		return lines;
	}

	private static Component ladderDetailLine(FlowTierStats.LadderStats ladder) {
		return Component.literal("  ")
				.append(icon(ladder.ladder()))
				.append(Component.literal(" "))
				.append(Component.literal(displayName(ladder.ladder())).withStyle(ChatFormatting.AQUA))
				.append(Component.literal(": ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(ladder.tierLabel()).withStyle(ChatFormatting.GOLD))
				.append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))
				.append(Component.literal(ladder.totalRating() + " ELO").withStyle(style -> style.withColor(eloColor(ladder.totalRating()))))
				.append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY))
				.append(Component.literal(ladder.wins() + "W/" + ladder.losses() + "L").withStyle(ChatFormatting.WHITE))
				.append(positionDetails(ladder));
	}

	private static Component positionDetails(FlowTierStats.LadderStats ladder) {
		if (!ladder.hasPosition()) {
			return Component.empty();
		}

		return Component.literal(" | #").withStyle(ChatFormatting.GRAY)
				.append(Component.literal(Integer.toString(ladder.position())).withStyle(ChatFormatting.WHITE));
	}

	private static Component decorated(FlowTierStats.LadderStats ladder) {
		MutableComponent text = Component.empty();
		boolean wrotePart = false;

		if (FlowTierClientConfig.gamemodeIconEnabled) {
			text.append(icon(ladder.ladder()));
			wrotePart = true;
		}

		if (FlowTierClientConfig.tierEnabled) {
			if (wrotePart) {
				text.append(Component.literal(" "));
			}
			text.append(Component.literal(tierLabel(ladder)).withStyle(ChatFormatting.GOLD));
			wrotePart = true;
		}

		if (FlowTierClientConfig.eloEnabled) {
			if (wrotePart) {
				text.append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY));
			}
			int color = FlowTierClientConfig.coloredElo ? eloColor(ladder.totalRating()) : 0x55FF55;
			text.append(Component.literal(Integer.toString(ladder.totalRating())).withStyle(style -> style.withColor(color)));
			if (FlowTierClientConfig.eloLabelEnabled) {
				text.append(Component.literal(" ELO").withStyle(style -> style.withColor(color)));
			}
			wrotePart = true;
		}

		if (FlowTierClientConfig.positionEnabled && ladder.hasPosition()) {
			if (wrotePart) {
				text.append(Component.literal(" | ").withStyle(ChatFormatting.DARK_GRAY));
			}
			if (FlowTierClientConfig.positionLabelEnabled) {
				text.append(Component.literal("#").withStyle(ChatFormatting.GRAY));
			}
			text.append(Component.literal(Integer.toString(ladder.position())).withStyle(ChatFormatting.WHITE));
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

	public static Component icon(String ladder) {
		return Component.literal(String.valueOf(iconGlyph(ladder))).withStyle(style -> style
				.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath("flowtiers", "default")))
				.withColor(0xFFFFFF));
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

	public static Optional<FlowTierStats.LadderStats> bestLadder(Map<String, FlowTierStats.LadderStats> ladders) {
		return ladders.values().stream()
				.filter(FlowTierStats.LadderStats::hasPlayedRanked)
				.filter(ladder -> !ladder.ladder().equals("GLOBAL"))
				.max(Comparator.comparingInt(FlowTierStats.LadderStats::totalRating));
	}
}
