package at.rest.mapper;


import at.rest.dtos.RegisterUserDTO;
import at.rest.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Mapper(componentModel = "cdi")
public interface UserMapper {

    @Mapping(target = "dateOfBirth", source = "dateOfBirth", qualifiedByName = "parseDateOfBirth")
    User toEntity(RegisterUserDTO dto);

    @Named("parseDateOfBirth")
    default LocalDate parseDateOfBirth(String dateOfBirth) {
        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) return null;
        try {
            // Versuch ISO-Format
            return LocalDate.parse(dateOfBirth);
        } catch (DateTimeParseException e) {
            // Versuch anderes Format
            return LocalDate.parse(dateOfBirth, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        }
    }
}
