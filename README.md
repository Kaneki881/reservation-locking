# TP — Réservations et verrouillage optimiste

Projet Maven autonome utilisant JPA 2.2 (`javax.persistence`), Hibernate 5.4.30.Final et H2 2.3.232 en mémoire. Aucun serveur MySQL n'est nécessaire.

## Lancer

Dans PowerShell, depuis ce dossier :

```powershell
.\voir-reservations.ps1
```

Ou avec Maven disponible dans `PATH` :

```powershell
mvn test
```
Resultat 
Le test crée une réservation, lit deux copies avec la même version, valide la première modification, vérifie le conflit de la deuxième, puis réessaie cette deuxième modification après rechargement. La console montre également les requêtes SQL Hibernate contenant `where id=? and version=?`.

Les classes `com.example.ConcurrentReservationSimulator` et `com.example.ConcurrentReservationWithRetry` fournissent chacune une méthode `main` exécutable depuis un IDE.

Le service utilise `EntityManager.merge` sur l'objet détaché : recopier ses champs dans une entité fraîchement chargée ferait perdre sa version d'origine et masquerait le conflit. Le retry reçoit une modification reproductible (`Consumer<Reservation>`) à réappliquer à la version rechargée. Dans une application réelle, la décision de remplacer une valeur modifiée par quelqu'un d'autre doit relever de la règle métier.

L'inspection IntelliJ qui ne trouve pas la table est désactivée sur @Table : une base H2 en mémoire n'est visible que dans le processus Java qui l'a créée. MODE=LEGACY dans l'URL conserve la syntaxe d'identité générée par Hibernate 5.4 avec H2 2.3.232.
