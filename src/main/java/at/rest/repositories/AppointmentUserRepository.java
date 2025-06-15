package at.rest.repositories;

import at.rest.models.compositeKeys.AppointmentUser;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class AppointmentUserRepository {

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public void save(AppointmentUser appointmentUser) {
        em.persist(appointmentUser);
    }
}
