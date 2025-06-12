package at.rest.models;

import at.rest.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;


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

    @Column(name = "forgot_Password_Token")
    private String forgotPasswordToken;
    @Column(name = "forgot_Password_Token_Expiry_Time_and_date")
    private LocalDateTime forgotPasswordTokenExpiry;

    @Column(name = "confirmation_token")
    private String confirmationToken;

    @Column(name = "birthdate")
    private LocalDate dateOfBirth;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "password_plain")
    @Transient
    private String password;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    private LocalDateTime tstamp;
}
