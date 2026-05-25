package dev.fecl.flowtiers.client;

public final class FlowTiersClientState {
	private static final FlowTierCache CACHE = new FlowTierCache();
	private static final MojangProfileResolver PROFILE_RESOLVER = new MojangProfileResolver();

	private FlowTiersClientState() {}

	public static FlowTierCache cache() {
		return CACHE;
	}

	public static MojangProfileResolver profileResolver() {
		return PROFILE_RESOLVER;
	}
}
