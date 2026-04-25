package com.baladi.controller;

import com.baladi.dto.CommentaireRequest;
import com.baladi.model.Commentaire;
import com.baladi.service.CommentaireService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Contrôleur REST pour gérer les commentaires sur les signalements.
 * Endpoints: /api/signalements/{id}/commentaires
 */
@RestController
@RequestMapping("/api/signalements")
public class CommentaireController {

    @Autowired
    private CommentaireService commentaireService;

    /**
     * GET /api/signalements/{signalementId}/commentaires
     * Récupère tous les commentaires d'un signalement (public).
     */
    @GetMapping("/{signalementId}/commentaires")
    public ResponseEntity<?> getCommentaires(@PathVariable Long signalementId) {
        try {
            List<Commentaire> commentaires = commentaireService.getCommentairesForSignalement(signalementId);
            return ResponseEntity.ok(Map.of(
                "signalementId", signalementId,
                "commentaires", commentaires,
                "total", commentaires.size()
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erreur", e.getMessage()));
        }
    }

    /**
     * GET /api/signalements/{signalementId}/commentaires/{commentaireId}
     * Récupère un commentaire spécifique (public).
     */
    @GetMapping("/{signalementId}/commentaires/{commentaireId}")
    public ResponseEntity<?> getCommentaire(
            @PathVariable Long signalementId,
            @PathVariable Long commentaireId) {
        try {
            Optional<Commentaire> commentaire = commentaireService.getCommentaireById(commentaireId);
            if (commentaire.isPresent()) {
                return ResponseEntity.ok(commentaire.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erreur", "Commentaire non trouvé"));
            }
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erreur", e.getMessage()));
        }
    }

    /**
     * POST /api/signalements/{signalementId}/commentaires
     * Ajoute un nouveau commentaire (authentifié).
     */
    @PostMapping("/{signalementId}/commentaires")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addCommentaire(
            @PathVariable Long signalementId,
            @Valid @RequestBody CommentaireRequest request) {
        try {
            Commentaire commentaire = commentaireService.addCommentaire(signalementId, request);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "message", "Commentaire ajouté avec succès",
                "commentaire", commentaire
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erreur", e.getMessage()));
        }
    }

    /**
     * PUT /api/signalements/{signalementId}/commentaires/{commentaireId}
     * Met à jour un commentaire (authentifié - auteur ou admin).
     */
    @PutMapping("/{signalementId}/commentaires/{commentaireId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateCommentaire(
            @PathVariable Long signalementId,
            @PathVariable Long commentaireId,
            @Valid @RequestBody CommentaireRequest request) {
        try {
            Commentaire commentaire = commentaireService.updateCommentaire(commentaireId, request);
            return ResponseEntity.ok(Map.of(
                "message", "Commentaire mis à jour avec succès",
                "commentaire", commentaire
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erreur", e.getMessage()));
        }
    }

    /**
     * DELETE /api/signalements/{signalementId}/commentaires/{commentaireId}
     * Supprime un commentaire (authentifié - auteur ou admin).
     */
    @DeleteMapping("/{signalementId}/commentaires/{commentaireId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteCommentaire(
            @PathVariable Long signalementId,
            @PathVariable Long commentaireId) {
        try {
            commentaireService.deleteCommentaire(commentaireId);
            return ResponseEntity.ok(Map.of("message", "Commentaire supprimé avec succès"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("erreur", e.getMessage()));
        }
    }
}
