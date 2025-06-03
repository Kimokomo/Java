package at.rest.servcie;

import at.rest.dtos.BuchDTO;
import at.rest.dtos.BuchPageDTO;
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

    public List<BuchDTO> getBooksPaginated(int page, int size) {
        return buchRepository.getBooksPaginated(page, size)
                .stream()
                .map(buchMapper::toDto)
                .collect(Collectors.toList());
    }

    public BuchPageDTO getBooksPaginatedWithCount(int page, int size) {
        List<BuchDTO> content = getBooksPaginated(page, size);
        long totalItems = countBooks();
        int totalPages = (int) Math.ceil((double) totalItems / size);

        BuchPageDTO pageDTO = new BuchPageDTO();
        pageDTO.setContent(content);
        pageDTO.setPage(page);
        pageDTO.setSize(size);
        pageDTO.setTotalItems(totalItems);
        pageDTO.setTotalPages(totalPages);

        return pageDTO;
    }

    public long countBooks() {
        return buchRepository.countBooks();
    }

}
