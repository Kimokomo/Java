package at.rest.validators;

import at.rest.dtos.RegisterUserDTO;
import at.rest.exceptions.DuplicateException;
import at.rest.exceptions.ValidationException;
import at.rest.mapper.UserMapper;
import at.rest.model.User;
import at.rest.servcie.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.time.Period;

@ApplicationScoped
public class UserValidator {

    @Inject
    UserService userService;

    @Inject
    UserMapper userMapper;

    public void validate(RegisterUserDTO dto) {
        // DTO in Entity umwandeln
        User user = userMapper.toEntity(dto);

        if (userService.findByUsername(user.getUsername()).isPresent()) {
            throw new DuplicateException("username already exists");
        }

        if (userService.findByEmail(user.getEmail()).isPresent()) {
            throw new DuplicateException("email already exists");
        }

        if (!userService.isPasswordValid(user.getPassword())) {
            throw new ValidationException("Passwort zu schwach – mindestens 8 Zeichen, ein Großbuchstabe, ein Kleinbuchstabe und eine Zahl erforderlich.");
        }

        if (user.getFirstname() == null || user.getFirstname().trim().length() < 2) {
            throw new ValidationException("Vorname muss mindestens 2 Zeichen lang sein.");
        }

        if (user.getLastname() == null || user.getLastname().trim().length() < 2) {
            throw new ValidationException("Nachname muss mindestens 2 Zeichen lang sein.");
        }

        if (user.getAge() == null || user.getAge() < 18) {
            throw new ValidationException("Alter muss mindestens 18 Jahre betragen.");
        }

        if (user.getDateOfBirth() == null || user.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new ValidationException("Geburtsdatum darf nicht leer sein oder in der Zukunft liegen.");
        }

        int calculatedAge = Period.between(user.getDateOfBirth(), LocalDate.now()).getYears();
        if (calculatedAge != user.getAge()) {
            throw new ValidationException("Alter stimmt nicht mit dem Geburtsdatum überein.");
        }

    }
}
