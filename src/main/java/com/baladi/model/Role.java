package com.baladi.model;

/**
 * Rôles disponibles dans l'application.
 * - CITOYEN : utilisateur normal, peut créer des signalements
 * - ADMIN   : administrateur, peut tout voir et changer les statuts
 */
public enum Role {
    CITOYEN,
    ADMIN
}
