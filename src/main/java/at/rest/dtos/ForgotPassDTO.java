package at.rest.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPassDTO {
    @NotBlank(message = "E-Mail/Benutzername darf nicht leer sein.")
    private String emailOrUsername;
}
