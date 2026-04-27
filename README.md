# Baladi — Plateforme de Signalement Civique

Plateforme web complète pour le signalement et le suivi des problèmes d'infrastructure urbaine à Nouakchott.

## ✨ Fonctionnalités Complete

### 🏛️ Authentification & Autorisation
- ✅ Inscription/Connexion sécurisée avec JWT
- ✅ Rôles utilisateur (CITOYEN, ADMIN)
- ✅ Tokens JWT avec expiration 24h
- ✅ Hachage BCrypt des mots de passe

### 📋 Gestion des Signalements
- ✅ **Créer**: Citoyens peuvent signaler des problèmes
- ✅ **Lire**: Liste publique + détails + pagination
- ✅ **Mettre à jour**: Propriétaire ou admin peuvent modifier
- ✅ **Supprimer**: Propriétaire ou admin peuvent supprimer
- ✅ **Statut**: Admin change le statut (EN_ATTENTE → EN_COURS → RESOLU)
- ✅ **Catégories**: VOIRIE, ÉLECTRICITÉ, DÉCHETS, EAU, AUTRE
- ✅ **Localisation**: Latitude/Longitude + photos

### 💬 Commentaires (NOUVEAU)
- ✅ Ajouter commentaires sur les signalements
- ✅ Consulter tous les commentaires
- ✅ Modifier ses commentaires
- ✅ Supprimer ses commentaires (ou admin)
- ✅ Endpoints: `/api/signalements/{id}/commentaires`

### 📊 Dashboard Admin
- ✅ Statistiques globales (total, par statut, par catégorie)
- ✅ Gestion des statuts de signalements
- ✅ Endpoint: `/api/admin/statistiques`

### 🔍 Pagination & Filtrage (NOUVEAU)
- ✅ Pagination sur liste signalements
- ✅ Tri par date, titre, statut
- ✅ Ordre ascendant/descendant
- ✅ Endpoint: `/api/signalements/page?page=0&size=10&sortBy=dateCreation`

### 🛡️ Sécurité
- ✅ CORS configuré
- ✅ Validation des données avec annotations (@NotBlank, @Email, @Size)
- ✅ Gestion centralisée d'erreurs (GlobalExceptionHandler)
- ✅ Authentification basée sur JWT
- ✅ Autorisation au niveau des endpoints

---

## 🚀 Getting Started

### Prérequis
- Java 21+
- Maven 3.8+
- Navigateur moderne

### Installation & Démarrage

#### 1. Cloner le projet
```bash
cd /home/mine/beledi-1
```

#### 2. Compiler le projet
```bash
mvn clean compile
```

#### 3. Lancer le serveur
```bash
mvn spring-boot:run
```

L'application démarre sur: **http://localhost:8080**

### Base de Données
- **Mode développement**: H2 (en mémoire)
- **Console H2**: http://localhost:8080/h2-console
  - User: `sa`
  - Password: (laissez vide)
  - JDBC URL: `jdbc:h2:mem:baladidb`

### Pages Disponibles
- **Accueil**: http://localhost:8080
- **Inscription**: http://localhost:8080/register.html
- **Connexion**: http://localhost:8080/login.html
- **Dashboard Citoyen**: http://localhost:8080/dashboard.html
- **Dashboard Admin**: http://localhost:8080/admin.html
- **Documentation API**: http://localhost:8080/api-docs.html

---

## 📚 API Documentation

### Base URL
```
http://localhost:8080/api
```

### Authentification
Tous les endpoints protégés nécessitent un header:
```
Authorization: Bearer {JWT_TOKEN}
```

### Endpoints Principaux

#### Auth
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| POST | `/auth/register` | ❌ | Créer un compte |
| POST | `/auth/login` | ❌ | Se connecter |

#### Signalements
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| GET | `/signalements` | ❌ | Liste tous |
| GET | `/signalements/page` | ❌ | Liste paginée |
| GET | `/signalements/{id}` | ❌ | Détail |
| GET | `/signalements/mes` | ✅ | Mes signalements |
| POST | `/signalements` | ✅ | Créer |
| PUT | `/signalements/{id}` | ✅ | Mettre à jour |
| PUT | `/signalements/{id}/statut` | 👑 | Changer statut (admin) |
| DELETE | `/signalements/{id}` | ✅ | Supprimer |

#### Commentaires
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| GET | `/signalements/{id}/commentaires` | ❌ | Tous les commentaires |
| GET | `/signalements/{id}/commentaires/{cId}` | ❌ | Un commentaire |
| POST | `/signalements/{id}/commentaires` | ✅ | Ajouter |
| PUT | `/signalements/{id}/commentaires/{cId}` | ✅ | Mettre à jour |
| DELETE | `/signalements/{id}/commentaires/{cId}` | ✅ | Supprimer |

#### Admin
| Méthode | Endpoint | Auth | Description |
|---------|----------|------|-------------|
| GET | `/admin/statistiques` | 👑 | Stats dashboard |

**Légende**: ❌ Public | ✅ Authentifié | 👑 Admin seulement

### Exemple de Requête

#### S'inscrire
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom": "Ahmed Ould Mohamed",
    "email": "ahmed@example.com",
    "motDePasse": "SecurePassword123!"
  }'
```

#### Se connecter
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ahmed@example.com",
    "motDePasse": "SecurePassword123!"
  }'
```

#### Créer un signalement
```bash
curl -X POST http://localhost:8080/api/signalements \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer {JWT_TOKEN}" \
  -d '{
    "titre": "Nid de poule dangereux",
    "description": "Grand trou sur la route principale",
    "categorie": "VOIRIE",
    "latitude": 18.0785,
    "longitude": -15.9654,
    "photoUrl": null
  }'
```

---

## 🏗️ Architecture

```
src/main/java/com/baladi/
├── model/              # Entités JPA
│   ├── User.java
│   ├── Signalement.java
│   ├── Commentaire.java
│   ├── Role.java (enum)
│   ├── Statut.java (enum)
│   └── Categorie.java (enum)
├── controller/         # API REST Controllers
│   ├── UserController.java
│   ├── SignalementController.java
│   └── CommentaireController.java
├── service/            # Logique métier
│   ├── UserService.java
│   ├── SignalementService.java
│   └── CommentaireService.java
├── repository/         # Accès données (JPA)
│   ├── UserRepository.java
│   ├── SignalementRepository.java
│   └── CommentaireRepository.java
├── dto/                # Data Transfer Objects
│   ├── LoginRequest.java
│   ├── RegisterRequest.java
│   ├── SignalementDTO.java
│   └── CommentaireRequest.java (NEW)
├── security/           # JWT & Spring Security
│   ├── JwtUtil.java
│   ├── JwtFilter.java
│   ├── SecurityConfig.java
│   └── UserDetailsServiceImpl.java
├── exception/          # Exception Handling
│   └── GlobalExceptionHandler.java (NEW)
└── BaladiApplication.java
```

---

## 🔐 Sécurité

### Configuration
- **JWT Secret**: Configuré dans `application.properties`
- **Expiration Token**: 24 heures
- **Hachage Mot de Passe**: BCrypt
- **CORS**: Autorisé pour localhost

### Points Sensibles Protégés
- ❌ Créer/Mettre à jour/Supprimer un signalement: Authentification requise
- ❌ Accéder aux statistiques admin: Admin seulement
- ❌ Changer le statut d'un signalement: Admin seulement

---

## 📋 Améliorations Apportées (par rapport à la version initiale)

### ✅ Corrections Critiques
1. **Base de Données**: Migration de Supabase PostgreSQL vers H2 local (développement)
2. **Commentaires**: Implémentation complète (service + contrôleur + endpoints)

### ✅ Nouvelles Fonctionnalités
3. **Pagination**: Endpoint `/api/signalements/page` avec tri
4. **Mise à jour Signalement**: PUT `/api/signalements/{id}`
5. **Suppression Signalement**: DELETE `/api/signalements/{id}`
6. **Mise à jour Commentaires**: PUT/DELETE `/api/signalements/{id}/commentaires/{cId}`
7. **Gestion d'Erreurs Globale**: GlobalExceptionHandler pour réponses cohérentes
8. **Documentation API**: Page `/api-docs.html` complète

### ✅ Améliorations de Sécurité
9. **Validation Stricte**: Annotations @NotBlank, @Email, @Size
10. **Vérifications Authorization**: Propriétaire ou admin pour modifier/supprimer
11. **Gestion d'Erreurs Sécurisée**: Messages d'erreur appropriés

### ✅ Améliorations UX
12. **API Cohérente**: Format de réponse uniforme
13. **Support du Frontend**: Fonction `apiDelete()` ajoutée à utils.js
14. **Documentation**: Notes complètes en ligne

---

## 🧪 Test Rapide

### 1. Créer un compte citoyen
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nom":"Ahmed Mohamed",
    "email":"ahmed@test.com",
    "motDePasse":"Test123!"
  }'
```

### 2. Récupérer le token JWT
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ahmed@test.com","motDePasse":"Test123!"}'
```

### 3. Créer un signalement
```bash
TOKEN="eyJhbGci..." # Token reçu à l'étape 2

curl -X POST http://localhost:8080/api/signalements \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "titre":"Lampadaire cassé",
    "description":"Électricité défaillante Rue 1",
    "categorie":"ELECTRICITE",
    "latitude":18.08,
    "longitude":-15.97
  }'
```

### 4. Consulter les signalements
```bash
curl http://localhost:8080/api/signalements | jq .
```

---

## 📝 Notes Développement

### Développement Futur
- [ ] Upload de photos (Supabase Storage)
- [ ] Partage sur réseaux sociaux
- [ ] Notifications push
- [ ] Intégration Google Maps
- [ ] Rapport PDF des statistiques
- [ ] Système de vote (upvote/downvote)
- [ ] Catégorisation automatique (ML)

### Production Checklist
- [ ] Configurer variable d'environnement pour JWT secret
- [ ] Activer HTTPS/SSL
- [ ] Configurer base de données PostgreSQL/Supabase
- [ ] Mettre en `ddl-auto=validate` pour Hibernate
- [ ] Implémenter monitoring & logging
- [ ] Rate limiting sur login
- [ ] Sauvegarder les données régulièrement

### Configuration Production (application.properties)
```properties
# Base de Données PostgreSQL/Supabase
spring.datasource.url=jdbc:postgresql://HOST:PORT/DATABASE?sslmode=require
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate

# JWT Secret (venir de l'environnement)
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# HTTPS
server.ssl.key-store=${SSL_KEYSTORE}
server.ssl.key-store-password=${SSL_PASSWORD}
```

---

## 📞 Support

En cas de problème:
1. Consultez les logs: `http://localhost:8080/h2-console`
2. Vérifiez la documentation API: `http://localhost:8080/api-docs.html`
3. Testez avec cURL les endpoints fournis

---

## 📄 Licence

Plateforme Baladi — Groupe G11 (24262, 24094, 24204, 24048)  
Supnum 2026
