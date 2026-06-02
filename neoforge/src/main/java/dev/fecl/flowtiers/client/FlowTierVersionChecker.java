package dev.fecl.flowtiers.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.fecl.flowtiers.FlowTiers;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;

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
	private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().connectTimeout(TIMEOUT).followRedirects(HttpClient.Redirect.NORMAL).build();

	private FlowTierVersionChecker() {}

	public static void register() {
		NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingIn event) -> {
			if (FlowTierClientConfig.versionCheckEnabled) checkAndNotify(Minecraft.getInstance());
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
		String query = "?loaders=%5B%22neoforge%22%5D&game_versions=%5B%22" + gameVersion() + "%22%5D&include_changelog=false";
		HttpRequest request = HttpRequest.newBuilder(URI.create(MODRINTH_URI + query))
				.timeout(TIMEOUT).header("Accept", "application/json").header("User-Agent", "FlowTiers/" + currentVersion()).GET().build();
		try {
			HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < 200 || response.statusCode() >= 300) throw new IOException("Modrinth API returned HTTP " + response.statusCode());
			JsonArray versions = GSON.fromJson(response.body(), JsonArray.class);
			String latest = null;
			if (versions != null) for (JsonElement element : versions) {
				if (!element.isJsonObject()) continue;
				JsonObject version = element.getAsJsonObject();
				if (!"release".equalsIgnoreCase(string(version, "version_type", "release")) || !"listed".equalsIgnoreCase(string(version, "status", "listed"))) continue;
				String candidate = string(version, "version_number", null);
				if (candidate != null && (latest == null || compareVersions(candidate, latest) > 0)) latest = candidate;
			}
			return Optional.ofNullable(latest);
		} catch (IOException | InterruptedException exception) {
			if (exception instanceof InterruptedException) Thread.currentThread().interrupt();
			FlowTiers.LOGGER.warn("Failed to fetch FlowTiers version data from Modrinth.", exception);
			return Optional.empty();
		}
	}

	private static void notifyIfOutdated(Minecraft client, String latestVersion) {
		if (client.player == null || compareVersions(latestVersion, currentVersion()) <= 0) return;
		MutableComponent message = Component.literal("[FlowTiers] ").withStyle(ChatFormatting.AQUA)
				.append(Component.literal("A new version is available: ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(latestVersion).withStyle(ChatFormatting.GREEN))
				.append(Component.literal(" (you have " + currentVersion() + "). ").withStyle(ChatFormatting.GRAY))
				.append(Component.literal(DOWNLOAD_URI.toString()).withStyle(ChatFormatting.YELLOW));
		client.player.sendSystemMessage(message);
	}

	private static String currentVersion() {
		return ModList.get().getModContainerById(FlowTiers.MOD_ID).map(container -> container.getModInfo().getVersion().toString()).orElse("0.0.0");
	}

	private static String gameVersion() {
		Object version = SharedConstants.getCurrentVersion();
		try { return (String) version.getClass().getMethod("id").invoke(version); }
		catch (ReflectiveOperationException ignored) {
			try { return (String) version.getClass().getMethod("getId").invoke(version); }
			catch (ReflectiveOperationException ignoredAgain) { return version.toString(); }
		}
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
		int metadata = value.indexOf('+');
		if (metadata >= 0) value = value.substring(0, metadata);
		int suffix = value.indexOf('-');
		return suffix >= 0 ? value.substring(0, suffix) : value;
	}

	private static int numberPart(String value) {
		String digits = value.replaceAll("[^0-9].*$", "");
		if (digits.isEmpty()) return 0;
		try { return Integer.parseInt(digits); } catch (NumberFormatException ignored) { return 0; }
	}
}
