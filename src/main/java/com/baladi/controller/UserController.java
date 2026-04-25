package com.baladi.controller;

import com.baladi.dto.LoginRequest;
import com.baladi.dto.RegisterRequest;
import com.baladi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST pour l'authentification.
 *
 * Préfixe des routes : /api/auth
 *
 * @RestController = @Controller + @ResponseBody
 *   → toutes les méthodes retournent directement du JSON
 *
 * @CrossOrigin = autorise les requêtes depuis n'importe quelle origine
 *   (nécessaire pour que le frontend HTML/JS puisse appeler l'API)
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    // =========================================================================
    // POST /api/auth/register — Inscription d'un nouveau citoyen
    // =========================================================================
    /**
     * Corps de la requête (JSON) :
     * {
     *   "nom": "Ahmed Ould Mohamed",
     *   "email": "ahmed@example.com",
     *   "motDePasse": "monPassword123"
     * }
     *
     * @Valid déclenche la validation des annotations (@NotBlank, @Email...) du DTO.
     * Si la validation échoue, Spring retourne automatiquement un 400 Bad Request.
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            Map<String, Object> response = userService.register(request);
            return ResponseEntity.ok(response); // 200 OK
        } catch (RuntimeException e) {
            // Ex: email déjà utilisé
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", e.getMessage())); // 400 Bad Request
        }
    }

    // =========================================================================
    // POST /api/auth/login — Connexion et obtention du token JWT
    // =========================================================================
    /**
     * Corps de la requête (JSON) :
     * {
     *   "email": "ahmed@example.com",
     *   "motDePasse": "monPassword123"
     * }
     *
     * Réponse (JSON) en cas de succès :
     * {
     *   "token": "eyJhbGciOiJIUzI1NiJ9...",
     *   "type": "Bearer",
     *   "nom": "Ahmed Ould Mohamed",
     *   "email": "ahmed@example.com",
     *   "role": "CITOYEN"
     * }
     *
     * Le client doit sauvegarder le token et l'envoyer dans chaque requête :
     * Header : Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Map<String, Object> response = userService.login(request);
            return ResponseEntity.ok(response); // 200 OK
        } catch (Exception e) {
            // Ne pas révéler si c'est l'email ou le mot de passe qui est faux (sécurité)
            return ResponseEntity.badRequest()
                    .body(Map.of("erreur", "Email ou mot de passe incorrect")); // 400 Bad Request
        }
    }
}
