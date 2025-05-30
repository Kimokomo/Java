package at.rest.dtos;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor               // <- Für JSON-B (deserialization)
@AllArgsConstructor              // <- Für Lombok @Builder intern
@Builder
public class BuchDTO {
    private Long id;
    private String titel;
    private String autor;
    private int erscheinungsjahr;
    private String verfuegbar;
}
