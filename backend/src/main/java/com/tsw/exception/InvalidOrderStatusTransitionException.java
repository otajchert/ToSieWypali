package com.tsw.exception;

public class InvalidOrderStatusTransitionException extends ApiException {

    public InvalidOrderStatusTransitionException(String message) {
        super(ApiErrorCode.INVALID_ORDER_STATUS_TRANSITION, message);
    }
}
