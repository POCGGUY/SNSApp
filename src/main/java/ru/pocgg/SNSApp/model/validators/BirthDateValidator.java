package ru.pocgg.SNSApp.model.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.pocgg.SNSApp.model.annotations.validations.ValidBirthDate;

import java.time.*;

public class BirthDateValidator implements ConstraintValidator<ValidBirthDate, LocalDate> {
    private int minAge;
    private int maxAge;

    @Override
    public void initialize(ValidBirthDate constraintAnnotation) {
        this.minAge = ValidBirthDate.minAge;
        this.maxAge = ValidBirthDate.maxAge;
    }

    @Override
    public boolean isValid(LocalDate birthDate, ConstraintValidatorContext context) {
        if (birthDate == null) {
            return false;
        }
        LocalDate today = LocalDate.now();
        Period period = Period.between(birthDate, today);
        int age = period.getYears();

        return age >= minAge && age <= maxAge;
    }
}
