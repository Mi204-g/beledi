package com.baladi.controller;

import com.baladi.service.SignalementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST pour les fonctionnalités d'administration.
 *
 * Préfixe des routes : /api/admin
 *
 * TOUTES les routes de ce controller nécessitent le rôle ADMIN.
 * La double protection (SecurityConfig + @PreAuthorize) est une bonne pratique.
 */
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')") // Protège toutes les routes de cette classe
public class AdminController {

    @Autowired
    private SignalementService signalementService;

    // =========================================================================
    // GET /api/admin/statistiques — Tableau de bord admin
    // =========================================================================
    /**
     * Retourne des statistiques pour le tableau de bord administrateur.
     *
     * Exemple de réponse JSON :
     * {
     *   "total": 42,
     *   "parStatut": {
     *     "EN_ATTENTE": 15,
     *     "EN_COURS": 10,
     *     "RESOLU": 17
     *   },
     *   "parCategorie": {
     *     "VOIRIE": 12,
     *     "ELECTRICITE": 8,
     *     "DECHETS": 10,
     *     "EAU": 7,
     *     "AUTRE": 5
     *   }
     * }
     */
    @GetMapping("/statistiques")
    public ResponseEntity<Map<String, Object>> getStatistiques() {
        return ResponseEntity.ok(signalementService.getStatistiques());
    }
}
