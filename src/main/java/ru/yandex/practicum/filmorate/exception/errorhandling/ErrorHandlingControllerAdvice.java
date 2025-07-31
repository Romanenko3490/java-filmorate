package ru.yandex.practicum.filmorate.exception.errorhandling;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.util.List;
import java.util.stream.Collectors;


@Slf4j
@ControllerAdvice
public class ErrorHandlingControllerAdvice {

    @ResponseBody
    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ViolationErrorResponse handleConstraintViolationException(ConstraintViolationException ex) {
        final List<Violation> violations = ex.getConstraintViolations().stream()
                .map(violation -> new Violation(
                        violation.getPropertyPath().toString(),
                        violation.getMessage()
                ))
                .collect(Collectors.toList());
        log.error("Constraint Violations Found ({}) : {}", violations.size(), violations);
        return new ViolationErrorResponse(violations);
    }

    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ViolationErrorResponse handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        final List<Violation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(violation -> new Violation(
                        violation.getField().toString(),
                        violation.getDefaultMessage()
                ))
                .collect(Collectors.toList());
        log.error("Validation  Failed for {} field : {}", violations.size(), violations);
        return new ViolationErrorResponse(violations);
    }

    @ResponseBody
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ViolationErrorResponse handleValidationException(final ValidationException ex) {
        log.error("Validation Failed", ex.getMessage());
        return new ViolationErrorResponse(List.of(new Violation("Validation Failed", ex.getMessage())));
    }

    // согласно тестов из Postman коллекции ответ должен содержать атрибут error
    @ResponseBody
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(final NotFoundException ex) {
        log.error("Not Found Exception", ex.getMessage());
        return new ErrorResponse("Not Found", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ViolationErrorResponse handleInternalServerException(final Throwable ex) {
        log.error("Internal Server Error", ex);
        return new ViolationErrorResponse(List.of(new Violation(null, ex.getMessage())));
    }

    @ResponseBody
    @ExceptionHandler(InternalServerException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ViolationErrorResponse handleInternalServerException(final InternalServerException ex) {
        log.error("Internal Server Error", ex);
        return new ViolationErrorResponse(List.of(new Violation(null, ex.getMessage())));
    }
}
