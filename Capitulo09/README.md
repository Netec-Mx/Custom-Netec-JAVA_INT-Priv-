# Práctica: Cliente HTTP asíncrono y comunicación WebSocket

## Metadatos

| Campo | Valor |
|---|---|
| **Duración** | 120 minutos |
| **Complejidad** | Alta |
| **Nivel Bloom** | Crear |
| **Tecnologías** | Java SE 11 (JDK 11.0.23+9 Temurin), java.net.http, CompletableFuture, SSLContext, WebSocket, JPMS, WireMock 3.6.0, Maven 3.9.8 |

## Descripción General

El participante construirá un cliente con `HttpClient`, `HttpRequest` y `HttpResponse` para consumir una API REST, enviando solicitudes síncronas y asíncronas con `CompletableFuture` y configurando comunicación segura mediante HTTPS. Posteriormente creará una conexión WebSocket con `WebSocket.Builder`, procesará mensajes y eventos con `WebSocket.Listener`, implementará el manejo de errores y la reconexión automática, e integrará la solución como un módulo que declare la dependencia con `java.net.http`.

Este laboratorio consolida conocimientos de los laboratorios anteriores: programación asíncrona (Lab 06-00-01), modularización JPMS (Lab 07-00-01) y seguridad con KeyStore PKCS12 (Lab 08-00-01).

## Objetivos de Aprendizaje

- [ ] Construir y configurar una instancia de `HttpClient` con versión HTTP/2, timeout de conexión y política de redirección, y enviar solicitudes CRUD síncronas procesando `HttpResponse` con `BodyHandlers`.
- [ ] Refactorizar solicitudes síncronas a asíncronas con `sendAsync()`, encadenando `thenApply()`, `thenAccept()` y `exceptionally()`, y ejecutar múltiples solicitudes en paralelo con `CompletableFuture.allOf()`.
- [ ] Configurar un `SSLContext` con el KeyStore PKCS12 del Lab 08-00-01 para establecer comunicación HTTPS con WireMock en el puerto 8443.
- [ ] Implementar `WebSocket.Listener` para recibir mensajes en tiempo real y construir un mecanismo de reconexión automática con retroceso exponencial usando `ScheduledExecutorService`.
- [ ] Integrar el cliente HTTP y WebSocket como módulo JPMS `com.httpclient.lab` declarando `requires java.net.http` en `module-info.java`.

## Prerrequisitos

### Conocimientos previos

| Requisito | Laboratorio de referencia |
|---|---|
| Programación asíncrona con `CompletableFuture` y `ForkJoinPool` | Lab 06-00-01 |
| Estructura de módulos JPMS y `module-info.java` | Lab 07-00-01 |
| Generación de KeyStore PKCS12 y configuración SSL | Lab 08-00-01 |
| Conceptos básicos de HTTP (métodos, códigos de estado, cabeceras) | Conocimiento general |

### Acceso y artefactos requeridos

| Recurso | Ubicación / Detalle |
|---|---|
| JDK 11.0.23+9 Eclipse Temurin | Instalado y en `PATH` — [https://adoptium.net/temurin/releases/?version=11](https://adoptium.net/temurin/releases/?version=11) |
| Apache Maven 3.9.8 | Instalado y en `PATH` — [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi) |
| IntelliJ IDEA Community 2024.1.4 | [https://www.jetbrains.com/idea/download/](https://www.jetbrains.com/idea/download/) |
| WireMock Standalone 3.6.0 | `~/labs/tools/wiremock-standalone-3.6.0.jar` — [https://repo1.maven.org/maven2/org/wiremock/wiremock-standalone/3.6.0/wiremock-standalone-3.6.0.jar](https://repo1.maven.org/maven2/org/wiremock/wiremock-standalone/3.6.0/wiremock-standalone-3.6.0.jar) |
| Archivo `keystore.p12` del Lab 08-00-01 | `~/cursos/java-se11-labs/src/main/resources/keystore.p12` (contraseña: `changeit`) |
| Directorio de trabajo global | `~/cursos/java-se11-labs` (Linux/macOS) o `C:\cursos\java-se11-labs` (Windows) |

## Entorno de Laboratorio

### Estructura del proyecto

Este laboratorio crea un subpaquete nuevo `com.httpclient.lab` dentro del proyecto Maven existente. La estructura final será:

```
~/cursos/java-se11-labs/
├── pom.xml
├── stubs/
│   ├── mappings/
│   │   ├── get-products.json
│   │   ├── post-orders.json
│   │   ├── put-product.json
│   │   └── delete-product.json
│   └── __files/
│       └── products-response.json
├── src/main/java/
│   ├── module-info.java
│   └── com/httpclient/lab/
│       ├── client/
│       │   ├── ProductHttpClient.java
│       │   ├── AsyncProductClient.java
│       │   └── SecureHttpClient.java
│       ├── websocket/
│       │   ├── NotificationListener.java
│       │   └── WebSocketConnectionManager.java
│       └── app/
│           └── HttpClientDemo.java
└── src/main/resources/
    └── keystore.p12
```

### Software requerido

| Software | Versión exacta | Propósito |
|---|---|---|
| JDK Eclipse Temurin | 11.0.23+9 | Compilación y ejecución |
| Apache Maven | 3.9.8 | Gestión de dependencias y compilación |
| WireMock Standalone | 3.6.0 | Servidor mock REST y WebSocket |
| maven-compiler-plugin | 3.13.0 | Compilación con `--release 11` |

## Instrucciones Paso a Paso

### Paso 1: Preparar la estructura de directorios y stubs de WireMock

**Objetivo:** Crear los directorios del nuevo módulo y los archivos de configuración de WireMock que simularán la API REST.

1. Abre una terminal y navega al directorio de trabajo:

```bash
cd ~/cursos/java-se11-labs
```

2. Crea la estructura de directorios para el código fuente:

```bash
mkdir -p src/main/java/com/httpclient/lab/{client,websocket,app}
```

3. Crea la estructura de stubs de WireMock:

```bash
mkdir -p stubs/mappings stubs/__files
```

4. Crea el archivo de respuesta JSON para productos en `stubs/__files/products-response.json`:

```json
[
  {"id": 1, "name": "Laptop Pro", "price": 1299.99, "category": "Electrónica"},
  {"id": 2, "name": "Teclado Mecánico", "price": 89.50, "category": "Periféricos"},
  {"id": 3, "name": "Monitor 27 pulgadas", "price": 449.00, "category": "Electrónica"},
  {"id": 4, "name": "Mouse Ergonómico", "price": 45.99, "category": "Periféricos"}
]
```

5. Crea el mapping `stubs/mappings/get-products.json`:

```json
{
  "request": {
    "method": "GET",
    "url": "/api/products"
  },
  "response": {
    "status": 200,
    "bodyFileName": "products-response.json",
    "headers": {
      "Content-Type": "application/json"
    }
  }
}
```

6. Crea el mapping `stubs/mappings/post-orders.json`:

```json
{
  "request": {
    "method": "POST",
    "url": "/api/orders"
  },
  "response": {
    "status": 201,
    "jsonBody": {
      "id": 100,
      "product": "Laptop Pro",
      "quantity": 2,
      "status": "CREATED"
    },
    "headers": {
      "Content-Type": "application/json"
    }
  }
}
```

7. Crea el mapping `stubs/mappings/put-product.json`:

```json
{
  "request": {
    "method": "PUT",
    "urlPathPattern": "/api/products/[0-9]+"
  },
  "response": {
    "status": 200,
    "jsonBody": {
      "id": 1,
      "name": "Laptop Pro Max",
      "price": 1499.99,
      "category": "Electrónica"
    },
    "headers": {
      "Content-Type": "application/json"
    }
  }
}
```

8. Crea el mapping `stubs/mappings/delete-product.json`:

```json
{
  "request": {
    "method": "DELETE",
    "urlPathPattern": "/api/products/[0-9]+"
  },
  "response": {
    "status": 204
  }
}
```

**Salida esperada:** Ocho archivos creados sin errores en las rutas indicadas.

**Verificación:**

```bash
find stubs -type f | sort
```

```
stubs/__files/products-response.json
stubs/mappings/delete-product.json
stubs/mappings/get-products.json
stubs/mappings/post-orders.json
stubs/mappings/put-product.json
```

---

### Paso 2: Iniciar WireMock y verificar los endpoints REST

**Objetivo:** Levantar WireMock 3.6.0 en modo standalone con los stubs configurados y verificar que los endpoints responden correctamente.

1. Abre una **nueva terminal** (mantén esta terminal dedicada a WireMock durante todo el laboratorio) e inicia WireMock:

```bash
java -jar ~/labs/tools/wiremock-standalone-3.6.0.jar \
  --port 8080 \
  --root-dir ~/cursos/java-se11-labs/stubs \
  --verbose
```

2. En la terminal principal, verifica el endpoint GET:

```bash
curl -s http://localhost:8080/api/products | head -5
```

**Salida esperada:**

```json
[{"id":1,"name":"Laptop Pro","price":1299.99,"category":"Electrónica"},
```

3. Verifica el endpoint POST:

```bash
curl -s -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"product":"Laptop Pro","quantity":2}'
```

**Salida esperada:**

```json
{"id":100,"product":"Laptop Pro","quantity":2,"status":"CREATED"}
```

4. Verifica el endpoint DELETE:

```bash
curl -s -o /dev/null -w "%{http_code}" -X DELETE http://localhost:8080/api/products/1
```

**Salida esperada:** `204`

**Verificación:** Los tres comandos curl retornan los códigos de estado esperados (200, 201, 204).

---

### Paso 3: Actualizar el pom.xml para el módulo HTTP Client

**Objetivo:** Configurar el `pom.xml` existente para compilar con `--release 11` y sin dependencias externas adicionales (la API `java.net.http` es parte del JDK).

1. Abre `~/cursos/java-se11-labs/pom.xml` y asegúrate de que el bloque `maven-compiler-plugin` incluya la configuración de release 11. Si ya existe del Lab anterior, verifica que sea exactamente así:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.curso</groupId>
    <artifactId>java-se11-labs</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <packaging>jar</packaging>

    <properties>
        <java.version>11</java.version>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.13.0</version>
                <configuration>
                    <release>11</release>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

> **Nota:** Si tu `pom.xml` ya contiene dependencias de laboratorios anteriores (PostgreSQL JDBC, Bouncy Castle, etc.), consérvelas. Solo verifica que el `maven-compiler-plugin` esté configurado como se muestra.

2. Compila para verificar la configuración:

```bash
cd ~/cursos/java-se11-labs
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS` sin errores.

**Verificación:** El comando `mvn compile` termina exitosamente.

---

### Paso 4: Implementar el cliente HTTP síncrono con operaciones CRUD

**Objetivo:** Crear la clase `ProductHttpClient` que configure `HttpClient` con HTTP/2, timeout y redirección, e implemente los cuatro métodos HTTP (GET, POST, PUT, DELETE) de forma síncrona.

1. Crea el archivo `src/main/java/com/httpclient/lab/client/ProductHttpClient.java`:

```java
package com.httpclient.lab.client;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ProductHttpClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient client;

    public ProductHttpClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    // --- GET /api/products ---
    public HttpResponse<String> getProducts()
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/products"))
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // --- POST /api/orders ---
    public HttpResponse<String> createOrder(String productName, int quantity)
            throws IOException, InterruptedException {

        String jsonBody = String.format(
                "{\"product\":\"%s\",\"quantity\":%d}",
                productName, quantity);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/orders"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // --- PUT /api/products/{id} ---
    public HttpResponse<String> updateProduct(int id, String name, double price)
            throws IOException, InterruptedException {

        String jsonBody = String.format(
                "{\"id\":%d,\"name\":\"%s\",\"price\":%.2f,\"category\":\"Electrónica\"}",
                id, name, price);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/products/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // --- DELETE /api/products/{id} ---
    public HttpResponse<String> deleteProduct(int id)
            throws IOException, InterruptedException {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/products/" + id))
                .DELETE()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // Exponer el HttpClient para reutilización en ejercicios posteriores
    public HttpClient getClient() {
        return this.client;
    }
}
```

2. Crea una clase de demostración temporal para probar las operaciones CRUD. Crea `src/main/java/com/httpclient/lab/app/HttpClientDemo.java`:

```java
package com.httpclient.lab.app;

import com.httpclient.lab.client.ProductHttpClient;
import java.net.http.HttpResponse;

public class HttpClientDemo {

    public static void main(String[] args) throws Exception {

        ProductHttpClient httpClient = new ProductHttpClient();

        System.out.println("=== Ejercicio 1 & 2: Operaciones CRUD síncronas ===\n");

        // GET
        HttpResponse<String> getResp = httpClient.getProducts();
        System.out.println("[GET] Código: " + getResp.statusCode());
        System.out.println("[GET] Cuerpo: " + getResp.body());
        System.out.println();

        // POST
        HttpResponse<String> postResp = httpClient.createOrder("Laptop Pro", 2);
        System.out.println("[POST] Código: " + postResp.statusCode());
        System.out.println("[POST] Cuerpo: " + postResp.body());
        System.out.println();

        // PUT
        HttpResponse<String> putResp = httpClient.updateProduct(1, "Laptop Pro Max", 1499.99);
        System.out.println("[PUT] Código: " + putResp.statusCode());
        System.out.println("[PUT] Cuerpo: " + putResp.body());
        System.out.println();

        // DELETE
        HttpResponse<String> delResp = httpClient.deleteProduct(1);
        System.out.println("[DELETE] Código: " + delResp.statusCode());
        System.out.println();

        // Verificación de códigos
        assert getResp.statusCode() == 200 : "GET debería retornar 200";
        assert postResp.statusCode() == 201 : "POST debería retornar 201";
        assert putResp.statusCode() == 200 : "PUT debería retornar 200";
        assert delResp.statusCode() == 204 : "DELETE debería retornar 204";

        System.out.println(">>> Todas las operaciones CRUD completadas exitosamente.");
    }
}
```

3. Compila y ejecuta:

```bash
mvn compile -q
java -ea -cp target/classes com.httpclient.lab.app.HttpClientDemo
```

**Salida esperada:**

```
=== Ejercicio 1 & 2: Operaciones CRUD síncronas ===

[GET] Código: 200
[GET] Cuerpo: [{"id":1,"name":"Laptop Pro","price":1299.99,...}]

[POST] Código: 201
[POST] Cuerpo: {"id":100,"product":"Laptop Pro","quantity":2,"status":"CREATED"}

[PUT] Código: 200
[PUT] Cuerpo: {"id":1,"name":"Laptop Pro Max","price":1499.99,"category":"Electrónica"}

[DELETE] Código: 204

>>> Todas las operaciones CRUD completadas exitosamente.
```

**Verificación:** Los cuatro códigos de estado coinciden con los esperados (200, 201, 200, 204) y no se lanzan `AssertionError`.

---

### Paso 5: Implementar solicitudes asíncronas con CompletableFuture

**Objetivo:** Refactorizar el cliente para usar `sendAsync()`, encadenar transformaciones con `thenApply()`, `thenAccept()` y `exceptionally()`, y ejecutar múltiples solicitudes en paralelo con `CompletableFuture.allOf()`, midiendo el tiempo total.

1. Crea el archivo `src/main/java/com/httpclient/lab/client/AsyncProductClient.java`:

```java
package com.httpclient.lab.client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

public class AsyncProductClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final HttpClient client;

    public AsyncProductClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }

    public CompletableFuture<String> getProductsAsync() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/products"))
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("[ASYNC GET] Código: " + response.statusCode()
                            + " | Hilo: " + Thread.currentThread().getName());
                    return response.body();
                })
                .exceptionally(ex -> {
                    System.err.println("[ASYNC GET] Error: " + ex.getMessage());
                    return "[]";
                });
    }

    public CompletableFuture<String> createOrderAsync(String product, int qty) {
        String jsonBody = String.format(
                "{\"product\":\"%s\",\"quantity\":%d}", product, qty);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/orders"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("[ASYNC POST] Código: " + response.statusCode()
                            + " | Hilo: " + Thread.currentThread().getName());
                    return response.body();
                })
                .exceptionally(ex -> {
                    System.err.println("[ASYNC POST] Error: " + ex.getMessage());
                    return "{}";
                });
    }

    public CompletableFuture<String> updateProductAsync(int id, String name, double price) {
        String jsonBody = String.format(
                "{\"id\":%d,\"name\":\"%s\",\"price\":%.2f}", id, name, price);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/products/" + id))
                .header("Content-Type", "application/json")
                .PUT(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    System.out.println("[ASYNC PUT] Código: " + response.statusCode()
                            + " | Hilo: " + Thread.currentThread().getName());
                    return response.body();
                })
                .exceptionally(ex -> {
                    System.err.println("[ASYNC PUT] Error: " + ex.getMessage());
                    return "{}";
                });
    }

    public HttpClient getClient() {
        return this.client;
    }
}
```

2. Actualiza `HttpClientDemo.java` agregando el bloque de pruebas asíncronas **después** del bloque CRUD síncrono existente. Reemplaza todo el contenido del archivo:

```java
package com.httpclient.lab.app;

import com.httpclient.lab.client.AsyncProductClient;
import com.httpclient.lab.client.ProductHttpClient;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class HttpClientDemo {

    public static void main(String[] args) throws Exception {

        // ========== Ejercicio 1 & 2: CRUD síncrono ==========
        ProductHttpClient syncClient = new ProductHttpClient();
        System.out.println("=== Ejercicio 1 & 2: Operaciones CRUD síncronas ===\n");

        long startSync = System.currentTimeMillis();

        HttpResponse<String> getResp = syncClient.getProducts();
        System.out.println("[GET] Código: " + getResp.statusCode());

        HttpResponse<String> postResp = syncClient.createOrder("Laptop Pro", 2);
        System.out.println("[POST] Código: " + postResp.statusCode());

        HttpResponse<String> putResp = syncClient.updateProduct(1, "Laptop Pro Max", 1499.99);
        System.out.println("[PUT] Código: " + putResp.statusCode());

        HttpResponse<String> delResp = syncClient.deleteProduct(1);
        System.out.println("[DELETE] Código: " + delResp.statusCode());

        long syncTime = System.currentTimeMillis() - startSync;
        System.out.println("\n>>> Tiempo síncrono total: " + syncTime + " ms\n");

        // ========== Ejercicio 3: Solicitudes asíncronas ==========
        AsyncProductClient asyncClient = new AsyncProductClient();
        System.out.println("=== Ejercicio 3: Solicitudes asíncronas en paralelo ===\n");

        long startAsync = System.currentTimeMillis();

        CompletableFuture<String> f1 = asyncClient.getProductsAsync();
        CompletableFuture<String> f2 = asyncClient.createOrderAsync("Monitor 27\"", 1);
        CompletableFuture<String> f3 = asyncClient.updateProductAsync(2, "Teclado Pro", 129.99);

        // Esperar a que las tres solicitudes completen
        CompletableFuture.allOf(f1, f2, f3).join();

        long asyncTime = System.currentTimeMillis() - startAsync;

        System.out.println("\n[Resultado GET]  " + f1.get().substring(0, Math.min(80, f1.get().length())) + "...");
        System.out.println("[Resultado POST] " + f2.get());
        System.out.println("[Resultado PUT]  " + f3.get());

        System.out.println("\n>>> Tiempo asíncrono total: " + asyncTime + " ms");
        System.out.println(">>> Diferencia (síncrono - asíncrono): " + (syncTime - asyncTime) + " ms");
        System.out.println("\n>>> Ejercicio 3 completado exitosamente.");
    }
}
```

3. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.httpclient.lab.app.HttpClientDemo
```

**Salida esperada (fragmento):**

```
=== Ejercicio 3: Solicitudes asíncronas en paralelo ===

[ASYNC GET] Código: 200 | Hilo: HttpClient-1-Worker-1
[ASYNC POST] Código: 201 | Hilo: HttpClient-1-Worker-2
[ASYNC PUT] Código: 200 | Hilo: HttpClient-1-Worker-3

>>> Tiempo asíncrono total: XX ms
>>> Diferencia (síncrono - asíncrono): XX ms
```

**Verificación:** Observe que los hilos de ejecución asíncrona son diferentes (`Worker-N`) y que el tiempo asíncrono es menor o comparable al síncrono. Las tres solicitudes se ejecutan en paralelo gracias a `CompletableFuture.allOf()`.

---

### Paso 6: Configurar WireMock con HTTPS y preparar el SSLContext

**Objetivo:** Levantar una segunda instancia de WireMock con HTTPS en el puerto 8443 usando el KeyStore PKCS12 del Lab 08-00-01, y crear un cliente HTTP seguro.

1. Primero, verifica que el archivo `keystore.p12` existe:

```bash
ls -la ~/cursos/java-se11-labs/src/main/resources/keystore.p12
```

> **Nota:** Si no dispones del `keystore.p12` del Lab 08-00-01, genera uno autofirmado con:
> ```bash
> keytool -genkeypair -alias wiremock -keyalg RSA -keysize 2048 \
>   -storetype PKCS12 -keystore ~/cursos/java-se11-labs/src/main/resources/keystore.p12 \
>   -storepass changeit -validity 365 \
>   -dname "CN=localhost, OU=Lab, O=Curso, L=Ciudad, ST=Estado, C=MX"
> ```

2. Abre una **tercera terminal** e inicia WireMock con HTTPS en el puerto 8443:

```bash
java -jar ~/labs/tools/wiremock-standalone-3.6.0.jar \
  --https-port 8443 \
  --https-keystore ~/cursos/java-se11-labs/src/main/resources/keystore.p12 \
  --keystore-password changeit \
  --keystore-type PKCS12 \
  --root-dir ~/cursos/java-se11-labs/stubs \
  --verbose
```

3. Verifica el endpoint HTTPS con curl (aceptando certificados autofirmados):

```bash
curl -sk https://localhost:8443/api/products | head -3
```

**Salida esperada:** La misma lista JSON de productos que el endpoint HTTP.

4. Crea el archivo `src/main/java/com/httpclient/lab/client/SecureHttpClient.java`:

```java
package com.httpclient.lab.client;

import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.time.Duration;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

public class SecureHttpClient {

    private static final String HTTPS_BASE_URL = "https://localhost:8443/api";
    private final HttpClient client;

    public SecureHttpClient(String keystorePath, String keystorePassword)
            throws Exception {

        // 1. Cargar el KeyStore PKCS12
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream is = Files.newInputStream(Path.of(keystorePath))) {
            keyStore.load(is, keystorePassword.toCharArray());
        }

        // 2. Inicializar TrustManagerFactory con el KeyStore
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        // 3. Construir SSLContext
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, tmf.getTrustManagers(), null);

        // 4. Construir HttpClient con SSLContext
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .sslContext(sslContext)
                .build();

        System.out.println("[SECURE] HttpClient configurado con SSLContext PKCS12");
    }

    public HttpResponse<String> getProductsSecure()
            throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(HTTPS_BASE_URL + "/products"))
                .header("Accept", "application/json")
                .GET()
                .build();

        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpClient getClient() {
        return this.client;
    }
}
```

5. Agrega la prueba HTTPS en `HttpClientDemo.java`. Inserta el siguiente bloque **al final del método `main()`**, antes de la llave de cierre:

```java
        // ========== Ejercicio 4: HTTPS seguro ==========
        System.out.println("\n=== Ejercicio 4: Comunicación HTTPS segura ===\n");

        try {
            String keystorePath = "src/main/resources/keystore.p12";
            com.httpclient.lab.client.SecureHttpClient secureClient =
                    new com.httpclient.lab.client.SecureHttpClient(keystorePath, "changeit");

            HttpResponse<String> secureResp = secureClient.getProductsSecure();
            System.out.println("[HTTPS GET] Código: " + secureResp.statusCode());
            System.out.println("[HTTPS GET] Cuerpo: " +
                    secureResp.body().substring(0, Math.min(100, secureResp.body().length())) + "...");

            assert secureResp.statusCode() == 200 : "HTTPS GET debería retornar 200";
            System.out.println("\n>>> Ejercicio 4 completado: comunicación HTTPS exitosa.");
        } catch (Exception e) {
            System.err.println("[HTTPS] Error: " + e.getMessage());
            e.printStackTrace();
        }
```

6. Compila y ejecuta:

```bash
mvn compile -q
java -ea -cp target/classes com.httpclient.lab.app.HttpClientDemo
```

**Salida esperada (fragmento del ejercicio 4):**

```
=== Ejercicio 4: Comunicación HTTPS segura ===

[SECURE] HttpClient configurado con SSLContext PKCS12
[HTTPS GET] Código: 200
[HTTPS GET] Cuerpo: [{"id":1,"name":"Laptop Pro","price":1299.99,"category":"Electrónica"},{"id":2...

>>> Ejercicio 4 completado: comunicación HTTPS exitosa.
```

**Verificación:** El código de estado es 200 y el cuerpo contiene la lista JSON de productos, demostrando que la comunicación TLS funciona con el KeyStore PKCS12.

---

### Paso 7: Implementar el WebSocket.Listener para recibir notificaciones

**Objetivo:** Crear la clase `NotificationListener` que implemente `WebSocket.Listener` con los métodos `onOpen()`, `onText()`, `onError()` y `onClose()`, y conectarla al endpoint WebSocket de WireMock.

1. Crea el archivo `src/main/java/com/httpclient/lab/websocket/NotificationListener.java`:

```java
package com.httpclient.lab.websocket;

import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class NotificationListener implements WebSocket.Listener {

    private final CompletableFuture<Void> closeFuture = new CompletableFuture<>();

    @Override
    public void onOpen(WebSocket webSocket) {
        System.out.println("[WS] Conexión abierta. Solicitando primer mensaje...");
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(WebSocket webSocket,
                                      CharSequence data,
                                      boolean last) {
        System.out.println("[WS] Mensaje recibido: " + data);
        webSocket.request(1);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket webSocket,
                                       int statusCode,
                                       String reason) {
        System.out.println("[WS] Conexión cerrada. Código: " + statusCode
                + ", Razón: " + reason);
        closeFuture.complete(null);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void onError(WebSocket webSocket, Throwable error) {
        System.err.println("[WS] Error: " + error.getMessage());
        closeFuture.completeExceptionally(error);
    }

    public CompletableFuture<Void> getCloseFuture() {
        return closeFuture;
    }
}
```

2. Agrega el bloque de prueba WebSocket en `HttpClientDemo.java` al final del método `main()`:

```java
        // ========== Ejercicio 5: WebSocket básico ==========
        System.out.println("\n=== Ejercicio 5: Conexión WebSocket ===\n");

        try {
            com.httpclient.lab.websocket.NotificationListener listener =
                    new com.httpclient.lab.websocket.NotificationListener();

            java.net.http.WebSocket ws = java.net.http.HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(
                            URI.create("ws://localhost:8080/ws/notifications"),
                            listener)
                    .join();

            System.out.println("[WS] WebSocket conectado. Esperando mensajes (10 seg)...");

            // Enviar un mensaje de prueba al servidor
            ws.sendText("ping desde cliente", true);

            // Esperar 10 segundos para recibir mensajes
            Thread.sleep(10_000);

            // Cerrar la conexión de forma ordenada
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "Fin de la demostración")
                    .thenRun(() -> System.out.println("[WS] Cierre enviado."))
                    .join();

            System.out.println("\n>>> Ejercicio 5 completado: WebSocket funcional.");
        } catch (Exception e) {
            System.err.println("[WS] Nota: WireMock standalone puede no soportar WebSocket.");
            System.err.println("[WS] Error: " + e.getMessage());
            System.out.println("[WS] Continuando con el resto del laboratorio...");
        }
```

> **Importante:** WireMock 3.6.0 standalone tiene soporte limitado para WebSocket. Si la conexión falla, el bloque `catch` captura el error y el laboratorio continúa. El instructor puede proporcionar un servidor WebSocket alternativo con un simple `ServerSocket` o usar la clase del Paso 8 para simular la reconexión.

3. Compila y ejecuta:

```bash
mvn compile -q
java -ea -cp target/classes com.httpclient.lab.app.HttpClientDemo
```

**Salida esperada (si WebSocket está disponible):**

```
=== Ejercicio 5: Conexión WebSocket ===

[WS] Conexión abierta. Solicitando primer mensaje...
[WS] WebSocket conectado. Esperando mensajes (10 seg)...
[WS] Mensaje recibido: ...
[WS] Cierre enviado.

>>> Ejercicio 5 completado: WebSocket funcional.
```

**Salida esperada (si WebSocket no está disponible en WireMock):**

```
[WS] Nota: WireMock standalone puede no soportar WebSocket.
[WS] Error: ...
[WS] Continuando con el resto del laboratorio...
```

**Verificación:** La clase `NotificationListener` compila sin errores e implementa correctamente los cuatro métodos de `WebSocket.Listener`. Si la conexión se establece, los mensajes se imprimen por consola.

---

### Paso 8: Implementar reconexión automática con retroceso exponencial

**Objetivo:** Crear `WebSocketConnectionManager` con un mecanismo de reconexión automática que use `ScheduledExecutorService` y retroceso exponencial (máximo 60 segundos, máximo 5 intentos).

1. Crea el archivo `src/main/java/com/httpclient/lab/websocket/WebSocketConnectionManager.java`:

```java
package com.httpclient.lab.websocket;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class WebSocketConnectionManager {

    private static final int MAX_RETRIES = 5;
    private static final long MAX_DELAY_SECONDS = 60;

    private final HttpClient httpClient;
    private final URI wsUri;
    private final ScheduledExecutorService scheduler;
    private final AtomicInteger attemptCounter = new AtomicInteger(0);

    private volatile WebSocket currentWebSocket;

    public WebSocketConnectionManager(HttpClient httpClient, String wsUrl) {
        this.httpClient = httpClient;
        this.wsUri = URI.create(wsUrl);
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ws-reconnect-scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    public CompletableFuture<WebSocket> connect() {
        int attempt = attemptCounter.incrementAndGet();
        System.out.println("[RECONNECT] Intento " + attempt + " de " + MAX_RETRIES);

        NotificationListener listener = new NotificationListener() {
            @Override
            public void onError(WebSocket webSocket, Throwable error) {
                super.onError(webSocket, error);
                System.err.println("[RECONNECT] Conexión perdida: " + error.getMessage());
                scheduleReconnect(attemptCounter.get());
            }

            @Override
            public CompletableFuture<Void> onClose(WebSocket webSocket,
                                                     int statusCode,
                                                     String reason) {
                // No reconectar en cierre normal (1000)
                if (statusCode != WebSocket.NORMAL_CLOSURE) {
                    System.out.println("[RECONNECT] Cierre inesperado: " + statusCode);
                    scheduleReconnect(attemptCounter.get());
                }
                return CompletableFuture.completedFuture(null);
            }
        };

        return httpClient.newWebSocketBuilder()
                .buildAsync(wsUri, listener)
                .thenApply(ws -> {
                    this.currentWebSocket = ws;
                    attemptCounter.set(0); // Reset en conexión exitosa
                    System.out.println("[RECONNECT] Conexión establecida exitosamente.");
                    return ws;
                })
                .exceptionally(ex -> {
                    System.err.println("[RECONNECT] Fallo en conexión: " + ex.getMessage());
                    scheduleReconnect(attempt);
                    return null;
                });
    }

    private void scheduleReconnect(int attempt) {
        if (attempt >= MAX_RETRIES) {
            System.err.println("[RECONNECT] Máximo de reintentos alcanzado (" + MAX_RETRIES + "). Abortando.");
            return;
        }

        // Retroceso exponencial: 2^attempt segundos, máximo 60
        long delay = Math.min((long) Math.pow(2, attempt), MAX_DELAY_SECONDS);
        System.out.println("[RECONNECT] Reintentando en " + delay + " segundos...");

        scheduler.schedule(() -> connect(), delay, TimeUnit.SECONDS);
    }

    public void disconnect() {
        if (currentWebSocket != null) {
            currentWebSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Desconexión manual")
                    .thenRun(() -> System.out.println("[RECONNECT] Desconectado."));
        }
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
```

2. Agrega la prueba de reconexión en `HttpClientDemo.java` al final del método `main()`:

```java
        // ========== Ejercicio 6: Reconexión automática ==========
        System.out.println("\n=== Ejercicio 6: Reconexión con retroceso exponencial ===\n");

        com.httpclient.lab.websocket.WebSocketConnectionManager manager =
                new com.httpclient.lab.websocket.WebSocketConnectionManager(
                        java.net.http.HttpClient.newHttpClient(),
                        "ws://localhost:8080/ws/notifications");

        // Intentar conectar (si WireMock no soporta WS, se activará la reconexión)
        manager.connect();

        // Esperar 15 segundos para observar intentos de reconexión
        System.out.println("[RECONNECT] Observando comportamiento durante 15 segundos...");
        Thread.sleep(15_000);

        manager.disconnect();
        System.out.println("\n>>> Ejercicio 6 completado: mecanismo de reconexión implementado.");
```

3. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.httpclient.lab.app.HttpClientDemo
```

**Salida esperada (cuando WebSocket no está disponible — demuestra la reconexión):**

```
=== Ejercicio 6: Reconexión con retroceso exponencial ===

[RECONNECT] Intento 1 de 5
[RECONNECT] Fallo en conexión: ...
[RECONNECT] Reintentando en 2 segundos...
[RECONNECT] Intento 2 de 5
[RECONNECT] Fallo en conexión: ...
[RECONNECT] Reintentando en 4 segundos...
[RECONNECT] Intento 3 de 5
...
[RECONNECT] Observando comportamiento durante 15 segundos...

>>> Ejercicio 6 completado: mecanismo de reconexión implementado.
```

**Verificación:** El mecanismo de reconexión muestra intentos con delays crecientes (2s, 4s, 8s...) y se detiene al alcanzar el máximo de 5 intentos o al llamar `disconnect()`.

---

### Paso 9: Integrar como módulo JPMS con module-info.java

**Objetivo:** Crear el descriptor de módulo JPMS `module-info.java` declarando `requires java.net.http` y exportando el paquete del cliente, compilar desde ModulePath y verificar las dependencias.

1. Crea el archivo `src/main/java/module-info.java`:

```java
module com.httpclient.lab {
    requires java.net.http;
    requires java.logging;

    exports com.httpclient.lab.client;
    exports com.httpclient.lab.websocket;
    exports com.httpclient.lab.app;
}
```

> **Nota:** Si el `pom.xml` contiene dependencias de laboratorios anteriores (PostgreSQL JDBC, Bouncy Castle) que no son módulos JPMS compatibles, puede ser necesario compilar temporalmente sin `module-info.java` o agregar directivas `requires` adicionales. Para este ejercicio, si hay conflictos con dependencias previas, renombra temporalmente el archivo:
> ```bash
> # Solo si hay conflictos de compilación con dependencias previas:
> # mv src/main/java/module-info.java src/main/java/module-info.java.bak
> ```

2. Compila el proyecto:

```bash
mvn compile -q
```

3. Si la compilación es exitosa con `module-info.java`, verifica el módulo:

```bash
jar --create --file target/httpclient-lab.jar \
    -C target/classes .

jar --describe-module --file target/httpclient-lab.jar
```

**Salida esperada:**

```
com.httpclient.lab jar:file:///...target/httpclient-lab.jar
requires java.logging
requires java.net.http
requires java mandated
exports com.httpclient.lab.app
exports com.httpclient.lab.client
exports com.httpclient.lab.websocket
```

4. Ejecuta desde el ModulePath:

```bash
java --module-path target/httpclient-lab.jar \
     --module com.httpclient.lab/com.httpclient.lab.app.HttpClientDemo
```

**Salida esperada:** La misma salida completa de todos los ejercicios anteriores, pero ejecutada desde el sistema modular JPMS.

5. Si hay conflictos con dependencias de labs anteriores, usa la alternativa de ejecución por classpath y documenta el `module-info.java` como artefacto de referencia:

```bash
## Alternativa: ejecución por classpath (siempre funciona)
java -cp target/classes com.httpclient.lab.app.HttpClientDemo
```

**Verificación:** El comando `jar --describe-module` muestra las tres directivas `requires` y los tres `exports` declarados en `module-info.java`. El módulo `java.net.http` aparece como dependencia resuelta.

---

### Paso 10: Ejecutar la demostración integrada final

**Objetivo:** Ejecutar el flujo completo que integra todos los ejercicios: CRUD síncrono, solicitudes asíncronas en paralelo, HTTPS seguro, WebSocket y reconexión, todo desde el módulo JPMS.

1. Asegúrate de que ambas instancias de WireMock están activas (HTTP en 8080, HTTPS en 8443).

2. Revisa que `HttpClientDemo.java` contiene todos los bloques de los ejercicios 1 al 6. El archivo completo final debe tener esta estructura:

```java
package com.httpclient.lab.app;

import com.httpclient.lab.client.AsyncProductClient;
import com.httpclient.lab.client.ProductHttpClient;
import com.httpclient.lab.client.SecureHttpClient;
import com.httpclient.lab.websocket.WebSocketConnectionManager;
import com.httpclient.lab.websocket.NotificationListener;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.http.WebSocket;
import java.util.concurrent.CompletableFuture;

public class HttpClientDemo {

    public static void main(String[] args) throws Exception {

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║  Lab 09-00-01: Cliente HTTP y WebSocket         ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        // --- Ejercicio 1 & 2: CRUD síncrono ---
        ProductHttpClient syncClient = new ProductHttpClient();
        System.out.println("=== Ejercicios 1 & 2: CRUD síncrono ===\n");

        long startSync = System.currentTimeMillis();
        HttpResponse<String> getResp = syncClient.getProducts();
        System.out.println("[GET]    Código: " + getResp.statusCode());
        HttpResponse<String> postResp = syncClient.createOrder("Laptop Pro", 2);
        System.out.println("[POST]   Código: " + postResp.statusCode());
        HttpResponse<String> putResp = syncClient.updateProduct(1, "Laptop Pro Max", 1499.99);
        System.out.println("[PUT]    Código: " + putResp.statusCode());
        HttpResponse<String> delResp = syncClient.deleteProduct(1);
        System.out.println("[DELETE] Código: " + delResp.statusCode());
        long syncTime = System.currentTimeMillis() - startSync;
        System.out.println(">>> Tiempo síncrono: " + syncTime + " ms\n");

        // --- Ejercicio 3: Asíncrono ---
        AsyncProductClient asyncClient = new AsyncProductClient();
        System.out.println("=== Ejercicio 3: Solicitudes asíncronas ===\n");

        long startAsync = System.currentTimeMillis();
        CompletableFuture<String> f1 = asyncClient.getProductsAsync();
        CompletableFuture<String> f2 = asyncClient.createOrderAsync("Monitor", 1);
        CompletableFuture<String> f3 = asyncClient.updateProductAsync(2, "Teclado Pro", 129.99);
        CompletableFuture.allOf(f1, f2, f3).join();
        long asyncTime = System.currentTimeMillis() - startAsync;
        System.out.println(">>> Tiempo asíncrono: " + asyncTime + " ms");
        System.out.println(">>> Ganancia: " + (syncTime - asyncTime) + " ms\n");

        // --- Ejercicio 4: HTTPS ---
        System.out.println("=== Ejercicio 4: HTTPS seguro ===\n");
        try {
            SecureHttpClient secureClient =
                    new SecureHttpClient("src/main/resources/keystore.p12", "changeit");
            HttpResponse<String> secResp = secureClient.getProductsSecure();
            System.out.println("[HTTPS] Código: " + secResp.statusCode());
            System.out.println(">>> HTTPS exitoso.\n");
        } catch (Exception e) {
            System.err.println("[HTTPS] " + e.getMessage() + "\n");
        }

        // --- Ejercicio 5: WebSocket ---
        System.out.println("=== Ejercicio 5: WebSocket ===\n");
        try {
            NotificationListener listener = new NotificationListener();
            WebSocket ws = HttpClient.newHttpClient()
                    .newWebSocketBuilder()
                    .buildAsync(URI.create("ws://localhost:8080/ws/notifications"), listener)
                    .join();
            ws.sendText("ping", true);
            Thread.sleep(5_000);
            ws.sendClose(WebSocket.NORMAL_CLOSURE, "fin").join();
            System.out.println(">>> WebSocket exitoso.\n");
        } catch (Exception e) {
            System.out.println("[WS] No disponible: " + e.getMessage() + "\n");
        }

        // --- Ejercicio 6: Reconexión ---
        System.out.println("=== Ejercicio 6: Reconexión automática ===\n");
        WebSocketConnectionManager mgr = new WebSocketConnectionManager(
                HttpClient.newHttpClient(), "ws://localhost:9999/invalid");
        mgr.connect();
        Thread.sleep(12_000);
        mgr.disconnect();
        System.out.println(">>> Reconexión demostrada.\n");

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║  Laboratorio completado exitosamente            ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
    }
}
```

3. Compila y ejecuta la demostración completa:

```bash
mvn compile -q
java -ea -cp target/classes com.httpclient.lab.app.HttpClientDemo
```

**Salida esperada (resumen):**

```
╔══════════════════════════════════════════════════╗
║  Lab 09-00-01: Cliente HTTP y WebSocket         ║
╚══════════════════════════════════════════════════╝

=== Ejercicios 1 & 2: CRUD síncrono ===

[GET]    Código: 200
[POST]   Código: 201
[PUT]    Código: 200
[DELETE] Código: 204
>>> Tiempo síncrono: XX ms

=== Ejercicio 3: Solicitudes asíncronas ===

[ASYNC GET] Código: 200 | Hilo: HttpClient-1-Worker-1
[ASYNC POST] Código: 201 | Hilo: HttpClient-1-Worker-2
[ASYNC PUT] Código: 200 | Hilo: HttpClient-1-Worker-3
>>> Tiempo asíncrono: XX ms
>>> Ganancia: XX ms

=== Ejercicio 4: HTTPS seguro ===

[SECURE] HttpClient configurado con SSLContext PKCS12
[HTTPS] Código: 200
>>> HTTPS exitoso.

=== Ejercicio 5: WebSocket ===
...

=== Ejercicio 6: Reconexión automática ===

[RECONNECT] Intento 1 de 5
[RECONNECT] Fallo en conexión: ...
[RECONNECT] Reintentando en 2 segundos...
[RECONNECT] Intento 2 de 5
...
>>> Reconexión demostrada.

╔══════════════════════════════════════════════════╗
║  Laboratorio completado exitosamente            ║
╚══════════════════════════════════════════════════╝
```

**Verificación:** Todos los ejercicios se ejecutan secuencialmente. Los códigos de estado HTTP son correctos, las solicitudes asíncronas se ejecutan en hilos separados, HTTPS funciona con el KeyStore PKCS12, y el mecanismo de reconexión muestra retroceso exponencial.

## Validación y Pruebas

### Criterios de validación medibles

| # | Criterio | Evidencia requerida |
|---|---|---|
| 1 | `HttpClient` configurado con HTTP/2, timeout 10s y redirección NORMAL | Código en `ProductHttpClient.java` con los tres métodos del builder |
| 2 | GET retorna código 200, POST retorna 201, PUT retorna 200, DELETE retorna 204 | Salida de consola con los cuatro códigos |
| 3 | Solicitudes asíncronas ejecutadas en hilos `Worker-N` distintos | Salida de consola mostrando nombres de hilos diferentes |
| 4 | `CompletableFuture.allOf()` espera las tres solicitudes | Tiempo asíncrono menor o comparable al síncrono |
| 5 | HTTPS funcional con SSLContext cargado desde `keystore.p12` | Código 200 en la solicitud HTTPS al puerto 8443 |
| 6 | `NotificationListener` implementa los 4 métodos de `WebSocket.Listener` | Código fuente con `onOpen`, `onText`, `onError`, `onClose` |
| 7 | Retroceso exponencial con delays 2s, 4s, 8s... hasta máximo 60s | Salida de consola con timestamps de reconexión |
| 8 | `module-info.java` declara `requires java.net.http` | Salida de `jar --describe-module` |

### Caso adverso: solicitud a endpoint inexistente

Ejecuta la siguiente prueba para verificar que `exceptionally()` maneja correctamente errores de red:

```bash
## Detener WireMock temporalmente (Ctrl+C en la terminal de WireMock HTTP)
## Luego ejecutar solo el cliente asíncrono:
java -cp target/classes com.httpclient.lab.app.HttpClientDemo
```

**Resultado esperado:** El bloque `exceptionally()` captura el `java.net.ConnectException` y retorna el valor por defecto (`"[]"` o `"{}"`), sin que la aplicación lance una excepción no manejada.

### Caso adverso: KeyStore con contraseña incorrecta

Modifica temporalmente la contraseña en la invocación del `SecureHttpClient`:

```java
// Cambiar "changeit" por "wrongpassword"
SecureHttpClient secureClient = new SecureHttpClient("src/main/resources/keystore.p12", "wrongpassword");
```

**Resultado esperado:** Se lanza `java.io.IOException: keystore password was incorrect` capturada por el bloque `try-catch`, y la aplicación continúa ejecutando los demás ejercicios.

## Solución de Problemas

### Problema 1: `java.net.ConnectException: Connection refused` al ejecutar solicitudes HTTP

**Síntomas:** Todas las solicitudes HTTP fallan con `Connection refused` al puerto 8080 o 8443.

**Causa:** WireMock no está en ejecución o se inició en un puerto diferente al esperado.

**Solución:**

1. Verifica que WireMock está activo:

```bash
curl -s http://localhost:8080/__admin/mappings | head -3
```

2. Si no responde, reinicia WireMock:

```bash
java -jar ~/labs/tools/wiremock-standalone-3.6.0.jar \
  --port 8080 \
  --root-dir ~/cursos/java-se11-labs/stubs \
  --verbose
```

3. Para HTTPS, verifica el puerto 8443:

```bash
curl -sk https://localhost:8443/__admin/mappings | head -3
```

---

### Problema 2: `module-info.java` causa errores de compilación con dependencias de labs anteriores

**Síntomas:** `mvn compile` falla con errores como `package X is not visible` o `module not found: Y` al agregar `module-info.java`.

**Causa:** Dependencias de labs anteriores (PostgreSQL JDBC, Bouncy Castle) no son módulos JPMS automáticos compatibles, o sus paquetes no están declarados en el descriptor de módulo.

**Solución:**

1. Renombra temporalmente `module-info.java` para compilar por classpath:

```bash
mv src/main/java/module-info.java src/main/java/module-info.java.bak
mvn compile -q
```

2. Para mantener la modularización, agrega las dependencias como módulos automáticos:

```java
module com.httpclient.lab {
    requires java.net.http;
    requires java.logging;
    requires java.sql;           // Si usa JDBC de labs anteriores
    requires org.postgresql.jdbc; // Nombre automático del JAR
    exports com.httpclient.lab.client;
    exports com.httpclient.lab.websocket;
    exports com.httpclient.lab.app;
}
```

3. Verifica los nombres de módulos automáticos de tus dependencias:

```bash
jar --describe-module --file ~/.m2/repository/org/postgresql/postgresql/42.7.3/postgresql-42.7.3.jar
```

## Limpieza

1. Detén ambas instancias de WireMock (Ctrl+C en las terminales dedicadas).

2. Elimina el JAR temporal de verificación modular:

```bash
rm -f target/httpclient-lab.jar
```

3. Si renombraste `module-info.java`, restáuralo:

```bash
mv src/main/java/module-info.java.bak src/main/java/module-info.java 2>/dev/null
```

4. El código fuente del laboratorio permanece en el proyecto para referencia futura. No elimines los archivos `.java` ni los stubs de WireMock.

## Resumen

En este laboratorio se construyó un cliente HTTP completo utilizando la API `java.net.http` de Java SE 11, cubriendo los siguientes temas del temario:

| Tema | Ejercicio |
|---|---|
| 9.1 Nueva API java.net.http | Paso 3–4: Configuración del proyecto |
| 9.2 Arquitectura de HTTP Client | Paso 4: Builder pattern con `HttpClient.newBuilder()` |
| 9.3 Clase HttpClient | Paso 4: Versión HTTP/2, timeout, redirección |
| 9.4 Clase HttpRequest | Paso 4: GET, POST, PUT, DELETE con `BodyPublishers` |
| 9.5 Clase HttpResponse | Paso 4: `BodyHandlers.ofString()`, códigos de estado |
| 9.6 Envío síncrono | Paso 4: `client.send()` |
| 9.7 Envío asíncrono | Paso 5: `sendAsync()`, `CompletableFuture.allOf()` |
| 9.8 Consumo de APIs REST | Pasos 4–5: Operaciones CRUD completas |
| 9.9 Comunicación HTTP segura | Paso 6: SSLContext con KeyStore PKCS12 |
| 9.10 WebSockets | Paso 7: Conexión y recepción de mensajes |
| 9.11 WebSocket.Builder | Paso 7: `newWebSocketBuilder().buildAsync()` |
| 9.12 WebSocket.Listener | Pasos 7–8: `onOpen`, `onText`, `onError`, `onClose` + reconexión |

### Conceptos clave consolidados

- **HttpClient** es inmutable y reutilizable; se configura una vez con el *builder* y se comparte en toda la aplicación.
- **sendAsync()** retorna `CompletableFuture`, permitiendo encadenar transformaciones sin bloquear el hilo llamante.
- **SSLContext** se inyecta en el builder de `HttpClient` para habilitar HTTPS con certificados personalizados.
- **WebSocket.Listener** usa el patrón de contrapresión (`request(1)`) para controlar el flujo de mensajes.
- El retroceso exponencial es una estrategia estándar para reconexión resiliente en sistemas distribuidos.

### Recursos adicionales

- [JEP 321: HTTP Client (Standard)](https://openjdk.org/jeps/321) — Especificación oficial
- [Java SE 11 API: java.net.http](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/package-summary.html) — Documentación de referencia
- [WireMock 3.6.0 Documentation](https://wiremock.org/docs/) — Configuración de stubs y HTTPS
- [Baeldung: Java 11 HttpClient](https://www.baeldung.com/java-9-http-client) — Tutorial práctico
