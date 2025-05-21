package ru.pocgg.SNSApp.model.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.ValidationException;
import ru.pocgg.SNSApp.model.annotations.validations.StringValidBirthDate;

import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Collections;

public class StringBirthDateValidator implements ConstraintValidator<StringValidBirthDate, String> {
    private int minAge;
    private int maxAge;

    @Override
    public void initialize(StringValidBirthDate constraintAnnotation) {
        this.minAge = StringValidBirthDate.minAge;
        this.maxAge = StringValidBirthDate.maxAge;
    }

    @Override
    public boolean isValid(String birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) {
            return true;
        }
        LocalDate today = LocalDate.now();
        Period period;
        try {
            period = Period.between(LocalDate.parse(birthDate), today);
        } catch (DateTimeParseException ex){
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("wrong birthdate format, right one is: " +
                    "(yyyy-mm-dd)").addConstraintViolation();
            return false;
        }
        int age = period.getYears();

        return age >= minAge && age <= maxAge;
    }
}
