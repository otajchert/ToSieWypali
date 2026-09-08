package com.tsw.exception;

public class DuplicateOrderItemException extends ApiException {

    public DuplicateOrderItemException() {
        super(ApiErrorCode.DUPLICATE_ORDER_ITEM, "Zamówienie zawiera powtórzony produkt");
    }
}
