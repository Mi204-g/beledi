package com.baladi.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilitaire pour la gestion des tokens JWT.
 *
 * Un token JWT (JSON Web Token) est une chaîne signée qui contient :
 *   - Header  : algorithme utilisé (HS256)
 *   - Payload : les "claims" (ex: l'email de l'utilisateur, la date d'expiration)
 *   - Signature : garantit que le token n'a pas été falsifié
 *
 * Format : xxxxx.yyyyy.zzzzz
 */
@Component
public class JwtUtil {

    // Injecte la valeur depuis application.properties (jwt.secret)
    @Value("${jwt.secret}")
    private String secret;

    // Durée de validité du token en millisecondes (depuis application.properties)
    @Value("${jwt.expiration}")
    private long expiration;

    // =========================================================================
    // Génération de la clé de signature
    // =========================================================================

    /**
     * Convertit le secret (String) en clé cryptographique pour signer le JWT.
     * JJWT exige une clé d'au moins 256 bits (32 octets) pour l'algorithme HS256.
     */
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // =========================================================================
    // Génération du token
    // =========================================================================

    /**
     * Génère un token JWT pour un utilisateur authentifié.
     * Le "sujet" (subject) du token est l'email de l'utilisateur.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // On pourrait ajouter des claims supplémentaires ici (ex: le rôle)
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Construit le token JWT avec toutes ses propriétés.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)             // Payload personnalisé
                .setSubject(subject)           // L'email de l'utilisateur
                .setIssuedAt(now)              // Date de création
                .setExpiration(expirationDate) // Date d'expiration
                .signWith(getSigningKey(), SignatureAlgorithm.HS256) // Signature
                .compact();                    // Sérialise en String "xxx.yyy.zzz"
    }

    // =========================================================================
    // Extraction d'informations du token
    // =========================================================================

    /**
     * Extrait l'email (subject) du token JWT.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait la date d'expiration du token.
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Méthode générique pour extraire n'importe quel "claim" du token.
     * Utilise une lambda pour spécifier quel claim extraire.
     *
     * Exemple : extractClaim(token, Claims::getSubject) → extrait l'email
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parse le token et retourne tous les claims (payload).
     * Lance une exception si le token est invalide ou expiré.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey()) // Vérifie la signature avec notre clé secrète
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // =========================================================================
    // Validation du token
    // =========================================================================

    /**
     * Vérifie si le token est expiré.
     */
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /**
     * Valide le token : vérifie que l'email correspond à l'utilisateur
     * et que le token n'est pas expiré.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
