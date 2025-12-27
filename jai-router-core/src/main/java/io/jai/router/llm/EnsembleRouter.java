package io.jai.router.llm;

import io.jai.router.core.RoutingResult;
import java.util.List;

public class EnsembleRouter {

    public RoutingResult route(List<RoutingResult> votes) {
        // Simple majority: pick max by confidence
        return votes.stream().max((a,b) -> Double.compare(a.confidence(), b.confidence())).orElse(null);
    }
}

