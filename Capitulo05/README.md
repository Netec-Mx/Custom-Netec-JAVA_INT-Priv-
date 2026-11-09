# Práctica: Ejecución concurrente de tareas con ExecutorService y CompletableFuture

## Metadatos

| Campo | Valor |
|---|---|
| **Duración** | 125 minutos |
| **Complejidad** | Alta |
| **Nivel Bloom** | Crear |
| **Módulo** | 5 — Concurrencia |
| **Paquete** | `com.curso.modulo5` |

## Descripción General

En este laboratorio construirás un sistema de procesamiento concurrente de pedidos que evoluciona progresivamente a través de seis fases: desde hilos manuales con `Thread` y `Runnable` hasta cadenas asíncronas con `CompletableFuture`. Trabajarás sobre el dataset de 50 pedidos generado por `DataSetFactory` del Módulo 4, dividiendo la carga en lotes, sincronizando etapas con `CyclicBarrier`, y resolviendo una condición de carrera real con `AtomicInteger`. Al finalizar, la clase `ConcurrencyDemo` orquestará todas las fases y comparará métricas de tiempo secuencial vs concurrente.

## Objetivos de Aprendizaje

- [ ] Crear tareas con `Thread`/`Runnable` y migrarlas a un pool administrado con `ExecutorService`, implementando el patrón de cierre ordenado `shutdown()` + `awaitTermination()`.
- [ ] Obtener resultados de tareas concurrentes mediante `Callable<V>` y `Future<V>`, manejando `ExecutionException` e `InterruptedException`.
- [ ] Sincronizar etapas de un pipeline con `CyclicBarrier` y encadenar procesos asíncronos con `CompletableFuture` usando `supplyAsync`, `thenApplyAsync`, `thenAcceptAsync` y `exceptionally`.
- [ ] Reproducir una condición de carrera sobre un contador compartido y resolverla con `AtomicInteger`, verificando corrección bajo 8 hilos concurrentes.
- [ ] Comparar el overhead de `Thread` directo vs `ExecutorService` midiendo tiempos de ejecución secuencial y concurrente.

## Prerrequisitos

**Conocimientos:**
- Laboratorio 04-00-01 completado: `DataSetFactory`, `OrderStreamPipelines` y clases del dominio (`Order`, `Product`, `OrderLine`) disponibles y compilando.
- Comprensión de expresiones lambda y referencias a métodos (Módulo 3).
- Familiaridad con Stream API (Módulo 4) para filtrados y reducciones.

**Acceso y software:**

| Software | Versión exacta | Fuente |
|---|---|---|
| JDK Eclipse Temurin | 11.0.23+9 (x64) | https://adoptium.net/temurin/releases/?version=11 |
| Apache Maven | 3.9.8 | https://maven.apache.org/download.cgi |
| IntelliJ IDEA Community | 2024.1.4 | https://www.jetbrains.com/idea/download/ |
| Git | 2.45.2 | https://git-scm.com/downloads |

## Entorno de Laboratorio

**Estructura de directorios al finalizar este laboratorio:**

```
src/main/java/com/curso/
├── modulo1/          ← Order.java (con OrderLine support)
├── modulo2/          ← Product.java, DataRepository.java
├── modulo3/          ← LambdaDemo.java, etc.
├── modulo4/          ← DataSetFactory.java, OrderStreamPipelines.java, etc.
└── modulo5/          ← NUEVO
    ├── OrderBatchRunnable.java
    ├── OrderBatchCallable.java
    ├── PipelineStage.java
    ├── RaceConditionDemo.java
    └── ConcurrencyDemo.java
```

**Procesador recomendado:** Mínimo 4 núcleos físicos para observar paralelismo real. Los ejercicios de `CyclicBarrier` y condición de carrera producen resultados más evidentes con 4+ núcleos.

## Instrucciones Paso a Paso

### Paso 1: Verificar el entorno y crear el paquete del Módulo 5

**Objetivo:** Confirmar que el proyecto compila correctamente con los módulos 1–4 y crear la estructura del Módulo 5.

1. Abre una terminal y navega al directorio del proyecto:

```bash
cd ~/cursos/java-se11-labs
```

2. Verifica las versiones del JDK y Maven:

```bash
java -version
```

```
openjdk version "11.0.23" 2024-04-16
```

```bash
mvn -version
```

3. Compila el proyecto completo para verificar integridad:

```bash
mvn compile -q
```

4. Crea el paquete del Módulo 5:

```bash
mkdir -p src/main/java/com/curso/modulo5
```

5. Verifica la estructura:

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
src/main/java/com/curso/modulo5
```

**Verificación:** Los cinco paquetes existen y `mvn compile` reporta `BUILD SUCCESS`.

---

### Paso 2: Crear OrderBatchRunnable — tareas con Thread y Runnable (Fase 1)

**Objetivo:** Implementar una tarea `Runnable` que procese un sublote de pedidos, imprimiendo el nombre del hilo y los totales. Esto demuestra la creación manual de hilos y el no-determinismo en el orden de ejecución.

1. Crea el archivo `src/main/java/com/curso/modulo5/OrderBatchRunnable.java`:

```java
package com.curso.modulo5;

import com.curso.modulo1.Order;
import java.util.List;

/**
 * Tarea Runnable que procesa un lote de pedidos.
 * Imprime el nombre del hilo ejecutor y el total acumulado del lote.
 */
public class OrderBatchRunnable implements Runnable {

    private final String batchName;
    private final List<Order> orders;

    public OrderBatchRunnable(String batchName, List<Order> orders) {
        this.batchName = batchName;
        this.orders = orders;
    }

    @Override
    public void run() {
        String threadName = Thread.currentThread().getName();
        System.out.printf("[%s] Lote '%s' — procesando %d pedidos%n",
                threadName, batchName, orders.size());

        double total = 0.0;
        for (Order order : orders) {
            total += order.getTotalAmount();
            // Simular trabajo de procesamiento
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.printf("[%s] Lote '%s' interrumpido%n", threadName, batchName);
                return;
            }
        }

        System.out.printf("[%s] Lote '%s' — total: $%.2f (%d pedidos)%n",
                threadName, batchName, total, orders.size());
    }

    public String getBatchName() {
        return batchName;
    }

    public List<Order> getOrders() {
        return orders;
    }
}
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** La clase implementa `Runnable`, recibe un sublote de pedidos y usa `Thread.currentThread().getName()` para identificar el hilo ejecutor.

---

### Paso 3: Ejecutar tareas con Thread directo — observar no-determinismo

**Objetivo:** Dividir los 50 pedidos de `DataSetFactory` en tres lotes y ejecutarlos con `Thread` directo, observando que el orden de salida varía entre ejecuciones.

1. Crea `src/main/java/com/curso/modulo5/ConcurrencyDemo.java` con la Fase 1:

```java
package com.curso.modulo5;

import com.curso.modulo1.Order;
import com.curso.modulo4.DataSetFactory;

import java.util.List;

public class ConcurrencyDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("=== MÓDULO 5: CONCURRENCIA ===\n");

        List<Order> allOrders = DataSetFactory.createOrders();
        System.out.println("Total de pedidos generados: " + allOrders.size());

        // --- FASE 1: Thread + Runnable ---
        fase1_ThreadRunnable(allOrders);
    }

    /**
     * Fase 1: Divide los pedidos en 3 lotes y los ejecuta con Thread directo.
     */
    private static void fase1_ThreadRunnable(List<Order> allOrders)
            throws InterruptedException {
        System.out.println("\n--- FASE 1: Thread + Runnable ---");

        int size = allOrders.size();
        int third = size / 3;

        List<Order> lote1 = allOrders.subList(0, third);
        List<Order> lote2 = allOrders.subList(third, third * 2);
        List<Order> lote3 = allOrders.subList(third * 2, size);

        long start = System.currentTimeMillis();

        Thread t1 = new Thread(new OrderBatchRunnable("Lote-A", lote1));
        Thread t2 = new Thread(new OrderBatchRunnable("Lote-B", lote2));
        Thread t3 = new Thread(new OrderBatchRunnable("Lote-C", lote3));

        t1.start();
        t2.start();
        t3.start();

        // Esperamos a que los tres hilos terminen
        t1.join();
        t2.join();
        t3.join();

        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("Fase 1 completada en %d ms%n", elapsed);
    }
}
```

2. Compila y ejecuta:

```bash
mvn compile -q
```

```bash
mvn exec:java -Dexec.mainClass="com.curso.modulo5.ConcurrencyDemo" -q
```

**Salida esperada** (el orden de los lotes variará entre ejecuciones):

```
=== MÓDULO 5: CONCURRENCIA ===

Total de pedidos generados: 50

--- FASE 1: Thread + Runnable ---
[Thread-2] Lote 'Lote-C' — procesando 18 pedidos
[Thread-0] Lote 'Lote-A' — procesando 16 pedidos
[Thread-1] Lote 'Lote-B' — procesando 16 pedidos
[Thread-0] Lote 'Lote-A' — total: $XXXX.XX (16 pedidos)
[Thread-1] Lote 'Lote-B' — total: $XXXX.XX (16 pedidos)
[Thread-2] Lote 'Lote-C' — total: $XXXX.XX (18 pedidos)
Fase 1 completada en ~200 ms
```

3. Ejecuta **dos veces más** y observa que el orden de las líneas cambia:

```bash
mvn exec:java -Dexec.mainClass="com.curso.modulo5.ConcurrencyDemo" -q
```

**Verificación:** Los tres lotes se procesan en paralelo. El tiempo total es significativamente menor que 50 × 10 ms = 500 ms (ejecución secuencial). El orden de impresión varía, demostrando el no-determinismo de la planificación de hilos.

---

### Paso 4: Medir ejecución secuencial como línea base

**Objetivo:** Añadir una medición secuencial para comparar con la ejecución concurrente.

1. Añade el siguiente método a `ConcurrencyDemo`, **antes** de la llamada a `fase1_ThreadRunnable` en `main`:

```java
/**
 * Ejecución secuencial como línea base de comparación.
 */
private static void medirSecuencial(List<Order> allOrders) {
    System.out.println("\n--- LÍNEA BASE: Ejecución secuencial ---");
    long start = System.currentTimeMillis();

    Runnable tarea1 = new OrderBatchRunnable("Seq-A", allOrders);
    tarea1.run(); // Ejecuta en el hilo main, NO crea hilo nuevo

    long elapsed = System.currentTimeMillis() - start;
    System.out.printf("Secuencial completado en %d ms%n", elapsed);
}
```

2. Modifica `main` para invocar primero la medición secuencial:

```java
public static void main(String[] args) throws Exception {
    System.out.println("=== MÓDULO 5: CONCURRENCIA ===\n");

    List<Order> allOrders = DataSetFactory.createOrders();
    System.out.println("Total de pedidos generados: " + allOrders.size());

    // Línea base secuencial
    medirSecuencial(allOrders);

    // Fase 1: Thread + Runnable
    fase1_ThreadRunnable(allOrders);
}
```

3. Compila y ejecuta:

```bash
mvn compile -q && mvn exec:java -Dexec.mainClass="com.curso.modulo5.ConcurrencyDemo" -q
```

**Salida esperada:** La ejecución secuencial tarda ~500 ms (50 pedidos × 10 ms) mientras que la Fase 1 concurrente tarda ~180–200 ms.

**Verificación:** La diferencia de tiempos demuestra el beneficio de la concurrencia. Nota que `tarea1.run()` ejecuta en el hilo `main` (no crea un hilo nuevo), lo cual es un punto clave de la lección 5.2/5.3.

---

### Paso 5: Migrar a ExecutorService con FixedThreadPool (Fase 2)

**Objetivo:** Reemplazar la creación manual de hilos por un pool administrado de 4 hilos, implementando el patrón de cierre ordenado.

1. Añade el siguiente método a `ConcurrencyDemo`:

```java
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
```

```java
/**
 * Fase 2: Migra las tareas Runnable a un ExecutorService con pool fijo.
 * Implementa cierre ordenado: shutdown() + awaitTermination().
 */
private static void fase2_ExecutorService(List<Order> allOrders)
        throws InterruptedException {
    System.out.println("\n--- FASE 2: ExecutorService (FixedThreadPool) ---");

    int size = allOrders.size();
    int third = size / 3;

    List<Order> lote1 = allOrders.subList(0, third);
    List<Order> lote2 = allOrders.subList(third, third * 2);
    List<Order> lote3 = allOrders.subList(third * 2, size);

    ExecutorService pool = Executors.newFixedThreadPool(4);
    long start = System.currentTimeMillis();

    try {
        pool.submit(new OrderBatchRunnable("Pool-A", lote1));
        pool.submit(new OrderBatchRunnable("Pool-B", lote2));
        pool.submit(new OrderBatchRunnable("Pool-C", lote3));
    } finally {
        // Patrón de cierre ordenado
        pool.shutdown(); // No acepta nuevas tareas
        boolean terminated = pool.awaitTermination(30, TimeUnit.SECONDS);
        if (!terminated) {
            System.err.println("ADVERTENCIA: El pool no terminó en 30s, forzando cierre...");
            pool.shutdownNow();
        }
    }

    long elapsed = System.currentTimeMillis() - start;
    System.out.printf("Fase 2 completada en %d ms%n", elapsed);
}
```

2. Añade la invocación en `main`:

```java
// Fase 2: ExecutorService
fase2_ExecutorService(allOrders);
```

3. Compila y ejecuta:

```bash
mvn compile -q && mvn exec:java -Dexec.mainClass="com.curso.modulo5.ConcurrencyDemo" -q
```

**Salida esperada:**

```
--- FASE 2: ExecutorService (FixedThreadPool) ---
[pool-1-thread-1] Lote 'Pool-A' — procesando 16 pedidos
[pool-1-thread-2] Lote 'Pool-B' — procesando 16 pedidos
[pool-1-thread-3] Lote 'Pool-C' — procesando 18 pedidos
...
Fase 2 completada en ~200 ms
```

**Verificación:** Los nombres de los hilos ahora siguen el patrón `pool-N-thread-M` en lugar de `Thread-N`. El pool se cierra ordenadamente sin advertencias. El tiempo es similar a la Fase 1, pero el código es más limpio y reutilizable.

---

### Paso 6: Crear OrderBatchCallable — tareas que devuelven resultados (Fase 3)

**Objetivo:** Convertir la tarea `Runnable` a `Callable<List<Order>>` para poder recuperar los pedidos procesados como resultado.

1. Crea `src/main/java/com/curso/modulo5/OrderBatchCallable.java`:

```java
package com.curso.modulo5;

import com.curso.modulo1.Order;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Tarea Callable que procesa un lote de pedidos y devuelve
 * la lista de pedidos procesados exitosamente.
 */
public class OrderBatchCallable implements Callable<List<Order>> {

    private final String batchName;
    private final List<Order> orders;

    public OrderBatchCallable(String batchName, List<Order> orders) {
        this.batchName = batchName;
        this.orders = orders;
    }

    @Override
    public List<Order> call() throws Exception {
        String threadName = Thread.currentThread().getName();
        System.out.printf("[%s] Callable '%s' — procesando %d pedidos%n",
                threadName, batchName, orders.size());

        List<Order> processed = new ArrayList<>();

        for (Order order : orders) {
            // Simular validación/procesamiento
            Thread.sleep(10);
            processed.add(order);
        }

        double total = processed.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        System.out.printf("[%s] Callable '%s' — completado: $%.2f (%d pedidos)%n",
                threadName, batchName, total, processed.size());

        return processed;
    }
}
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** La clase implementa `Callable<List<Order>>` y su método `call()` declara `throws Exception`, a diferencia de `Runnable.run()` que no puede lanzar excepciones checked.

---

### Paso 7: Ejecutar Callable con invokeAll y Future.get() (Fase 3)

**Objetivo:** Enviar las tareas `Callable` al pool con `invokeAll()`, recuperar resultados con `Future.get()` y manejar `ExecutionException`.

1. Añade los imports necesarios a `ConcurrencyDemo`:

```java
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import java.util.ArrayList;
import java.util.Arrays;
```

2. Añade el método de la Fase 3:

```java
/**
 * Fase 3: Callable + Future — obtener resultados de tareas concurrentes.
 */
private static void fase3_CallableFuture(List<Order> allOrders)
        throws InterruptedException {
    System.out.println("\n--- FASE 3: Callable + Future (invokeAll) ---");

    int size = allOrders.size();
    int third = size / 3;

    List<Callable<List<Order>>> tasks = Arrays.asList(
            new OrderBatchCallable("Call-A", allOrders.subList(0, third)),
            new OrderBatchCallable("Call-B", allOrders.subList(third, third * 2)),
            new OrderBatchCallable("Call-C", allOrders.subList(third * 2, size))
    );

    ExecutorService pool = Executors.newFixedThreadPool(4);
    long start = System.currentTimeMillis();

    try {
        // invokeAll bloquea hasta que TODAS las tareas terminan
        List<Future<List<Order>>> futures = pool.invokeAll(tasks);

        // Recopilar resultados
        List<Order> allProcessed = new ArrayList<>();
        for (Future<List<Order>> future : futures) {
            try {
                List<Order> result = future.get(); // bloquea si no terminó
                allProcessed.addAll(result);
            } catch (ExecutionException e) {
                System.err.println("Error en tarea: " + e.getCause().getMessage());
            }
        }

        double grandTotal = allProcessed.stream()
                .mapToDouble(Order::getTotalAmount)
                .sum();

        System.out.printf("Resultados consolidados: %d pedidos, total: $%.2f%n",
                allProcessed.size(), grandTotal);

    } finally {
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
    }

    long elapsed = System.currentTimeMillis() - start;
    System.out.printf("Fase 3 completada en %d ms%n", elapsed);
}
```

3. Añade la invocación en `main`:

```java
// Fase 3: Callable + Future
fase3_CallableFuture(allOrders);
```

4. Compila y ejecuta:

```bash
mvn compile -q && mvn exec:java -Dexec.mainClass="com.curso.modulo5.ConcurrencyDemo" -q
```

**Salida esperada:**

```
--- FASE 3: Callable + Future (invokeAll) ---
[pool-2-thread-1] Callable 'Call-A' — procesando 16 pedidos
[pool-2-thread-2] Callable 'Call-B' — procesando 16 pedidos
[pool-2-thread-3] Callable 'Call-C' — procesando 18 pedidos
...
Resultados consolidados: 50 pedidos, total: $XXXX.XX
Fase 3 completada en ~200 ms
```

**Verificación:** Los 50 pedidos se consolidan correctamente. `Future.get()` devuelve los resultados y el `ExecutionException` está manejado. El total consolidado debe coincidir con la suma secuencial.

---

### Paso 8: Crear PipelineStage para sincronización con CyclicBarrier (Fase 4)

**Objetivo:** Implementar un pipeline de tres etapas (carga, validación, persistencia simulada) donde cada etapa espera a que todas las tareas de la etapa anterior terminen antes de avanzar.

1. Crea `src/main/java/com/curso/modulo5/PipelineStage.java`:

```java
package com.curso.modulo5;

import com.curso.modulo1.Order;

import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Tarea que ejecuta tres etapas de pipeline sincronizadas con CyclicBarrier.
 * Cada hilo procesa su lote y espera en la barrera antes de avanzar.
 */
public class PipelineStage implements Runnable {

    private final String workerName;
    private final List<Order> orders;
    private final CyclicBarrier barrier;

    public PipelineStage(String workerName, List<Order> orders,
                         CyclicBarrier barrier) {
        this.workerName = workerName;
        this.orders = orders;
        this.barrier = barrier;
    }

    @Override
    public void run() {
        try {
            // --- Etapa 1: Carga ---
            System.out.printf("[%s] %s: Etapa 1 (Carga) — %d pedidos cargados%n",
                    Thread.currentThread().getName(), workerName, orders.size());
            Thread.sleep(50); // Simular I/O de carga
            barrier.await(); // Esperar a que todos carguen

            // --- Etapa 2: Validación ---
            long validCount = orders.stream()
                    .filter(o -> o.getTotalAmount() > 0)
                    .count();
            System.out.printf("[%s] %s: Etapa 2 (Validación) — %d válidos de %d%n",
                    Thread.currentThread().getName(), workerName,
                    validCount, orders.size());
            Thread.sleep(30); // Simular validación
            barrier.await(); // Esperar a que todos validen

            // --- Etapa 3: Persistencia simulada ---
            double total = orders.stream()
                    .mapToDouble(Order::getTotalAmount)
                    .sum();
            System.out.printf("[%s] %s: Etapa 3 (Persistencia) — total persistido: $%.2f%n",
                    Thread.currentThread().getName(), workerName, total);
            Thread.sleep(20); // Simular escritura
            barrier.await(); // Esperar a que todos persistan

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.printf("[%s] %s: Interrumpido%n",
                    Thread.currentThread().getName(), workerName);
        } catch (BrokenBarrierException e) {
            System.err.printf("[%s] %s: Barrera rota — %s%n",
                    Thread.currentThread().getName(), workerName, e.getMessage());
        }
    }
}
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** La clase usa `CyclicBarrier.await()` tres veces (una por etapa). La barrera se reutiliza automáticamente después de cada ciclo, que es la característica clave de `CyclicBarrier`.

---

### Paso 9: Ejecutar el pipeline con CyclicBarrier (Fase 4)

**Objetivo:** Orquestar tres workers sincronizados con una `CyclicBarrier(3)` y observar que las etapas se ejecutan en orden.

1. Añade el método de la Fase 4 a `ConcurrencyDemo`:

```java
import java.util.concurrent.CyclicBarrier;
```

```java
/**
 * Fase 4: Pipeline de 3 etapas sincronizado con CyclicBarrier.
 */
private static void fase4_CyclicBarrier(List<Order> allOrders)
        throws InterruptedException {
    System.out.println("\n--- FASE 4: CyclicBarrier (Pipeline 3 etapas) ---");

    int size = allOrders.size();
    int third = size / 3;

    // Barrera para 3 participantes con acción al completar cada ciclo
    CyclicBarrier barrier = new CyclicBarrier(3, () ->
            System.out.println(">>> Barrera alcanzada — todos los workers completaron la etapa")
    );

    ExecutorService pool = Executors.newFixedThreadPool(4);
    long start = System.currentTimeMillis();

    try {
        pool.submit(new PipelineStage("Worker-1",
                allOrders.subList(0, third), barrier));
        pool.submit(new PipelineStage("Worker-2",
                allOrders.subList(third, third * 2), barrier));
        pool.submit(new PipelineStage("Worker-3",
                allOrders.subList(third * 2, size), barrier));
    } finally {
        pool.shutdown();
        pool.awaitTermination(30, TimeUnit.SECONDS);
    }

    long elapsed = System.currentTimeMillis() - start;
    System.out.printf("Fase 4 completada en %d ms%n", elapsed);
}
```

2. Añade la invocación en `main`:

```java
// Fase 4: CyclicBarrier
fase4_CyclicBarrier(allOrders);
```

3. Compila y ejecuta:

```bash
mvn compile -q && mvn exec:java -Dexec.mainClass="com.curso.modulo5.ConcurrencyDemo" -q
```

**Salida esperada** (las líneas dentro de cada etapa pueden intercalarse, pero las etapas se ejecutan en orden):

```
--- FASE 4: CyclicBarrier (Pipeline 3 etapas) ---
[pool-3-thread-1] Worker-1: Etapa 1 (Carga) — 16 pedidos cargados
[pool-3-thread-2] Worker-2: Etapa 1 (Carga) — 16 pedidos cargados
[pool-3-thread-3] Worker-3: Etapa 1 (Carga) — 18 pedidos cargados
>>> Barrera alcanzada — todos los workers completaron la etapa
[pool-3-thread-3] Worker-3: Etapa 2 (Validación) — 18 válidos de 18
[pool-3-thread-1] Worker-1: Etapa 2 (Validación) — 16 válidos de 16
[pool-3-thread-2] Worker-2: Etapa 2 (Validación) — 16 válidos de 16
>>> Barrera alcanzada — todos los workers completaron la etapa
...
>>> Barrera alcanzada — todos los workers completaron la etapa
Fase 4 completada en ~310 ms
```

**Verificación:** El mensaje `>>> Barrera alcanzada` aparece exactamente **3 veces** (una por etapa). Ningún worker inicia la Etapa 2 antes de que los tres hayan completado la Etapa 1. Esto confirma la sincronización correcta.

---

### Paso 10: Construir cadena asíncrona con CompletableFuture (Fase 5)

**Objetivo:** Crear un pipeline asíncrono con `CompletableFuture` que encadene carga → filtrado → reporte, con manejo de errores mediante `exceptionally`.

1. Añade los imports a `ConcurrencyDemo`:

```java
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
```

2. Añade el método de la Fase 5:

```java
/**
 * Fase 5: CompletableFuture — cadena asíncrona con pool dedicado.
 * supplyAsync → thenApplyAsync → thenAcceptAsync → exceptionally
 */
private static void fase5_CompletableFuture(List<Order> allOrders)
        throws Exception {
    System.out.println("\n--- FASE 5: CompletableFuture (cadena asíncrona) ---");

    // Pool dedicado para las tareas asíncronas
    ExecutorService asyncPool = Executors.newFixedThreadPool(4);
    long start = System.currentTimeMillis();

    try {
        CompletableFuture<Void> pipeline = CompletableFuture

                // Etapa 1: Carga asíncrona de pedidos
                .supplyAsync(() -> {
                    System.out.printf("[%s] Cargando %d pedidos...%n",
                            Thread.currentThread().getName(), allOrders.size());
                    try {
                        Thread.sleep(100); // Simular I/O
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                    return allOrders;
                }, asyncPool)

                // Etapa 2: Filtrado con Stream (pedidos > $100)
                .thenApplyAsync(orders -> {
                    System.out.printf("[%s] Filtrando pedidos > $100...%n",
                            Thread.currentThread().getName());
                    List<Order> filtered = orders.stream()
                            .filter(o -> o.getTotalAmount() > 100.0)
                            .collect(Collectors.toList());
                    System.out.printf("[%s] %d pedidos pasan el filtro%n",
                            Thread.currentThread().getName(), filtered.size());
                    return filtered;
                }, asyncPool)

                // Etapa 3: Generar reporte
                .thenAcceptAsync(filtered -> {
                    System.out.printf("[%s] Generando reporte...%n",
                            Thread.currentThread().getName());
                    double total = filtered.stream()
                            .mapToDouble(Order::getTotalAmount)
                            .sum();
                    double avg = filtered.isEmpty() ? 0 :
                            total / filtered.size();
                    System.out.printf("[%s] Reporte: %d pedidos, " +
                                    "total=$%.2f, promedio=$%.2f%n",
                            Thread.currentThread().getName(),
                            filtered.size(), total, avg);
                }, asyncPool)

                // Manejo de errores en cualquier etapa
                .exceptionally(ex -> {
                    System.err.println("Error en pipeline: " + ex.getMessage());
                    return null;
                });

        // Esperar a que el pipeline complete
        pipeline.join();

    } finally {
        asyncPool.shutdown();
        asyncPool.awaitTermination(30, TimeUnit.SECONDS);
    }

    long elapsed = System.currentTimeMillis() - start;
    System.out.printf("Fase 5 completada en %d ms%n", elapsed);
}
```

3. Añade la invocación en `main`:

```java
// Fase 5: CompletableFuture
fase5_CompletableFuture(allOrders);
```

4. Compila y ejecuta:

```bash
mvn compile -q && mvn exec:java -Dexec.mainClass="com.curso.modulo5.ConcurrencyDemo" -q
```

**Salida esperada:**

```
--- FASE 5: CompletableFuture (cadena asíncrona) ---
[pool-4-thread-1] Cargando 50 pedidos...
[pool-4-thread-2] Filtrando pedidos > $100...
[pool-4-thread-2] XX pedidos pasan el filtro
[pool-4-thread-3] Generando reporte...
[pool-4-thread-3] Reporte: XX pedidos, total=$XXXX.XX, promedio=$XXX.XX
Fase 5 completada en ~120 ms
```

**Verificación:** Las tres etapas se ejecutan en secuencia lógica pero en hilos del pool (no en `main`). El `exceptionally` no se invoca porque no hay errores. Cada etapa puede ejecutarse en un hilo diferente del pool.

---

### Paso 11: Probar manejo de errores en CompletableFuture

**Objetivo:** Verificar que `exceptionally` captura errores inyectados en el pipeline.

1. Añade un método de prueba temporal a `ConcurrencyDemo`:

```java
/**
 * Prueba de manejo de errores en CompletableFuture.
 */
private static void fase5_ErrorTest() throws Exception {
    System.out.println("\n--- FASE 5b: CompletableFuture (prueba de error) ---");

    CompletableFuture<String> failingPipeline = CompletableFuture
            .supplyAsync(() -> {
                System.out.printf("[%s] Simulando carga que falla...%n",
                        Thread.currentThread().getName());
                // Simular error
                if (true) {
                    throw new RuntimeException("Error de conexión simulado");
                }
                return "datos";
            })
            .thenApply(data -> {
                // Esta etapa NO se ejecuta si la anterior falla
                System.out.println("Esta línea NO debe aparecer");
                return data.toUpperCase();
            })
            .exceptionally(ex -> {
                System.out.printf("[%s] Capturado: %s%n",
                        Thread.currentThread().getName(),
                        ex.getCause().getMessage());
                return "VALOR_POR_DEFECTO";
            });

    String result = failingPipeline.join();
    System.out.println("Resultado recuperado: " + result);
}
```

2. Invoca desde `main` (temporalmente):

```java
// Fase 5b: Prueba de error
fase5_ErrorTest();
```

3. Compila y ejecuta:

```bash
mvn compile -q && mvn exec:java -Dexec.mainClass="com.curso.modulo5.ConcurrencyDemo" -q
```

**Salida esperada:**

```
--- FASE 5b: CompletableFuture (prueba de error) ---
[ForkJoinPool.commonPool-worker-X] Simulando carga que falla...
[main] Capturado: Error de conexión simulado
Resultado recuperado: VALOR_POR_DEFECTO
```

**Verificación:** La etapa `thenApply` **no se ejecuta** (la línea "Esta línea NO debe aparecer" no aparece). El `exceptionally` captura el error y devuelve un valor de recuperación. Esto demuestra el manejo robusto de errores en pipelines asíncronos.

---

### Paso 12: Crear RaceConditionDemo — reproducir y resolver condición de carrera (Fase 6)

**Objetivo:** Demostrar una condición de carrera reproducible sobre un contador `int` compartido y resolverla con `AtomicInteger`.

1. Crea `src/main/java/com/curso/modulo5/RaceConditionDemo.java`:

```java
package com.curso.modulo5;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Demuestra una condición de carrera y su solución con AtomicInteger.
 */
public class RaceConditionDemo {

    // ---- Contador NO seguro (race condition) ----
    private static int unsafeCounter = 0;

    // ---- Contador seguro (AtomicInteger) ----
    private static final AtomicInteger safeCounter = new AtomicInteger(0);

    private static final int THREADS = 8;
    private static final int INCREMENTS_PER_THREAD = 10_000;
    private static final int EXPECTED_TOTAL = THREADS * INCREMENTS_PER_THREAD;

    /**
     * Ejecuta la demostración de condición de carrera.
     * @return true si el contador atómico produce el resultado correcto
     */
    public static boolean run() throws InterruptedException {
        System.out.println("\n--- FASE 6: Condición de carrera y AtomicInteger ---");
        System.out.printf("Configuración: %d hilos × %d incrementos = %d esperado%n",
                THREADS, INCREMENTS_PER_THREAD, EXPECTED_TOTAL);

        // --- Prueba 1: Contador NO seguro ---
        unsafeCounter = 0;
        ExecutorService pool1 = Executors.newFixedThreadPool(THREADS);

        for (int i = 0; i < THREADS; i++) {
            pool1.submit(() -> {
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    unsafeCounter++; // NO es atómico: read-modify-write
                }
            });
        }

        pool1.shutdown();
        pool1.awaitTermination(30, TimeUnit.SECONDS);

        boolean unsafeCorrect = (unsafeCounter == EXPECTED_TOTAL);
        System.out.printf("Contador NO seguro: %d (esperado: %d) — %s%n",
                unsafeCounter, EXPECTED_TOTAL,
                unsafeCorrect ? "CORRECTO (suerte)" : "INCORRECTO (race condition)");

        // --- Prueba 2: Contador seguro con AtomicInteger ---
        safeCounter.set(0);
        ExecutorService pool2 = Executors.newFixedThreadPool(THREADS);

        for (int i = 0; i < THREADS; i++) {
            pool2.submit(() -> {
                for (int j = 0; j < INCREMENTS_PER_THREAD; j++) {
                    safeCounter.incrementAndGet(); // Operación atómica CAS
                }
            });
        }

        pool2.shutdown();
        pool2.awaitTermination(30, TimeUnit.SECONDS);

        boolean safeCorrect = (safeCounter.get() == EXPECTED_TOTAL);
        System.out.printf("Contador AtomicInteger: %d (esperado: %d) — %s%n",
                safeCounter.get(), EXPECTED_TOTAL,
                safeCorrect ? "CORRECTO (siempre)" : "INCORRECTO (bug)");

        return safeCorrect;
    }
}
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** La clase tiene dos pruebas: una con `int` (vulnerable) y otra con `AtomicInteger` (segura). La operación `unsafeCounter++` no es atómica porque implica tres pasos: leer, incrementar, escribir.

---

### Paso 13: Integrar la Fase 6 en ConcurrencyDemo

**Objetivo:** Ejecutar la demostración de condición de carrera y verificar que `AtomicInteger` siempre produce el resultado correcto.

1. Añade la invocación en `main` de `ConcurrencyDemo`:

```java
// Fase 6: Condición de carrera
boolean raceTestPassed = RaceConditionDemo.run();
System.out.println("Prueba de AtomicInteger: " +
        (raceTestPassed ? "PASÓ ✓" : "FALLÓ ✗"));
```

2. Compila y ejecuta:

```bash
mvn compile -q && mvn exec:java -Dexec.mainClass="com.curso.modulo5.ConcurrencyDemo" -q
```

**Salida esperada:**

```
--- FASE 6: Condición de carrera y AtomicInteger ---
Configuración: 8 hilos × 10000 incrementos = 80000 esperado
Contador NO seguro: 67432 (esperado: 80000) — INCORRECTO (race condition)
Contador AtomicInteger: 80000 (esperado: 80000) — CORRECTO (siempre)
Prueba de AtomicInteger: PASÓ ✓
```

> **Nota:** El valor del contador no seguro variará en cada ejecución (raramente será 80000). Esto es precisamente la condición de carrera: el resultado es no-determinístico.

3. Ejecuta **tres veces** para confirmar que el contador no seguro varía pero el atómico siempre es 80000:

```bash
for i in 1 2 3; do echo "--- Ejecución $i ---"; mvn exec:java -Dexec.mainClass="com.curso.modulo5.RaceConditionDemo" -q 2>/dev/null; done
```

> En Windows PowerShell:
```powershell
1..3 | ForEach-Object { Write-Host "--- Ejecución $_ ---"; mvn exec:java "-Dexec.mainClass=com.curso.modulo5.RaceConditionDemo" -q }
```

> **Nota:** Para ejecutar `RaceConditionDemo` directamente, añade un `main` temporal:

```java
// Añadir al final de RaceConditionDemo.java para ejecución directa
public static void main(String[] args) throws InterruptedException {
    run();
}
```

**Verificación:** En las tres ejecuciones, el contador no seguro muestra valores diferentes (todos menores que 80000), mientras que el `AtomicInteger` siempre muestra exactamente 80000.

---

### Paso 14: Consolidar ConcurrencyDemo con métricas comparativas

**Objetivo:** Ensamblar el `main` final de `ConcurrencyDemo` que orquesta las seis fases e imprime un resumen comparativo de tiempos.

1. Reemplaza el método `main` completo de `ConcurrencyDemo`:

```java
public static void main(String[] args) throws Exception {
    System.out.println("╔══════════════════════════════════════════╗");
    System.out.println("║   MÓDULO 5: CONCURRENCIA — Demo Final   ║");
    System.out.println("╚══════════════════════════════════════════╝\n");

    List<Order> allOrders = DataSetFactory.createOrders();
    System.out.println("Dataset: " + allOrders.size() + " pedidos\n");

    long totalStart = System.currentTimeMillis();

    // Línea base secuencial
    long t0 = System.currentTimeMillis();
    medirSecuencial(allOrders);
    long seqTime = System.currentTimeMillis() - t0;

    // Fase 1: Thread + Runnable
    long t1 = System.currentTimeMillis();
    fase1_ThreadRunnable(allOrders);
    long f1Time = System.currentTimeMillis() - t1;

    // Fase 2: ExecutorService
    long t2 = System.currentTimeMillis();
    fase2_ExecutorService(allOrders);
    long f2Time = System.currentTimeMillis() - t2;

    // Fase 3: Callable + Future
    long t3 = System.currentTimeMillis();
    fase3_CallableFuture(allOrders);
    long f3Time = System.currentTimeMillis() - t3;

    // Fase 4: CyclicBarrier
    long t4 = System.currentTimeMillis();
    fase4_CyclicBarrier(allOrders);
    long f4Time = System.currentTimeMillis() - t4;

    // Fase 5: CompletableFuture
    long t5 = System.currentTimeMillis();
    fase5_CompletableFuture(allOrders);
    long f5Time = System.currentTimeMillis() - t5;

    // Fase 5b: Error test
    fase5_ErrorTest();

    // Fase 6: Race condition
    boolean raceOk = RaceConditionDemo.run();

    long totalElapsed = System.currentTimeMillis() - totalStart;

    // Resumen
    System.out.println("\n╔══════════════════════════════════════════╗");
    System.out.println("║          RESUMEN DE TIEMPOS              ║");
    System.out.println("╠══════════════════════════════════════════╣");
    System.out.printf("║ Secuencial:          %6d ms            ║%n", seqTime);
    System.out.printf("║ Fase 1 (Thread):     %6d ms            ║%n", f1Time);
    System.out.printf("║ Fase 2 (Executor):   %6d ms            ║%n", f2Time);
    System.out.printf("║ Fase 3 (Callable):   %6d ms            ║%n", f3Time);
    System.out.printf("║ Fase 4 (Barrier):    %6d ms            ║%n", f4Time);
    System.out.printf("║ Fase 5 (CF):         %6d ms            ║%n", f5Time);
    System.out.printf("║ AtomicInteger test:  %s                 ║%n",
            raceOk ? "PASÓ ✓" : "FALLÓ ✗");
    System.out.printf("║ Total:               %6d ms            ║%n", totalElapsed);
    System.out.println("╚══════════════════════════════════════════╝");
}
```

2. Verifica que todos los imports están presentes al inicio de `ConcurrencyDemo.java`:

```java
package com.curso.modulo5;

import com.curso.modulo1.Order;
import com.curso.modulo4.DataSetFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
```

3. Compila y ejecuta la demostración final:

```bash
mvn compile -q
```

```bash
mvn exec:java -Dexec.mainClass="com.curso.modulo5.ConcurrencyDemo" -q
```

**Salida esperada** (tiempos aproximados en máquina de 4 núcleos):

```
╔══════════════════════════════════════════╗
║   MÓDULO 5: CONCURRENCIA — Demo Final   ║
╚══════════════════════════════════════════╝

Dataset: 50 pedidos

--- LÍNEA BASE: Ejecución secuencial ---
...
Secuencial completado en ~500 ms

--- FASE 1: Thread + Runnable ---
...
Fase 1 completada en ~200 ms

--- FASE 2: ExecutorService (FixedThreadPool) ---
...
Fase 2 completada en ~200 ms

--- FASE 3: Callable + Future (invokeAll) ---
...
Fase 3 completada en ~200 ms

--- FASE 4: CyclicBarrier (Pipeline 3 etapas) ---
...
Fase 4 completada en ~310 ms

--- FASE 5: CompletableFuture (cadena asíncrona) ---
...
Fase 5 completada en ~120 ms

--- FASE 5b: CompletableFuture (prueba de error) ---
...

--- FASE 6: Condición de carrera y AtomicInteger ---
...

╔══════════════════════════════════════════╗
║          RESUMEN DE TIEMPOS              ║
╠══════════════════════════════════════════╣
║ Secuencial:            ~500 ms           ║
║ Fase 1 (Thread):       ~200 ms           ║
║ Fase 2 (Executor):     ~200 ms           ║
║ Fase 3 (Callable):     ~200 ms           ║
║ Fase 4 (Barrier):      ~310 ms           ║
║ Fase 5 (CF):           ~120 ms           ║
║ AtomicInteger test:    PASÓ ✓            ║
║ Total:                ~2500 ms           ║
╚══════════════════════════════════════════╝
```

**Verificación:** Todas las fases completan sin errores. La ejecución secuencial es ~2.5× más lenta que las fases concurrentes. El test de `AtomicInteger` siempre pasa.

---

### Paso 15: Verificar la estructura final del proyecto

**Objetivo:** Confirmar que todos los archivos del Módulo 5 están en su lugar y el proyecto compila limpiamente.

1. Lista los archivos del Módulo 5:

```bash
find src/main/java/com/curso/modulo5 -name "*.java" | sort
```

**Salida esperada:**

```
src/main/java/com/curso/modulo5/ConcurrencyDemo.java
src/main/java/com/curso/modulo5/OrderBatchCallable.java
src/main/java/com/curso/modulo5/OrderBatchRunnable.java
src/main/java/com/curso/modulo5/PipelineStage.java
src/main/java/com/curso/modulo5/RaceConditionDemo.java
```

2. Ejecuta una compilación limpia completa:

```bash
mvn clean compile
```

**Salida esperada:**

```
[INFO] BUILD SUCCESS
```

3. Verifica que todos los módulos anteriores siguen compilando:

```bash
mvn exec:java -Dexec.mainClass="com.curso.modulo4.StreamReportDemo" -q
```

**Verificación:** El Módulo 4 sigue ejecutándose correctamente. No se han roto dependencias entre módulos. El proyecto tiene 5 paquetes funcionales.

## Validación y Pruebas

Para considerar el laboratorio completado exitosamente, verifica los siguientes criterios:

**Criterio 1 — Compilación limpia:**

```bash
mvn clean compile -q
```

Resultado: `BUILD SUCCESS` sin advertencias de compilación.

**Criterio 2 — Ejecución completa de ConcurrencyDemo:**

```bash
mvn exec:java -Dexec.mainClass="com.curso.modulo5.ConcurrencyDemo" -q 2>&1 | tail -15
```

Resultado: El resumen de tiempos se imprime y `AtomicInteger test: PASÓ ✓`.

**Criterio 3 — No-determinismo observable (Fase 1):**

Ejecuta dos veces y compara el orden de las líneas de los lotes:

```bash
mvn exec:java -Dexec.mainClass="com.curso.modulo5.ConcurrencyDemo" -q 2>&1 | grep "Lote '"
```

Resultado: El orden de las líneas varía entre ejecuciones.

**Criterio 4 — Cierre ordenado de pools:**

Ninguna ejecución debe mostrar el mensaje `ADVERTENCIA: El pool no terminó en 30s`. Si aparece, hay un deadlock o una tarea bloqueada.

**Criterio 5 — Condición de carrera reproducible:**

```bash
mvn exec:java -Dexec.mainClass="com.curso.modulo5.RaceConditionDemo" -q 2>&1 | grep "Contador"
```

Resultado: El contador NO seguro muestra un valor < 80000 (en la mayoría de ejecuciones). El `AtomicInteger` siempre muestra 80000.

**Criterio 6 — Caso adverso (manejo de error en CompletableFuture):**

La Fase 5b debe imprimir `Capturado: Error de conexión simulado` y devolver `VALOR_POR_DEFECTO`. La línea `"Esta línea NO debe aparecer"` **no** debe aparecer en la salida. Esto valida que el pipeline asíncrono no ejecuta etapas posteriores al error y que `exceptionally` proporciona un valor de recuperación verificable.

**Criterio 7 — Speedup medible:**

En el resumen de tiempos, las Fases 1–3 deben completar en menos tiempo que la ejecución secuencial. En máquinas de 4+ núcleos, el speedup esperado es ~2× a 3×.

## Solución de Problemas

**Problema 1: El contador no seguro siempre muestra 80000 (no se reproduce la race condition)**

| Aspecto | Detalle |
|---|---|
| **Síntoma** | `unsafeCounter` muestra exactamente 80000 en todas las ejecuciones. |
| **Causa** | En máquinas con pocos núcleos o JVMs con optimizaciones agresivas, la JIT puede serializar los accesos. También puede ocurrir si `INCREMENTS_PER_THREAD` es demasiado bajo. |
| **Solución** | Incrementa las constantes en `RaceConditionDemo`: cambia `INCREMENTS_PER_THREAD` a `100_000` y `THREADS` a `16`. Recompila y ejecuta. Con más contención, la race condition se manifiesta de forma consistente: |

```java
private static final int THREADS = 16;
private static final int INCREMENTS_PER_THREAD = 100_000;
private static final int EXPECTED_TOTAL = THREADS * INCREMENTS_PER_THREAD;
```

---

**Problema 2: `java.util.concurrent.BrokenBarrierException` en la Fase 4**

| Aspecto | Detalle |
|---|---|
| **Síntoma** | Uno o más workers reportan `Barrera rota` y la Fase 4 termina con resultados incompletos. |
| **Causa** | Si uno de los hilos lanza una excepción no capturada antes de llegar a `barrier.await()`, la barrera se "rompe" y los demás hilos que ya estaban esperando reciben `BrokenBarrierException`. Esto puede ocurrir si `Order.getTotalAmount()` lanza `NullPointerException` en pedidos con datos incompletos. |
| **Solución** | Verifica que `DataSetFactory.createOrders()` no genera pedidos con `totalAmount` nulo. Si el problema persiste, añade validación defensiva en `PipelineStage.run()`: |

```java
// En la Etapa 2 de PipelineStage, reemplaza el filtro por:
long validCount = orders.stream()
        .filter(o -> o != null && o.getTotalAmount() > 0)
        .count();
```

Adicionalmente, verifica que la barrera se crea con exactamente el mismo número de workers que se envían al pool (3 workers → `CyclicBarrier(3)`).

## Limpieza

Este laboratorio no crea recursos externos (contenedores, bases de datos, archivos temporales). La limpieza consiste en:

1. Eliminar el método `main` temporal de `RaceConditionDemo` si lo añadiste en el Paso 13:

```java
// Eliminar si existe:
// public static void main(String[] args) throws InterruptedException { run(); }
```

2. Compilar una última vez para confirmar integridad:

```bash
mvn clean compile -q
```

3. (Opcional) Hacer commit del Módulo 5:

```bash
git add src/main/java/com/curso/modulo5/
git commit -m "Lab 05-00-01: Módulo 5 — Concurrencia con ExecutorService, CyclicBarrier, CompletableFuture y AtomicInteger"
```

## Resumen

En este laboratorio construiste un sistema de procesamiento concurrente progresivo a través de seis fases:

| Fase | Concepto | Clase clave |
|---|---|---|
| Línea base | Ejecución secuencial en hilo `main` | `OrderBatchRunnable.run()` |
| Fase 1 | `Thread` + `Runnable` + `join()` | `Thread`, `OrderBatchRunnable` |
| Fase 2 | `ExecutorService` + cierre ordenado | `Executors.newFixedThreadPool(4)` |
| Fase 3 | `Callable<V>` + `Future<V>` + `invokeAll` | `OrderBatchCallable` |
| Fase 4 | `CyclicBarrier` (pipeline sincronizado) | `PipelineStage` |
| Fase 5 | `CompletableFuture` (cadena asíncrona) | `supplyAsync` → `thenApplyAsync` |
| Fase 6 | Race condition + `AtomicInteger` | `RaceConditionDemo` |

**Puntos clave aprendidos:**

- `Thread` directo crea overhead de gestión; `ExecutorService` reutiliza hilos y centraliza el control del ciclo de vida.
- El patrón `shutdown()` + `awaitTermination()` es obligatorio para evitar pérdida de tareas en vuelo.
- `Callable<V>` permite obtener resultados y propagar excepciones, a diferencia de `Runnable`.
- `CyclicBarrier` sincroniza N hilos en puntos de encuentro reutilizables.
- `CompletableFuture` permite construir pipelines asíncronos declarativos con manejo de errores integrado.
- La operación `i++` no es atómica; `AtomicInteger.incrementAndGet()` usa CAS (Compare-And-Swap) para garantizar corrección sin locks.

**Recursos adicionales:**

- [Documentación de java.util.concurrent — Oracle JDK 11](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/package-summary.html)
- [CompletableFuture API — Oracle JDK 11](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/CompletableFuture.html)
- [Java Concurrency in Practice — Brian Goetz et al.](https://jcip.net/)
- [Guía de AtomicInteger — Baeldung](https://www.baeldung.com/java-atomic-variables)
