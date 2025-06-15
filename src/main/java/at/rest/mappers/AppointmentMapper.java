package at.rest.mappers;

import at.rest.dtos.AppointmentDTO;
import at.rest.models.entities.Appointment;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "cdi")
public interface AppointmentMapper {
    AppointmentDTO toDTO(Appointment appointment);
    List<AppointmentDTO> toDTOList(List<Appointment> appointments);
}
