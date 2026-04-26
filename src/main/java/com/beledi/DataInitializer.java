package com.beledi;

import com.beledi.model.*;
import com.beledi.repository.CommentaireRepository;
import com.beledi.repository.SignalementRepository;
import com.beledi.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Initialise des données de test au démarrage de l'application.
 *
 * CommandLineRunner : Spring Boot exécute run() automatiquement après le démarrage.
 * Utile pour insérer des données initiales (admin, données de demo...).
 *
 * Cette classe insère :
 * - 1 administrateur
 * - 2 citoyens
 * - 3 signalements de démonstration
 * - 2 commentaires de démonstration
 *
 * Elle ne réinsère RIEN si des utilisateurs existent déjà en base.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SignalementRepository signalementRepository;

    @Autowired
    private CommentaireRepository commentaireRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // BCrypt

    @Override
    public void run(String... args) throws Exception {

        // Garde-fou : on ne réinsère pas si des données existent déjà
        if (userRepository.count() > 0) {
            System.out.println("=== DataInitializer : données déjà présentes, initialisation ignorée ===");
            return;
        }

        System.out.println("=== DataInitializer : insertion des données de test... ===");

        // =====================================================================
        // Création des utilisateurs
        // =====================================================================

        // --- Admin ---
        User admin = new User();
        admin.setNom("Administrateur Baladi");
        admin.setEmail("admin@baladi.mr");
        // passwordEncoder.encode() hashe le mot de passe avec BCrypt
        admin.setMotDePasse(passwordEncoder.encode("admin123"));
        admin.setRole(Role.ADMIN);
        admin.setDateInscription(LocalDateTime.now().minusDays(30));
        userRepository.save(admin);

        // --- Citoyen 1 ---
        User citoyen1 = new User();
        citoyen1.setNom("Ahmed Ould Mohamed");
        citoyen1.setEmail("ahmed@baladi.mr");
        citoyen1.setMotDePasse(passwordEncoder.encode("citoyen123"));
        citoyen1.setRole(Role.CITOYEN);
        citoyen1.setDateInscription(LocalDateTime.now().minusDays(15));
        userRepository.save(citoyen1);

        // --- Citoyen 2 ---
        User citoyen2 = new User();
        citoyen2.setNom("Fatima Mint Cheikh");
        citoyen2.setEmail("fatima@baladi.mr");
        citoyen2.setMotDePasse(passwordEncoder.encode("citoyen123"));
        citoyen2.setRole(Role.CITOYEN);
        citoyen2.setDateInscription(LocalDateTime.now().minusDays(10));
        userRepository.save(citoyen2);

        // =====================================================================
        // Création des signalements
        // =====================================================================

        // --- Signalement 1 : Voirie (EN_ATTENTE) ---
        Signalement s1 = new Signalement();
        s1.setTitre("Nid-de-poule dangereux rue Ahmadou Kourouma");
        s1.setDescription("Un grand trou de 50 cm de diamètre sur la chaussée principale. "
                + "Plusieurs motos sont tombées cette semaine. Urgent !");
        s1.setCategorie(Categorie.VOIRIE);
        s1.setStatut(Statut.EN_ATTENTE);
        s1.setLatitude(18.0785);
        s1.setLongitude(-15.9654);
        s1.setDateCreation(LocalDateTime.now().minusDays(3));
        s1.setUser(citoyen1);
        signalementRepository.save(s1);

        // --- Signalement 2 : Électricité (EN_COURS) ---
        Signalement s2 = new Signalement();
        s2.setTitre("Coupure d'électricité prolongée quartier Tevragh Zeina");
        s2.setDescription("Plus d'électricité depuis 48 heures dans tout le quartier. "
                + "La SOMELEC a été contactée mais aucune équipe n'est intervenue.");
        s2.setCategorie(Categorie.ELECTRICITE);
        s2.setStatut(Statut.EN_COURS);
        s2.setLatitude(18.0900);
        s2.setLongitude(-15.9700);
        s2.setDateCreation(LocalDateTime.now().minusDays(2));
        s2.setUser(citoyen2);
        signalementRepository.save(s2);

        // --- Signalement 3 : Déchets (RESOLU) ---
        Signalement s3 = new Signalement();
        s3.setTitre("Accumulation de déchets marché El Mina");
        s3.setDescription("Tas de déchets non collectés depuis une semaine devant l'entrée "
                + "principale du marché. Risque sanitaire pour les commerçants et clients.");
        s3.setCategorie(Categorie.DECHETS);
        s3.setStatut(Statut.RESOLU);
        s3.setLatitude(18.0650);
        s3.setLongitude(-15.9800);
        s3.setDateCreation(LocalDateTime.now().minusDays(7));
        s3.setUser(citoyen1);
        signalementRepository.save(s3);

        // =====================================================================
        // Création de commentaires de démonstration
        // =====================================================================

        // Commentaire de l'admin sur le signalement 1
        Commentaire c1 = new Commentaire();
        c1.setContenu("Signalement bien reçu. Une équipe de voirie sera dépêchée sur place lundi prochain.");
        c1.setDate(LocalDateTime.now().minusDays(1));
        c1.setUser(admin);
        c1.setSignalement(s1);
        commentaireRepository.save(c1);

        // Commentaire du citoyen 2 sur le signalement 1
        Commentaire c2 = new Commentaire();
        c2.setContenu("Je confirme, j'ai failli tomber ce matin avec ma moto. C'est très dangereux !");
        c2.setDate(LocalDateTime.now().minusHours(5));
        c2.setUser(citoyen2);
        c2.setSignalement(s1);
        commentaireRepository.save(c2);

        // =====================================================================
        // Récapitulatif dans la console
        // =====================================================================
        System.out.println("=================================================================");
        System.out.println("  DONNÉES DE TEST INITIALISÉES AVEC SUCCÈS !");
        System.out.println("=================================================================");
        System.out.println("  ADMIN   : admin@baladi.mr   / admin123");
        System.out.println("  CITOYEN : ahmed@baladi.mr   / citoyen123");
        System.out.println("  CITOYEN : fatima@baladi.mr  / citoyen123");
        System.out.println("-----------------------------------------------------------------");
        System.out.println("  Signalements créés : 3 (1 EN_ATTENTE, 1 EN_COURS, 1 RESOLU)");
        System.out.println("  Commentaires créés : 2");
        System.out.println("=================================================================");
    }
}
