# Práctica: Implementación de contratos con interfaces funcionales

## Metadatos

| Campo | Valor |
|---|---|
| **Duración** | 80 minutos |
| **Complejidad** | Media |
| **Nivel Bloom** | Aplicar (Apply) |
| **Módulo** | 1 — Interfaces y Funcionales |
| **Tecnologías** | Java SE 11 (JDK Temurin 11.0.23+9), Apache Maven 3.9.8, IntelliJ IDEA Community 2024.1.4 |

## Descripción General

En este laboratorio crearás desde cero el proyecto Maven **java-se11-labs** que será la base de todo el curso. Dentro del paquete `com.curso.modulo1`, diseñarás un dominio de negocio de gestión de pedidos (*Order Management*) compuesto por interfaces con métodos abstractos, `default`, `static` y `private`, tres interfaces funcionales personalizadas anotadas con `@FunctionalInterface`, y múltiples implementaciones concretas. Verificarás que el código cliente (`OrderClient`) puede intercambiar implementaciones sin modificación, demostrando el principio de sustitución de Liskov y el polimorfismo basado en interfaces.

## Objetivos de Aprendizaje

- [ ] Distinguir la diferencia semántica y de uso entre métodos abstractos, `default`, `static` y `private` dentro de una interfaz en Java SE 11.
- [ ] Aplicar la anotación `@FunctionalInterface` para garantizar en tiempo de compilación que una interfaz cumple el contrato de interfaz funcional (exactamente un método abstracto).
- [ ] Demostrar el principio de sustitución de Liskov intercambiando distintas implementaciones concretas de las interfaces sin alterar el código del cliente que las consume.
- [ ] Crear y configurar un proyecto Maven con JDK 11 que sirva como base reutilizable para los laboratorios siguientes del curso.

## Prerrequisitos

### Conocimientos previos

| Conocimiento | Nivel |
|---|---|
| Declaración de clases, métodos y tipos primitivos en Java | Básico |
| Conceptos de POO: herencia, polimorfismo e interfaces | Básico |
| Uso básico de terminal / línea de comandos | Básico |

### Acceso y software

| Componente | Versión exacta | Fuente oficial |
|---|---|---|
| JDK Eclipse Temurin | 11.0.23+9 | https://adoptium.net/temurin/releases/?version=11 |
| Apache Maven | 3.9.8 | https://maven.apache.org/download.cgi |
| IntelliJ IDEA Community Edition | 2024.1.4 | https://www.jetbrains.com/idea/download/ |
| Git | 2.45.2 | https://git-scm.com/downloads |

### Verificaciones previas

Ejecuta los siguientes comandos y confirma que las versiones coinciden:

```bash
java -version
```

```
openjdk version "11.0.23" 2024-04-16
```

```bash
mvn -version
```

```
Apache Maven 3.9.8
```

## Entorno de Laboratorio

### Directorio de trabajo

| Sistema operativo | Ruta |
|---|---|
| Linux / macOS | `~/cursos/java-se11-labs` |
| Windows | `C:\cursos\java-se11-labs` |

### Estructura final del proyecto

```
java-se11-labs/
├── pom.xml
└── src/
    └── main/
        └── java/
            └── com/
                └── curso/
                    └── modulo1/
                        ├── Processable.java
                        ├── Auditable.java
                        ├── Discountable.java
                        ├── OrderValidator.java
                        ├── OrderTransformer.java
                        ├── OrderNotifier.java
                        ├── Order.java
                        ├── StandardOrderProcessor.java
                        ├── ExpressOrderProcessor.java
                        ├── StandardDiscount.java
                        ├── VipDiscount.java
                        └── OrderClient.java
```

## Instrucciones Paso a Paso

### Paso 1: Crear el directorio del proyecto y el archivo POM

**Objetivo:** Establecer la estructura Maven base que se reutilizará en todos los módulos del curso.

1. Abre una terminal y crea el directorio raíz:

```bash
mkdir -p ~/cursos/java-se11-labs
cd ~/cursos/java-se11-labs
```

> En Windows usa: `mkdir C:\cursos\java-se11-labs && cd C:\cursos\java-se11-labs`

2. Crea el archivo `pom.xml` con el siguiente contenido:

```xml
<?xml version="1.0" encoding="UTF-8"?>
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

3. Crea la estructura de directorios fuente:

```bash
mkdir -p src/main/java/com/curso/modulo1
```

4. Valida que Maven reconoce el proyecto:

```bash
mvn validate
```

**Salida esperada:**

```
[INFO] BUILD SUCCESS
```

**Verificación:** El comando `mvn validate` finaliza sin errores y muestra `BUILD SUCCESS`.

---

### Paso 2: Abrir el proyecto en IntelliJ IDEA

**Objetivo:** Configurar el IDE para trabajar con el proyecto Maven y JDK 11.

1. Abre IntelliJ IDEA Community Edition 2024.1.4.
2. Selecciona **File → Open** y navega hasta `~/cursos/java-se11-labs`.
3. Cuando IntelliJ pregunte, selecciona **Open as Project** y confirma **Trust Project**.
4. Ve a **File → Project Structure → Project** y verifica que el **Project SDK** apunta a Temurin 11.0.23. Si no aparece, haz clic en **Add SDK → JDK** y selecciona la ruta de instalación de Temurin 11.
5. En la misma ventana, confirma que **Project language level** está en **11 — Local variable syntax for lambda parameters**.
6. Espera a que IntelliJ termine de indexar y sincronizar Maven (indicador en la barra inferior).

**Verificación:** En la ventana **Project**, la estructura `src/main/java/com/curso/modulo1` aparece con el icono azul de directorio fuente (sources root).

---

### Paso 3: Crear la clase de dominio Order

**Objetivo:** Definir la entidad central del dominio que será procesada por todas las interfaces.

1. En el paquete `com.curso.modulo1`, crea la clase `Order.java`:

```java
package com.curso.modulo1;

public class Order {

    private final String id;
    private final String customerName;
    private final double amount;
    private final String type; // "STANDARD" o "EXPRESS"

    public Order(String id, String customerName, double amount, String type) {
        this.id = id;
        this.customerName = customerName;
        this.amount = amount;
        this.type = type;
    }

    public String getId() { return id; }
    public String getCustomerName() { return customerName; }
    public double getAmount() { return amount; }
    public String getType() { return type; }

    @Override
    public String toString() {
        return String.format("Order{id='%s', customer='%s', amount=%.2f, type='%s'}",
                id, customerName, amount, type);
    }
}
```

**Verificación:** La clase compila sin errores (sin subrayados rojos en IntelliJ).

---

### Paso 4: Crear la interfaz Processable (contrato básico)

**Objetivo:** Definir un contrato con un método abstracto que establezca el comportamiento principal del procesamiento de pedidos.

1. Crea la interfaz `Processable.java` en `com.curso.modulo1`:

```java
package com.curso.modulo1;

/**
 * Contrato básico para procesamiento de pedidos.
 * Define el método abstracto que toda implementación debe cumplir.
 */
public interface Processable {

    /**
     * Procesa un pedido y retorna un mensaje de confirmación.
     * @param order el pedido a procesar
     * @return mensaje descriptivo del resultado del procesamiento
     */
    String process(Order order);
}
```

**Verificación:** La interfaz contiene exactamente un método abstracto implícitamente `public abstract`. IntelliJ no muestra errores.

---

### Paso 5: Crear la interfaz avanzada Auditable

**Objetivo:** Diseñar una interfaz que combine métodos `default`, `static` y `private` para demostrar las capacidades avanzadas de interfaces en Java 9+.

1. Crea la interfaz `Auditable.java`:

```java
package com.curso.modulo1;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Interfaz avanzada que proporciona capacidades de auditoría.
 * Demuestra métodos default, static y private en interfaces (Java 9+).
 */
public interface Auditable {

    /**
     * Método default: registra el timestamp de inicio del procesamiento.
     * Las clases implementadoras heredan este comportamiento sin obligación de sobreescribirlo.
     */
    default String logStart(Order order) {
        return formatEntry("INICIO", order);
    }

    /**
     * Método default: registra el timestamp de fin del procesamiento.
     */
    default String logEnd(Order order) {
        return formatEntry("FIN", order);
    }

    /**
     * Método static de utilidad: formatea un mensaje de log genérico.
     * Se invoca mediante Auditable.formatLogMessage(...).
     */
    static String formatLogMessage(String action, String orderId) {
        return String.format("[LOG] %s | Pedido: %s | %s",
                action, orderId, currentTimestamp());
    }

    /**
     * Método private: genera el timestamp actual.
     * Solo accesible desde otros métodos de esta interfaz.
     */
    private static String currentTimestamp() {
        return LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Método private de instancia: construye una entrada de log formateada.
     * Reutilizado por logStart y logEnd para evitar duplicación.
     */
    private String formatEntry(String phase, Order order) {
        return String.format("[AUDIT] %s | %s | Monto: %.2f | %s",
                phase, order.getId(), order.getAmount(), currentTimestamp());
    }
}
```

**Verificación:** La interfaz compila correctamente. Observa que `Auditable` **no** tiene métodos abstractos: todos tienen cuerpo. Esto es válido en Java 11.

---

### Paso 6: Crear la interfaz avanzada Discountable

**Objetivo:** Crear una interfaz con un método `default` que delega a un método `private` para calcular la tasa de descuento.

1. Crea la interfaz `Discountable.java`:

```java
package com.curso.modulo1;

/**
 * Interfaz avanzada para aplicar descuentos a pedidos.
 * El método default apply() delega al método private calculateRate().
 */
public interface Discountable {

    /**
     * Método abstracto: cada implementación define su tasa de descuento base.
     * @return porcentaje de descuento como valor entre 0.0 y 1.0
     */
    double getBaseRate();

    /**
     * Método default: aplica el descuento al monto del pedido.
     * Delega el cálculo de la tasa final al método private calculateRate().
     */
    default double apply(Order order) {
        double rate = calculateRate(order.getAmount());
        double discount = order.getAmount() * rate;
        System.out.printf("  Descuento aplicado a %s: %.2f (tasa: %.0f%%)%n",
                order.getId(), discount, rate * 100);
        return order.getAmount() - discount;
    }

    /**
     * Método private: calcula la tasa final considerando un bono
     * adicional del 5% para montos superiores a 500.
     */
    private double calculateRate(double amount) {
        double bonus = amount > 500.0 ? 0.05 : 0.0;
        return getBaseRate() + bonus;
    }
}
```

**Verificación:** La interfaz tiene exactamente un método abstracto (`getBaseRate()`), un `default` y un `private`. Compila sin errores.

---

### Paso 7: Implementar StandardOrderProcessor

**Objetivo:** Crear la primera implementación concreta que cumple los contratos `Processable` y `Auditable`.

1. Crea la clase `StandardOrderProcessor.java`:

```java
package com.curso.modulo1;

/**
 * Procesador de pedidos estándar.
 * Implementa Processable (contrato básico) y Auditable (capacidad de auditoría).
 */
public class StandardOrderProcessor implements Processable, Auditable {

    @Override
    public String process(Order order) {
        System.out.println(logStart(order));
        String result = String.format("Pedido %s procesado en modo ESTÁNDAR. Monto: %.2f",
                order.getId(), order.getAmount());
        System.out.println(logEnd(order));
        return result;
    }
}
```

**Verificación:** La clase implementa `process()` de `Processable` y hereda `logStart()`/`logEnd()` de `Auditable` sin necesidad de sobreescribirlos.

---

### Paso 8: Implementar ExpressOrderProcessor

**Objetivo:** Crear la segunda implementación que demuestre comportamiento diferente bajo el mismo contrato.

1. Crea la clase `ExpressOrderProcessor.java`:

```java
package com.curso.modulo1;

/**
 * Procesador de pedidos express con recargo del 15%.
 * Demuestra que el mismo contrato puede tener comportamientos distintos.
 */
public class ExpressOrderProcessor implements Processable, Auditable {

    private static final double EXPRESS_SURCHARGE = 0.15;

    @Override
    public String process(Order order) {
        System.out.println(logStart(order));
        double total = order.getAmount() * (1 + EXPRESS_SURCHARGE);
        String result = String.format(
                "Pedido %s procesado en modo EXPRESS. Monto original: %.2f | Total con recargo: %.2f",
                order.getId(), order.getAmount(), total);
        System.out.println(logEnd(order));
        return result;
    }
}
```

**Verificación:** La clase compila sin errores y proporciona una implementación diferente de `process()`.

---

### Paso 9: Implementar StandardDiscount y VipDiscount

**Objetivo:** Crear dos implementaciones de `Discountable` para verificar que el método `default` delega correctamente al `private`.

1. Crea la clase `StandardDiscount.java`:

```java
package com.curso.modulo1;

/**
 * Descuento estándar del 5%.
 */
public class StandardDiscount implements Discountable {

    @Override
    public double getBaseRate() {
        return 0.05;
    }
}
```

2. Crea la clase `VipDiscount.java`:

```java
package com.curso.modulo1;

/**
 * Descuento VIP del 15%.
 */
public class VipDiscount implements Discountable {

    @Override
    public double getBaseRate() {
        return 0.15;
    }
}
```

**Verificación:** Ambas clases compilan. Cada una solo implementa `getBaseRate()`; el método `apply()` se hereda del `default` en `Discountable`.

---

### Paso 10: Crear la interfaz funcional OrderValidator

**Objetivo:** Definir la primera interfaz funcional personalizada con la anotación `@FunctionalInterface`.

1. Crea la interfaz `OrderValidator.java`:

```java
package com.curso.modulo1;

/**
 * Interfaz funcional que valida un pedido.
 * Contrato: exactamente un método abstracto.
 *
 * @param <T> tipo del objeto a validar
 */
@FunctionalInterface
public interface OrderValidator<T> {

    /**
     * Evalúa si el objeto cumple una condición de validación.
     * @param target objeto a validar
     * @return true si la validación es exitosa
     */
    boolean validate(T target);
}
```

**Verificación:** La anotación `@FunctionalInterface` no genera error de compilación, lo que confirma que la interfaz tiene exactamente un método abstracto.

---

### Paso 11: Crear la interfaz funcional OrderTransformer

**Objetivo:** Definir una interfaz funcional genérica con dos parámetros de tipo para transformaciones.

1. Crea la interfaz `OrderTransformer.java`:

```java
package com.curso.modulo1;

/**
 * Interfaz funcional que transforma un objeto de tipo T en tipo R.
 *
 * @param <T> tipo de entrada
 * @param <R> tipo de salida
 */
@FunctionalInterface
public interface OrderTransformer<T, R> {

    /**
     * Transforma el objeto de entrada en un resultado.
     * @param input objeto a transformar
     * @return resultado de la transformación
     */
    R transform(T input);
}
```

**Verificación:** La interfaz compila con `@FunctionalInterface` sin errores.

---

### Paso 12: Crear la interfaz funcional OrderNotifier

**Objetivo:** Definir una interfaz funcional sin valor de retorno para notificaciones.

1. Crea la interfaz `OrderNotifier.java`:

```java
package com.curso.modulo1;

/**
 * Interfaz funcional que notifica sobre un pedido sin retornar valor.
 */
@FunctionalInterface
public interface OrderNotifier {

    /**
     * Envía una notificación sobre el pedido indicado.
     * @param order pedido sobre el cual notificar
     */
    void notify(Order order);
}
```

**Verificación:** Compila correctamente. Tiene exactamente un método abstracto con retorno `void`.

---

### Paso 13: Verificar contrato funcional — prueba negativa

**Objetivo:** Comprobar que `@FunctionalInterface` rechaza interfaces con más de un método abstracto.

1. Crea temporalmente un archivo `BrokenFunctional.java` en el mismo paquete:

```java
package com.curso.modulo1;

@FunctionalInterface
public interface BrokenFunctional {
    void actionA();
    void actionB(); // Segundo método abstracto: debe causar error
}
```

2. Intenta compilar el proyecto:

```bash
mvn compile
```

**Salida esperada (fragmento del error):**

```
[ERROR] BrokenFunctional.java: Unexpected @FunctionalInterface annotation
  BrokenFunctional is not a functional interface
    multiple non-overriding abstract methods found in interface BrokenFunctional
```

3. **Elimina** el archivo `BrokenFunctional.java` después de verificar el error. Este archivo no debe permanecer en el proyecto.

**Verificación:** El compilador rechaza la interfaz, confirmando que `@FunctionalInterface` actúa como guarda en tiempo de compilación.

---

### Paso 14: Crear la clase OrderClient (código cliente)

**Objetivo:** Construir la clase principal que demuestra el polimorfismo e intercambiabilidad de implementaciones sin modificar el código cliente.

1. Crea la clase `OrderClient.java`:

```java
package com.curso.modulo1;

/**
 * Clase cliente que consume las interfaces del dominio.
 * Demuestra el principio de sustitución de Liskov: las implementaciones
 * se intercambian sin modificar este código.
 */
public class OrderClient {

    // ─── Sección 1: Polimorfismo con Processable ───

    /**
     * Procesa un pedido usando cualquier implementación de Processable.
     * El cliente no conoce ni depende del tipo concreto.
     */
    public static String executeProcess(Processable processor, Order order) {
        return processor.process(order);
    }

    // ─── Sección 2: Polimorfismo con Discountable ───

    /**
     * Aplica un descuento usando cualquier implementación de Discountable.
     */
    public static double executeDiscount(Discountable discounter, Order order) {
        return discounter.apply(order);
    }

    // ─── Sección 3: Interfaces funcionales ───

    /**
     * Valida un pedido usando cualquier implementación de OrderValidator.
     */
    public static <T> boolean executeValidation(OrderValidator<T> validator, T target) {
        return validator.validate(target);
    }

    /**
     * Transforma un pedido usando cualquier implementación de OrderTransformer.
     */
    public static <T, R> R executeTransformation(OrderTransformer<T, R> transformer, T input) {
        return transformer.transform(input);
    }

    /**
     * Envía notificación usando cualquier implementación de OrderNotifier.
     */
    public static void executeNotification(OrderNotifier notifier, Order order) {
        notifier.notify(order);
    }

    // ─── Método main: punto de entrada de demostración ───

    public static void main(String[] args) {

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  Lab 01-00-01: Contratos con Interfaces");
        System.out.println("═══════════════════════════════════════════════");

        // Datos de prueba
        Order order1 = new Order("ORD-001", "Ana García", 350.00, "STANDARD");
        Order order2 = new Order("ORD-002", "Carlos López", 750.00, "EXPRESS");

        // ── 1. Processable: intercambio de implementaciones ──
        System.out.println("\n--- Processable: StandardOrderProcessor ---");
        Processable standardProc = new StandardOrderProcessor();
        System.out.println(executeProcess(standardProc, order1));

        System.out.println("\n--- Processable: ExpressOrderProcessor ---");
        Processable expressProc = new ExpressOrderProcessor();
        System.out.println(executeProcess(expressProc, order2));

        // ── 2. Auditable: método static ──
        System.out.println("\n--- Auditable: método static ---");
        System.out.println(Auditable.formatLogMessage("CONSULTA", order1.getId()));

        // ── 3. Discountable: intercambio de implementaciones ──
        System.out.println("\n--- Discountable: StandardDiscount ---");
        Discountable stdDiscount = new StandardDiscount();
        double finalStd = executeDiscount(stdDiscount, order1);
        System.out.printf("  Monto final (estándar): %.2f%n", finalStd);

        System.out.println("\n--- Discountable: VipDiscount ---");
        Discountable vipDiscount = new VipDiscount();
        double finalVip = executeDiscount(vipDiscount, order2);
        System.out.printf("  Monto final (VIP): %.2f%n", finalVip);

        // ── 4. Interfaces funcionales con clases anónimas ──
        System.out.println("\n--- OrderValidator (clase anónima) ---");
        OrderValidator<Order> amountValidator = new OrderValidator<Order>() {
            @Override
            public boolean validate(Order o) {
                return o.getAmount() > 0;
            }
        };
        System.out.printf("  ORD-001 válido: %b%n",
                executeValidation(amountValidator, order1));

        System.out.println("\n--- OrderTransformer (clase anónima) ---");
        OrderTransformer<Order, String> summaryTransformer = new OrderTransformer<Order, String>() {
            @Override
            public String transform(Order o) {
                return String.format("Resumen: %s -> %s (%.2f)",
                        o.getId(), o.getCustomerName(), o.getAmount());
            }
        };
        System.out.println("  " + executeTransformation(summaryTransformer, order1));

        System.out.println("\n--- OrderNotifier (clase anónima) ---");
        OrderNotifier emailNotifier = new OrderNotifier() {
            @Override
            public void notify(Order o) {
                System.out.printf("  [EMAIL] Notificación enviada a %s por pedido %s%n",
                        o.getCustomerName(), o.getId());
            }
        };
        executeNotification(emailNotifier, order2);

        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("  Laboratorio completado exitosamente.");
        System.out.println("═══════════════════════════════════════════════");
    }
}
```

**Verificación:** La clase compila sin errores en IntelliJ.

---

### Paso 15: Compilar el proyecto completo con Maven

**Objetivo:** Validar que todos los artefactos compilan correctamente bajo `--release 11`.

1. Desde la terminal, en el directorio raíz del proyecto:

```bash
mvn clean compile
```

**Salida esperada:**

```
[INFO] --- maven-compiler-plugin:3.13.0:compile (default-compile) @ java-se11-labs ---
[INFO] Compiling 11 source files with javac [debug release 11]
[INFO] BUILD SUCCESS
```

**Verificación:** El mensaje indica 11 archivos fuente compilados (Order, 3 interfaces de dominio, 3 interfaces funcionales, 4 clases de implementación + OrderClient) y `BUILD SUCCESS`.

---

### Paso 16: Ejecutar OrderClient y verificar la salida

**Objetivo:** Ejecutar la clase principal y comprobar que todas las interfaces y sus implementaciones funcionan correctamente.

1. Ejecuta la clase desde Maven:

```bash
mvn -q exec:java -Dexec.mainClass="com.curso.modulo1.OrderClient"
```

> Si no tienes el plugin `exec-maven-plugin`, ejecuta desde IntelliJ: clic derecho en `OrderClient.java` → **Run 'OrderClient.main()'**.

2. Alternativamente, ejecuta directamente con `java`:

```bash
java -cp target/classes com.curso.modulo1.OrderClient
```

**Salida esperada (los timestamps variarán):**

```
═══════════════════════════════════════════════
  Lab 01-00-01: Contratos con Interfaces
═══════════════════════════════════════════════

--- Processable: StandardOrderProcessor ---
[AUDIT] INICIO | ORD-001 | Monto: 350.00 | 2025-01-15 10:30:00
[AUDIT] FIN | ORD-001 | Monto: 350.00 | 2025-01-15 10:30:00
Pedido ORD-001 procesado en modo ESTÁNDAR. Monto: 350.00

--- Processable: ExpressOrderProcessor ---
[AUDIT] INICIO | ORD-002 | Monto: 750.00 | 2025-01-15 10:30:00
[AUDIT] FIN | ORD-002 | Monto: 750.00 | 2025-01-15 10:30:00
Pedido ORD-002 procesado en modo EXPRESS. Monto original: 750.00 | Total con recargo: 862.50

--- Auditable: método static ---
[LOG] CONSULTA | Pedido: ORD-001 | 2025-01-15 10:30:00

--- Discountable: StandardDiscount ---
  Descuento aplicado a ORD-001: 17.50 (tasa: 5%)
  Monto final (estándar): 332.50

--- Discountable: VipDiscount ---
  Descuento aplicado a ORD-002: 150.00 (tasa: 20%)
  Monto final (VIP): 600.00

--- OrderValidator (clase anónima) ---
  ORD-001 válido: true

--- OrderTransformer (clase anónima) ---
  Resumen: ORD-001 -> Ana García (350.00)

--- OrderNotifier (clase anónima) ---
  [EMAIL] Notificación enviada a Carlos López por pedido ORD-002

═══════════════════════════════════════════════
  Laboratorio completado exitosamente.
═══════════════════════════════════════════════
```

**Verificación:** Confirma los siguientes puntos en la salida:
- `StandardOrderProcessor` y `ExpressOrderProcessor` producen resultados diferentes para el mismo método `process()`.
- Los mensajes `[AUDIT]` provienen de los métodos `default` de `Auditable`.
- El mensaje `[LOG]` proviene del método `static` de `Auditable`.
- El descuento VIP para ORD-002 (monto 750 > 500) aplica tasa 20% (15% base + 5% bono del método `private`).
- Las tres interfaces funcionales se instancian con clases anónimas y producen resultados correctos.

---

### Paso 17: Demostrar intercambiabilidad (principio de sustitución)

**Objetivo:** Verificar explícitamente que se pueden intercambiar implementaciones sin cambiar el código de `OrderClient`.

1. En `OrderClient.main()`, localiza la línea:

```java
Processable standardProc = new StandardOrderProcessor();
```

2. Cámbiala temporalmente por:

```java
Processable standardProc = new ExpressOrderProcessor();
```

3. Ejecuta de nuevo:

```bash
java -cp target/classes com.curso.modulo1.OrderClient
```

4. Observa que `ORD-001` ahora se procesa en modo EXPRESS sin que el método `executeProcess()` haya cambiado.

5. **Revierte el cambio** para dejar `StandardOrderProcessor` como implementación original.

**Verificación:** El método `executeProcess()` no requirió modificación alguna para trabajar con una implementación diferente. Esto demuestra el polimorfismo basado en interfaces y el principio de sustitución de Liskov.

---

### Paso 18: Verificar cobertura de tipos de métodos en interfaces

**Objetivo:** Confirmar que el laboratorio ejercita los cuatro tipos de métodos en interfaces que exige el temario.

1. Revisa el siguiente inventario en tu código:

| Tipo de método | Interfaz | Método | Verificado |
|---|---|---|---|
| Abstracto | `Processable` | `process(Order)` | ✓ |
| Abstracto | `Discountable` | `getBaseRate()` | ✓ |
| `default` | `Auditable` | `logStart(Order)`, `logEnd(Order)` | ✓ |
| `default` | `Discountable` | `apply(Order)` | ✓ |
| `static` | `Auditable` | `formatLogMessage(String, String)` | ✓ |
| `private static` | `Auditable` | `currentTimestamp()` | ✓ |
| `private` (instancia) | `Auditable` | `formatEntry(String, Order)` | ✓ |
| `private` (instancia) | `Discountable` | `calculateRate(double)` | ✓ |

**Verificación:** Los cuatro tipos (abstracto, `default`, `static`, `private`) están presentes y fueron ejecutados en la salida del Paso 16.

## Validación y Pruebas

### Prueba 1: Compilación estricta bajo release 11

```bash
mvn clean compile
```

**Criterio:** `BUILD SUCCESS` con 11 archivos compilados. Cero warnings de compilación.

### Prueba 2: Ejecución completa sin excepciones

```bash
java -cp target/classes com.curso.modulo1.OrderClient
```

**Criterio:** La salida contiene las 7 secciones delimitadas por `---` y finaliza con el mensaje `Laboratorio completado exitosamente.` sin ningún `Exception` o `Error` en la traza.

### Prueba 3: Validación del descuento con método private

**Criterio:** Para `ORD-002` (monto 750.00) con `VipDiscount` (base 15%):
- El método `private calculateRate()` suma 5% de bono (750 > 500), resultando en tasa 20%.
- Descuento = 750 × 0.20 = 150.00.
- Monto final = 750 - 150 = **600.00**.

Verifica que la línea de salida dice exactamente: `Monto final (VIP): 600.00`.

### Prueba 4: Validación de @FunctionalInterface (caso adverso)

**Criterio:** El Paso 13 demostró que agregar un segundo método abstracto a una interfaz anotada con `@FunctionalInterface` produce un error de compilación. Esto constituye la prueba adversa: intentar violar el contrato es rechazado por el compilador, no por una verificación en tiempo de ejecución. No es posible "engañar" a la anotación con métodos `default` adicionales (estos no cuentan como abstractos).

### Prueba 5: Verificación de estructura Maven

```bash
find src/main/java/com/curso/modulo1 -name "*.java" | wc -l
```

**Criterio:** El resultado debe ser **11** archivos `.java`.

## Solución de Problemas

### Problema 1: Error "release version 11 not supported"

**Síntoma:** Al ejecutar `mvn compile`, aparece:

```
[ERROR] error: release version 11 not supported
```

**Causa:** Maven está usando un JDK inferior a 11 (por ejemplo, JDK 8) como JDK de compilación, a pesar de que el `pom.xml` especifica `<release>11</release>`.

**Solución:**

1. Verifica qué JDK usa Maven:

```bash
mvn -version
```

2. Busca la línea `Java version` en la salida. Si no muestra `11.0.23`, configura `JAVA_HOME` correctamente:

```bash
## Linux/macOS (ajusta la ruta a tu instalación de Temurin 11)
export JAVA_HOME=/usr/lib/jvm/temurin-11-jdk
export PATH="$JAVA_HOME/bin:$PATH"
```

```cmd
:: Windows (ajusta la ruta)
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-11.0.23+9
set PATH=%JAVA_HOME%\bin;%PATH%
```

3. Ejecuta `mvn -version` de nuevo y confirma `Java version: 11.0.23`.

---

### Problema 2: Error "private methods in interfaces not supported" al compilar

**Síntoma:** Al compilar `Auditable.java`, aparece:

```
[ERROR] private interface methods are not supported in -source 8
```

**Causa:** El `pom.xml` tiene `<maven.compiler.source>8</maven.compiler.source>` o falta la directiva `<release>11</release>` en el plugin `maven-compiler-plugin`. Los métodos `private` en interfaces requieren al menos Java 9 (JEP 213).

**Solución:**

1. Abre `pom.xml` y verifica que las propiedades y el plugin estén configurados exactamente así:

```xml
<properties>
    <java.version>11</java.version>
    <maven.compiler.source>11</maven.compiler.source>
    <maven.compiler.target>11</maven.compiler.target>
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
```

2. Ejecuta `mvn clean compile` para recompilar desde cero.

## Limpieza

Este laboratorio establece el proyecto base que se reutilizará en los módulos siguientes. **No elimines** ningún archivo creado. Los únicos elementos que deben haberse eliminado son:

1. El archivo temporal `BrokenFunctional.java` (eliminado en el Paso 13).
2. El cambio temporal en `OrderClient.java` (revertido en el Paso 17).

Verifica que el estado final del proyecto es limpio:

```bash
mvn clean compile
```

El directorio `target/` se regenera con cada `mvn compile`. La estructura de `src/` debe coincidir con la tabla del apartado **Entorno de Laboratorio**.

## Resumen

### Logros alcanzados

En este laboratorio has:

- **Creado el proyecto Maven base** (`java-se11-labs`) con configuración estricta para Java SE 11 que será extendido en los cuatro módulos restantes del curso.
- **Diseñado tres interfaces de dominio** (`Processable`, `Auditable`, `Discountable`) que ejercitan los cuatro tipos de métodos en interfaces: abstractos, `default`, `static` y `private`.
- **Implementado tres interfaces funcionales** (`OrderValidator<T>`, `OrderTransformer<T,R>`, `OrderNotifier`) validadas con `@FunctionalInterface`, confirmando que el compilador rechaza contratos inválidos.
- **Construido cuatro clases de implementación** y una clase cliente (`OrderClient`) que demuestra el polimorfismo e intercambiabilidad sin modificar el código consumidor.

### Relación con los temas del temario

| Tema | Cobertura en el laboratorio |
|---|---|
| 1.1 Interfaces | Pasos 3–4, 7–8: definición de contratos, implementación múltiple, polimorfismo |
| 1.2 Interfaces Avanzadas | Pasos 5–6, 9: métodos `default`, `static` y `private` en `Auditable` y `Discountable` |
| 1.3 Interfaces Funcionales | Pasos 10–13: `@FunctionalInterface`, genéricos, prueba negativa de compilación |

### Próximos pasos

En el **Módulo 2** extenderás este proyecto añadiendo el paquete `com.curso.modulo2` con colecciones genéricas (`List`, `Set`, `Map`), parámetros de tipo y comodines acotados para gestionar conjuntos de objetos `Order`. Las interfaces funcionales creadas aquí serán reutilizadas como base para las expresiones lambda del Módulo 3.

### Recursos adicionales

- [JEP 213: Milling Project Coin — Private Interface Methods](https://openjdk.org/jeps/213)
- [Java Language Specification SE 11 — §9.4: Method Declarations in Interfaces](https://docs.oracle.com/javase/specs/jls/se11/html/jls-9.html#jls-9.4)
- [Oracle Tutorial: Default Methods](https://docs.oracle.com/javase/tutorial/java/IandI/defaultmethods.html)
- [Baeldung: @FunctionalInterface Annotation](https://www.baeldung.com/java-8-functional-interfaces)
