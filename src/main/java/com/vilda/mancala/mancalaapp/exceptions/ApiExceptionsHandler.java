package com.vilda.mancala.mancalaapp.exceptions;

import com.vilda.mancala.mancalaapp.client.spec.model.Error;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionsHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Error> notFoundExceptionHandler(NotFoundException ex) {
        Error error = new Error();
        error.setMessage(ex.getMessage());
        error.setCode(404);

        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Error> badRequestExceptionHandler(BadRequestException ex) {
        Error error = new Error();
        error.setMessage(ex.getMessage());
        error.setCode(400);

        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
}
