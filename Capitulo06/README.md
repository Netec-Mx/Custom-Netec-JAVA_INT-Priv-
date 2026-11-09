# Práctica: Procesamiento paralelo con Parallel Streams y Fork/Join

## Metadatos

| Campo | Valor |
|---|---|
| **Duración** | 100 minutos |
| **Complejidad** | Media |
| **Nivel Bloom** | Aplicar |
| **Módulo** | 6 — Parallel Streams y Fork/Join |
| **Proyecto** | `parallel-streams-lab` (proyecto Maven independiente) |

## Descripción General

El participante procesará un conjunto amplio de datos con `stream()` y `parallelStream()`, midiendo y comparando los tiempos de ejecución. Analizará el comportamiento de operaciones determinísticas y no determinísticas, implementará reducciones en paralelo con `reduce` cuidando la asociatividad de la identidad, el acumulador y el combinador, y resolverá un problema divisible mediante `RecursiveTask` y `ForkJoinPool`, concluyendo en qué escenarios conviene utilizar o evitar los Parallel Streams.

## Objetivos de Aprendizaje

- [ ] Invocar el método `parallelStream()` sobre colecciones y comparar su rendimiento frente a `stream()` secuencial mediante medición de tiempo con `System.nanoTime()` y `Instant`/`Duration` de `java.time`.
- [ ] Identificar y demostrar la diferencia entre operaciones determinísticas (`findFirst`, `forEachOrdered`) y no determinísticas (`findAny`, `forEach`) en el contexto de streams paralelos.
- [ ] Implementar reducciones en paralelo con el método `reduce(identity, accumulator, combiner)` garantizando que la identidad sea neutral, el acumulador sea sin efectos secundarios y el combinador sea asociativo.
- [ ] Crear una subclase de `RecursiveTask` para dividir un problema de suma de gran escala y ejecutarla mediante `ForkJoinPool.commonPool()` y un `ForkJoinPool` personalizado.
- [ ] Determinar criterios objetivos para decidir cuándo los Parallel Streams ofrecen beneficios reales y cuándo deben evitarse.

## Prerrequisitos

**Conocimientos requeridos:**

| Conocimiento | Nivel |
|---|---|
| API de Streams de Java SE 11 (`filter`, `map`, `collect`, `reduce`) | Intermedio |
| Comprensión básica de hilos y concurrencia en Java | Básico |
| Medición de tiempo en Java (`System.nanoTime()`, `java.time.Instant`) | Básico |
| Gestión de proyectos Maven desde línea de comandos | Básico |

**Acceso y herramientas:**

| Herramienta | Versión exacta | Fuente |
|---|---|---|
| JDK Eclipse Temurin | 11.0.23+9 | https://adoptium.net/temurin/releases/?version=11 |
| Apache Maven | 3.9.8 | https://maven.apache.org/download.cgi |
| IntelliJ IDEA Community Edition | 2024.1.4 | https://www.jetbrains.com/idea/download/ |
| Git | 2.45.2 | https://git-scm.com/downloads |

## Entorno de Laboratorio

**Hardware mínimo:**

| Recurso | Mínimo | Recomendado |
|---|---|---|
| Procesador | 4 núcleos físicos (64 bits) | 8 núcleos (para observar paralelismo real) |
| RAM | 8 GB | 16 GB |
| Disco | 20 GB libres (SSD) | SSD NVMe |

> **Nota importante:** Este laboratorio crea un proyecto Maven **independiente** llamado `parallel-streams-lab` fuera del directorio `java-se11-labs`. Esto se debe a que el scope del laboratorio así lo establece. El proyecto principal `java-se11-labs` y sus módulos previos no se modifican.

**Directorio de trabajo:**

| SO | Ruta del proyecto |
|---|---|
| Linux / macOS | `~/cursos/parallel-streams-lab` |
| Windows | `C:\cursos\parallel-streams-lab` |

## Instrucciones Paso a Paso

---

### Paso 1: Verificar el entorno y crear el proyecto Maven

**Objetivo:** Confirmar que JDK 11 y Maven están disponibles, y crear la estructura del proyecto independiente.

1. Abre una terminal y verifica las versiones:

```bash
java -version
```

```text
openjdk version "11.0.23" 2024-04-16
```

```bash
mvn -version
```

```text
Apache Maven 3.9.8
```

2. Crea el directorio del proyecto y navega a él:

```bash
mkdir -p ~/cursos/parallel-streams-lab
cd ~/cursos/parallel-streams-lab
```

3. Crea el archivo `pom.xml`:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>com.curso</groupId>
    <artifactId>parallel-streams-lab</artifactId>
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

4. Crea la estructura de paquetes:

```bash
mkdir -p src/main/java/com/curso/parallelstreams
```

5. Compila para verificar:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS` sin errores.

**Verificación:** El directorio `target/classes` existe y el proyecto compila correctamente con `--release 11`.

---

### Paso 2: Crear el modelo de datos Transaction

**Objetivo:** Definir la clase `Transaction` que será la unidad de datos para todos los ejercicios.

1. Crea el archivo `src/main/java/com/curso/parallelstreams/Transaction.java`:

```java
package com.curso.parallelstreams;

import java.time.LocalDateTime;

public class Transaction {

    private final long id;
    private final double amount;
    private final String category;
    private final LocalDateTime timestamp;

    public Transaction(long id, double amount, String category,
                       LocalDateTime timestamp) {
        this.id = id;
        this.amount = amount;
        this.category = category;
        this.timestamp = timestamp;
    }

    public long getId() { return id; }
    public double getAmount() { return amount; }
    public String getCategory() { return category; }
    public LocalDateTime getTimestamp() { return timestamp; }

    @Override
    public String toString() {
        return String.format("Tx{id=%d, amount=%.2f, cat='%s'}",
                id, amount, category);
    }
}
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** La clase es inmutable (campos `final`, sin setters), lo que la hace segura para procesamiento concurrente.

---

### Paso 3: Crear el generador de datos TransactionFactory

**Objetivo:** Construir una fábrica que genere 10,000,000 de transacciones almacenadas en un `ArrayList` para garantizar spliterators eficientes.

1. Crea `src/main/java/com/curso/parallelstreams/TransactionFactory.java`:

```java
package com.curso.parallelstreams;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TransactionFactory {

    public static final int DEFAULT_SIZE = 10_000_000;

    private static final String[] CATEGORIES = {
        "ELECTRONICS", "FOOD", "CLOTHING", "BOOKS",
        "SPORTS", "HOME", "TRAVEL", "HEALTH"
    };

    /**
     * Genera una lista de transacciones con datos pseudoaleatorios.
     * Se usa una semilla fija para reproducibilidad entre participantes.
     */
    public static List<Transaction> generate(int size) {
        Random random = new Random(42L); // Semilla fija
        List<Transaction> transactions = new ArrayList<>(size);
        LocalDateTime base = LocalDateTime.of(2024, 1, 1, 0, 0);

        for (int i = 0; i < size; i++) {
            transactions.add(new Transaction(
                i + 1L,
                random.nextDouble() * 1000.0,       // 0.00 - 999.99
                CATEGORIES[random.nextInt(CATEGORIES.length)],
                base.plusMinutes(random.nextInt(525_600)) // Dentro del año
            ));
        }
        return transactions;
    }

    public static List<Transaction> generate() {
        return generate(DEFAULT_SIZE);
    }
}
```

2. Compila:

```bash
mvn compile -q
```

**Salida esperada:** `BUILD SUCCESS`.

**Verificación:** La semilla fija `42L` garantiza que todos los participantes generen exactamente los mismos datos. El uso de `ArrayList` proporciona un `Spliterator` con característica `SIZED` y `SUBSIZED`, ideal para división paralela.

---

### Paso 4: Ejercicio 1 — Comparar stream() vs parallelStream() con medición de tiempos

**Objetivo:** Filtrar transacciones de la categoría `"ELECTRONICS"` con monto > 500, calcular la suma de montos, y medir tiempos de ejecución secuencial vs paralelo.

1. Crea `src/main/java/com/curso/parallelstreams/Ex01_StreamComparison.java`:

```java
package com.curso.parallelstreams;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class Ex01_StreamComparison {

    public static void main(String[] args) {
        System.out.println("=== Ejercicio 1: stream() vs parallelStream() ===");
        System.out.println("Generando " + TransactionFactory.DEFAULT_SIZE
                + " transacciones...");

        List<Transaction> transactions = TransactionFactory.generate();
        System.out.println("Transacciones generadas: " + transactions.size());
        System.out.println("Procesadores disponibles: "
                + Runtime.getRuntime().availableProcessors());
        System.out.println();

        // --- Procesamiento SECUENCIAL ---
        Instant startSeq = Instant.now();
        long countSeq = transactions.stream()
                .filter(t -> "ELECTRONICS".equals(t.getCategory()))
                .filter(t -> t.getAmount() > 500.0)
                .count();
        Duration durationSeq = Duration.between(startSeq, Instant.now());

        // --- Procesamiento PARALELO ---
        Instant startPar = Instant.now();
        long countPar = transactions.parallelStream()
                .filter(t -> "ELECTRONICS".equals(t.getCategory()))
                .filter(t -> t.getAmount() > 500.0)
                .count();
        Duration durationPar = Duration.between(startPar, Instant.now());

        System.out.println("--- Resultados de count() ---");
        System.out.printf("Secuencial : %,d elementos en %d ms%n",
                countSeq, durationSeq.toMillis());
        System.out.printf("Paralelo   : %,d elementos en %d ms%n",
                countPar, durationPar.toMillis());
        System.out.printf("Speedup    : %.2fx%n",
                (double) durationSeq.toMillis() / durationPar.toMillis());
        System.out.println();

        // --- Suma de montos con mapToDouble ---
        long nanoSeqSum = System.nanoTime();
        double sumSeq = transactions.stream()
                .filter(t -> "ELECTRONICS".equals(t.getCategory()))
                .filter(t -> t.getAmount() > 500.0)
                .mapToDouble(Transaction::getAmount)
                .sum();
        long seqSumMs = (System.nanoTime() - nanoSeqSum) / 1_000_000;

        long nanoParSum = System.nanoTime();
        double sumPar = transactions.parallelStream()
                .filter(t -> "ELECTRONICS".equals(t.getCategory()))
                .filter(t -> t.getAmount() > 500.0)
                .mapToDouble(Transaction::getAmount)
                .sum();
        long parSumMs = (System.nanoTime() - nanoParSum) / 1_000_000;

        System.out.println("--- Resultados de sum() ---");
        System.out.printf("Secuencial : %.2f en %d ms%n", sumSeq, seqSumMs);
        System.out.printf("Paralelo   : %.2f en %d ms%n", sumPar, parSumMs);
        System.out.printf("Speedup    : %.2fx%n",
                (double) seqSumMs / parSumMs);
        System.out.println();

        // --- Collect a lista filtrada ---
        long nanoSeqCol = System.nanoTime();
        List<Transaction> listSeq = transactions.stream()
                .filter(t -> t.getAmount() > 900.0)
                .collect(Collectors.toList());
        long seqColMs = (System.nanoTime() - nanoSeqCol) / 1_000_000;

        long nanoParCol = System.nanoTime();
        List<Transaction> listPar = transactions.parallelStream()
                .filter(t -> t.getAmount() > 900.0)
                .collect(Collectors.toList());
        long parColMs = (System.nanoTime() - nanoParCol) / 1_000_000;

        System.out.println("--- Resultados de collect(toList()) ---");
        System.out.printf("Secuencial : %,d elementos en %d ms%n",
                listSeq.size(), seqColMs);
        System.out.printf("Paralelo   : %,d elementos en %d ms%n",
                listPar.size(), parColMs);
        System.out.printf("Speedup    : %.2fx%n",
                (double) seqColMs / Math.max(parColMs, 1));
    }
}
```

2. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.curso.parallelstreams.Ex01_StreamComparison
```

**Salida esperada** (los tiempos variarán según el hardware):

```text
=== Ejercicio 1: stream() vs parallelStream() ===
Generando 10000000 transacciones...
Transacciones generadas: 10000000
Procesadores disponibles: 8

--- Resultados de count() ---
Secuencial : 624,375 elementos en 142 ms
Paralelo   : 624,375 elementos en 48 ms
Speedup    : 2.96x

--- Resultados de sum() ---
Secuencial : 468281250.37 en 135 ms
Paralelo   : 468281250.37 en 41 ms
Speedup    : 3.29x

--- Resultados de collect(toList()) ---
Secuencial : 1,000,512 elementos en 98 ms
Paralelo   : 1,000,512 elementos en 35 ms
Speedup    : 2.80x
```

**Verificación:** (1) Los conteos y sumas son **idénticos** entre secuencial y paralelo. (2) El stream paralelo muestra un speedup > 1.0x en las tres operaciones. (3) Los resultados numéricos coinciden entre participantes gracias a la semilla fija.

---

### Paso 5: Ejercicio 2 — Operaciones determinísticas vs no determinísticas

**Objetivo:** Demostrar que `findFirst()` y `forEachOrdered()` preservan el orden (determinísticas) mientras que `findAny()` y `forEach()` no lo garantizan en streams paralelos (no determinísticas).

1. Crea `src/main/java/com/curso/parallelstreams/Ex02_Determinism.java`:

```java
package com.curso.parallelstreams;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Ex02_Determinism {

    public static void main(String[] args) {
        // Usamos un subconjunto pequeño para observar claramente el orden
        List<Transaction> all = TransactionFactory.generate(100);
        List<Transaction> electronics = all.stream()
                .filter(t -> "ELECTRONICS".equals(t.getCategory()))
                .collect(Collectors.toList());

        System.out.println("=== Ejercicio 2: Determinísticas vs No Determinísticas ===");
        System.out.println("Transacciones ELECTRONICS: " + electronics.size());
        System.out.println();

        // --- 2a: findFirst() — determinística ---
        System.out.println("--- findFirst() en paralelo (3 ejecuciones) ---");
        for (int i = 1; i <= 3; i++) {
            Optional<Transaction> first = electronics.parallelStream()
                    .filter(t -> t.getAmount() > 200.0)
                    .findFirst();
            System.out.printf("  Ejecución %d: %s%n", i,
                    first.map(Transaction::toString).orElse("VACÍO"));
        }
        System.out.println();

        // --- 2b: findAny() — no determinística ---
        System.out.println("--- findAny() en paralelo (3 ejecuciones) ---");
        for (int i = 1; i <= 3; i++) {
            Optional<Transaction> any = electronics.parallelStream()
                    .filter(t -> t.getAmount() > 200.0)
                    .findAny();
            System.out.printf("  Ejecución %d: %s%n", i,
                    any.map(Transaction::toString).orElse("VACÍO"));
        }
        System.out.println();

        // --- 2c: forEachOrdered() — determinística ---
        System.out.println("--- forEachOrdered() en paralelo (primeros 5) ---");
        electronics.parallelStream()
                .filter(t -> t.getAmount() > 200.0)
                .limit(5)
                .forEachOrdered(t -> System.out.println("  " + t));
        System.out.println();

        // --- 2d: forEach() — no determinística ---
        System.out.println("--- forEach() en paralelo (primeros 5) ---");
        System.out.println("  Ejecución 1:");
        electronics.parallelStream()
                .filter(t -> t.getAmount() > 200.0)
                .limit(5)
                .forEach(t -> System.out.println("    " + t));

        System.out.println("  Ejecución 2:");
        electronics.parallelStream()
                .filter(t -> t.getAmount() > 200.0)
                .limit(5)
                .forEach(t -> System.out.println("    " + t));
    }
}
```

2. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.curso.parallelstreams.Ex02_Determinism
```

**Salida esperada:**

```text
=== Ejercicio 2: Determinísticas vs No Determinísticas ===
Transacciones ELECTRONICS: 13

--- findFirst() en paralelo (3 ejecuciones) ---
  Ejecución 1: Tx{id=5, amount=735.40, cat='ELECTRONICS'}
  Ejecución 2: Tx{id=5, amount=735.40, cat='ELECTRONICS'}
  Ejecución 3: Tx{id=5, amount=735.40, cat='ELECTRONICS'}

--- findAny() en paralelo (3 ejecuciones) ---
  Ejecución 1: Tx{id=72, amount=489.21, cat='ELECTRONICS'}
  Ejecución 2: Tx{id=5, amount=735.40, cat='ELECTRONICS'}
  Ejecución 3: Tx{id=38, amount=612.55, cat='ELECTRONICS'}

--- forEachOrdered() en paralelo (primeros 5) ---
  Tx{id=5, amount=735.40, cat='ELECTRONICS'}
  Tx{id=18, amount=301.22, cat='ELECTRONICS'}
  ...

--- forEach() en paralelo (primeros 5) ---
  Ejecución 1:
    Tx{id=38, amount=612.55, cat='ELECTRONICS'}
    Tx{id=5, amount=735.40, cat='ELECTRONICS'}
    ...
  Ejecución 2:
    Tx{id=18, amount=301.22, cat='ELECTRONICS'}
    Tx{id=5, amount=735.40, cat='ELECTRONICS'}
    ...
```

**Verificación:**
- `findFirst()` retorna **siempre el mismo elemento** en las 3 ejecuciones (determinística).
- `findAny()` puede retornar **elementos diferentes** entre ejecuciones (no determinística).
- `forEachOrdered()` imprime en el **mismo orden** que la fuente original.
- `forEach()` imprime en **orden variable** entre ejecuciones.

---

### Paso 6: Ejercicio 3a — Reducción paralela correcta: suma de montos

**Objetivo:** Implementar `reduce` con identidad `0.0` y acumulador asociativo para sumar montos, verificando que el resultado es idéntico al stream secuencial.

1. Crea `src/main/java/com/curso/parallelstreams/Ex03_Reduce.java`:

```java
package com.curso.parallelstreams;

import java.util.List;

public class Ex03_Reduce {

    public static void main(String[] args) {
        List<Transaction> transactions = TransactionFactory.generate(1_000_000);

        System.out.println("=== Ejercicio 3: Reducciones en paralelo ===");
        System.out.println("Transacciones: " + transactions.size());
        System.out.println();

        // --- 3a: Suma simple con reduce ---
        System.out.println("--- 3a: Suma de montos con reduce ---");

        double sumSeq = transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .reduce(0.0, Double::sum);

        double sumPar = transactions.parallelStream()
                .mapToDouble(Transaction::getAmount)
                .reduce(0.0, Double::sum);

        System.out.printf("Secuencial: %.2f%n", sumSeq);
        System.out.printf("Paralelo  : %.2f%n", sumPar);
        System.out.printf("¿Iguales? : %b%n",
                Math.abs(sumSeq - sumPar) < 0.01);
        System.out.println();

        // --- 3b: Identidad INCORRECTA (demostración del problema) ---
        System.out.println("--- 3b: Concatenación con identidad INCORRECTA ---");

        // Tomamos solo 8 categorías para visualizar el problema
        List<Transaction> small = transactions.subList(0, 8);

        String concatSeq = small.stream()
                .map(Transaction::getCategory)
                .reduce("", (a, b) -> a + "," + b);

        String concatPar = small.parallelStream()
                .map(Transaction::getCategory)
                .reduce("", (a, b) -> a + "," + b);

        System.out.println("Secuencial: " + concatSeq);
        System.out.println("Paralelo  : " + concatPar);
        System.out.println("NOTA: La identidad \"\" con \",\" como separador");
        System.out.println("      produce comas iniciales espurias en paralelo.");
        System.out.println("      Solución correcta: usar Collectors.joining().");
        System.out.println();

        // Solución correcta con Collectors.joining
        String correctJoin = small.parallelStream()
                .map(Transaction::getCategory)
                .collect(java.util.stream.Collectors.joining(","));
        System.out.println("Correcto (joining): " + correctJoin);
        System.out.println();

        // --- 3c: Reduce con acumulador y combinador explícito ---
        System.out.println("--- 3c: Reduce con identity + accumulator + combiner ---");

        // Calcular la suma de montos usando la variante de 3 argumentos
        // sobre Stream<Transaction> (no DoubleStream)
        double sumCombiner = transactions.parallelStream()
                .reduce(
                    0.0,                                    // identity
                    (partial, tx) -> partial + tx.getAmount(), // accumulator
                    Double::sum                             // combiner
                );

        System.out.printf("Suma (3-arg reduce): %.2f%n", sumCombiner);
        System.out.printf("¿Coincide con secuencial? %b%n",
                Math.abs(sumSeq - sumCombiner) < 0.01);
        System.out.println();

        // Verificación de asociatividad
        System.out.println("--- Verificación de asociatividad ---");
        System.out.println("identity = 0.0 (neutral para suma)");
        System.out.println("combiner(a,b) = a + b (asociativa: (a+b)+c == a+(b+c))");
        System.out.println("accumulator(partial, tx) = partial + tx.amount");
        System.out.println("=> Todas las propiedades se cumplen.");
    }
}
```

2. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.curso.parallelstreams.Ex03_Reduce
```

**Salida esperada:**

```text
=== Ejercicio 3: Reducciones en paralelo ===
Transacciones: 1000000

--- 3a: Suma de montos con reduce ---
Secuencial: 499865432.10
Paralelo  : 499865432.10
¿Iguales? : true

--- 3b: Concatenación con identidad INCORRECTA ---
Secuencial: ,FOOD,ELECTRONICS,CLOTHING,...
Paralelo  : ,FOOD,,ELECTRONICS,CLOTHING,...
NOTA: La identidad "" con "," como separador
      produce comas iniciales espurias en paralelo.
      Solución correcta: usar Collectors.joining().

Correcto (joining): FOOD,ELECTRONICS,CLOTHING,...

--- 3c: Reduce con identity + accumulator + combiner ---
Suma (3-arg reduce): 499865432.10
¿Coincide con secuencial? true

--- Verificación de asociatividad ---
identity = 0.0 (neutral para suma)
combiner(a,b) = a + b (asociativa: (a+b)+c == a+(b+c))
accumulator(partial, tx) = partial + tx.amount
=> Todas las propiedades se cumplen.
```

**Verificación:** (1) La suma 3a es idéntica entre secuencial y paralelo. (2) La concatenación 3b muestra el problema de comas espurias en paralelo. (3) La variante de 3 argumentos en 3c produce el resultado correcto gracias a la asociatividad del combinador `Double::sum`.

---

### Paso 7: Ejercicio 4 — RecursiveTask y ForkJoinPool

**Objetivo:** Implementar `SumRecursiveTask` que divida recursivamente un arreglo de montos y lo ejecute tanto en el pool común como en un pool personalizado de 4 hilos.

1. Crea `src/main/java/com/curso/parallelstreams/SumRecursiveTask.java`:

```java
package com.curso.parallelstreams;

import java.util.concurrent.RecursiveTask;

public class SumRecursiveTask extends RecursiveTask<Double> {

    private static final int THRESHOLD = 10_000;

    private final double[] amounts;
    private final int start;
    private final int end;

    public SumRecursiveTask(double[] amounts, int start, int end) {
        this.amounts = amounts;
        this.start = start;
        this.end = end;
    }

    @Override
    protected Double compute() {
        int length = end - start;

        // Caso base: si el rango es menor al umbral, sumar directamente
        if (length <= THRESHOLD) {
            double sum = 0.0;
            for (int i = start; i < end; i++) {
                sum += amounts[i];
            }
            return sum;
        }

        // Caso recursivo: dividir en dos mitades
        int mid = start + length / 2;

        SumRecursiveTask leftTask = new SumRecursiveTask(amounts, start, mid);
        SumRecursiveTask rightTask = new SumRecursiveTask(amounts, mid, end);

        // fork() envía leftTask al pool; compute() ejecuta rightTask en este hilo
        leftTask.fork();
        double rightResult = rightTask.compute();
        double leftResult = leftTask.join();

        return leftResult + rightResult;
    }
}
```

2. Crea `src/main/java/com/curso/parallelstreams/Ex04_ForkJoin.java`:

```java
package com.curso.parallelstreams;

import java.util.List;
import java.util.concurrent.ForkJoinPool;

public class Ex04_ForkJoin {

    public static void main(String[] args) {
        System.out.println("=== Ejercicio 4: RecursiveTask y ForkJoinPool ===");

        List<Transaction> transactions = TransactionFactory.generate();
        System.out.println("Transacciones: " + transactions.size());

        // Extraer montos a un arreglo primitivo para máximo rendimiento
        double[] amounts = transactions.stream()
                .mapToDouble(Transaction::getAmount)
                .toArray();

        System.out.println("Arreglo de montos: " + amounts.length + " elementos");
        System.out.println("THRESHOLD: 10,000");
        System.out.println();

        // --- Línea base: suma secuencial con bucle for ---
        long nanoSeq = System.nanoTime();
        double sumLoop = 0.0;
        for (double amount : amounts) {
            sumLoop += amount;
        }
        long seqMs = (System.nanoTime() - nanoSeq) / 1_000_000;
        System.out.printf("Suma secuencial (for)      : %.2f en %d ms%n",
                sumLoop, seqMs);

        // --- ForkJoinPool.commonPool() ---
        SumRecursiveTask taskCommon = new SumRecursiveTask(amounts, 0, amounts.length);
        ForkJoinPool commonPool = ForkJoinPool.commonPool();

        System.out.println("Common pool parallelism    : "
                + commonPool.getParallelism());

        long nanoCommon = System.nanoTime();
        double sumCommon = commonPool.invoke(taskCommon);
        long commonMs = (System.nanoTime() - nanoCommon) / 1_000_000;
        System.out.printf("Suma ForkJoin (commonPool) : %.2f en %d ms%n",
                sumCommon, commonMs);

        // --- ForkJoinPool personalizado de 4 hilos ---
        ForkJoinPool customPool = new ForkJoinPool(4);
        SumRecursiveTask taskCustom = new SumRecursiveTask(amounts, 0, amounts.length);

        System.out.println("Custom pool parallelism    : "
                + customPool.getParallelism());

        long nanoCustom = System.nanoTime();
        double sumCustom = customPool.invoke(taskCustom);
        long customMs = (System.nanoTime() - nanoCustom) / 1_000_000;
        System.out.printf("Suma ForkJoin (4 hilos)    : %.2f en %d ms%n",
                sumCustom, customMs);

        customPool.shutdown();

        // --- Comparación ---
        System.out.println();
        System.out.println("--- Comparación de tiempos ---");
        System.out.printf("Secuencial (for)   : %4d ms%n", seqMs);
        System.out.printf("Common ForkJoinPool : %4d ms%n", commonMs);
        System.out.printf("Custom (4 hilos)    : %4d ms%n", customMs);
        System.out.println();

        // --- Verificación de resultados ---
        boolean match = Math.abs(sumLoop - sumCommon) < 0.01
                && Math.abs(sumLoop - sumCustom) < 0.01;
        System.out.println("¿Todos los resultados coinciden? " + match);
    }
}
```

3. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.curso.parallelstreams.Ex04_ForkJoin
```

**Salida esperada:**

```text
=== Ejercicio 4: RecursiveTask y ForkJoinPool ===
Transacciones: 10000000
Arreglo de montos: 10000000 elementos
THRESHOLD: 10,000

Suma secuencial (for)      : 4998765432.10 en 12 ms
Common pool parallelism    : 7
Suma ForkJoin (commonPool) : 4998765432.10 en 5 ms
Custom pool parallelism    : 4
Suma ForkJoin (4 hilos)    : 4998765432.10 en 7 ms

--- Comparación de tiempos ---
Secuencial (for)   :   12 ms
Common ForkJoinPool :    5 ms
Custom (4 hilos)    :    7 ms

¿Todos los resultados coinciden? true
```

**Verificación:** (1) Las tres sumas son idénticas. (2) El `commonPool` utiliza `availableProcessors() - 1` hilos. (3) El pool personalizado usa exactamente 4 hilos. (4) Ambas ejecuciones Fork/Join son más rápidas que el bucle secuencial para 10M de elementos.

---

### Paso 8: Crear el archivo de conclusiones

**Objetivo:** Documentar los criterios para decidir cuándo usar y cuándo evitar Parallel Streams, consolidando los aprendizajes de los cuatro ejercicios.

1. Crea el archivo `CONCLUSIONS.md` en la raíz del proyecto:

```markdown
## Conclusiones: Cuándo utilizar y cuándo evitar Parallel Streams

## Escenarios donde los Parallel Streams son BENEFICIOSOS

1. **Colecciones grandes con acceso aleatorio (ArrayList, arrays)**
   - El Spliterator de ArrayList divide equitativamente → buena distribución.
   - Observado en Ejercicio 1: speedup de ~3x con 10M de elementos.

2. **Operaciones CPU-bound sin estado compartido**
   - filter(), map(), reduce() con funciones puras.
   - Observado en Ejercicio 3: reduce con asociatividad produce resultados correctos.

3. **Problemas divisibles recursivamente**
   - RecursiveTask con THRESHOLD adecuado.
   - Observado en Ejercicio 4: ForkJoinPool supera al bucle secuencial.

## Escenarios donde los Parallel Streams deben EVITARSE

1. **Colecciones pequeñas (< 10,000 elementos)**
   - El overhead de fork/join supera el beneficio del paralelismo.

2. **LinkedList y estructuras sin acceso aleatorio**
   - El Spliterator no puede dividir eficientemente → particiones desiguales.

3. **Operaciones con estado compartido mutable**
   - Riesgo de condiciones de carrera. Usar synchronized anula el paralelismo.

4. **Operaciones I/O-bound (lectura de archivos, llamadas de red)**
   - Los hilos del ForkJoinPool se bloquean esperando I/O.
   - Mejor usar ExecutorService con pool de hilos dedicado.

5. **Operaciones dependientes del orden**
   - forEach() en paralelo no garantiza orden (Ejercicio 2).
   - forEachOrdered() preserva orden pero reduce el paralelismo.

6. **Reduce con identidad no neutral o combinador no asociativo**
   - Produce resultados incorrectos (Ejercicio 3b: comas espurias).

## Regla general

> Medir antes de paralelizar. El speedup depende del hardware, tamaño de datos,
> tipo de colección y naturaleza de la operación. Usar System.nanoTime() o
> Instant/Duration para comparar empíricamente.
```

**Verificación:** El archivo resume los 6 temas de la lección (6.1–6.6) con referencias concretas a los ejercicios realizados.

---

### Paso 9: Crear la clase de demostración integrada

**Objetivo:** Proporcionar un punto de entrada único que ejecute los cuatro ejercicios en secuencia para verificación del instructor.

1. Crea `src/main/java/com/curso/parallelstreams/ParallelStreamsDemo.java`:

```java
package com.curso.parallelstreams;

public class ParallelStreamsDemo {

    public static void main(String[] args) {
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║  Lab 06-00-01: Parallel Streams y Fork/Join     ║");
        System.out.println("║  Procesadores: "
                + Runtime.getRuntime().availableProcessors()
                + "                                  ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();

        System.out.println("▶ Ejecutando Ejercicio 1...");
        Ex01_StreamComparison.main(args);
        System.out.println();

        System.out.println("▶ Ejecutando Ejercicio 2...");
        Ex02_Determinism.main(args);
        System.out.println();

        System.out.println("▶ Ejecutando Ejercicio 3...");
        Ex03_Reduce.main(args);
        System.out.println();

        System.out.println("▶ Ejecutando Ejercicio 4...");
        Ex04_ForkJoin.main(args);
        System.out.println();

        System.out.println("════════════════════════════════════════════════════");
        System.out.println("Laboratorio completado. Revise CONCLUSIONS.md");
        System.out.println("════════════════════════════════════════════════════");
    }
}
```

2. Compila y ejecuta la demostración completa:

```bash
mvn compile -q
java -cp target/classes com.curso.parallelstreams.ParallelStreamsDemo
```

**Salida esperada:** Los cuatro ejercicios se ejecutan secuencialmente con sus respectivos resultados.

**Verificación:** El instructor puede ejecutar una sola clase para validar todo el laboratorio.

---

### Paso 10: Verificar la estructura final del proyecto

**Objetivo:** Confirmar que todos los archivos están en su ubicación correcta.

1. Lista la estructura del proyecto:

```bash
find . -name "*.java" -o -name "*.xml" -o -name "*.md" | sort
```

**Salida esperada:**

```text
./CONCLUSIONS.md
./pom.xml
./src/main/java/com/curso/parallelstreams/Ex01_StreamComparison.java
./src/main/java/com/curso/parallelstreams/Ex02_Determinism.java
./src/main/java/com/curso/parallelstreams/Ex03_Reduce.java
./src/main/java/com/curso/parallelstreams/Ex04_ForkJoin.java
./src/main/java/com/curso/parallelstreams/ParallelStreamsDemo.java
./src/main/java/com/curso/parallelstreams/SumRecursiveTask.java
./src/main/java/com/curso/parallelstreams/Transaction.java
./src/main/java/com/curso/parallelstreams/TransactionFactory.java
```

2. Compila una última vez para confirmar que no hay errores:

```bash
mvn compile
```

**Salida esperada:**

```text
[INFO] BUILD SUCCESS
```

**Verificación:** 10 archivos fuente (8 `.java`, 1 `pom.xml`, 1 `CONCLUSIONS.md`). Todos compilan sin warnings bajo `--release 11`.

## Validación y Pruebas

Ejecute las siguientes verificaciones para confirmar que el laboratorio se completó correctamente:

| # | Criterio de validación | Comando / Acción | Resultado esperado |
|---|---|---|---|
| 1 | El proyecto compila sin errores | `mvn compile` | `BUILD SUCCESS` |
| 2 | Ejercicio 1: resultados idénticos seq/par | Ejecutar `Ex01_StreamComparison` | count y sum iguales en ambos modos |
| 3 | Ejercicio 1: speedup > 1.0x | Observar columna Speedup | Valor > 1.0 en máquina multinúcleo |
| 4 | Ejercicio 2: `findFirst()` determinística | 3 ejecuciones consecutivas | Mismo elemento retornado siempre |
| 5 | Ejercicio 2: `findAny()` no determinística | 3 ejecuciones consecutivas | Al menos 1 resultado diferente (probable) |
| 6 | Ejercicio 3a: reduce suma correcta | Comparar seq vs par | Diferencia < 0.01 |
| 7 | Ejercicio 3b: identidad incorrecta visible | Observar comas espurias | Concatenación paralela muestra `,` extra |
| 8 | Ejercicio 3c: reduce 3-arg correcto | Comparar con suma secuencial | Valores coinciden |
| 9 | Ejercicio 4: 3 sumas coinciden | `¿Todos los resultados coinciden?` | `true` |
| 10 | Ejercicio 4: pool personalizado usa 4 hilos | `Custom pool parallelism` | Valor = 4 |
| 11 | `CONCLUSIONS.md` existe y cubre 6 temas | `cat CONCLUSIONS.md` | Secciones beneficiosos/evitar presentes |
| 12 | `ParallelStreamsDemo` ejecuta los 4 ejercicios | Ejecutar clase | 4 secciones de salida visibles |

**Caso adverso (limitación de paralelismo):** Si la máquina tiene solo 2 núcleos, el speedup del Ejercicio 1 puede ser cercano a 1.0x o incluso < 1.0x. Esto no es un error; confirma que el overhead de coordinación supera el beneficio cuando hay pocos núcleos. Documente esta observación como evidencia del criterio 6.6 ("Cuándo evitar Parallel Streams").

## Solución de Problemas

### Problema 1: OutOfMemoryError al generar 10 millones de transacciones

**Síntoma:** Al ejecutar `Ex01_StreamComparison`, la JVM lanza:

```text
Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
```

**Causa:** El heap por defecto de la JVM (256 MB en algunas configuraciones) no es suficiente para almacenar 10 millones de objetos `Transaction` en un `ArrayList`. Cada `Transaction` ocupa aproximadamente 64–80 bytes, lo que requiere ~800 MB solo para la lista.

**Solución:** Incrementar el heap máximo de la JVM a 2 GB:

```bash
java -Xmx2g -cp target/classes com.curso.parallelstreams.Ex01_StreamComparison
```

Si la máquina tiene solo 8 GB de RAM y otros procesos consumen memoria, reduzca el dataset temporalmente editando la llamada en `Ex01_StreamComparison.main()`:

```java
List<Transaction> transactions = TransactionFactory.generate(5_000_000);
```

Tras verificar que funciona, restaure a `generate()` (10M) con `-Xmx2g`.

---

### Problema 2: El speedup paralelo es ≤ 1.0x (más lento que secuencial)

**Síntoma:** En el Ejercicio 1, el tiempo paralelo es igual o mayor al secuencial:

```text
Secuencial : 624,375 elementos en 85 ms
Paralelo   : 624,375 elementos en 120 ms
Speedup    : 0.71x
```

**Causa:** Esto puede ocurrir por múltiples razones:
- **Warm-up de la JVM:** La primera ejecución sufre el costo de compilación JIT. El stream secuencial se ejecuta primero y "calienta" la JVM, beneficiando al paralelo (o viceversa).
- **Pocos núcleos:** En máquinas con 2 núcleos, el overhead de Fork/Join domina.
- **Carga del sistema:** Otros procesos (IDE, navegador) consumen CPU.

**Solución:** Ejecute el benchmark múltiples veces para descartar efectos de warm-up. Añada una ejecución de calentamiento antes de medir:

```bash
## Ejecutar dos veces: la primera calienta, la segunda mide
java -Xmx2g -cp target/classes com.curso.parallelstreams.Ex01_StreamComparison
java -Xmx2g -cp target/classes com.curso.parallelstreams.Ex01_StreamComparison
```

Si el speedup sigue siendo ≤ 1.0x en una máquina de 2 núcleos, documente la observación en `CONCLUSIONS.md` como evidencia empírica de la lección 6.6.

## Limpieza

Este laboratorio usa un proyecto Maven independiente. Para liberar espacio en disco:

```bash
## Eliminar artefactos compilados (conserva el código fuente)
cd ~/cursos/parallel-streams-lab
mvn clean
```

Si desea eliminar completamente el proyecto (solo si ya no lo necesita):

```bash
cd ~/cursos
rm -rf parallel-streams-lab
```

> **Nota:** No elimine el proyecto principal `~/cursos/java-se11-labs`, que contiene los módulos 1–5 de laboratorios anteriores y será referenciado en laboratorios futuros.

## Resumen

En este laboratorio se cubrieron los seis temas del Capítulo 6:

| Tema | Ejercicio | Concepto clave demostrado |
|---|---|---|
| 6.1 Método `parallelStream()` | Ej. 1 | Creación y uso básico de streams paralelos |
| 6.2 `stream()` vs `parallelStream()` | Ej. 1 | Medición comparativa de tiempos con `Instant`/`Duration` y `System.nanoTime()` |
| 6.3 Operaciones determinísticas | Ej. 2 | `findFirst()` y `forEachOrdered()` preservan orden |
| 6.3 Operaciones no determinísticas | Ej. 2 | `findAny()` y `forEach()` no garantizan orden en paralelo |
| 6.4 Método `reduce` y parallelStreams | Ej. 3 | Identidad neutral, acumulador sin efectos, combinador asociativo |
| 6.5 Fork/Join Framework | Ej. 4 | `RecursiveTask`, `fork()`/`join()`, `ForkJoinPool` común y personalizado |
| 6.6 Cuándo utilizar/evitar | `CONCLUSIONS.md` | Criterios: tamaño, tipo de colección, CPU vs I/O, estado compartido |

**Conceptos fundamentales reforzados:**
- Los Parallel Streams utilizan internamente el Common `ForkJoinPool` con patrón work-stealing.
- El `Spliterator` de `ArrayList` divide eficientemente; el de `LinkedList` no.
- La corrección de `reduce` en paralelo depende de tres propiedades: identidad neutral, acumulador sin efectos secundarios y combinador asociativo.
- Medir empíricamente es imprescindible antes de decidir paralelizar.

**Conexión con laboratorios futuros:** Los conceptos de `ForkJoinPool` y concurrencia vistos aquí serán la base para el Lab 09-00-01 cuando se trabaje con `CompletableFuture` y pools de hilos personalizados.

### Recursos adicionales

- [Documentación oficial: java.util.stream (Java SE 11)](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/stream/package-summary.html)
- [Documentación oficial: ForkJoinPool (Java SE 11)](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/ForkJoinPool.html)
- [Documentación oficial: RecursiveTask (Java SE 11)](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/RecursiveTask.html)
- [Baeldung: Guide to Java Parallel Streams](https://www.baeldung.com/java-when-to-use-parallel-stream)
- [Java SE 11 Programmer II Study Guide — Boyarsky & Selikoff, McGraw-Hill, 2020](https://www.oreilly.com/library/view/ocp-oracle-certified/9781260135190/)
