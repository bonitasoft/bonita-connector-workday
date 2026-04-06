package com.bonitasoft.connectors.workday;

public record GetBusinessProcessResult(String processStatus, String currentStep, String responseBody) {}
