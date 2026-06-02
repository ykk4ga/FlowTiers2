package dev.fecl.flowtiers.client;

import java.util.Comparator;

public final class FlowTierStatsClipboard {
	private FlowTierStatsClipboard() {}

	public static String format(String name, FlowTierStats stats) {
		StringBuilder text = new StringBuilder("FlowPvP Stats - ").append(name);
		if (stats == null) {
			text.append("\nNo ranked stats found.");
		} else {
			stats.ladders().values().stream()
					.filter(FlowTierStats.LadderStats::hasPlayedRanked)
					.sorted(Comparator
							.comparingInt((FlowTierStats.LadderStats ladder) -> ladder.ladder().equals("GLOBAL") ? 0 : 1)
							.thenComparing(Comparator.comparingInt(FlowTierStats.LadderStats::totalRating).reversed()))
					.forEach(ladder -> {
						text.append("\n").append(FlowTierFormatter.displayName(ladder.ladder()))
								.append(": ").append(ladder.tierLabel())
								.append(" | ").append(ladder.totalRating()).append(" SR");
						if (ladder.hasPosition()) text.append(" | #").append(ladder.position());
						text.append(" | ").append(ladder.wins()).append("W ").append(ladder.losses()).append("L");
					});
		}
		return text.append("\nhttps://flowpvp.gg/user/").append(name).toString();
	}
}
