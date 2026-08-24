package com.tsw.exception;

public class LastShippingMethodException extends ApiException {

    public LastShippingMethodException() {
        super(
                ApiErrorCode.LAST_SHIPPING_METHOD_REQUIRED,
                "Nie można wyłączyć jedynej aktywnej metody dostawy"
        );
    }
}
