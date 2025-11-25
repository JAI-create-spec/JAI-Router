package com.jai.router.core;

/**
 * Abstraction for keyword matching strategies.
 * 
 * <p>Implementations should be thread-safe if used as shared beans.
 */
public interface KeywordMatcher {
    /**
     * Find the best matching service for the given text.
     * @param text the input text to match
     * @return match result containing service, confidence, and explanation
     */
    MatchResult findBestMatch(String text);
    
    /**
     * Result of keyword matching.
     * @param service the matched service name
     * @param confidence confidence score between 0.0 and 1.0
     * @param explanation explanation of the match
     */
    record MatchResult(String service, double confidence, String explanation) {}
}
