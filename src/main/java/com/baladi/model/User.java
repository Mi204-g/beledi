package com.baladi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;


import java.time.LocalDateTime;
import java.util.List;

/**
 * Entité JPA représentant un utilisateur de la plateforme.
 * Hibernate génère automatiquement la table "users" dans Supabase.
 *
 * NOTE : on évite @Data de Lombok sur les entités JPA car il génère
 * un equals/hashCode qui peut causer des problèmes avec les collections lazy.
 * On utilise @Getter + @Setter + @NoArgsConstructor à la place.
 */
@Entity
@Table(name = "users") // "user" est un mot réservé en SQL — on utilise "users"
public class User {

    public User() {
    }

    // Clé primaire auto-incrémentée par PostgreSQL (SERIAL / BIGSERIAL)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Nom complet de l'utilisateur
    @Column(nullable = false)
    private String nom;

    // Email unique — sert aussi d'identifiant de connexion
    @Column(nullable = false, unique = true)
    private String email;

    // Mot de passe hashé avec BCrypt — @JsonIgnore l'exclut de toutes les réponses JSON
    @JsonIgnore
    @Column(nullable = false)
    private String motDePasse;

    // Rôle stocké en base comme une chaîne ("CITOYEN" ou "ADMIN")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // Date et heure d'inscription
    @Column(nullable = false)
    private LocalDateTime dateInscription;

    // Relation bidirectionnelle : un user peut avoir plusieurs signalements
    // @JsonIgnore évite la récursion infinie lors de la sérialisation JSON
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Signalement> signalements;

    // Relation bidirectionnelle : un user peut avoir plusieurs commentaires
    @JsonIgnore
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Commentaire> commentaires;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public LocalDateTime getDateInscription() { return dateInscription; }
    public void setDateInscription(LocalDateTime dateInscription) { this.dateInscription = dateInscription; }

    public List<Signalement> getSignalements() { return signalements; }
    public void setSignalements(List<Signalement> signalements) { this.signalements = signalements; }

    public List<Commentaire> getCommentaires() { return commentaires; }
    public void setCommentaires(List<Commentaire> commentaires) { this.commentaires = commentaires; }
}
