package com.vilda.mancala.mancalaapp.util.validators;

import com.vilda.mancala.mancalaapp.util.validation.validators.ValidNameValidator;
import com.vilda.mancala.mancalaapp.util.validation.validators.ValidUserName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintValidatorContext;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class ValidNameValidatorTest {

    @Mock
    private ConstraintValidatorContext validatorContext;
    private ValidNameValidator validNameValidator;

    @BeforeEach
    void setUp() {
        validNameValidator = new ValidNameValidator();
    }

    @Test
    void shouldReturnTrueWhenUserNameIsValid() {
        ValidUserName validUserName = mock(ValidUserName.class);
        String userName = "userNameAlphaChars";

        validNameValidator.initialize(validUserName);

        assertThat(validNameValidator.isValid(userName, validatorContext), is(Boolean.TRUE));
    }

    @Test
    void shouldReturnFalseWhenUserNameIsEmpty() {
        ValidUserName validUserName = mock(ValidUserName.class);
        validNameValidator.initialize(validUserName);

        assertThat(validNameValidator.isValid("", validatorContext), is(Boolean.FALSE));
    }

    @Test
    void shouldReturnFalseWhenUserNameHasNotUnicodeChars() {
        ValidUserName validUserName = mock(ValidUserName.class);
        String userName = "userName@alp#";

        validNameValidator.initialize(validUserName);

        assertThat(validNameValidator.isValid(userName, validatorContext), is(Boolean.FALSE));
    }

}
