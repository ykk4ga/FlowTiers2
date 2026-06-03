package dev.fecl.flowtiers.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class FlowTierNametagCache {
	private static final int MAX_ENTRIES = 512;
	private static final Map<UUID, Entry> CACHE = new ConcurrentHashMap<>();

	private FlowTierNametagCache() {
	}

	public static Component get(UUID uuid, FlowTierStats stats, Component playerName) {
		String signature = signature(stats, playerName);
		Entry entry = CACHE.get(uuid);
		if (entry != null && entry.signature().equals(signature)) {
			return entry.text();
		}

		Component text = FlowTierFormatter.nametag(stats, playerName);
		CACHE.put(uuid, new Entry(signature, text, width(text)));
		if (CACHE.size() > MAX_ENTRIES) CACHE.clear();
		return text;
	}

	public static int width(UUID uuid, FlowTierStats stats, Component playerName) {
		get(uuid, stats, playerName);
		Entry entry = CACHE.get(uuid);
		return entry == null ? 0 : entry.width();
	}

	private static int width(Component text) {
		Minecraft client = Minecraft.getInstance();
		return client == null ? text.getString().length() : client.font.width(text);
	}

	private static String signature(FlowTierStats stats, Component playerName) {
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

	private record Entry(String signature, Component text, int width) {
	}
}
