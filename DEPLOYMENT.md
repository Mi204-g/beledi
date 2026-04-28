# 🚀 DEPLOYMENT GUIDE — Beledi Platform

> ⚠️ **Production Environment Setup Guide**
> Last Updated: April 28, 2026

---

## 🔒 Security — Critical Points

### 1. JWT Secret Management
**NEVER hardcode the secret in the source code!**

**Before (DANGEROUS)**:
```properties
jwt.secret=belediSecretKey2024GroupeG11SupnumMauritanie_SuperSecure!
```

**After (SECURE)**:
Use environment variables.

```bash
# In the shell before launching the app
export JWT_SECRET="$(openssl rand -base64 32)"

# Or in Docker: -e JWT_SECRET=...
# Or in K8s: envFrom.secretRef
```

Update `application.properties`:
```properties
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000
```

### 2. Database Credentials
Use environment variables for all sensitive database information.

```properties
# application.properties
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

### 3. HTTPS / SSL
Mandatory for any production deployment to protect data in transit.

```properties
server.ssl.enabled=true
server.ssl.key-store=${SSL_KEYSTORE_PATH}
server.ssl.key-store-password=${SSL_KEYSTORE_PASSWORD}
server.ssl.key-store-type=PKCS12
server.ssl.key-alias=tomcat
```

---

## 🗄️ Database Configuration

### Recommended: PostgreSQL (e.g., Supabase)

```properties
# PostgreSQL Configuration
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}?sslmode=require

# Hibernate Optimization
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate  # ⚠️ NEVER use 'update' or 'create' in production!
spring.jpa.show-sql=false               # Disable verbose logging
```

### Schema Migration
1. **Initial Export**: Extract the schema from your H2 development database.
2. **Versioning**: It is highly recommended to use **Flyway** or **Liquibase**.

```xml
<!-- Add to pom.xml for Flyway -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
```

### Backup Strategy
```bash
# Regular PostgreSQL dump
pg_dump -h $DB_HOST -U $DB_USER -d $DB_NAME > backup_$(date +%Y%m%d).sql
```

---

## 🚀 Deployment Options

### Option 1: Docker (Recommended)

**Dockerfile**:
```dockerfile
# Build Stage
FROM maven:3.9.6-eclipse-temurin-21-headless AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run Stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-Xmx512m", "-jar", "app.jar"]
```

**Build & Run**:
```bash
docker build -t beledi:latest .
docker run -p 8080:8080 \
           -e JWT_SECRET="your_secret" \
           -e DB_URL="jdbc:postgresql://host:port/db" \
           -e DB_USER="user" \
           -e DB_PASSWORD="password" \
           beledi:latest
```

### Option 2: VPS (Ubuntu/Debian)

```bash
# 1. Install Java 21
sudo apt update && sudo apt install openjdk-21-jdk -y

# 2. Build the JAR locally
mvn clean package -DskipTests

# 3. Transfer to server
scp target/beledi-0.0.1-SNAPSHOT.jar user@vps:/opt/beledi/

# 4. Run as a service (recommended) or via CLI
java -Xmx1G -jar /opt/beledi/beledi-0.0.1-SNAPSHOT.jar
```

---

## 📊 Monitoring & Maintenance

### Logging Configuration
```properties
logging.level.root=INFO
logging.level.com.beledi=INFO
logging.file.name=/var/log/beledi/app.log
logging.file.max-size=10MB
logging.file.max-history=30
```

### Health Checks (Spring Boot Actuator)
Add the dependency to `pom.xml`:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

Configure endpoints:
```properties
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=always
```

---

## 🧪 Pre-Production Checklist

- [ ] **Unit Tests**: All tests passed (`mvn test`).
- [ ] **Build**: JAR generated without errors (`mvn clean package`).
- [ ] **Secrets**: No passwords or keys hardcoded in the repository.
- [ ] **CORS**: Restricted to authorized production domains.
- [ ] **SSL**: HTTPS certificate is valid and active.
- [ ] **Backups**: Database backup procedure is tested and working.

---

## 🚨 Incident Response

| Issue | Action |
| :--- | :--- |
| **Database Down** | Check `ping ${DB_HOST}` and verify credentials via `psql`. |
| **JWT Errors** | Ensure `jwt.secret` is consistent across all application instances. |
| **Disk Full** | Archive old logs in `/var/log/beledi/` and clear `/tmp`. |
| **Performance Lag** | Check HikariCP connection pool metrics and database indexes. |

---

## 📞 Support & Credits

**Developed by**: Groupe G11 - Supnum 2026

**Official Documentation**:
- [Spring Boot Production Ready](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-features.html)
- [PostgreSQL Optimization](https://www.postgresql.org/docs/current/performance-tips.html)

---
*Last Updated by Junie - April 2026*
