package com.bonitasoft.connectors.workday;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.bonitasoft.engine.connector.ConnectorValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class UpdateWorkerConnectorTest {

    @Mock
    private WorkdayClient mockClient;

    private UpdateWorkerConnector connector;
    private Map<String, Object> inputs;

    @BeforeEach
    void setUp() {
        connector = new UpdateWorkerConnector();
        inputs = new HashMap<>();
        inputs.put("clientId", "test-client-id");
        inputs.put("clientSecret", "test-client-secret");
        inputs.put("refreshToken", "test-refresh-token");
        inputs.put("tenantAlias", "test-tenant");
        inputs.put("connectTimeout", 30000);
        inputs.put("readTimeout", 60000);
        inputs.put("workerId", "worker-123");
        inputs.put("updateData", "{\"title\":\"Senior Engineer\"}");
    }

    private void injectMockClient() throws Exception {
        connector.setInputParameters(inputs);
        connector.validateInputParameters();
        var clientField = AbstractWorkdayConnector.class.getDeclaredField("client");
        clientField.setAccessible(true);
        clientField.set(connector, mockClient);
    }

    @Test
    void shouldExecuteSuccessfully() throws Exception {
        injectMockClient();
        when(mockClient.updateWorker(any())).thenReturn(
                new UpdateWorkerResult("worker-123", "{\"status\":\"updated\"}"));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(true);
        assertThat(outputs.get("workerId")).isEqualTo("worker-123");
        assertThat(outputs.get("responseBody")).isNotNull();
    }

    @Test
    void shouldFailValidationWhenWorkerIdMissing() {
        inputs.remove("workerId");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void shouldFailValidationWhenUpdateDataMissing() {
        inputs.remove("updateData");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void shouldFailValidationWhenClientIdMissing() {
        inputs.remove("clientId");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void shouldSetErrorOutputsOnFailure() throws Exception {
        injectMockClient();
        when(mockClient.updateWorker(any())).thenThrow(new WorkdayException("Forbidden", 403, false));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(false);
        assertThat(outputs.get("errorMessage")).isNotNull();
    }

    @Test
    void shouldApplyDefaultsForNullOptionalInputs() throws Exception {
        inputs.remove("effectiveDate");
        injectMockClient();
        when(mockClient.updateWorker(any())).thenReturn(
                new UpdateWorkerResult("worker-123", "{}"));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(true);
    }

    @Test
    void shouldPopulateAllOutputFields() throws Exception {
        injectMockClient();
        when(mockClient.updateWorker(any())).thenReturn(
                new UpdateWorkerResult("w-1", "{\"ok\":true}"));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(true);
        assertThat(outputs.get("workerId")).isNotNull();
        assertThat(outputs.get("responseBody")).isNotNull();
    }
}
