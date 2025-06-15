package at.rest.services.appointment;

import at.rest.dtos.AppointmentDTO;
import at.rest.enums.AppointmentStatus;
import at.rest.enums.ConfirmationStatus;
import at.rest.exceptions.AppointmentBookingException;
import at.rest.mappers.AppointmentMapper;
import at.rest.models.entities.Appointment;
import at.rest.models.compositeKeys.AppointmentUser;
import at.rest.models.compositeKeys.AppointmentUserId;
import at.rest.models.entities.User;
import at.rest.repositories.AppointmentRepository;
import at.rest.repositories.AppointmentUserRepository;
import at.rest.services.user.UserService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class AppointmentService {

    @Inject
    private AppointmentRepository appointmentRepository;

    @Inject
    private AppointmentUserRepository appointmentUserRepository;

    @Inject
    AppointmentMapper appointmentMapper;

    @Inject
    private UserService userService;


    public List<AppointmentDTO> findAppointmentDTOsByDate(LocalDate date) {
        List<Appointment> appointments = appointmentRepository.findByDate(date);
        return appointmentMapper.toDTOList(appointments);
    }

    @Transactional
    public AppointmentDTO bookAppointment(String username, AppointmentDTO appointmentDTO) {

        User user = findUserOrThrow(username);
        Appointment appointment = findAvailableAppointmentOrThrow(appointmentDTO.getId());
        checkIfUserAlreadyBooked(appointment, user);

        AppointmentUser appointmentUser = AppointmentUser.builder()
                .id(new AppointmentUserId(appointment.getId(), user.getId()))
                .appointment(appointment)
                .user(user)
                .confirmationStatus(ConfirmationStatus.PENDING)
                .build();

        int updatedRows = appointmentRepository.reduceSpotIfAvailable(appointment.getId());
        if (updatedRows == 0) {
            throw new AppointmentBookingException("Keine Plätze mehr verfügbar");
        }

        appointmentUserRepository.save(appointmentUser);
        return appointmentMapper.toDTO(appointmentRepository.findById(appointment.getId()));
    }

    private Appointment findAvailableAppointmentOrThrow(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId);
        if (appointment == null) {
            throw new AppointmentBookingException("Termin nicht gefunden");
        }
        if (!AppointmentStatus.AVAILABLE.equals(appointment.getStatus())) {
            throw new AppointmentBookingException("Termin nicht verfügbar");
        }
        return appointment;
    }

    private User findUserOrThrow(String username) {
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new AppointmentBookingException("Benutzer nicht gefunden");
        }
        return user;
    }

    private void checkIfUserAlreadyBooked(Appointment appointment, User user) {
        boolean alreadyBooked = appointment.getAppointmentUsers().stream()
                .anyMatch(link -> link.getUser().getId().equals(user.getId()));
        if (alreadyBooked) {
            throw new AppointmentBookingException("Du hast diesen Termin bereits gebucht");
        }
    }
}
