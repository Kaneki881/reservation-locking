package com.example;

import static org.junit.jupiter.api.Assertions.*;
import com.example.model.Reservation;
import com.example.service.ReservationRetryHandler;
import com.example.service.ReservationService;
import com.example.service.ReservationServiceImpl;
import java.time.LocalDateTime;
import javax.persistence.OptimisticLockException;
import org.junit.jupiter.api.Test;

class ReservationConcurrencyTest {
    @Test
    void conflitOptimisteEtRetry() {
        try (ReservationService service = new ReservationServiceImpl()) {
            Reservation initiale = service.creerReservation(new Reservation(
                "Salle A", LocalDateTime.of(2030, 1, 10, 9, 0), LocalDateTime.of(2030, 1, 10, 11, 0)));
            assertEquals(0, initiale.getVersion());
            assertEquals(1, service.listerReservations().size());

            Reservation tx1 = service.trouverParId(initiale.getId());
            Reservation tx2 = service.trouverParId(initiale.getId());
            tx1.setSalle("Salle B");
            Reservation premiereModification = service.modifierReservation(tx1);
            assertEquals(1, premiereModification.getVersion());
            System.out.println("Transaction 1 : modification réussie, version " + premiereModification.getVersion());

            tx2.setSalle("Salle C");
            assertThrows(OptimisticLockException.class, () -> service.modifierReservation(tx2));
            assertEquals("Salle B", service.trouverParId(initiale.getId()).getSalle());
            System.out.println("Transaction 2 : conflit optimiste détecté; la salle reste B.");

            Reservation apresRetry = new ReservationRetryHandler(service).modifierAvecRetry(
                tx2, r -> r.setSalle("Salle C"));
            assertEquals(2, apresRetry.getVersion());
            assertEquals("Salle C", service.trouverParId(initiale.getId()).getSalle());
            System.out.println("Transaction 2 : retry réussi, salle " + apresRetry.getSalle()
                + ", version " + apresRetry.getVersion());
        }
    }
}
