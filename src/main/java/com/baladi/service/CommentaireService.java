package com.baladi.service;

import com.baladi.dto.CommentaireRequest;
import com.baladi.model.Commentaire;
import com.baladi.model.Signalement;
import com.baladi.model.User;
import com.baladi.repository.CommentaireRepository;
import com.baladi.repository.SignalementRepository;
import com.baladi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service pour gérer les commentaires sur les signalements.
 * Contient toute la logique métier relative aux commentaires.
 */
@Service
public class CommentaireService {

    @Autowired
    private CommentaireRepository commentaireRepository;

    @Autowired
    private SignalementRepository signalementRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Récupère l'utilisateur actuellement connecté.
     * Utilise le SecurityContextHolder (ThreadLocal) pour accéder à l'authentification.
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    /**
     * Ajoute un commentaire sur un signalement.
     * @param signalementId L'ID du signalement
     * @param request Les données du commentaire
     * @return Le commentaire créé
     */
    public Commentaire addCommentaire(Long signalementId, CommentaireRequest request) {
        // Vérifier que le signalement existe
        Signalement signalement = signalementRepository.findById(signalementId)
                .orElseThrow(() -> new RuntimeException("Signalement non trouvé"));
        
        // Récupérer l'utilisateur connecté
        User user = getCurrentUser();
        
        // Créer le commentaire
        Commentaire commentaire = new Commentaire();
        commentaire.setContenu(request.getContenu());
        commentaire.setDate(LocalDateTime.now());
        commentaire.setUser(user);
        commentaire.setSignalement(signalement);
        
        return commentaireRepository.save(commentaire);
    }

    /**
     * Récupère tous les commentaires d'un signalement.
     * @param signalementId L'ID du signalement
     * @return Liste des commentaires
     */
    public List<Commentaire> getCommentairesForSignalement(Long signalementId) {
        // Vérifier que le signalement existe
        if (!signalementRepository.existsById(signalementId)) {
            throw new RuntimeException("Signalement non trouvé");
        }
        
        Signalement signalement = new Signalement();
        signalement.setId(signalementId);
        
        return commentaireRepository.findBySignalement(signalement);
    }

    /**
     * Récupère un commentaire par son ID.
     * @param commentaireId L'ID du commentaire
     * @return Le commentaire trouvé
     */
    public Optional<Commentaire> getCommentaireById(Long commentaireId) {
        return commentaireRepository.findById(commentaireId);
    }

    /**
     * Supprime un commentaire (seulement l'auteur ou un admin peut).
     * @param commentaireId L'ID du commentaire
     */
    public void deleteCommentaire(Long commentaireId) {
        Commentaire commentaire = commentaireRepository.findById(commentaireId)
                .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
        
        User currentUser = getCurrentUser();
        
        // Vérifier que l'utilisateur actuel est l'auteur ou un admin
        if (!commentaire.getUser().getId().equals(currentUser.getId()) && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Vous n'avez pas le droit de supprimer ce commentaire");
        }
        
        commentaireRepository.deleteById(commentaireId);
    }

    /**
     * Met à jour le contenu d'un commentaire.
     * @param commentaireId L'ID du commentaire
     * @param request Les nouvelles données
     * @return Le commentaire mis à jour
     */
    public Commentaire updateCommentaire(Long commentaireId, CommentaireRequest request) {
        Commentaire commentaire = commentaireRepository.findById(commentaireId)
                .orElseThrow(() -> new RuntimeException("Commentaire non trouvé"));
        
        User currentUser = getCurrentUser();
        
        // Vérifier que l'utilisateur actuel est l'auteur ou un admin
        if (!commentaire.getUser().getId().equals(currentUser.getId()) && 
            !currentUser.getRole().name().equals("ADMIN")) {
            throw new RuntimeException("Vous n'avez pas le droit de modifier ce commentaire");
        }
        
        commentaire.setContenu(request.getContenu());
        return commentaireRepository.save(commentaire);
    }
}
