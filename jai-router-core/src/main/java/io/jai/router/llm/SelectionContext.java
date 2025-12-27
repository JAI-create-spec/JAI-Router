package io.jai.router.llm;

import java.util.List;

public class SelectionContext {
    private Long latencyBudgetMs = 100L;
    private Double costBudget = 0.001;
    private List<String> allowedProviders;
    private String userPreference;
    private String requestDomain;

    public Long getLatencyBudgetMs() { return latencyBudgetMs; }
    public Double getCostBudget() { return costBudget; }
    public List<String> getAllowedProviders() { return allowedProviders; }
    public String getUserPreference() { return userPreference; }
    public String getRequestDomain() { return requestDomain; }

    public void setLatencyBudgetMs(Long latencyBudgetMs) { this.latencyBudgetMs = latencyBudgetMs; }
    public void setCostBudget(Double costBudget) { this.costBudget = costBudget; }
    public void setAllowedProviders(List<String> allowedProviders) { this.allowedProviders = allowedProviders; }
    public void setUserPreference(String userPreference) { this.userPreference = userPreference; }
    public void setRequestDomain(String requestDomain) { this.requestDomain = requestDomain; }
}

