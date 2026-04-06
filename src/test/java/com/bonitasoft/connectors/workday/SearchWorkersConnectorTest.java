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
import java.util.List;
import java.util.Map;

@ExtendWith(MockitoExtension.class)
class SearchWorkersConnectorTest {

    @Mock
    private WorkdayClient mockClient;

    private SearchWorkersConnector connector;
    private Map<String, Object> inputs;

    @BeforeEach
    void setUp() {
        connector = new SearchWorkersConnector();
        inputs = new HashMap<>();
        inputs.put("clientId", "test-client-id");
        inputs.put("clientSecret", "test-client-secret");
        inputs.put("refreshToken", "test-refresh-token");
        inputs.put("tenantAlias", "test-tenant");
        inputs.put("connectTimeout", 30000);
        inputs.put("readTimeout", 60000);
        inputs.put("limit", 50);
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
        List<Map<String, Object>> workers = List.of(Map.of("id", "w-1"), Map.of("id", "w-2"));
        when(mockClient.searchWorkers(any())).thenReturn(
                new SearchWorkersResult("{\"data\":[]}", workers, 2));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(true);
        assertThat(outputs.get("totalCount")).isEqualTo(2);
        assertThat(outputs.get("workersList")).isNotNull();
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
        when(mockClient.searchWorkers(any())).thenThrow(new WorkdayException("Server error", 500, true));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(false);
        assertThat(outputs.get("errorMessage")).isNotNull();
    }

    @Test
    void shouldApplyDefaultsForNullOptionalInputs() throws Exception {
        inputs.remove("searchQuery");
        inputs.remove("wqlQuery");
        inputs.remove("offset");
        injectMockClient();
        when(mockClient.searchWorkers(any())).thenReturn(
                new SearchWorkersResult("{}", List.of(), 0));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(true);
    }

    @Test
    void shouldPopulateAllOutputFields() throws Exception {
        injectMockClient();
        List<Map<String, Object>> workers = List.of(Map.of("id", "w-1"));
        when(mockClient.searchWorkers(any())).thenReturn(
                new SearchWorkersResult("{\"total\":1}", workers, 1));
        connector.executeBusinessLogic();
        var outputs = connector.getOutputs();
        assertThat(outputs.get("success")).isEqualTo(true);
        assertThat(outputs.get("workers")).isNotNull();
        assertThat(outputs.get("workersList")).isNotNull();
        assertThat(outputs.get("totalCount")).isNotNull();
    }
}
