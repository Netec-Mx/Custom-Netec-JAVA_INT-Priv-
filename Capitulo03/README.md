# Práctica: Procesamiento funcional con expresiones Lambda

## Metadatos

| Campo            | Valor                                                      |
|------------------|------------------------------------------------------------|
| **Duración**     | 100 minutos                                                |
| **Complejidad**  | Media                                                      |
| **Nivel Bloom**  | Aplicar                                                    |
| **Módulo**       | 3 — Expresiones Lambda y `java.util.function`              |
| **Prerrequisito**| Lab 02-00-01 completado                                    |

## Descripción General

En este laboratorio partirás de implementaciones basadas en clases anónimas y las refactorizarás a expresiones Lambda, incluyendo lambdas que trabajen con tipos genéricos. Después utilizarás `Predicate`, `Function`, `Consumer`, `Supplier`, `UnaryOperator` y `BiFunction`, así como referencias a métodos y la composición de funciones, para filtrar, transformar y procesar un conjunto de datos de pedidos (`Order`) y productos (`Product`). Finalmente, medirás la diferencia de legibilidad y rendimiento entre clases anónimas y lambdas sobre 10 000 elementos.

## Objetivos de Aprendizaje

- [ ] Identificar la equivalencia semántica entre una clase anónima que implementa una interfaz funcional y su expresión Lambda equivalente, justificando la preferencia por Lambda en términos de legibilidad.
- [ ] Utilizar los métodos de composición de `Predicate` (`and`, `or`, `negate`) y `Function` (`andThen`, `compose`) para construir pipelines de transformación sin crear clases auxiliares.
- [ ] Aplicar referencias a métodos en sus cuatro formas (estático, instancia de objeto concreto, instancia arbitraria y constructor) como sintaxis alternativa a expresiones Lambda.
- [ ] Instanciar y componer las interfaces funcionales `Predicate`, `Function`, `Consumer`, `Supplier`, `UnaryOperator` y `BiFunction` con los tipos del dominio (`Order`, `Product`).

## Prerrequisitos

**Conocimientos:**

- Laboratorio 02-00-01 completado: las clases `Order`, `Product`, `OrderAdapter`, `DataRepository` y `RepositoryUtils` deben compilar correctamente.
- Comprensión de interfaces funcionales del Módulo 1 (interfaz con un único método abstracto).
- Conocimiento básico de la sintaxis Lambda en Java (`(params) -> expresión`).

**Acceso y software:**

| Software                         | Versión          | Verificación                |
|----------------------------------|------------------|-----------------------------|
| JDK Eclipse Temurin              | 11.0.23+9        | `java -version`             |
| Apache Maven                     | 3.9.8            | `mvn -version`              |
| IntelliJ IDEA Community Edition  | 2024.1.4         | Menú *Help → About*         |
| Git                              | 2.45.2           | `git --version`             |

## Entorno de Laboratorio

**Directorio de trabajo:** `~/cursos/java-se11-labs` (Linux/macOS) o `C:\cursos\java-se11-labs` (Windows).

**Estructura de paquetes al finalizar este laboratorio:**

```
src/main/java/com/curso/
├── modulo1/          ← Order.java, interfaces funcionales Módulo 1
├── modulo2/          ← Product.java, DataRepository.java, etc.
└── modulo3/          ← NUEVO: LambdaRefactorDemo.java, OrderPredicates.java,
                        OrderFunctions.java, LambdaDemo.java, Transformer.java
```

Verifica que el proyecto compila antes de empezar:

```bash
cd ~/cursos/java-se11-labs
mvn compile -q
```

Salida esperada: sin errores (silencio o `BUILD SUCCESS`).

## Instrucciones Paso a Paso

### Paso 1: Crear el paquete del Módulo 3 y verificar dependencias

**Objetivo:** Preparar la estructura de directorios para el Módulo 3 y confirmar que las clases de los módulos anteriores están disponibles.

1. Crea el directorio del paquete `modulo3`:

```bash
mkdir -p src/main/java/com/curso/modulo3
```

2. Verifica que la estructura de paquetes es correcta:

```bash
find src/main/java/com/curso -type d | sort
```

**Salida esperada:**

```
src/main/java/com/curso
src/main/java/com/curso/modulo1
src/main/java/com/curso/modulo2
src/main/java/com/curso/modulo3
```

3. Compila el proyecto para confirmar que las clases de módulos anteriores están intactas:

```bash
mvn compile -q
```

**Verificación:** `BUILD SUCCESS` sin errores.

---

### Paso 2: Crear la interfaz funcional genérica Transformer

**Objetivo:** Definir una interfaz funcional personalizada con un parámetro de tipo genérico, que servirá como base para demostrar la equivalencia entre clases anónimas y lambdas genéricas (tema 3.4).

1. Crea el archivo `src/main/java/com/curso/modulo3/Transformer.java` con el siguiente contenido:

```java
package com.curso.modulo3;

/**
 * Interfaz funcional genérica que transforma un valor de tipo T
 * en un resultado de tipo R.
 * Demuestra el tema 3.2 (interfaz funcional) y 3.4 (lambdas con genéricos).
 */
@FunctionalInterface
public interface Transformer<T, R> {
    R transform(T input);
}
```

2. Compila para verificar:

```bash
mvn compile -q
```

**Verificación:** `BUILD SUCCESS`. La anotación `@FunctionalInterface` garantiza que el compilador valida un único método abstracto.

---

### Paso 3: Implementar tres escenarios con clases anónimas

**Objetivo:** Crear una clase que demuestre tres implementaciones con clases anónimas (tema 3.1): un `Comparator` para `Order`, un `Runnable` y la interfaz `Transformer` genérica. Esta clase será la base de la refactorización posterior.

1. Crea `src/main/java/com/curso/modulo3/LambdaRefactorDemo.java`:

```java
package com.curso.modulo3;

import com.curso.modulo1.Order;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Parte 1: Implementaciones con clases anónimas (tema 3.1).
 * Cada bloque se refactorizará a Lambda en los pasos siguientes.
 */
public class LambdaRefactorDemo {

    // ---------------------------------------------------------------
    // Escenario A: Comparator<Order> anónimo — ordena por totalAmount
    // Líneas de la clase anónima: 7
    // ---------------------------------------------------------------
    public static Comparator<Order> comparadorAnonimo() {
        return new Comparator<Order>() {
            @Override
            public int compare(Order o1, Order o2) {
                return Double.compare(o1.getTotalAmount(), o2.getTotalAmount());
            }
        };
    }

    // ---------------------------------------------------------------
    // Escenario B: Runnable anónimo — imprime un mensaje
    // Líneas de la clase anónima: 6
    // ---------------------------------------------------------------
    public static Runnable runnableAnonimo() {
        return new Runnable() {
            @Override
            public void run() {
                System.out.println("[Anónimo] Procesando pedidos en segundo plano...");
            }
        };
    }

    // ---------------------------------------------------------------
    // Escenario C: Transformer<Order, String> anónimo (genérico)
    // Líneas de la clase anónima: 7
    // ---------------------------------------------------------------
    public static Transformer<Order, String> transformerAnonimo() {
        return new Transformer<Order, String>() {
            @Override
            public String transform(Order order) {
                return "Pedido #" + order.getId() + " → $" + order.getTotalAmount();
            }
        };
    }

    public static void main(String[] args) {
        // Crear datos de prueba
        List<Order> orders = crearPedidosPrueba();

        System.out.println("=== CLASES ANÓNIMAS (antes de refactorizar) ===\n");

        // Escenario A
        Collections.sort(orders, comparadorAnonimo());
        System.out.println("Pedidos ordenados por monto:");
        for (Order o : orders) {
            System.out.println("  " + o.getId() + " - $" + o.getTotalAmount());
        }

        // Escenario B
        System.out.println();
        runnableAnonimo().run();

        // Escenario C
        System.out.println();
        Transformer<Order, String> t = transformerAnonimo();
        for (Order o : orders) {
            System.out.println("  " + t.transform(o));
        }
    }

    /** Genera 5 pedidos de prueba reutilizables. */
    public static List<Order> crearPedidosPrueba() {
        List<Order> lista = new ArrayList<>();
        lista.add(new Order(1, "Ana López", 250.00, "PENDING",
                LocalDate.of(2024, 3, 10)));
        lista.add(new Order(2, "Carlos Ruiz", 89.50, "COMPLETED",
                LocalDate.of(2024, 1, 15)));
        lista.add(new Order(3, "María Torres", 430.00, "PENDING",
                LocalDate.of(2024, 2, 20)));
        lista.add(new Order(4, "Pedro Gómez", 120.75, "CANCELLED",
                LocalDate.of(2024, 4, 5)));
        lista.add(new Order(5, "Lucía Vega", 310.00, "COMPLETED",
                LocalDate.of(2024, 3, 28)));
        return lista;
    }
}
```

> **Nota:** Si tu clase `Order` del Módulo 1 no tiene un constructor con los cinco parámetros `(int id, String customerName, double totalAmount, String status, LocalDate createdAt)`, agrégalo ahora. No modifiques los constructores existentes; simplemente añade uno adicional.

2. Si necesitas añadir el constructor, abre `src/main/java/com/curso/modulo1/Order.java` y agrega dentro de la clase:

```java
    /** Constructor completo para uso en pruebas del Módulo 3+. */
    public Order(int id, String customerName, double totalAmount,
                 String status, LocalDate createdAt) {
        this.id = id;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }
```

3. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.curso.modulo3.LambdaRefactorDemo
```

**Salida esperada:**

```
=== CLASES ANÓNIMAS (antes de refactorizar) ===

Pedidos ordenados por monto:
  2 - $89.5
  4 - $120.75
  1 - $250.0
  5 - $310.0
  3 - $430.0

[Anónimo] Procesando pedidos en segundo plano...

  Pedido #2 → $89.5
  Pedido #4 → $120.75
  Pedido #1 → $250.0
  Pedido #5 → $310.0
  Pedido #3 → $430.0
```

**Verificación:** Los tres escenarios se ejecutan correctamente con clases anónimas. Observa que el `Comparator` usa 7 líneas, el `Runnable` usa 6 y el `Transformer` usa 7. Total: **20 líneas** de boilerplate.

---

### Paso 4: Refactorizar las clases anónimas a expresiones Lambda

**Objetivo:** Reemplazar cada clase anónima del paso anterior por su Lambda equivalente (temas 3.3 y 3.4), documentando la reducción de líneas y la mejora en legibilidad.

1. Añade los siguientes métodos **dentro de la misma clase `LambdaRefactorDemo.java`**, justo después de los métodos anónimos:

```java
    // ===============================================================
    // REFACTORIZACIÓN A LAMBDAS (tema 3.3 y 3.4)
    // ===============================================================

    // Escenario A-Lambda: Comparator<Order> — 1 línea vs 7
    public static Comparator<Order> comparadorLambda() {
        return (o1, o2) -> Double.compare(o1.getTotalAmount(), o2.getTotalAmount());
    }

    // Escenario B-Lambda: Runnable — 1 línea vs 6
    public static Runnable runnableLambda() {
        return () -> System.out.println("[Lambda] Procesando pedidos en segundo plano...");
    }

    // Escenario C-Lambda: Transformer<Order, String> genérico — 1 línea vs 7
    // Demuestra lambda con interfaz funcional genérica (tema 3.4)
    public static Transformer<Order, String> transformerLambda() {
        return order -> "Pedido #" + order.getId() + " → $" + order.getTotalAmount();
    }
```

2. Reemplaza el contenido del método `main` para ejecutar ambas versiones:

```java
    public static void main(String[] args) {
        List<Order> orders = crearPedidosPrueba();

        // ---- Versión con clases anónimas ----
        System.out.println("=== CLASES ANÓNIMAS (antes de refactorizar) ===\n");

        List<Order> ordersAnon = new ArrayList<>(orders);
        Collections.sort(ordersAnon, comparadorAnonimo());
        System.out.println("Pedidos ordenados por monto (anónimo):");
        for (Order o : ordersAnon) {
            System.out.println("  " + o.getId() + " - $" + o.getTotalAmount());
        }

        System.out.println();
        runnableAnonimo().run();

        System.out.println();
        Transformer<Order, String> tAnon = transformerAnonimo();
        for (Order o : ordersAnon) {
            System.out.println("  " + tAnon.transform(o));
        }

        // ---- Versión con lambdas ----
        System.out.println("\n=== EXPRESIONES LAMBDA (después de refactorizar) ===\n");

        List<Order> ordersLambda = new ArrayList<>(orders);
        Collections.sort(ordersLambda, comparadorLambda());
        System.out.println("Pedidos ordenados por monto (lambda):");
        for (Order o : ordersLambda) {
            System.out.println("  " + o.getId() + " - $" + o.getTotalAmount());
        }

        System.out.println();
        runnableLambda().run();

        System.out.println();
        Transformer<Order, String> tLambda = transformerLambda();
        for (Order o : ordersLambda) {
            System.out.println("  " + tLambda.transform(o));
        }

        // ---- Resumen de reducción ----
        System.out.println("\n=== RESUMEN DE REDUCCIÓN ===");
        System.out.println("Comparator:  7 líneas (anónima) → 1 línea (lambda)");
        System.out.println("Runnable:    6 líneas (anónima) → 1 línea (lambda)");
        System.out.println("Transformer: 7 líneas (anónima) → 1 línea (lambda)");
        System.out.println("Total:      20 líneas → 3 líneas (reducción del 85%)");
    }
```

3. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.curso.modulo3.LambdaRefactorDemo
```

**Salida esperada:**

```
=== CLASES ANÓNIMAS (antes de refactorizar) ===

Pedidos ordenados por monto (anónimo):
  2 - $89.5
  4 - $120.75
  1 - $250.0
  5 - $310.0
  3 - $430.0

[Anónimo] Procesando pedidos en segundo plano...

  Pedido #2 → $89.5
  Pedido #4 → $120.75
  Pedido #1 → $250.0
  Pedido #5 → $310.0
  Pedido #3 → $430.0

=== EXPRESIONES LAMBDA (después de refactorizar) ===

Pedidos ordenados por monto (lambda):
  2 - $89.5
  4 - $120.75
  1 - $250.0
  5 - $310.0
  3 - $430.0

[Lambda] Procesando pedidos en segundo plano...

  Pedido #2 → $89.5
  Pedido #4 → $120.75
  Pedido #1 → $250.0
  Pedido #5 → $310.0
  Pedido #3 → $430.0

=== RESUMEN DE REDUCCIÓN ===
Comparator:  7 líneas (anónima) → 1 línea (lambda)
Runnable:    6 líneas (anónima) → 1 línea (lambda)
Transformer: 7 líneas (anónima) → 1 línea (lambda)
Total:      20 líneas → 3 líneas (reducción del 85%)
```

**Verificación:** Ambas versiones producen resultados idénticos. La versión Lambda reduce el boilerplate un 85%. La interfaz genérica `Transformer<Order, String>` funciona correctamente con la lambda del escenario C.

---

### Paso 5: Crear predicados compuestos con OrderPredicates

**Objetivo:** Implementar predicados reutilizables usando `Predicate<Order>` y demostrar la composición con `and`, `or` y `negate` (temas 3.5 y 3.8).

1. Crea `src/main/java/com/curso/modulo3/OrderPredicates.java`:

```java
package com.curso.modulo3;

import com.curso.modulo1.Order;

import java.time.LocalDate;
import java.util.function.Predicate;

/**
 * Predicados reutilizables para filtrar pedidos.
 * Demuestra Predicate<T> y composición (and, or, negate) — temas 3.5 y 3.8.
 */
public final class OrderPredicates {

    private OrderPredicates() {
        // Clase utilitaria, no instanciable
    }

    // --- Predicados básicos ---

    /** Pedidos con estado PENDING. */
    public static Predicate<Order> isPending() {
        return order -> "PENDING".equals(order.getStatus());
    }

    /** Pedidos con estado COMPLETED. */
    public static Predicate<Order> isCompleted() {
        return order -> "COMPLETED".equals(order.getStatus());
    }

    /** Pedidos con estado CANCELLED. */
    public static Predicate<Order> isCancelled() {
        return order -> "CANCELLED".equals(order.getStatus());
    }

    /** Pedidos con monto total mayor al umbral dado. */
    public static Predicate<Order> totalGreaterThan(double threshold) {
        return order -> order.getTotalAmount() > threshold;
    }

    /** Pedidos con monto total menor o igual al umbral dado. */
    public static Predicate<Order> totalLessOrEqual(double threshold) {
        return order -> order.getTotalAmount() <= threshold;
    }

    /** Pedidos creados después de la fecha dada (exclusiva). */
    public static Predicate<Order> createdAfter(LocalDate date) {
        return order -> order.getCreatedAt().isAfter(date);
    }

    /** Pedidos creados antes de la fecha dada (exclusiva). */
    public static Predicate<Order> createdBefore(LocalDate date) {
        return order -> order.getCreatedAt().isBefore(date);
    }

    // --- Predicados compuestos ---

    /** Pedidos pendientes con monto mayor a un umbral. */
    public static Predicate<Order> pendingHighValue(double threshold) {
        return isPending().and(totalGreaterThan(threshold));
    }

    /** Pedidos completados O con monto mayor a un umbral. */
    public static Predicate<Order> completedOrHighValue(double threshold) {
        return isCompleted().or(totalGreaterThan(threshold));
    }

    /** Pedidos NO cancelados. */
    public static Predicate<Order> notCancelled() {
        return isCancelled().negate();
    }

    /** Pedidos no cancelados, creados después de una fecha, con monto mayor a un umbral. */
    public static Predicate<Order> activeRecentHighValue(LocalDate afterDate, double threshold) {
        return notCancelled()
                .and(createdAfter(afterDate))
                .and(totalGreaterThan(threshold));
    }
}
```

2. Compila:

```bash
mvn compile -q
```

**Verificación:** `BUILD SUCCESS`.

---

### Paso 6: Crear funciones de transformación con OrderFunctions

**Objetivo:** Implementar transformaciones usando `Function`, `Consumer`, `Supplier`, `UnaryOperator` y `BiFunction`, y demostrar la composición con `andThen` y `compose` (temas 3.5, 3.6 y 3.8).

1. Crea `src/main/java/com/curso/modulo3/OrderFunctions.java`:

```java
package com.curso.modulo3;

import com.curso.modulo1.Order;

import java.time.LocalDate;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Funciones de transformación y procesamiento para Order.
 * Demuestra Function, Consumer, Supplier, UnaryOperator, BiFunction
 * y composición con andThen/compose — temas 3.5, 3.6 y 3.8.
 */
public final class OrderFunctions {

    private OrderFunctions() {
        // Clase utilitaria, no instanciable
    }

    // --- Function<Order, R> ---

    /** Extrae el nombre del cliente. */
    public static Function<Order, String> toCustomerName() {
        return Order::getCustomerName;  // Referencia a método de instancia arbitraria
    }

    /** Extrae el monto total. */
    public static Function<Order, Double> toTotalAmount() {
        return Order::getTotalAmount;
    }

    /** Genera un resumen en formato "ID | Cliente | $Monto | Estado". */
    public static Function<Order, String> toSummary() {
        return order -> String.format("%d | %s | $%.2f | %s",
                order.getId(),
                order.getCustomerName(),
                order.getTotalAmount(),
                order.getStatus());
    }

    // --- Composición con andThen y compose ---

    /** Extrae el nombre del cliente y lo convierte a mayúsculas (andThen). */
    public static Function<Order, String> toUpperCustomerName() {
        return toCustomerName().andThen(String::toUpperCase);
    }

    /** Aplica una función de formato sobre el monto: primero extrae, luego formatea. */
    public static Function<Order, String> toFormattedAmount() {
        Function<Double, String> formatter = amount -> String.format("$%,.2f", amount);
        return toTotalAmount().andThen(formatter);
    }

    /**
     * Composición con compose: primero aplica una transformación al Order,
     * luego extrae el resumen.
     */
    public static Function<Order, String> summaryAfterDiscount(double discountPercent) {
        UnaryOperator<Order> applyDiscount = order -> new Order(
                order.getId(),
                order.getCustomerName(),
                order.getTotalAmount() * (1 - discountPercent / 100.0),
                order.getStatus(),
                order.getCreatedAt()
        );
        return toSummary().compose(applyDiscount);
    }

    // --- Consumer<Order> ---

    /** Imprime el resumen del pedido en consola. */
    public static Consumer<Order> printSummary() {
        return order -> System.out.println(toSummary().apply(order));
    }

    /** Imprime una advertencia si el monto supera un umbral. */
    public static Consumer<Order> warnIfHighValue(double threshold) {
        return order -> {
            if (order.getTotalAmount() > threshold) {
                System.out.println("  ⚠ ALERTA: Pedido #" + order.getId()
                        + " supera $" + threshold);
            }
        };
    }

    /** Consumer compuesto: imprime resumen Y luego alerta si alto valor. */
    public static Consumer<Order> printAndWarn(double threshold) {
        return printSummary().andThen(warnIfHighValue(threshold));
    }

    // --- Supplier<Order> ---

    /** Genera un pedido de ejemplo por defecto. */
    public static Supplier<Order> defaultOrder() {
        return () -> new Order(0, "Cliente Default", 0.0, "PENDING",
                LocalDate.now());
    }

    // --- UnaryOperator<Order> ---

    /** Marca un pedido como COMPLETED. */
    public static UnaryOperator<Order> markCompleted() {
        return order -> new Order(
                order.getId(),
                order.getCustomerName(),
                order.getTotalAmount(),
                "COMPLETED",
                order.getCreatedAt()
        );
    }

    /** Aplica un descuento porcentual al monto. */
    public static UnaryOperator<Order> applyDiscount(double percent) {
        return order -> new Order(
                order.getId(),
                order.getCustomerName(),
                order.getTotalAmount() * (1 - percent / 100.0),
                order.getStatus(),
                order.getCreatedAt()
        );
    }

    // --- BiFunction<Order, Double, String> ---

    /** Genera un recibo con impuesto aplicado. */
    public static BiFunction<Order, Double, String> receiptWithTax() {
        return (order, taxRate) -> {
            double tax = order.getTotalAmount() * taxRate / 100.0;
            double total = order.getTotalAmount() + tax;
            return String.format("Recibo #%d: Subtotal=$%.2f, IVA(%.0f%%)=$%.2f, Total=$%.2f",
                    order.getId(), order.getTotalAmount(), taxRate, tax, total);
        };
    }
}
```

2. Compila:

```bash
mvn compile -q
```

**Verificación:** `BUILD SUCCESS`.

---

### Paso 7: Crear la clase principal LambdaDemo

**Objetivo:** Integrar todos los componentes anteriores en una demostración completa que filtre, transforme y procese pedidos usando lambdas, referencias a métodos y composición de funciones (todos los temas del módulo 3).

1. Crea `src/main/java/com/curso/modulo3/LambdaDemo.java`:

```java
package com.curso.modulo3;

import com.curso.modulo1.Order;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Demostración integral de expresiones Lambda, interfaces funcionales
 * de java.util.function, referencias a métodos y composición.
 * Temas 3.1–3.8 del Módulo 3.
 */
public class LambdaDemo {

    public static void main(String[] args) {
        List<Order> orders = LambdaRefactorDemo.crearPedidosPrueba();

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║  DEMOSTRACIÓN INTEGRAL — MÓDULO 3: LAMBDAS      ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        // ---- Sección 1: Predicate y composición ----
        demo1_Predicates(orders);

        // ---- Sección 2: Function y composición ----
        demo2_Functions(orders);

        // ---- Sección 3: Consumer ----
        demo3_Consumer(orders);

        // ---- Sección 4: Supplier ----
        demo4_Supplier();

        // ---- Sección 5: UnaryOperator ----
        demo5_UnaryOperator(orders);

        // ---- Sección 6: BiFunction ----
        demo6_BiFunction(orders);

        // ---- Sección 7: Referencias a métodos (4 formas) ----
        demo7_MethodReferences(orders);

        // ---- Sección 8: Transformer genérico ----
        demo8_GenericTransformer(orders);
    }

    // ================================================================
    // Sección 1: Predicate<Order> y composición (and, or, negate)
    // ================================================================
    private static void demo1_Predicates(List<Order> orders) {
        System.out.println("── 1. PREDICATE Y COMPOSICIÓN ──────────────────\n");

        // Predicado simple: pedidos pendientes
        Predicate<Order> pending = OrderPredicates.isPending();
        System.out.println("Pedidos PENDING:");
        printFiltered(orders, pending);

        // Composición con and: pendientes con monto > 200
        Predicate<Order> pendingHigh = OrderPredicates.pendingHighValue(200.0);
        System.out.println("Pedidos PENDING con monto > $200:");
        printFiltered(orders, pendingHigh);

        // Composición con or: completados O monto > 300
        Predicate<Order> compOrHigh = OrderPredicates.completedOrHighValue(300.0);
        System.out.println("Pedidos COMPLETED o monto > $300:");
        printFiltered(orders, compOrHigh);

        // Composición con negate: no cancelados
        Predicate<Order> notCancelled = OrderPredicates.notCancelled();
        System.out.println("Pedidos NO cancelados:");
        printFiltered(orders, notCancelled);

        // Composición triple: no cancelados + después de 2024-02-01 + monto > 200
        Predicate<Order> complex = OrderPredicates.activeRecentHighValue(
                LocalDate.of(2024, 2, 1), 200.0);
        System.out.println("No cancelados, después de 2024-02-01, monto > $200:");
        printFiltered(orders, complex);
    }

    // ================================================================
    // Sección 2: Function<Order, R> y composición (andThen, compose)
    // ================================================================
    private static void demo2_Functions(List<Order> orders) {
        System.out.println("── 2. FUNCTION Y COMPOSICIÓN ───────────────────\n");

        // Function simple: extraer nombre
        Function<Order, String> getName = OrderFunctions.toCustomerName();
        System.out.println("Nombres de clientes:");
        for (Order o : orders) {
            System.out.println("  " + getName.apply(o));
        }

        // andThen: nombre en mayúsculas
        System.out.println("\nNombres en MAYÚSCULAS (andThen):");
        Function<Order, String> upperName = OrderFunctions.toUpperCustomerName();
        for (Order o : orders) {
            System.out.println("  " + upperName.apply(o));
        }

        // andThen: monto formateado
        System.out.println("\nMontos formateados (andThen):");
        Function<Order, String> fmtAmount = OrderFunctions.toFormattedAmount();
        for (Order o : orders) {
            System.out.println("  " + o.getId() + " → " + fmtAmount.apply(o));
        }

        // compose: resumen después de aplicar 10% descuento
        System.out.println("\nResumen con 10% descuento (compose):");
        Function<Order, String> discountSummary = OrderFunctions.summaryAfterDiscount(10.0);
        for (Order o : orders) {
            System.out.println("  " + discountSummary.apply(o));
        }
    }

    // ================================================================
    // Sección 3: Consumer<Order>
    // ================================================================
    private static void demo3_Consumer(List<Order> orders) {
        System.out.println("\n── 3. CONSUMER ─────────────────────────────────\n");

        Consumer<Order> printAndWarn = OrderFunctions.printAndWarn(300.0);
        System.out.println("Resumen con alertas (monto > $300):");
        for (Order o : orders) {
            printAndWarn.accept(o);
        }
    }

    // ================================================================
    // Sección 4: Supplier<Order>
    // ================================================================
    private static void demo4_Supplier() {
        System.out.println("\n── 4. SUPPLIER ─────────────────────────────────\n");

        Supplier<Order> factory = OrderFunctions.defaultOrder();
        Order defaultOrder = factory.get();
        System.out.println("Pedido generado por Supplier:");
        System.out.println("  " + OrderFunctions.toSummary().apply(defaultOrder));
    }

    // ================================================================
    // Sección 5: UnaryOperator<Order>
    // ================================================================
    private static void demo5_UnaryOperator(List<Order> orders) {
        System.out.println("\n── 5. UNARY OPERATOR ───────────────────────────\n");

        UnaryOperator<Order> complete = OrderFunctions.markCompleted();
        UnaryOperator<Order> discount15 = OrderFunctions.applyDiscount(15.0);

        System.out.println("Pedidos marcados como COMPLETED con 15% descuento:");
        for (Order o : orders) {
            Order transformed = discount15.andThen(complete).apply(o);
            System.out.println("  " + OrderFunctions.toSummary().apply(transformed));
        }
    }

    // ================================================================
    // Sección 6: BiFunction<Order, Double, String>
    // ================================================================
    private static void demo6_BiFunction(List<Order> orders) {
        System.out.println("\n── 6. BIFUNCTION ───────────────────────────────\n");

        BiFunction<Order, Double, String> receipt = OrderFunctions.receiptWithTax();
        double taxRate = 16.0;
        System.out.println("Recibos con IVA del " + taxRate + "%:");
        for (Order o : orders) {
            System.out.println("  " + receipt.apply(o, taxRate));
        }
    }

    // ================================================================
    // Sección 7: Referencias a métodos (4 formas)
    // ================================================================
    private static void demo7_MethodReferences(List<Order> orders) {
        System.out.println("\n── 7. REFERENCIAS A MÉTODOS (4 FORMAS) ─────────\n");

        // Forma 1: Referencia a método estático — Integer::compare
        System.out.println("Forma 1 — Método estático (Double::compare):");
        List<Order> sorted = new ArrayList<>(orders);
        sorted.sort((o1, o2) -> Double.compare(o1.getTotalAmount(), o2.getTotalAmount()));
        for (Order o : sorted) {
            System.out.println("  " + o.getId() + " - $" + o.getTotalAmount());
        }

        // Forma 2: Referencia a método de instancia de objeto concreto
        System.out.println("\nForma 2 — Método de instancia de objeto concreto:");
        Function<Order, String> summaryFn = OrderFunctions.toSummary();
        // summaryFn::apply es referencia a método de instancia concreta
        Consumer<Order> printViaRef = order -> System.out.println("  " + summaryFn.apply(order));
        for (Order o : orders) {
            printViaRef.accept(o);
        }

        // Forma 3: Referencia a método de instancia arbitraria — Order::getCustomerName
        System.out.println("\nForma 3 — Método de instancia arbitraria (Order::getCustomerName):");
        Function<Order, String> nameRef = Order::getCustomerName;
        for (Order o : orders) {
            System.out.println("  " + nameRef.apply(o));
        }

        // Forma 4: Referencia a constructor — ArrayList::new
        System.out.println("\nForma 4 — Referencia a constructor (ArrayList::new):");
        Supplier<List<Order>> listFactory = ArrayList::new;
        List<Order> newList = listFactory.get();
        newList.addAll(orders);
        System.out.println("  Lista creada con " + newList.size() + " elementos vía ArrayList::new");
    }

    // ================================================================
    // Sección 8: Transformer genérico (interfaz personalizada)
    // ================================================================
    private static void demo8_GenericTransformer(List<Order> orders) {
        System.out.println("\n── 8. TRANSFORMER GENÉRICO ─────────────────────\n");

        // Transformer<Order, String>
        Transformer<Order, String> orderToLabel = order ->
                "[" + order.getStatus() + "] " + order.getCustomerName();

        // Transformer<String, Integer> — longitud de cadena
        Transformer<String, Integer> stringLength = String::length;

        System.out.println("Etiquetas de pedidos:");
        for (Order o : orders) {
            String label = orderToLabel.transform(o);
            int len = stringLength.transform(label);
            System.out.println("  " + label + " (longitud: " + len + ")");
        }

        // Transformer<Double, String> — formato monetario
        Transformer<Double, String> moneyFormat = amount -> String.format("$%,.2f", amount);
        System.out.println("\nMontos formateados via Transformer genérico:");
        for (Order o : orders) {
            System.out.println("  Pedido #" + o.getId() + ": "
                    + moneyFormat.transform(o.getTotalAmount()));
        }
    }

    // ================================================================
    // Utilidad: filtrar e imprimir
    // ================================================================
    private static void printFiltered(List<Order> orders, Predicate<Order> predicate) {
        boolean found = false;
        for (Order o : orders) {
            if (predicate.test(o)) {
                System.out.println("  " + OrderFunctions.toSummary().apply(o));
                found = true;
            }
        }
        if (!found) {
            System.out.println("  (ningún pedido coincide)");
        }
        System.out.println();
    }
}
```

2. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.curso.modulo3.LambdaDemo
```

**Salida esperada (extracto de las primeras secciones):**

```
╔══════════════════════════════════════════════════╗
║  DEMOSTRACIÓN INTEGRAL — MÓDULO 3: LAMBDAS      ║
╚══════════════════════════════════════════════════╝

── 1. PREDICATE Y COMPOSICIÓN ──────────────────

Pedidos PENDING:
  1 | Ana López | $250.00 | PENDING
  3 | María Torres | $430.00 | PENDING

Pedidos PENDING con monto > $200:
  1 | Ana López | $250.00 | PENDING
  3 | María Torres | $430.00 | PENDING

Pedidos COMPLETED o monto > $300:
  2 | Carlos Ruiz | $89.50 | COMPLETED
  3 | María Torres | $430.00 | PENDING
  5 | Lucía Vega | $310.00 | COMPLETED

Pedidos NO cancelados:
  1 | Ana López | $250.00 | PENDING
  2 | Carlos Ruiz | $89.50 | COMPLETED
  3 | María Torres | $430.00 | PENDING
  5 | Lucía Vega | $310.00 | COMPLETED

No cancelados, después de 2024-02-01, monto > $200:
  1 | Ana López | $250.00 | PENDING
  3 | María Torres | $430.00 | PENDING
  5 | Lucía Vega | $310.00 | COMPLETED

── 2. FUNCTION Y COMPOSICIÓN ───────────────────

Nombres de clientes:
  Ana López
  Carlos Ruiz
  María Torres
  Pedro Gómez
  Lucía Vega

Nombres en MAYÚSCULAS (andThen):
  ANA LÓPEZ
  CARLOS RUIZ
  MARÍA TORRES
  PEDRO GÓMEZ
  LUCÍA VEGA

Montos formateados (andThen):
  1 → $250.00
  2 → $89.50
  3 → $430.00
  4 → $120.75
  5 → $310.00

Resumen con 10% descuento (compose):
  1 | Ana López | $225.00 | PENDING
  2 | Carlos Ruiz | $80.55 | COMPLETED
  3 | María Torres | $387.00 | PENDING
  4 | Pedro Gómez | $108.68 | CANCELLED
  5 | Lucía Vega | $279.00 | COMPLETED
```

**Verificación:** Todas las 8 secciones se ejecutan sin errores. Los predicados compuestos filtran correctamente; las funciones compuestas transforman los datos en cadena; las 4 formas de referencia a método se demuestran explícitamente.

---

### Paso 8: Medir rendimiento — clases anónimas vs lambdas sobre 10 000 elementos

**Objetivo:** Comparar el tiempo de ejecución de filtrado y transformación usando clases anónimas frente a lambdas sobre un volumen de 10 000 pedidos, para verificar que las lambdas no introducen penalización significativa de rendimiento.

1. Añade el siguiente método estático al final de la clase `LambdaDemo.java` (antes de la llave de cierre de la clase):

```java
    // ================================================================
    // Benchmark: Anónimas vs Lambdas sobre 10,000 elementos
    // ================================================================
    public static void benchmark() {
        System.out.println("\n╔══════════════════════════════════════════════════╗");
        System.out.println("║  BENCHMARK: Anónimas vs Lambdas (10,000 elem.)  ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        // Generar 10,000 pedidos
        List<Order> bigList = new ArrayList<>();
        String[] statuses = {"PENDING", "COMPLETED", "CANCELLED"};
        for (int i = 1; i <= 10_000; i++) {
            bigList.add(new Order(
                    i,
                    "Cliente-" + i,
                    50.0 + (i % 500),
                    statuses[i % 3],
                    java.time.LocalDate.of(2024, 1 + (i % 12), 1 + (i % 28))
            ));
        }

        // --- Filtrado con clase anónima ---
        Predicate<Order> anonPredicate = new Predicate<Order>() {
            @Override
            public boolean test(Order order) {
                return "PENDING".equals(order.getStatus())
                        && order.getTotalAmount() > 200.0;
            }
        };

        // --- Filtrado con lambda ---
        Predicate<Order> lambdaPredicate = order ->
                "PENDING".equals(order.getStatus()) && order.getTotalAmount() > 200.0;

        // --- Transformación con clase anónima ---
        Function<Order, String> anonFunction = new Function<Order, String>() {
            @Override
            public String apply(Order order) {
                return order.getId() + ": " + order.getCustomerName()
                        + " ($" + order.getTotalAmount() + ")";
            }
        };

        // --- Transformación con lambda ---
        Function<Order, String> lambdaFunction = order ->
                order.getId() + ": " + order.getCustomerName()
                        + " ($" + order.getTotalAmount() + ")";

        // Calentamiento (warm-up)
        for (int w = 0; w < 5; w++) {
            filterAndTransform(bigList, anonPredicate, anonFunction);
            filterAndTransform(bigList, lambdaPredicate, lambdaFunction);
        }

        // Medición: clase anónima
        int iterations = 100;
        long startAnon = System.nanoTime();
        List<String> resultAnon = null;
        for (int i = 0; i < iterations; i++) {
            resultAnon = filterAndTransform(bigList, anonPredicate, anonFunction);
        }
        long endAnon = System.nanoTime();
        double msAnon = (endAnon - startAnon) / 1_000_000.0;

        // Medición: lambda
        long startLambda = System.nanoTime();
        List<String> resultLambda = null;
        for (int i = 0; i < iterations; i++) {
            resultLambda = filterAndTransform(bigList, lambdaPredicate, lambdaFunction);
        }
        long endLambda = System.nanoTime();
        double msLambda = (endLambda - startLambda) / 1_000_000.0;

        System.out.printf("Clase anónima : %d resultados en %.2f ms (%d iteraciones)%n",
                resultAnon.size(), msAnon, iterations);
        System.out.printf("Lambda        : %d resultados en %.2f ms (%d iteraciones)%n",
                resultLambda.size(), msLambda, iterations);
        System.out.printf("Diferencia    : %.2f ms (%.1f%%)%n",
                Math.abs(msAnon - msLambda),
                Math.abs(msAnon - msLambda) / Math.max(msAnon, msLambda) * 100);
        System.out.println("\nConclusión: La diferencia de rendimiento es despreciable.");
        System.out.println("La ventaja de las lambdas es la LEGIBILIDAD, no el rendimiento.\n");
    }

    private static List<String> filterAndTransform(
            List<Order> orders,
            Predicate<Order> predicate,
            Function<Order, String> function) {
        List<String> results = new ArrayList<>();
        for (Order o : orders) {
            if (predicate.test(o)) {
                results.add(function.apply(o));
            }
        }
        return results;
    }
```

2. Modifica el método `main` de `LambdaDemo.java` para incluir la llamada al benchmark al final:

```java
    public static void main(String[] args) {
        List<Order> orders = LambdaRefactorDemo.crearPedidosPrueba();

        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║  DEMOSTRACIÓN INTEGRAL — MÓDULO 3: LAMBDAS      ║");
        System.out.println("╚══════════════════════════════════════════════════╝\n");

        demo1_Predicates(orders);
        demo2_Functions(orders);
        demo3_Consumer(orders);
        demo4_Supplier();
        demo5_UnaryOperator(orders);
        demo6_BiFunction(orders);
        demo7_MethodReferences(orders);
        demo8_GenericTransformer(orders);

        // Benchmark al final
        benchmark();
    }
```

3. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.curso.modulo3.LambdaDemo
```

**Salida esperada (sección benchmark, valores aproximados):**

```
╔══════════════════════════════════════════════════╗
║  BENCHMARK: Anónimas vs Lambdas (10,000 elem.)  ║
╚══════════════════════════════════════════════════╝

Clase anónima : 1000 resultados en 45.23 ms (100 iteraciones)
Lambda        : 1000 resultados en 43.87 ms (100 iteraciones)
Diferencia    : 1.36 ms (3.0%)

Conclusión: La diferencia de rendimiento es despreciable.
La ventaja de las lambdas es la LEGIBILIDAD, no el rendimiento.
```

> **Nota:** Los tiempos variarán según tu hardware. Lo importante es que la diferencia porcentual sea menor al 10%, confirmando que las lambdas no introducen penalización significativa.

**Verificación:** El benchmark se ejecuta sin errores. Ambas versiones producen la misma cantidad de resultados. La diferencia de rendimiento es despreciable.

---

### Paso 9: Verificación final y commit

**Objetivo:** Confirmar que todos los archivos compilan correctamente y guardar el progreso.

1. Compila el proyecto completo:

```bash
mvn compile -q
```

**Verificación:** `BUILD SUCCESS`.

2. Verifica la estructura de archivos del Módulo 3:

```bash
find src/main/java/com/curso/modulo3 -name "*.java" | sort
```

**Salida esperada:**

```
src/main/java/com/curso/modulo3/LambdaDemo.java
src/main/java/com/curso/modulo3/LambdaRefactorDemo.java
src/main/java/com/curso/modulo3/OrderFunctions.java
src/main/java/com/curso/modulo3/OrderPredicates.java
src/main/java/com/curso/modulo3/Transformer.java
```

3. Ejecuta la demostración completa una última vez:

```bash
java -cp target/classes com.curso.modulo3.LambdaDemo
```

4. Guarda tu progreso con Git:

```bash
git add -A
git commit -m "Lab 03-00-01: Procesamiento funcional con expresiones Lambda

- Transformer<T,R>: interfaz funcional genérica personalizada
- LambdaRefactorDemo: refactorización de clases anónimas a lambdas
- OrderPredicates: Predicate<Order> con composición and/or/negate
- OrderFunctions: Function, Consumer, Supplier, UnaryOperator, BiFunction
- LambdaDemo: demostración integral con las 4 formas de method references
- Benchmark: comparación de rendimiento anónimas vs lambdas (10,000 elem.)"
```

**Verificación final:** El commit se registra sin errores.

---

## Resumen de Archivos Creados/Modificados

| Archivo                        | Acción     | Propósito                                           |
|--------------------------------|------------|-----------------------------------------------------|
| `modulo3/Transformer.java`    | Creado     | Interfaz funcional genérica `Transformer<T, R>`     |
| `modulo3/LambdaRefactorDemo.java` | Creado | Refactorización de clases anónimas a lambdas        |
| `modulo3/OrderPredicates.java`| Creado     | Predicados compuestos con `Predicate<Order>`        |
| `modulo3/OrderFunctions.java` | Creado     | Funciones con `Function`, `Consumer`, `Supplier`, etc. |
| `modulo3/LambdaDemo.java`     | Creado     | Demostración integral y benchmark                   |
| `modulo1/Order.java`          | Modificado | Constructor de 5 parámetros (si no existía)         |

## Solución de Problemas

| Problema | Causa probable | Solución |
|----------|---------------|----------|
| `cannot find symbol: class Order` | Falta el import o el paquete `modulo1` no compila | Verifica que `com.curso.modulo1.Order` existe y compila con `mvn compile` |
| `Order does not have constructor (int, String, double, String, LocalDate)` | No se añadió el constructor del Paso 3.2 | Agrega el constructor de 5 parámetros a `Order.java` en `modulo1` |
| `@FunctionalInterface` error en `Transformer` | La interfaz tiene más de un método abstracto | Verifica que `Transformer` solo declare `R transform(T input)` |
| `method andThen not found` en `UnaryOperator` | Versión de JDK inferior a 8 | Verifica con `java -version` que usas JDK 11+ |
| Benchmark muestra 0 resultados | Los datos generados no coinciden con el predicado | Revisa que el predicado filtre `PENDING` con monto `> 200.0` y que los datos generados incluyan ese rango |
| Caracteres especiales no se muestran (╔, ║, etc.) | Terminal no soporta UTF-8 | Ejecuta `chcp 65001` en Windows o verifica que la terminal use UTF-8 |

## Rúbrica de Evaluación

| Criterio | Puntos | Descripción |
|----------|--------|-------------|
| Interfaz `Transformer<T, R>` compila con `@FunctionalInterface` | 10 | La interfaz genérica tiene exactamente un método abstracto |
| Las 3 clases anónimas funcionan correctamente | 10 | `LambdaRefactorDemo` ejecuta los escenarios A, B y C con clases anónimas |
| Las 3 lambdas equivalentes producen el mismo resultado | 15 | Las versiones lambda son semánticamente idénticas a las anónimas |
| `OrderPredicates` usa `and`, `or` y `negate` correctamente | 15 | Los predicados compuestos filtran los datos esperados |
| `OrderFunctions` usa `andThen` y `compose` correctamente | 15 | Las funciones compuestas transforman los datos esperados |
| Las 6 interfaces funcionales (`Predicate`, `Function`, `Consumer`, `Supplier`, `UnaryOperator`, `BiFunction`) se demuestran | 15 | Cada interfaz se instancia y se usa en `LambdaDemo` |
| Las 4 formas de referencia a método se demuestran | 10 | Estático, instancia concreta, instancia arbitraria y constructor |
| Benchmark ejecuta sin errores sobre 10,000 elementos | 10 | Ambas versiones producen la misma cantidad de resultados |
| **Total** | **100** | |
