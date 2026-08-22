package com.tsw.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException exception) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));

        if (detail.isBlank()) {
            detail = "Dane żądania są nieprawidłowe";
        }

        return problem(HttpStatus.BAD_REQUEST, "Nieprawidłowe dane żądania", detail);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableRequest() {
        return problem(
                HttpStatus.BAD_REQUEST,
                "Nieprawidłowe dane żądania",
                "Nie udało się odczytać danych żądania"
        );
    }

    @ExceptionHandler(InvalidQuantityException.class)
    public ProblemDetail handleInvalidQuantity(InvalidQuantityException exception) {
        return problem(HttpStatus.BAD_REQUEST, "Nieprawidłowa ilość", exception.getMessage());
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException exception) {
        return problem(HttpStatus.NOT_FOUND, "Nie znaleziono zasobu", exception.getMessage());
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ProblemDetail handleInsufficientStock(InsufficientStockException exception) {
        return problem(HttpStatus.CONFLICT, "Niewystarczający stan magazynowy", exception.getMessage());
    }

    @ExceptionHandler(InvalidOrderStatusTransitionException.class)
    public ProblemDetail handleInvalidOrderStatusTransition(InvalidOrderStatusTransitionException exception) {
        return problem(HttpStatus.CONFLICT, "Niedozwolona zmiana statusu", exception.getMessage());
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
