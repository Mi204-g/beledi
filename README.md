# 🏗️ Beledi — Plateforme de Signalement Civique

[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![JWT](https://img.shields.io/badge/JWT-Authentication-black?style=for-the-badge&logo=json-web-tokens&logoColor=white)](https://jwt.io/)
[![H2](https://img.shields.io/badge/Database-H2%20%2F%20Postgres-blue?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.h2database.com/)

**Beledi** est une plateforme web moderne conçue pour faciliter le signalement et le suivi des problèmes d'infrastructure urbaine (voirie, électricité, déchets, etc.) à Mauritanie. Elle permet aux citoyens de participer activement à l'amélioration de leur ville et aux administrateurs de gérer efficacement les interventions.

---

## 🚀 Fonctionnalités Principales

### 🔐 Sécurité & Utilisateurs
- **Authentification JWT** : Inscription et connexion sécurisées.
- **Gestion des Rôles** : Accès différencié pour `CITOYEN` et `ADMIN`.
- **Protection des données** : Hachage des mots de passe avec BCrypt et validation stricte des entrées.

### 📋 Gestion des Signalements
- **Cycle de vie complet** : Création, consultation, modification et suppression.
- **Suivi en temps réel** : Changement de statut par les administrateurs (`EN_ATTENTE` → `EN_COURS` → `RESOLU`).
- **Organisation** : Catégorisation (VOIRIE, ÉLECTRICITÉ, DÉCHETS, EAU, AUTRE) et géolocalisation.
- **Média** : Support pour l'ajout de photos via URL.

### 💬 Interaction & Feedback
- **Système de commentaires** : Permet aux utilisateurs de discuter sur chaque signalement.
- **Transparence** : Historique des échanges visible par tous les utilisateurs authentifiés.

### 📊 Pilotage Admin
- **Dashboard Statistique** : Vue d'ensemble des signalements par statut et par catégorie.
- **Interface de gestion** : Outils dédiés pour la modération et la mise à jour rapide.

---

## 🛠️ Stack Technique

- **Backend** : Spring Boot 3.2.0, Spring Security (JWT), Spring Data JPA.
- **Base de données** : H2 (Développement) / PostgreSQL (Production).
- **Langage** : Java 21.
- **Outils** : Maven, JWT (JJWT), Hibernate, Validation API.

---

## 📂 Structure du Projet

```text
src/main/java/com/beledi/
├── controller/     # API Endpoints (RestControllers)
├── service/        # Logique métier et validation
├── model/          # Entités JPA (Base de données)
├── repository/     # Interfaces d'accès aux données
├── dto/            # Objets de transfert de données (Request/Response)
├── security/       # Configuration JWT et Spring Security
└── exception/      # Gestion centralisée des erreurs
```

---

## 🚦 Démarrage Rapide

### Prérequis
- Java 21 ou supérieur
- Maven 3.8+

### Installation
1. **Cloner le projet**
   ```bash
   git clone <repository-url>
   cd beledi
   ```

2. **Compiler**
   ```bash
   mvn clean install
   ```

3. **Lancer l'application**
   ```bash
   mvn spring-boot:run
   ```

L'application sera accessible sur : **http://localhost:8080**

### Accès Rapide
- **Frontend** : `http://localhost:8080/index.html`
- **Console H2** : `http://localhost:8080/h2-console` (JDBC URL: `jdbc:h2:mem:baladidb`)
- **Documentation API** : `http://localhost:8080/api-docs.html`

---

## 📖 Guide de l'API

### Authentification
| Méthode | Endpoint | Description |
|:--- |:--- |:--- |
| `POST` | `/api/auth/register` | Création d'un nouveau compte |
| `POST` | `/api/auth/login` | Connexion et récupération du Token JWT |

### Signalements
| Méthode | Endpoint | Description | Auth |
|:--- |:--- |:--- |:--- |
| `GET` | `/api/signalements` | Liste tous les signalements | Public |
| `GET` | `/api/signalements/page` | Liste paginée et triée | Public |
| `POST` | `/api/signalements` | Créer un signalement | ✅ JWT |
| `PUT` | `/api/signalements/{id}` | Modifier un signalement | ✅ Owner/Admin |
| `DELETE` | `/api/signalements/{id}` | Supprimer un signalement | ✅ Owner/Admin |
| `PATCH` | `/api/signalements/{id}/statut`| Changer le statut | 👑 Admin |

---

## 📈 Améliorations Récentes
- ✅ **Pagination & Tri** : Amélioration des performances d'affichage.
- ✅ **Gestion d'erreurs** : Réponses JSON uniformes pour toutes les exceptions.
- ✅ **Commentaires** : Ajout de la logique de discussion sur les signalements.
- ✅ **Documentation** : Mise en place d'une page de documentation interactive.

---

## 🗓️ Roadmap
- [ ] Intégration de Google Maps pour une sélection de lieu plus précise.
- [ ] Système d'upload d'images direct (Supabase Storage/S3).
- [ ] Notifications par email pour le suivi des changements de statut.
- [ ] Application mobile (React Native / Flutter).

---

## 👥 Équipe & Crédits
**Projet développé par le Groupe G11 - Supnum 2026**
- Matricules : 24262, 24094, 24204, 24048

---
Licensed under MIT.
