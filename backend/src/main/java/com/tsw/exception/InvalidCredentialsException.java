package com.tsw.exception;

public class InvalidCredentialsException extends ApiException {

    public InvalidCredentialsException() {
        this("Nieprawidłowy adres e-mail lub hasło");
    }

    public InvalidCredentialsException(String message) {
        super(ApiErrorCode.INVALID_CREDENTIALS, message);
    }
}
