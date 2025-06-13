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

    @Column(name = "confirmation_token")
    private String confirmationToken;
    @Column(name = "confirmed_mail")
    private boolean confirmed;
    @Column(name = "google_confirmed_mail")
    private boolean googleConfirmed;

    @Column(name = "forgot_password_token")
    private String forgotPasswordToken;
    @Column(name = "forgot_password_token_expiry_time_and_date")
    private LocalDateTime forgotPasswordTokenExpiry;

    @Column(name = "birthdate")
    private LocalDate dateOfBirth;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Transient
    @Column(name = "password_plain")
    private String password;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "time_stamp")
    private LocalDateTime tstamp;
}
