package com.baladi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;


import java.time.LocalDateTime;

/**
 * Entité JPA représentant un commentaire sur un signalement.
 * Hibernate génère automatiquement la table "commentaires" dans Supabase.
 */
@Entity
@Table(name = "commentaires")
public class Commentaire {

    public Commentaire() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Contenu textuel du commentaire
    @Column(nullable = false, columnDefinition = "TEXT")
    private String contenu;

    // Date et heure du commentaire
    @Column(nullable = false)
    private LocalDateTime date;

    // L'utilisateur qui a écrit le commentaire
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Le signalement auquel appartient ce commentaire
    // @JsonIgnore : évite la boucle infinie Commentaire → Signalement → Commentaires → ...
    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "signalement_id", nullable = false)
    private Signalement signalement;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getContenu() { return contenu; }
    public void setContenu(String contenu) { this.contenu = contenu; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Signalement getSignalement() { return signalement; }
    public void setSignalement(Signalement signalement) { this.signalement = signalement; }
}
