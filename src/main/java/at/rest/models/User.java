package at.rest.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @SequenceGenerator(name = "user_seq", sequenceName = "user_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "google_id", unique = true)
    private String googleId;

    private String firstname;
    private String lastname;
    private Integer age;
    private boolean confirmed;

    @Column(name = "confirmation_token")
    private String confirmationToken;

    @Column(name = "birthdate")
    private LocalDate dateOfBirth;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_plain")
    private String password;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String role; // "user", "admin", "superadmin" // besser enum
}
