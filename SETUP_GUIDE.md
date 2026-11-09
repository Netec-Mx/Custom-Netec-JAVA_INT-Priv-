# Desarrollo Avanzado con Java SE 11 — Setup Guide

<p align="center"><img src="assets/LogoNetec.png" alt="NETEC" width="180" /></p>

Guía consolidada de instalación y preparación del entorno.

| Curso | Clave | Versión | Estado | Última validación |
|---|---|---|---|---|
| Desarrollo Avanzado con Java SE 11 | [CLAVE COMPLETA] | [V1.0] | BORRADOR | [FECHA DE VALIDACIÓN] |

## Accesos

- **Repositorio:** [https://github.com/Netec-Mx/261108-Java-INT](https://github.com/Netec-Mx/261108-Java-INT)
- **Repositorio de laboratorios:** [https://github.com/Netec-Mx/261108-Java-INT](https://github.com/Netec-Mx/261108-Java-INT)
- **README principal:** [INSERTAR ENLACE]
- **Archivos de instalación:** [INSERTAR ENLACE]
- **Documentación oficial:** [INSERTAR ENLACE]

## Software y versiones

| Componente | Versión exacta | Fuente oficial | Momento | Validación |
|---|---|---|---|---|
| Java Development Kit (JDK) — Eclipse Temurin | [VERSIÓN POR VALIDAR] | [https://adoptium.net/temurin/releases/?version=11](https://adoptium.net/temurin/releases/?version=11) | Antes del curso | [COMANDO DE PRUEBA] |
| IntelliJ IDEA Community Edition | 2024.1.4 | [https://www.jetbrains.com/idea/download/other.html](https://www.jetbrains.com/idea/download/other.html) | Antes del curso | [COMANDO DE PRUEBA] |
| Apache Maven | 3.9.8 | [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi) | Antes del curso | [COMANDO DE PRUEBA] |
| Git | 2.45.2 | [https://git-scm.com/downloads](https://git-scm.com/downloads) | Antes del curso | [COMANDO DE PRUEBA] |
| Java Development Kit (JDK) OpenJDK | 11.0.23 | [https://adoptium.net/temurin/releases/?version=11](https://adoptium.net/temurin/releases/?version=11) | Antes del curso | [COMANDO DE PRUEBA] |
| PostgreSQL | 16.3 | [https://www.postgresql.org/download/](https://www.postgresql.org/download/) | Antes del curso | [COMANDO DE PRUEBA] |
| PostgreSQL JDBC Driver | 42.7.3 | [https://jdbc.postgresql.org/download/](https://jdbc.postgresql.org/download/) | Antes del curso | [COMANDO DE PRUEBA] |
| WireMock Standalone | 3.6.0 | [https://wiremock.org/docs/standalone/java-jar/](https://wiremock.org/docs/standalone/java-jar/) | Antes del curso | [COMANDO DE PRUEBA] |
| Apache JMeter | 5.6.3 | [https://jmeter.apache.org/download_jmeter.cgi](https://jmeter.apache.org/download_jmeter.cgi) | Antes del curso | [COMANDO DE PRUEBA] |
| SonarLint (plugin IntelliJ IDEA) | 10.4.0.79986 | [https://plugins.jetbrains.com/plugin/7973-sonarlint](https://plugins.jetbrains.com/plugin/7973-sonarlint) | Antes del curso | [COMANDO DE PRUEBA] |
| Bouncy Castle Provider | 1.78.1 | [https://www.bouncycastle.org/download/bouncy-castle-java/](https://www.bouncycastle.org/download/bouncy-castle-java/) | Antes del curso | [COMANDO DE PRUEBA] |

## Matriz de prácticas

| Lab | Nombre | Duración | Resultado esperado | Acceso |
|---|---|---|---|---|
| 01-00-01 | Práctica: Implementación de contratos con interfaces funcionales | 80 min | Proyecto Maven java-se11-labs creado y compilando sin errores bajo JDK 11 | [Capítulo 01](https://github.com/Netec-Mx/261108-Java-INT/blob/main/Capitulo01/README.md#práctica-implementación-de-contratos-con-interfaces-funcionales) |
| 02-00-01 | Práctica: Colecciones genéricas para gestión de datos | 100 min | Clase genérica DataRepository<T> con operaciones CRUD completas y comodines acotados correctamente aplicados | [Capítulo 02](https://github.com/Netec-Mx/261108-Java-INT/blob/main/Capitulo02/README.md#práctica-colecciones-genéricas-para-gestión-de-datos) |
| 03-00-01 | Práctica: Procesamiento funcional con expresiones Lambda | 100 min | Mínimo tres clases anónimas refactorizadas a expresiones Lambda equivalentes con documentación comparativa | [Capítulo 03](https://github.com/Netec-Mx/261108-Java-INT/blob/main/Capitulo03/README.md#práctica-procesamiento-funcional-con-expresiones-lambda) |
| 04-00-01 | Práctica: Procesamiento y transformación de datos con Stream API | 115 min | Entidad OrderLine creada e integrada al dominio Order con relación de composición | [Capítulo 04](https://github.com/Netec-Mx/261108-Java-INT/blob/main/Capitulo04/README.md#práctica-procesamiento-y-transformación-de-datos-con-stream-api) |
| 05-00-01 | Práctica: Ejecución concurrente de tareas con ExecutorService y CompletableFuture | 125 min | Tres tareas Runnable migradas exitosamente a ExecutorService con cierre ordenado documentado | [Capítulo 05](https://github.com/Netec-Mx/261108-Java-INT/blob/main/Capitulo05/README.md#práctica-ejecución-concurrente-de-tareas-con-executorservice-y-completablefuture) |
| 06-00-01 | Práctica: Procesamiento paralelo con Parallel Streams y Fork/Join | 100 min | Tabla comparativa documentada con tiempos de ejecución de stream() vs parallelStream() para el conjunto de 10M de registros Transaction | [Capítulo 06](https://github.com/Netec-Mx/261108-Java-INT/blob/main/Capitulo06/README.md#práctica-procesamiento-paralelo-con-parallel-streams-y-forkjoin) |
| 07-00-01 | Práctica: Modularización de una aplicación con JPMS en Java SE 11 | 105 min | Estructura de proyecto bookstore-modular/ con cuatro módulos JPMS correctamente separados y compilables | [Capítulo 07](https://github.com/Netec-Mx/261108-Java-INT/blob/main/Capitulo07/README.md#práctica-modularización-de-una-aplicación-con-jpms-en-java-se-11) |
| 08-00-01 | Práctica: Implementación de controles de codificación segura en Java SE 11 | 110 min | Informe de vulnerabilidades identificadas con SonarLint (mínimo 5 vulnerabilidades documentadas y clasificadas) | [Capítulo 08](https://github.com/Netec-Mx/261108-Java-INT/blob/main/Capitulo08/README.md#práctica-implementación-de-controles-de-codificación-segura-en-java-se-11) |
| 09-00-01 | Práctica: Cliente HTTP asíncrono y comunicación WebSocket | 120 min | Clase RestApiClient.java con métodos GET, POST, PUT y DELETE implementados síncronamente con HttpClient y PreparedStatement | [Capítulo 09](https://github.com/Netec-Mx/261108-Java-INT/blob/main/Capitulo09/README.md#práctica-cliente-http-asíncrono-y-comunicación-websocket) |
---

*Material didáctico preparado por Global K, S.A. de C.V.*
