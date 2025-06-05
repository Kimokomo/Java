package at.rest.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDTO {
    private String email;
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private Integer age;
    private String dateOfBirth;
}
