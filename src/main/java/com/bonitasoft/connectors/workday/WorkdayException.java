package com.bonitasoft.connectors.workday;

/**
 * Typed exception for Workday HCM connector operations.
 */
public class WorkdayException extends Exception {

    private final int statusCode;
    private final boolean retryable;

    public WorkdayException(String message) {
        super(message);
        this.statusCode = -1;
        this.retryable = false;
    }

    public WorkdayException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = -1;
        this.retryable = false;
    }

    public WorkdayException(String message, int statusCode, boolean retryable) {
        super(message);
        this.statusCode = statusCode;
        this.retryable = retryable;
    }

    public WorkdayException(String message, int statusCode, boolean retryable, Throwable cause) {
        super(message, cause);
        this.statusCode = statusCode;
        this.retryable = retryable;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public boolean isRetryable() {
        return retryable;
    }
}
