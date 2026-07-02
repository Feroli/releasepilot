package com.releasepilot.promotion.domain;

public class DomainException extends RuntimeException {
    private final DomainErrorCode code;

    public DomainException(DomainErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public DomainErrorCode code() {
        return code;
    }
}

