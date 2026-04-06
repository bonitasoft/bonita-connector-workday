package com.bonitasoft.connectors.workday;

import lombok.extern.slf4j.Slf4j;

/**
 * Updates an existing worker's data in Workday via PATCH.
 */
@Slf4j
public class UpdateWorkerConnector extends AbstractWorkdayConnector {

    static final String INPUT_CLIENT_ID = "clientId";
    static final String INPUT_CLIENT_SECRET = "clientSecret";
    static final String INPUT_REFRESH_TOKEN = "refreshToken";
    static final String INPUT_TENANT_ALIAS = "tenantAlias";
    static final String INPUT_BASE_PATH = "basePath";
    static final String INPUT_TOKEN_ENDPOINT = "tokenEndpoint";
    static final String INPUT_CONNECT_TIMEOUT = "connectTimeout";
    static final String INPUT_READ_TIMEOUT = "readTimeout";
    static final String INPUT_WORKER_ID = "workerId";
    static final String INPUT_UPDATE_DATA = "updateData";
    static final String INPUT_EFFECTIVE_DATE = "effectiveDate";

    static final String OUTPUT_WORKER_ID = "workerId";
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
                .workerId(readStringInput(INPUT_WORKER_ID))
                .updateData(readStringInput(INPUT_UPDATE_DATA))
                .effectiveDate(readStringInput(INPUT_EFFECTIVE_DATE))
                .build();
    }

    @Override
    protected void validateConfiguration(WorkdayConfiguration config) {
        super.validateConfiguration(config);
        if (config.getWorkerId() == null || config.getWorkerId().isBlank()) {
            throw new IllegalArgumentException("workerId is mandatory");
        }
        if (config.getUpdateData() == null || config.getUpdateData().isBlank()) {
            throw new IllegalArgumentException("updateData is mandatory");
        }
    }

    @Override
    protected void doExecute() throws WorkdayException {
        log.info("Executing Update Worker connector for workerId={}", configuration.getWorkerId());
        UpdateWorkerResult result = client.updateWorker(configuration);
        setOutputParameter(OUTPUT_WORKER_ID, result.workerId());
        setOutputParameter(OUTPUT_RESPONSE_BODY, result.responseBody());
        log.info("Update Worker connector executed successfully");
    }
}
