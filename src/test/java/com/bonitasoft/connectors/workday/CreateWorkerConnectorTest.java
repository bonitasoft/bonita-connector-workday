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
class CreateWorkerConnectorTest {

    @Mock
    private WorkdayClient mockClient;

    private CreateWorkerConnector connector;
    private Map<String, Object> inputs;

    @BeforeEach
    void setUp() {
        connector = new CreateWorkerConnector();
        inputs = new HashMap<>();
        inputs.put("clientId", "test-client-id");
        inputs.put("clientSecret", "test-client-secret");
        inputs.put("refreshToken", "test-refresh-token");
        inputs.put("tenantAlias", "test-tenant");
        inputs.put("connectTimeout", 30000);
        inputs.put("readTimeout", 60000);
        inputs.put("workerData", "{\"firstName\":\"John\",\"lastName\":\"Doe\"}");
        inputs.put("hireDate", "2026-01-15");
        inputs.put("workerType", "Employee");
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
        when(mockClient.createWorker(any())).thenReturn(
                new CreateWorkerResult("worker-new-1", "EMP-NEW-1", "{\"id\":\"worker-new-1\"}"));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(true);
        assertThat(outputs.get("workerId")).isEqualTo("worker-new-1");
        assertThat(outputs.get("employeeId")).isEqualTo("EMP-NEW-1");
        assertThat(outputs.get("responseBody")).isNotNull();
    }

    @Test
    void shouldFailValidationWhenWorkerDataMissing() {
        inputs.remove("workerData");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void shouldFailValidationWhenHireDateMissing() {
        inputs.remove("hireDate");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void shouldFailValidationWhenWorkerTypeMissing() {
        inputs.remove("workerType");
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
        when(mockClient.createWorker(any())).thenThrow(new WorkdayException("Bad request", 400, false));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(false);
        assertThat(outputs.get("errorMessage")).isNotNull();
        assertThat(outputs.get("errorMessage").toString()).contains("Bad request");
    }

    @Test
    void shouldPopulateAllOutputFields() throws Exception {
        injectMockClient();
        when(mockClient.createWorker(any())).thenReturn(
                new CreateWorkerResult("w-1", "E-1", "{\"status\":\"created\"}"));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(true);
        assertThat(outputs.get("workerId")).isNotNull();
        assertThat(outputs.get("employeeId")).isNotNull();
        assertThat(outputs.get("responseBody")).isNotNull();
    }
}
