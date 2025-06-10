package at.rest.validators;

import at.rest.dtos.RegisterUserDTO;
import at.rest.exceptions.ValidationException;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.LocalDate;
import java.time.Period;

@ApplicationScoped
public class UserValidator {

    public void validate(RegisterUserDTO dto) {

        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new ValidationException("email darf nicht leer sein");
        }

        if (dto.getUsername() == null || dto.getUsername().trim().isEmpty()) {
            throw new ValidationException("username darf nicht leer sein");
        }

        if (!isPasswordValid(dto.getPassword())) {
            throw new ValidationException("Passwort zu schwach – mindestens 8 Zeichen, ein Großbuchstabe, ein Kleinbuchstabe und eine Zahl erforderlich.");
        }

        if (dto.getFirstname() == null || dto.getFirstname().trim().length() < 2) {
            throw new ValidationException("Vorname muss mindestens 2 Zeichen lang sein.");
        }

        if (dto.getLastname() == null || dto.getLastname().trim().length() < 2) {
            throw new ValidationException("Nachname muss mindestens 2 Zeichen lang sein.");
        }

        if (dto.getAge() == null || dto.getAge() < 18) {
            throw new ValidationException("Alter muss mindestens 18 Jahre betragen.");
        }

        if (dto.getDateOfBirth() == null || dto.getDateOfBirth().isAfter(LocalDate.now())) {
            throw new ValidationException("Geburtsdatum darf nicht leer sein oder in der Zukunft liegen.");
        }

        int calculatedAge = Period.between(dto.getDateOfBirth(), LocalDate.now()).getYears();
        if (calculatedAge != dto.getAge()) {
            throw new ValidationException("Alter stimmt nicht mit dem Geburtsdatum überein.");
        }

    }

    public boolean isPasswordValid(String password) {
        if (password == null) return false;

        // Mindestlänge 8 Zeichen
        if (password.length() < 8) return false;

        // Muss mindestens einen Großbuchstaben, Kleinbuchstaben und Zahl enthalten
        boolean hasUpper = password.matches(".*[A-Z].*");
        boolean hasLower = password.matches(".*[a-z].*");
        boolean hasDigit = password.matches(".*\\d.*");

        return hasUpper && hasLower && hasDigit;
    }
}
