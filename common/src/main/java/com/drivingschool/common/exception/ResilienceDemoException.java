package com.drivingschool.common.exception;

public class ResilienceDemoException extends RuntimeException {
    public ResilienceDemoException(String message) {
        super(message);
    }

    public ResilienceDemoException(String message, Exception e) {
        super(message, e);
    }
}
