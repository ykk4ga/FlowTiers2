package dev.fecl.flowtiers.client;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import dev.fecl.flowtiers.FlowTiers;

public final class FlowTierCache {
	private static final Duration CACHE_TTL = Duration.ofMinutes(5);
	private static final Duration FAILED_TTL = Duration.ofMinutes(1);
	private static final Duration FETCH_SPACING = Duration.ofMillis(150);

	private final FlowTierApiClient apiClient = new FlowTierApiClient();
	private final Map<UUID, CacheEntry> cache = new ConcurrentHashMap<>();
	private final Map<UUID, CompletableFuture<FlowTierStats>> inFlight = new ConcurrentHashMap<>();
	private final ScheduledExecutorService fetchExecutor = Executors.newSingleThreadScheduledExecutor(task -> {
		Thread thread = new Thread(task, "FlowTiers API Fetch");
		thread.setDaemon(true);
		return thread;
	});
	private long nextFetchAt;

	public Optional<FlowTierStats> getIfFresh(UUID uuid) {
		CacheEntry entry = cache.get(uuid);
		if (entry == null || entry.isExpired()) {
			return Optional.empty();
		}
		return Optional.ofNullable(entry.stats());
	}

	public Map<UUID, FlowTierStats> freshStats() {
		Map<UUID, FlowTierStats> stats = new ConcurrentHashMap<>();
		cache.forEach((uuid, entry) -> {
			if (!entry.isExpired() && entry.stats() != null) stats.put(uuid, entry.stats());
		});
		return stats;
	}

	public CompletableFuture<FlowTierStats> fetch(UUID uuid) {
		CacheEntry entry = cache.get(uuid);
		if (entry != null && !entry.isExpired()) {
			return CompletableFuture.completedFuture(entry.stats());
		}

		return inFlight.computeIfAbsent(uuid, key -> {
			CompletableFuture<FlowTierStats> future = new CompletableFuture<>();
			fetchExecutor.schedule(() -> {
				try {
					FlowTierStats stats = apiClient.fetchRanked(key);
					cache.put(key, new CacheEntry(stats, System.currentTimeMillis(), stats == null ? FAILED_TTL : CACHE_TTL));
					future.complete(stats);
				} catch (Exception exception) {
					cache.put(key, new CacheEntry(null, System.currentTimeMillis(), FAILED_TTL));
					FlowTiers.LOGGER.warn("Failed to fetch FlowPvP ranked stats for {}", key, exception);
					future.complete(null);
				} finally {
					inFlight.remove(key);
				}
			}, reserveFetchDelay(), TimeUnit.MILLISECONDS);
			return future;
		});
	}

	public void invalidate(UUID uuid) {
		cache.remove(uuid);
	}

	public void clear() {
		cache.clear();
	}

	private synchronized long reserveFetchDelay() {
		long now = System.currentTimeMillis();
		long scheduledAt = Math.max(nextFetchAt, now);
		nextFetchAt = scheduledAt + FETCH_SPACING.toMillis();
		return Math.max(0L, scheduledAt - now);
	}

	private record CacheEntry(FlowTierStats stats, long fetchedAt, Duration ttl) {
		boolean isExpired() {
			return System.currentTimeMillis() - fetchedAt > ttl.toMillis();
		}
	}
}
