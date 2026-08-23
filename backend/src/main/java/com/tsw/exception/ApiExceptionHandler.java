package com.tsw.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String INVALID_VALUE = "Nieprawidłowa wartość";

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        return validationProblem(validationErrors(exception.getBindingResult()));
    }

    @ExceptionHandler(BindException.class)
    public ProblemDetail handleBinding(BindException exception) {
        return validationProblem(validationErrors(exception.getBindingResult()));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException exception) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        for (ConstraintViolation<?> violation : exception.getConstraintViolations()) {
            addError(errors, violation.getPropertyPath().toString(), violation.getMessage());
        }
        return validationProblem(errors);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableRequest() {
        return ApiProblemDetails.create(
                ApiErrorCode.INVALID_REQUEST,
                "Nie udało się odczytać danych żądania"
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException exception) {
        ProblemDetail problem = ApiProblemDetails.create(
                ApiErrorCode.INVALID_REQUEST,
                "Parametr żądania ma nieprawidłowy format"
        );
        problem.setProperty("errors", Map.of(exception.getName(), List.of(INVALID_VALUE)));
        return problem;
    }

    @ExceptionHandler({ServletRequestBindingException.class, MissingServletRequestPartException.class})
    public ProblemDetail handleMissingRequestValue() {
        return ApiProblemDetails.create(
                ApiErrorCode.INVALID_REQUEST,
                "Brakuje wymaganych danych żądania"
        );
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ProblemDetail handleMaxUploadSize() {
        return ApiProblemDetails.create(
                ApiErrorCode.FILE_TOO_LARGE,
                "Przesłany plik jest zbyt duży"
        );
    }

    @ExceptionHandler(MultipartException.class)
    public ProblemDetail handleMultipartRequest() {
        return ApiProblemDetails.create(
                ApiErrorCode.INVALID_REQUEST,
                "Nie udało się odczytać przesłanych plików"
        );
    }

    @ExceptionHandler(CategoryNotEmptyException.class)
    public ProblemDetail handleCategoryNotEmpty(CategoryNotEmptyException exception) {
        ProblemDetail problem = problem(exception);
        problem.setProperty("productCount", exception.getProductCount());
        return problem;
    }

    @ExceptionHandler(StorageOperationException.class)
    public ProblemDetail handleStorageOperation(
            StorageOperationException exception,
            HttpServletRequest request
    ) {
        LOGGER.error(
                "Storage operation failed for {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );
        return problem(exception);
    }

    @ExceptionHandler(OrderConfigurationException.class)
    public ProblemDetail handleOrderConfiguration(
            OrderConfigurationException exception,
            HttpServletRequest request
    ) {
        LOGGER.error(
                "Order configuration error for {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );
        return problem(exception);
    }

    @ExceptionHandler(ApiException.class)
    public ProblemDetail handleApiException(ApiException exception) {
        return problem(exception);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataConflict(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        LOGGER.warn(
                "Data integrity conflict for {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );
        return ApiProblemDetails.create(
                ApiErrorCode.DATA_CONFLICT,
                "Nie można wykonać operacji z powodu konfliktu danych"
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ProblemDetail handleAuthentication() {
        return ApiProblemDetails.create(
                ApiErrorCode.UNAUTHORIZED,
                "Wymagane jest zalogowanie"
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied() {
        return ApiProblemDetails.create(
                ApiErrorCode.FORBIDDEN,
                "Brak uprawnień do wykonania tej operacji"
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ProblemDetail handleNoResource() {
        return ApiProblemDetails.create(
                ApiErrorCode.RESOURCE_NOT_FOUND,
                "Nie znaleziono zasobu"
        );
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ProblemDetail handleMethodNotAllowed() {
        return ApiProblemDetails.create(
                ApiErrorCode.METHOD_NOT_ALLOWED,
                "Ta metoda nie jest obsługiwana dla wskazanego adresu"
        );
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ProblemDetail handleUnsupportedMediaType() {
        return ApiProblemDetails.create(
                ApiErrorCode.UNSUPPORTED_MEDIA_TYPE,
                "Format danych żądania nie jest obsługiwany"
        );
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ProblemDetail handleNotAcceptable() {
        return ApiProblemDetails.create(
                ApiErrorCode.NOT_ACCEPTABLE,
                "Nie można przygotować odpowiedzi w wymaganym formacie"
        );
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception exception, HttpServletRequest request) {
        LOGGER.error(
                "Unhandled exception for {} {}",
                request.getMethod(),
                request.getRequestURI(),
                exception
        );
        return ApiProblemDetails.create(
                ApiErrorCode.INTERNAL_ERROR,
                "Wystąpił nieoczekiwany błąd"
        );
    }

    private ProblemDetail validationProblem(Map<String, List<String>> errors) {
        ProblemDetail problem = ApiProblemDetails.create(
                ApiErrorCode.VALIDATION_FAILED,
                "Dane żądania są nieprawidłowe"
        );
        problem.setProperty("errors", errors);
        return problem;
    }

    private Map<String, List<String>> validationErrors(BindingResult bindingResult) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        bindingResult.getFieldErrors().forEach(error ->
                addError(errors, error.getField(), error.getDefaultMessage())
        );
        bindingResult.getGlobalErrors().forEach(error ->
                addError(errors, "_global", error.getDefaultMessage())
        );
        return errors;
    }

    private void addError(Map<String, List<String>> errors, String field, String message) {
        String errorMessage = message == null || message.isBlank() ? INVALID_VALUE : message;
        List<String> fieldErrors = errors.computeIfAbsent(field, ignored -> new ArrayList<>());
        if (!fieldErrors.contains(errorMessage)) {
            fieldErrors.add(errorMessage);
        }
    }

    private ProblemDetail problem(ApiException exception) {
        return ApiProblemDetails.create(exception.getCode(), exception.getMessage());
    }
}
