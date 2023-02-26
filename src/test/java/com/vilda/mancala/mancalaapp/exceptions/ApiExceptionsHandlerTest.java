package com.vilda.mancala.mancalaapp.exceptions;

import com.vilda.mancala.mancalaapp.client.spec.model.Error;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@ExtendWith(MockitoExtension.class)
public class ApiExceptionsHandlerTest {

    @InjectMocks
    private ApiExceptionsHandler apiExceptionsHandler;

    @Test
    void shouldCreateNotFoundExceptionHandler() {
        Error error = new Error();
        error.setCode(404);
        error.setMessage("test message for NotFoundException");

        ResponseEntity<Error> resp = apiExceptionsHandler.notFoundExceptionHandler(
                new NotFoundException("test message for NotFoundException"));

        assertThat(resp.getStatusCode(), is(HttpStatus.NOT_FOUND));
        assertThat(resp.getBody(), is(error));
    }

    @Test
    void shouldCreateBadRequestExceptionHandler() {
        Error error = new Error();
        error.setCode(400);
        error.setMessage("test message for BadRequestException");

        ResponseEntity<Error> resp = apiExceptionsHandler.badRequestExceptionHandler(
                new BadRequestException("test message for BadRequestException"));

        assertThat(resp.getStatusCode(), is(HttpStatus.BAD_REQUEST));
        assertThat(resp.getBody(), is(error));
    }
}
