package com.agilecheckup.persistency.entity;

/**
 * Defines the scope of dashboard analytics data.
 * Used to distinguish between individual team analytics and aggregated assessment matrix analytics.
 */
public enum AnalyticsScope {
    
    /**
     * Analytics for a specific team within an assessment matrix.
     * The teamId field will contain the actual team identifier.
     */
    TEAM,
    
    /**
     * Aggregated analytics across all teams participating in an assessment matrix.
     * The teamId field will be null for this scope.
     */
    ASSESSMENT_MATRIX
}