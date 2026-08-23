package com.tsw.exception;

public class InsufficientStockException extends ApiException {

    public InsufficientStockException(String message) {
        super(ApiErrorCode.INSUFFICIENT_STOCK, message);
    }
}
