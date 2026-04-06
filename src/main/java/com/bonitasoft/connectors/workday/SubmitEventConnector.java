package com.bonitasoft.connectors.workday;

import lombok.extern.slf4j.Slf4j;

/**
 * Submits a business process event (Compensation Change, Termination, Transfer, Promotion, Job Change) in Workday.
 */
@Slf4j
public class SubmitEventConnector extends AbstractWorkdayConnector {

    static final String INPUT_CLIENT_ID = "clientId";
    static final String INPUT_CLIENT_SECRET = "clientSecret";
    static final String INPUT_REFRESH_TOKEN = "refreshToken";
    static final String INPUT_TENANT_ALIAS = "tenantAlias";
    static final String INPUT_BASE_PATH = "basePath";
    static final String INPUT_TOKEN_ENDPOINT = "tokenEndpoint";
    static final String INPUT_CONNECT_TIMEOUT = "connectTimeout";
    static final String INPUT_READ_TIMEOUT = "readTimeout";
    static final String INPUT_EVENT_TYPE = "eventType";
    static final String INPUT_WORKER_ID = "workerId";
    static final String INPUT_EVENT_DATA = "eventData";
    static final String INPUT_EFFECTIVE_DATE = "effectiveDate";
    static final String INPUT_REASON = "reason";

    static final String OUTPUT_PROCESS_ID = "processId";
    static final String OUTPUT_PROCESS_STATUS = "processStatus";
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
                .eventType(readStringInput(INPUT_EVENT_TYPE))
                .workerId(readStringInput(INPUT_WORKER_ID))
                .eventData(readStringInput(INPUT_EVENT_DATA))
                .effectiveDate(readStringInput(INPUT_EFFECTIVE_DATE))
                .reason(readStringInput(INPUT_REASON))
                .build();
    }

    @Override
    protected void validateConfiguration(WorkdayConfiguration config) {
        super.validateConfiguration(config);
        if (config.getEventType() == null || config.getEventType().isBlank()) {
            throw new IllegalArgumentException("eventType is mandatory");
        }
        if (config.getWorkerId() == null || config.getWorkerId().isBlank()) {
            throw new IllegalArgumentException("workerId is mandatory");
        }
        if (config.getEventData() == null || config.getEventData().isBlank()) {
            throw new IllegalArgumentException("eventData is mandatory");
        }
        if (config.getEffectiveDate() == null || config.getEffectiveDate().isBlank()) {
            throw new IllegalArgumentException("effectiveDate is mandatory");
        }
    }

    @Override
    protected void doExecute() throws WorkdayException {
        log.info("Executing Submit Event connector, eventType={}, workerId={}",
                configuration.getEventType(), configuration.getWorkerId());
        SubmitEventResult result = client.submitEvent(configuration);
        setOutputParameter(OUTPUT_PROCESS_ID, result.processId());
        setOutputParameter(OUTPUT_PROCESS_STATUS, result.processStatus());
        setOutputParameter(OUTPUT_RESPONSE_BODY, result.responseBody());
        log.info("Submit Event connector executed successfully, processId={}", result.processId());
    }
}
