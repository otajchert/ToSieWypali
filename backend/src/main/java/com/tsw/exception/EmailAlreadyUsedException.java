package com.tsw.exception;

public class EmailAlreadyUsedException extends ApiException {

    public EmailAlreadyUsedException() {
        this("Adres e-mail jest już zajęty");
    }

    public EmailAlreadyUsedException(String message) {
        super(ApiErrorCode.EMAIL_ALREADY_USED, message);
    }
}
