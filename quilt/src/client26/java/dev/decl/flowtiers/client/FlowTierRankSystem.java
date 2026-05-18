package dev.decl.flowtiers.client;

public final class FlowTierRankSystem {
	public static final String RATING_LABEL = "SR";

	private FlowTierRankSystem() {
	}

	public static String fallbackTierLabel(int rating) {
		if (rating >= 2175) return "HT1";
		if (rating >= 1900) return "MT1";
		if (rating >= 1770) return "LT1";
		if (rating >= 1650) return "HT2";
		if (rating >= 1525) return "MT2";
		if (rating >= 1400) return "LT2";
		if (rating >= 1275) return "HT3";
		if (rating >= 1125) return "MT3";
		if (rating >= 1025) return "LT3";
		if (rating >= 900) return "HT4";
		if (rating >= 800) return "MT4";
		if (rating >= 700) return "LT4";
		if (rating >= 600) return "HT5";
		if (rating >= 400) return "MT5";
		return "LT5";
	}

	public static String normalizeRankLabel(String rank) {
		if (rank == null || rank.isBlank()) {
			return rank;
		}

		String compact = rank.trim()
				.toUpperCase()
				.replace('-', '_')
				.replace(' ', '_');
		compact = compact.replace("LOW_TIER_", "LT")
				.replace("MID_TIER_", "MT")
				.replace("HIGH_TIER_", "HT")
				.replace("LOWTIER_", "LT")
				.replace("MIDTIER_", "MT")
				.replace("HIGHTIER_", "HT")
				.replace("_", "");

		if (isTierTag(compact)) {
			return compact;
		}

		String legacyTag = legacyRankToTierTag(compact);
		if (legacyTag != null) {
			return legacyTag;
		}

		return titleCaseTier(rank);
	}

	public static int tierColor(String tier, int position) {
		if (position == 1 || "HT1".equalsIgnoreCase(tier)) return 0xFF55FF;
		if (startsWithAny(tier, "MT1", "LT1")) return 0x8B5CF6;
		if (startsWithAny(tier, "HT2", "MT2", "LT2")) return 0x55FFFF;
		if (startsWithAny(tier, "HT3", "MT3", "LT3")) return 0x50C878;
		if (startsWithAny(tier, "HT4", "MT4", "LT4")) return 0xFFD700;
		if (startsWithAny(tier, "HT5", "MT5")) return 0xC0C0C0;
		if (startsWithAny(tier, "LT5")) return 0xCD7F32;
		return 0xFFFFFF;
	}

	public static int ratingColor(int rating) {
		return tierColor(fallbackTierLabel(rating), 0);
	}

	private static boolean isTierTag(String rank) {
		if (rank.length() != 3) {
			return false;
		}

		String prefix = rank.substring(0, 2);
		char number = rank.charAt(2);
		return (prefix.equals("LT") || prefix.equals("MT") || prefix.equals("HT"))
				&& number >= '1' && number <= '5';
	}

	private static String legacyRankToTierTag(String rank) {
		return switch (rank) {
			case "GRANDMASTER", "NETHERITE" -> "HT1";
			case "DIAMONDIII" -> "MT1";
			case "DIAMONDII" -> "LT1";
			case "DIAMONDI" -> "HT2";
			case "EMERALDIII" -> "MT2";
			case "EMERALDII" -> "LT2";
			case "EMERALDI" -> "HT3";
			case "GOLDIII" -> "MT3";
			case "GOLDII" -> "LT3";
			case "GOLDI" -> "HT4";
			case "IRONIII" -> "MT4";
			case "IRONII" -> "LT4";
			case "IRONI" -> "HT5";
			case "COPPERIII", "COPPERII" -> "MT5";
			case "COPPERI", "COALIII", "COALII", "COALI" -> "LT5";
			default -> null;
		};
	}

	private static boolean startsWithAny(String value, String... prefixes) {
		if (value == null) {
			return false;
		}

		for (String prefix : prefixes) {
			if (value.startsWith(prefix)) {
				return true;
			}
		}
		return false;
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

			builder.append(titleCaseTierWord(word));
		}

		return builder.toString();
	}

	private static String titleCaseTierWord(String word) {
		return switch (word.toUpperCase()) {
			case "I", "II", "III", "IV", "V" -> word.toUpperCase();
			default -> Character.toUpperCase(word.charAt(0)) + word.substring(1);
		};
	}
}
