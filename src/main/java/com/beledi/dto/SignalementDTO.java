package com.beledi.dto;

import com.beledi.model.Categorie;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;


/**
 * DTO pour la création d'un nouveau signalement.
 *
 * On utilise un DTO au lieu de l'entité directement pour deux raisons :
 * 1. Sécurité : le client ne peut pas forcer le statut ou l'utilisateur
 * 2. Validation : on contrôle exactement ce que le client peut envoyer
 */
public class SignalementDTO {

    @NotBlank(message = "Le titre est obligatoire")
    private String titre;

    @NotBlank(message = "La description est obligatoire")
    private String description;

    // L'URL de la photo est optionnelle (l'upload se fait via Supabase Storage)
    private String photoUrl;

    // Coordonnées GPS (optionnelles)
    private Double latitude;
    private Double longitude;

    // La catégorie est obligatoire
    @NotNull(message = "La catégorie est obligatoire (VOIRIE, ELECTRICITE, DECHETS, EAU, AUTRE)")
    private Categorie categorie;

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

    public Categorie getCategorie() { return categorie; }
    public void setCategorie(Categorie categorie) { this.categorie = categorie; }
}
