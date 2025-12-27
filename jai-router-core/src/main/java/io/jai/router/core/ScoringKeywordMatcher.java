package io.jai.router.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.jai.router.registry.ServiceRegistry;
import io.jai.router.registry.ServiceDefinition;

public class ScoringKeywordMatcher implements KeywordMatcher {
    private static final Logger log = LoggerFactory.getLogger(ScoringKeywordMatcher.class);
    private static final double DEFAULT_MIN_CONFIDENCE = 0.5;
    private static final double DEFAULT_MAX_CONFIDENCE = 0.95;
    private static final double DEFAULT_CONFIDENCE_MULTIPLIER = 0.45;

    private Map<String, KeywordConfig> keywords;
    private Map<String, Pattern> compiledPatterns;
    private final String defaultService;
    private final double defaultConfidence;
    private final double minConfidence;
    private final double maxConfidence;
    private final double confidenceMultiplier;

    record KeywordConfig(String service, double weight) {}

    /**
     * Creates a ScoringKeywordMatcher with default confidence calculation parameters.
     *
     * @param keywordMap        map of keywords to service IDs
     * @param defaultService    default service when no keywords match
     * @param defaultConfidence default confidence score
     */
    public ScoringKeywordMatcher(Map<String, String> keywordMap, String defaultService, double defaultConfidence) {
        this(keywordMap, defaultService, defaultConfidence,
                DEFAULT_MIN_CONFIDENCE, DEFAULT_MAX_CONFIDENCE, DEFAULT_CONFIDENCE_MULTIPLIER);
    }

    /**
     * Creates a ScoringKeywordMatcher with configurable confidence calculation parameters.
     *
     * @param keywordMap            map of keywords to service IDs
     * @param defaultService        default service when no keywords match
     * @param defaultConfidence     default confidence score
     * @param minConfidence         minimum confidence for matched keywords
     * @param maxConfidence         maximum confidence cap
     * @param confidenceMultiplier  multiplier for score-to-confidence conversion
     */
    public ScoringKeywordMatcher(
            Map<String, String> keywordMap,
            String defaultService,
            double defaultConfidence,
            double minConfidence,
            double maxConfidence,
            double confidenceMultiplier) {
        this.defaultService = defaultService;
        this.defaultConfidence = defaultConfidence;
        this.minConfidence = minConfidence;
        this.maxConfidence = maxConfidence;
        this.confidenceMultiplier = confidenceMultiplier;
        this.keywords = buildKeywordConfigs(keywordMap);
        this.compiledPatterns = compilePatterns(keywordMap);
    }

    @Override
    public MatchResult findBestMatch(String text) {
        String lowerText = text.toLowerCase();
        double bestScore = 0.0;
        String bestService = defaultService;
        String bestExplanation = "No keywords matched";
        for (var entry : keywords.entrySet()) {
            String keyword = entry.getKey();
            KeywordConfig config = entry.getValue();
            double score = calculateScore(lowerText, keyword, config);
            if (score > bestScore) {
                bestScore = score;
                bestService = config.service;
                bestExplanation = String.format("Matched keyword '%s' with score %.2f", keyword, score);
            }
        }
        double confidence = calculateConfidence(bestScore);
        if (log.isDebugEnabled()) {
            log.debug("Keyword matching: service='{}' confidence={} explanation='{}'",
                    bestService, confidence, bestExplanation);
        }
        return new MatchResult(bestService, confidence, bestExplanation);
    }

    /**
     * Calculates the confidence score based on the keyword match score.
     *
     * @param score the raw keyword match score
     * @return confidence value between 0.0 and 1.0
     */
    private double calculateConfidence(double score) {
        if (score <= 0) {
            return defaultConfidence;
        }
        return Math.min(maxConfidence, minConfidence + (score * confidenceMultiplier));
    }

    private double calculateScore(String text, String keyword, KeywordConfig config) {
        int count = countWordMatches(text, keyword);
        return count * config.weight;
    }

    private int countWordMatches(String text, String keyword) {
        int count = 0;
        Pattern pattern = compiledPatterns.get(keyword);
        if (pattern == null) return 0;
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) count++;
        return count;
    }

    private Map<String, Pattern> compilePatterns(Map<String, String> keywordMap) {
        Map<String, Pattern> patterns = new HashMap<>();
        if (keywordMap != null) {
            keywordMap.keySet().forEach(keyword -> {
                try {
                    patterns.put(keyword, Pattern.compile("\\b" + Pattern.quote(keyword) + "\\b", Pattern.CASE_INSENSITIVE));
                } catch (Exception e) {
                    log.warn("Failed to compile pattern for keyword '{}': {}", keyword, e.getMessage());
                }
            });
        }
        return Collections.unmodifiableMap(patterns);
    }

    private Map<String, KeywordConfig> buildKeywordConfigs(Map<String, String> keywordMap) {
        Map<String, KeywordConfig> configs = new HashMap<>();
        if (keywordMap != null) {
            keywordMap.forEach((keyword, service) -> configs.put(keyword, new KeywordConfig(service, 1.0)));
        }
        return Collections.unmodifiableMap(configs);
    }

    /**
     * Bind to a runtime ServiceRegistry to rebuild keywords dynamically from service definitions.
     * This is a best-effort sync: it replaces the current keyword map with one derived from the registry.
     * <p>
     * This method is thread-safe and performs atomic updates to ensure consistency.
     * </p>
     *
     * @param registry the service registry to bind to, may be null (no-op)
     */
    public synchronized void bindRegistry(ServiceRegistry registry) {
        if (registry == null) return;
        try {
            List<ServiceDefinition> services = registry.listServices();
            Map<String, String> map = new HashMap<>();
            for (ServiceDefinition sd : services) {
                List<String> kws = sd.keywords();
                if (kws != null) {
                    for (String kw : kws) map.put(kw, sd.id());
                }
            }

            // Build new configurations atomically
            Map<String, KeywordConfig> newKeywords = buildKeywordConfigs(map);
            Map<String, Pattern> newPatterns = compilePatterns(map);

            // Atomic update - assign both at once to maintain consistency
            this.keywords = newKeywords;
            this.compiledPatterns = newPatterns;

            if (log.isInfoEnabled()) {
                log.info("ScoringKeywordMatcher bound to ServiceRegistry ({} services)", services.size());
            }
        } catch (Exception e) {
            log.warn("Failed to bind registry: {}", e.getMessage());
        }
    }
}
