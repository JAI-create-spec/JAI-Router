
package com.jai.router.core;

public class DecisionContext {
    private final String payload;

    public DecisionContext(String payload) {
        this.payload = payload;
    }

    public String getPayload() {
        return payload;
    }
}
