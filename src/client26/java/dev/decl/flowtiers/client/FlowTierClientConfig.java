package dev.decl.flowtiers.client;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.decl.flowtiers.FlowTiers;
import net.fabricmc.loader.api.FabricLoader;

public final class FlowTierClientConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("flowtiers.json");

	public static boolean hudEnabled = true;
	public static boolean nametagEnabled = true;
	public static boolean tabListEnabled = true;
	public static String preferredLadder = "SWORD";
	public static DisplayMode displayMode = DisplayMode.PREFERRED_LADDER;
	public static boolean rankSectionEnabled = true;
	public static boolean gamemodeIconEnabled = true;
	public static boolean tierEnabled = false;
	public static boolean shortTierNames = false;
	public static boolean eloEnabled = true;
	public static boolean eloLabelEnabled = false;
	public static boolean coloredElo = true;
	public static boolean coloredTier = true;
	public static boolean coloredPosition = true;
	public static boolean positionEnabled = false;
	public static boolean positionLabelEnabled = false;
	public static int hudX = 7;
	public static int hudY = 8;
	public static boolean hudBackground = true;
	public static boolean hudRecordEnabled = true;
	public static boolean hudStreakEnabled = false;
	public static NametagAlignment nametagAlignment = NametagAlignment.LEFT;
	public static boolean suppressRankedDuplicates = true;
	public static List<NametagComponent> nametagOrder = defaultNametagOrder();

	private FlowTierClientConfig() {}

	public static List<NametagComponent> defaultNametagOrder() {
		return new ArrayList<>(List.of(
				NametagComponent.GAMEMODE_ICON, NametagComponent.TIER,
				NametagComponent.ELO, NametagComponent.POSITION
		));
	}

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
			hudBackground = data.hudBackground;
			hudRecordEnabled = data.hudRecordEnabled;
			hudStreakEnabled = data.hudStreakEnabled;
			nametagAlignment = data.nametagAlignment == null ? NametagAlignment.LEFT :
					NametagAlignment.valueOf(data.nametagAlignment.toUpperCase());
			suppressRankedDuplicates = data.suppressRankedDuplicates;
			if (data.nametagOrder != null && !data.nametagOrder.isEmpty()) {
				nametagOrder = new ArrayList<>();
				for (String s : data.nametagOrder) {
					try { nametagOrder.add(NametagComponent.valueOf(s)); } catch (Exception ignored) {}
				}
				if (nametagOrder.isEmpty()) nametagOrder = defaultNametagOrder();
			} else {
				nametagOrder = defaultNametagOrder();
			}
		} catch (IOException exception) {
			FlowTiers.LOGGER.warn("Failed to load FlowTiers config.", exception);
		}
	}

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
		return ladder.trim().toUpperCase().replace('-', '_');
	}

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

	private static final class Data {
		boolean hudEnabled = true;
		boolean nametagEnabled = true;
		boolean tabListEnabled = true;
		String preferredLadder = "SWORD";
		String displayMode = DisplayMode.PREFERRED_LADDER.name();
		boolean rankSectionEnabled = true;
		boolean gamemodeIconEnabled = true;
		boolean tierEnabled = false;
		boolean shortTierNames = false;
		boolean eloEnabled = true;
		boolean eloLabelEnabled = false;
		boolean coloredElo = true;
		boolean coloredTier = true;
		boolean coloredPosition = true;
		boolean positionEnabled = false;
		boolean positionLabelEnabled = false;
		int hudX = 7;
		int hudY = 8;
		boolean hudBackground = true;
		boolean hudRecordEnabled = true;
		boolean hudStreakEnabled = false;
		String nametagAlignment = NametagAlignment.LEFT.name();
		boolean suppressRankedDuplicates = true;
		List<String> nametagOrder = null;

		static Data fromCurrent() {
			Data data = new Data();
			data.hudEnabled = FlowTierClientConfig.hudEnabled;
			data.nametagEnabled = FlowTierClientConfig.nametagEnabled;
			data.tabListEnabled = FlowTierClientConfig.tabListEnabled;
			data.preferredLadder = FlowTierClientConfig.preferredLadder;
			data.displayMode = FlowTierClientConfig.displayMode.name();
			data.rankSectionEnabled = FlowTierClientConfig.gamemodeIconEnabled || FlowTierClientConfig.tierEnabled;
			data.gamemodeIconEnabled = FlowTierClientConfig.gamemodeIconEnabled;
			data.tierEnabled = FlowTierClientConfig.tierEnabled;
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
			data.hudBackground = FlowTierClientConfig.hudBackground;
			data.hudRecordEnabled = FlowTierClientConfig.hudRecordEnabled;
			data.hudStreakEnabled = FlowTierClientConfig.hudStreakEnabled;
			data.nametagAlignment = FlowTierClientConfig.nametagAlignment.name();
			data.suppressRankedDuplicates = FlowTierClientConfig.suppressRankedDuplicates;
			data.nametagOrder = FlowTierClientConfig.nametagOrder.stream()
					.map(Enum::name).collect(Collectors.toList());
			return data;
		}
	}
}