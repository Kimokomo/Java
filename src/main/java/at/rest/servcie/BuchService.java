package at.rest.servcie;

import at.rest.dtos.BuchDTO;
import at.rest.mapper.BuchMapper;
import at.rest.model.Buch;
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

    public void saveBook(BuchDTO buch) {
        buchRepository.save(buchMapper.toEntity(buch));
    }

    public void deleteBookById(Long id) {
        buchRepository.deleteById(id);
    }

    public void updateBook(Long id, BuchDTO buchDto) {
        Buch existingBuch = buchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Buch nicht gefunden mit ID " + id));

        // Update Felder
        existingBuch.setTitel(buchDto.getTitel());
        existingBuch.setAutor(buchDto.getAutor());
        existingBuch.setErscheinungsjahr(buchDto.getErscheinungsjahr());
        existingBuch.setVerfuegbar(buchDto.getVerfuegbar());

        // Save = merge (update)
        buchRepository.save(existingBuch);
    }


}
