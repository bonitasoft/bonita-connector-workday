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
class SubmitEventConnectorTest {

    @Mock
    private WorkdayClient mockClient;

    private SubmitEventConnector connector;
    private Map<String, Object> inputs;

    @BeforeEach
    void setUp() {
        connector = new SubmitEventConnector();
        inputs = new HashMap<>();
        inputs.put("clientId", "test-client-id");
        inputs.put("clientSecret", "test-client-secret");
        inputs.put("refreshToken", "test-refresh-token");
        inputs.put("tenantAlias", "test-tenant");
        inputs.put("connectTimeout", 30000);
        inputs.put("readTimeout", 60000);
        inputs.put("eventType", "Compensation_Change");
        inputs.put("workerId", "worker-123");
        inputs.put("eventData", "{\"amount\":\"50000\"}");
        inputs.put("effectiveDate", "2026-04-01");
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
        when(mockClient.submitEvent(any())).thenReturn(
                new SubmitEventResult("bp-new-1", "Initiated", "{\"id\":\"bp-new-1\"}"));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(true);
        assertThat(outputs.get("processId")).isEqualTo("bp-new-1");
        assertThat(outputs.get("processStatus")).isEqualTo("Initiated");
        assertThat(outputs.get("responseBody")).isNotNull();
    }

    @Test
    void shouldFailValidationWhenEventTypeMissing() {
        inputs.remove("eventType");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void shouldFailValidationWhenWorkerIdMissing() {
        inputs.remove("workerId");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void shouldFailValidationWhenEventDataMissing() {
        inputs.remove("eventData");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void shouldFailValidationWhenEffectiveDateMissing() {
        inputs.remove("effectiveDate");
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
        when(mockClient.submitEvent(any())).thenThrow(new WorkdayException("Conflict", 409, false));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(false);
        assertThat(outputs.get("errorMessage")).isNotNull();
        assertThat(outputs.get("errorMessage").toString()).contains("Conflict");
    }

    @Test
    void shouldApplyDefaultsForNullOptionalInputs() throws Exception {
        inputs.remove("reason");
        injectMockClient();
        when(mockClient.submitEvent(any())).thenReturn(
                new SubmitEventResult("bp-2", "Initiated", "{}"));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(true);
    }

    @Test
    void shouldPopulateAllOutputFields() throws Exception {
        injectMockClient();
        when(mockClient.submitEvent(any())).thenReturn(
                new SubmitEventResult("bp-3", "Pending", "{\"ok\":true}"));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(true);
        assertThat(outputs.get("processId")).isNotNull();
        assertThat(outputs.get("processStatus")).isNotNull();
        assertThat(outputs.get("responseBody")).isNotNull();
    }
}
