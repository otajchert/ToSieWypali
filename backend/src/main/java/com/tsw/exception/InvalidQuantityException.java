package com.tsw.exception;

public class InvalidQuantityException extends ApiException {

    public InvalidQuantityException(String message) {
        super(ApiErrorCode.INVALID_QUANTITY, message);
    }
}
