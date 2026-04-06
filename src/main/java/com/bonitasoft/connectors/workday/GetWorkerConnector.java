package com.bonitasoft.connectors.workday;

import lombok.extern.slf4j.Slf4j;

/**
 * Retrieves a worker's data from Workday by worker ID.
 */
@Slf4j
public class GetWorkerConnector extends AbstractWorkdayConnector {

    static final String INPUT_CLIENT_ID = "clientId";
    static final String INPUT_CLIENT_SECRET = "clientSecret";
    static final String INPUT_REFRESH_TOKEN = "refreshToken";
    static final String INPUT_TENANT_ALIAS = "tenantAlias";
    static final String INPUT_BASE_PATH = "basePath";
    static final String INPUT_TOKEN_ENDPOINT = "tokenEndpoint";
    static final String INPUT_CONNECT_TIMEOUT = "connectTimeout";
    static final String INPUT_READ_TIMEOUT = "readTimeout";
    static final String INPUT_WORKER_ID = "workerId";
    static final String INPUT_INCLUDE_PERSONAL_DATA = "includePersonalData";
    static final String INPUT_INCLUDE_EMPLOYMENT_DATA = "includeEmploymentData";

    static final String OUTPUT_WORKER_DATA = "workerData";
    static final String OUTPUT_WORKER_DATA_MAP = "workerDataMap";
    static final String OUTPUT_FULL_NAME = "fullName";
    static final String OUTPUT_EMPLOYEE_ID = "employeeId";

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
                .includePersonalData(readBooleanInput(INPUT_INCLUDE_PERSONAL_DATA, true))
                .includeEmploymentData(readBooleanInput(INPUT_INCLUDE_EMPLOYMENT_DATA, true))
                .build();
    }

    @Override
    protected void validateConfiguration(WorkdayConfiguration config) {
        super.validateConfiguration(config);
        if (config.getWorkerId() == null || config.getWorkerId().isBlank()) {
            throw new IllegalArgumentException("workerId is mandatory");
        }
    }

    @Override
    protected void doExecute() throws WorkdayException {
        log.info("Executing Get Worker connector for workerId={}", configuration.getWorkerId());
        GetWorkerResult result = client.getWorker(configuration);
        setOutputParameter(OUTPUT_WORKER_DATA, result.workerData());
        setOutputParameter(OUTPUT_WORKER_DATA_MAP, result.workerDataMap());
        setOutputParameter(OUTPUT_FULL_NAME, result.fullName());
        setOutputParameter(OUTPUT_EMPLOYEE_ID, result.employeeId());
        log.info("Get Worker connector executed successfully");
    }
}
