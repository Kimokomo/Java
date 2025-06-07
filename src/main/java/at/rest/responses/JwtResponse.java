package at.rest.responses;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;

}
