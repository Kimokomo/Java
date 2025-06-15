package at.rest.services.appointment;

import at.rest.dtos.AdminAppointmentDto;
import at.rest.models.views.AdminAppointmentOverview;
import at.rest.repositories.AdminAppointmentOverviewRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class AdminAppointmentService {
    @Inject
    AdminAppointmentOverviewRepository adminAppointmentOverviewRepository;

    public List<AdminAppointmentDto> getAllAppointments() {
        List<AdminAppointmentOverview> entities = adminAppointmentOverviewRepository.findAll();
        return entities.stream()
                .map(entity -> AdminAppointmentDto.builder()
                        .id(entity.getId())
                        .userId(entity.getUserId())
                        .username(entity.getUsername())
                        .email(entity.getEmail())
                        .firstname(entity.getFirstname())
                        .lastname(entity.getLastname())
                        .age(entity.getAge())
                        .appointmentId(entity.getAppointmentId())
                        .appointmentDateTime(entity.getAppointmentDateTime())
                        .description(entity.getDescription())
                        .note(entity.getNote())
                        .appointmentStatus(entity.getAppointmentStatus())
                        .confirmationStatus(entity.getConfirmationStatus())
                        .build())
                .collect(Collectors.toList());
    }
}
