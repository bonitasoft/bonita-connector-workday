package com.bonitasoft.connectors.workday;

import lombok.extern.slf4j.Slf4j;

/**
 * Retrieves a Workday business process status by process ID.
 */
@Slf4j
public class GetBusinessProcessConnector extends AbstractWorkdayConnector {

    static final String INPUT_CLIENT_ID = "clientId";
    static final String INPUT_CLIENT_SECRET = "clientSecret";
    static final String INPUT_REFRESH_TOKEN = "refreshToken";
    static final String INPUT_TENANT_ALIAS = "tenantAlias";
    static final String INPUT_BASE_PATH = "basePath";
    static final String INPUT_TOKEN_ENDPOINT = "tokenEndpoint";
    static final String INPUT_CONNECT_TIMEOUT = "connectTimeout";
    static final String INPUT_READ_TIMEOUT = "readTimeout";
    static final String INPUT_PROCESS_ID = "processId";

    static final String OUTPUT_PROCESS_STATUS = "processStatus";
    static final String OUTPUT_CURRENT_STEP = "currentStep";
    static final String OUTPUT_RESPONSE_BODY = "responseBody";

    @Override
    protected WorkdayConfiguration buildConfiguration() {
        return WorkdayConfiguration.builder()
                .clientId(readStringInput(INPUT_CLIENT_ID))
                .clientSecret(readStringInput(INPUT_CLIENT_SECRET))
                .refreshToken(readStringInput(INPUT_REFRESH_TOKEN))
                .tenantAlias(readStringInput(INPUT_TENANT_ALIAS))
                .basePath(readStringInput(INPUT_BASE_PATH))
                .tokenEndpoint(readStringInput(INPUT_TOKEN_ENDPOINT))
                .connectTimeout(readIntegerInput(INPUT_CONNECT_TIMEOUT, 30000))
                .readTimeout(readIntegerInput(INPUT_READ_TIMEOUT, 60000))
                .processId(readStringInput(INPUT_PROCESS_ID))
                .build();
    }

    @Override
    protected void validateConfiguration(WorkdayConfiguration config) {
        super.validateConfiguration(config);
        if (config.getProcessId() == null || config.getProcessId().isBlank()) {
            throw new IllegalArgumentException("processId is mandatory");
        }
    }

    @Override
    protected void doExecute() throws WorkdayException {
        log.info("Executing Get Business Process connector for processId={}", configuration.getProcessId());
        GetBusinessProcessResult result = client.getBusinessProcess(configuration);
        setOutputParameter(OUTPUT_PROCESS_STATUS, result.processStatus());
        setOutputParameter(OUTPUT_CURRENT_STEP, result.currentStep());
        setOutputParameter(OUTPUT_RESPONSE_BODY, result.responseBody());
        log.info("Get Business Process connector executed successfully, status={}", result.processStatus());
    }
}
