package com.jai.router.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Word-boundary aware keyword matcher with scoring mechanism.
 * 
 * <p>This implementation:
 * <ul>
 *   <li>Uses word boundary matching to avoid partial word matches</li>
 *   <li>Implements scoring to find the best match (not first match)</li>
 *   <li>Calculates confidence based on match quality</li>
 *   <li>Supports multiple keyword matches</li>
 * </ul>
 */
public class ScoringKeywordMatcher implements KeywordMatcher {
    private static final Logger log = LoggerFactory.getLogger(ScoringKeywordMatcher.class);
    
    private final Map<String, KeywordConfig> keywords;
    private final String defaultService;
    private final double defaultConfidence;
    
    /**
     * Configuration for a keyword.
     */
    record KeywordConfig(String service, double weight) {}
    
    /**
     * Create a new ScoringKeywordMatcher.
     * @param keywordMap mapping from keyword to service name
     * @param defaultService service to use when no keywords match
     * @param defaultConfidence confidence to use when no keywords match
     */
    public ScoringKeywordMatcher(
        Map<String, String> keywordMap,
        String defaultService,
        double defaultConfidence
    ) {
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
                bestExplanation = String.format(
                    "Matched keyword '%s' with score %.2f",
                    keyword, score
                );
            }
        }
        
        double confidence = bestScore > 0 
            ? Math.min(0.95, 0.5 + (bestScore * 0.45))
            : defaultConfidence;
        
        if (log.isDebugEnabled()) {
            log.debug("Keyword matching: service='{}' confidence={} explanation='{}'",
                bestService, confidence, bestExplanation);
        }
        
        return new MatchResult(bestService, confidence, bestExplanation);
    }
    
    /**
     * Calculate score for a keyword match.
     * @param text the text to search in (lowercase)
     * @param keyword the keyword to search for
     * @param config the keyword configuration
     * @return score (0 if no match, >0 if match)
     */
    private double calculateScore(String text, String keyword, KeywordConfig config) {
        int count = countWordMatches(text, keyword);
        return count * config.weight;
    }
    
    /**
     * Count word boundary matches for a keyword.
     * @param text the text to search in
     * @param keyword the keyword to search for
     * @return number of matches
     */
    private int countWordMatches(String text, String keyword) {
        int count = 0;
        try {
            String pattern = "\\b" + Pattern.quote(keyword) + "\\b";
            Matcher matcher = Pattern.compile(pattern).matcher(text);
            while (matcher.find()) {
                count++;
            }
        } catch (Exception e) {
            log.warn("Error matching keyword '{}': {}", keyword, e.getMessage());
        }
        return count;
    }
    
    /**
     * Build keyword configurations from keyword map.
     * @param keywordMap mapping from keyword to service name
     * @return unmodifiable map of keyword configurations
     */
    private Map<String, KeywordConfig> buildKeywordConfigs(Map<String, String> keywordMap) {
        Map<String, KeywordConfig> configs = new HashMap<>();
        keywordMap.forEach((keyword, service) -> {
            configs.put(keyword, new KeywordConfig(service, 1.0));
        });
        return Collections.unmodifiableMap(configs);
    }
}
