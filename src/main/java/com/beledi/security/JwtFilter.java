package com.beledi.security;

import com.beledi.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre JWT qui s'exécute UNE FOIS par requête HTTP.
 *
 * Son rôle : lire le token JWT dans l'en-tête "Authorization",
 * le valider, puis informer Spring Security de l'identité de l'utilisateur.
 *
 * Flux d'une requête sécurisée :
 *   Client → [JwtFilter] → [SecurityFilterChain] → Controller
 *
 * L'en-tête attendu : Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
 */
@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        // ----- Étape 1 : Lire l'en-tête Authorization -----
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        // L'en-tête doit commencer par "Bearer " (avec un espace)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // On extrait le token en enlevant les 7 premiers caractères ("Bearer ")
            token = authHeader.substring(7);
            try {
                email = jwtUtil.extractUsername(token); // Extrait l'email du payload JWT
            } catch (Exception e) {
                // Token malformé, expiré ou signature invalide → on ignore et on continue
                logger.warn("JWT invalide ou expiré : " + e.getMessage());
            }
        }

        // ----- Étape 2 : Valider le token et authentifier l'utilisateur -----
        // On traite seulement si :
        // - on a bien extrait un email du token
        // - aucune authentification n'est déjà en cours pour cette requête
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                if (jwtUtil.validateToken(token, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (Exception e) {
                // Utilisateur introuvable en base (compte supprimé) ou token invalide → on ignore
                logger.warn("Authentification JWT échouée : " + e.getMessage());
            }
        }

        // ----- Étape 3 : Passer la requête au filtre suivant -----
        // Même si le token est absent ou invalide, la requête continue.
        // Spring Security décidera ensuite si la route est accessible ou non.
        filterChain.doFilter(request, response);
    }
}
