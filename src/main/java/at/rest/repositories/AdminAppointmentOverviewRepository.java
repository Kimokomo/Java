package at.rest.repositories;

import at.rest.models.views.AdminAppointmentOverview;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

@ApplicationScoped
public class AdminAppointmentOverviewRepository {

    @PersistenceContext
    private EntityManager em;

    public List<AdminAppointmentOverview> findAll() {
        return em.createQuery("SELECT a FROM AdminAppointmentOverview a", AdminAppointmentOverview.class)
                .getResultList();
    }
}
