package at.rest.repositories;

import at.rest.model.Buch;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.*;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class BuchRepository {

    @PersistenceContext(unitName = "myJpaUnit")
    private EntityManager em;

    public List<Buch> getAllBooks() {
        return em.createQuery("SELECT b FROM Buch b ORDER BY b.id", Buch.class).getResultList();
    }

    @Transactional
    public void save(Buch buch) {
        if (buch.getId() == null) {
            em.persist(buch); // Neues Buch
        } else {
            em.merge(buch);   // Existierendes Buch → Update
        }
    }

    @Transactional
    public void deleteById(Long id) {
        Buch buch = em.find(Buch.class, id);
        if (buch != null) {
            em.remove(buch);
        }
    }

    public Optional<Buch> findById(Long id) {
        Buch buch = em.find(Buch.class, id);
        return Optional.ofNullable(buch);
    }

    @Transactional
    public void saveAll(List<Buch> buecher) {
        for (Buch buch : buecher) {
            if (buch.getId() == null) {
                em.persist(buch);
            } else {
                em.merge(buch);
            }
        }
    }

    public List<Buch> getBooksPaginated(int page, int size) {
        int offset = (page > 0) ? (page - 1) * size : 0;
        return em.createQuery("SELECT b FROM Buch b ORDER BY b.id", Buch.class)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();
    }

    public long countBooks() {
        return em.createQuery("SELECT COUNT(b) FROM Buch b", Long.class)
                .getSingleResult();
    }


}
