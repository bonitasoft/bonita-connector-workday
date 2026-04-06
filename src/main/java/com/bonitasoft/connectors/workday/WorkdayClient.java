package com.bonitasoft.connectors.workday;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * API client facade for Workday HCM REST API v1.
 * Uses java.net.http.HttpClient and OAuth2 Client Credentials with refresh token.
 */
@Slf4j
public class WorkdayClient {

    private final WorkdayConfiguration configuration;
    private final RetryPolicy retryPolicy;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private String accessToken;

    public WorkdayClient(WorkdayConfiguration configuration) throws WorkdayException {
        this.configuration = configuration;
        this.retryPolicy = new RetryPolicy(configuration.getMaxRetries());
        this.objectMapper = new ObjectMapper();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(configuration.getConnectTimeout()))
                .build();
        authenticate();
        log.debug("WorkdayClient initialized for tenant {}", configuration.getTenantAlias());
    }

    // Visible for testing
    WorkdayClient(WorkdayConfiguration configuration, HttpClient httpClient, String accessToken) {
        this.configuration = configuration;
        this.retryPolicy = new RetryPolicy(configuration.getMaxRetries());
        this.objectMapper = new ObjectMapper();
        this.httpClient = httpClient;
        this.accessToken = accessToken;
    }

    private void authenticate() throws WorkdayException {
        try {
            String body = "grant_type=refresh_token"
                    + "&client_id=" + URLEncoder.encode(configuration.getClientId(), StandardCharsets.UTF_8)
                    + "&client_secret=" + URLEncoder.encode(configuration.getClientSecret(), StandardCharsets.UTF_8)
                    + "&refresh_token=" + URLEncoder.encode(configuration.getRefreshToken(), StandardCharsets.UTF_8);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(configuration.getResolvedTokenEndpoint()))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .timeout(Duration.ofMillis(configuration.getReadTimeout()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                throw new WorkdayException("OAuth2 authentication failed: HTTP " + response.statusCode()
                        + " - " + response.body(), response.statusCode(), false);
            }

            JsonNode tokenResponse = objectMapper.readTree(response.body());
            this.accessToken = tokenResponse.get("access_token").asText();
            log.debug("Successfully obtained Workday access token");
        } catch (WorkdayException e) {
            throw e;
        } catch (Exception e) {
            throw new WorkdayException("Failed to authenticate with Workday: " + e.getMessage(), e);
        }
    }

    public GetWorkerResult getWorker(WorkdayConfiguration config) throws WorkdayException {
        return retryPolicy.execute(() -> {
            String url = config.getResolvedBasePath() + "/" + config.getTenantAlias()
                    + "/workers/" + URLEncoder.encode(config.getWorkerId(), StandardCharsets.UTF_8);

            HttpRequest request = buildGetRequest(url);
            HttpResponse<String> response = sendRequest(request);
            checkResponse(response);

            String responseBody = response.body();
            JsonNode root = objectMapper.readTree(responseBody);
            Map<String, Object> workerDataMap = objectMapper.convertValue(root, new TypeReference<>() {});

            String fullName = extractJsonPath(root, "descriptor");
            String employeeId = extractJsonPath(root, "employeeID");

            return new GetWorkerResult(responseBody, workerDataMap, fullName, employeeId);
        });
    }

    public SearchWorkersResult searchWorkers(WorkdayConfiguration config) throws WorkdayException {
        return retryPolicy.execute(() -> {
            StringBuilder urlBuilder = new StringBuilder();
            urlBuilder.append(config.getResolvedBasePath())
                    .append("/").append(config.getTenantAlias())
                    .append("/workers");

            List<String> params = new ArrayList<>();
            if (config.getSearchQuery() != null && !config.getSearchQuery().isBlank()) {
                params.add("search=" + URLEncoder.encode(config.getSearchQuery(), StandardCharsets.UTF_8));
            }
            if (config.getWqlQuery() != null && !config.getWqlQuery().isBlank()) {
                params.add("query=" + URLEncoder.encode(config.getWqlQuery(), StandardCharsets.UTF_8));
            }
            if (config.getSupervisoryOrganization() != null && !config.getSupervisoryOrganization().isBlank()) {
                params.add("supervisoryOrganization=" + URLEncoder.encode(config.getSupervisoryOrganization(), StandardCharsets.UTF_8));
            }
            if (config.getLimit() != null) {
                params.add("limit=" + config.getLimit());
            }
            if (config.getOffset() != null) {
                params.add("offset=" + config.getOffset());
            }
            if (!params.isEmpty()) {
                urlBuilder.append("?").append(String.join("&", params));
            }

            HttpRequest request = buildGetRequest(urlBuilder.toString());
            HttpResponse<String> response = sendRequest(request);
            checkResponse(response);

            String responseBody = response.body();
            JsonNode root = objectMapper.readTree(responseBody);

            List<Map<String, Object>> workersList = new ArrayList<>();
            JsonNode dataNode = root.get("data");
            if (dataNode != null && dataNode.isArray()) {
                for (JsonNode worker : dataNode) {
                    workersList.add(objectMapper.convertValue(worker, new TypeReference<>() {}));
                }
            }

            int totalCount = 0;
            JsonNode totalNode = root.get("total");
            if (totalNode != null) {
                totalCount = totalNode.asInt();
            }

            return new SearchWorkersResult(responseBody, workersList, totalCount);
        });
    }

    public CreateWorkerResult createWorker(WorkdayConfiguration config) throws WorkdayException {
        return retryPolicy.execute(() -> {
            String url = config.getResolvedBasePath() + "/" + config.getTenantAlias() + "/workers";

            // Build the request body combining worker data with hire metadata
            JsonNode workerNode = objectMapper.readTree(config.getWorkerData());
            var bodyMap = objectMapper.convertValue(workerNode, new TypeReference<Map<String, Object>>() {});
            bodyMap.put("hireDate", config.getHireDate());
            bodyMap.put("workerType", config.getWorkerType());
            String requestBody = objectMapper.writeValueAsString(bodyMap);

            HttpRequest request = buildPostRequest(url, requestBody);
            HttpResponse<String> response = sendRequest(request);
            checkResponse(response);

            String responseBody = response.body();
            JsonNode root = objectMapper.readTree(responseBody);

            String workerId = extractJsonPath(root, "id");
            String employeeId = extractJsonPath(root, "employeeID");

            return new CreateWorkerResult(workerId, employeeId, responseBody);
        });
    }

    public UpdateWorkerResult updateWorker(WorkdayConfiguration config) throws WorkdayException {
        return retryPolicy.execute(() -> {
            String url = config.getResolvedBasePath() + "/" + config.getTenantAlias()
                    + "/workers/" + URLEncoder.encode(config.getWorkerId(), StandardCharsets.UTF_8);

            String requestBody = config.getUpdateData();
            if (config.getEffectiveDate() != null && !config.getEffectiveDate().isBlank()) {
                JsonNode updateNode = objectMapper.readTree(requestBody);
                var bodyMap = objectMapper.convertValue(updateNode, new TypeReference<Map<String, Object>>() {});
                bodyMap.put("effectiveDate", config.getEffectiveDate());
                requestBody = objectMapper.writeValueAsString(bodyMap);
            }

            HttpRequest request = buildPatchRequest(url, requestBody);
            HttpResponse<String> response = sendRequest(request);
            checkResponse(response);

            String responseBody = response.body();
            JsonNode root = objectMapper.readTree(responseBody);
            String workerId = extractJsonPath(root, "id");

            return new UpdateWorkerResult(workerId != null ? workerId : config.getWorkerId(), responseBody);
        });
    }

    public GetBusinessProcessResult getBusinessProcess(WorkdayConfiguration config) throws WorkdayException {
        return retryPolicy.execute(() -> {
            String url = config.getResolvedBasePath() + "/" + config.getTenantAlias()
                    + "/businessProcesses/" + URLEncoder.encode(config.getProcessId(), StandardCharsets.UTF_8);

            HttpRequest request = buildGetRequest(url);
            HttpResponse<String> response = sendRequest(request);
            checkResponse(response);

            String responseBody = response.body();
            JsonNode root = objectMapper.readTree(responseBody);

            String processStatus = extractJsonPath(root, "status");
            String currentStep = extractJsonPath(root, "currentStep");

            return new GetBusinessProcessResult(processStatus, currentStep, responseBody);
        });
    }

    public SubmitEventResult submitEvent(WorkdayConfiguration config) throws WorkdayException {
        return retryPolicy.execute(() -> {
            String url = config.getResolvedBasePath() + "/" + config.getTenantAlias() + "/businessProcesses";

            JsonNode eventNode = objectMapper.readTree(config.getEventData());
            var bodyMap = objectMapper.convertValue(eventNode, new TypeReference<Map<String, Object>>() {});
            bodyMap.put("eventType", config.getEventType());
            bodyMap.put("workerId", config.getWorkerId());
            bodyMap.put("effectiveDate", config.getEffectiveDate());
            if (config.getReason() != null && !config.getReason().isBlank()) {
                bodyMap.put("reason", config.getReason());
            }
            String requestBody = objectMapper.writeValueAsString(bodyMap);

            HttpRequest request = buildPostRequest(url, requestBody);
            HttpResponse<String> response = sendRequest(request);
            checkResponse(response);

            String responseBody = response.body();
            JsonNode root = objectMapper.readTree(responseBody);

            String processId = extractJsonPath(root, "id");
            String processStatus = extractJsonPath(root, "status");

            return new SubmitEventResult(processId, processStatus, responseBody);
        });
    }

    private HttpRequest buildGetRequest(String url) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET()
                .timeout(Duration.ofMillis(configuration.getReadTimeout()))
                .build();
    }

    private HttpRequest buildPostRequest(String url, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofMillis(configuration.getReadTimeout()))
                .build();
    }

    private HttpRequest buildPatchRequest(String url, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                .timeout(Duration.ofMillis(configuration.getReadTimeout()))
                .build();
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws WorkdayException {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            throw new WorkdayException("Network error during Workday API call: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new WorkdayException("Workday API call interrupted", e);
        }
    }

    private void checkResponse(HttpResponse<String> response) throws WorkdayException {
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }
        boolean retryable = RetryPolicy.isRetryableStatusCode(statusCode);
        String errorDetail = "Workday API error: HTTP " + statusCode;
        try {
            JsonNode errorBody = objectMapper.readTree(response.body());
            if (errorBody.has("error")) {
                errorDetail += " - " + errorBody.get("error").asText();
            }
        } catch (Exception ignored) {
            // Could not parse error body
        }
        throw new WorkdayException(errorDetail, statusCode, retryable);
    }

    private String extractJsonPath(JsonNode root, String fieldName) {
        if (root == null) return null;
        JsonNode node = root.get(fieldName);
        if (node != null && !node.isNull()) {
            return node.asText();
        }
        // Try nested under "descriptor" for Workday-style responses
        if (root.has("descriptor")) {
            JsonNode descriptor = root.get("descriptor");
            if (descriptor.isTextual()) {
                return fieldName.equals("descriptor") ? descriptor.asText() : null;
            }
        }
        return null;
    }
}
