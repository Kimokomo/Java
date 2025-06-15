package at.rest.models.views;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Immutable;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Entity
@Table(name = "v_admin_appointment_overview")
@Immutable
public class AdminAppointmentOverview {

    // ROW_NUMBER() in der View als eindeutige ID
    @Id
    @Column(name = "id")
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "username")
    private String username;

    @Column(name = "email")
    private String email;

    @Column(name = "firstname")
    private String firstname;

    @Column(name = "lastname")
    private String lastname;

    @Column(name = "age")
    private Integer age;

    @Column(name = "appointment_id")
    private Long appointmentId;

    @Column(name = "appointment_date_time")
    private LocalDateTime appointmentDateTime;

    @Column(name = "description")
    private String description;

    @Column(name = "note")
    private String note;

    @Column(name = "appointment_status")
    private String appointmentStatus;

    @Column(name = "confirmation_status")
    private String confirmationStatus;
}
