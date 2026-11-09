# Práctica: Colecciones genéricas para gestión de datos

## Metadatos

| Campo            | Valor                                                      |
|------------------|------------------------------------------------------------|
| **Duración**     | 100 minutos                                                |
| **Complejidad**  | Media                                                      |
| **Nivel Bloom**  | Aplicar                                                    |
| **Módulo**       | 2 — Colecciones genéricas                                  |
| **Laboratorio**  | 02-00-01                                                   |

## Descripción General

El participante construirá un componente genérico de gestión de datos utilizando parámetros de tipo y comodines acotados, y lo integrará con las principales implementaciones de `List`, `Set` y `Map` para registrar, buscar, actualizar y eliminar elementos. Finalmente ordenará las colecciones mediante `Comparable` y `Comparator`, comparando el comportamiento de cada estructura y eligiendo la más adecuada según el escenario de uso.

El trabajo se realiza dentro del proyecto Maven `java-se11-labs` creado en el Laboratorio 01-00-01, extendiendo las entidades `Order` del Módulo 1 y creando el nuevo paquete `com.curso.modulo2`.

## Objetivos de Aprendizaje

- [ ] Definir clases y métodos genéricos con parámetros de tipo acotados (*bounded type parameters*) y comodines (`? extends T`, `? super T`) para garantizar la seguridad de tipos en tiempo de compilación.
- [ ] Comparar el comportamiento de `ArrayList`, `LinkedList`, `HashSet`, `LinkedHashSet`, `TreeSet`, `HashMap`, `LinkedHashMap` y `TreeMap` ante operaciones CRUD, duplicados y ordenamiento.
- [ ] Implementar las interfaces `Comparable<T>` y `Comparator<T>` en las entidades del dominio para definir ordenamientos naturales y alternativos sobre las colecciones.
- [ ] Integrar el componente `DataRepository<T>` con las colecciones anteriores y ejecutar escenarios completos desde la clase `DataRepositoryDemo`.

## Prerrequisitos

| Tipo               | Detalle                                                                                       |
|--------------------|-----------------------------------------------------------------------------------------------|
| **Laboratorio**    | Lab 01-00-01 completado: proyecto Maven `java-se11-labs` con el dominio `Order` funcional.    |
| **Conocimiento**   | Conceptos de POO (herencia, polimorfismo), clases genéricas básicas, estructuras de datos.     |
| **Software**       | JDK Eclipse Temurin 11.0.23+9, Apache Maven 3.9.8, IntelliJ IDEA Community 2024.1.4, Git 2.45.2. |
| **Directorio**     | `~/cursos/java-se11-labs` (Linux/macOS) o `C:\cursos\java-se11-labs` (Windows).               |

## Entorno de Laboratorio

| Componente         | Versión / Detalle                                                                                  |
|--------------------|----------------------------------------------------------------------------------------------------|
| JDK                | Eclipse Temurin 11.0.23+9 — https://adoptium.net/temurin/releases/?version=11                      |
| Maven              | 3.9.8 — https://maven.apache.org/download.cgi                                                     |
| IntelliJ IDEA CE   | 2024.1.4 — https://www.jetbrains.com/idea/download/                                               |
| Sistema operativo  | Linux, macOS o Windows 10/11 (64 bits)                                                             |
| RAM mínima         | 8 GB (16 GB recomendado)                                                                           |

Verifica que el proyecto compila antes de iniciar:

```bash
cd ~/cursos/java-se11-labs
mvn compile -q
```

Salida esperada: `BUILD SUCCESS` sin errores.

## Instrucciones Paso a Paso

### Paso 1: Crear la estructura de paquetes del Módulo 2

**Objetivo:** Preparar el paquete `com.curso.modulo2` sin alterar los paquetes existentes del Módulo 1.

1. Desde la terminal, crea el directorio de paquetes:

```bash
mkdir -p src/main/java/com/curso/modulo2
```

2. Verifica la estructura resultante:

```bash
find src/main/java/com/curso -type d | sort
```

**Salida esperada:**

```
src/main/java/com/curso
src/main/java/com/curso/modulo1
src/main/java/com/curso/modulo2
```

**Verificación:** Ambos paquetes coexisten. El paquete `modulo1` permanece intacto.

---

### Paso 2: Crear la entidad Product

**Objetivo:** Definir una entidad `Product` que se utilizará en conjuntos (`Set`) para demostrar el manejo de duplicados y ordenamiento.

1. Crea el archivo `src/main/java/com/curso/modulo2/Product.java`:

```java
package com.curso.modulo2;

import java.util.Objects;

public class Product implements Comparable<Product> {

    private int id;
    private String name;
    private double price;
    private String category;

    public Product(int id, String name, double price, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
    }

    // --- Getters y Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    // --- equals/hashCode basados en id ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return id == product.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // --- Ordenamiento natural: por nombre ascendente ---
    @Override
    public int compareTo(Product other) {
        return this.name.compareTo(other.name);
    }

    @Override
    public String toString() {
        return String.format("Product{id=%d, name='%s', price=%.2f, category='%s'}",
                id, name, price, category);
    }
}
```

2. Compila para verificar:

```bash
mvn compile -q
```

**Verificación:** `BUILD SUCCESS`. La clase implementa `Comparable<Product>` con ordenamiento natural por nombre.

---

### Paso 3: Extender la entidad Order con Comparable

**Objetivo:** Añadir la interfaz `Comparable<Order>` a la clase `Order` del Módulo 1 para definir un ordenamiento natural por fecha de creación, sin romper el contrato existente.

1. Abre `src/main/java/com/curso/modulo1/Order.java` y realiza las siguientes modificaciones. Primero, añade el import necesario y modifica la declaración de la clase para implementar `Comparable<Order>`:

```java
package com.curso.modulo1;

import java.time.LocalDateTime;
import java.util.Objects;
```

2. Modifica la línea de declaración de la clase para incluir `Comparable<Order>` (conserva las demás interfaces que ya implementa):

```java
public class Order implements Processable, Auditable, Comparable<Order> {
```

3. Añade el campo `createdAt` y su getter si no existen, además de los campos `id`, `customerName`, `totalAmount` y `status` que ya deben estar presentes. Agrega los siguientes campos y métodos **solo si no existen** en tu clase actual:

```java
    private int id;
    private String customerName;
    private double totalAmount;
    private String status;
    private LocalDateTime createdAt;

    public Order(int id, String customerName, double totalAmount, String status) {
        this.id = id;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = LocalDateTime.now();
    }

    // Constructor con fecha explícita (para pruebas de ordenamiento)
    public Order(int id, String customerName, double totalAmount,
                 String status, LocalDateTime createdAt) {
        this.id = id;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
```

4. Añade las implementaciones de `equals`, `hashCode`, `compareTo` y `toString`:

```java
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Order)) return false;
        Order order = (Order) o;
        return id == order.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // Ordenamiento natural: por fecha de creación ascendente
    @Override
    public int compareTo(Order other) {
        return this.createdAt.compareTo(other.createdAt);
    }

    @Override
    public String toString() {
        return String.format("Order{id=%d, customer='%s', total=%.2f, status='%s', created=%s}",
                id, customerName, totalAmount, status, createdAt);
    }
```

5. Compila el proyecto completo para asegurar que la clase `Order` modificada no rompe las clases del Módulo 1:

```bash
mvn compile -q
```

**Verificación:** `BUILD SUCCESS`. La clase `Order` ahora implementa `Comparable<Order>` con ordenamiento natural por `createdAt`.

---

### Paso 4: Crear la interfaz genérica Identifiable

**Objetivo:** Definir un contrato genérico que permita a `DataRepository` identificar elementos por su `id`, usando un parámetro de tipo acotado.

1. Crea `src/main/java/com/curso/modulo2/Identifiable.java`:

```java
package com.curso.modulo2;

/**
 * Contrato que deben cumplir las entidades gestionadas por DataRepository.
 * El parámetro T representa el tipo del identificador.
 */
public interface Identifiable<T> {
    T getIdentifier();
}
```

2. Haz que `Product` implemente `Identifiable<Integer>`. Modifica la declaración de la clase en `Product.java`:

```java
public class Product implements Comparable<Product>, Identifiable<Integer> {
```

3. Añade el método requerido por la interfaz al final de la clase `Product`:

```java
    @Override
    public Integer getIdentifier() {
        return this.id;
    }
```

4. Crea un adaptador para `Order` en el paquete `modulo2`. Crea `src/main/java/com/curso/modulo2/OrderAdapter.java`:

```java
package com.curso.modulo2;

import com.curso.modulo1.Order;

/**
 * Envuelve un Order del Módulo 1 para hacerlo compatible con Identifiable.
 */
public class OrderAdapter implements Identifiable<Integer> {

    private final Order order;

    public OrderAdapter(Order order) {
        this.order = order;
    }

    @Override
    public Integer getIdentifier() {
        return order.getId();
    }

    public Order getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return order.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OrderAdapter)) return false;
        return order.equals(((OrderAdapter) o).order);
    }

    @Override
    public int hashCode() {
        return order.hashCode();
    }
}
```

5. Compila:

```bash
mvn compile -q
```

**Verificación:** `BUILD SUCCESS`. Tanto `Product` como `OrderAdapter` implementan `Identifiable<Integer>`.

---

### Paso 5: Construir la clase genérica DataRepository

**Objetivo:** Implementar un repositorio genérico con operaciones CRUD que acepte cualquier tipo que implemente `Identifiable<Integer>`, demostrando parámetros de tipo acotados.

1. Crea `src/main/java/com/curso/modulo2/DataRepository.java`:

```java
package com.curso.modulo2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio genérico con operaciones CRUD.
 * T debe implementar Identifiable<Integer> para garantizar
 * que cada elemento tiene un identificador entero.
 *
 * @param <T> tipo de elemento gestionado
 */
public class DataRepository<T extends Identifiable<Integer>> {

    private final List<T> elements = new ArrayList<>();

    /**
     * Agrega un elemento. Rechaza duplicados por identificador.
     */
    public boolean add(T element) {
        if (findById(element.getIdentifier()).isPresent()) {
            System.out.println("  [WARN] Elemento con id="
                    + element.getIdentifier() + " ya existe. No se agregó.");
            return false;
        }
        elements.add(element);
        return true;
    }

    /**
     * Elimina un elemento por referencia (usa equals).
     */
    public boolean remove(T element) {
        return elements.remove(element);
    }

    /**
     * Busca un elemento por su identificador.
     */
    public Optional<T> findById(int id) {
        for (T e : elements) {
            if (e.getIdentifier().equals(id)) {
                return Optional.of(e);
            }
        }
        return Optional.empty();
    }

    /**
     * Actualiza un elemento: reemplaza el existente con el mismo id.
     */
    public boolean update(T element) {
        for (int i = 0; i < elements.size(); i++) {
            if (elements.get(i).getIdentifier().equals(element.getIdentifier())) {
                elements.set(i, element);
                return true;
            }
        }
        return false;
    }

    /**
     * Retorna una copia inmutable de todos los elementos.
     */
    public List<T> listAll() {
        return Collections.unmodifiableList(new ArrayList<>(elements));
    }

    /**
     * Retorna el número de elementos.
     */
    public int size() {
        return elements.size();
    }
}
```

2. Compila:

```bash
mvn compile -q
```

**Verificación:** `BUILD SUCCESS`. La clase `DataRepository<T extends Identifiable<Integer>>` demuestra un *bounded type parameter*.

---

### Paso 6: Crear un método utilitario con comodines acotados

**Objetivo:** Practicar comodines `? extends` y `? super` en métodos estáticos genéricos, aplicando el principio PECS (*Producer Extends, Consumer Super*).

1. Crea `src/main/java/com/curso/modulo2/RepositoryUtils.java`:

```java
package com.curso.modulo2;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilidades genéricas que demuestran comodines acotados (PECS).
 */
public class RepositoryUtils {

    /**
     * Copia elementos de un repositorio (productor: ? extends T)
     * a una lista destino (consumidor: ? super T).
     */
    public static <T extends Identifiable<Integer>> void copyToList(
            DataRepository<? extends T> source,
            List<? super T> destination) {

        for (T element : source.listAll()) {
            destination.add(element);
        }
    }

    /**
     * Cuenta elementos cuyo identificador está dentro de un rango.
     * Acepta cualquier repositorio de Identifiable<Integer>.
     */
    public static long countInRange(
            DataRepository<? extends Identifiable<Integer>> repo,
            int minIdInclusive,
            int maxIdInclusive) {

        long count = 0;
        for (Identifiable<Integer> elem : repo.listAll()) {
            int id = elem.getIdentifier();
            if (id >= minIdInclusive && id <= maxIdInclusive) {
                count++;
            }
        }
        return count;
    }

    /**
     * Fusiona dos repositorios en una nueva lista.
     */
    public static <T extends Identifiable<Integer>> List<T> merge(
            DataRepository<? extends T> repo1,
            DataRepository<? extends T> repo2) {

        List<T> merged = new ArrayList<>();
        copyToList(repo1, merged);
        copyToList(repo2, merged);
        return merged;
    }
}
```

2. Compila:

```bash
mvn compile -q
```

**Verificación:** `BUILD SUCCESS`. Los métodos usan `? extends T` para lectura y `? super T` para escritura.

---

### Paso 7: Demostrar CRUD con ArrayList (lista de pedidos)

**Objetivo:** Usar `DataRepository` con `OrderAdapter` respaldado por `ArrayList` internamente, y verificar operaciones CRUD con acceso por índice.

1. Crea `src/main/java/com/curso/modulo2/DataRepositoryDemo.java` con el esqueleto inicial:

```java
package com.curso.modulo2;

import com.curso.modulo1.Order;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Clase de demostración que ejecuta todos los escenarios del Módulo 2.
 * Punto de entrada para verificación del instructor.
 */
public class DataRepositoryDemo {

    public static void main(String[] args) {
        System.out.println("=== MÓDULO 2: Colecciones Genéricas ===\n");

        demoCrudArrayList();
        demoHashSetDuplicados();
        demoLinkedHashMapRegistro();
        demoComparableYComparator();
        demoTreeSetConComparator();
        demoComodinesAcotados();

        System.out.println("\n=== FIN DE LA DEMOSTRACIÓN ===");
    }

    // --- Sección 1: CRUD con ArrayList ---
    private static void demoCrudArrayList() {
        System.out.println("--- 1. CRUD con DataRepository<OrderAdapter> (ArrayList interno) ---");

        DataRepository<OrderAdapter> orderRepo = new DataRepository<>();

        // Crear pedidos con fechas distintas para ordenamiento posterior
        Order o1 = new Order(1, "Ana García", 250.00, "PENDING",
                LocalDateTime.of(2024, 6, 15, 10, 30));
        Order o2 = new Order(2, "Luis Martínez", 180.50, "SHIPPED",
                LocalDateTime.of(2024, 6, 14, 8, 0));
        Order o3 = new Order(3, "María López", 320.75, "DELIVERED",
                LocalDateTime.of(2024, 6, 16, 14, 45));
        Order o4 = new Order(4, "Carlos Ruiz", 95.00, "PENDING",
                LocalDateTime.of(2024, 6, 13, 9, 15));

        // ADD
        orderRepo.add(new OrderAdapter(o1));
        orderRepo.add(new OrderAdapter(o2));
        orderRepo.add(new OrderAdapter(o3));
        orderRepo.add(new OrderAdapter(o4));

        System.out.println("Elementos tras agregar 4 pedidos: " + orderRepo.size());

        // Intentar agregar duplicado
        orderRepo.add(new OrderAdapter(o1));

        // FIND BY ID
        orderRepo.findById(2).ifPresent(oa ->
                System.out.println("Encontrado id=2: " + oa));

        // UPDATE
        Order o2Updated = new Order(2, "Luis Martínez", 199.99, "DELIVERED",
                LocalDateTime.of(2024, 6, 14, 8, 0));
        boolean updated = orderRepo.update(new OrderAdapter(o2Updated));
        System.out.println("Actualizado id=2: " + updated);

        // REMOVE
        boolean removed = orderRepo.remove(new OrderAdapter(o4));
        System.out.println("Eliminado id=4: " + removed);
        System.out.println("Elementos tras eliminar: " + orderRepo.size());

        // LIST ALL
        System.out.println("Listado completo:");
        orderRepo.listAll().forEach(oa -> System.out.println("  " + oa));
        System.out.println();
    }

    // Métodos placeholder — se completarán en los siguientes pasos
    private static void demoHashSetDuplicados() {}
    private static void demoLinkedHashMapRegistro() {}
    private static void demoComparableYComparator() {}
    private static void demoTreeSetConComparator() {}
    private static void demoComodinesAcotados() {}
}
```

2. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.curso.modulo2.DataRepositoryDemo
```

**Salida esperada:**

```
=== MÓDULO 2: Colecciones Genéricas ===

--- 1. CRUD con DataRepository<OrderAdapter> (ArrayList interno) ---
Elementos tras agregar 4 pedidos: 4
  [WARN] Elemento con id=1 ya existe. No se agregó.
Encontrado id=2: Order{id=2, customer='Luis Martínez', total=180.50, status='SHIPPED', created=2024-06-14T08:00}
Actualizado id=2: true
Eliminado id=4: true
Elementos tras eliminar: 3
Listado completo:
  Order{id=1, customer='Ana García', total=250.00, status='PENDING', created=2024-06-15T10:30}
  Order{id=2, customer='Luis Martínez', total=199.99, status='DELIVERED', created=2024-06-14T08:00}
  Order{id=3, customer='María López', total=320.75, status='DELIVERED', created=2024-06-16T14:45}

=== FIN DE LA DEMOSTRACIÓN ===
```

**Verificación:** El repositorio rechaza duplicados, actualiza, elimina y lista correctamente. El tamaño final es 3.

---

### Paso 8: Demostrar HashSet con productos (sin duplicados)

**Objetivo:** Comparar `HashSet`, `LinkedHashSet` y `TreeSet` con la entidad `Product`, observando cómo cada implementación trata duplicados y orden.

1. Reemplaza el método `demoHashSetDuplicados()` en `DataRepositoryDemo.java`:

```java
    private static void demoHashSetDuplicados() {
        System.out.println("--- 2. HashSet / LinkedHashSet / TreeSet con Product ---");

        Product p1 = new Product(101, "Teclado Mecánico", 89.99, "Periféricos");
        Product p2 = new Product(102, "Monitor 27\"", 349.00, "Monitores");
        Product p3 = new Product(103, "Ratón Inalámbrico", 45.50, "Periféricos");
        Product p4 = new Product(101, "Teclado Mecánico (dup)", 89.99, "Periféricos"); // mismo id

        // --- HashSet: sin orden garantizado, sin duplicados ---
        Set<Product> hashSet = new HashSet<>();
        hashSet.add(p1);
        hashSet.add(p2);
        hashSet.add(p3);
        boolean dupAdded = hashSet.add(p4); // p4.id == p1.id → equals true
        System.out.println("HashSet - ¿duplicado agregado? " + dupAdded);
        System.out.println("HashSet - tamaño: " + hashSet.size());
        System.out.println("HashSet (orden NO garantizado):");
        hashSet.forEach(p -> System.out.println("  " + p));

        // --- LinkedHashSet: orden de inserción, sin duplicados ---
        Set<Product> linkedHashSet = new LinkedHashSet<>();
        linkedHashSet.add(p1);
        linkedHashSet.add(p2);
        linkedHashSet.add(p3);
        linkedHashSet.add(p4);
        System.out.println("LinkedHashSet - tamaño: " + linkedHashSet.size());
        System.out.println("LinkedHashSet (orden de inserción):");
        linkedHashSet.forEach(p -> System.out.println("  " + p));

        // --- TreeSet: orden natural (compareTo → por nombre) ---
        Set<Product> treeSet = new TreeSet<>();
        treeSet.add(p1);
        treeSet.add(p2);
        treeSet.add(p3);
        System.out.println("TreeSet - tamaño: " + treeSet.size());
        System.out.println("TreeSet (orden natural por nombre):");
        treeSet.forEach(p -> System.out.println("  " + p));
        System.out.println();
    }
```

2. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.curso.modulo2.DataRepositoryDemo
```

**Salida esperada (sección 2):**

```
--- 2. HashSet / LinkedHashSet / TreeSet con Product ---
HashSet - ¿duplicado agregado? false
HashSet - tamaño: 3
HashSet (orden NO garantizado):
  Product{id=103, name='Ratón Inalámbrico', price=45.50, category='Periféricos'}
  Product{id=101, name='Teclado Mecánico', price=89.99, category='Periféricos'}
  Product{id=102, name='Monitor 27"', price=349.00, category='Monitores'}
LinkedHashSet - tamaño: 3
LinkedHashSet (orden de inserción):
  Product{id=101, name='Teclado Mecánico', price=89.99, category='Periféricos'}
  Product{id=102, name='Monitor 27"', price=349.00, category='Monitores'}
  Product{id=103, name='Ratón Inalámbrico', price=45.50, category='Periféricos'}
TreeSet (orden natural por nombre):
  Product{id=102, name='Monitor 27"', price=349.00, category='Monitores'}
  Product{id=103, name='Ratón Inalámbrico', price=45.50, category='Periféricos'}
  Product{id=101, name='Teclado Mecánico', price=89.99, category='Periféricos'}
```

**Verificación:** `HashSet` rechaza el duplicado (`false`). `LinkedHashSet` preserva orden de inserción. `TreeSet` ordena alfabéticamente por nombre (resultado de `compareTo`). Los tres conjuntos tienen tamaño 3.

> **Nota:** El orden exacto del `HashSet` puede variar entre ejecuciones; esto es esperado.

---

### Paso 9: Demostrar LinkedHashMap para registro indexado de pedidos

**Objetivo:** Usar `LinkedHashMap<String, Order>` para un registro de pedidos indexados por código, y comparar con `HashMap` y `TreeMap`.

1. Reemplaza el método `demoLinkedHashMapRegistro()`:

```java
    private static void demoLinkedHashMapRegistro() {
        System.out.println("--- 3. HashMap / LinkedHashMap / TreeMap con Order ---");

        Order o1 = new Order(1, "Ana García", 250.00, "PENDING",
                LocalDateTime.of(2024, 6, 15, 10, 30));
        Order o2 = new Order(2, "Luis Martínez", 180.50, "SHIPPED",
                LocalDateTime.of(2024, 6, 14, 8, 0));
        Order o3 = new Order(3, "María López", 320.75, "DELIVERED",
                LocalDateTime.of(2024, 6, 16, 14, 45));

        // --- HashMap: sin orden garantizado ---
        Map<String, Order> hashMap = new HashMap<>();
        hashMap.put("ORD-003", o3);
        hashMap.put("ORD-001", o1);
        hashMap.put("ORD-002", o2);
        System.out.println("HashMap (orden NO garantizado):");
        hashMap.forEach((k, v) -> System.out.println("  " + k + " → " + v));

        // --- LinkedHashMap: orden de inserción ---
        Map<String, Order> linkedHashMap = new LinkedHashMap<>();
        linkedHashMap.put("ORD-003", o3);
        linkedHashMap.put("ORD-001", o1);
        linkedHashMap.put("ORD-002", o2);
        System.out.println("LinkedHashMap (orden de inserción):");
        linkedHashMap.forEach((k, v) -> System.out.println("  " + k + " → " + v));

        // --- TreeMap: orden natural de las claves (String → lexicográfico) ---
        Map<String, Order> treeMap = new TreeMap<>();
        treeMap.put("ORD-003", o3);
        treeMap.put("ORD-001", o1);
        treeMap.put("ORD-002", o2);
        System.out.println("TreeMap (orden natural de clave):");
        treeMap.forEach((k, v) -> System.out.println("  " + k + " → " + v));

        // CRUD en LinkedHashMap
        System.out.println("Buscar ORD-002: " + linkedHashMap.get("ORD-002"));
        linkedHashMap.put("ORD-002",
                new Order(2, "Luis Martínez", 199.99, "DELIVERED",
                        LocalDateTime.of(2024, 6, 14, 8, 0)));
        System.out.println("Tras actualizar ORD-002: " + linkedHashMap.get("ORD-002"));
        linkedHashMap.remove("ORD-003");
        System.out.println("Tras eliminar ORD-003, tamaño: " + linkedHashMap.size());
        System.out.println();
    }
```

2. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.curso.modulo2.DataRepositoryDemo
```

**Salida esperada (sección 3):**

```
--- 3. HashMap / LinkedHashMap / TreeMap con Order ---
HashMap (orden NO garantizado):
  ORD-001 → Order{id=1, ...}
  ORD-002 → Order{id=2, ...}
  ORD-003 → Order{id=3, ...}
LinkedHashMap (orden de inserción):
  ORD-003 → Order{id=3, ...}
  ORD-001 → Order{id=1, ...}
  ORD-002 → Order{id=2, ...}
TreeMap (orden natural de clave):
  ORD-001 → Order{id=1, ...}
  ORD-002 → Order{id=2, ...}
  ORD-003 → Order{id=3, ...}
Buscar ORD-002: Order{id=2, ...}
Tras actualizar ORD-002: Order{id=2, customer='Luis Martínez', total=199.99, status='DELIVERED', ...}
Tras eliminar ORD-003, tamaño: 2
```

**Verificación:** `LinkedHashMap` conserva el orden de inserción (ORD-003 primero). `TreeMap` ordena claves lexicográficamente. Las operaciones CRUD funcionan correctamente.

---

### Paso 10: Crear Comparators externos para Order

**Objetivo:** Implementar dos `Comparator` externos que permitan ordenar pedidos por monto total y por estado, complementando el ordenamiento natural por fecha.

1. Crea `src/main/java/com/curso/modulo2/OrderComparators.java`:

```java
package com.curso.modulo2;

import com.curso.modulo1.Order;

import java.util.Comparator;

/**
 * Comparadores alternativos para Order.
 * Complementan el ordenamiento natural (Comparable → por createdAt).
 */
public final class OrderComparators {

    private OrderComparators() {
        // Clase utilitaria, no instanciable
    }

    /**
     * Ordena por monto total ascendente.
     */
    public static final Comparator<Order> BY_TOTAL_ASC =
            Comparator.comparingDouble(Order::getTotalAmount);

    /**
     * Ordena por monto total descendente.
     */
    public static final Comparator<Order> BY_TOTAL_DESC =
            BY_TOTAL_ASC.reversed();

    /**
     * Ordena por estado del pedido (alfabético) y luego por monto descendente.
     */
    public static final Comparator<Order> BY_STATUS_THEN_TOTAL =
            Comparator.comparing(Order::getStatus)
                    .thenComparing(BY_TOTAL_DESC);

    /**
     * Ordena por nombre del cliente (alfabético ascendente).
     */
    public static final Comparator<Order> BY_CUSTOMER_NAME =
            Comparator.comparing(Order::getCustomerName);
}
```

2. Compila:

```bash
mvn compile -q
```

**Verificación:** `BUILD SUCCESS`. Se definen cuatro comparadores usando la API fluida de `Comparator` (composición con `thenComparing` y `reversed`).

---

### Paso 11: Demostrar ordenamiento con Comparable y Comparator

**Objetivo:** Ordenar una lista de `Order` usando `Collections.sort()` (natural), `List.sort()` (con Comparator) y comparar resultados.

1. Reemplaza el método `demoComparableYComparator()`:

```java
    private static void demoComparableYComparator() {
        System.out.println("--- 4. Ordenamiento con Comparable y Comparator ---");

        List<Order> orders = new ArrayList<>(List.of(
                new Order(1, "Ana García", 250.00, "PENDING",
                        LocalDateTime.of(2024, 6, 15, 10, 30)),
                new Order(2, "Luis Martínez", 180.50, "SHIPPED",
                        LocalDateTime.of(2024, 6, 14, 8, 0)),
                new Order(3, "María López", 320.75, "DELIVERED",
                        LocalDateTime.of(2024, 6, 16, 14, 45)),
                new Order(4, "Carlos Ruiz", 95.00, "PENDING",
                        LocalDateTime.of(2024, 6, 13, 9, 15)),
                new Order(5, "Elena Torres", 180.50, "DELIVERED",
                        LocalDateTime.of(2024, 6, 12, 16, 0))
        ));

        // 4a. Ordenamiento natural (Comparable → por createdAt)
        Collections.sort(orders);
        System.out.println("Orden natural (por fecha de creación):");
        orders.forEach(o -> System.out.println("  " + o.getCreatedAt()
                + " | id=" + o.getId() + " | " + o.getCustomerName()));

        // 4b. Ordenamiento por monto total ascendente (Comparator)
        orders.sort(OrderComparators.BY_TOTAL_ASC);
        System.out.println("Por monto total (ascendente):");
        orders.forEach(o -> System.out.println("  $" + o.getTotalAmount()
                + " | id=" + o.getId() + " | " + o.getCustomerName()));

        // 4c. Ordenamiento por estado y luego monto descendente
        orders.sort(OrderComparators.BY_STATUS_THEN_TOTAL);
        System.out.println("Por estado, luego monto (descendente):");
        orders.forEach(o -> System.out.println("  " + o.getStatus()
                + " | $" + o.getTotalAmount()
                + " | id=" + o.getId()));

        // 4d. Ordenamiento por nombre del cliente
        orders.sort(OrderComparators.BY_CUSTOMER_NAME);
        System.out.println("Por nombre del cliente:");
        orders.forEach(o -> System.out.println("  " + o.getCustomerName()
                + " | id=" + o.getId()));
        System.out.println();
    }
```

2. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.curso.modulo2.DataRepositoryDemo
```

**Salida esperada (sección 4):**

```
--- 4. Ordenamiento con Comparable y Comparator ---
Orden natural (por fecha de creación):
  2024-06-12T16:00 | id=5 | Elena Torres
  2024-06-13T09:15 | id=4 | Carlos Ruiz
  2024-06-14T08:00 | id=2 | Luis Martínez
  2024-06-15T10:30 | id=1 | Ana García
  2024-06-16T14:45 | id=3 | María López
Por monto total (ascendente):
  $95.0 | id=4 | Carlos Ruiz
  $180.5 | id=2 | Luis Martínez
  $180.5 | id=5 | Elena Torres
  $250.0 | id=1 | Ana García
  $320.75 | id=3 | María López
Por estado, luego monto (descendente):
  DELIVERED | $320.75 | id=3
  DELIVERED | $180.5 | id=5
  PENDING | $250.0 | id=1
  PENDING | $95.0 | id=4
  SHIPPED | $180.5 | id=2
Por nombre del cliente:
  Ana García | id=1
  Carlos Ruiz | id=4
  Elena Torres | id=5
  Luis Martínez | id=2
  María López | id=3
```

**Verificación:** Cada bloque muestra un criterio de ordenamiento diferente. `Collections.sort()` usa `compareTo` (natural). `List.sort()` acepta un `Comparator`. El ordenamiento compuesto (estado + monto) agrupa correctamente.

---

### Paso 12: Demostrar TreeSet con Comparator personalizado

**Objetivo:** Crear un `TreeSet` con un `Comparator` externo para ordenar `Order` por monto total en lugar del orden natural.

1. Reemplaza el método `demoTreeSetConComparator()`:

```java
    private static void demoTreeSetConComparator() {
        System.out.println("--- 5. TreeSet con Comparator personalizado ---");

        // TreeSet con orden por monto total descendente
        // NOTA: TreeSet usa el Comparator para determinar igualdad,
        // por lo que dos pedidos con el mismo monto se consideran "iguales".
        // Usamos thenComparing(Order::getId) para desambiguar.
        Comparator<Order> byTotalDescThenId =
                Comparator.comparingDouble(Order::getTotalAmount).reversed()
                        .thenComparingInt(Order::getId);

        Set<Order> treeSet = new TreeSet<>(byTotalDescThenId);

        treeSet.add(new Order(1, "Ana García", 250.00, "PENDING",
                LocalDateTime.of(2024, 6, 15, 10, 30)));
        treeSet.add(new Order(2, "Luis Martínez", 180.50, "SHIPPED",
                LocalDateTime.of(2024, 6, 14, 8, 0)));
        treeSet.add(new Order(3, "María López", 320.75, "DELIVERED",
                LocalDateTime.of(2024, 6, 16, 14, 45)));
        treeSet.add(new Order(4, "Carlos Ruiz", 95.00, "PENDING",
                LocalDateTime.of(2024, 6, 13, 9, 15)));
        treeSet.add(new Order(5, "Elena Torres", 180.50, "DELIVERED",
                LocalDateTime.of(2024, 6, 12, 16, 0)));

        System.out.println("TreeSet (monto descendente, desambiguado por id):");
        treeSet.forEach(o -> System.out.println("  $" + o.getTotalAmount()
                + " | id=" + o.getId() + " | " + o.getCustomerName()));

        System.out.println("Tamaño: " + treeSet.size());

        // Comparar con TreeSet usando orden natural (Comparable → por fecha)
        Set<Order> treeSetNatural = new TreeSet<>();
        treeSetNatural.addAll(treeSet);
        System.out.println("TreeSet natural (por fecha de creación):");
        treeSetNatural.forEach(o -> System.out.println("  " + o.getCreatedAt()
                + " | id=" + o.getId()));
        System.out.println();
    }
```

2. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.curso.modulo2.DataRepositoryDemo
```

**Salida esperada (sección 5):**

```
--- 5. TreeSet con Comparator personalizado ---
TreeSet (monto descendente, desambiguado por id):
  $320.75 | id=3 | María López
  $250.0 | id=1 | Ana García
  $180.5 | id=2 | Luis Martínez
  $180.5 | id=5 | Elena Torres
  $95.0 | id=4 | Carlos Ruiz
Tamaño: 5
TreeSet natural (por fecha de creación):
  2024-06-12T16:00 | id=5
  2024-06-13T09:15 | id=4
  2024-06-14T08:00 | id=2
  2024-06-15T10:30 | id=1
  2024-06-16T14:45 | id=3
```

**Verificación:** El `TreeSet` con `Comparator` personalizado conserva los 5 elementos (gracias a `thenComparingInt`). Sin el desambiguador, los pedidos con monto 180.50 se habrían tratado como duplicados. El `TreeSet` sin `Comparator` usa el orden natural por fecha.

---

### Paso 13: Demostrar comodines acotados con RepositoryUtils

**Objetivo:** Ejecutar los métodos utilitarios que usan `? extends` y `? super` para transferir y contar elementos entre repositorios.

1. Reemplaza el método `demoComodinesAcotados()`:

```java
    private static void demoComodinesAcotados() {
        System.out.println("--- 6. Comodines acotados (PECS) con RepositoryUtils ---");

        // Repositorio de productos
        DataRepository<Product> productRepo = new DataRepository<>();
        productRepo.add(new Product(201, "SSD 1TB", 120.00, "Almacenamiento"));
        productRepo.add(new Product(202, "RAM 16GB", 65.00, "Memoria"));
        productRepo.add(new Product(203, "CPU Ryzen 5", 210.00, "Procesadores"));

        // Copiar a una lista usando comodín ? super T
        List<Identifiable<Integer>> destino = new ArrayList<>();
        RepositoryUtils.copyToList(productRepo, destino);
        System.out.println("Elementos copiados a lista genérica:");
        destino.forEach(e -> System.out.println("  " + e));

        // Contar elementos en rango de id
        long count = RepositoryUtils.countInRange(productRepo, 201, 202);
        System.out.println("Productos con id entre 201 y 202: " + count);

        // Crear segundo repositorio y fusionar
        DataRepository<Product> productRepo2 = new DataRepository<>();
        productRepo2.add(new Product(204, "GPU RTX 3060", 450.00, "Gráficas"));
        productRepo2.add(new Product(205, "Fuente 750W", 85.00, "Alimentación"));

        List<Product> merged = RepositoryUtils.merge(productRepo, productRepo2);
        System.out.println("Fusión de repositorios (" + merged.size() + " elementos):");
        merged.forEach(p -> System.out.println("  " + p));
        System.out.println();
    }
```

2. Compila y ejecuta la demostración completa:

```bash
mvn compile -q
java -cp target/classes com.curso.modulo2.DataRepositoryDemo
```

**Salida esperada (sección 6):**

```
--- 6. Comodines acotados (PECS) con RepositoryUtils ---
Elementos copiados a lista genérica:
  Product{id=201, name='SSD 1TB', price=120.00, category='Almacenamiento'}
  Product{id=202, name='RAM 16GB', price=65.00, category='Memoria'}
  Product{id=203, name='CPU Ryzen 5', price=210.00, category='Procesadores'}
Productos con id entre 201 y 202: 2
Fusión de repositorios (5 elementos):
  Product{id=201, name='SSD 1TB', price=120.00, category='Almacenamiento'}
  Product{id=202, name='RAM 16GB', price=65.00, category='Memoria'}
  Product{id=203, name='CPU Ryzen 5', price=210.00, category='Procesadores'}
  Product{id=204, name='GPU RTX 3060', price=450.00, category='Gráficas'}
  Product{id=205, name='Fuente 750W', price=85.00, category='Alimentación'}
```

**Verificación:** `copyToList` acepta `List<Identifiable<Integer>>` como destino (comodín `? super T`). `merge` combina dos repositorios en una lista tipada. `countInRange` filtra correctamente por rango de identificador.

---

### Paso 14: Agregar comentarios de justificación de estructura

**Objetivo:** Documentar en la clase `DataRepositoryDemo` la justificación técnica de la elección de cada estructura de datos, consolidando el aprendizaje.

1. Añade el siguiente bloque de comentarios al inicio de la clase `DataRepositoryDemo`, justo después de la declaración del paquete y los imports:

```java
/**
 * Demostración del Módulo 2: Colecciones Genéricas.
 *
 * JUSTIFICACIÓN DE ESTRUCTURAS:
 *
 * 1. ArrayList (dentro de DataRepository):
 *    - Acceso O(1) por índice, ideal para listados y búsquedas secuenciales.
 *    - Inserción al final O(1) amortizado.
 *    - Elegido porque el patrón CRUD principal es agregar al final y recorrer.
 *
 * 2. HashSet (catálogo de productos):
 *    - O(1) para add/remove/contains.
 *    - Rechaza duplicados basándose en equals()/hashCode().
 *    - Elegido cuando no importa el orden y se necesita unicidad rápida.
 *
 * 3. LinkedHashSet:
 *    - Mismas garantías que HashSet + preserva orden de inserción.
 *    - Elegido cuando se necesita unicidad Y recorrer en orden de llegada.
 *
 * 4. TreeSet:
 *    - O(log n) para operaciones. Mantiene elementos ordenados.
 *    - Usa Comparable (natural) o Comparator (personalizado).
 *    - Elegido cuando se necesita un conjunto siempre ordenado.
 *
 * 5. HashMap:
 *    - O(1) para get/put. Sin orden garantizado.
 *    - Elegido para índices rápidos por clave cuando el orden no importa.
 *
 * 6. LinkedHashMap:
 *    - O(1) + orden de inserción.
 *    - Elegido para registros que deben recorrerse en orden cronológico.
 *
 * 7. TreeMap:
 *    - O(log n). Claves siempre ordenadas.
 *    - Elegido cuando las claves deben presentarse en orden natural o personalizado.
 */
```

2. Compila para verificar que los comentarios no introdujeron errores de sintaxis:

```bash
mvn compile -q
```

**Verificación:** `BUILD SUCCESS`. Los comentarios documentan la complejidad algorítmica y el criterio de selección de cada estructura.

---

### Paso 15: Comparar ArrayList vs LinkedList en operaciones de inserción

**Objetivo:** Demostrar experimentalmente la diferencia de rendimiento entre `ArrayList` y `LinkedList` para inserciones al inicio, reforzando la comprensión de cuándo elegir cada implementación.

1. Crea `src/main/java/com/curso/modulo2/ListComparisonDemo.java`:

```java
package com.curso.modulo2;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Comparación de rendimiento: ArrayList vs LinkedList
 * para inserciones al inicio de la lista.
 */
public class ListComparisonDemo {

    private static final int ELEMENT_COUNT = 50_000;

    public static void main(String[] args) {
        System.out.println("Insertando " + ELEMENT_COUNT
                + " elementos al INICIO de cada lista...\n");

        // ArrayList
        List<Integer> arrayList = new ArrayList<>();
        long startAL = System.nanoTime();
        for (int i = 0; i < ELEMENT_COUNT; i++) {
            arrayList.add(0, i); // Inserción al inicio: O(n)
        }
        long durationAL = (System.nanoTime() - startAL) / 1_000_000;

        // LinkedList
        List<Integer> linkedList = new LinkedList<>();
        long startLL = System.nanoTime();
        for (int i = 0; i < ELEMENT_COUNT; i++) {
            linkedList.add(0, i); // Inserción al inicio: O(1)
        }
        long durationLL = (System.nanoTime() - startLL) / 1_000_000;

        System.out.println("ArrayList  - inserción al inicio: " + durationAL + " ms");
        System.out.println("LinkedList - inserción al inicio: " + durationLL + " ms");
        System.out.println("\nConclusión: LinkedList es más eficiente para "
                + "inserciones frecuentes al inicio.");
        System.out.println("ArrayList es preferible para acceso aleatorio "
                + "y recorridos secuenciales.");
    }
}
```

2. Compila y ejecuta:

```bash
mvn compile -q
java -cp target/classes com.curso.modulo2.ListComparisonDemo
```

**Salida esperada (tiempos aproximados):**

```
Insertando 50000 elementos al INICIO de cada lista...

ArrayList  - inserción al inicio: 450 ms
LinkedList - inserción al inicio: 12 ms

Conclusión: LinkedList es más eficiente para inserciones frecuentes al inicio.
ArrayList es preferible para acceso aleatorio y recorridos secuenciales.
```

**Verificación:** `LinkedList` es significativamente más rápido para inserciones al inicio. Los tiempos exactos varían según el hardware, pero la diferencia debe ser de al menos un orden de magnitud.

---

### Paso 16: Verificar la estructura final del proyecto

**Objetivo:** Confirmar que todos los archivos del Módulo 2 están en su ubicación correcta y el proyecto compila limpiamente.

1. Verifica la estructura de archivos:

```bash
find src/main/java/com/curso/modulo2 -name "*.java" | sort
```

**Salida esperada:**

```
src/main/java/com/curso/modulo2/DataRepository.java
src/main/java/com/curso/modulo2/DataRepositoryDemo.java
src/main/java/com/curso/modulo2/Identifiable.java
src/main/java/com/curso/modulo2/ListComparisonDemo.java
src/main/java/com/curso/modulo2/OrderAdapter.java
src/main/java/com/curso/modulo2/OrderComparators.java
src/main/java/com/curso/modulo2/Product.java
src/main/java/com/curso/modulo2/RepositoryUtils.java
```

2. Ejecuta una compilación limpia completa:

```bash
mvn clean compile
```

**Salida esperada:**

```
[INFO] BUILD SUCCESS
```

3. Ejecuta la demostración completa por última vez:

```bash
java -cp target/classes com.curso.modulo2.DataRepositoryDemo
```

**Verificación:** Las 6 secciones de la demostración se ejecutan sin errores. El proyecto tiene 8 archivos Java en `modulo2` y la clase `Order` modificada en `modulo1`.

## Validación y Pruebas

### Criterio 1: Seguridad de tipos genéricos

Verifica que el compilador rechaza usos incorrectos del tipo genérico. Crea temporalmente el siguiente código en cualquier método `main`:

```java
// ESTE CÓDIGO NO DEBE COMPILAR — verificar y luego eliminar
DataRepository<String> invalid = new DataRepository<>(); // Error: String no implementa Identifiable<Integer>
```

Compila:

```bash
mvn compile
```

**Resultado esperado:** Error de compilación indicando que `String` no cumple el *bound* `Identifiable<Integer>`. Elimina la línea después de verificar.

### Criterio 2: Rechazo de duplicados en DataRepository

Ejecuta la demostración y confirma que la línea `[WARN] Elemento con id=1 ya existe` aparece en la sección 1.

### Criterio 3: Ordenamiento natural vs Comparator

En la sección 4 de la salida, verifica que:
- El orden natural lista las fechas de menor a mayor (id=5 primero, id=3 último).
- El orden por monto lista $95.0 primero y $320.75 último.
- El orden compuesto agrupa DELIVERED, PENDING y SHIPPED correctamente.

### Criterio 4: TreeSet con desambiguación

En la sección 5, verifica que el `TreeSet` contiene exactamente 5 elementos. Si se omitiera `thenComparingInt(Order::getId)`, los dos pedidos con monto 180.50 colapsarían a uno solo (tamaño = 4). Este es el caso adverso que valida la comprensión del contrato de `TreeSet`.

### Criterio 5: Caso adverso — Comodín incorrecto

Verifica que el siguiente código **no compila** (crear temporalmente y eliminar):

```java
// Intentar agregar a una lista de ? extends (productor) — DEBE FALLAR
List<? extends Identifiable<Integer>> producerList = new ArrayList<>();
producerList.add(new Product(999, "Test", 1.0, "Test")); // Error de compilación
```

```bash
mvn compile
```

**Resultado esperado:** Error `no suitable method found for add`. Esto confirma que `? extends` es solo para lectura (principio PECS). Elimina la línea después de verificar.

## Solución de Problemas

### Problema 1: `ClassCastException` o error de compilación al ordenar con TreeSet

**Síntoma:** Al crear `new TreeSet<>()` sin `Comparator` y agregar objetos, se obtiene `ClassCastException: ... cannot be cast to java.lang.Comparable` en tiempo de ejecución, o un error de compilación si el tipo no implementa `Comparable`.

**Causa:** `TreeSet` sin `Comparator` explícito requiere que los elementos implementen `Comparable<T>`. Si la entidad no lo implementa (o si se usa `OrderAdapter` que no implementa `Comparable`), la JVM lanza la excepción al intentar comparar el segundo elemento insertado.

**Solución:**
1. Implementa `Comparable<T>` en la entidad (como se hizo con `Order` y `Product` en este laboratorio).
2. O bien, pasa un `Comparator` al constructor del `TreeSet`:

```java
Set<Order> treeSet = new TreeSet<>(Comparator.comparingDouble(Order::getTotalAmount));
```

3. Verifica siempre que el `Comparator` desambigüe elementos con valores iguales usando `thenComparing` para evitar pérdida silenciosa de datos.

---

### Problema 2: `UnsupportedOperationException` al modificar la lista retornada por `listAll()`

**Síntoma:** Al intentar `orderRepo.listAll().add(...)` o `orderRepo.listAll().remove(...)`, se obtiene `java.lang.UnsupportedOperationException`.

**Causa:** El método `listAll()` de `DataRepository` retorna una lista no modificable creada con `Collections.unmodifiableList()`. Esto es intencional para proteger la encapsulación del repositorio.

**Solución:**
1. Si necesitas una lista modificable, crea una copia:

```java
List<OrderAdapter> modifiable = new ArrayList<>(orderRepo.listAll());
modifiable.add(new OrderAdapter(newOrder)); // Esto funciona
```

2. Para modificar el repositorio, usa los métodos `add()`, `remove()` y `update()` directamente.

## Limpieza

No se requiere limpieza especial para este laboratorio. Los archivos creados en `com.curso.modulo2` son necesarios para los módulos posteriores (Módulo 3: Lambdas consumirá las entidades `Order` y `Product` con los `Comparator` definidos aquí).

Confirma que el proyecto queda en estado limpio:

```bash
mvn clean compile -q
```

Resultado: `BUILD SUCCESS`.

## Resumen

En este laboratorio se construyó un componente genérico completo (`DataRepository<T>`) que demuestra:

- **Parámetros de tipo acotados:** `<T extends Identifiable<Integer>>` garantiza que solo entidades con identificador entero pueden almacenarse.
- **Comodines acotados (PECS):** `? extends T` para lectura en `copyToList` y `merge`; `? super T` para escritura en listas destino.
- **Comportamiento de colecciones:** `ArrayList` para acceso secuencial, `HashSet` para unicidad sin orden, `LinkedHashSet` para unicidad con orden de inserción, `TreeSet` para conjuntos ordenados, `HashMap`/`LinkedHashMap`/`TreeMap` para mapas con diferentes garantías de orden.
- **Comparable vs Comparator:** Ordenamiento natural por `createdAt` en `Order` y por `name` en `Product`; comparadores externos para monto, estado y nombre del cliente.
- **Trampa de TreeSet:** Sin desambiguación en el `Comparator`, elementos con valores iguales se pierden silenciosamente.

### Archivos creados/modificados

| Archivo | Acción |
|---------|--------|
| `com.curso.modulo1.Order` | Modificado: añadido `Comparable<Order>`, `equals`, `hashCode`, constructor con fecha |
| `com.curso.modulo2.Identifiable` | Nuevo: interfaz genérica |
| `com.curso.modulo2.Product` | Nuevo: entidad con `Comparable` e `Identifiable` |
| `com.curso.modulo2.OrderAdapter` | Nuevo: adaptador para `Order` → `Identifiable` |
| `com.curso.modulo2.DataRepository` | Nuevo: repositorio genérico CRUD |
| `com.curso.modulo2.RepositoryUtils` | Nuevo: utilidades con comodines |
| `com.curso.modulo2.OrderComparators` | Nuevo: comparadores externos |
| `com.curso.modulo2.DataRepositoryDemo` | Nuevo: clase de demostración principal |
| `com.curso.modulo2.ListComparisonDemo` | Nuevo: comparación ArrayList vs LinkedList |

### Recursos adicionales

- [Java Generics Tutorial (Oracle)](https://docs.oracle.com/javase/tutorial/java/generics/index.html)
- [Collections Framework Overview (Java SE 11)](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/doc-files/coll-overview.html)
- [Effective Java, 3.ª ed. — Joshua Bloch, Capítulo 5: Generics](https://www.oreilly.com/library/view/effective-java-3rd/9780134686097/)
- [Baeldung: Guide to Java Comparable and Comparator](https://www.baeldung.com/java-comparator-comparable)
