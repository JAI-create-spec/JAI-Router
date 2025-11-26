package io.jai.router.registry;

import java.util.List;

public record ServiceDefinition(String id, String displayName, List<String> keywords) {}
