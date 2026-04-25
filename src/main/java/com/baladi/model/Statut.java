package com.baladi.model;

/**
 * Statut d'avancement d'un signalement.
 * - EN_ATTENTE : le signalement vient d'être créé, pas encore pris en charge
 * - EN_COURS   : les services compétents traitent le problème
 * - RESOLU     : le problème a été résolu
 */
public enum Statut {
    EN_ATTENTE,
    EN_COURS,
    RESOLU
}
