package com.jai.router.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

import java.util.Map;

public class ScoringKeywordMatcherTest {

    private ScoringKeywordMatcher matcher;

    @BeforeEach
    void setUp() {
        Map<String, String> keywords = Map.of(
            "encrypt", "cryptography-service",
            "decrypt", "cryptography-service",
            "login", "auth-service",
            "token", "auth-service",
            "report", "bi-service",
            "dashboard", "bi-service"
        );
        matcher = new ScoringKeywordMatcher(keywords, "default-service", 0.5);
    }

    @Test
    void matchesKeywordWithWordBoundary() {
        KeywordMatcher.MatchResult result = matcher.findBestMatch("Please encrypt this message");
        assertThat(result.service()).isEqualTo("cryptography-service");
        assertThat(result.confidence()).isGreaterThan(0.5);
    }

    @Test
    void doesNotMatchPartialWords() {
        KeywordMatcher.MatchResult result = matcher.findBestMatch("Please encryption this message");
        // Should not match "encrypt" in "encryption"
        assertThat(result.service()).isEqualTo("default-service");
        assertThat(result.confidence()).isEqualTo(0.5);
    }

    @Test
    void returnsDefaultServiceForUnknownKeywords() {
        KeywordMatcher.MatchResult result = matcher.findBestMatch("hello world");
        assertThat(result.service()).isEqualTo("default-service");
        assertThat(result.confidence()).isEqualTo(0.5);
    }

    @Test
    void handlesVeryLongPayloads() {
        String longPayload = "encrypt " + "a".repeat(5000);
        KeywordMatcher.MatchResult result = matcher.findBestMatch(longPayload);
        assertThat(result.service()).isEqualTo("cryptography-service");
        assertThat(result.confidence()).isGreaterThan(0.5);
    }

    @Test
    void isCaseInsensitive() {
        KeywordMatcher.MatchResult result1 = matcher.findBestMatch("ENCRYPT data");
        KeywordMatcher.MatchResult result2 = matcher.findBestMatch("encrypt data");
        assertThat(result1.service()).isEqualTo(result2.service());
        assertThat(result1.confidence()).isEqualTo(result2.confidence());
    }

    @Test
    void countsMultipleMatches() {
        KeywordMatcher.MatchResult result = matcher.findBestMatch("encrypt and decrypt");
        // Should find both keywords and have higher confidence
        assertThat(result.service()).isEqualTo("cryptography-service");
        assertThat(result.confidence()).isGreaterThan(0.7);
    }

    @Test
    void selectsBestMatchWhenMultipleServicesMatch() {
        KeywordMatcher.MatchResult result = matcher.findBestMatch("login to dashboard");
        // Both "login" and "dashboard" match, but they map to different services
        // The one with more matches should win
        assertThat(result.service()).isIn("auth-service", "bi-service");
    }

    @Test
    void confidenceIsWithinValidRange() {
        KeywordMatcher.MatchResult result = matcher.findBestMatch("encrypt data");
        assertThat(result.confidence()).isBetween(0.0, 1.0);
    }

    @Test
    void explanationIsProvided() {
        KeywordMatcher.MatchResult result = matcher.findBestMatch("encrypt data");
        assertThat(result.explanation()).isNotBlank();
    }
}
