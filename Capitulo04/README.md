# Práctica: Procesamiento y transformación de datos con Stream API

## Metadatos

| Campo | Valor |
|---|---|
| **Duración** | 115 minutos |
| **Complejidad** | Alta |
| **Nivel Bloom** | Aplicar |
| **Módulo** | 4 — Stream API |
| **Prerrequisito directo** | Lab 03-00-01 |

## Descripción General

En este laboratorio construirás nueve pipelines de procesamiento sobre un conjunto de datos de negocio (pedidos, líneas de pedido y productos) utilizando la Stream API de Java SE 11. Partirás de una clase `DataSetFactory` que genera 50 pedidos reproducibles y avanzarás progresivamente desde operaciones intermedias simples (`filter`, `map`) hasta reducciones (`reduce`), operaciones de corto circuito (`anyMatch`, `findFirst`), manejo de ausencia con `Optional` y reportes agrupados con `Collectors`. El resultado final es una clase `StreamReportDemo` ejecutable que imprime todos los reportes por consola y que será reutilizada en el Módulo 5.

## Objetivos de Aprendizaje

- [ ] Construir pipelines que combinen operaciones intermedias (`filter`, `map`, `flatMap`, `sorted`, `distinct`) con operaciones finales (`collect`, `reduce`, `count`) sobre colecciones de dominio.
- [ ] Aplicar operaciones de corto circuito (`anyMatch`, `findFirst`) y manejar resultados ausentes con `Optional` (`of`, `ofNullable`, `empty`, `map`, `filter`, `orElseGet`).
- [ ] Generar reportes agrupados y particionados mediante `Collectors.groupingBy`, `partitioningBy`, `joining` y `toMap`.
- [ ] Distinguir entre evaluación perezosa (lazy) de operaciones intermedias y evaluación inmediata (eager) de operaciones finales, verificando el comportamiento con `peek`.

## Prerrequisitos

### Conocimientos

| Tema | Detalle |
|---|---|
| Lab 03-00-01 completado | Clases `Order`, `Product`, interfaces funcionales y lambdas del Módulo 3 disponibles |
| Expresiones Lambda | Sintaxis, referencias a métodos, interfaces funcionales de `java.util.function` |
| Colecciones Java | `List`, `Map`, `Set` y sus operaciones básicas |
| Evaluación lazy | Concepto de evaluación diferida en programación funcional |

### Acceso y Software

| Componente | Versión | Fuente oficial |
|---|---|---|
| JDK Eclipse Temurin | 11.0.23+9 | https://adoptium.net/temurin/releases/?version=11 |
| Apache Maven | 3.9.8 | https://maven.apache.org/download.cgi |
| IntelliJ IDEA Community | 2024.1.4 | https://www.jetbrains.com/idea/download/ |
| Git | 2.45.2 | https://git-scm.com/downloads |

## Entorno de Laboratorio

**Directorio de trabajo:** `~/cursos/java-se11-labs` (Linux/macOS) o `C:\cursos\java-se11-labs` (Windows).

**Estructura existente tras el Lab 03-00-01:**

```
src/main/java/com/curso/
├── modulo1/          ← Order.java, interfaces funcionales
├── modulo2/          ← Product.java, DataRepository.java
└── modulo3/          ← LambdaRefactorDemo.java, OrderPredicates.java,
                        OrderFunctions.java, LambdaDemo.java, Transformer.java
```

**Estructura objetivo al finalizar este laboratorio:**

```
src/main/java/com/curso/
├── modulo1/
├── modulo2/
├── modulo3/
└── modulo4/          ← NUEVO
    ├── OrderLine.java
    ├── DataSetFactory.java
    ├── OrderStreamPipelines.java
    └── StreamReportDemo.java
```

**Verificación rápida del entorno:**

```bash
cd ~/cursos/java-se11-labs
java -version
mvn -version
mvn compile -q
```

> La salida de `mvn compile -q` debe mostrar `BUILD SUCCESS`. Si falla, revisa que el Lab 03-00-01 esté completo.

## Instrucciones Paso a Paso

### Paso 1: Crear el paquete del Módulo 4 y verificar la estructura

**Objetivo:** Preparar el paquete `com.curso.modulo4` que contendrá todas las clases de este laboratorio.

1. Abre una terminal en el directorio raíz del proyecto:

```bash
cd ~/cursos/java-se11-labs
```

2. Crea el directorio del paquete:

```bash
mkdir -p src/main/java/com/curso/modulo4
```

3. Verifica la estructura completa:

```bash
find src/main/java/com/curso -type d | sort
```

**Salida esperada:**

```
src/main/java/com/curso
src/main/java/com/curso/modulo1
src/main/java/com/curso/modulo2
src/main/java/com/curso/modulo3
src/main/java/com/curso/modulo4
```

**Verificación:** El paquete `modulo4` aparece junto a los tres módulos anteriores.

---

### Paso 2: Crear la entidad OrderLine (línea de pedido)

**Objetivo:** Definir la clase `OrderLine` que modela cada línea dentro de un pedido, conteniendo un `Product`, una cantidad y un subtotal. Esta entidad permite usar `flatMap` para aplanar colecciones anidadas.

1. Crea el archivo `src/main/java/com/curso/modulo4/OrderLine.java`:

```java
package com.curso.modulo4;

import com.curso.modulo2.Product;

public class OrderLine {

    private final Product product;
    private final int quantity;
    private final double subtotal;

    public OrderLine(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
        this.subtotal = product.getPrice() * quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getSubtotal() { return subtotal; }

    @Override
    public String toString() {
        return product.getName() + " x" + quantity
               + " = $" + String.format("%.2f", subtotal);
    }
}
```

2. Compila para verificar que `Product` del Módulo 2 es accesible:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** La clase compila sin errores. Si `Product` no tiene el método `getPrice()`, revisa tu implementación del Módulo 2 y añádelo.

---

### Paso 3: Extender Order para soportar líneas de pedido

**Objetivo:** Añadir a la clase `Order` del Módulo 1 un campo `List<OrderLine>` y un método `getOrderLines()` sin romper los constructores existentes. Esto habilita el uso de `flatMap` sobre las líneas de cada pedido.

1. Abre `src/main/java/com/curso/modulo1/Order.java` y añade el import y el campo al inicio de la clase (sin eliminar campos ni constructores existentes):

```java
// Añadir al inicio del archivo, junto a los demás imports
import com.curso.modulo4.OrderLine;
import java.util.ArrayList;
import java.util.Collections;
```

2. Dentro de la clase `Order`, añade el campo y los métodos de acceso:

```java
    // --- Campo para líneas de pedido (Módulo 4) ---
    private List<OrderLine> orderLines = new ArrayList<>();

    public List<OrderLine> getOrderLines() {
        return Collections.unmodifiableList(orderLines);
    }

    public void addOrderLine(OrderLine line) {
        this.orderLines.add(line);
    }

    public void setOrderLines(List<OrderLine> lines) {
        this.orderLines = new ArrayList<>(lines);
    }
```

3. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** La clase `Order` ahora expone `getOrderLines()` que devuelve una lista inmutable. Los constructores y métodos previos siguen intactos.

> **Nota:** Si el compilador reporta una dependencia circular entre `modulo1` y `modulo4`, verifica que `OrderLine` solo importe de `modulo2` (no de `modulo1`). La dependencia es unidireccional: `Order` → `OrderLine` → `Product`.

---

### Paso 4: Crear la clase DataSetFactory con 50 pedidos reproducibles

**Objetivo:** Implementar `DataSetFactory` como el único generador de datos de prueba del proyecto a partir del Módulo 4. Usa un `Supplier<List<Order>>` y genera exactamente 50 pedidos con estados variados, múltiples líneas y montos diversos.

1. Crea `src/main/java/com/curso/modulo4/DataSetFactory.java`:

```java
package com.curso.modulo4;

import com.curso.modulo1.Order;
import com.curso.modulo2.Product;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class DataSetFactory {

    public static final int DEFAULT_SIZE = 50;

    private static final String[] STATUSES = {
        "ACTIVE", "COMPLETED", "CANCELLED", "PENDING"
    };

    private static final String[] CUSTOMERS = {
        "Ana García", "Carlos López", "Beatriz Ruiz",
        "Daniel Mora", "Elena Torres", "Fernando Díaz",
        "Gabriela Soto", "Hugo Vargas", "Isabel Peña", "Jorge Ramos"
    };

    private static final List<Product> CATALOG = Arrays.asList(
        new Product(1, "Laptop Pro 15", 1299.99),
        new Product(2, "Mouse Inalámbrico", 29.99),
        new Product(3, "Teclado Mecánico", 89.99),
        new Product(4, "Monitor 27 4K", 449.99),
        new Product(5, "Auriculares BT", 59.99),
        new Product(6, "Webcam HD", 74.99),
        new Product(7, "Hub USB-C", 39.99),
        new Product(8, "SSD 1TB", 109.99),
        new Product(9, "Cable HDMI 2m", 14.99),
        new Product(10, "Soporte Monitor", 34.99)
    );

    /**
     * Supplier que genera una lista de DEFAULT_SIZE pedidos reproducibles.
     * Cada pedido tiene entre 1 y 4 líneas de pedido.
     */
    public static final Supplier<List<Order>> orderSupplier = () -> {
        List<Order> orders = new ArrayList<>();
        for (int i = 1; i <= DEFAULT_SIZE; i++) {
            String code = String.format("ORD-%04d", i);
            String customer = CUSTOMERS[i % CUSTOMERS.length];
            String status = STATUSES[i % STATUSES.length];
            LocalDate date = LocalDate.of(2024, 1, 1).plusDays(i);

            Order order = new Order(i, customer, 0.0, status, date);
            // Generar entre 1 y 4 líneas de pedido
            int lineCount = (i % 4) + 1;
            double total = 0.0;
            List<OrderLine> lines = new ArrayList<>();
            for (int j = 0; j < lineCount; j++) {
                Product product = CATALOG.get((i + j) % CATALOG.size());
                int qty = (j % 3) + 1;
                OrderLine line = new OrderLine(product, qty);
                lines.add(line);
                total += line.getSubtotal();
            }
            order.setOrderLines(lines);
            order.setTotalAmount(total);
            orders.add(order);
        }
        return orders;
    };

    /**
     * Método de conveniencia que invoca al Supplier.
     */
    public static List<Order> createOrders() {
        return orderSupplier.get();
    }

    public static List<Product> getCatalog() {
        return CATALOG;
    }
}
```

> **Nota:** Si tu clase `Product` del Módulo 2 no tiene un constructor `(int id, String name, double price)`, añádelo ahora. De igual forma, si `Order` no tiene `setTotalAmount(double)`, agrégalo.

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** La clase compila y genera exactamente 50 pedidos. La constante `DEFAULT_SIZE = 50` garantiza reproducibilidad entre participantes.

---

### Paso 5: Verificar el constructor y métodos requeridos en Product y Order

**Objetivo:** Asegurar que las clases `Product` y `Order` exponen los métodos que `DataSetFactory` y los pipelines posteriores necesitan.

1. Si `Product` no tiene el constructor de tres argumentos, abre `src/main/java/com/curso/modulo2/Product.java` y añade:

```java
    // Constructor requerido por DataSetFactory
    public Product(int id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // Getters si no existen
    public int getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
```

2. Si `Order` no tiene `setTotalAmount`, abre `src/main/java/com/curso/modulo1/Order.java` y añade:

```java
    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    // Getter de código de pedido para reportes
    public String getCode() {
        return String.format("ORD-%04d", id);
    }
```

3. Compila todo el proyecto:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** Todos los módulos compilan sin errores. Los métodos `getCode()`, `getStatus()`, `getTotalAmount()`, `getCustomerName()`, `getOrderLines()` y `setTotalAmount()` están disponibles en `Order`.

---

### Paso 6: Crear la clase OrderStreamPipelines — estructura base

**Objetivo:** Crear la clase que contendrá los nueve pipelines de procesamiento. En este paso se establece la estructura con el primer pipeline: filtrar pedidos activos y extraer sus resúmenes.

1. Crea `src/main/java/com/curso/modulo4/OrderStreamPipelines.java`:

```java
package com.curso.modulo4;

import com.curso.modulo1.Order;
import com.curso.modulo2.Product;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contiene 9 pipelines de procesamiento sobre pedidos.
 * Cada método documenta las operaciones utilizadas y el resultado esperado.
 */
public class OrderStreamPipelines {

    private final List<Order> orders;

    public OrderStreamPipelines(List<Order> orders) {
        this.orders = orders;
    }

    // ─── Pipeline 1: filter + map + collect(toList) ───
    /**
     * Obtiene los resúmenes (String) de pedidos con estado "ACTIVE".
     * Operaciones: filter (intermedia) → map (intermedia) → collect (final).
     */
    public List<String> getActiveOrderSummaries() {
        return orders.stream()
            .filter(o -> "ACTIVE".equals(o.getStatus()))
            .map(o -> o.getCode() + " | " + o.getCustomerName()
                       + " | $" + String.format("%.2f", o.getTotalAmount()))
            .collect(Collectors.toList());
    }
}
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** El pipeline 1 usa `filter` (operación intermedia lazy), `map` (intermedia lazy) y `collect` (operación final eager que dispara la ejecución).

---

### Paso 7: Pipeline 2 — flatMap + distinct + sorted para productos únicos

**Objetivo:** Usar `flatMap` para aplanar las líneas de pedido de todos los pedidos, extraer los productos y obtener una lista de nombres únicos ordenados alfabéticamente.

1. Añade el siguiente método a `OrderStreamPipelines.java`:

```java
    // ─── Pipeline 2: flatMap + distinct + sorted ───
    /**
     * Obtiene los nombres de todos los productos pedidos, sin duplicados, ordenados.
     * flatMap aplana List<OrderLine> de cada Order en un solo Stream<OrderLine>.
     */
    public List<String> getUniqueProductNamesSorted() {
        return orders.stream()
            .flatMap(o -> o.getOrderLines().stream())   // Aplana OrderLines
            .map(line -> line.getProduct().getName())    // Extrae nombre
            .distinct()                                   // Elimina duplicados
            .sorted()                                     // Orden alfabético
            .collect(Collectors.toList());
    }
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** `flatMap` convierte `Stream<Order>` → `Stream<OrderLine>` aplanando las listas anidadas. `distinct` es una operación *stateful* que necesita recordar elementos previos. `sorted` también es *stateful*.

---

### Paso 8: Pipeline 3 — reduce para calcular el monto total

**Objetivo:** Usar `reduce` para acumular el monto total de todos los pedidos en un solo valor.

1. Añade el siguiente método a `OrderStreamPipelines.java`:

```java
    // ─── Pipeline 3: reduce ───
    /**
     * Calcula el monto total de todos los pedidos mediante reduce.
     * reduce(identity, accumulator) es una operación final.
     */
    public double getTotalRevenue() {
        return orders.stream()
            .map(Order::getTotalAmount)
            .reduce(0.0, Double::sum);
    }
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** `reduce(0.0, Double::sum)` usa `0.0` como identidad y `Double::sum` como acumulador. El resultado es determinístico porque la suma de doubles con la misma secuencia produce el mismo resultado.

---

### Paso 9: Pipeline 4 — count para contar pedidos por estado

**Objetivo:** Usar `Collectors.groupingBy` combinado con `Collectors.counting()` para obtener un mapa de estado → cantidad.

1. Añade el siguiente método:

```java
    // ─── Pipeline 4: groupingBy + counting ───
    /**
     * Cuenta pedidos por estado: Map<String, Long>.
     * groupingBy es un Collector downstream que agrupa por clave.
     */
    public Map<String, Long> countOrdersByStatus() {
        return orders.stream()
            .collect(Collectors.groupingBy(Order::getStatus, Collectors.counting()));
    }
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** `groupingBy` con downstream `counting()` produce un `Map<String, Long>` con las claves `ACTIVE`, `COMPLETED`, `CANCELLED` y `PENDING`.

---

### Paso 10: Pipeline 5 — anyMatch + findFirst + Optional

**Objetivo:** Usar operaciones de corto circuito para localizar el primer pedido cuyo monto supere un umbral. Manejar el caso ausente con `Optional.orElseGet`.

1. Añade los siguientes métodos:

```java
    // ─── Pipeline 5: anyMatch + findFirst + Optional ───
    /**
     * Verifica si existe algún pedido con monto superior al umbral.
     * anyMatch es una operación final de corto circuito.
     */
    public boolean existsOrderAbove(double threshold) {
        return orders.stream()
            .anyMatch(o -> o.getTotalAmount() > threshold);
    }

    /**
     * Encuentra el primer pedido con monto superior al umbral.
     * findFirst devuelve Optional<Order>; se maneja la ausencia con orElseGet.
     */
    public Order findFirstOrderAbove(double threshold) {
        return orders.stream()
            .filter(o -> o.getTotalAmount() > threshold)
            .findFirst()
            .orElseGet(() -> {
                System.out.println("  [INFO] No se encontró pedido sobre $"
                    + String.format("%.2f", threshold) + ". Retornando pedido vacío.");
                return new Order(0, "N/A", 0.0, "EMPTY",
                    java.time.LocalDate.now());
            });
    }
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** `anyMatch` detiene el recorrido al encontrar el primer elemento que cumple la condición. `findFirst` retorna `Optional<Order>`, y `orElseGet` proporciona un valor por defecto mediante un `Supplier` si el `Optional` está vacío.

---

### Paso 11: Explorar Optional de forma explícita

**Objetivo:** Demostrar la creación y encadenamiento de `Optional` con `of`, `ofNullable`, `empty`, `map`, `filter` y `orElseGet` de forma independiente a los streams.

1. Añade los siguientes métodos estáticos a `OrderStreamPipelines.java`:

```java
    // ─── Exploración de Optional ───
    /**
     * Demuestra Optional.of, ofNullable, empty, map, filter, orElseGet.
     */
    public static void demonstrateOptional() {
        // Optional.of — lanza NullPointerException si el valor es null
        Optional<String> present = Optional.of("Pedido encontrado");
        System.out.println("  Optional.of: " + present.get());

        // Optional.ofNullable — seguro con valores posiblemente nulos
        String nullValue = null;
        Optional<String> nullable = Optional.ofNullable(nullValue);
        System.out.println("  Optional.ofNullable(null) presente: "
            + nullable.isPresent());

        // Optional.empty — representa ausencia explícita
        Optional<String> empty = Optional.empty();
        System.out.println("  Optional.empty presente: " + empty.isPresent());

        // Encadenamiento: map + filter + orElseGet
        String result = Optional.ofNullable("ord-0001")
            .map(String::toUpperCase)
            .filter(s -> s.startsWith("ORD"))
            .orElseGet(() -> "CÓDIGO DESCONOCIDO");
        System.out.println("  Encadenamiento Optional: " + result);

        // Caso donde filter no coincide
        String fallback = Optional.ofNullable("xyz-999")
            .map(String::toUpperCase)
            .filter(s -> s.startsWith("ORD"))
            .orElseGet(() -> "CÓDIGO DESCONOCIDO");
        System.out.println("  Encadenamiento con fallback: " + fallback);
    }
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** El método demuestra los tres métodos de fábrica de `Optional` y el encadenamiento `map` → `filter` → `orElseGet` que evita `NullPointerException` de forma declarativa.

---

### Paso 12: Pipeline 6 — groupingBy para agrupar pedidos por estado

**Objetivo:** Generar un `Map<String, List<Order>>` que agrupe todos los pedidos por su estado.

1. Añade el siguiente método:

```java
    // ─── Pipeline 6: groupingBy ───
    /**
     * Agrupa pedidos por estado. Resultado: Map<String, List<Order>>.
     */
    public Map<String, List<Order>> groupOrdersByStatus() {
        return orders.stream()
            .collect(Collectors.groupingBy(Order::getStatus));
    }
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** `groupingBy(Order::getStatus)` sin downstream collector usa `toList()` implícitamente como downstream.

---

### Paso 13: Pipeline 7 — partitioningBy para separar por umbral de monto

**Objetivo:** Usar `partitioningBy` para dividir pedidos en dos grupos: los que superan un umbral de monto y los que no.

1. Añade el siguiente método:

```java
    // ─── Pipeline 7: partitioningBy ───
    /**
     * Particiona pedidos en dos grupos: monto > umbral (true) y monto <= umbral (false).
     * partitioningBy siempre retorna exactamente dos entradas: true y false.
     */
    public Map<Boolean, List<Order>> partitionByAmount(double threshold) {
        return orders.stream()
            .collect(Collectors.partitioningBy(
                o -> o.getTotalAmount() > threshold));
    }
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** A diferencia de `groupingBy`, `partitioningBy` siempre produce exactamente dos claves (`true` y `false`), lo que simplifica el acceso posterior.

---

### Paso 14: Pipeline 8 — joining para generar reporte CSV

**Objetivo:** Usar `Collectors.joining` para construir una cadena CSV con los códigos de todos los pedidos activos.

1. Añade el siguiente método:

```java
    // ─── Pipeline 8: joining ───
    /**
     * Genera un reporte CSV con los códigos de pedidos activos.
     * joining(delimiter, prefix, suffix) concatena los elementos del Stream.
     */
    public String getActiveOrderCodesCSV() {
        return orders.stream()
            .filter(o -> "ACTIVE".equals(o.getStatus()))
            .map(Order::getCode)
            .sorted()
            .collect(Collectors.joining(", ", "[", "]"));
    }
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** `joining(", ", "[", "]")` produce una cadena como `[ORD-0001, ORD-0005, ...]` con delimitador, prefijo y sufijo.

---

### Paso 15: Pipeline 9 — toMap para crear índice por código

**Objetivo:** Usar `Collectors.toMap` para construir un `Map<String, Order>` indexado por el código de pedido.

1. Añade el siguiente método:

```java
    // ─── Pipeline 9: toMap ───
    /**
     * Construye un índice Map<String, Order> por código de pedido.
     * toMap lanza IllegalStateException si hay claves duplicadas;
     * usamos merge function para manejar ese caso.
     */
    public Map<String, Order> buildOrderIndex() {
        return orders.stream()
            .collect(Collectors.toMap(
                Order::getCode,           // keyMapper
                o -> o,                   // valueMapper
                (existing, replacement) -> existing  // merge: conservar primero
            ));
    }
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** La función de merge `(existing, replacement) -> existing` previene `IllegalStateException` si dos pedidos tuviesen el mismo código (caso defensivo). Con nuestro dataset de 50 pedidos con códigos únicos, nunca se invocará.

---

### Paso 16: Añadir pipeline de demostración de evaluación lazy con peek

**Objetivo:** Demostrar que las operaciones intermedias no se ejecutan hasta que una operación final las dispara, usando `peek` como herramienta de diagnóstico.

1. Añade el siguiente método:

```java
    // ─── Demostración de evaluación lazy ───
    /**
     * Muestra que peek (operación intermedia) solo se ejecuta cuando
     * la operación final (count) dispara el pipeline.
     */
    public long demonstrateLazyEvaluation() {
        System.out.println("  → Definiendo pipeline (aún no se ejecuta)...");

        long count = orders.stream()
            .peek(o -> System.out.println("    peek: procesando " + o.getCode()))
            .filter(o -> "ACTIVE".equals(o.getStatus()))
            .peek(o -> System.out.println("    peek post-filter: " + o.getCode()))
            .count();

        System.out.println("  → Pipeline ejecutado. Pedidos activos: " + count);
        return count;
    }
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** Al ejecutar este método se observará que `peek` imprime mensajes solo cuando `count()` (operación final) dispara la evaluación. Los elementos que no pasan el `filter` no llegan al segundo `peek`.

---

### Paso 17: Añadir pipeline con limit y skip

**Objetivo:** Demostrar las operaciones intermedias `skip` y `limit` para paginar resultados.

1. Añade el siguiente método:

```java
    // ─── Pipeline extra: skip + limit (paginación) ───
    /**
     * Obtiene una "página" de pedidos: salta los primeros 'skip' y toma 'limit'.
     */
    public List<String> getOrderPage(int skipCount, int pageSize) {
        return orders.stream()
            .sorted((a, b) -> a.getCode().compareTo(b.getCode()))
            .skip(skipCount)
            .limit(pageSize)
            .map(o -> o.getCode() + " | $"
                       + String.format("%.2f", o.getTotalAmount()))
            .collect(Collectors.toList());
    }
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** `skip` y `limit` son operaciones intermedias *stateful*. Combinadas con `sorted`, permiten implementar paginación declarativa.

---

### Paso 18: Añadir pipeline con summarizingDouble

**Objetivo:** Usar `Collectors.summarizingDouble` para obtener estadísticas descriptivas (count, sum, min, average, max) de los montos.

1. Añade el import y el método:

```java
    // Añadir import al inicio del archivo
    // import java.util.DoubleSummaryStatistics;
```

```java
    // ─── Pipeline extra: summarizingDouble ───
    /**
     * Genera estadísticas descriptivas de los montos de pedidos.
     */
    public java.util.DoubleSummaryStatistics getAmountStatistics() {
        return orders.stream()
            .collect(Collectors.summarizingDouble(Order::getTotalAmount));
    }
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** `DoubleSummaryStatistics` expone `getCount()`, `getSum()`, `getMin()`, `getAverage()` y `getMax()`.

---

### Paso 19: Construir la clase StreamReportDemo

**Objetivo:** Crear la clase principal ejecutable que invoca todos los pipelines y genera un reporte completo por consola. Esta clase será el punto de entrada de verificación del instructor.

1. Crea `src/main/java/com/curso/modulo4/StreamReportDemo.java`:

```java
package com.curso.modulo4;

import com.curso.modulo1.Order;

import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;

public class StreamReportDemo {

    public static void main(String[] args) {

        System.out.println("═══════════════════════════════════════════════");
        System.out.println("  STREAM REPORT DEMO — Módulo 4");
        System.out.println("═══════════════════════════════════════════════");

        // Generar dataset
        List<Order> orders = DataSetFactory.createOrders();
        System.out.println("\nDataset generado: " + orders.size() + " pedidos.\n");

        OrderStreamPipelines pipelines = new OrderStreamPipelines(orders);

        // ─── Pipeline 1: Resúmenes de pedidos activos ───
        System.out.println("── Pipeline 1: Resúmenes de pedidos ACTIVE ──");
        List<String> activeSummaries = pipelines.getActiveOrderSummaries();
        System.out.println("  Cantidad: " + activeSummaries.size());
        activeSummaries.forEach(s -> System.out.println("  " + s));

        // ─── Pipeline 2: Productos únicos ordenados ───
        System.out.println("\n── Pipeline 2: Productos únicos (flatMap) ──");
        List<String> uniqueProducts = pipelines.getUniqueProductNamesSorted();
        System.out.println("  Productos únicos: " + uniqueProducts.size());
        uniqueProducts.forEach(p -> System.out.println("  • " + p));

        // ─── Pipeline 3: Monto total (reduce) ───
        System.out.println("\n── Pipeline 3: Monto total (reduce) ──");
        double totalRevenue = pipelines.getTotalRevenue();
        System.out.println("  Revenue total: $"
            + String.format("%.2f", totalRevenue));

        // ─── Pipeline 4: Conteo por estado ───
        System.out.println("\n── Pipeline 4: Conteo por estado ──");
        Map<String, Long> countByStatus = pipelines.countOrdersByStatus();
        countByStatus.forEach((status, count) ->
            System.out.println("  " + status + ": " + count));

        // ─── Pipeline 5: anyMatch + findFirst + Optional ───
        System.out.println("\n── Pipeline 5: Corto circuito + Optional ──");
        double threshold = 2000.0;
        boolean exists = pipelines.existsOrderAbove(threshold);
        System.out.println("  ¿Existe pedido > $"
            + String.format("%.2f", threshold) + "? " + exists);
        Order found = pipelines.findFirstOrderAbove(threshold);
        System.out.println("  Primer pedido encontrado: " + found.getCode()
            + " | $" + String.format("%.2f", found.getTotalAmount()));

        // ─── Demostración de Optional ───
        System.out.println("\n── Demostración de Optional ──");
        OrderStreamPipelines.demonstrateOptional();

        // ─── Pipeline 6: Agrupar por estado ───
        System.out.println("\n── Pipeline 6: Agrupar por estado (groupingBy) ──");
        Map<String, List<Order>> grouped = pipelines.groupOrdersByStatus();
        grouped.forEach((status, list) ->
            System.out.println("  " + status + ": " + list.size() + " pedidos"));

        // ─── Pipeline 7: Particionar por monto ───
        System.out.println("\n── Pipeline 7: Particionar por monto (partitioningBy) ──");
        double partitionThreshold = 500.0;
        Map<Boolean, List<Order>> partitioned =
            pipelines.partitionByAmount(partitionThreshold);
        System.out.println("  Monto > $" + String.format("%.2f", partitionThreshold)
            + ": " + partitioned.get(true).size() + " pedidos");
        System.out.println("  Monto <= $" + String.format("%.2f", partitionThreshold)
            + ": " + partitioned.get(false).size() + " pedidos");

        // ─── Pipeline 8: Reporte CSV (joining) ───
        System.out.println("\n── Pipeline 8: Códigos activos CSV (joining) ──");
        String csv = pipelines.getActiveOrderCodesCSV();
        System.out.println("  " + csv);

        // ─── Pipeline 9: Índice por código (toMap) ───
        System.out.println("\n── Pipeline 9: Índice por código (toMap) ──");
        Map<String, Order> index = pipelines.buildOrderIndex();
        System.out.println("  Tamaño del índice: " + index.size());
        // Verificar acceso directo
        Order sample = index.get("ORD-0025");
        if (sample != null) {
            System.out.println("  ORD-0025 → " + sample.getCustomerName()
                + " | $" + String.format("%.2f", sample.getTotalAmount()));
        }

        // ─── Evaluación lazy con peek ───
        System.out.println("\n── Demostración: Evaluación Lazy (peek) ──");
        pipelines.demonstrateLazyEvaluation();

        // ─── Paginación con skip + limit ───
        System.out.println("\n── Paginación: skip(10) + limit(5) ──");
        List<String> page = pipelines.getOrderPage(10, 5);
        page.forEach(p -> System.out.println("  " + p));

        // ─── Estadísticas (summarizingDouble) ───
        System.out.println("\n── Estadísticas de montos ──");
        DoubleSummaryStatistics stats = pipelines.getAmountStatistics();
        System.out.println("  Count:   " + stats.getCount());
        System.out.println("  Sum:     $" + String.format("%.2f", stats.getSum()));
        System.out.println("  Min:     $" + String.format("%.2f", stats.getMin()));
        System.out.println("  Average: $" + String.format("%.2f", stats.getAverage()));
        System.out.println("  Max:     $" + String.format("%.2f", stats.getMax()));

        System.out.println("\n═══════════════════════════════════════════════");
        System.out.println("  REPORTE COMPLETO — Fin");
        System.out.println("═══════════════════════════════════════════════");
    }
}
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** La clase compila exitosamente y está lista para ejecutarse.

---

### Paso 20: Ejecutar StreamReportDemo y verificar todos los pipelines

**Objetivo:** Ejecutar la clase principal y validar que los 9 pipelines más las demostraciones adicionales producen resultados coherentes con el dataset de 50 pedidos.

1. Ejecuta la clase:

```bash
mvn exec:java -Dexec.mainClass="com.curso.modulo4.StreamReportDemo" -q
```

> Si no tienes configurado el plugin `exec-maven-plugin`, puedes ejecutar directamente:

```bash
mvn compile -q
java -cp target/classes com.curso.modulo4.StreamReportDemo
```

**Salida esperada (fragmento representativo):**

```
═══════════════════════════════════════════════
  STREAM REPORT DEMO — Módulo 4
═══════════════════════════════════════════════

Dataset generado: 50 pedidos.

── Pipeline 1: Resúmenes de pedidos ACTIVE ──
  Cantidad: 12
  ORD-0002 | Carlos López | $...
  ...

── Pipeline 2: Productos únicos (flatMap) ──
  Productos únicos: 10
  • Auriculares BT
  • Cable HDMI 2m
  • Hub USB-C
  • Laptop Pro 15
  • Monitor 27 4K
  • Mouse Inalámbrico
  • SSD 1TB
  • Soporte Monitor
  • Teclado Mecánico
  • Webcam HD

── Pipeline 3: Monto total (reduce) ──
  Revenue total: $...

── Pipeline 4: Conteo por estado ──
  ACTIVE: 12
  CANCELLED: 13
  COMPLETED: 12
  PENDING: 13

── Pipeline 5: Corto circuito + Optional ──
  ¿Existe pedido > $2000.00? true
  Primer pedido encontrado: ORD-... | $...

── Demostración de Optional ──
  Optional.of: Pedido encontrado
  Optional.ofNullable(null) presente: false
  Optional.empty presente: false
  Encadenamiento Optional: ORD-0001
  Encadenamiento con fallback: CÓDIGO DESCONOCIDO

── Pipeline 6: Agrupar por estado (groupingBy) ──
  ACTIVE: 12 pedidos
  CANCELLED: 13 pedidos
  ...

── Pipeline 7: Particionar por monto (partitioningBy) ──
  Monto > $500.00: ... pedidos
  Monto <= $500.00: ... pedidos

── Pipeline 8: Códigos activos CSV (joining) ──
  [ORD-0002, ORD-0006, ...]

── Pipeline 9: Índice por código (toMap) ──
  Tamaño del índice: 50
  ORD-0025 → ... | $...

── Demostración: Evaluación Lazy (peek) ──
  → Definiendo pipeline (aún no se ejecuta)...
    peek: procesando ORD-0001
    peek: procesando ORD-0002
    peek post-filter: ORD-0002
    ...
  → Pipeline ejecutado. Pedidos activos: 12

── Paginación: skip(10) + limit(5) ──
  ORD-0011 | $...
  ...

── Estadísticas de montos ──
  Count:   50
  Sum:     $...
  Min:     $...
  Average: $...
  Max:     $...

═══════════════════════════════════════════════
  REPORTE COMPLETO — Fin
═══════════════════════════════════════════════
```

2. Verifica los siguientes criterios:

| Criterio | Valor esperado |
|---|---|
| Dataset generado | Exactamente 50 pedidos |
| Pipeline 1 — pedidos ACTIVE | ~12 pedidos (depende del módulo aritmético) |
| Pipeline 2 — productos únicos | Exactamente 10 (tamaño del catálogo) |
| Pipeline 4 — suma de conteos | 50 (12 + 12 + 13 + 13) |
| Pipeline 9 — tamaño del índice | 50 |
| Evaluación lazy | Los mensajes de `peek` aparecen intercalados con el filtrado |
| Estadísticas — count | 50 |

**Verificación:** Todos los pipelines producen resultados coherentes. Los conteos suman 50. Los productos únicos son exactamente los 10 del catálogo. La demostración de `peek` confirma la evaluación perezosa.

---

### Paso 21: Verificar que el proyecto completo compila sin regresiones

**Objetivo:** Asegurar que las modificaciones a `Order` y `Product` no rompen los módulos anteriores.

1. Ejecuta la compilación completa:

```bash
mvn clean compile -q
```

2. Ejecuta la clase del Módulo 3 para verificar compatibilidad:

```bash
java -cp target/classes com.curso.modulo3.LambdaDemo
```

**Salida esperada:** La salida del Lab 03-00-01 se produce sin errores.

**Verificación:** `BUILD SUCCESS` y las clases de módulos anteriores siguen funcionando correctamente. No hay regresiones.

---

### Paso 22: Verificar la estructura final del proyecto

**Objetivo:** Confirmar que todos los archivos creados están en su ubicación correcta.

1. Lista los archivos del Módulo 4:

```bash
find src/main/java/com/curso/modulo4 -name "*.java" | sort
```

**Salida esperada:**

```
src/main/java/com/curso/modulo4/DataSetFactory.java
src/main/java/com/curso/modulo4/OrderLine.java
src/main/java/com/curso/modulo4/OrderStreamPipelines.java
src/main/java/com/curso/modulo4/StreamReportDemo.java
```

**Verificación:** Cuatro archivos Java en el paquete `com.curso.modulo4`.

## Validación y Pruebas

### Criterios de aceptación medibles

Ejecuta los siguientes comandos y verifica cada criterio:

**1. Compilación limpia:**

```bash
mvn clean compile -q && echo "✓ Compilación exitosa"
```

**2. Dataset de 50 pedidos:**

```bash
java -cp target/classes -Dtest.only=dataset com.curso.modulo4.StreamReportDemo 2>&1 | head -5
```

Debe mostrar `Dataset generado: 50 pedidos.`

**3. Ejecución completa sin excepciones:**

```bash
java -cp target/classes com.curso.modulo4.StreamReportDemo 2>&1 | tail -3
```

Debe terminar con `REPORTE COMPLETO — Fin`.

**4. Verificación de conteos coherentes:**

```bash
java -cp target/classes com.curso.modulo4.StreamReportDemo 2>&1 | grep -E "(Count:|Tamaño del índice:)"
```

Ambas líneas deben mostrar el valor `50`.

**5. Caso adverso — Optional con valor ausente:**

La demostración de `Optional` debe mostrar:
- `Optional.ofNullable(null) presente: false`
- `Encadenamiento con fallback: CÓDIGO DESCONOCIDO`

Esto verifica que el manejo de ausencia funciona correctamente y no se produce `NullPointerException`.

**6. Caso adverso — Stream de uso único:**

Para verificar que un Stream no puede reutilizarse, añade temporalmente al final de `main` en `StreamReportDemo`:

```java
        // CASO ADVERSO: descomentar para verificar IllegalStateException
        // java.util.stream.Stream<Order> s = orders.stream().filter(o -> true);
        // s.count(); // Primera operación terminal: OK
        // s.count(); // Segunda: lanza IllegalStateException
```

> No es necesario ejecutar este código; basta con entender que el segundo `count()` lanzaría `IllegalStateException`, confirmando la característica de uso único de los Streams.

### Tabla resumen de validación

| # | Criterio | Comando de verificación | Resultado esperado |
|---|---|---|---|
| 1 | Compilación | `mvn clean compile -q` | `BUILD SUCCESS` |
| 2 | Dataset | Línea "50 pedidos" en salida | 50 |
| 3 | Sin excepciones | Ejecución completa | Termina sin `Exception` |
| 4 | Consistencia | Count = 50, índice = 50 | Ambos 50 |
| 5 | Optional vacío | Fallback mostrado | `CÓDIGO DESCONOCIDO` |
| 6 | Módulos previos | `java -cp ... LambdaDemo` | Sin regresiones |

## Solución de Problemas

### Problema 1: `IllegalStateException: stream has already been operated upon`

**Síntomas:** Al ejecutar `StreamReportDemo`, una de las secciones lanza:

```
java.lang.IllegalStateException: stream has already been operated upon or closed
```

**Causa:** Se almacenó un `Stream` en una variable y se intentó usar dos veces con operaciones finales diferentes. Los Streams son de uso único: una vez que una operación terminal los consume, quedan cerrados.

**Solución:** Nunca almacenes un Stream para reutilizarlo. Crea un nuevo Stream desde la colección fuente cada vez:

```java
// ✗ Incorrecto
Stream<Order> s = orders.stream().filter(o -> "ACTIVE".equals(o.getStatus()));
long count = s.count();        // OK
List<Order> list = s.collect(Collectors.toList()); // ¡IllegalStateException!

// ✓ Correcto
long count = orders.stream()
    .filter(o -> "ACTIVE".equals(o.getStatus())).count();
List<Order> list = orders.stream()
    .filter(o -> "ACTIVE".equals(o.getStatus())).collect(Collectors.toList());
```

---

### Problema 2: `Collectors.toMap` lanza `IllegalStateException: Duplicate key`

**Síntomas:** Al ejecutar el Pipeline 9 (`buildOrderIndex`), la ejecución falla con:

```
java.lang.IllegalStateException: Duplicate key ORD-XXXX
```

**Causa:** Dos pedidos en el dataset generaron el mismo código. Esto puede ocurrir si se modificó `DataSetFactory` y se introdujeron códigos duplicados, o si se usó `toMap` sin función de merge.

**Solución:** Siempre proporciona una función de merge como tercer argumento de `toMap`:

```java
// ✗ Sin merge — falla con duplicados
.collect(Collectors.toMap(Order::getCode, o -> o));

// ✓ Con merge — maneja duplicados conservando el primero
.collect(Collectors.toMap(
    Order::getCode,
    o -> o,
    (existing, replacement) -> existing
));
```

Verifica también que `DataSetFactory` genera códigos únicos con `String.format("ORD-%04d", i)` donde `i` va de 1 a 50.

## Limpieza

Este laboratorio no crea recursos externos (contenedores, bases de datos, servicios). La limpieza se limita a los artefactos de compilación:

```bash
mvn clean -q
```

> **No elimines** los archivos fuente del Módulo 4. Las clases `DataSetFactory` y `OrderStreamPipelines` serán importadas por el Módulo 5 (concurrencia con `parallelStream` y `ForkJoinPool`).

Si añadiste código temporal para el caso adverso de `IllegalStateException`, asegúrate de dejarlo comentado o eliminarlo.

## Resumen

En este laboratorio has construido un sistema completo de procesamiento de datos con la Stream API de Java SE 11:

| Concepto ejercitado | Pipeline(s) | Temas de la lección |
|---|---|---|
| Operaciones intermedias lazy | 1, 2, 7, 8, paginación | 4.1, 4.2, 4.4 |
| `flatMap` para aplanar colecciones anidadas | 2 | 4.4 |
| `reduce` como operación final | 3 | 4.5 |
| `groupingBy`, `counting` | 4, 6 | 4.7 |
| `anyMatch`, `findFirst` (corto circuito) | 5 | 4.6 |
| `Optional` (`of`, `ofNullable`, `empty`, `map`, `filter`, `orElseGet`) | 5, demostración | 4.3 |
| `partitioningBy` | 7 | 4.7 |
| `joining` | 8 | 4.7 |
| `toMap` | 9 | 4.7 |
| `summarizingDouble` | Estadísticas | 4.7 |
| Evaluación lazy con `peek` | Demostración | 4.1, 4.2 |
| `skip` + `limit` (paginación) | Paginación | 4.4 |

**Puntos clave:**
- Las operaciones intermedias son **lazy**: no se ejecutan hasta que una operación final las dispara.
- Los Streams son de **uso único** y no almacenan datos.
- `Optional` elimina la necesidad de verificaciones `null` explícitas mediante un API declarativo.
- `Collectors` proporciona herramientas poderosas para transformar streams en colecciones, mapas, cadenas y estadísticas.
- `DataSetFactory` es el generador canónico de datos del proyecto; no dupliques datasets en módulos posteriores.

### Recursos adicionales

- [java.util.stream — Documentación oficial Java SE 11](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/stream/package-summary.html)
- [java.util.Optional — Documentación oficial Java SE 11](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/Optional.html)
- [java.util.stream.Collectors — Documentación oficial Java SE 11](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/stream/Collectors.html)
- [Modern Java in Action — Urma, Fusco, Mycroft (Manning, 2ª ed.)](https://www.manning.com/books/modern-java-in-action)

---
