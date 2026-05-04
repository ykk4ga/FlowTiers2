package dev.decl.flowtiers.client;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import dev.decl.flowtiers.FlowTiers;

public final class FlowTierCache {
	private static final Duration CACHE_TTL = Duration.ofMinutes(10);
	private static final Duration FAILED_TTL = Duration.ofMinutes(1);

	private final FlowTierApiClient apiClient = new FlowTierApiClient();
	private final Map<UUID, CacheEntry> cache = new ConcurrentHashMap<>();
	private final Map<UUID, CompletableFuture<FlowTierStats>> inFlight = new ConcurrentHashMap<>();

	public Optional<FlowTierStats> getIfFresh(UUID uuid) {
		CacheEntry entry = cache.get(uuid);
		if (entry == null || entry.isExpired()) {
			return Optional.empty();
		}

		return Optional.ofNullable(entry.stats());
	}

	public CompletableFuture<FlowTierStats> fetch(UUID uuid) {
		CacheEntry entry = cache.get(uuid);
		if (entry != null && !entry.isExpired()) {
			return CompletableFuture.completedFuture(entry.stats());
		}

		return inFlight.computeIfAbsent(uuid, key -> CompletableFuture.supplyAsync(() -> {
			try {
				FlowTierStats stats = apiClient.fetchRanked(key);
				cache.put(key, new CacheEntry(stats, System.currentTimeMillis(), stats == null ? FAILED_TTL : CACHE_TTL));
				return stats;
			} catch (Exception exception) {
				cache.put(key, new CacheEntry(null, System.currentTimeMillis(), FAILED_TTL));
				FlowTiers.LOGGER.warn("Failed to fetch FlowPvP ranked stats for {}", key, exception);
				return null;
			}
		}).whenComplete((stats, throwable) -> inFlight.remove(key)));
	}

	private record CacheEntry(FlowTierStats stats, long fetchedAt, Duration ttl) {
		boolean isExpired() {
			return System.currentTimeMillis() - fetchedAt > ttl.toMillis();
		}
	}
}
