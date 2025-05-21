package ru.pocgg.SNSApp.model.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.pocgg.SNSApp.model.annotations.validations.EnumValue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EnumValueValidator implements ConstraintValidator<EnumValue, String> {

    private Set<String> acceptedValues;
    private boolean ignoreCase;

    @Override
    public void initialize(EnumValue annotation) {
        ignoreCase = annotation.ignoreCase();
        Class<? extends Enum<?>> enumClass = annotation.enumClass();

        Enum<?>[] enumConstants = enumClass.getEnumConstants();

        acceptedValues = new HashSet<>();
        for (Enum<?> e : enumConstants) {
            acceptedValues.add(ignoreCase ? e.name().toLowerCase() : e.name());
        }
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String compare = ignoreCase ? value.toLowerCase() : value;
        return acceptedValues.contains(compare);
    }
}
