package com.interviewforge.exception;

import com.interviewforge.auth.EmailAlreadyRegisteredException;
import com.interviewforge.auth.InvalidCredentialsException;
import com.interviewforge.auth.PasswordTooLongException;
import com.interviewforge.questionbank.ResourceNotFoundException;
import com.interviewforge.practice.InsufficientQuestionsException;
import com.interviewforge.workplacecase.DuplicateWorkplaceCaseSlugException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.stream.Collectors;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        String detail = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .distinct()
                .collect(Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, "Validation failed", detail, request);
    }

    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    ProblemDetail handleDuplicateEmail(EmailAlreadyRegisteredException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Email already registered", "An account already exists for this email.", request);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    ProblemDetail handleInvalidCredentials(InvalidCredentialsException exception, HttpServletRequest request) {
        return problem(HttpStatus.UNAUTHORIZED, "Invalid credentials", "The email or password is incorrect.", request);
    }

    @ExceptionHandler(PasswordTooLongException.class)
    ProblemDetail handlePasswordTooLong(PasswordTooLongException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid password", "The password is too long after UTF-8 encoding.", request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ProblemDetail handleDataConflict(DataIntegrityViolationException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Conflict", "The request conflicts with existing data.", request);
    }

    @ExceptionHandler(DuplicateWorkplaceCaseSlugException.class)
    ProblemDetail handleDuplicateCaseSlug(DuplicateWorkplaceCaseSlugException exception, HttpServletRequest request) {
        return problem(HttpStatus.CONFLICT, "Workplace case slug already exists", exception.getMessage(), request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ProblemDetail handleNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return problem(HttpStatus.NOT_FOUND, "Not found", exception.getMessage(), request);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail handleInvalidRequest(IllegalArgumentException exception, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", exception.getMessage(), request);
    }

    @ExceptionHandler(InsufficientQuestionsException.class)
    ProblemDetail handleInsufficientQuestions(InsufficientQuestionsException exception, HttpServletRequest request) {
        return problem(HttpStatus.UNPROCESSABLE_ENTITY, "Not enough matching questions", exception.getMessage(), request);
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}
