package dev.fecl.flowtiers.client;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.fecl.flowtiers.FlowTiers;
import net.fabricmc.loader.api.FabricLoader;

public final class FlowTierClientConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("flowtiers.json");

	public static boolean hudEnabled = true;
	public static boolean nametagEnabled = true;
	public static boolean tabListEnabled = true;
	public static boolean versionCheckEnabled = true;
	public static String preferredLadder = "SWORD";
	public static DisplayMode displayMode = DisplayMode.PREFERRED_LADDER;
	public static boolean rankSectionEnabled = true;
	public static boolean gamemodeIconEnabled = true;
	public static boolean tierEnabled = false;
	public static boolean separatorEnabled = true;
	public static boolean shortTierNames = false;
	public static boolean eloEnabled = false;
	public static boolean eloLabelEnabled = false;
	public static boolean coloredElo = true;
	public static boolean coloredTier = true;
	public static boolean coloredPosition = true;
	public static boolean positionEnabled = false;
	public static boolean positionLabelEnabled = false;
	public static int hudX = 7;
	public static int hudY = 8;
	public static float hudScale = 1.0F;
	public static boolean hudBackground = true;
	public static boolean hudRecordEnabled = true;
	public static boolean hudStreakEnabled = false;
	public static NametagAlignment nametagAlignment = NametagAlignment.LEFT;
	public static boolean suppressRankedDuplicates = true;
	public static List<NametagComponent> nametagLeftOrder = defaultNametagLeftOrder();
	public static List<NametagComponent> nametagRightOrder = new ArrayList<>();
	public static Set<String> nametagSeparators = defaultNametagSeparators();
	public static List<PlayerReference> recentPlayers = new ArrayList<>();
	public static List<PlayerReference> favoritePlayers = new ArrayList<>();

	private FlowTierClientConfig() {}

	public static List<NametagComponent> defaultNametagLeftOrder() {
		return new ArrayList<>(List.of(
				NametagComponent.GAMEMODE_ICON, NametagComponent.TIER,
				NametagComponent.ELO, NametagComponent.POSITION
		));
	}
	public static Set<String> defaultNametagSeparators() { return new LinkedHashSet<>(List.of(separatorKey(NametagComponent.TIER, Edge.RIGHT), separatorKey(NametagComponent.ELO, Edge.RIGHT))); }

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			Data data = GSON.fromJson(reader, Data.class);
			if (data == null) return;

			hudEnabled = data.hudEnabled;
			nametagEnabled = data.nametagEnabled;
			tabListEnabled = data.tabListEnabled;
			versionCheckEnabled = data.versionCheckEnabled == null || data.versionCheckEnabled;
			preferredLadder = normalizeLadder(data.preferredLadder == null ? "SWORD" : data.preferredLadder);
			displayMode = DisplayMode.fromName(data.displayMode);
			rankSectionEnabled = data.rankSectionEnabled;
			gamemodeIconEnabled = data.gamemodeIconEnabled;
			tierEnabled = data.tierEnabled;
			if (!rankSectionEnabled) {
				gamemodeIconEnabled = false;
				tierEnabled = false;
			}
			shortTierNames = data.shortTierNames;
			eloEnabled = data.eloEnabled;
			eloLabelEnabled = data.eloLabelEnabled;
			coloredElo = data.coloredElo;
			coloredTier = data.coloredTier;
			coloredPosition = data.coloredPosition;
			positionEnabled = data.positionEnabled;
			positionLabelEnabled = data.positionLabelEnabled;
			hudX = Math.max(0, data.hudX);
			hudY = Math.max(0, data.hudY);
			hudScale = clampHudScale(data.hudScale == null ? 1.0F : data.hudScale);
			hudBackground = data.hudBackground;
			hudRecordEnabled = data.hudRecordEnabled;
			hudStreakEnabled = data.hudStreakEnabled;
			nametagAlignment = data.nametagAlignment == null ? NametagAlignment.LEFT :
					NametagAlignment.valueOf(data.nametagAlignment.toUpperCase());
			suppressRankedDuplicates = data.suppressRankedDuplicates;
			separatorEnabled = data.separatorEnabled;
			loadNametagLayout(data);
			recentPlayers = readPlayerReferences(data.recentPlayers);
			favoritePlayers = readPlayerReferences(data.favoritePlayers);
		} catch (IOException exception) {
			FlowTiers.LOGGER.warn("Failed to load FlowTiers config.", exception);
		}
	}

	public enum Edge { LEFT, RIGHT }
	public static void setNametagLayout(List<NametagComponent> left, List<NametagComponent> right) { nametagLeftOrder = new ArrayList<>(left); nametagRightOrder = new ArrayList<>(right); normalizeNametagLayout(); }
	public static boolean hasSeparator(NametagComponent component, Edge edge) { return nametagSeparators.contains(separatorKey(component, edge)); }
	public static boolean canEnableSeparator(NametagComponent component, Edge edge) { NametagComponent neighbor = neighbor(component, edge); return neighbor == null || !hasSeparator(neighbor, edge == Edge.LEFT ? Edge.RIGHT : Edge.LEFT); }
	public static void setSeparator(NametagComponent component, Edge edge, boolean enabled) { String key = separatorKey(component, edge); if (enabled && canEnableSeparator(component, edge)) { separatorEnabled = true; nametagSeparators.add(key); } if (!enabled) nametagSeparators.remove(key); }
	private static void loadNametagLayout(Data data) { if (data.nametagLeftOrder != null || data.nametagRightOrder != null) { nametagLeftOrder = readComponents(data.nametagLeftOrder); nametagRightOrder = readComponents(data.nametagRightOrder); nametagSeparators = data.nametagSeparators == null ? defaultNametagSeparators() : new LinkedHashSet<>(data.nametagSeparators); } else migrateLegacyLayout(data.nametagOrder, data.nametagAlignment); normalizeNametagLayout(); if (!separatorEnabled) { nametagSeparators.clear(); separatorEnabled = true; } }
	private static void migrateLegacyLayout(List<String> legacy, String alignment) { List<NametagComponent> modules = new ArrayList<>(); nametagSeparators = new LinkedHashSet<>(); NametagComponent previous = null; if (legacy != null) for (String value : legacy) { if ("SEPARATOR".equals(value)) { if (previous != null) nametagSeparators.add(separatorKey(previous, Edge.RIGHT)); continue; } NametagComponent component = parseComponent(value); if (component != null && !modules.contains(component)) { modules.add(component); previous = component; } } if (modules.isEmpty()) modules = defaultNametagLeftOrder(); boolean right = "RIGHT".equalsIgnoreCase(alignment); nametagLeftOrder = right ? new ArrayList<>() : modules; nametagRightOrder = right ? modules : new ArrayList<>(); }
	private static void normalizeNametagLayout() { LinkedHashSet<NametagComponent> seen = new LinkedHashSet<>(); nametagLeftOrder = unique(nametagLeftOrder, seen); nametagRightOrder = unique(nametagRightOrder, seen); for (NametagComponent component : NametagComponent.values()) if (!seen.contains(component)) nametagLeftOrder.add(component); nametagSeparators.removeIf(key -> !validSeparatorKey(key)); }
	private static List<NametagComponent> readComponents(List<String> values) { List<NametagComponent> result = new ArrayList<>(); if (values != null) for (String value : values) { NametagComponent component = parseComponent(value); if (component != null) result.add(component); } return result; }
	private static List<NametagComponent> unique(List<NametagComponent> values, Set<NametagComponent> seen) { List<NametagComponent> result = new ArrayList<>(); if (values != null) for (NametagComponent component : values) if (component != null && seen.add(component)) result.add(component); return result; }
	private static NametagComponent parseComponent(String value) { try { return NametagComponent.valueOf(value); } catch (Exception ignored) { return null; } }
	private static boolean validSeparatorKey(String key) { String[] parts = key.split(":"); return parts.length == 2 && parseComponent(parts[0]) != null && ("LEFT".equals(parts[1]) || "RIGHT".equals(parts[1])); }
	private static String separatorKey(NametagComponent component, Edge edge) { return component.name() + ":" + edge.name(); }
	private static NametagComponent neighbor(NametagComponent component, Edge edge) { NametagComponent result = neighborIn(nametagLeftOrder, component, edge); return result != null ? result : neighborIn(nametagRightOrder, component, edge); }
	private static NametagComponent neighborIn(List<NametagComponent> order, NametagComponent component, Edge edge) { int index = order.indexOf(component); if (index < 0) return null; int neighbor = edge == Edge.LEFT ? index - 1 : index + 1; return neighbor >= 0 && neighbor < order.size() ? order.get(neighbor) : null; }

	public static void save() {
		try {
			Files.createDirectories(CONFIG_PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
				GSON.toJson(Data.fromCurrent(), writer);
			}
		} catch (IOException exception) {
			FlowTiers.LOGGER.warn("Failed to save FlowTiers config.", exception);
		}
	}

	public static String normalizeLadder(String ladder) {
		String normalized = ladder.trim().toUpperCase().replace('-', '_').replace(' ', '_');
		return switch (normalized) {
			case "SPEARMACE", "SPEAR_MACE", "SPEAR" -> "SPEAR_MACE";
			case "CARTS", "MINECART", "MINECARTS" -> "CART";
			default -> normalized;
		};
	}

	public static float clampHudScale(float scale) { return Math.max(0.5F, Math.min(scale, 2.0F)); }
	public static void recordPlayerVisit(String uuid, String name) { recentPlayers.removeIf(entry -> entry.uuid().equalsIgnoreCase(uuid)); recentPlayers.add(0, new PlayerReference(uuid, name)); if (recentPlayers.size() > 12) recentPlayers = new ArrayList<>(recentPlayers.subList(0, 12)); save(); }
	public static boolean isFavorite(String uuid) { return favoritePlayers.stream().anyMatch(entry -> entry.uuid().equalsIgnoreCase(uuid)); }
	public static void toggleFavorite(String uuid, String name) { if (!favoritePlayers.removeIf(entry -> entry.uuid().equalsIgnoreCase(uuid))) favoritePlayers.add(new PlayerReference(uuid, name)); save(); }
	private static List<PlayerReference> readPlayerReferences(List<PlayerReference> values) { if (values == null) return new ArrayList<>(); LinkedHashSet<String> seen = new LinkedHashSet<>(); List<PlayerReference> result = new ArrayList<>(); for (PlayerReference value : values) if (value != null && value.uuid() != null && value.name() != null && seen.add(value.uuid().toLowerCase())) result.add(value); return result; }

	public enum DisplayMode {
		PREFERRED_LADDER, HIGHEST_TIER, GLOBAL;

		public static DisplayMode fromName(String name) {
			if (name == null) return PREFERRED_LADDER;
			try {
				return DisplayMode.valueOf(name.trim().toUpperCase());
			} catch (IllegalArgumentException ignored) {
				return PREFERRED_LADDER;
			}
		}
	}

	public enum NametagAlignment { LEFT, RIGHT }

	public enum NametagComponent { GAMEMODE_ICON, TIER, ELO, POSITION }
	public record PlayerReference(String uuid, String name) {}

	private static final class Data {
		boolean hudEnabled = true;
		boolean nametagEnabled = true;
		boolean tabListEnabled = true;
		Boolean versionCheckEnabled = true;
		String preferredLadder = "SWORD";
		String displayMode = DisplayMode.PREFERRED_LADDER.name();
		boolean rankSectionEnabled = true;
		boolean gamemodeIconEnabled = true;
		boolean tierEnabled = true;
		boolean separatorEnabled = true;
		boolean shortTierNames = false;
		boolean eloEnabled = false;
		boolean eloLabelEnabled = false;
		boolean coloredElo = true;
		boolean coloredTier = true;
		boolean coloredPosition = true;
		boolean positionEnabled = false;
		boolean positionLabelEnabled = false;
		int hudX = 7;
		int hudY = 8;
		Float hudScale = 1.0F;
		boolean hudBackground = true;
		boolean hudRecordEnabled = true;
		boolean hudStreakEnabled = false;
		String nametagAlignment = NametagAlignment.LEFT.name();
		boolean suppressRankedDuplicates = true;
		List<String> nametagOrder = null;
		List<String> nametagLeftOrder = null;
		List<String> nametagRightOrder = null;
		List<String> nametagSeparators = null;
		List<PlayerReference> recentPlayers = null;
		List<PlayerReference> favoritePlayers = null;

		static Data fromCurrent() {
			Data data = new Data();
			data.hudEnabled = FlowTierClientConfig.hudEnabled;
			data.nametagEnabled = FlowTierClientConfig.nametagEnabled;
			data.tabListEnabled = FlowTierClientConfig.tabListEnabled;
			data.versionCheckEnabled = FlowTierClientConfig.versionCheckEnabled;
			data.preferredLadder = FlowTierClientConfig.preferredLadder;
			data.displayMode = FlowTierClientConfig.displayMode.name();
			data.rankSectionEnabled = FlowTierClientConfig.gamemodeIconEnabled || FlowTierClientConfig.tierEnabled;
			data.gamemodeIconEnabled = FlowTierClientConfig.gamemodeIconEnabled;
			data.tierEnabled = FlowTierClientConfig.tierEnabled;
			data.separatorEnabled = FlowTierClientConfig.separatorEnabled;
			data.shortTierNames = FlowTierClientConfig.shortTierNames;
			data.eloEnabled = FlowTierClientConfig.eloEnabled;
			data.eloLabelEnabled = FlowTierClientConfig.eloLabelEnabled;
			data.coloredElo = FlowTierClientConfig.coloredElo;
			data.coloredTier = FlowTierClientConfig.coloredTier;
			data.coloredPosition = FlowTierClientConfig.coloredPosition;
			data.positionEnabled = FlowTierClientConfig.positionEnabled;
			data.positionLabelEnabled = FlowTierClientConfig.positionLabelEnabled;
			data.hudX = FlowTierClientConfig.hudX;
			data.hudY = FlowTierClientConfig.hudY;
			data.hudScale = FlowTierClientConfig.hudScale;
			data.hudBackground = FlowTierClientConfig.hudBackground;
			data.hudRecordEnabled = FlowTierClientConfig.hudRecordEnabled;
			data.hudStreakEnabled = FlowTierClientConfig.hudStreakEnabled;
			data.nametagAlignment = FlowTierClientConfig.nametagAlignment.name();
			data.suppressRankedDuplicates = FlowTierClientConfig.suppressRankedDuplicates;
			data.nametagLeftOrder = FlowTierClientConfig.nametagLeftOrder.stream()
					.map(Enum::name).collect(Collectors.toList());
			data.nametagRightOrder = FlowTierClientConfig.nametagRightOrder.stream()
					.map(Enum::name).collect(Collectors.toList());
			data.nametagSeparators = new ArrayList<>(FlowTierClientConfig.nametagSeparators);
			data.recentPlayers = new ArrayList<>(FlowTierClientConfig.recentPlayers);
			data.favoritePlayers = new ArrayList<>(FlowTierClientConfig.favoritePlayers);
			return data;
		}
	}
}
