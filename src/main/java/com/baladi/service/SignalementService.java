package com.baladi.service;

import com.baladi.dto.SignalementDTO;
import com.baladi.model.*;
import com.baladi.repository.SignalementRepository;
import com.baladi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service gérant la logique métier des signalements civiques.
 *
 * Ce service est le "cerveau" de l'application : il orchestre les opérations
 * entre les controllers (qui reçoivent les requêtes HTTP) et les repositories
 * (qui exécutent les requêtes SQL).
 */
@Service
public class SignalementService {

    @Autowired
    private SignalementRepository signalementRepository;

    @Autowired
    private UserRepository userRepository;

    // =========================================================================
    // Méthode utilitaire privée
    // =========================================================================

    /**
     * Récupère l'utilisateur actuellement connecté en lisant le contexte
     * de sécurité Spring Security (rempli par JwtFilter à chaque requête).
     *
     * SecurityContextHolder est un ThreadLocal : chaque requête HTTP a son
     * propre contexte, donc cette méthode retourne toujours l'user de la requête courante.
     */
    private User getCurrentUser() {
        // Le "name" dans le contexte Spring Security est l'email (défini dans UserDetailsServiceImpl)
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur connecté introuvable en base"));
    }

    // =========================================================================
    // Routes publiques
    // =========================================================================

    /**
     * Retourne la liste de TOUS les signalements.
     * Accessible sans authentification (route publique).
     */
    public List<Signalement> getAllSignalements() {
        return signalementRepository.findAll();
    }

    /**
     * Retourne un signalement par son identifiant.
     * Lance une exception si l'ID n'existe pas en base.
     */
    public Signalement getSignalementById(Long id) {
        return signalementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Aucun signalement trouvé avec l'id : " + id));
    }

    // =========================================================================
    // Routes citoyens connectés
    // =========================================================================

    /**
     * Crée un nouveau signalement pour l'utilisateur connecté.
     *
     * Le statut est automatiquement mis à EN_ATTENTE (le citoyen ne peut pas
     * choisir le statut, c'est l'admin qui le change).
     */
    public Signalement createSignalement(SignalementDTO dto) {
        User currentUser = getCurrentUser();

        Signalement signalement = new Signalement();
        signalement.setTitre(dto.getTitre());
        signalement.setDescription(dto.getDescription());
        signalement.setPhotoUrl(dto.getPhotoUrl());
        signalement.setLatitude(dto.getLatitude());
        signalement.setLongitude(dto.getLongitude());
        signalement.setCategorie(dto.getCategorie());
        signalement.setStatut(Statut.EN_ATTENTE); // Toujours EN_ATTENTE à la création
        signalement.setDateCreation(LocalDateTime.now());
        signalement.setUser(currentUser); // Lie le signalement à l'utilisateur connecté

        return signalementRepository.save(signalement);
    }

    /**
     * Retourne uniquement les signalements créés par l'utilisateur connecté.
     * Chaque citoyen ne voit que ses propres signalements dans cet endpoint.
     */
    public List<Signalement> getMesSignalements() {
        User currentUser = getCurrentUser();
        return signalementRepository.findByUser(currentUser);
    }

    // =========================================================================
    // Routes admin
    // =========================================================================

    /**
     * Change le statut d'un signalement (réservé à l'admin).
     * Ex: passer de EN_ATTENTE à EN_COURS quand les travaux commencent.
     */
    public Signalement updateStatut(Long id, Statut nouveauStatut) {
        Signalement signalement = getSignalementById(id);
        signalement.setStatut(nouveauStatut);
        return signalementRepository.save(signalement); // UPDATE en base
    }

    /**
     * Retourne des statistiques agrégées pour le tableau de bord admin.
     *
     * Retourne :
     * - Nombre de signalements par statut (EN_ATTENTE, EN_COURS, RESOLU)
     * - Nombre de signalements par catégorie (VOIRIE, ELECTRICITE...)
     * - Total général
     */
    public Map<String, Object> getStatistiques() {
        Map<String, Object> stats = new HashMap<>();

        // --- Statistiques par statut ---
        Map<String, Long> parStatut = new HashMap<>();
        for (Statut statut : Statut.values()) {
            parStatut.put(statut.name(), signalementRepository.countByStatut(statut));
        }

        // --- Statistiques par catégorie ---
        Map<String, Long> parCategorie = new HashMap<>();
        for (Categorie categorie : Categorie.values()) {
            parCategorie.put(categorie.name(), signalementRepository.countByCategorie(categorie));
        }

        stats.put("parStatut", parStatut);
        stats.put("parCategorie", parCategorie);
        stats.put("total", signalementRepository.count());

        return stats;
    }
}
