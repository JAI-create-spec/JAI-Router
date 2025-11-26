package io.jai.router.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.jai.router.registry.ServiceRegistry;
import io.jai.router.registry.ServiceDefinition;

public class ScoringKeywordMatcher implements KeywordMatcher {
    private static final Logger log = LoggerFactory.getLogger(ScoringKeywordMatcher.class);
    private Map<String, KeywordConfig> keywords;
    private final String defaultService;
    private final double defaultConfidence;
    record KeywordConfig(String service, double weight) {}

    public ScoringKeywordMatcher(Map<String, String> keywordMap, String defaultService, double defaultConfidence) {
        this.defaultService = defaultService;
        this.defaultConfidence = defaultConfidence;
        this.keywords = buildKeywordConfigs(keywordMap);
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
        double confidence = bestScore > 0 ? Math.min(0.95, 0.5 + (bestScore * 0.45)) : defaultConfidence;
        if (log.isDebugEnabled()) log.debug("Keyword matching: service='{}' confidence={} explanation='{}'", bestService, confidence, bestExplanation);
        return new MatchResult(bestService, confidence, bestExplanation);
    }

    private double calculateScore(String text, String keyword, KeywordConfig config) {
        int count = countWordMatches(text, keyword);
        return count * config.weight;
    }

    private int countWordMatches(String text, String keyword) {
        int count = 0;
        try {
            String pattern = "\\b" + Pattern.quote(keyword) + "\\b";
            Matcher matcher = Pattern.compile(pattern).matcher(text);
            while (matcher.find()) count++;
        } catch (Exception e) {
            log.warn("Error matching keyword '{}': {}", keyword, e.getMessage());
        }
        return count;
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
     */
    public void bindRegistry(ServiceRegistry registry) {
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
            this.keywords = buildKeywordConfigs(map);
            if (log.isInfoEnabled()) log.info("ScoringKeywordMatcher bound to ServiceRegistry ({} services)", services.size());
        } catch (Exception e) {
            log.warn("Failed to bind registry: {}", e.getMessage());
        }
    }
}
