package dev.fecl.flowtiers.client;

public final class FlowTiersClientState {
	private static final FlowTierCache CACHE = new FlowTierCache();
	private static final MojangProfileResolver PROFILE_RESOLVER = new MojangProfileResolver();
	private static final dev.fecl.flowtiers.client.leaderboard.FlowTierLeaderboardClient LEADERBOARD_CLIENT = new dev.fecl.flowtiers.client.leaderboard.FlowTierLeaderboardClient();

	private FlowTiersClientState() {}

	public static FlowTierCache cache() {
		return CACHE;
	}

	public static MojangProfileResolver profileResolver() {
		return PROFILE_RESOLVER;
	}

	public static dev.fecl.flowtiers.client.leaderboard.FlowTierLeaderboardClient leaderboardClient() {
		return LEADERBOARD_CLIENT;
	}
}