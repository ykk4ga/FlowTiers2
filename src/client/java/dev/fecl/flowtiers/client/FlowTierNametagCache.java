package dev.fecl.flowtiers.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public final class FlowTierNametagCache {
	private static final int MAX_ENTRIES = 512;
	private static final Map<UUID, Entry> CACHE = new ConcurrentHashMap<>();

	private FlowTierNametagCache() {
	}

	public static Text get(UUID uuid, FlowTierStats stats, Text playerName) {
		String signature = signature(stats, playerName);
		Entry entry = CACHE.get(uuid);
		if (entry != null && entry.signature().equals(signature)) {
			return entry.text();
		}

		Text text = FlowTierFormatter.nametag(stats, playerName);
		CACHE.put(uuid, new Entry(signature, text, width(text)));
		if (CACHE.size() > MAX_ENTRIES) CACHE.clear();
		return text;
	}

	public static int width(UUID uuid, FlowTierStats stats, Text playerName) {
		get(uuid, stats, playerName);
		Entry entry = CACHE.get(uuid);
		return entry == null ? 0 : entry.width();
	}

	private static int width(Text text) {
		MinecraftClient client = MinecraftClient.getInstance();
		return client == null || client.textRenderer == null ? text.getString().length() : client.textRenderer.getWidth(text);
	}

	private static String signature(FlowTierStats stats, Text playerName) {
		return playerName.getString()
				+ "|" + stats.lastUpdated()
				+ "|" + stats.ladders().hashCode()
				+ "|" + FlowTierClientConfig.displayMode
				+ "|" + FlowTierClientConfig.preferredLadder
				+ "|" + FlowTierClientConfig.shortTierNames
				+ "|" + FlowTierClientConfig.coloredTier
				+ "|" + FlowTierClientConfig.coloredElo
				+ "|" + FlowTierClientConfig.coloredPosition
				+ "|" + FlowTierClientConfig.gamemodeIconEnabled
				+ "|" + FlowTierClientConfig.tierEnabled
				+ "|" + FlowTierClientConfig.eloEnabled
				+ "|" + FlowTierClientConfig.eloLabelEnabled
				+ "|" + FlowTierClientConfig.positionEnabled
				+ "|" + FlowTierClientConfig.positionLabelEnabled
				+ "|" + FlowTierClientConfig.separatorEnabled
				+ "|" + FlowTierClientConfig.nametagLeftOrder
				+ "|" + FlowTierClientConfig.nametagRightOrder
				+ "|" + FlowTierClientConfig.nametagSeparators;
	}

	private record Entry(String signature, Text text, int width) {
	}
}
