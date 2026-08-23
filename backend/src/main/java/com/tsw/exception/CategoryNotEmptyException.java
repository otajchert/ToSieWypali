package com.tsw.exception;

public class CategoryNotEmptyException extends ApiException {

    private final long productCount;

    public CategoryNotEmptyException(long productCount) {
        this("Nie można usunąć kategorii zawierającej produkty", productCount);
    }

    public CategoryNotEmptyException(String message, long productCount) {
        super(ApiErrorCode.CATEGORY_NOT_EMPTY, message);
        this.productCount = productCount;
    }

    public long getProductCount() {
        return productCount;
    }
}
