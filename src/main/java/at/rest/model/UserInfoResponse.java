package at.rest.model;


import jakarta.json.bind.annotation.JsonbProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UserInfoResponse {

    @JsonbProperty("username")
    private String username;

    @JsonbProperty("role")
    private String role;
}
