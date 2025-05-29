package at.rest.servcie;

import at.rest.dtos.BuchDTO;
import at.rest.mapper.BuchMapper;
import at.rest.repositories.BuchRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;


@ApplicationScoped
public class BuchService {

    @Inject
    private BuchMapper buchMapper;

    @Inject
    private BuchRepository buchRepository;

    public List<BuchDTO> getAllBooks() {
        return buchRepository.getAllBooks()
                .stream()
                .map(buchMapper::toDto)
                .collect(Collectors.toList());
    }
}
