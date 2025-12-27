package io.jai.router.core;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * LLM client that chains multiple providers with automatic fallback.
 * <p>
 * This implementation tries each configured LLM client in sequence until one succeeds.
 * This is useful for high availability scenarios where you want to fall back to
 * alternative providers if the primary one fails.
 * </p>
 *
 * <h2>Usage Example</h2>
 * <pre>{@code
 * LlmClient primary = new OpenAILlmClient();
 * LlmClient secondary = new AnthropicLlmClient();
 * LlmClient fallback = new KeywordBasedLlmClient();
 *
 * LlmClient client = new FallbackLlmClient(primary, secondary, fallback);
 * // Will try primary first, then secondary, then fallback
 * }</pre>
 *
 * @author JAI Router Team
 * @since 1.0.0
 */
public class FallbackLlmClient implements LlmClient {

    private static final Logger log = LoggerFactory.getLogger(FallbackLlmClient.class);

    private final List<LlmClient> clients;

    /**
     * Creates a fallback client with the specified providers in order.
     *
     * @param clients LLM clients to try in sequence, must not be null or empty
     * @throws NullPointerException     if clients is null
     * @throws IllegalArgumentException if clients is empty
     */
    public FallbackLlmClient(@NotNull List<LlmClient> clients) {
        Objects.requireNonNull(clients, "Clients list cannot be null");
        if (clients.isEmpty()) {
            throw new IllegalArgumentException("At least one LLM client must be provided");
        }
        this.clients = new ArrayList<>(clients);
        log.info("FallbackLlmClient initialized with {} providers", clients.size());
    }

    /**
     * Creates a fallback client with the specified providers in order.
     *
     * @param clients LLM clients to try in sequence
     * @throws NullPointerException     if clients is null
     * @throws IllegalArgumentException if no clients provided
     */
    public FallbackLlmClient(@NotNull LlmClient... clients) {
        this(Arrays.asList(clients));
    }

    /**
     * Makes a routing decision by trying each client in sequence.
     * <p>
     * This method attempts to get a decision from each configured client in order.
     * If a client fails, it logs the failure and tries the next one. If all clients
     * fail, it throws an exception with the last failure as the cause.
     * </p>
     *
     * @param ctx the decision context containing input and metadata
     * @return routing decision from the first successful client
     * @throws LlmClientException if all clients fail
     */
    @Override
    @NotNull
    public RoutingDecision decide(@NotNull DecisionContext ctx) {
        Objects.requireNonNull(ctx, "DecisionContext cannot be null");

        Exception lastException = null;
        int attemptNumber = 0;

        for (LlmClient client : clients) {
            attemptNumber++;
            try {
                if (log.isDebugEnabled()) {
                    log.debug("Attempting LLM client {}/{}: {}",
                            attemptNumber, clients.size(), client.getName());
                }

                RoutingDecision decision = client.decide(ctx);

                if (attemptNumber > 1) {
                    log.info("Fallback successful: {} returned decision after {} failed attempts",
                            client.getName(), attemptNumber - 1);
                }

                return decision;

            } catch (Exception e) {
                lastException = e;
                log.warn("LLM client {} failed (attempt {}/{}): {}",
                        client.getName(), attemptNumber, clients.size(), e.getMessage());

                // If this is not the last client, continue to next
                if (attemptNumber < clients.size()) {
                    log.debug("Trying next LLM client in fallback chain");
                }
            }
        }

        // All clients failed
        String errorMessage = String.format(
                "All %d LLM clients failed in fallback chain", clients.size());
        log.error(errorMessage);
        throw new LlmClientException(errorMessage, lastException);
    }

    /**
     * Returns a descriptive name for this fallback client.
     *
     * @return client name with fallback chain information
     */
    @Override
    @NotNull
    public String getName() {
        return String.format("FallbackLlmClient[%d providers]", clients.size());
    }

    /**
     * Returns the list of configured clients in fallback order.
     *
     * @return unmodifiable list of clients
     */
    @NotNull
    public List<LlmClient> getClients() {
        return List.copyOf(clients);
    }

    /**
     * Returns the number of clients in the fallback chain.
     *
     * @return number of clients
     */
    public int getClientCount() {
        return clients.size();
    }
}
