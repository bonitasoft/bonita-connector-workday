package com.bonitasoft.connectors.workday;

import lombok.Builder;
import lombok.Data;

/**
 * Unified configuration for all Workday HCM connector operations.
 */
@Data
@Builder
public class WorkdayConfiguration {

    // Connection / Auth (Project/Runtime scope)
    private String clientId;
    private String clientSecret;
    private String refreshToken;
    private String tenantAlias;
    @Builder.Default
    private String basePath = null;
    @Builder.Default
    private String tokenEndpoint = null;
    @Builder.Default
    private int connectTimeout = 30000;
    @Builder.Default
    private int readTimeout = 60000;

    // Get Worker
    private String workerId;
    @Builder.Default
    private Boolean includePersonalData = true;
    @Builder.Default
    private Boolean includeEmploymentData = true;

    // Search Workers
    private String searchQuery;
    private String wqlQuery;
    private String supervisoryOrganization;
    @Builder.Default
    private Integer limit = 50;
    private Integer offset;

    // Create Worker
    private String workerData;
    private String hireDate;
    private String workerType;

    // Update Worker
    private String updateData;
    private String effectiveDate;

    // Get Business Process
    private String processId;

    // Submit Event
    private String eventType;
    private String eventData;
    private String reason;

    // Advanced
    @Builder.Default
    private int maxRetries = 5;

    /**
     * Returns the base URL for Workday REST API calls.
     * If basePath is not set, derives it from the tenant alias.
     */
    public String getResolvedBasePath() {
        if (basePath != null && !basePath.isBlank()) {
            return basePath;
        }
        return "https://wd2-impl-services1.workday.com/ccx/api/v1";
    }

    /**
     * Returns the OAuth2 token endpoint.
     * If tokenEndpoint is not set, derives it from the tenant alias.
     */
    public String getResolvedTokenEndpoint() {
        if (tokenEndpoint != null && !tokenEndpoint.isBlank()) {
            return tokenEndpoint;
        }
        return "https://wd2-impl-services1.workday.com/ccx/oauth2/" + tenantAlias + "/token";
    }
}
