package com.tsw.exception;

import org.springframework.http.ProblemDetail;

public final class ApiProblemDetails {

    private ApiProblemDetails() {
    }

    public static ProblemDetail create(ApiErrorCode code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(code.status(), detail);
        problem.setTitle(code.title());
        problem.setProperty("code", code.name());
        return problem;
    }
}
