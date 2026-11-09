<p align="center">
  <img src="assets/LogoNetec.png" alt="NETEC" width="300" />
</p>

# Desarrollo Avanzado con Java SE 11

**Custom Netec JAVA_INT (Priv)** · Duración: 30 horas · Nivel: intermedio

## Plataforma de laboratorios

Te damos la bienvenida a la **plataforma de laboratorios** del curso **Desarrollo Avanzado con Java SE 11**. Aquí podrás explorar diferentes tecnologías a través de prácticas guiadas. ¡Desarrolla tus habilidades y lleva tus conocimientos al siguiente nivel!

## Descripción del curso

En este curso realizaremos un recorrido por los conceptos, buenas prácticas y aplicación de las principales APIs de Java SE 11 usando ejemplos prácticos en código.

## Objetivos del curso

Al finalizar el curso, el participante será capaz de:

1. Implementar los conceptos de la POO en código.
2. Comprender el uso de las Interfaces funcionales y genéricos.
3. Implementar expresiones Lambda utilizando las interfaces funcionales de java.util.function.
4. Implementar expresiones Lambda utilizando la API de Stream.
5. Manejar archivos utilizando los paquetes de io y nio.2.
6. Manejar fechas usando la API de java.time.
7. Implementar Hilos usando ExecutorService y Thread.
8. Conectar sus aplicaciones a una base de datos usando JDBC.

## Perfil de audiencia

Este curso está pensado para participantes interesados en profundizar en las principales APIs de Java SE 11 para aplicarlas en su trabajo y fortalecer sus habilidades de desarrollo con Java.

## Prerrequisitos

- Experiencia en el lenguaje Java (Básico).
- Comprensión de los conceptos de la POO.
- Conocimiento en algún otro lenguaje orientado a objetos (Python, C#, Kotlin).
- Conocimiento básico de SQL.

## Lista de laboratorios

Cada uno de estos laboratorios está diseñado para ofrecerte una experiencia práctica. Haz clic en los enlaces para comenzar.

### [Práctica 1. Implementación de contratos con interfaces funcionales](Capitulo01/README.md)
  - **Capítulo 1:** Interfaces.
  - **Descripción**: diseño de interfaces con métodos abstractos, default, static y privados, interfaces funcionales propias validadas con `@FunctionalInterface` e implementaciones intercambiables sin modificar el código cliente.
  - ⏱️ **Duración estimada**: 80 min.

### [Práctica 2. Colecciones genéricas para gestión de datos](Capitulo02/README.md)
  - **Capítulo 2:** Colecciones y Genéricos.
  - **Descripción**: componente genérico con parámetros de tipo y comodines acotados, integrado con List, Set y Map, y ordenamiento con Comparable y Comparator.
  - ⏱️ **Duración estimada**: 100 min.

### [Práctica 3. Procesamiento funcional con expresiones Lambda](Capitulo03/README.md)
  - **Capítulo 3:** Lambda.
  - **Descripción**: refactorización de clases anónimas a expresiones Lambda, interfaces de `java.util.function`, referencias a métodos y composición de funciones.
  - ⏱️ **Duración estimada**: 100 min.

### [Práctica 4. Procesamiento y transformación de datos con Stream API](Capitulo04/README.md)
  - **Capítulo 4:** Manejo de Streams en Java.
  - **Descripción**: pipelines con operaciones intermedias, finales y de corto circuito, manejo de `Optional` y reportes con `Collectors`.
  - ⏱️ **Duración estimada**: 115 min.

### [Práctica 5. Ejecución concurrente de tareas con ExecutorService y CompletableFuture](Capitulo05/README.md)
  - **Capítulo 5:** Programación concurrente.
  - **Descripción**: tareas con Thread y Runnable migradas a ExecutorService, Callable y Future, CyclicBarrier, CompletableFuture y clases atómicas.
  - ⏱️ **Duración estimada**: 125 min.

### [Práctica 6. Procesamiento paralelo con Parallel Streams y Fork/Join](Capitulo06/README.md)
  - **Capítulo 6:** ParallelStreams.
  - **Descripción**: comparación de `stream()` y `parallelStream()`, operaciones determinísticas y no determinísticas, reducción en paralelo y Fork/Join.
  - ⏱️ **Duración estimada**: 100 min.

### [Práctica 7. Modularización de una aplicación con JPMS en Java SE 11](Capitulo07/README.md)
  - **Capítulo 7:** Uso de módulos en Java.
  - **Descripción**: división de una aplicación en módulos con `requires`, `exports`, `opens`, `uses` y `provides`, ejecución desde el ModulePath y migración desde Java 8.
  - ⏱️ **Duración estimada**: 105 min.

### [Práctica 8. Implementación de controles de codificación segura en Java SE 11](Capitulo08/README.md)
  - **Capítulo 8:** Codificación Segura en Java SE 11.
  - **Descripción**: validación de entrada, consultas parametrizadas con JDBC, inmutabilidad, deserialización segura, hashes, cifrado, firmas digitales, KeyStore y procesamiento seguro de XML.
  - ⏱️ **Duración estimada**: 110 min.

### [Práctica 9. Cliente HTTP asíncrono y comunicación WebSocket](Capitulo09/README.md)
  - **Capítulo 9:** HTTP Client y WebSockets en Java SE 11.
  - **Descripción**: consumo síncrono y asíncrono de una API REST con `java.net.http`, HTTPS, comunicación WebSocket con reconexión automática e integración como módulo.
  - ⏱️ **Duración estimada**: 120 min.

### Distribución del tiempo

| Capítulo | Duración del capítulo | Práctica |
| --- | --- | --- |
| 1. Interfaces | 117 min | 80 min |
| 2. Colecciones y Genéricos | 183 min | 100 min |
| 3. Lambda | 158 min | 100 min |
| 4. Manejo de Streams en Java | 210 min | 115 min |
| 5. Programación concurrente | 230 min | 125 min |
| 6. ParallelStreams | 183 min | 100 min |
| 7. Uso de módulos en Java | 210 min | 105 min |
| 8. Codificación Segura en Java SE 11 | 263 min | 110 min |
| 9. HTTP Client y WebSockets en Java SE 11 | 246 min | 120 min |
| **Total** | **1,800 min (30 h)** | **955 min** |

## Instrucciones generales del laboratorio

1. Antes del curso, preparar el entorno con la [Guía de configuración](Setup_Guide.md): JDK 11, Maven, IntelliJ IDEA, Git, PostgreSQL y WireMock.
2. Las prácticas se construyen de forma incremental sobre el proyecto Maven `java-se11-labs`, que se crea en la Práctica 1. Las prácticas 6 y 7 usan proyectos propios (`parallel-streams-lab` y `bookstore-modular`).
3. La Práctica 9 genera su propio KeyStore HTTPS para `localhost` (con la técnica de `keytool` vista en la Práctica 8) y usa el servidor WebSocket del curso, incluido en [`Capitulo09/recursos`](Capitulo09/recursos/NotificationServer.java).
4. Los comandos de las prácticas están escritos para una terminal bash: Linux, macOS o **Git Bash** en Windows.

## Estructura del repositorio

```text
.
├── README.md            Página principal (este archivo)
├── Setup_Guide.md       Guía de configuración del entorno
├── assets/              Logotipo
├── Capitulo01/          README.md de la Práctica 1
├── ...
├── Capitulo09/
│   ├── README.md        Práctica 9
│   └── recursos/        NotificationServer.java (servidor WebSocket del curso)
```

---

## 📬 **Contacto y más información**

Si tienes alguna pregunta o necesitas más detalles, no dudes en [contactarnos](mailto:soporte@netec.com). También puedes encontrar más recursos en nuestra [página](https://www.netec.com/).

---

¡Gracias por visitar nuestra plataforma! No olvides revisar todos los laboratorios y comenzar tu viaje de aprendizaje hoy mismo.
