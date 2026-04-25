package com.baladi.repository;

import com.baladi.model.Commentaire;
import com.baladi.model.Signalement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository JPA pour l'entité Commentaire.
 */
@Repository
public interface CommentaireRepository extends JpaRepository<Commentaire, Long> {

    // Récupère tous les commentaires d'un signalement donné
    // SELECT * FROM commentaires WHERE signalement_id = ?
    List<Commentaire> findBySignalement(Signalement signalement);
}
