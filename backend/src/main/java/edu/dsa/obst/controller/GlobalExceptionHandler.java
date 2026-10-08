package edu.dsa.obst.controller;

import edu.dsa.obst.model.ErrorResponse;
import edu.dsa.obst.service.ValidationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/** Turns input problems into 400 responses with readable messages. */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Our own validation errors (wrong lengths, duplicates, bad frequencies, ...). */
    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(ValidationException e) {
        return new ErrorResponse(400, e.getErrors());
    }

    /** The body is not valid JSON, or "keys"/"frequencies" is not an array. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadable(HttpMessageNotReadableException e) {
        return new ErrorResponse(400, List.of(
                "Request body must be JSON like {\"keys\": [10, 20, 30], \"frequencies\": [3, 5, 2]}."));
    }
}
