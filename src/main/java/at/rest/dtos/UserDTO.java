package at.rest.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Builder
public class UserDTO {

    private String email;
    private String username;
    private String password;
    private String firstname;
    private String lastname;
    private Integer age;
    private LocalDate dateOfBirth;
}
