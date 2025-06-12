package at.rest.dtos;

import at.rest.validators.AgeMatchesDateOfBirth;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AgeMatchesDateOfBirth
public class RegisterUserDTO {

    @NotBlank(message = "E-Mail darf nicht leer sein")
    @Email(message = "Ungültige E-Mail-Adresse")
    private String email;

    @NotBlank(message = "Benutzername darf nicht leer sein")
    private String username;

    @NotBlank(message = "Passwort darf nicht leer sein")
    @Size(min = 8, message = "Passwort muss mindestens 8 Zeichen lang sein")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
            message = "Passwort muss mindestens einen Großbuchstaben, einen Kleinbuchstaben und eine Zahl enthalten"
    )
    private String password;

    @NotBlank(message = "Vorname darf nicht leer sein")
    @Size(min = 2, message = "Vorname muss mindestens 2 Zeichen lang sein")
    private String firstname;

    @NotBlank(message = "Nachname darf nicht leer sein")
    @Size(min = 2, message = "Nachname muss mindestens 2 Zeichen lang sein")
    private String lastname;

    @NotNull(message = "Alter darf nicht leer sein")
    @Min(value = 18, message = "Alter muss mindestens 18 Jahre betragen")
    private Integer age;

    @NotNull(message = "Geburtsdatum darf nicht leer sein")
    @PastOrPresent(message = "Geburtsdatum darf nicht in der Zukunft liegen")
    private LocalDate dateOfBirth;
}
