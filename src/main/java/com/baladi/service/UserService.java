package com.baladi.service;

import com.baladi.dto.LoginRequest;
import com.baladi.dto.RegisterRequest;
import com.baladi.model.Role;
import com.baladi.model.User;
import com.baladi.repository.UserRepository;
import com.baladi.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Service gérant la logique métier liée aux utilisateurs :
 * - Inscription (register)
 * - Connexion (login) et génération du JWT
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // BCryptPasswordEncoder

    @Autowired
    private AuthenticationManager authenticationManager; // Vérifie email + mot de passe

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    // =========================================================================
    // Inscription
    // =========================================================================

    /**
     * Inscrit un nouveau citoyen dans la base de données.
     *
     * Étapes :
     * 1. Vérifie que l'email n'est pas déjà utilisé
     * 2. Hashe le mot de passe avec BCrypt
     * 3. Sauvegarde l'utilisateur avec le rôle CITOYEN
     *
     * @param request Données d'inscription (nom, email, motDePasse)
     * @return Map contenant un message de confirmation
     */
    public Map<String, Object> register(RegisterRequest request) {
        // Étape 1 : Vérification de l'unicité de l'email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Un compte existe déjà avec l'email : " + request.getEmail());
        }

        // Étape 2 : Construction de l'entité User
        User user = new User();
        user.setNom(request.getNom());
        user.setEmail(request.getEmail().toLowerCase().trim()); // Normalise l'email
        // BCrypt hashe le mot de passe : "monPassword" → "$2a$10$..." (hash irréversible)
        user.setMotDePasse(passwordEncoder.encode(request.getMotDePasse()));
        user.setRole(Role.CITOYEN); // Par défaut, tout nouvel inscrit est CITOYEN
        user.setDateInscription(LocalDateTime.now());

        // Étape 3 : Sauvegarde en base (INSERT dans Supabase)
        userRepository.save(user);

        // Réponse JSON retournée au client
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Inscription réussie ! Vous pouvez maintenant vous connecter.");
        response.put("nom", user.getNom());
        response.put("email", user.getEmail());
        return response;
    }

    // =========================================================================
    // Connexion
    // =========================================================================

    /**
     * Authentifie un utilisateur et retourne un token JWT.
     *
     * Étapes :
     * 1. Spring Security vérifie email + mot de passe (via authenticationManager)
     * 2. On génère un token JWT signé
     * 3. On retourne le token + infos utilisateur
     *
     * @param request Données de connexion (email, motDePasse)
     * @return Map contenant le token JWT et les infos de l'utilisateur
     */
    public Map<String, Object> login(LoginRequest request) {
        // Étape 1 : Vérification de l'email et du mot de passe
        // Si les credentials sont incorrects, Spring lance une exception
        // (BadCredentialsException) que l'on catch dans le controller
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getMotDePasse()
                )
        );

        // Étape 2 : Charge les UserDetails (pour la génération du JWT)
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());

        // Étape 3 : Génère le token JWT
        String token = jwtUtil.generateToken(userDetails);

        // Récupère l'utilisateur pour inclure ses infos dans la réponse
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Réponse JSON retournée au client
        // Le client devra stocker ce token (ex: dans localStorage)
        // et l'inclure dans chaque requête : Authorization: Bearer <token>
        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("type", "Bearer");
        response.put("nom", user.getNom());
        response.put("email", user.getEmail());
        response.put("role", user.getRole().name());
        return response;
    }
}
