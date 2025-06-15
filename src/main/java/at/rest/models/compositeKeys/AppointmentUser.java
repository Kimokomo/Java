package at.rest.models.compositeKeys;

import at.rest.enums.ConfirmationStatus;
import at.rest.models.entities.Appointment;
import at.rest.models.entities.User;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "user_appointments")
public class AppointmentUser {

    @EmbeddedId
    private AppointmentUserId id;

    @ManyToOne
    @MapsId("appointmentId")
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ConfirmationStatus confirmationStatus;
}
