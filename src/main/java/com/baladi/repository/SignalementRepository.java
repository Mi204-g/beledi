package com.baladi.repository;

import com.baladi.model.Categorie;
import com.baladi.model.Signalement;
import com.baladi.model.Statut;
import com.baladi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour l'entité Signalement.
 * Spring Data JPA génère automatiquement toutes ces requêtes SQL.
 */
@Repository
public interface SignalementRepository extends JpaRepository<Signalement, Long> {

    // Récupère tous les signalements d'un utilisateur donné
    // SELECT * FROM signalements WHERE user_id = ?
    List<Signalement> findByUser(User user);

    // Récupère tous les signalements avec un statut donné
    // SELECT * FROM signalements WHERE statut = ?
    List<Signalement> findByStatut(Statut statut);

    // Récupère tous les signalements d'une catégorie donnée
    // SELECT * FROM signalements WHERE categorie = ?
    List<Signalement> findByCategorie(Categorie categorie);

    // Compte les signalements par statut → pour les statistiques admin
    // SELECT COUNT(*) FROM signalements WHERE statut = ?
    long countByStatut(Statut statut);

    // Compte les signalements par catégorie → pour les statistiques admin
    // SELECT COUNT(*) FROM signalements WHERE categorie = ?
    long countByCategorie(Categorie categorie);
}
