package dev.fecl.flowtiers.client;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dev.fecl.flowtiers.FlowTiers;
import net.fabricmc.loader.api.FabricLoader;

public final class FlowTierSeasonArchive {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH = FabricLoader.getInstance().getConfigDir().resolve("flowtiers-seasons.json");
	private static Data data = new Data();

	private FlowTierSeasonArchive() {
	}

	public static void load() {
		if (!Files.exists(PATH)) {
			save();
			return;
		}

		try (Reader reader = Files.newBufferedReader(PATH)) {
			Data loaded = GSON.fromJson(reader, Data.class);
			if (loaded != null) data = loaded.normalize();
		} catch (IOException exception) {
			FlowTiers.LOGGER.warn("Failed to load FlowTiers seasons.", exception);
		}
	}

	public static String currentSeasonName() {
		return data.currentSeasonName;
	}

	public static void setCurrentSeasonName(String name) {
		data.currentSeasonName = cleanName(name);
		save();
	}

	public static Season startNewSeason(String newSeasonName, Map<UUID, FlowTierStats> currentStats) {
		Map<String, FlowTierStats> snapshots = new ConcurrentHashMap<>();
		currentStats.forEach((uuid, stats) -> {
			if (stats != null) snapshots.put(uuid.toString(), stats);
		});

		Season archived = new Season(data.nextSeasonNumber++, data.currentSeasonName, System.currentTimeMillis(), snapshots);
		if (!snapshots.isEmpty()) data.seasons.add(0, archived);
		data.currentSeasonName = cleanName(newSeasonName);
		save();
		return archived;
	}

	public static List<Season> seasonsFor(UUID uuid) {
		String key = uuid.toString();
		return data.seasons.stream()
				.filter(season -> season.snapshots != null && season.snapshots.containsKey(key))
				.toList();
	}

	public static Optional<FlowTierStats> stats(UUID uuid, Season season) {
		if (season == null || season.snapshots == null) return Optional.empty();
		return Optional.ofNullable(season.snapshots.get(uuid.toString()));
	}

	private static void save() {
		try {
			Files.createDirectories(PATH.getParent());
			try (Writer writer = Files.newBufferedWriter(PATH)) {
				GSON.toJson(data.normalize(), writer);
			}
		} catch (IOException exception) {
			FlowTiers.LOGGER.warn("Failed to save FlowTiers seasons.", exception);
		}
	}

	private static String cleanName(String name) {
		return name == null || name.isBlank() ? "Season " + data.nextSeasonNumber : name.trim();
	}

	private static final class Data {
		String currentSeasonName = "Season 1";
		int nextSeasonNumber = 1;
		List<Season> seasons = new ArrayList<>();

		Data normalize() {
			if (currentSeasonName == null || currentSeasonName.isBlank()) currentSeasonName = "Season " + Math.max(1, nextSeasonNumber);
			if (nextSeasonNumber < 1) nextSeasonNumber = 1;
			if (seasons == null) seasons = new ArrayList<>();
			return this;
		}
	}

	public static final class Season {
		private int number;
		private String name;
		private long archivedAt;
		private Map<String, FlowTierStats> snapshots;

		private Season(int number, String name, long archivedAt, Map<String, FlowTierStats> snapshots) {
			this.number = number;
			this.name = name;
			this.archivedAt = archivedAt;
			this.snapshots = snapshots;
		}

		public int number() { return number; }
		public String name() { return name == null ? "Season " + number : name; }
		public long archivedAt() { return archivedAt; }
	}
}
