package at.rest.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPassDTO {
    private String emailOrUsername;
}
