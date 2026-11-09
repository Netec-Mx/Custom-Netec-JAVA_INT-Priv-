<p align="center">
  <img src="https://raw.githubusercontent.com/Netec-Mx/261108-Java-INT/main/assets/LogoNetec.png" alt="NETEC" width="180" />
</p>

# Desarrollo Avanzado con Java SE 11

En este curso realizaremos un recorrido por los conceptos, buenas prácticas y aplicación de las principales APIs de Java SE 11 usando ejemplos prácticos en código.

## Accesos rápidos

- [**Setup Guide del curso**](https://github.com/Netec-Mx/261108-Java-INT/blob/main/SETUP_GUIDE.md)
- [Laboratorios por capítulo](#lista-de-laboratorios)

## Estructura

- `SETUP_GUIDE.md`: guía de instalación y preparación del entorno.
- `CapituloXX/README.md`: guía de laboratorio por capítulo.

## Lista de laboratorios

### Capítulo 1

- [Práctica: Implementación de contratos con interfaces funcionales](Capitulo01/README.md#práctica-implementación-de-contratos-con-interfaces-funcionales)
  - Descripción: El participante diseñará un conjunto de interfaces que definan los contratos de un dominio de negocio, incorporando métodos abstractos, default, static y privados como parte de las interfaces avanzadas. Posteriormente creará interfaces funcionales personalizadas, validará su contrato con la anotación @FunctionalInterface y desarrollará varias implementaciones concretas, comprobando cómo los conceptos de la POO permiten intercambiar comportamientos sin modificar el código cliente.
  - Duración estimada: 80 min
  - [Ver capítulo completo](Capitulo01/README.md)

### Capítulo 2

- [Práctica: Colecciones genéricas para gestión de datos](Capitulo02/README.md#práctica-colecciones-genéricas-para-gestión-de-datos)
  - Descripción: El participante construirá un componente genérico de gestión de datos utilizando parámetros de tipo y comodines acotados, y lo integrará con las principales implementaciones de List, Set y Map para registrar, buscar, actualizar y eliminar elementos. Finalmente ordenará las colecciones mediante Comparable y Comparator, comparando el comportamiento de cada estructura y eligiendo la más adecuada según el escenario de uso.
  - Duración estimada: 100 min
  - [Ver capítulo completo](Capitulo02/README.md)

### Capítulo 3

- [Práctica: Procesamiento funcional con expresiones Lambda](Capitulo03/README.md#práctica-procesamiento-funcional-con-expresiones-lambda)
  - Descripción: El participante partirá de implementaciones basadas en clases anónimas y las refactorizará a expresiones Lambda, incluyendo lambdas que trabajen con tipos genéricos. Después utilizará Predicate, Function, Consumer, Supplier, UnaryOperator y BiFunction, así como referencias a métodos y la composición de funciones, para filtrar, transformar y procesar un conjunto de datos, evaluando la mejora en legibilidad y mantenimiento del código.
  - Duración estimada: 100 min
  - [Ver capítulo completo](Capitulo03/README.md)

### Capítulo 4

- [Práctica: Procesamiento y transformación de datos con Stream API](Capitulo04/README.md#práctica-procesamiento-y-transformación-de-datos-con-stream-api)
  - Descripción: El participante construirá pipelines de procesamiento sobre una colección de datos de negocio combinando operaciones intermedias como filter, map, flatMap, sorted y distinct con operaciones finales como reduce, count y collect. Aplicará operaciones de corto circuito como anyMatch y findFirst, manejará resultados ausentes con Optional y generará reportes agrupados y particionados mediante Collectors como groupingBy, partitioningBy, joining y toMap.
  - Duración estimada: 115 min
  - [Ver capítulo completo](Capitulo04/README.md)

### Capítulo 5

- [Práctica: Ejecución concurrente de tareas con ExecutorService y CompletableFuture](Capitulo05/README.md#práctica-ejecución-concurrente-de-tareas-con-executorservice-y-completablefuture)
  - Descripción: El participante creará tareas con Thread y Runnable y las migrará a un pool de hilos administrado con ExecutorService. Utilizará Callable y Future para obtener resultados, sincronizará etapas de trabajo con CyclicBarrier y encadenará procesos asíncronos con CompletableFuture. Finalmente identificará condiciones de carrera y las resolverá con clases atómicas, aplicando buenas prácticas para el cierre ordenado de los pools y la prevención de problemas de concurrencia.
  - Duración estimada: 125 min
  - [Ver capítulo completo](Capitulo05/README.md)

### Capítulo 6

- [Práctica: Procesamiento paralelo con Parallel Streams y Fork/Join](Capitulo06/README.md#práctica-procesamiento-paralelo-con-parallel-streams-y-forkjoin)
  - Descripción: El participante procesará un conjunto amplio de datos con stream() y parallelStream(), midiendo y comparando los tiempos de ejecución. Analizará el comportamiento de operaciones determinísticas y no determinísticas, implementará reducciones en paralelo con reduce cuidando la asociatividad de la identidad, el acumulador y el combinador, y resolverá un problema divisible mediante RecursiveTask y ForkJoinPool, concluyendo en qué escenarios conviene utilizar o evitar los Parallel Streams.
  - Duración estimada: 100 min
  - [Ver capítulo completo](Capitulo06/README.md)

### Capítulo 7

- [Práctica: Modularización de una aplicación con JPMS en Java SE 11](Capitulo07/README.md#práctica-modularización-de-una-aplicación-con-jpms-en-java-se-11)
  - Descripción: El participante dividirá una aplicación tradicional basada en ClassPath en varios módulos, declarando en cada module-info.java las directivas requires, exports, opens, uses y provides necesarias. Compilará y ejecutará la solución desde el ModulePath, consultará los módulos del JDK, resolverá una dependencia cíclica reorganizando los paquetes y aplicará una estrategia de migración desde Java 8, relacionando los módulos utilizados con las APIs de Java SE 11 que consume la aplicación.
  - Duración estimada: 105 min
  - [Ver capítulo completo](Capitulo07/README.md)

### Capítulo 8

- [Práctica: Implementación de controles de codificación segura en Java SE 11](Capitulo08/README.md#práctica-implementación-de-controles-de-codificación-segura-en-java-se-11)
  - Descripción: El participante revisará una aplicación con vulnerabilidades comunes y aplicará controles de validación de entrada, prevención de inyección SQL mediante consultas parametrizadas con JDBC, copias defensivas, objetos inmutables y construcción segura de objetos. Configurará filtros para una deserialización segura, protegerá datos confidenciales con hashes mediante MessageDigest, cifrado con JCE y firmas digitales, administrará claves y certificados en un KeyStore y asegurará el procesamiento de XML frente a ataques de entidades externas.
  - Duración estimada: 110 min
  - [Ver capítulo completo](Capitulo08/README.md)

### Capítulo 9

- [Práctica: Cliente HTTP asíncrono y comunicación WebSocket](Capitulo09/README.md#práctica-cliente-http-asíncrono-y-comunicación-websocket)
  - Descripción: El participante construirá un cliente con HttpClient, HttpRequest y HttpResponse para consumir una API REST, enviando solicitudes síncronas y asíncronas con CompletableFuture y configurando comunicación segura mediante HTTPS. Posteriormente creará una conexión WebSocket con WebSocket.Builder, procesará mensajes y eventos con WebSocket.Listener, implementará el manejo de errores y la reconexión automática, e integrará la solución como un módulo que declare la dependencia con java.net.http.
  - Duración estimada: 120 min
  - [Ver capítulo completo](Capitulo09/README.md)

## Flujo de colaboración

- Trabajar en `changes_course`.
- Crear Pull Request hacia `main`.
- Merge por `Squash and merge`.

---

*Material didáctico preparado por Global K, S.A. de C.V.*
