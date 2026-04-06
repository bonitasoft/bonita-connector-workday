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
class GetBusinessProcessConnectorTest {

    @Mock
    private WorkdayClient mockClient;

    private GetBusinessProcessConnector connector;
    private Map<String, Object> inputs;

    @BeforeEach
    void setUp() {
        connector = new GetBusinessProcessConnector();
        inputs = new HashMap<>();
        inputs.put("clientId", "test-client-id");
        inputs.put("clientSecret", "test-client-secret");
        inputs.put("refreshToken", "test-refresh-token");
        inputs.put("tenantAlias", "test-tenant");
        inputs.put("connectTimeout", 30000);
        inputs.put("readTimeout", 60000);
        inputs.put("processId", "bp-123");
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
        when(mockClient.getBusinessProcess(any())).thenReturn(
                new GetBusinessProcessResult("Completed", "Approval", "{\"id\":\"bp-123\"}"));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(true);
        assertThat(outputs.get("processStatus")).isEqualTo("Completed");
        assertThat(outputs.get("currentStep")).isEqualTo("Approval");
        assertThat(outputs.get("responseBody")).isNotNull();
    }

    @Test
    void shouldFailValidationWhenProcessIdMissing() {
        inputs.remove("processId");
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
    void shouldFailValidationWhenTenantAliasMissing() {
        inputs.remove("tenantAlias");
        connector.setInputParameters(inputs);
        assertThatThrownBy(() -> connector.validateInputParameters())
                .isInstanceOf(ConnectorValidationException.class);
    }

    @Test
    void shouldSetErrorOutputsOnFailure() throws Exception {
        injectMockClient();
        when(mockClient.getBusinessProcess(any())).thenThrow(new WorkdayException("Not found", 404, false));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(false);
        assertThat(outputs.get("errorMessage")).isNotNull();
        assertThat(outputs.get("errorMessage").toString()).contains("Not found");
    }

    @Test
    void shouldPopulateAllOutputFields() throws Exception {
        injectMockClient();
        when(mockClient.getBusinessProcess(any())).thenReturn(
                new GetBusinessProcessResult("In Progress", "Review", "{\"ok\":true}"));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(true);
        assertThat(outputs.get("processStatus")).isNotNull();
        assertThat(outputs.get("currentStep")).isNotNull();
        assertThat(outputs.get("responseBody")).isNotNull();
    }
}
