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
		return FlowTierFormatter.bestLadder(ladders);
	}

	public Optional<LadderStats> displayLadder() {
		if (FlowTierClientConfig.displayMode == FlowTierClientConfig.DisplayMode.GLOBAL) {
			return ladder("GLOBAL");
		}

		if (FlowTierClientConfig.displayMode == FlowTierClientConfig.DisplayMode.HIGHEST_TIER) {
			return bestLadder();
		}

		return Optional.ofNullable(ladders.get(FlowTierClientConfig.normalizeLadder(FlowTierClientConfig.preferredLadder)));
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
				return "Grandmaster";
			}

			if (currentRank == null || currentRank.isBlank()) {
				return hasPlayedRanked() ? fallbackTierLabel(totalRating) : "Unranked";
			}

			return titleCaseTier(currentRank);
		}

		private static String fallbackTierLabel(int rating) {
			if (rating >= 2175) return "Netherite";
			if (rating >= 1900) return "Diamond III";
			if (rating >= 1770) return "Diamond II";
			if (rating >= 1650) return "Diamond I";
			if (rating >= 1525) return "Emerald III";
			if (rating >= 1400) return "Emerald II";
			if (rating >= 1275) return "Emerald I";
			if (rating >= 1125) return "Gold III";
			if (rating >= 1025) return "Gold II";
			if (rating >= 900) return "Gold I";
			if (rating >= 800) return "Iron III";
			if (rating >= 700) return "Iron II";
			if (rating >= 600) return "Iron I";
			if (rating >= 500) return "Copper III";
			if (rating >= 400) return "Copper II";
			if (rating >= 300) return "Copper I";
			if (rating >= 200) return "Coal III";
			if (rating >= 100) return "Coal II";
			return "Coal I";
		}

		private static String titleCaseTier(String rank) {
			String[] words = rank.replace('_', ' ').toLowerCase().split("\\s+");
			StringBuilder builder = new StringBuilder();
			for (String word : words) {
				if (word.isBlank()) {
					continue;
				}

				if (!builder.isEmpty()) {
					builder.append(' ');
				}

				builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
			}

			return builder.toString();
		}
	}
}
