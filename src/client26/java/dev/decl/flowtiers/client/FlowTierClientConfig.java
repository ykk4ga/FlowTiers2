package dev.decl.flowtiers.client;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

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
	public static boolean shortTierNames = false;
	public static boolean eloEnabled = true;
	public static boolean eloLabelEnabled = true;
	public static boolean positionEnabled = true;
	public static boolean positionLabelEnabled = true;
	public static int hudX = 7;
	public static int hudY = 8;
	public static boolean hudBackground = true;

	private FlowTierClientConfig() {
	}

	public static void load() {
		if (!Files.exists(CONFIG_PATH)) {
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
			Data data = GSON.fromJson(reader, Data.class);
			if (data == null) {
				return;
			}

			hudEnabled = data.hudEnabled;
			nametagEnabled = data.nametagEnabled;
			tabListEnabled = data.tabListEnabled;
			preferredLadder = normalizeLadder(data.preferredLadder == null ? "SWORD" : data.preferredLadder);
			displayMode = DisplayMode.fromName(data.displayMode);
			rankSectionEnabled = data.rankSectionEnabled;
			shortTierNames = data.shortTierNames;
			eloEnabled = data.eloEnabled;
			eloLabelEnabled = data.eloLabelEnabled;
			positionEnabled = data.positionEnabled;
			positionLabelEnabled = data.positionLabelEnabled;
			hudX = Math.max(0, data.hudX);
			hudY = Math.max(0, data.hudY);
			hudBackground = data.hudBackground;
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
		PREFERRED_LADDER,
		HIGHEST_TIER,
		GLOBAL;

		public static DisplayMode fromName(String name) {
			if (name == null) {
				return PREFERRED_LADDER;
			}

			try {
				return DisplayMode.valueOf(name.trim().toUpperCase());
			} catch (IllegalArgumentException ignored) {
				return PREFERRED_LADDER;
			}
		}
	}

	private static final class Data {
		boolean hudEnabled = true;
		boolean nametagEnabled = true;
		boolean tabListEnabled = true;
		String preferredLadder = "SWORD";
		String displayMode = DisplayMode.PREFERRED_LADDER.name();
		boolean rankSectionEnabled = true;
		boolean shortTierNames = false;
		boolean eloEnabled = true;
		boolean eloLabelEnabled = true;
		boolean positionEnabled = true;
		boolean positionLabelEnabled = true;
		int hudX = 7;
		int hudY = 8;
		boolean hudBackground = true;

		static Data fromCurrent() {
			Data data = new Data();
			data.hudEnabled = FlowTierClientConfig.hudEnabled;
			data.nametagEnabled = FlowTierClientConfig.nametagEnabled;
			data.tabListEnabled = FlowTierClientConfig.tabListEnabled;
			data.preferredLadder = FlowTierClientConfig.preferredLadder;
			data.displayMode = FlowTierClientConfig.displayMode.name();
			data.rankSectionEnabled = FlowTierClientConfig.rankSectionEnabled;
			data.shortTierNames = FlowTierClientConfig.shortTierNames;
			data.eloEnabled = FlowTierClientConfig.eloEnabled;
			data.eloLabelEnabled = FlowTierClientConfig.eloLabelEnabled;
			data.positionEnabled = FlowTierClientConfig.positionEnabled;
			data.positionLabelEnabled = FlowTierClientConfig.positionLabelEnabled;
			data.hudX = FlowTierClientConfig.hudX;
			data.hudY = FlowTierClientConfig.hudY;
			data.hudBackground = FlowTierClientConfig.hudBackground;
			return data;
		}
	}
}
