package at.rest.mappers;


import at.rest.dtos.RegisterUserDTO;
import at.rest.models.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface UserMapper {

   // @Mapping(target = "dateOfBirth", source = "dateOfBirth", qualifiedByName = "parseDateOfBirth")
    User toEntity(RegisterUserDTO dto);

//    @Named("parseDateOfBirth")
//    default LocalDate parseDateOfBirth(String dateOfBirth) {
//        if (dateOfBirth == null || dateOfBirth.trim().isEmpty()) return null;
//        try {
//            // ISO-Format
//            return LocalDate.parse(dateOfBirth);
//        } catch (DateTimeParseException e) {
//            // anderes Format
//            return LocalDate.parse(dateOfBirth, DateTimeFormatter.ofPattern("dd.MM.yyyy"));
//        }
//    }
}
