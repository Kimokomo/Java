package at.rest.model;


import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "buecher", schema = "slice")
public class Buch {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "buch_seq")
    @SequenceGenerator(name = "buch_seq", sequenceName = "buch_sequence", allocationSize = 1)
    private Long id;

    private String titel;
    private String autor;
    private int erscheinungsjahr;
    private String verfuegbar;
}
