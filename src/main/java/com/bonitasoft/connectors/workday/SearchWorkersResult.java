package com.bonitasoft.connectors.workday;

import java.util.List;
import java.util.Map;

public record SearchWorkersResult(String workers, List<Map<String, Object>> workersList, int totalCount) {}
