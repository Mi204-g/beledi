package com.beledi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;


import java.time.LocalDateTime;
import java.util.List;

/**
 * Entité JPA représentant un signalement civique.
 * Hibernate génère automatiquement la table "signalements" dans Supabase.
 */
@Entity
@Table(name = "signalements")
public class Signalement {

    public Signalement() {
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Titre court décrivant le problème
    @Column(nullable = false)
    private String titre;

    // Description détaillée (TEXT = pas de limite de longueur en PostgreSQL)
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    // URL de la photo sur Supabase Storage (peut être null si pas de photo)
    @Column
    private String photoUrl;

    // Coordonnées GPS de l'emplacement du problème
    private Double latitude;
    private Double longitude;

    // Statut du signalement, stocké comme texte en base (ex: "EN_ATTENTE")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut;

    // Date et heure de création du signalement
    @Column(nullable = false)
    private LocalDateTime dateCreation;

    // Relation ManyToOne : plusieurs signalements peuvent appartenir à un même utilisateur
    // FetchType.EAGER = l'utilisateur est chargé en même temps que le signalement
    // @JoinColumn : indique la colonne de clé étrangère dans la table "signalements"
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Catégorie du problème
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Categorie categorie;

    // Un signalement peut avoir plusieurs commentaires
    // @JsonIgnore : évite la récursion Signalement → Commentaire → Signalement
    @JsonIgnore
    @OneToMany(mappedBy = "signalement", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Commentaire> commentaires;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }

    public List<Commentaire> getCommentaires() { return commentaires; }
    public void setCommentaires(List<Commentaire> commentaires) { this.commentaires = commentaires; }
}
