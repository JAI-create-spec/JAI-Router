package io.jai.router.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Caching wrapper for LLM clients to reduce API calls and costs.
 * <p>
 * This implementation caches routing decisions based on input text to avoid
 * redundant LLM API calls. This can significantly reduce costs and improve
 * response times for repeated or similar queries.
 * </p>
 *
 * <h2>Features</h2>
 * <ul>
 *   <li>Exact match caching with configurable TTL</li>
 *   <li>Automatic cache eviction based on size and time</li>
 *   <li>Thread-safe concurrent access</li>
 *   <li>Cache hit/miss statistics</li>
 * </ul>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * LlmClient expensiveClient = new OpenAILlmClient();
 * LlmClient cachedClient = new CachingLlmClient(
 *     expensiveClient,
 *     1000,                    // max 1000 entries
 *     Duration.ofMinutes(30)   // 30 minute TTL
 * );
 *
 * // First call hits the LLM
 * RoutingDecision decision1 = cachedClient.decide(ctx);
 *
 * // Second call with same input returns cached result
 * RoutingDecision decision2 = cachedClient.decide(ctx);
 * }</pre>
 *
 * @author JAI Router Team
 * @since 1.0.0
 */
public class CachingLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(CachingLlmClient.class);

    private final LlmClient delegate;
    private final Map<String, CacheEntry> cache;
    private final int maxSize;
    private final Duration ttl;
    private long cacheHits = 0;
    private long cacheMisses = 0;

    /**
     * Creates a caching LLM client with the specified configuration.
     *
     * @param delegate the underlying LLM client to cache
     * @param maxSize  maximum number of entries to cache
     * @param ttl      time-to-live for cache entries
     * @throws NullPointerException     if delegate or ttl is null
     * @throws IllegalArgumentException if maxSize is not positive
     */
    public CachingLlmClient(@NotNull LlmClient delegate, int maxSize, @NotNull Duration ttl) {
        this.delegate = Objects.requireNonNull(delegate, "Delegate client cannot be null");
        this.ttl = Objects.requireNonNull(ttl, "TTL cannot be null");

        if (maxSize <= 0) {
            throw new IllegalArgumentException("maxSize must be positive");
        }
        this.maxSize = maxSize;
        this.cache = new ConcurrentHashMap<>(maxSize);

        log.info("CachingLlmClient initialized: maxSize={}, ttl={}", maxSize, ttl);
    }

    /**
     * Creates a caching LLM client with default settings (1000 entries, 1 hour TTL).
     *
     * @param delegate the underlying LLM client to cache
     */
    public CachingLlmClient(@NotNull LlmClient delegate) {
        this(delegate, 1000, Duration.ofHours(1));
    }

    /**
     * Makes a routing decision, using cache when possible.
     * <p>
     * This method first checks the cache for a valid entry. If found, it returns
     * the cached decision. Otherwise, it calls the delegate client and caches
     * the result for future use.
     * </p>
     *
     * @param ctx the decision context containing input and metadata
     * @return routing decision (from cache or delegate)
     * @throws LlmClientException if the delegate client fails
     */
    @Override
    @NotNull
    public synchronized RoutingDecision decide(@NotNull DecisionContext ctx) {
        Objects.requireNonNull(ctx, "DecisionContext cannot be null");

        String key = ctx.payload();

        // Check cache first
        CacheEntry entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            cacheHits++;
            if (log.isDebugEnabled()) {
                log.debug("Cache hit for input (hit rate: {:.2f}%)",
                        getCacheHitRate() * 100);
            }
            return entry.decision;
        }

        // Cache miss - call delegate
        cacheMisses++;
        if (log.isDebugEnabled()) {
            log.debug("Cache miss for input (hit rate: {:.2f}%)",
                    getCacheHitRate() * 100);
        }

        RoutingDecision decision = delegate.decide(ctx);

        // Store in cache
        putInCache(key, decision);

        return decision;
    }

    /**
     * Stores a decision in the cache, evicting old entries if necessary.
     *
     * @param key      the cache key (input text)
     * @param decision the routing decision to cache
     */
    private void putInCache(String key, RoutingDecision decision) {
        // Evict expired entries if cache is full
        if (cache.size() >= maxSize) {
            evictExpiredEntries();

            // If still full, evict oldest entry
            if (cache.size() >= maxSize) {
                evictOldestEntry();
            }
        }

        cache.put(key, new CacheEntry(decision, Instant.now().plus(ttl)));

        if (log.isTraceEnabled()) {
            log.trace("Cached decision for input (cache size: {})", cache.size());
        }
    }

    /**
     * Removes all expired entries from the cache.
     */
    private void evictExpiredEntries() {
        int beforeSize = cache.size();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int evicted = beforeSize - cache.size();

        if (evicted > 0 && log.isDebugEnabled()) {
            log.debug("Evicted {} expired cache entries", evicted);
        }
    }

    /**
     * Removes the oldest entry from the cache.
     */
    private void evictOldestEntry() {
        cache.entrySet().stream()
                .min((e1, e2) -> e1.getValue().expiresAt.compareTo(e2.getValue().expiresAt))
                .ifPresent(entry -> {
                    cache.remove(entry.getKey());
                    if (log.isDebugEnabled()) {
                        log.debug("Evicted oldest cache entry to make room");
                    }
                });
    }

    /**
     * Clears all entries from the cache.
     */
    public synchronized void clearCache() {
        int size = cache.size();
        cache.clear();
        log.info("Cache cleared ({} entries removed)", size);
    }

    /**
     * Returns the current cache hit rate.
     *
     * @return hit rate between 0.0 and 1.0
     */
    public synchronized double getCacheHitRate() {
        long total = cacheHits + cacheMisses;
        return total == 0 ? 0.0 : (double) cacheHits / total;
    }

    /**
     * Returns cache statistics.
     *
     * @return cache statistics
     */
    @NotNull
    public synchronized CacheStats getStats() {
        return new CacheStats(
                cache.size(),
                maxSize,
                cacheHits,
                cacheMisses,
                getCacheHitRate()
        );
    }

    /**
     * Resets cache statistics (hits and misses).
     */
    public synchronized void resetStats() {
        cacheHits = 0;
        cacheMisses = 0;
        log.info("Cache statistics reset");
    }

    @Override
    @NotNull
    public String getName() {
        return "CachingLlmClient[delegate=" + delegate.getName() + "]";
    }

    /**
     * Cache entry with expiration time.
     */
    private static class CacheEntry {
        final RoutingDecision decision;
        final Instant expiresAt;

        CacheEntry(RoutingDecision decision, Instant expiresAt) {
            this.decision = decision;
            this.expiresAt = expiresAt;
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiresAt);
        }
    }

    /**
     * Immutable cache statistics.
     *
     * @param currentSize current number of cached entries
     * @param maxSize     maximum cache size
     * @param hits        total cache hits
     * @param misses      total cache misses
     * @param hitRate     cache hit rate (0.0-1.0)
     */
    public record CacheStats(
            int currentSize,
            int maxSize,
            long hits,
            long misses,
            double hitRate
    ) {
        @Override
        public String toString() {
            return String.format(
                    "CacheStats[size=%d/%d, hits=%d, misses=%d, hitRate=%.2f%%]",
                    currentSize, maxSize, hits, misses, hitRate * 100
            );
        }
    }
}
