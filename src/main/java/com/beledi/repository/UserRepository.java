package com.beledi.repository;

import com.beledi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository JPA pour l'entité User.
 *
 * JpaRepository<User, Long> fournit automatiquement :
 *   - save(), findById(), findAll(), delete(), count()...
 *
 * Les méthodes ci-dessous sont générées par Spring Data JPA
 * d'après leur nom (pas besoin d'écrire de SQL !).
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Recherche un utilisateur par son email → utilisé lors de la connexion
    // SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // Vérifie si un email est déjà enregistré → utilisé à l'inscription
    // SELECT COUNT(*) > 0 FROM users WHERE email = ?
    boolean existsByEmail(String email);
}
