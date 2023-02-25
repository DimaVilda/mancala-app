package com.vilda.mancala.mancalaapp.util.validation.validators;

import liquibase.repackaged.org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class ValidNameValidator implements ConstraintValidator<ValidUserName, String> {

   @Override
   public boolean isValid(String name, ConstraintValidatorContext context) {
      return StringUtils.isNotBlank(name) && StringUtils.isAlpha(name);
   }
}
