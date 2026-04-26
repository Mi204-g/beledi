package com.beledi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO pour les requêtes de création/modification de commentaires.
 */
public class CommentaireRequest {

    @NotBlank(message = "Le contenu du commentaire ne peut pas être vide")
    @Size(min = 2, max = 2000, message = "Le contenu doit faire entre 2 et 2000 caractères")
    private String contenu;

    public CommentaireRequest() {}

    public CommentaireRequest(String contenu) {
        this.contenu = contenu;
    }

    public String getContenu() {
        return contenu;
    }

    public void setContenu(String contenu) {
        this.contenu = contenu;
    }
}
