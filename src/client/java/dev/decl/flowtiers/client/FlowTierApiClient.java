package dev.decl.flowtiers.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class FlowTierApiClient {
	private static final URI BASE_URI = URI.create("https://flowpvp.gg/api/");
	private static final Duration TIMEOUT = Duration.ofSeconds(8);
	private static final Gson GSON = new Gson();

	private final HttpClient httpClient = HttpClient.newBuilder()
			.connectTimeout(TIMEOUT)
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	public FlowTierStats fetchRanked(UUID uuid) throws IOException, InterruptedException {
		HttpRequest request = HttpRequest.newBuilder(BASE_URI.resolve("ranked/" + uuid))
				.timeout(TIMEOUT)
				.header("Accept", "application/json")
				.header("User-Agent", "FlowTiers Minecraft Mod")
				.GET()
				.build();

		HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() == 404 || response.body() == null || response.body().equals("null")) {
			return null;
		}

		if (response.statusCode() < 200 || response.statusCode() >= 300) {
			throw new IOException("FlowPvP API returned HTTP " + response.statusCode());
		}

		JsonObject root = GSON.fromJson(response.body(), JsonObject.class);
		if (root == null || root.isJsonNull()) {
			return null;
		}

		UUID playerUuid = UUID.fromString(string(root, "_id", uuid.toString()));
		String name = string(root, "lastKnownName", playerUuid.toString());
		long lastUpdated = longValue(root, "lastUpdated", 0L);
		Map<String, FlowTierStats.LadderStats> ladders = readLadders(root.getAsJsonObject("perLadder"));
		addGlobalStats(root, ladders);
		return new FlowTierStats(playerUuid, name, ladders, lastUpdated);
	}

	private static Map<String, FlowTierStats.LadderStats> readLadders(JsonObject perLadder) {
		Map<String, FlowTierStats.LadderStats> ladders = new HashMap<>();
		if (perLadder == null) {
			return ladders;
		}

		for (Map.Entry<String, JsonElement> entry : perLadder.entrySet()) {
			if (!entry.getValue().isJsonObject()) {
				continue;
			}

			JsonObject ladder = entry.getValue().getAsJsonObject();
			String key = FlowTierClientConfig.normalizeLadder(entry.getKey());
			ladders.put(key, new FlowTierStats.LadderStats(
					key,
					intValue(ladder, "totalRating", 0),
					intValue(ladder, "wins", 0),
					intValue(ladder, "losses", 0),
					intValue(ladder, "currentStreak", 0),
					intValue(ladder, "placementMatchesPlayed", 0),
					string(ladder, "currentRank", null),
					intValue(ladder, "position", 0)
			));
		}

		return ladders;
	}

	private static void addGlobalStats(JsonObject root, Map<String, FlowTierStats.LadderStats> ladders) {
		if (!root.has("globalElo")) {
			return;
		}

		int wins = ladders.values().stream().mapToInt(FlowTierStats.LadderStats::wins).sum();
		int losses = ladders.values().stream().mapToInt(FlowTierStats.LadderStats::losses).sum();
		int placements = ladders.values().stream().mapToInt(FlowTierStats.LadderStats::placementMatchesPlayed).sum();
		ladders.put("GLOBAL", new FlowTierStats.LadderStats(
				"GLOBAL",
				intValue(root, "globalElo", 0),
				wins,
				losses,
				0,
				placements,
				null,
				intValue(root, "globalPosition", 0)
		));
	}

	private static String string(JsonObject object, String key, String fallback) {
		JsonElement value = object.get(key);
		return value == null || value.isJsonNull() ? fallback : value.getAsString();
	}

	private static int intValue(JsonObject object, String key, int fallback) {
		JsonElement value = object.get(key);
		return value == null || value.isJsonNull() ? fallback : value.getAsInt();
	}

	private static long longValue(JsonObject object, String key, long fallback) {
		JsonElement value = object.get(key);
		return value == null || value.isJsonNull() ? fallback : value.getAsLong();
	}
}
