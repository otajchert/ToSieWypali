package com.tsw.exception;

public class OrderConfigurationException extends ApiException {

    public OrderConfigurationException() {
        this("Brak wymaganej konfiguracji zamówień");
    }

    public OrderConfigurationException(Throwable cause) {
        this("Brak wymaganej konfiguracji zamówień", cause);
    }

    public OrderConfigurationException(String message) {
        super(ApiErrorCode.ORDER_CONFIGURATION_ERROR, message);
    }

    public OrderConfigurationException(String message, Throwable cause) {
        super(ApiErrorCode.ORDER_CONFIGURATION_ERROR, message, cause);
    }
}
