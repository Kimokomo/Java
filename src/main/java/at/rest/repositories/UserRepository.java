package at.rest.repositories;

import at.rest.model.User;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

import java.util.Optional;

@ApplicationScoped
public class UserRepository {

    @PersistenceContext(unitName = "myJpaUnit")
    private EntityManager em;

    public Optional<User> findByUsername(String username) {
        var query = em.createQuery("SELECT u FROM User u WHERE u.username = :username", User.class);
        query.setParameter("username", username);
        return query.getResultStream().findFirst();
    }

    public Optional<User> findByEmail(String email) {
        var query = em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class);
        query.setParameter("email", email);
        return query.getResultStream().findFirst();
    }

    public Optional<User> findByConfirmationToken(String token) {
        var query = em.createQuery("SELECT u FROM User u WHERE u.confirmationToken = :token", User.class);
        query.setParameter("token", token);
        return query.getResultStream().findFirst();
    }

    public Optional<User> findByGoogleId(String googleId) {
        var query = em.createQuery("SELECT u FROM User u WHERE u.googleId = :googleId", User.class);
        query.setParameter("googleId", googleId);
        return query.getResultStream().findFirst();
    }

    @Transactional
    public void saveNew(User user) {
        if (user.getId() != null) {
            throw new IllegalArgumentException("New user must not have an ID.");
        }
        em.persist(user);
    }

    @Transactional
    public void update(User user) {
        if (user.getId() == null) {
            throw new IllegalArgumentException("Cannot update user without ID.");
        }
        em.merge(user);
    }

    @Transactional
    public void saveOrUpdate(User user) {
        if (user.getId() == null) {
            em.persist(user);
        } else {
            em.merge(user);
        }
    }
}
