package at.rest.validators;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = AgeMatchesDateOfBirthValidator.class)
@Target({ ElementType.TYPE })  // Klassenebene
@Retention(RetentionPolicy.RUNTIME)
public @interface AgeMatchesDateOfBirth {
    String message() default "Alter stimmt nicht mit dem Geburtsdatum überein.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
