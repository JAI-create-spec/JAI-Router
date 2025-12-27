package io.jai.router.llm;

import java.util.List;

public interface ExampleRegistry {
    List<RoutingExample> findSimilar(String request, int limit);
}

