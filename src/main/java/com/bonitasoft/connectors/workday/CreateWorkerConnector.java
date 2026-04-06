package com.bonitasoft.connectors.workday;

import lombok.extern.slf4j.Slf4j;

/**
 * Creates (hires) a new worker in Workday.
 */
@Slf4j
public class CreateWorkerConnector extends AbstractWorkdayConnector {

    static final String INPUT_CLIENT_ID = "clientId";
    static final String INPUT_CLIENT_SECRET = "clientSecret";
    static final String INPUT_REFRESH_TOKEN = "refreshToken";
    static final String INPUT_TENANT_ALIAS = "tenantAlias";
    static final String INPUT_BASE_PATH = "basePath";
    static final String INPUT_TOKEN_ENDPOINT = "tokenEndpoint";
    static final String INPUT_CONNECT_TIMEOUT = "connectTimeout";
    static final String INPUT_READ_TIMEOUT = "readTimeout";
    static final String INPUT_WORKER_DATA = "workerData";
    static final String INPUT_HIRE_DATE = "hireDate";
    static final String INPUT_WORKER_TYPE = "workerType";

    static final String OUTPUT_WORKER_ID = "workerId";
    static final String OUTPUT_EMPLOYEE_ID = "employeeId";
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
                .workerData(readStringInput(INPUT_WORKER_DATA))
                .hireDate(readStringInput(INPUT_HIRE_DATE))
                .workerType(readStringInput(INPUT_WORKER_TYPE))
                .build();
    }

    @Override
    protected void validateConfiguration(WorkdayConfiguration config) {
        super.validateConfiguration(config);
        if (config.getWorkerData() == null || config.getWorkerData().isBlank()) {
            throw new IllegalArgumentException("workerData is mandatory");
        }
        if (config.getHireDate() == null || config.getHireDate().isBlank()) {
            throw new IllegalArgumentException("hireDate is mandatory");
        }
        if (config.getWorkerType() == null || config.getWorkerType().isBlank()) {
            throw new IllegalArgumentException("workerType is mandatory");
        }
    }

    @Override
    protected void doExecute() throws WorkdayException {
        log.info("Executing Create Worker connector");
        CreateWorkerResult result = client.createWorker(configuration);
        setOutputParameter(OUTPUT_WORKER_ID, result.workerId());
        setOutputParameter(OUTPUT_EMPLOYEE_ID, result.employeeId());
        setOutputParameter(OUTPUT_RESPONSE_BODY, result.responseBody());
        log.info("Create Worker connector executed successfully, workerId={}", result.workerId());
    }
}
