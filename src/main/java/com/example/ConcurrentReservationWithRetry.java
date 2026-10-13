package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationRetryHandler;
import com.example.service.ReservationService;
import com.example.service.ReservationServiceImpl;
import java.time.LocalDateTime;
import javax.persistence.OptimisticLockException;

public class ConcurrentReservationWithRetry {
    public static void main(String[] args) {
        try (ReservationService service = new ReservationServiceImpl()) {
            Reservation initiale = service.creerReservation(new Reservation(
                "Salle A", LocalDateTime.of(2030, 2, 10, 9, 0), LocalDateTime.of(2030, 2, 10, 11, 0)));
            Reservation tx1 = service.trouverParId(initiale.getId());
            Reservation tx2 = service.trouverParId(initiale.getId());

            tx1.setSalle("Salle B");
            service.modifierReservation(tx1);
            System.out.println("Transaction 1 : modification réussie.");

            try {
                Reservation result = new ReservationRetryHandler(service).modifierAvecRetry(
                    tx2, r -> r.setSalle("Salle C"));
                System.out.println("Transaction 2 : modification réussie après retry (version "
                    + result.getVersion() + ").");
            } catch (OptimisticLockException e) {
                System.out.println("Transaction 2 : échec après toutes les tentatives.");
            }
        }
    }
}
