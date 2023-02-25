package com.vilda.mancala.mancalaapp.util.validation.contributors;

import com.vilda.mancala.mancalaapp.client.spec.model.NewGameSetup;
import com.vilda.mancala.mancalaapp.util.validation.groups.NameGroup;
import com.vilda.mancala.mancalaapp.util.validation.validators.ValidUserName;
import org.hibernate.validator.cfg.GenericConstraintDef;
import org.hibernate.validator.spi.cfg.ConstraintMappingContributor;

import java.lang.annotation.Annotation;

public class NewGameSetupRequestConstraintMappingContributor implements ConstraintMappingContributor {

    @Override
    public void createConstraintMappings(ConstraintMappingBuilder constraintMappingBuilder) {
        constraintMappingBuilder.addConstraintMapping()
                .type(NewGameSetup.class)
                .field("playerOneName")
                .constraint(createConstraint(ValidUserName.class, NameGroup.class))
                .field("playerTwoName")
                .constraint(createConstraint(ValidUserName.class, NameGroup.class));
    }

    private <T extends Annotation> GenericConstraintDef<T> createConstraint(Class<T> annotation, Class<?> group) {
        return new GenericConstraintDef<>(annotation).groups(group);
    }
}
