package io.jai.router.core;

public interface KeywordMatcher {
    MatchResult findBestMatch(String text);
    record MatchResult(String service, double confidence, String explanation) {}
}
