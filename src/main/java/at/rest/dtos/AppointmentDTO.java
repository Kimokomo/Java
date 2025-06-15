package at.rest.dtos;

import at.rest.enums.AppointmentStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDTO {
    private Long id;
    private LocalDateTime dateTime;
    private LocalDate date;
    private String time;
    private String description;
    private AppointmentStatus status;
    private Integer spotsLeft;
    private String note;
}
