package com.example.service;

import com.example.model.Reservation;
import java.util.List;
import java.util.Objects;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class ReservationServiceImpl implements ReservationService {
    private final EntityManagerFactory emf;

    public ReservationServiceImpl() {
        this.emf = Persistence.createEntityManagerFactory("reservationPU");
    }

    @Override
    public Reservation creerReservation(Reservation reservation) {
        Objects.requireNonNull(reservation, "reservation");
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(reservation);
            em.getTransaction().commit();
            return reservation;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation modifierReservation(Reservation reservation) {
        Objects.requireNonNull(reservation, "reservation");
        if (reservation.getId() == null) throw new IllegalArgumentException("Réservation sans identifiant");
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            if (em.find(Reservation.class, reservation.getId()) == null) {
                throw new IllegalArgumentException("Réservation non trouvée : " + reservation.getId());
            }
            // merge compare la version du détaché à celle en base.
            Reservation managed = em.merge(reservation);
            em.flush();
            em.getTransaction().commit();
            return managed;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation trouverParId(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Reservation.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Reservation> listerReservations() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT r FROM Reservation r ORDER BY r.id", Reservation.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void close() { emf.close(); }
}
