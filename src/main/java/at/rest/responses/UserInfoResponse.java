package at.rest.responses;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
public class UserInfoResponse {

    private String username;
    private String role;
    private Date tokenExpiration;

}
