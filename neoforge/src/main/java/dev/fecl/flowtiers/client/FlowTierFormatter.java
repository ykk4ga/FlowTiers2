package dev.fecl.flowtiers.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

public final class FlowTierFormatter {
	private static final int SEPARATOR_COLOR = 0xABABAB;

	private FlowTierFormatter() {
	}

	public static Component compact(FlowTierStats stats) {
		FlowTierStats.LadderStats ladder = stats.displayLadder().orElse(null);
		if (ladder == null || !ladder.hasPlayedRanked()) {
			return Component.literal("Unranked").withStyle(ChatFormatting.GRAY);
		}
		Component left = decorated(ladder, FlowTierClientConfig.nametagLeftOrder);
		Component right = decorated(ladder, FlowTierClientConfig.nametagRightOrder);
		if (left.getString().isEmpty()) return right;
		if (right.getString().isEmpty()) return left;
		return left.copy().append(Component.literal(" ")).append(right);
	}

	public static Component nametag(FlowTierStats stats, Component playerName) {
		FlowTierStats.LadderStats ladder = stats.displayLadder().orElse(null);
		if (ladder == null || !ladder.hasPlayedRanked()) return playerName;
		Component left = decorated(ladder, FlowTierClientConfig.nametagLeftOrder);
		Component right = decorated(ladder, FlowTierClientConfig.nametagRightOrder);
		if (alreadyDecorated(playerName, left, right)) return playerName;
		MutableComponent text = Component.empty();
		if (!left.getString().isEmpty()) text.append(left).append(Component.literal(" "));
		text.append(playerName);
		if (!right.getString().isEmpty()) text.append(Component.literal(" ")).append(right);
		return text;
	}

	private static boolean alreadyDecorated(Component playerName, Component left, Component right) {
		String name = playerName.getString();
		String leftValue = left.getString();
		String rightValue = right.getString();
		boolean hasLayout = !leftValue.isEmpty() || !rightValue.isEmpty();
		return hasLayout && (leftValue.isEmpty() || name.contains(leftValue)) && (rightValue.isEmpty() || name.contains(rightValue));
	}

	public static Component previewCompact() {
		Minecraft client = Minecraft.getInstance();
		if (client != null && client.player != null) {
			FlowTierStats real = FlowTiersClientState.cache().getIfFresh(client.player.getUUID()).orElse(null);
			if (real != null) return nametag(real, client.player.getName());
		}
		FlowTierStats.LadderStats fake = new FlowTierStats.LadderStats(
				FlowTierClientConfig.preferredLadder,
				800, 10, 5, 2, 10, "MT4", 123
		);
		return nametag(new FlowTierStats(java.util.UUID.randomUUID(), "PlayerName", Map.of(fake.ladder(), fake), 0L), Component.literal("PlayerName"));
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
				.append(Component.literal(ratingText(ladder.totalRating())).withStyle(Style.EMPTY.withColor(ratingColor(ladder.totalRating()))))
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

	private static Component decorated(FlowTierStats.LadderStats ladder, List<FlowTierClientConfig.NametagComponent> order) {
		MutableComponent text = Component.empty();
		List<FlowTierClientConfig.NametagComponent> visible = order.stream().filter(component -> isVisible(component, ladder)).toList();
		if (visible.isEmpty()) return text;
		if (FlowTierClientConfig.separatorEnabled && FlowTierClientConfig.hasSeparator(visible.get(0), FlowTierClientConfig.Edge.LEFT)) text.append(separator("| "));
		for (int i = 0; i < visible.size(); i++) {
			FlowTierClientConfig.NametagComponent component = visible.get(i);
			if (i > 0) text.append(FlowTierClientConfig.separatorEnabled && boundaryHasSeparator(visible.get(i - 1), component) ? separator(" | ") : Component.literal(" "));
			text.append(moduleText(component, ladder));
		}
		if (FlowTierClientConfig.separatorEnabled && FlowTierClientConfig.hasSeparator(visible.get(visible.size() - 1), FlowTierClientConfig.Edge.RIGHT)) text.append(separator(" |"));
		return text;
	}

	private static boolean boundaryHasSeparator(FlowTierClientConfig.NametagComponent left, FlowTierClientConfig.NametagComponent right) { return FlowTierClientConfig.hasSeparator(left, FlowTierClientConfig.Edge.RIGHT) || FlowTierClientConfig.hasSeparator(right, FlowTierClientConfig.Edge.LEFT); }
	private static boolean isVisible(FlowTierClientConfig.NametagComponent component, FlowTierStats.LadderStats ladder) { return switch (component) { case GAMEMODE_ICON -> FlowTierClientConfig.gamemodeIconEnabled; case TIER -> FlowTierClientConfig.tierEnabled; case ELO -> FlowTierClientConfig.eloEnabled; case POSITION -> FlowTierClientConfig.positionEnabled && ladder.hasPosition(); }; }
	private static Component moduleText(FlowTierClientConfig.NametagComponent component, FlowTierStats.LadderStats ladder) {
		return switch (component) {
			case GAMEMODE_ICON -> icon(ladder.ladder());
			case TIER -> FlowTierClientConfig.coloredTier ? Component.literal(tierLabel(ladder)).withStyle(Style.EMPTY.withColor(tierColor(ladder.tierLabel(), ladder.position()))) : Component.literal(tierLabel(ladder)).withStyle(ChatFormatting.WHITE);
			case ELO -> { int color = FlowTierClientConfig.coloredElo ? ratingColor(ladder.totalRating()) : 0xFFFFFF; yield Component.literal(Integer.toString(ladder.totalRating()) + (FlowTierClientConfig.eloLabelEnabled ? " " + FlowTierRankSystem.RATING_LABEL : "")).withStyle(Style.EMPTY.withColor(color)); }
			case POSITION -> { int color = FlowTierClientConfig.coloredPosition ? positionColor(ladder.tierLabel(), ladder.position()) : 0xFFFFFF; yield Component.literal((FlowTierClientConfig.positionLabelEnabled ? "#" : "") + ladder.position()).withStyle(Style.EMPTY.withColor(color)); }
		};
	}

	private static MutableComponent separator(String value) {
		return Component.literal(value).withStyle(Style.EMPTY.withColor(SEPARATOR_COLOR));
	}

	private static String tierLabel(FlowTierStats.LadderStats ladder) {
		if (!FlowTierClientConfig.shortTierNames) return ladder.tierLabel();

		String tag = ladder.tierLabel();
		if (tag.matches("(?i)[LMH]T[1-5]")) return tag.toUpperCase();

		String[] words = tag.split("\\s+");
		if (words.length < 2) return tag;
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
		return Component.literal(String.valueOf(iconGlyph(ladder)))
				.withStyle(fontStyle().withColor(0xFFFFFF));
	}

	private static Style fontStyle() {
		Style style = Style.EMPTY;
		try {
			Object id = createIdentifier("flowtiers", "default");
			Object font = createFontDescription(id);
			return (Style) Style.class.getMethod("withFont", font.getClass().getInterfaces().length == 0 ? font.getClass() : font.getClass().getInterfaces()[0])
					.invoke(style, font);
		} catch (ReflectiveOperationException ignored) {
			try {
				Object id = createIdentifier("flowtiers", "default");
				return (Style) Style.class.getMethod("withFont", id.getClass()).invoke(style, id);
			} catch (ReflectiveOperationException ignoredAgain) {
				return style;
			}
		}
	}

	private static Object createFontDescription(Object id) throws ReflectiveOperationException {
		Class<?> resourceClass = Class.forName("net.minecraft.network.chat.FontDescription$Resource");
		return resourceClass.getConstructor(id.getClass()).newInstance(id);
	}

	private static Object createIdentifier(String namespace, String path) throws ReflectiveOperationException {
		Class<?> idClass = identifierClass();
		return idClass.getMethod("fromNamespaceAndPath", String.class, String.class).invoke(null, namespace, path);
	}

	private static Class<?> identifierClass() throws ClassNotFoundException {
		try {
			return Class.forName("net.minecraft.resources.Identifier");
		} catch (ClassNotFoundException ignored) {
			return Class.forName("net.minecraft.resources.ResourceLocation");
		}
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
