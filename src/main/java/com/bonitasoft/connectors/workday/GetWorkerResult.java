package com.bonitasoft.connectors.workday;

import java.util.Map;

public record GetWorkerResult(String workerData, Map<String, Object> workerDataMap, String fullName, String employeeId) {}
