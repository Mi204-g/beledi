package com.baladi.controller;

import com.baladi.dto.SignalementDTO;
import com.baladi.model.Signalement;
import com.baladi.model.Statut;
import com.baladi.service.SignalementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controller REST pour la gestion des signalements civiques.
 *
 * Préfixe des routes : /api/signalements
 *
 * Résumé des endpoints :
 *   GET    /api/signalements           → public
 *   GET    /api/signalements/{id}      → public
 *   POST   /api/signalements           → citoyen connecté
 *   GET    /api/signalements/mes       → citoyen connecté
 *   PUT    /api/signalements/{id}/statut → admin seulement
 */
@RestController
@RequestMapping("/api/signalements")
@CrossOrigin(origins = "*")
public class SignalementController {

    @Autowired
    private SignalementService signalementService;

    // =========================================================================
    // GET /api/signalements — Liste tous les signalements (PUBLIC)
    // =========================================================================
    /**
     * Accessible sans token JWT.
     * Retourne la liste complète des signalements.
     */
    @GetMapping
    public ResponseEntity<List<Signalement>> getAllSignalements() {
        return ResponseEntity.ok(signalementService.getAllSignalements());
    }

    // =========================================================================
    // GET /api/signalements/mes — Mes signalements (CITOYEN CONNECTÉ)
    // =========================================================================
    /**
     * IMPORTANT : Cette route doit être AVANT /{id} pour éviter que Spring
     * interprète "mes" comme un identifiant numérique.
     *
     * @PreAuthorize("isAuthenticated()") : vérifie que l'utilisateur est connecté.
     * Si non connecté → 403 Forbidden (accès refusé).
     */
    @GetMapping("/mes")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Signalement>> getMesSignalements() {
        return ResponseEntity.ok(signalementService.getMesSignalements());
    }

    // =========================================================================
    // GET /api/signalements/{id} — Détail d'un signalement (PUBLIC)
    // =========================================================================
    /**
     * @PathVariable Long id : extrait l'identifiant de l'URL.
     * Ex: GET /api/signalements/42 → id = 42
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getSignalementById(@PathVariable Long id) {
        try {
            Signalement signalement = signalementService.getSignalementById(id);
            return ResponseEntity.ok(signalement);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }
    }

    // =========================================================================
    // POST /api/signalements — Créer un signalement (CITOYEN CONNECTÉ)
    // =========================================================================
    /**
     * Corps de la requête (JSON) :
     * {
     *   "titre": "Nid de poule dangereux",
     *   "description": "Grand trou sur la route principale, dangereux pour les motos",
     *   "categorie": "VOIRIE",
     *   "latitude": 18.0785,
     *   "longitude": -15.9654,
     *   "photoUrl": "https://xxx.supabase.co/storage/v1/..."  (optionnel)
     * }
     *
     * @PreAuthorize : seuls CITOYEN et ADMIN peuvent créer un signalement.
     */
    @PostMapping
    @PreAuthorize("hasRole('CITOYEN') or hasRole('ADMIN')")
    public ResponseEntity<?> createSignalement(@Valid @RequestBody SignalementDTO dto) {
        try {
            Signalement signalement = signalementService.createSignalement(dto);
            return ResponseEntity.ok(signalement); // 200 OK avec le signalement créé
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", e.getMessage())); // 400 Bad Request
        }
    }

    // =========================================================================
    // PUT /api/signalements/{id}/statut — Changer le statut (ADMIN SEULEMENT)
    // =========================================================================
    /**
     * Corps de la requête (JSON) :
     * {
     *   "statut": "EN_COURS"
     * }
     * Valeurs acceptées : EN_ATTENTE, EN_COURS, RESOLU
     *
     * @PreAuthorize("hasRole('ADMIN')") : seul l'admin peut changer le statut.
     */
    @PutMapping("/{id}/statut")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateStatut(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        try {
            // Convertit la chaîne reçue en enum Statut
            String statutStr = body.get("statut");
            if (statutStr == null || statutStr.isBlank()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("erreur", "Le champ 'statut' est obligatoire"));
            }
            Statut statut = Statut.valueOf(statutStr.toUpperCase().trim());
            Signalement signalement = signalementService.updateStatut(id, statut);
            return ResponseEntity.ok(signalement);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur",
                            "Statut invalide. Valeurs acceptées : EN_ATTENTE, EN_COURS, RESOLU"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", e.getMessage()));
        }
    }
}
