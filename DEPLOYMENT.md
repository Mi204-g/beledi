# CONFIGURATION PRODUCTION — Baladi Platform

> ⚠️ Guide de mise en place en environnement production
> Dernière mise à jour: 25 Avril 2026

## 🔒 Sécurité — Points Critiques

### 1. JWT Secret
**JAMAIS mettre le secret en dur dans le code!**

**Avant (DANGEREUX)**:
```properties
jwt.secret=baladiSecretKey2024GroupeG11SupnumMauritanie_SuperSecure!
```

**Après (SÉCURISÉ)**:
```bash
# Dans le shell avant de lancer l'app
export JWT_SECRET="$(openssl rand -base64 32)"

# Ou dans docker: -e JWT_SECRET=...
# Ou dans k8s: envFrom.configMapRef
```

Update `application.properties`:
```properties
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000
```

### 2. Identifiants Base de Données
Utiliser les variables d'environnement, JAMAIS hardcoder!

```properties
# application.properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

### 3. HTTPS / SSL
Configuration obligatoire en prod!

```properties
server.ssl.enabled=true
server.ssl.key-store=${SSL_KEYSTORE_PATH}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
```

### 4. CORS
Restrict origins en production:

```java
@Configuration
public class CorsConfig {
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("https://baladi.mr", "https://app.baladi.mr"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

---

## 🗄️ Base de Données

### Configuration Recommended: PostgreSQL/Supabase

```properties
# PostgreSQL / Supabase
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?sslmode=require&prepareThreshold=0

# Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate  # ⚠️ JAMAIS 'update' en prod!
spring.jpa.show-sql=false               # ⚠️ Désactiver verbose
```

### Migration Schema
1. Avant déploiement, exécuter les migrations:
```bash
# Récupérer le schéma depuis H2 dev:
# Copier les create table depuis logs/h2-console
# Ou exporter depuis Hibernate: spring.jpa.hibernate.ddl-auto=create vs validate
```

2. Utiliser Liquibase/Flyway pour versioning:
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

### Backup Strategy
```bash
# PostgreSQL dump
pg_dump -h HOST -U USER -d DATABASE > backup_$(date +%Y%m%d).sql

# Supabase: Utiliser les outils Supabase CLI
supabase db dump -f backup.sql
```

---

## 🚀 Déploiement

### Option 1: Docker

```dockerfile
FROM openjdk:21-slim

WORKDIR /app

# Build app
COPY pom.xml .
COPY src ./src
RUN apt-get update && apt-get install -y maven
RUN mvn clean package -DskipTests

# Copy JAR
FROM openjdk:21-slim
COPY --from=0 /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Xmx256m", "-jar", "app.jar"]
```

```bash
# Build et run
docker build -t baladi:latest .
docker run -e JWT_SECRET="..." \
           -e DB_URL="..." \
           -e DB_USER="..." \
           -e DB_PASSWORD="..." \
           -p 8080:8080 \
           baladi:latest
```

### Option 2: Cloud (Heroku, Render, Railway, etc.)

```bash
# Heroku
heroku create baladi-app
heroku config:set JWT_SECRET="..." DB_URL="..." ...
git push heroku main
```

### Option 3: VPS (Ubuntu/Debian)

```bash
# Installer Java 21
sudo apt update
sudo apt install openjdk-21-jdk

# Build locally
mvn clean package

# Copy to server
scp target/baladi-0.0.1-SNAPSHOT.jar user@vps:/home/baladi/

# SSH et run
ssh user@vps
cd /home/baladi
java -Xmx1G -jar baladi-0.0.1-SNAPSHOT.jar
```

---

## 📊 Monitoring & Logging

### Application Logs
```properties
# application.properties
logging.level.root=INFO
logging.level.com.baladi=DEBUG
logging.file.name=/var/log/baladi/app.log
logging.file.max-size=10MB
logging.file.max-history=30
```

### Metrics (Actuator)
```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```properties
# Endpoints Actuator
management.endpoints.web.exposure.include=health,metrics,info,prometheus
management.endpoint.health.show-details=always
```

Access: `http://localhost:8080/actuator/health`

### Sentry (Error Tracking)
```xml
<dependency>
    <groupId>io.sentry</groupId>
    <artifactId>sentry-spring-boot-starter</artifactId>
    <version>6.31.0</version>
</dependency>
```

```properties
sentry.dsn=${SENTRY_DSN}
sentry.environment=production
```

---

## 🔐 Rate Limiting (Brute Force Protection)

Ajouter au projet:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-circuitbreaker-micrometer</artifactId>
</dependency>
```

Implement custom security:
```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    private final Map<String, Integer> attempts = new ConcurrentHashMap<>();
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
            HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        if (request.getRequestURI().contains("/api/auth/login")) {
            String ip = getClientIP(request);
            int count = attempts.getOrDefault(ip, 0);
            
            if (count > 5) {  // Max 5 attempts per minute
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Too many login attempts. Try again later.");
                return;
            }
            
            attempts.put(ip, count + 1);
            Timer.run(() -> attempts.remove(ip), 1, TimeUnit.MINUTES);
        }
        
        chain.doFilter(request, response);
    }
}
```

---

## 🧪 Tester avant Production

### Checklist
- [ ] Tous les tests unitaires passent: `mvn test`
- [ ] Build JAR réussit: `mvn clean package`
- [ ] Application démarre sans erreurs
- [ ] Endpoints testés avec curl/Postman
- [ ] Load test (50+ requêtes simultanées): `ab -n 100 -c 50 http://localhost:8080/api/signalements`
- [ ] SSL/HTTPS vérifié
- [ ] JWT token renouvellement testé
- [ ] Backup/Recovery plan testé
- [ ] Monitoring setup (logs, metrics, alertes)

### Load Testing avec Apache Bench
```bash
ab -n 1000 -c 100 http://localhost:8080/api/signalements
```

---

## 📋 Checklist Pre-Deployment

```bash
#!/bin/bash
# pre-deploy-checks.sh

echo "🔍 Checking code quality..."
mvn clean verify -DskipTests

echo "✅ Running unit tests..."
mvn test

echo "📦 Building JAR..."
mvn clean package

echo "🔐 Verifying secrets NOT in code..."
grep -r "password=" src/ && echo "⚠️  WARNING: Secrets found in code!" && exit 1

echo "✅ All checks passed!"
```

---

## 🚨 Incident Response

### Erreur ConnectException (BD DOWN)
1. Vérifier: `ping ${DB_HOST}`
2. Vérifier credentials: `psql -h HOST -U USER -d DB`
3. Vérifier firewall/routes
4. Basculer sur replica (si HA setup)

### Erreur JWT Expired / Invalid
1. Vérifier que les tokens client sont valides
2. Vérifier que `jwt.secret` n'a pas changé
3. Augmenter `jwt.expiration` si nécessaire
4. Notifier utilisateurs de se réconnecter

### Espace disque faible
1. Archiver anciens logs: `tar -czf logs-archive-$(date +%Y%m).tar.gz /var/log/baladi/`
2. Nettoyer cache temporaire: `rm -rf /tmp/baladi-*`
3. Ajouter espace disque (cloud)

### Trop de connexions BD
1. Augmenter `maximum-pool-size` dans HikariCP
2. Vérifier pour les connexions ouvertes non fermées
3. Upgrade (plan Supabase)
4. Implémenter connection pooling côté app

---

## 📞 Support Production

**Contacts Emergency (24/7)**:
- DevOps Lead: +222 XX XX XX XX
- DB Admin: +222 XX XX XX XX
- Security Officer: security@baladi.mr

**Docs References**:
- Spring Boot: https://spring.io/projects/spring-boot
- PostgreSQL: https://www.postgresql.org/docs/
- Supabase: https://supabase.com/docs
- JWT Best Practices: https://tools.ietf.org/html/rfc8725

---

Setup by: Groupe G11  
Last Updated: 25 Avril 2026
