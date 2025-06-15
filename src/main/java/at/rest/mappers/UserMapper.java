package at.rest.mappers;


import at.rest.dtos.RegisterUserDTO;
import at.rest.dtos.UserDTO;
import at.rest.models.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface UserMapper {

    User toEntity(RegisterUserDTO dto);
    UserDTO toDTO(User user);
}
