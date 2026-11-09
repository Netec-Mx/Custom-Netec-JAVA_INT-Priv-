# Práctica: Modularización de una aplicación con JPMS en Java SE 11

## Metadatos

| Campo | Valor |
|---|---|
| **Duración** | 105 minutos |
| **Complejidad** | Alta |
| **Nivel Bloom** | Crear |
| **Tecnologías** | JPMS (Java SE 11), module-info.java, javac 11.0.23, java 11.0.23, ServiceLoader API, Maven 3.9.8 |

## Descripción General

En este laboratorio dividirás una aplicación monolítica de librería en línea (`bookstore-legacy`), que funciona completamente sobre el ClassPath, en cinco módulos JPMS independientes. Declararás directivas `requires`, `exports`, `opens`, `uses` y `provides` en cada `module-info.java`, compilarás y ejecutarás desde el ModulePath, resolverás una dependencia cíclica introducida deliberadamente y aplicarás la estrategia de migración automática para integrar un JAR de terceros sin descriptor de módulo.

## Objetivos de Aprendizaje

- [ ] Analizar la estructura de una aplicación monolítica e identificar las dependencias entre capas para diseñar un grafo de módulos JPMS.
- [ ] Declarar correctamente las directivas `requires`, `exports`, `opens`, `uses` y `provides` en archivos `module-info.java` para cinco módulos del proyecto.
- [ ] Compilar un proyecto multi-módulo con `javac --module-source-path` y ejecutarlo con `java --module-path --module`, diagnosticando errores de resolución.
- [ ] Identificar y resolver una dependencia cíclica entre módulos mediante la extracción de interfaces a un módulo de API común.
- [ ] Integrar una dependencia de terceros sin `module-info.java` como módulo automático, aplicando la estrategia de migración desde Java 8.

## Prerrequisitos

**Conocimientos:**
- Haber completado el Lab 06-00-01 (familiaridad con el entorno de desarrollo y terminal).
- Comprensión de la estructura de paquetes y modificadores de acceso en Java.
- Conceptos básicos de compilación y ejecución desde línea de comandos.

**Acceso y herramientas:**
- JDK Eclipse Temurin 11.0.23+9 con `javac`, `java` y `jar` accesibles desde terminal.
- IntelliJ IDEA Community Edition 2024.1.4.
- Apache Maven 3.9.8 con `maven-compiler-plugin` 3.13.0.
- Directorio de trabajo: `~/cursos/java-se11-labs` (Linux/macOS) o `C:\cursos\java-se11-labs` (Windows).

## Entorno de Laboratorio

**Software requerido:**

| Herramienta | Versión exacta | Propósito |
|---|---|---|
| JDK Eclipse Temurin | 11.0.23+9 | Compilación y ejecución modular |
| Apache Maven | 3.9.8 | Gestión de compilación |
| IntelliJ IDEA CE | 2024.1.4 | IDE de desarrollo |
| Git | 2.45.2 | Control de versiones |

**Estructura de directorios del laboratorio:**

```
~/cursos/java-se11-labs/
├── pom.xml                          (proyecto existente del curso)
├── bookstore-legacy/                (aplicación monolítica - punto de partida)
└── bookstore-modular/               (proyecto multi-módulo - resultado final)
    └── src/
        ├── com.bookstore.model/
        ├── com.bookstore.api/
        ├── com.bookstore.repository/
        ├── com.bookstore.service/
        └── com.bookstore.app/
```

## Instrucciones Paso a Paso

---

### Paso 1: Verificar el entorno y las herramientas del JDK modular

**Objetivo:** Confirmar que el JDK 11 está correctamente instalado y que las herramientas de línea de comandos soportan el sistema modular.

1. Abre una terminal y verifica la versión del JDK:

```bash
java -version
```

2. Verifica que `javac` también corresponde a la versión 11:

```bash
javac -version
```

3. Lista los módulos disponibles en el JDK instalado:

```bash
java --list-modules
```

4. Cuenta el número total de módulos del JDK:

```bash
java --list-modules | wc -l
```

**Salida esperada:**

```
openjdk version "11.0.23" 2024-04-16
OpenJDK Runtime Environment Temurin-11.0.23+9 (build 11.0.23+9)
```

Para `--list-modules`, verás una lista de aproximadamente 70 módulos que comienzan con `java.base`, `java.sql`, `java.logging`, etc.

**Verificación:** La versión reportada es `11.0.23` y el comando `--list-modules` muestra módulos con prefijo `java.*` y `jdk.*`. Esto confirma que el JDK es modular.

---

### Paso 2: Inspeccionar módulos clave del JDK con --describe-module

**Objetivo:** Usar `--describe-module` para comprender qué exporta y requiere cada módulo del JDK que utilizará la aplicación.

1. Describe el módulo `java.sql`:

```bash
java --describe-module java.sql
```

2. Describe el módulo `java.logging`:

```bash
java --describe-module java.logging
```

3. Describe el módulo `java.base`:

```bash
java --describe-module java.base
```

**Salida esperada (java.sql):**

```
java.sql jar:file:///...
exports java.sql
exports javax.sql
requires java.base mandated
requires java.logging transitive
requires java.transaction.xa transitive
requires java.xml transitive
uses java.sql.Driver
```

**Verificación:** Observa que `java.sql` declara `requires java.logging transitive`, lo cual significa que cualquier módulo que requiera `java.sql` obtiene acceso automático a `java.logging`. Anota también que `java.sql` declara `uses java.sql.Driver` — un ejemplo real de SPI en el JDK.

---

### Paso 3: Crear la aplicación monolítica bookstore-legacy

**Objetivo:** Construir la aplicación de partida que simula una librería en línea con todas las capas en un único classpath sin módulos.

1. Navega al directorio del curso y crea la estructura del proyecto legado:

```bash
cd ~/cursos/java-se11-labs
mkdir -p bookstore-legacy/src/main/java/com/bookstore/model
mkdir -p bookstore-legacy/src/main/java/com/bookstore/repository
mkdir -p bookstore-legacy/src/main/java/com/bookstore/service
mkdir -p bookstore-legacy/src/main/java/com/bookstore/service/spi
mkdir -p bookstore-legacy/src/main/java/com/bookstore/service/impl
mkdir -p bookstore-legacy/src/main/java/com/bookstore/app
```

2. Crea el modelo de dominio `Book.java`:

```java
// bookstore-legacy/src/main/java/com/bookstore/model/Book.java
package com.bookstore.model;

import java.io.Serializable;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String isbn;
    private final String title;
    private final String author;
    private final double price;

    public Book(String isbn, String title, String author, double price) {
        this.isbn = isbn;
        this.title = title;
        this.author = author;
        this.price = price;
    }

    public String getIsbn()   { return isbn; }
    public String getTitle()  { return title; }
    public String getAuthor() { return author; }
    public double getPrice()  { return price; }

    @Override
    public String toString() {
        return String.format("Book[isbn=%s, title=%s, author=%s, price=%.2f]",
                isbn, title, author, price);
    }
}
```

3. Crea la interfaz del repositorio `BookRepository.java`:

```java
// bookstore-legacy/src/main/java/com/bookstore/repository/BookRepository.java
package com.bookstore.repository;

import com.bookstore.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    List<Book> findAll();
    Optional<Book> findByIsbn(String isbn);
    void save(Book book);
}
```

4. Crea la implementación en memoria `InMemoryBookRepository.java`:

```java
// bookstore-legacy/src/main/java/com/bookstore/repository/InMemoryBookRepository.java
package com.bookstore.repository;

import com.bookstore.model.Book;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class InMemoryBookRepository implements BookRepository {
    private static final Logger LOG =
            Logger.getLogger(InMemoryBookRepository.class.getName());
    private final List<Book> store = new ArrayList<>();

    public InMemoryBookRepository() {
        store.add(new Book("978-0-13-468599-1",
                "The Java Module System", "Nicolai Parlog", 49.99));
        store.add(new Book("978-0-596-00712-6",
                "Head First Design Patterns", "Eric Freeman", 39.95));
        store.add(new Book("978-0-13-685991-7",
                "Effective Java", "Joshua Bloch", 44.99));
        LOG.info("InMemoryBookRepository inicializado con " + store.size() + " libros");
    }

    @Override
    public List<Book> findAll() { return List.copyOf(store); }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return store.stream()
                .filter(b -> b.getIsbn().equals(isbn))
                .findFirst();
    }

    @Override
    public void save(Book book) {
        store.add(book);
        LOG.info("Libro guardado: " + book.getIsbn());
    }
}
```

5. Crea la interfaz SPI para proveedores de descuento `DiscountProvider.java`:

```java
// bookstore-legacy/src/main/java/com/bookstore/service/spi/DiscountProvider.java
package com.bookstore.service.spi;

import com.bookstore.model.Book;

public interface DiscountProvider {
    double calculateDiscount(Book book);
    String providerName();
}
```

6. Crea la implementación de descuento estacional `SeasonalDiscountProvider.java`:

```java
// bookstore-legacy/src/main/java/com/bookstore/service/impl/SeasonalDiscountProvider.java
package com.bookstore.service.impl;

import com.bookstore.model.Book;
import com.bookstore.service.spi.DiscountProvider;

public class SeasonalDiscountProvider implements DiscountProvider {
    @Override
    public double calculateDiscount(Book book) {
        return book.getPrice() * 0.15; // 15% de descuento estacional
    }

    @Override
    public String providerName() {
        return "Descuento Estacional (15%)";
    }
}
```

7. Crea el servicio de librería `BookService.java`:

```java
// bookstore-legacy/src/main/java/com/bookstore/service/BookService.java
package com.bookstore.service;

import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import com.bookstore.service.spi.DiscountProvider;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Logger;

public class BookService {
    private static final Logger LOG =
            Logger.getLogger(BookService.class.getName());
    private final BookRepository repository;

    public BookService(BookRepository repository) {
        this.repository = repository;
    }

    public List<Book> listBooks() {
        return repository.findAll();
    }

    public Optional<Book> findBook(String isbn) {
        return repository.findByIsbn(isbn);
    }

    public void addBook(Book book) {
        repository.save(book);
    }

    public void applyDiscounts() {
        ServiceLoader<DiscountProvider> providers =
                ServiceLoader.load(DiscountProvider.class);
        List<Book> books = repository.findAll();

        for (DiscountProvider provider : providers) {
            LOG.info("Proveedor encontrado: " + provider.providerName());
            for (Book book : books) {
                double discount = provider.calculateDiscount(book);
                LOG.info(String.format("  %s -> descuento: $%.2f",
                        book.getTitle(), discount));
            }
        }
    }
}
```

8. Crea la clase principal `Main.java`:

```java
// bookstore-legacy/src/main/java/com/bookstore/app/Main.java
package com.bookstore.app;

import com.bookstore.model.Book;
import com.bookstore.repository.InMemoryBookRepository;
import com.bookstore.service.BookService;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Bookstore Legacy (ClassPath) ===");

        BookService service = new BookService(new InMemoryBookRepository());

        List<Book> books = service.listBooks();
        System.out.println("\nCatálogo de libros:");
        books.forEach(b -> System.out.println("  " + b));

        service.findBook("978-0-13-685991-7")
                .ifPresent(b -> System.out.println("\nBúsqueda por ISBN: " + b));

        System.out.println("\nAplicando descuentos via ServiceLoader:");
        service.applyDiscounts();

        System.out.println("\n=== Fin ===");
    }
}
```

**Salida esperada:** Archivos creados en la estructura `bookstore-legacy/`.

**Verificación:** Ejecuta `find bookstore-legacy/src -name "*.java" | wc -l` y confirma que hay **7 archivos** Java.

---

### Paso 4: Compilar y ejecutar la aplicación legado en ClassPath

**Objetivo:** Demostrar que la aplicación funciona completamente en el ClassPath tradicional, sin módulos.

1. Compila todos los archivos Java desde el directorio raíz del proyecto legado:

```bash
cd ~/cursos/java-se11-labs/bookstore-legacy
mkdir -p out
javac -d out $(find src -name "*.java")
```

2. Ejecuta la aplicación desde el ClassPath:

```bash
java -cp out com.bookstore.app.Main
```

**Salida esperada:**

```
=== Bookstore Legacy (ClassPath) ===

Catálogo de libros:
  Book[isbn=978-0-13-468599-1, title=The Java Module System, author=Nicolai Parlog, price=49.99]
  Book[isbn=978-0-596-00712-6, title=Head First Design Patterns, author=Eric Freeman, price=39.95]
  Book[isbn=978-0-13-685991-7, title=Effective Java, author=Joshua Bloch, price=44.99]

Búsqueda por ISBN: Book[isbn=978-0-13-685991-7, title=Effective Java, author=Joshua Bloch, price=44.99]

Aplicando descuentos via ServiceLoader:

=== Fin ===
```

**Verificación:** La aplicación compila y ejecuta sin errores. Observa que `ServiceLoader` **no encuentra ningún proveedor** porque no existe el archivo `META-INF/services/` en el classpath. Este problema se resolverá con la directiva `provides` en el sistema modular.

---

### Paso 5: Identificar dependencias entre capas y documentar el grafo

**Objetivo:** Analizar las dependencias entre paquetes de la aplicación legado para planificar la modularización.

1. Usa `jdeps` para analizar las dependencias de la aplicación compilada:

```bash
jdeps --class-path out -recursive -summary out
```

2. Genera un análisis detallado por paquete:

```bash
jdeps --class-path out -verbose:package out
```

3. Documenta el grafo de dependencias identificado. Crea un archivo de notas:

```bash
cat > ~/cursos/java-se11-labs/bookstore-legacy/dependency-graph.txt << 'EOF'
Grafo de dependencias de bookstore-legacy:
==========================================

com.bookstore.app     --> com.bookstore.model
com.bookstore.app     --> com.bookstore.repository
com.bookstore.app     --> com.bookstore.service

com.bookstore.service --> com.bookstore.model
com.bookstore.service --> com.bookstore.repository
com.bookstore.service --> com.bookstore.service.spi

com.bookstore.service.impl --> com.bookstore.model
com.bookstore.service.impl --> com.bookstore.service.spi

com.bookstore.repository --> com.bookstore.model

Módulos JDK requeridos:
  - java.base     (implícito: List, Optional, String, etc.)
  - java.logging  (java.util.logging.Logger)
  - java.sql      (futuro: acceso a datos real)

Plan de módulos:
  1. com.bookstore.model      -> exports com.bookstore.model
  2. com.bookstore.repository -> requires com.bookstore.model, requires java.logging
  3. com.bookstore.service    -> requires com.bookstore.model, uses DiscountProvider
  4. com.bookstore.app        -> requires todos los anteriores
EOF
```

**Salida esperada:** `jdeps` muestra las dependencias entre paquetes y hacia módulos del JDK (`java.base`, `java.logging`).

**Verificación:** El grafo confirma que `com.bookstore.model` es la capa base sin dependencias hacia otras capas de la aplicación. Esta es la capa que se modularizará primero.

---

### Paso 6: Crear la estructura del proyecto multi-módulo

**Objetivo:** Establecer la estructura de directorios para cinco módulos JPMS bajo `bookstore-modular/`.

1. Regresa al directorio raíz y crea la estructura multi-módulo:

```bash
cd ~/cursos/java-se11-labs
mkdir -p bookstore-modular/src/com.bookstore.model/com/bookstore/model
mkdir -p bookstore-modular/src/com.bookstore.repository/com/bookstore/repository
mkdir -p bookstore-modular/src/com.bookstore.service/com/bookstore/service/spi
mkdir -p bookstore-modular/src/com.bookstore.service/com/bookstore/service/impl
mkdir -p bookstore-modular/src/com.bookstore.app/com/bookstore/app
```

2. Verifica la estructura creada:

```bash
find bookstore-modular/src -type d | head -20
```

**Salida esperada:**

```
bookstore-modular/src
bookstore-modular/src/com.bookstore.model
bookstore-modular/src/com.bookstore.model/com
bookstore-modular/src/com.bookstore.model/com/bookstore
bookstore-modular/src/com.bookstore.model/com/bookstore/model
bookstore-modular/src/com.bookstore.repository
...
```

**Verificación:** Cada directorio de primer nivel bajo `src/` lleva el nombre completo del módulo (por ejemplo, `com.bookstore.model`). Esta convención es obligatoria para `--module-source-path`.

---

### Paso 7: Crear el módulo com.bookstore.model

**Objetivo:** Definir el módulo de entidades de dominio con su `module-info.java` y la clase `Book`.

1. Copia la clase `Book.java` al módulo:

```bash
cp bookstore-legacy/src/main/java/com/bookstore/model/Book.java \
   bookstore-modular/src/com.bookstore.model/com/bookstore/model/Book.java
```

2. Crea el descriptor de módulo `module-info.java`:

```java
// bookstore-modular/src/com.bookstore.model/module-info.java
module com.bookstore.model {
    exports com.bookstore.model;
    opens com.bookstore.model;  // Permite acceso reflectivo (serialización, frameworks)
}
```

**Salida esperada:** Dos archivos en el módulo `com.bookstore.model`.

**Verificación:** La directiva `exports` hace que `com.bookstore.model` sea accesible para otros módulos en tiempo de compilación y ejecución. La directiva `opens` permite adicionalmente el acceso reflectivo en tiempo de ejecución, necesario para frameworks de serialización.

---

### Paso 8: Crear el módulo com.bookstore.repository

**Objetivo:** Definir el módulo de acceso a datos que depende del modelo y de `java.logging`.

1. Copia las clases del repositorio:

```bash
cp bookstore-legacy/src/main/java/com/bookstore/repository/BookRepository.java \
   bookstore-modular/src/com.bookstore.repository/com/bookstore/repository/
cp bookstore-legacy/src/main/java/com/bookstore/repository/InMemoryBookRepository.java \
   bookstore-modular/src/com.bookstore.repository/com/bookstore/repository/
```

2. Crea el descriptor de módulo:

```java
// bookstore-modular/src/com.bookstore.repository/module-info.java
module com.bookstore.repository {
    requires com.bookstore.model;
    requires java.logging;

    exports com.bookstore.repository;
}
```

**Salida esperada:** Tres archivos en el módulo `com.bookstore.repository`.

**Verificación:** El módulo declara `requires com.bookstore.model` porque `BookRepository` usa la clase `Book`, y `requires java.logging` porque `InMemoryBookRepository` usa `java.util.logging.Logger`. El paquete `com.bookstore.repository` se exporta para que el servicio y la app puedan acceder a él.

---

### Paso 9: Crear el módulo com.bookstore.service con SPI

**Objetivo:** Definir el módulo de lógica de negocio que declara `uses` para el SPI de descuentos y `provides` para la implementación.

1. Copia las clases del servicio:

```bash
cp bookstore-legacy/src/main/java/com/bookstore/service/BookService.java \
   bookstore-modular/src/com.bookstore.service/com/bookstore/service/
cp bookstore-legacy/src/main/java/com/bookstore/service/spi/DiscountProvider.java \
   bookstore-modular/src/com.bookstore.service/com/bookstore/service/spi/
cp bookstore-legacy/src/main/java/com/bookstore/service/impl/SeasonalDiscountProvider.java \
   bookstore-modular/src/com.bookstore.service/com/bookstore/service/impl/
```

2. Crea el descriptor de módulo:

```java
// bookstore-modular/src/com.bookstore.service/module-info.java
module com.bookstore.service {
    requires com.bookstore.model;
    requires com.bookstore.repository;
    requires java.logging;

    exports com.bookstore.service;
    exports com.bookstore.service.spi;

    uses com.bookstore.service.spi.DiscountProvider;

    provides com.bookstore.service.spi.DiscountProvider
        with com.bookstore.service.impl.SeasonalDiscountProvider;
}
```

**Salida esperada:** Cuatro archivos en el módulo `com.bookstore.service`.

**Verificación:** La directiva `uses` declara que este módulo consumirá implementaciones de `DiscountProvider` vía `ServiceLoader`. La directiva `provides...with` registra `SeasonalDiscountProvider` como implementación concreta. A diferencia del classpath, donde se necesitaba `META-INF/services/`, JPMS gestiona esto de forma declarativa.

---

### Paso 10: Crear el módulo com.bookstore.app

**Objetivo:** Definir el módulo principal que agrega todas las dependencias y contiene el punto de entrada.

1. Copia la clase principal:

```bash
cp bookstore-legacy/src/main/java/com/bookstore/app/Main.java \
   bookstore-modular/src/com.bookstore.app/com/bookstore/app/
```

2. Crea el descriptor de módulo:

```java
// bookstore-modular/src/com.bookstore.app/module-info.java
module com.bookstore.app {
    requires com.bookstore.model;
    requires com.bookstore.repository;
    requires com.bookstore.service;
}
```

**Salida esperada:** Dos archivos en el módulo `com.bookstore.app`.

**Verificación:** Este módulo no exporta nada (es el módulo de nivel superior) y requiere los tres módulos de la aplicación. `java.base` se requiere implícitamente.

---

### Paso 11: Compilar el proyecto multi-módulo completo

**Objetivo:** Usar `javac --module-source-path` para compilar todos los módulos simultáneamente.

1. Navega al directorio del proyecto modular:

```bash
cd ~/cursos/java-se11-labs/bookstore-modular
```

2. Compila todos los módulos:

```bash
javac -d out --module-source-path src $(find src -name "*.java")
```

**Salida esperada:** Sin errores. Se crea el directorio `out/` con subdirectorios para cada módulo.

3. Verifica la estructura de salida:

```bash
find out -type f -name "*.class" | sort
```

**Salida esperada:**

```
out/com.bookstore.app/com/bookstore/app/Main.class
out/com.bookstore.app/module-info.class
out/com.bookstore.model/com/bookstore/model/Book.class
out/com.bookstore.model/module-info.class
out/com.bookstore.repository/com/bookstore/repository/BookRepository.class
out/com.bookstore.repository/com/bookstore/repository/InMemoryBookRepository.class
out/com.bookstore.repository/module-info.class
out/com.bookstore.service/com/bookstore/service/BookService.class
out/com.bookstore.service/com/bookstore/service/impl/SeasonalDiscountProvider.class
out/com.bookstore.service/com/bookstore/service/spi/DiscountProvider.class
out/com.bookstore.service/module-info.class
```

**Verificación:** Hay exactamente **11 archivos `.class`** (7 clases + 4 `module-info.class`). Cada módulo tiene su propio directorio de salida.

---

### Paso 12: Ejecutar la aplicación desde el ModulePath

**Objetivo:** Ejecutar la aplicación modularizada usando `--module-path` y `--module`.

1. Ejecuta la aplicación especificando el módulo y la clase principal:

```bash
java --module-path out --module com.bookstore.app/com.bookstore.app.Main
```

**Salida esperada:**

```
=== Bookstore Legacy (ClassPath) ===

Catálogo de libros:
  Book[isbn=978-0-13-468599-1, title=The Java Module System, author=Nicolai Parlog, price=49.99]
  Book[isbn=978-0-596-00712-6, title=Head First Design Patterns, author=Eric Freeman, price=39.95]
  Book[isbn=978-0-13-685991-7, title=Effective Java, author=Joshua Bloch, price=44.99]

Búsqueda por ISBN: Book[isbn=978-0-13-685991-7, title=Effective Java, author=Joshua Bloch, price=44.99]

Aplicando descuentos via ServiceLoader:

=== Fin ===
```

**Verificación:** La aplicación funciona idénticamente al modo ClassPath. Sin embargo, observa que `ServiceLoader` **aún no encuentra el proveedor**. Esto se debe a que `ServiceLoader.load()` en el código legado carga desde el classpath del hilo actual. En el siguiente paso corregiremos esto.

---

### Paso 13: Corregir el ServiceLoader para el contexto modular

**Objetivo:** Adaptar `BookService` para que `ServiceLoader` funcione correctamente en el contexto modular.

1. Edita `BookService.java` en el módulo de servicio para usar la versión modular de `ServiceLoader`:

```java
// bookstore-modular/src/com.bookstore.service/com/bookstore/service/BookService.java
package com.bookstore.service;

import com.bookstore.model.Book;
import com.bookstore.repository.BookRepository;
import com.bookstore.service.spi.DiscountProvider;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BookService {
    private static final Logger LOG =
            Logger.getLogger(BookService.class.getName());
    private final BookRepository repository;
    private final List<DiscountProvider> discountProviders;

    public BookService(BookRepository repository) {
        this.repository = repository;
        this.discountProviders = ServiceLoader
                .load(DiscountProvider.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toList());
        LOG.info("Proveedores de descuento cargados: " + discountProviders.size());
    }

    public List<Book> listBooks() {
        return repository.findAll();
    }

    public Optional<Book> findBook(String isbn) {
        return repository.findByIsbn(isbn);
    }

    public void addBook(Book book) {
        repository.save(book);
    }

    public void applyDiscounts() {
        List<Book> books = repository.findAll();
        if (discountProviders.isEmpty()) {
            LOG.warning("No se encontraron proveedores de descuento");
            return;
        }
        for (DiscountProvider provider : discountProviders) {
            System.out.println("  Proveedor: " + provider.providerName());
            for (Book book : books) {
                double discount = provider.calculateDiscount(book);
                System.out.printf("    %s -> descuento: $%.2f (precio final: $%.2f)%n",
                        book.getTitle(), discount, book.getPrice() - discount);
            }
        }
    }
}
```

2. Actualiza `Main.java` para mostrar el título correcto:

```java
// bookstore-modular/src/com.bookstore.app/com/bookstore/app/Main.java
package com.bookstore.app;

import com.bookstore.model.Book;
import com.bookstore.repository.InMemoryBookRepository;
import com.bookstore.service.BookService;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Bookstore Modular (JPMS) ===");

        BookService service = new BookService(new InMemoryBookRepository());

        List<Book> books = service.listBooks();
        System.out.println("\nCatálogo de libros:");
        books.forEach(b -> System.out.println("  " + b));

        service.findBook("978-0-13-685991-7")
                .ifPresent(b -> System.out.println("\nBúsqueda por ISBN: " + b));

        System.out.println("\nAplicando descuentos via ServiceLoader (JPMS):");
        service.applyDiscounts();

        System.out.println("\n=== Fin ===");
    }
}
```

3. Recompila y ejecuta:

```bash
javac -d out --module-source-path src $(find src -name "*.java")
java --module-path out --module com.bookstore.app/com.bookstore.app.Main
```

**Salida esperada:**

```
=== Bookstore Modular (JPMS) ===

Catálogo de libros:
  Book[isbn=978-0-13-468599-1, title=The Java Module System, author=Nicolai Parlog, price=49.99]
  Book[isbn=978-0-596-00712-6, title=Head First Design Patterns, author=Eric Freeman, price=39.95]
  Book[isbn=978-0-13-685991-7, title=Effective Java, author=Joshua Bloch, price=44.99]

Búsqueda por ISBN: Book[isbn=978-0-13-685991-7, title=Effective Java, author=Joshua Bloch, price=44.99]

Aplicando descuentos via ServiceLoader (JPMS):
  Proveedor: Descuento Estacional (15%)
    The Java Module System -> descuento: $7.50 (precio final: $42.49)
    Head First Design Patterns -> descuento: $5.99 (precio final: $33.96)
    Effective Java -> descuento: $6.75 (precio final: $38.24)

=== Fin ===
```

**Verificación:** Ahora `ServiceLoader` **sí encuentra** el `SeasonalDiscountProvider` gracias a la directiva `provides...with` en `module-info.java`. Esta es la diferencia clave entre el mecanismo SPI en ClassPath (archivo `META-INF/services/`) y en JPMS (declarativo en el descriptor de módulo).

---

### Paso 14: Inspeccionar los módulos del proyecto compilado

**Objetivo:** Usar `--describe-module` y `jar` para inspeccionar los módulos compilados del proyecto.

1. Describe el módulo `com.bookstore.service` desde los archivos compilados:

```bash
java --module-path out --describe-module com.bookstore.service
```

**Salida esperada:**

```
com.bookstore.service file:///...out/com.bookstore.service/
requires com.bookstore.model
requires com.bookstore.repository
requires java.logging
requires java.base mandated
exports com.bookstore.service
exports com.bookstore.service.spi
uses com.bookstore.service.spi.DiscountProvider
provides com.bookstore.service.spi.DiscountProvider with com.bookstore.service.impl.SeasonalDiscountProvider
```

2. Describe el módulo `com.bookstore.model`:

```bash
java --module-path out --describe-module com.bookstore.model
```

**Salida esperada:**

```
com.bookstore.model file:///...out/com.bookstore.model/
requires java.base mandated
exports com.bookstore.model
opens com.bookstore.model
```

**Verificación:** La salida confirma que las directivas declaradas en cada `module-info.java` son reconocidas correctamente por la JVM. Observa que `java.base mandated` aparece automáticamente sin haberlo declarado.

---

### Paso 15: Demostrar la encapsulación fuerte — acceso denegado

**Objetivo:** Verificar que el sistema modular impide el acceso a paquetes no exportados.

1. Crea una clase de prueba que intente acceder al paquete interno `com.bookstore.service.impl`:

```java
// bookstore-modular/src/com.bookstore.app/com/bookstore/app/EncapsulationTest.java
package com.bookstore.app;

// Intento de importar una clase de un paquete NO exportado
import com.bookstore.service.impl.SeasonalDiscountProvider;

public class EncapsulationTest {
    public static void main(String[] args) {
        SeasonalDiscountProvider provider = new SeasonalDiscountProvider();
        System.out.println(provider.providerName());
    }
}
```

2. Intenta compilar:

```bash
javac -d out --module-source-path src $(find src -name "*.java")
```

**Salida esperada (ERROR):**

```
src/com.bookstore.app/com/bookstore/app/EncapsulationTest.java:4: error: package com.bookstore.service.impl is not visible
import com.bookstore.service.impl.SeasonalDiscountProvider;
                                 ^
  (package com.bookstore.service.impl is declared in module com.bookstore.service, which does not export it)
1 error
```

3. Elimina el archivo de prueba para restaurar la compilación:

```bash
rm bookstore-modular/src/com.bookstore.app/com/bookstore/app/EncapsulationTest.java
```

**Verificación:** El compilador rechaza el acceso al paquete `com.bookstore.service.impl` porque el módulo `com.bookstore.service` solo exporta `com.bookstore.service` y `com.bookstore.service.spi`. Esta es la **encapsulación fuerte** que JPMS proporciona y que no existía en el ClassPath.

---

### Paso 16: Introducir una dependencia cíclica deliberada

**Objetivo:** Simular un escenario real donde dos módulos se requieren mutuamente, diagnosticar el error y planificar la solución.

1. Modifica `module-info.java` del módulo `com.bookstore.repository` para agregar una dependencia hacia `com.bookstore.service`:

```java
// bookstore-modular/src/com.bookstore.repository/module-info.java
// VERSIÓN CON CICLO - DELIBERADAMENTE INCORRECTA
module com.bookstore.repository {
    requires com.bookstore.model;
    requires com.bookstore.service;  // <-- CICLO: service ya requiere repository
    requires java.logging;

    exports com.bookstore.repository;
}
```

2. Intenta compilar:

```bash
javac -d out --module-source-path src $(find src -name "*.java")
```

**Salida esperada (ERROR):**

```
error: cyclic dependence involving com.bookstore.repository
  requires com.bookstore.service;
```

**Verificación:** El compilador detecta el ciclo `com.bookstore.repository → com.bookstore.service → com.bookstore.repository` y lo rechaza. En el ClassPath, este tipo de dependencia circular pasaría desapercibida hasta causar problemas en tiempo de ejecución.

---

### Paso 17: Resolver la dependencia cíclica con un módulo de API común

**Objetivo:** Extraer las interfaces compartidas a un nuevo módulo `com.bookstore.api` para romper el ciclo de dependencias.

1. Crea la estructura del nuevo módulo:

```bash
mkdir -p bookstore-modular/src/com.bookstore.api/com/bookstore/api
```

2. Crea la interfaz `BookRepository` en el módulo de API (moviéndola desde `repository`):

```java
// bookstore-modular/src/com.bookstore.api/com/bookstore/api/BookRepository.java
package com.bookstore.api;

import com.bookstore.model.Book;
import java.util.List;
import java.util.Optional;

public interface BookRepository {
    List<Book> findAll();
    Optional<Book> findByIsbn(String isbn);
    void save(Book book);
}
```

3. Crea el descriptor de módulo para `com.bookstore.api`:

```java
// bookstore-modular/src/com.bookstore.api/module-info.java
module com.bookstore.api {
    requires transitive com.bookstore.model;

    exports com.bookstore.api;
}
```

4. Actualiza `InMemoryBookRepository.java` para importar la interfaz desde el paquete API:

```java
// bookstore-modular/src/com.bookstore.repository/com/bookstore/repository/InMemoryBookRepository.java
package com.bookstore.repository;

import com.bookstore.api.BookRepository;
import com.bookstore.model.Book;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class InMemoryBookRepository implements BookRepository {
    private static final Logger LOG =
            Logger.getLogger(InMemoryBookRepository.class.getName());
    private final List<Book> store = new ArrayList<>();

    public InMemoryBookRepository() {
        store.add(new Book("978-0-13-468599-1",
                "The Java Module System", "Nicolai Parlog", 49.99));
        store.add(new Book("978-0-596-00712-6",
                "Head First Design Patterns", "Eric Freeman", 39.95));
        store.add(new Book("978-0-13-685991-7",
                "Effective Java", "Joshua Bloch", 44.99));
        LOG.info("InMemoryBookRepository inicializado con " + store.size() + " libros");
    }

    @Override
    public List<Book> findAll() { return List.copyOf(store); }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return store.stream()
                .filter(b -> b.getIsbn().equals(isbn))
                .findFirst();
    }

    @Override
    public void save(Book book) {
        store.add(book);
        LOG.info("Libro guardado: " + book.getIsbn());
    }
}
```

5. Elimina `BookRepository.java` del módulo repository (ahora está en API):

```bash
rm bookstore-modular/src/com.bookstore.repository/com/bookstore/repository/BookRepository.java
```

6. Actualiza el `module-info.java` de `com.bookstore.repository` (sin el ciclo):

```java
// bookstore-modular/src/com.bookstore.repository/module-info.java
module com.bookstore.repository {
    requires com.bookstore.api;
    requires java.logging;

    exports com.bookstore.repository;
}
```

7. Actualiza `BookService.java` para importar desde el paquete API:

```java
// bookstore-modular/src/com.bookstore.service/com/bookstore/service/BookService.java
package com.bookstore.service;

import com.bookstore.model.Book;
import com.bookstore.api.BookRepository;
import com.bookstore.service.spi.DiscountProvider;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class BookService {
    private static final Logger LOG =
            Logger.getLogger(BookService.class.getName());
    private final BookRepository repository;
    private final List<DiscountProvider> discountProviders;

    public BookService(BookRepository repository) {
        this.repository = repository;
        this.discountProviders = ServiceLoader
                .load(DiscountProvider.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toList());
        LOG.info("Proveedores de descuento cargados: " + discountProviders.size());
    }

    public List<Book> listBooks() {
        return repository.findAll();
    }

    public Optional<Book> findBook(String isbn) {
        return repository.findByIsbn(isbn);
    }

    public void addBook(Book book) {
        repository.save(book);
    }

    public void applyDiscounts() {
        List<Book> books = repository.findAll();
        if (discountProviders.isEmpty()) {
            LOG.warning("No se encontraron proveedores de descuento");
            return;
        }
        for (DiscountProvider provider : discountProviders) {
            System.out.println("  Proveedor: " + provider.providerName());
            for (Book book : books) {
                double discount = provider.calculateDiscount(book);
                System.out.printf("    %s -> descuento: $%.2f (precio final: $%.2f)%n",
                        book.getTitle(), discount, book.getPrice() - discount);
            }
        }
    }
}
```

8. Actualiza `module-info.java` de `com.bookstore.service`:

```java
// bookstore-modular/src/com.bookstore.service/module-info.java
module com.bookstore.service {
    requires com.bookstore.api;
    requires com.bookstore.repository;
    requires java.logging;

    exports com.bookstore.service;
    exports com.bookstore.service.spi;

    uses com.bookstore.service.spi.DiscountProvider;

    provides com.bookstore.service.spi.DiscountProvider
        with com.bookstore.service.impl.SeasonalDiscountProvider;
}
```

9. Actualiza `Main.java` para importar desde los paquetes correctos:

```java
// bookstore-modular/src/com.bookstore.app/com/bookstore/app/Main.java
package com.bookstore.app;

import com.bookstore.model.Book;
import com.bookstore.repository.InMemoryBookRepository;
import com.bookstore.service.BookService;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Bookstore Modular (JPMS - Ciclo Resuelto) ===");

        BookService service = new BookService(new InMemoryBookRepository());

        List<Book> books = service.listBooks();
        System.out.println("\nCatálogo de libros:");
        books.forEach(b -> System.out.println("  " + b));

        service.findBook("978-0-13-685991-7")
                .ifPresent(b -> System.out.println("\nBúsqueda por ISBN: " + b));

        System.out.println("\nAplicando descuentos via ServiceLoader (JPMS):");
        service.applyDiscounts();

        System.out.println("\n=== Fin ===");
    }
}
```

10. Actualiza `module-info.java` de `com.bookstore.app`:

```java
// bookstore-modular/src/com.bookstore.app/module-info.java
module com.bookstore.app {
    requires com.bookstore.model;
    requires com.bookstore.api;
    requires com.bookstore.repository;
    requires com.bookstore.service;
}
```

**Salida esperada:** Todos los archivos actualizados.

**Verificación:** El nuevo grafo de dependencias es acíclico:
- `com.bookstore.model` ← `com.bookstore.api` ← `com.bookstore.repository`
- `com.bookstore.model` ← `com.bookstore.api` ← `com.bookstore.service`
- Ambos fluyen hacia `com.bookstore.app` sin ciclos.

---

### Paso 18: Compilar y ejecutar la versión sin ciclos

**Objetivo:** Verificar que la resolución del ciclo compila y ejecuta correctamente.

1. Limpia la salida anterior y recompila:

```bash
rm -rf out
javac -d out --module-source-path src $(find src -name "*.java")
```

2. Ejecuta la aplicación:

```bash
java --module-path out --module com.bookstore.app/com.bookstore.app.Main
```

**Salida esperada:**

```
=== Bookstore Modular (JPMS - Ciclo Resuelto) ===

Catálogo de libros:
  Book[isbn=978-0-13-468599-1, title=The Java Module System, author=Nicolai Parlog, price=49.99]
  Book[isbn=978-0-596-00712-6, title=Head First Design Patterns, author=Eric Freeman, price=39.95]
  Book[isbn=978-0-13-685991-7, title=Effective Java, author=Joshua Bloch, price=44.99]

Búsqueda por ISBN: Book[isbn=978-0-13-685991-7, title=Effective Java, author=Joshua Bloch, price=44.99]

Aplicando descuentos via ServiceLoader (JPMS):
  Proveedor: Descuento Estacional (15%)
    The Java Module System -> descuento: $7.50 (precio final: $42.49)
    Head First Design Patterns -> descuento: $5.99 (precio final: $33.96)
    Effective Java -> descuento: $6.75 (precio final: $38.24)

=== Fin ===
```

**Verificación:** La aplicación funciona correctamente con cinco módulos y sin dependencias cíclicas. El `ServiceLoader` encuentra y aplica el proveedor de descuentos.

---

### Paso 19: Crear un JAR de utilidades sin module-info.java (simulando librería Java 8)

**Objetivo:** Simular una librería de terceros que no tiene `module-info.java` para practicar la migración con módulos automáticos.

1. Crea un directorio temporal para la librería:

```bash
mkdir -p ~/cursos/java-se11-labs/bookstore-utils/com/bookstore/utils
```

2. Crea una clase de utilidad:

```java
// ~/cursos/java-se11-labs/bookstore-utils/com/bookstore/utils/StringUtils.java
package com.bookstore.utils;

public class StringUtils {
    public static String toTitleCase(String input) {
        if (input == null || input.isEmpty()) return input;
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : input.toCharArray()) {
            if (Character.isWhitespace(c)) {
                capitalizeNext = true;
                result.append(c);
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(Character.toLowerCase(c));
            }
        }
        return result.toString();
    }

    public static String truncate(String input, int maxLength) {
        if (input == null || input.length() <= maxLength) return input;
        return input.substring(0, maxLength - 3) + "...";
    }
}
```

3. Compila la clase sin módulos (classpath puro):

```bash
cd ~/cursos/java-se11-labs/bookstore-utils
javac -d classes com/bookstore/utils/StringUtils.java
```

4. Empaqueta como JAR sin `module-info.java`:

```bash
jar --create --file bookstore.utils.jar -C classes .
```

5. Verifica que el JAR no contiene `module-info.class`:

```bash
jar --list --file bookstore.utils.jar
```

**Salida esperada:**

```
META-INF/
META-INF/MANIFEST.MF
com/
com/bookstore/
com/bookstore/utils/
com/bookstore/utils/StringUtils.class
```

**Verificación:** No aparece `module-info.class` en el listado. Este JAR se comportará como un **módulo automático** cuando se coloque en el module-path.

---

### Paso 20: Determinar el nombre del módulo automático

**Objetivo:** Usar `jar --describe-module` para conocer el nombre que JPMS asignará automáticamente al JAR.

1. Consulta el nombre del módulo automático:

```bash
jar --describe-module --file bookstore.utils.jar
```

**Salida esperada:**

```
No module descriptor found. Derived automatic module.

bookstore.utils automatic
requires java.base mandated
contains com.bookstore.utils
```

**Verificación:** El nombre derivado es `bookstore.utils` (basado en el nombre del archivo JAR, eliminando la extensión `.jar` y reemplazando caracteres no válidos). Este es el nombre que se usará en la directiva `requires`.

---

### Paso 21: Integrar el módulo automático en el proyecto

**Objetivo:** Usar la librería de terceros como módulo automático declarándola en `requires`.

1. Copia el JAR al directorio de librerías del proyecto:

```bash
mkdir -p ~/cursos/java-se11-labs/bookstore-modular/libs
cp ~/cursos/java-se11-labs/bookstore-utils/bookstore.utils.jar \
   ~/cursos/java-se11-labs/bookstore-modular/libs/
```

2. Actualiza `module-info.java` de `com.bookstore.app` para requerir el módulo automático:

```java
// bookstore-modular/src/com.bookstore.app/module-info.java
module com.bookstore.app {
    requires com.bookstore.model;
    requires com.bookstore.api;
    requires com.bookstore.repository;
    requires com.bookstore.service;
    requires bookstore.utils;  // Módulo automático (JAR sin module-info)
}
```

3. Actualiza `Main.java` para usar la utilidad:

```java
// bookstore-modular/src/com.bookstore.app/com/bookstore/app/Main.java
package com.bookstore.app;

import com.bookstore.model.Book;
import com.bookstore.repository.InMemoryBookRepository;
import com.bookstore.service.BookService;
import com.bookstore.utils.StringUtils;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Bookstore Modular (JPMS - con Módulo Automático) ===");

        BookService service = new BookService(new InMemoryBookRepository());

        List<Book> books = service.listBooks();
        System.out.println("\nCatálogo de libros (títulos en Title Case):");
        books.forEach(b -> System.out.printf("  [%s] %s por %s - $%.2f%n",
                b.getIsbn(),
                StringUtils.toTitleCase(b.getTitle()),
                b.getAuthor(),
                b.getPrice()));

        System.out.println("\nTítulos truncados (20 chars):");
        books.forEach(b -> System.out.println("  " +
                StringUtils.truncate(b.getTitle(), 20)));

        service.findBook("978-0-13-685991-7")
                .ifPresent(b -> System.out.println("\nBúsqueda: " + b));

        System.out.println("\nDescuentos via ServiceLoader:");
        service.applyDiscounts();

        System.out.println("\n=== Fin ===");
    }
}
```

4. Compila incluyendo el directorio de librerías en el module-path:

```bash
cd ~/cursos/java-se11-labs/bookstore-modular
rm -rf out
javac -d out --module-source-path src --module-path libs $(find src -name "*.java")
```

5. Ejecuta con el module-path combinado:

```bash
java --module-path out:libs --module com.bookstore.app/com.bookstore.app.Main
```

> **Nota (Windows):** Reemplaza `:` por `;` en el module-path: `--module-path out;libs`

**Salida esperada:**

```
=== Bookstore Modular (JPMS - con Módulo Automático) ===

Catálogo de libros (títulos en Title Case):
  [978-0-13-468599-1] The Java Module System por Nicolai Parlog - $49.99
  [978-0-596-00712-6] Head First Design Patterns por Eric Freeman - $39.95
  [978-0-13-685991-7] Effective Java por Joshua Bloch - $44.99

Títulos truncados (20 chars):
  The Java Module S...
  Head First Design...
  Effective Java

Búsqueda: Book[isbn=978-0-13-685991-7, title=Effective Java, author=Joshua Bloch, price=44.99]

Descuentos via ServiceLoader:
  Proveedor: Descuento Estacional (15%)
    The Java Module System -> descuento: $7.50 (precio final: $42.49)
    Head First Design Patterns -> descuento: $5.99 (precio final: $33.96)
    Effective Java -> descuento: $6.75 (precio final: $38.24)

=== Fin ===
```

**Verificación:** La librería `bookstore.utils.jar` (sin `module-info.java`) se integra exitosamente como módulo automático. El módulo automático exporta todos sus paquetes y puede ser requerido por módulos explícitos. Esta es la estrategia de migración **bottom-up** para dependencias de terceros.

---

### Paso 22: Empaquetar los módulos como JARs modulares

**Objetivo:** Crear JARs individuales para cada módulo y verificar que contienen el descriptor de módulo.

1. Crea un directorio para los JARs:

```bash
mkdir -p ~/cursos/java-se11-labs/bookstore-modular/mods
```

2. Empaqueta cada módulo:

```bash
jar --create --file mods/com.bookstore.model.jar -C out/com.bookstore.model .
jar --create --file mods/com.bookstore.api.jar -C out/com.bookstore.api .
jar --create --file mods/com.bookstore.repository.jar -C out/com.bookstore.repository .
jar --create --file mods/com.bookstore.service.jar -C out/com.bookstore.service .
jar --create --file mods/com.bookstore.app.jar \
    --main-class com.bookstore.app.Main -C out/com.bookstore.app .
```

3. Verifica que el JAR de la app tiene la clase principal configurada:

```bash
jar --describe-module --file mods/com.bookstore.app.jar
```

**Salida esperada:**

```
com.bookstore.app jar:file:///...mods/com.bookstore.app.jar
requires com.bookstore.api
requires com.bookstore.model
requires com.bookstore.repository
requires com.bookstore.service
requires bookstore.utils
requires java.base mandated
main-class com.bookstore.app.Main
```

4. Ejecuta desde los JARs modulares:

```bash
java --module-path mods:libs --module com.bookstore.app
```

**Salida esperada:** La misma salida del Paso 21.

**Verificación:** Al usar `--main-class` durante la creación del JAR, no es necesario especificar la clase principal en `--module`. El comando `java --module com.bookstore.app` es suficiente.

---

### Paso 23: Verificar el grafo completo de módulos resueltos

**Objetivo:** Usar `--show-module-resolution` para visualizar cómo la JVM resuelve las dependencias al arrancar.

1. Ejecuta con la bandera de diagnóstico:

```bash
java --module-path mods:libs --show-module-resolution --module com.bookstore.app 2>&1 | head -20
```

**Salida esperada (primeras líneas):**

```
root com.bookstore.app file:///...mods/com.bookstore.app.jar
com.bookstore.app requires com.bookstore.model file:///...mods/com.bookstore.model.jar
com.bookstore.app requires com.bookstore.api file:///...mods/com.bookstore.api.jar
com.bookstore.app requires com.bookstore.repository file:///...mods/com.bookstore.repository.jar
com.bookstore.app requires com.bookstore.service file:///...mods/com.bookstore.service.jar
com.bookstore.app requires bookstore.utils file:///...libs/bookstore.utils.jar automatic
com.bookstore.service requires com.bookstore.api ...
com.bookstore.service requires java.logging ...
...
```

**Verificación:** La salida muestra la cadena completa de resolución. Observa que `bookstore.utils` se marca como `automatic`, confirmando su naturaleza de módulo automático. No hay ciclos en el grafo.

---

### Paso 24: Crear un segundo proveedor de descuento para validar SPI extensible

**Objetivo:** Demostrar que el mecanismo `uses`/`provides` permite agregar nuevos proveedores sin modificar el código cliente.

1. Crea un nuevo directorio para un módulo de proveedor adicional:

```bash
mkdir -p bookstore-modular/src/com.bookstore.discount.bulk/com/bookstore/discount/bulk
```

2. Crea la implementación del proveedor de descuento por volumen:

```java
// bookstore-modular/src/com.bookstore.discount.bulk/com/bookstore/discount/bulk/BulkDiscountProvider.java
package com.bookstore.discount.bulk;

import com.bookstore.model.Book;
import com.bookstore.service.spi.DiscountProvider;

public class BulkDiscountProvider implements DiscountProvider {
    @Override
    public double calculateDiscount(Book book) {
        // Descuento del 10% para libros con precio > $40
        return book.getPrice() > 40.0 ? book.getPrice() * 0.10 : 0.0;
    }

    @Override
    public String providerName() {
        return "Descuento por Volumen (10% si precio > $40)";
    }
}
```

3. Crea el descriptor de módulo:

```java
// bookstore-modular/src/com.bookstore.discount.bulk/module-info.java
module com.bookstore.discount.bulk {
    requires com.bookstore.model;
    requires com.bookstore.service;

    provides com.bookstore.service.spi.DiscountProvider
        with com.bookstore.discount.bulk.BulkDiscountProvider;
}
```

4. Recompila todo el proyecto:

```bash
rm -rf out
javac -d out --module-source-path src --module-path libs $(find src -name "*.java")
```

5. Ejecuta la aplicación (sin modificar `Main.java` ni `BookService.java`):

```bash
java --module-path out:libs --module com.bookstore.app/com.bookstore.app.Main
```

**Salida esperada:**

```
=== Bookstore Modular (JPMS - con Módulo Automático) ===

Catálogo de libros (títulos en Title Case):
  [978-0-13-468599-1] The Java Module System por Nicolai Parlog - $49.99
  [978-0-596-00712-6] Head First Design Patterns por Eric Freeman - $39.95
  [978-0-13-685991-7] Effective Java por Joshua Bloch - $44.99

Títulos truncados (20 chars):
  The Java Module S...
  Head First Design...
  Effective Java

Búsqueda: Book[isbn=978-0-13-685991-7, title=Effective Java, author=Joshua Bloch, price=44.99]

Descuentos via ServiceLoader:
  Proveedor: Descuento Estacional (15%)
    The Java Module System -> descuento: $7.50 (precio final: $42.49)
    Head First Design Patterns -> descuento: $5.99 (precio final: $33.96)
    Effective Java -> descuento: $6.75 (precio final: $38.24)
  Proveedor: Descuento por Volumen (10% si precio > $40)
    The Java Module System -> descuento: $5.00 (precio final: $44.99)
    Head First Design Patterns -> descuento: $0.00 (precio final: $39.95)
    Effective Java -> descuento: $4.50 (precio final: $40.49)

=== Fin ===
```

**Verificación:** ¡Aparecen **dos proveedores** de descuento! El `BulkDiscountProvider` fue descubierto automáticamente por `ServiceLoader` gracias a la directiva `provides` en su `module-info.java`. **No se modificó ni una línea** en `BookService` ni en `Main`. Este es el poder del patrón SPI con JPMS.

---

### Paso 25: Resumen del grafo final de módulos

**Objetivo:** Documentar la arquitectura final del proyecto modularizado.

1. Crea el archivo de documentación del grafo final:

```bash
cat > ~/cursos/java-se11-labs/bookstore-modular/MODULE-GRAPH.md << 'EOF'
## Grafo de Módulos - Bookstore Modular

## Módulos del proyecto (6):
1. **com.bookstore.model** - Entidades de dominio
2. **com.bookstore.api** - Interfaces de contrato (resuelve ciclos)
3. **com.bookstore.repository** - Implementación de acceso a datos
4. **com.bookstore.service** - Lógica de negocio + SPI
5. **com.bookstore.discount.bulk** - Proveedor de descuento adicional
6. **com.bookstore.app** - Punto de entrada

## Módulo automático (1):
- **bookstore.utils** - Utilidades de terceros (sin module-info)

## Dependencias:
```
com.bookstore.model       → (ninguna, solo java.base)
com.bookstore.api         → com.bookstore.model (transitive)
com.bookstore.repository  → com.bookstore.api, java.logging
com.bookstore.service     → com.bookstore.api, com.bookstore.repository, java.logging
com.bookstore.discount.bulk → com.bookstore.model, com.bookstore.service
com.bookstore.app         → todos los anteriores + bookstore.utils
```

## Directivas utilizadas:
- **requires**: dependencia de compilación y ejecución
- **requires transitive**: dependencia propagada a consumidores
- **exports**: paquete visible para otros módulos
- **opens**: paquete accesible por reflexión
- **uses**: declara consumo de SPI
- **provides...with**: registra implementación de SPI
EOF
```

**Verificación:** El archivo `MODULE-GRAPH.md` documenta la arquitectura completa para referencia futura y para el Lab 09-00-01.

---

## Validación y Pruebas

### Prueba 1: Compilación limpia completa

```bash
cd ~/cursos/java-se11-labs/bookstore-modular
rm -rf out
javac -d out --module-source-path src --module-path libs $(find src -name "*.java")
echo "Exit code: $?"
```

**Criterio de éxito:** Exit code `0`, sin warnings ni errores.

### Prueba 2: Ejecución con salida completa

```bash
java --module-path out:libs --module com.bookstore.app/com.bookstore.app.Main 2>/dev/null
```

**Criterio de éxito:** Se muestran 3 libros, 1 búsqueda exitosa y 2 proveedores de descuento (Estacional y Volumen).

### Prueba 3: Encapsulación fuerte verificada

Intenta acceder al paquete interno desde línea de comandos:

```bash
cat > /tmp/HackTest.java << 'EOF'
import com.bookstore.service.impl.SeasonalDiscountProvider;
public class HackTest {
    public static void main(String[] args) {
        new SeasonalDiscountProvider();
    }
}
EOF
javac --module-path out:libs --add-modules com.bookstore.service /tmp/HackTest.java 2>&1
```

**Criterio de éxito:** Error de compilación indicando que el paquete `com.bookstore.service.impl` no es visible.

### Prueba 4: Resolución de módulos sin ciclos

```bash
java --module-path out:libs --show-module-resolution --dry-run --module com.bookstore.app 2>&1 | grep -i "cycle"
```

**Criterio de éxito:** Sin salida (no se detectan ciclos).

### Prueba 5: Caso adverso — módulo inexistente

```bash
java --module-path out:libs --module com.bookstore.inexistente/com.bookstore.app.Main 2>&1
```

**Criterio de éxito:** Error claro: `Error: Module com.bookstore.inexistente not found`. El sistema modular provee mensajes de error descriptivos, a diferencia del classpath donde se obtendría un `ClassNotFoundException` genérico.

### Prueba 6: Verificación de conteo de archivos

```bash
find ~/cursos/java-se11-labs/bookstore-modular/src -name "module-info.java" | wc -l
find ~/cursos/java-se11-labs/bookstore-modular/src -name "*.java" | wc -l
```

**Criterio de éxito:** 6 archivos `module-info.java` y 15 archivos `.java` en total (6 descriptores + 9 clases).

---

## Solución de Problemas

### Problema 1: Error "package exists in multiple modules"

**Síntoma:**

```
error: module com.bookstore.repository reads package com.bookstore.model from both
com.bookstore.model and com.bookstore.api
```

**Causa:** Dos módulos diferentes exportan el mismo paquete (`com.bookstore.model`). Esto ocurre si accidentalmente se dejó una copia de `Book.java` en el módulo `com.bookstore.api` dentro del paquete `com.bookstore.model` en lugar de `com.bookstore.api`.

**Solución:**

1. Verifica que cada paquete Java exista en **exactamente un módulo**:

```bash
find src -name "*.java" -path "*/model/*" | sort
```

2. Asegúrate de que `BookRepository.java` en el módulo API usa el paquete `com.bookstore.api`, NO `com.bookstore.model`:

```bash
head -1 src/com.bookstore.api/com/bookstore/api/BookRepository.java
## Debe mostrar: package com.bookstore.api;
```

3. Si hay archivos duplicados, elimínalos y recompila.

### Problema 2: ServiceLoader no encuentra proveedores en tiempo de ejecución

**Síntoma:** La aplicación compila correctamente pero `ServiceLoader` no encuentra ningún `DiscountProvider`, mostrando el mensaje "No se encontraron proveedores de descuento".

**Causa:** El módulo que declara `provides` no está presente en el module-path durante la ejecución, o la directiva `uses` falta en el módulo consumidor.

**Solución:**

1. Verifica que `com.bookstore.service` declara `uses`:

```bash
grep -n "uses" src/com.bookstore.service/module-info.java
## Debe mostrar: uses com.bookstore.service.spi.DiscountProvider;
```

2. Verifica que los módulos proveedores declaran `provides`:

```bash
grep -rn "provides" src/*/module-info.java
```

3. Confirma que **todos** los módulos compilados están en el module-path:

```bash
ls out/
## Debe listar: com.bookstore.app  com.bookstore.api  com.bookstore.discount.bulk
## com.bookstore.model  com.bookstore.repository  com.bookstore.service
```

4. Si falta algún módulo en `out/`, recompila con `$(find src -name "*.java")` para incluir todos los archivos fuente.

---

## Limpieza

1. Los directorios de compilación pueden eliminarse para liberar espacio:

```bash
rm -rf ~/cursos/java-se11-labs/bookstore-modular/out
rm -rf ~/cursos/java-se11-labs/bookstore-legacy/out
rm -rf ~/cursos/java-se11-labs/bookstore-utils/classes
```

2. **No elimines** los directorios `bookstore-modular/` ni `bookstore-modular/libs/` — serán referenciados en el Lab 09-00-01 para integrar el módulo `java.net.http`.

3. El directorio `bookstore-legacy/` puede conservarse como referencia de la versión pre-modular.

---

## Resumen

En este laboratorio has completado el ciclo completo de modularización de una aplicación Java:

- **Análisis de la aplicación legado:** Usaste `jdeps` y `--describe-module` para comprender las dependencias entre capas y hacia módulos del JDK (`java.base`, `java.logging`, `java.sql`).
- **Creación de módulos JPMS:** Dividiste la aplicación en 6 módulos con descriptores `module-info.java` que declaran `requires`, `exports`, `opens`, `uses` y `provides`.
- **Compilación y ejecución modular:** Compilaste con `javac --module-source-path` y ejecutaste con `java --module-path --module`, verificando la encapsulación fuerte.
- **Resolución de dependencias cíclicas:** Diagnosticaste un ciclo entre `service` y `repository`, y lo resolviste extrayendo interfaces a un módulo `com.bookstore.api` común.
- **Migración con módulos automáticos:** Integraste un JAR de terceros sin `module-info.java` como módulo automático, usando `jar --describe-module` para determinar su nombre derivado.
- **SPI extensible:** Demostraste que `ServiceLoader` con `uses`/`provides` permite agregar nuevos proveedores sin modificar código existente.

**Temas del temario cubiertos:** 7.1 (programación modular), 7.2 (ClassPath vs ModulePath), 7.3 (módulos en Java), 7.4 (ventajas del sistema modular), 7.5 (JDK modular), 7.6 (tipos de módulos: explícitos, automáticos, unnamed), 7.7 (`requires` y `exports`), 7.8 (`opens`), 7.9 (`uses` y `provides`), 7.10 (`module-info.java`), 7.11 (dependencias entre módulos), 7.12 (manejo de dependencias cíclicas).

**Recursos adicionales:**
- [Documentación oficial JPMS — Oracle Java SE 11](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/module/package-summary.html)
- [JEP 261: Module System](https://openjdk.org/jeps/261)
- [Guía de migración a módulos — Baeldung](https://www.baeldung.com/java-9-modularity)
- [The Java Module System — Nicolai Parlog (Manning, 2019)](https://www.manning.com/books/the-java-module-system)
