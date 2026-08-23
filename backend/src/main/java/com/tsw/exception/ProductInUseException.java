package com.tsw.exception;

public class ProductInUseException extends ApiException {

    public ProductInUseException() {
        this("Nie można usunąć produktu użytego w zamówieniu");
    }

    public ProductInUseException(String message) {
        super(ApiErrorCode.PRODUCT_IN_USE, message);
    }
}
