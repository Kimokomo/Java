package at.rest.dtos;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BuchDTO {
    private Long id;
    private String titel;
    private String autor;
    private int erscheinungsjahr;
    private String verfuegbar;
}
