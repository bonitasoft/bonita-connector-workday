package com.bonitasoft.connectors.workday;

import lombok.extern.slf4j.Slf4j;

/**
 * Searches workers in Workday by search query, WQL, or supervisory organization.
 */
@Slf4j
public class SearchWorkersConnector extends AbstractWorkdayConnector {

    static final String INPUT_CLIENT_ID = "clientId";
    static final String INPUT_CLIENT_SECRET = "clientSecret";
    static final String INPUT_REFRESH_TOKEN = "refreshToken";
    static final String INPUT_TENANT_ALIAS = "tenantAlias";
    static final String INPUT_BASE_PATH = "basePath";
    static final String INPUT_TOKEN_ENDPOINT = "tokenEndpoint";
    static final String INPUT_CONNECT_TIMEOUT = "connectTimeout";
    static final String INPUT_READ_TIMEOUT = "readTimeout";
    static final String INPUT_SEARCH_QUERY = "searchQuery";
    static final String INPUT_WQL_QUERY = "wqlQuery";
    static final String INPUT_SUPERVISORY_ORGANIZATION = "supervisoryOrganization";
    static final String INPUT_LIMIT = "limit";
    static final String INPUT_OFFSET = "offset";

    static final String OUTPUT_WORKERS = "workers";
    static final String OUTPUT_WORKERS_LIST = "workersList";
    static final String OUTPUT_TOTAL_COUNT = "totalCount";

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
                .searchQuery(readStringInput(INPUT_SEARCH_QUERY))
                .wqlQuery(readStringInput(INPUT_WQL_QUERY))
                .supervisoryOrganization(readStringInput(INPUT_SUPERVISORY_ORGANIZATION))
                .limit(readIntegerInput(INPUT_LIMIT, 50))
                .offset(getInputParameter(INPUT_OFFSET) != null ? readIntegerInput(INPUT_OFFSET, 0) : null)
                .build();
    }

    @Override
    protected void doExecute() throws WorkdayException {
        log.info("Executing Search Workers connector");
        SearchWorkersResult result = client.searchWorkers(configuration);
        setOutputParameter(OUTPUT_WORKERS, result.workers());
        setOutputParameter(OUTPUT_WORKERS_LIST, result.workersList());
        setOutputParameter(OUTPUT_TOTAL_COUNT, result.totalCount());
        log.info("Search Workers connector executed successfully, found {} workers", result.totalCount());
    }
}
