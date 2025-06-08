package at.rest.mappers;


import at.rest.dtos.BuchDTO;
import at.rest.models.Buch;
import org.mapstruct.Mapper;

@Mapper(componentModel = "cdi")
public interface BuchMapper {

    BuchDTO toDto(Buch buch);

    Buch toEntity(BuchDTO buchDTO);
}
