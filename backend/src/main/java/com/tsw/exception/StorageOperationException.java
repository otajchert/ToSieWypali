package com.tsw.exception;

public class StorageOperationException extends ApiException {

    public StorageOperationException(Throwable cause) {
        this("Nie udało się wykonać operacji na pliku", cause);
    }

    public StorageOperationException(String message) {
        super(ApiErrorCode.STORAGE_OPERATION_FAILED, message);
    }

    public StorageOperationException(String message, Throwable cause) {
        super(ApiErrorCode.STORAGE_OPERATION_FAILED, message, cause);
    }
}
