package com.vilda.mancala.mancalaapp.util.validation.validators;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Constraint(validatedBy = ValidNameValidator.class)
public @interface ValidUserName {
    String message() default "${validatedValue} is invalid name!!";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
