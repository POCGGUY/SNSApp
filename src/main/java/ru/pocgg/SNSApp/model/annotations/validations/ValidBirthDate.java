package ru.pocgg.SNSApp.model.annotations.validations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.pocgg.SNSApp.model.validators.BirthDateValidator;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = BirthDateValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidBirthDate {
    int minAge = 14;
    int maxAge = 150;
    String message() default "Birth date must be between " + minAge + " and " + maxAge;
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
