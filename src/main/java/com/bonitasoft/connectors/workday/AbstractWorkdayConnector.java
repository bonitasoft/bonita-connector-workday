package com.bonitasoft.connectors.workday;

import lombok.extern.slf4j.Slf4j;
import org.bonitasoft.engine.connector.AbstractConnector;
import org.bonitasoft.engine.connector.ConnectorException;
import org.bonitasoft.engine.connector.ConnectorValidationException;

import java.util.List;
import java.util.Map;

/**
 * Abstract base connector for Workday HCM operations.
 * Handles common lifecycle: validation, connection, execution with error handling.
 */
@Slf4j
public abstract class AbstractWorkdayConnector extends AbstractConnector {

    protected static final String OUTPUT_SUCCESS = "success";
    protected static final String OUTPUT_ERROR_MESSAGE = "errorMessage";

    protected WorkdayConfiguration configuration;
    protected WorkdayClient client;

    @Override
    public void validateInputParameters() throws ConnectorValidationException {
        try {
            this.configuration = buildConfiguration();
            validateConfiguration(this.configuration);
        } catch (IllegalArgumentException e) {
            throw new ConnectorValidationException(this, e.getMessage());
        }
    }

    @Override
    public void connect() throws ConnectorException {
        try {
            this.client = new WorkdayClient(this.configuration);
            log.info("Workday HCM connector connected successfully");
        } catch (WorkdayException e) {
            throw new ConnectorException("Failed to connect: " + e.getMessage(), e);
        }
    }

    @Override
    public void disconnect() throws ConnectorException {
        this.client = null;
    }

    @Override
    protected void executeBusinessLogic() throws ConnectorException {
        try {
            doExecute();
            setOutputParameter(OUTPUT_SUCCESS, true);
        } catch (WorkdayException e) {
            log.error("Workday connector execution failed: {}", e.getMessage(), e);
            setOutputParameter(OUTPUT_SUCCESS, false);
            setOutputParameter(OUTPUT_ERROR_MESSAGE, e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error in Workday connector: {}", e.getMessage(), e);
            setOutputParameter(OUTPUT_SUCCESS, false);
            String detail = e.getClass().getSimpleName() + ": " + e.getMessage();
            if (e.getCause() != null) {
                detail += " caused by " + e.getCause().getClass().getSimpleName() + ": " + e.getCause().getMessage();
            }
            setOutputParameter(OUTPUT_ERROR_MESSAGE, "Unexpected error: " + detail);
        }
    }

    protected abstract void doExecute() throws WorkdayException;

    protected abstract WorkdayConfiguration buildConfiguration();

    /**
     * Validates common connection parameters.
     * Subclasses should call super and add operation-specific validation.
     */
    protected void validateConfiguration(WorkdayConfiguration config) {
        if (config.getClientId() == null || config.getClientId().isBlank()) {
            throw new IllegalArgumentException("clientId is mandatory");
        }
        if (config.getClientSecret() == null || config.getClientSecret().isBlank()) {
            throw new IllegalArgumentException("clientSecret is mandatory");
        }
        if (config.getRefreshToken() == null || config.getRefreshToken().isBlank()) {
            throw new IllegalArgumentException("refreshToken is mandatory");
        }
        if (config.getTenantAlias() == null || config.getTenantAlias().isBlank()) {
            throw new IllegalArgumentException("tenantAlias is mandatory");
        }
    }

    protected String readStringInput(String name) {
        Object value = getInputParameter(name);
        return value != null ? value.toString() : null;
    }

    protected String readStringInput(String name, String defaultValue) {
        String value = readStringInput(name);
        return (value != null && !value.isBlank()) ? value : defaultValue;
    }

    protected Boolean readBooleanInput(String name, boolean defaultValue) {
        Object value = getInputParameter(name);
        return value != null ? (Boolean) value : defaultValue;
    }

    protected Integer readIntegerInput(String name, int defaultValue) {
        Object value = getInputParameter(name);
        return value != null ? ((Number) value).intValue() : defaultValue;
    }

    /**
     * Expose output parameters for testing (package-private).
     */
    Map<String, Object> getOutputs() {
        return getOutputParameters();
    }
}
