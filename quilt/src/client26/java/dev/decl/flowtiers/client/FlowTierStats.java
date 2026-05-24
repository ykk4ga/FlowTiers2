package dev.decl.flowtiers.client;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public record FlowTierStats(UUID uuid, String name, Map<String, LadderStats> ladders, long lastUpdated) {
	public Optional<LadderStats> ladder(String ladder) {
		return Optional.ofNullable(ladders.get(FlowTierClientConfig.normalizeLadder(ladder)))
				.filter(LadderStats::hasPlayedRanked);
	}

	public Optional<LadderStats> bestLadder() {
		return ladders.values().stream()
				.filter(LadderStats::hasPlayedRanked)
				.filter(ladder -> !ladder.ladder().equals("GLOBAL"))
				.max(java.util.Comparator.comparingInt(LadderStats::totalRating));
	}

	public Optional<LadderStats> displayLadder() {
		if (FlowTierClientConfig.displayMode == FlowTierClientConfig.DisplayMode.GLOBAL) {
			return ladder("GLOBAL");
		}

		if (FlowTierClientConfig.displayMode == FlowTierClientConfig.DisplayMode.HIGHEST_TIER) {
			return bestLadder();
		}

		return ladder(FlowTierClientConfig.preferredLadder).or(this::bestLadder);
	}

	public record LadderStats(
			String ladder,
			int totalRating,
			int wins,
			int losses,
			int currentStreak,
			int placementMatchesPlayed,
			String currentRank,
			int position
	) {
		public boolean hasPlayedRanked() {
			return ladder.equals("GLOBAL") || wins > 0 || losses > 0 || placementMatchesPlayed > 0 || currentRank != null;
		}

		public boolean hasPosition() {
			return position > 0;
		}

		public String tierLabel() {
			if (position == 1) {
				return "HT1";
			}

			if (currentRank == null || currentRank.isBlank()) {
				return hasPlayedRanked() ? FlowTierRankSystem.fallbackTierLabel(totalRating) : "Unranked";
			}

			return FlowTierRankSystem.normalizeRankLabel(currentRank);
		}
	}
}
