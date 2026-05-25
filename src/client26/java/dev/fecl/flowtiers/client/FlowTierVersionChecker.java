package dev.fecl.flowtiers.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.fecl.flowtiers.FlowTiers;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class FlowTierVersionChecker {
	private static final URI MODRINTH_URI = URI.create("https://api.modrinth.com/v2/project/flowtiers/version");
	private static final URI DOWNLOAD_URI = URI.create("https://modrinth.com/mod/flowtiers/versions");
	private static final Duration TIMEOUT = Duration.ofSeconds(8);
	private static final Gson GSON = new Gson();
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
			.connectTimeout(TIMEOUT)
			.followRedirects(HttpClient.Redirect.NORMAL)
			.build();

	private FlowTierVersionChecker() {
	}

	public static void register() {
		net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (FlowTierClientConfig.versionCheckEnabled) {
				checkAndNotify(client);
			}
		});
	}

	private static void checkAndNotify(Minecraft client) {
		CompletableFuture.supplyAsync(FlowTierVersionChecker::fetchLatestCompatibleVersion)
				.thenAccept(result -> result.ifPresent(version -> client.execute(() -> notifyIfOutdated(client, version))))
				.exceptionally(throwable -> {
					FlowTiers.LOGGER.warn("Failed to check FlowTiers updates.", throwable);
					return null;
				});
	}

	private static Optional<String> fetchLatestCompatibleVersion() {
		String gameVersion = FabricLoader.getInstance()
				.getModContainer("minecraft")
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElse("");
		String query = "?loaders=%5B%22fabric%22%5D&game_versions=%5B%22" + gameVersion + "%22%5D&include_changelog=false";
		HttpRequest request = HttpRequest.newBuilder(URI.create(MODRINTH_URI + query))
				.timeout(TIMEOUT)
				.header("Accept", "application/json")
				.header("User-Agent", "FlowTiers/" + currentVersion())
				.GET()
				.build();

		try {
			HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) {
				throw new IOException("Modrinth API returned HTTP " + response.statusCode());
			}

			JsonArray versions = GSON.fromJson(response.body(), JsonArray.class);
			if (versions == null) return Optional.empty();

			String latestVersion = null;
			for (JsonElement element : versions) {
				if (!element.isJsonObject()) continue;
				JsonObject version = element.getAsJsonObject();
				if (!isListedRelease(version)) continue;
				JsonElement versionNumber = version.get("version_number");
				if (versionNumber != null && !versionNumber.isJsonNull()) {
					String candidate = versionNumber.getAsString();
					if (latestVersion == null || compareVersions(candidate, latestVersion) > 0) {
						latestVersion = candidate;
					}
				}
			}
			return Optional.ofNullable(latestVersion);
		} catch (IOException | InterruptedException exception) {
			if (exception instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			FlowTiers.LOGGER.warn("Failed to fetch FlowTiers version data from Modrinth.", exception);
		}

		return Optional.empty();
	}

	private static boolean isListedRelease(JsonObject version) {
		return "release".equalsIgnoreCase(string(version, "version_type", "release"))
				&& "listed".equalsIgnoreCase(string(version, "status", "listed"));
	}

	private static void notifyIfOutdated(Minecraft client, String latestVersion) {
		if (client.player == null || compareVersions(latestVersion, currentVersion()) <= 0) return;

		MutableComponent message = Component.literal("[FlowTiers] ")
				.withStyle(ChatFormatting.AQUA)
				.append(Component.literal("A new version is available: ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(latestVersion).withStyle(ChatFormatting.GREEN))
				.append(Component.literal(" (you have " + currentVersion() + "). ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(DOWNLOAD_URI.toString()).withStyle(ChatFormatting.YELLOW));
		client.player.sendSystemMessage(message);
	}

	private static String currentVersion() {
		return FabricLoader.getInstance()
				.getModContainer(FlowTiers.MOD_ID)
				.map(container -> container.getMetadata().getVersion().getFriendlyString())
				.orElse("0.0.0");
	}

	private static String string(JsonObject object, String key, String fallback) {
		JsonElement value = object.get(key);
		return value == null || value.isJsonNull() ? fallback : value.getAsString();
	}

	private static int compareVersions(String a, String b) {
		String[] left = cleanVersion(a).split("\\.");
		String[] right = cleanVersion(b).split("\\.");
		int length = Math.max(left.length, right.length);
		for (int i = 0; i < length; i++) {
			int leftPart = i < left.length ? numberPart(left[i]) : 0;
			int rightPart = i < right.length ? numberPart(right[i]) : 0;
			if (leftPart != rightPart) return Integer.compare(leftPart, rightPart);
		}
		return cleanVersion(a).compareToIgnoreCase(cleanVersion(b));
	}

	private static String cleanVersion(String version) {
		String value = version == null ? "" : version.trim();
		if (value.startsWith("v") || value.startsWith("V")) value = value.substring(1);
		int metadataIndex = value.indexOf('+');
		if (metadataIndex >= 0) value = value.substring(0, metadataIndex);
		int suffixIndex = value.indexOf('-');
		if (suffixIndex >= 0) value = value.substring(0, suffixIndex);
		return value;
	}

	private static int numberPart(String value) {
		String digits = value.replaceAll("[^0-9].*$", "");
		if (digits.isEmpty()) return 0;
		try {
			return Integer.parseInt(digits);
		} catch (NumberFormatException ignored) {
			return 0;
		}
	}
}
