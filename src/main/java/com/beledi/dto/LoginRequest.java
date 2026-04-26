package com.beledi.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


/**
 * DTO (Data Transfer Object) pour la requête de connexion.
 *
 * Un DTO est un objet simple utilisé pour transporter des données
 * entre le client et le serveur. Il n'est pas lié à une table en base.
 *
 * @Data de Lombok génère : getters, setters, equals, hashCode, toString.
 * (On peut utiliser @Data sur les DTOs, contrairement aux entités JPA.)
 */
public class LoginRequest {

    // @NotBlank : le champ ne doit pas être null ni vide
    // @Email : doit avoir le format d'un email valide
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String motDePasse;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMotDePasse() { return motDePasse; }
    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
}
