package com.tsw.exception;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(ApiErrorCode code, String message) {
        super(requireNotFoundCode(code), message);
    }

    private static ApiErrorCode requireNotFoundCode(ApiErrorCode code) {
        if (code.status().value() != 404) {
            throw new IllegalArgumentException("Resource error code must use HTTP 404");
        }
        return code;
    }
}
