package com.example.service;

import com.example.model.Reservation;
import java.util.Objects;
import java.util.function.Consumer;
import javax.persistence.OptimisticLockException;

public class ReservationRetryHandler {
    private final ReservationService service;
    private final int maxTentatives;

    public ReservationRetryHandler(ReservationService service) {
        this(service, 3);
    }

    public ReservationRetryHandler(ReservationService service, int maxTentatives) {
        this.service = Objects.requireNonNull(service, "service");
        if (maxTentatives < 1) throw new IllegalArgumentException("maxTentatives doit être positif");
        this.maxTentatives = maxTentatives;
    }

    /**
     * La modification est réappliquée à une version fraîche après chaque conflit.
     * Le Consumer doit être reproductible (par exemple, un setter avec la valeur voulue).
     */
    public Reservation modifierAvecRetry(Reservation versionInitiale, Consumer<Reservation> modification) {
        Objects.requireNonNull(versionInitiale, "versionInitiale");
        Objects.requireNonNull(modification, "modification");
        if (versionInitiale.getId() == null) throw new IllegalArgumentException("Réservation sans identifiant");
        Reservation candidate = versionInitiale;
        for (int tentative = 1; tentative <= maxTentatives; tentative++) {
            modification.accept(candidate);
            try {
                return service.modifierReservation(candidate);
            } catch (OptimisticLockException e) {
                if (tentative == maxTentatives) throw e;
                System.out.println("Conflit détecté, nouvelle tentative " + (tentative + 1));
                candidate = service.trouverParId(versionInitiale.getId());
                if (candidate == null) throw new IllegalArgumentException("Réservation supprimée pendant le retry");
            }
        }
        throw new IllegalStateException("Aucune tentative effectuée");
    }
}
