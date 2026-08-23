package com.tsw.exception;

public class InvalidCategoryHierarchyException extends ApiException {

    public InvalidCategoryHierarchyException(String message) {
        super(ApiErrorCode.INVALID_CATEGORY_HIERARCHY, message);
    }
}
