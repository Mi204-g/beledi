package com.beledi.security;

import com.beledi.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration principale de Spring Security.
 *
 * Dans Spring Boot 3 / Spring Security 6, on ne peut plus étendre
 * WebSecurityConfigurerAdapter (déprécié). On définit des @Bean à la place.
 *
 * Ce fichier configure :
 * - L'encodage des mots de passe (BCrypt)
 * - L'authentification par base de données
 * - Les règles d'accès aux routes (qui peut accéder à quoi)
 * - L'intégration du filtre JWT
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Active les annotations @PreAuthorize dans les controllers
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtFilter jwtFilter;

    // =========================================================================
    // Beans de configuration
    // =========================================================================

    /**
     * BCryptPasswordEncoder : algorithme de hachage des mots de passe.
     * BCrypt est recommandé car il est lent par design (résistant aux brute-force)
     * et intègre un "salt" aléatoire automatiquement.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * DaoAuthenticationProvider : dit à Spring Security comment vérifier
     * un login/mot de passe. Il utilise :
     * - notre UserDetailsServiceImpl (charge l'user depuis la BDD)
     * - notre PasswordEncoder (compare le mot de passe avec le hash BCrypt)
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * AuthenticationManager : le "chef d'orchestre" de l'authentification.
     * On l'injecte dans UserService pour pouvoir déclencher l'auth manuellement.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // =========================================================================
    // Règles de sécurité HTTP
    // =========================================================================

    /**
     * Configure les règles d'accès aux routes de l'API.
     *
     * IMPORTANT : les règles sont évaluées dans l'ORDRE.
     * La première règle qui correspond à une requête est appliquée.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Désactive CSRF : inutile avec JWT (pas de cookies de session)
            .csrf(csrf -> csrf.disable())

            // Configuration des autorisations par route
            .authorizeHttpRequests(auth -> auth

                // === FICHIERS STATIQUES (site web) ===
                .requestMatchers("/", "/*.html", "/js/**", "/css/**", "/images/**", "/uploads/**").permitAll()

                // === ROUTES PUBLIQUES (pas de token requis) ===
                // Inscription, connexion et console H2 : tout le monde peut y accéder
                .requestMatchers("/api/auth/**", "/h2-console/**").permitAll()

                // === ROUTES ADMIN : réservées au rôle ADMIN ===
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // === SIGNALEMENTS : /mes nécessite d'être connecté ===
                // IMPORTANT : cette règle doit être AVANT la règle GET générale
                // car les règles s'appliquent dans l'ordre (premier match gagne)
                .requestMatchers(HttpMethod.GET, "/api/signalements/mes").authenticated()

                // === SIGNALEMENTS : lecture publique (liste et détail) ===
                // GET /api/signalements     → liste de tous les signalements
                // GET /api/signalements/123 → détail d'un signalement
                .requestMatchers(HttpMethod.GET, "/api/signalements", "/api/signalements/*").permitAll()

                // === TOUT LE RESTE nécessite d'être connecté ===
                // Ex: POST /api/signalements (créer), PUT /api/signalements/{id}/statut
                .anyRequest().authenticated()
            )

            // Pas de sessions HTTP côté serveur : STATELESS (on utilise JWT)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Autoriser l'affichage de la console H2 dans une frame (indispensable pour /h2-console)
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))

            // Enregistre notre fournisseur d'authentification
            .authenticationProvider(authenticationProvider())

            // Ajoute notre JwtFilter AVANT le filtre standard Spring Security
            // → il s'exécute en premier pour extraire l'identité depuis le JWT
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
