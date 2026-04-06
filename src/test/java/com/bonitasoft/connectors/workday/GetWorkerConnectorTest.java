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
class GetWorkerConnectorTest {

    @Mock
    private WorkdayClient mockClient;

    private GetWorkerConnector connector;
    private Map<String, Object> inputs;

    @BeforeEach
    void setUp() {
        connector = new GetWorkerConnector();
        inputs = new HashMap<>();
        inputs.put("clientId", "test-client-id");
        inputs.put("clientSecret", "test-client-secret");
        inputs.put("refreshToken", "test-refresh-token");
        inputs.put("tenantAlias", "test-tenant");
        inputs.put("workerId", "worker-123");
        inputs.put("includePersonalData", true);
        inputs.put("includeEmploymentData", true);
        inputs.put("connectTimeout", 30000);
        inputs.put("readTimeout", 60000);
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
        when(mockClient.getWorker(any())).thenReturn(
                new GetWorkerResult("{\"id\":\"worker-123\"}", Map.of("id", "worker-123"), "John Doe", "EMP-001"));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(true);
        assertThat(outputs.get("workerData")).isEqualTo("{\"id\":\"worker-123\"}");
        assertThat(outputs.get("fullName")).isEqualTo("John Doe");
        assertThat(outputs.get("employeeId")).isEqualTo("EMP-001");
        assertThat(outputs.get("workerDataMap")).isNotNull();
    }

    @Test
    void shouldFailValidationWhenClientIdMissing() {
        inputs.remove("clientId");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void shouldFailValidationWhenClientSecretMissing() {
        inputs.remove("clientSecret");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void shouldFailValidationWhenRefreshTokenMissing() {
        inputs.remove("refreshToken");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void shouldFailValidationWhenTenantAliasMissing() {
        inputs.remove("tenantAlias");
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
    void shouldSetErrorOutputsOnFailure() throws Exception {
        injectMockClient();
        when(mockClient.getWorker(any())).thenThrow(new WorkdayException("Not found", 404, false));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(false);
        assertThat(outputs.get("errorMessage")).isNotNull();
        assertThat(outputs.get("errorMessage").toString()).contains("Not found");
    }

    @Test
    void shouldApplyDefaultsForNullOptionalInputs() throws Exception {
        inputs.remove("includePersonalData");
        inputs.remove("includeEmploymentData");
        inputs.remove("connectTimeout");
        inputs.remove("readTimeout");
        injectMockClient();
        when(mockClient.getWorker(any())).thenReturn(
                new GetWorkerResult("{}", Map.of(), "Jane", "EMP-002"));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(true);
    }

    @Test
    void shouldPopulateAllOutputFields() throws Exception {
        injectMockClient();
        Map<String, Object> dataMap = Map.of("id", "w-1", "name", "Test");
        when(mockClient.getWorker(any())).thenReturn(
                new GetWorkerResult("{\"id\":\"w-1\"}", dataMap, "Test Name", "EMP-100"));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(true);
        assertThat(outputs.get("workerData")).isNotNull();
        assertThat(outputs.get("workerDataMap")).isNotNull();
        assertThat(outputs.get("fullName")).isNotNull();
        assertThat(outputs.get("employeeId")).isNotNull();
    }
}
