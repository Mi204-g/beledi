package com.beledi.service;

import com.beledi.model.User;
import com.beledi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Collections;

/**
 * Implémentation de UserDetailsService — interface OBLIGATOIRE pour Spring Security.
 *
 * Spring Security appelle loadUserByUsername() pour charger un utilisateur
 * lors d'une tentative de connexion ou de validation d'un token JWT.
 *
 * Dans notre cas, le "username" est l'email de l'utilisateur.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Charge un utilisateur depuis la base de données par son email.
     * Spring Security appelle cette méthode automatiquement.
     *
     * @param email L'email de l'utilisateur (utilisé comme identifiant)
     * @return Un objet UserDetails que Spring Security utilise en interne
     * @throws UsernameNotFoundException si aucun utilisateur ne correspond
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Cherche l'utilisateur dans Supabase via son email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                    new UsernameNotFoundException("Aucun utilisateur trouvé avec l'email : " + email)
                );

        // Retourne un UserDetails Spring Security avec :
        // - l'email (identifiant)
        // - le mot de passe hashé (Spring le compare avec BCrypt)
        // - les autorités (ROLE_CITOYEN ou ROLE_ADMIN)
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getMotDePasse(),
                getAuthorities(user)
        );
    }

    /**
     * Convertit notre enum Role en autorité Spring Security.
     *
     * Spring Security exige le préfixe "ROLE_" pour les rôles.
     * Ex: Role.ADMIN → "ROLE_ADMIN"
     *     Role.CITOYEN → "ROLE_CITOYEN"
     *
     * Cela permet d'utiliser .hasRole("ADMIN") (Spring ajoute "ROLE_" automatiquement)
     * ou .hasAuthority("ROLE_ADMIN") (sans ajout automatique).
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        String roleName = "ROLE_" + user.getRole().name(); // Ex: "ROLE_ADMIN"
        return Collections.singletonList(new SimpleGrantedAuthority(roleName));
    }
}
