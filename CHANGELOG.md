# CHANGELOG — Baladi Platform

## Version 1.1.0 — Complete & Production-Ready (25 Avril 2026)

### ✨ Nouvelles Fonctionnalités

#### Commentaires (NOUVEAU)
- Service complet pour gérer les commentaires sur les signalements
- Endpoints REST pour ajouter, consulter, mettre à jour, supprimer des commentaires
- Validation des données avec annotations
- Contrôle d'accès: propriétaire du commentaire ou admin

#### Pagination (NOUVEAU)
- Endpoint `/api/signalements/page` avec support pagination
- Paramètres: `page`, `size`, `sortBy`, `descending`
- Tri possible par: dateCreation, titre, statut, etc.
- Format de réponse uniforme avec métadata

#### Update/Delete Signalements (NOUVEAU)
- PUT `/api/signalements/{id}` pour mettre à jour
- DELETE `/api/signalements/{id}` pour supprimer
- Vérifications: propriétaire ou admin
- Validations statut: impossible de modifier si résolu, etc.

#### Gestion d'Erreurs Centralisée (NOUVEAU)
- `GlobalExceptionHandler` pour uniformiser les réponses d'erreur
- Gestion des exceptions de validation
- Messages d'erreur informatifs
- Logging des erreurs serveur

#### Documentation API (NOUVEAU)
- Page `/api-docs.html` avec reference complète
- Exemples de requêtes cURL
- Énumérés et paramètres documentés

### 🔧 Corrections Critiques

#### Base de Données
- Migration de Supabase PostgreSQL → H2 (développement local)
- Suppression des identifiants en dur (sécurité)
- Configuration simplifiée pour démarrage rapide

#### Validation
- Ajout annotations `@NotBlank`, `@Email`, `@Size` sur DTOs
- Validation côté serveur pour tous les inputs
- Messages d'erreur détaillés pour le formulaire

### 🛡️ Améliorations Sécurité

- Héritage de `@PreAuthorize` sur tous les endpoints sensibles
- Contrôle d'accès au niveau service (double vérification)
- Gestion des erreurs sans fuite d'information sensible
- Validation stricte des données entrantes

### 📦 Nouvelles Dépendances / Fichiers

| Fichier | Type | Description |
|---------|------|-------------|
| `CommentaireService.java` | Service | Logique métier commentaires |
| `CommentaireController.java` | Controller | Endpoints commentaires |
| `CommentaireRequest.java` | DTO | Requête création/modification |
| `GlobalExceptionHandler.java` | Exception | Gestion centralisée erreurs |
| `api-docs.html` | Static | Documentation API |
| `README.md` | Doc | Guide complet |

### 🔄 Changements Existants

#### SignalementService.java
- Ajout import `Page` et `PageRequest`
- Nouvelle méthode `getAllSignalementsPageable()`
- Nouvelle méthode `updateSignalement()`
- Nouvelle méthode `deleteSignalement()`

#### SignalementController.java
- Ajout imports pour `Page`, `HttpStatus`
- Nouvel endpoint GET `/page` (pagination)
- Nouvel endpoint PUT `/{id}` (update)
- Nouvel endpoint DELETE `/{id}` (delete)

#### utils.js
- Ajout fonction `apiDelete(path)`
- Support pour les requêtes DELETE authentifiées

#### application.properties
- Changement BD: PostgreSQL → H2
- Activation console H2
- Dialecte Hibernate: PostgreSQLDialect → H2Dialect

---

## Version 1.0.0 — Initial Release (Date inconnue)

### Fonctionnalités de Base
- ✅ Authentification JWT
- ✅ Gestion utilisateurs (CITOYEN, ADMIN)
- ✅ CRUD Signalements complet
- ✅ Statuts workflow (EN_ATTENTE → EN_COURS → RESOLU)
- ✅ Catégories signalements
- ✅ Dashboard admin avec statistiques
- ✅ Interface web (HTML/CSS/JS)
- ✅ JWT Filter pour authentification
- ✅ Spring Security integration

### Problèmes Connus (Corrigés en 1.1.0)
- ❌ Commentaires: Service incomplet (API manquante)
- ❌ Pas de pagination sur listes longues
- ❌ Impossible d'update/delete signalements
- ❌ Erreurs mal formatées (pas de handler global)
- ❌ Base de données externe (Supabase) nécessaire en dev

---

## Statistiques Projet

| Métrique | Avant v1.1.0 | Après v1.1.0 | Delta |
|----------|------------|-----------|-------|
| Classes Java | 16 | 18 | +2 |
| Controllers | 2 | 3 | +1 |
| Services | 2 | 3 | +1 |
| DTOs | 3 | 4 | +1 |
| Endpoints API | 11 | 20 | +9 |
| Exception Handlers | 0 | 1 | +1 |
| Pages HTML | 5 | 6 | +1 |

---

## Pour les Futurs Développeurs

### Commandes Utiles
```bash
# Compiler
mvn clean compile

# Tester
mvn test

# Builder JAR
mvn clean package

# Lancer serveur dev
mvn spring-boot:run

# Voir logs détaillés
mvn spring-boot:run -DskipTests -X
```

### Points d'Extension Recommandés
1. **Upload Photos**: Ajouter endpoint multipart en SignalementController
2. **Notifications**: Implémenter système d'alertes SignalementService
3. **Recherche**: Ajouter full-text search en SignalementRepository
4. **Permissions Fine-Grained**: Ajouter table permissions vs simplement CITOYEN/ADMIN
5. **Audit**: Logger toutes les modifications dans table d'audit

---

Dernière mise à jour: 25 Avril 2026
