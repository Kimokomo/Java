package at.rest.dtos;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminAppointmentDto {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private String firstname;
    private String lastname;
    private Integer age;
    private Long appointmentId;
    private LocalDateTime appointmentDateTime;
    private String description;
    private String note;
    private String appointmentStatus;
    private String confirmationStatus;
}
