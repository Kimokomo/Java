package at.rest.validators;

import at.rest.dtos.RegisterUserDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.Period;

public class AgeMatchesDateOfBirthValidator implements ConstraintValidator<AgeMatchesDateOfBirth, RegisterUserDTO> {
    @Override
    public boolean isValid(RegisterUserDTO dto, ConstraintValidatorContext constraintValidatorContext) {
        if (dto == null) return true;
        if (dto.getAge() == null || dto.getDateOfBirth() == null) return true;

        int calculatedAge = Period.between(dto.getDateOfBirth(), LocalDate.now()).getYears();
        return calculatedAge == dto.getAge();
    }
}
