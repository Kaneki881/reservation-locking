package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationService;
import com.example.service.ReservationServiceImpl;
import java.time.LocalDateTime;
import javax.persistence.OptimisticLockException;

public class ConcurrentReservationSimulator {
    public static void main(String[] args) {
        try (ReservationService service = new ReservationServiceImpl()) {
            Reservation initiale = service.creerReservation(new Reservation(
                "Salle A", LocalDateTime.of(2030, 1, 10, 9, 0), LocalDateTime.of(2030, 1, 10, 11, 0)));

            // Deux lectures indépendantes donnent la même version initiale.
            Reservation tx1 = service.trouverParId(initiale.getId());
            Reservation tx2 = service.trouverParId(initiale.getId());

            tx1.setSalle("Salle B");
            service.modifierReservation(tx1);
            System.out.println("Transaction 1 : modification réussie.");

            try {
                tx2.setSalle("Salle C");
                service.modifierReservation(tx2);
                System.out.println("Transaction 2 : modification réussie.");
            } catch (OptimisticLockException e) {
                System.out.println("Transaction 2 : conflit optimiste détecté.");
            }
        }
    }
}
