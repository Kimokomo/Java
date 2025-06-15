package at.rest.repositories;

import at.rest.models.entities.Appointment;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TypedQuery;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;

@ApplicationScoped
public class AppointmentRepository {
    @PersistenceContext
    private EntityManager em;

    public List<Appointment> findAll() {
        TypedQuery<Appointment> query = em.createQuery("SELECT a FROM Appointment a", Appointment.class);
        return query.getResultList();
    }

    public List<Appointment> findByDate(LocalDate date) {
        TypedQuery<Appointment> query = em.createQuery("SELECT a FROM Appointment a WHERE a.date = :date", Appointment.class);
        query.setParameter("date", date);
        return query.getResultList();
    }

    @Transactional
    public void saveNew(Appointment appointment) {
        if (appointment.getId() != null) {
            throw new IllegalArgumentException("New appointment must not have an ID.");
        }
        em.persist(appointment);
    }

    @Transactional
    public void update(Appointment appointment) {
        if (appointment.getId() == null) {
            throw new IllegalArgumentException("Cannot update appointment without ID.");
        }
        em.merge(appointment);
    }

    @Transactional
    public void saveOrUpdate(Appointment appointment) {
        if (appointment.getId() == null) {
            em.persist(appointment);
        } else {
            em.merge(appointment);
        }
    }

    public Appointment findById(Long id) {
        return em.find(Appointment.class, id);
    }

    @Transactional
    public int reduceSpotIfAvailable(Long appointmentId) {
        // Erst atomar -1 machen, wenn noch Plätze vorhanden
        Query query = em.createQuery(
                "UPDATE Appointment a SET a.spotsLeft = a.spotsLeft - 1 WHERE a.id = :id AND a.spotsLeft > 0"
        );
        query.setParameter("id", appointmentId);
        return query.executeUpdate();
    }

}
