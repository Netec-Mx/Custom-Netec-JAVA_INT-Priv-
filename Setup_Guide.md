<p align="center">
  <img src="assets/LogoNetec.png" alt="NETEC" width="300" />
</p>

# Setup Guide General

## Desarrollo Avanzado con Java SE 11

| Control documental | Información |
|---|---|
| Clave del curso | Custom Netec JAVA_INT (Priv) |
| Versión del documento | 1.0 |
| Estado | BORRADOR (pendiente de validación de extremo a extremo por el instructor) |
| Instructor responsable | Por asignar |
| Fecha de elaboración | 2026-11-09 |
| Fecha de última validación | Pendiente |
| Repositorio de laboratorios | [https://github.com/Netec-Mx/Custom-Netec-JAVA_INT-Priv-](https://github.com/Netec-Mx/Custom-Netec-JAVA_INT-Priv-) |
| Rama y revisión analizada | `main` (commit `da121a7`) con los cambios de la rama `changes_course` |
| Alcance revisado | 9 capítulos, 9 prácticas (`Capitulo01` a `Capitulo09`), `README.md` y recurso `Capitulo09/recursos/NotificationServer.java` |
| Modalidad del entorno | Local (estación del participante) |

# Objetivo

Preparar en la estación de cada participante un entorno de desarrollo Java SE 11 con JDK Eclipse Temurin 11, Apache Maven, IntelliJ IDEA Community Edition (con SonarLint), Git, PostgreSQL 16 y las herramientas locales de la Práctica 9 (WireMock Standalone y el servidor WebSocket del curso). El entorno se considera listo cuando todas las validaciones obligatorias de la sección [Validación del Entorno](#validacion-entorno) se aprueban.

# Requisitos de Hardware

| Recurso | Mínimo requerido | Recomendado | Observaciones |
|---|---|---|---|
| Procesador y arquitectura | x64 (Intel/AMD) o ARM64 (Apple Silicon), 4 núcleos | 8 núcleos o más | La Práctica 6 compara `stream()` contra `parallelStream()` y Fork/Join: con menos de 4 núcleos la mejora en paralelo apenas se nota. |
| Memoria RAM | 8 GB | 16 GB | IntelliJ IDEA, PostgreSQL, dos instancias de WireMock y el servidor WebSocket se ejecutan al mismo tiempo en la Práctica 9. La Práctica 6 genera 10 millones de registros en memoria. |
| Espacio libre | 10 GB | 20 GB | JDK, IntelliJ IDEA, PostgreSQL, repositorio local de Maven (`~/.m2`) y proyectos del curso. |
| Pantalla | 1366 × 768 | 1920 × 1080 o dos monitores | Se trabaja con el IDE y hasta tres terminales abiertas. |
| Conectividad | Acceso a Internet | Banda ancha estable | Necesario para instalar el software y para que Maven descargue dependencias y plugins (Maven Central). |

# Sistemas Operativos Compatibles

| Sistema operativo | Edición y versión | Arquitectura | Condición de soporte o validación |
|---|---|---|---|
| Windows | 10 (22H2) u 11 | x64 | Plataforma base. Compatible según fabricante; requiere **Git Bash** para ejecutar los comandos de las prácticas. |
| Ubuntu | 22.04 LTS o 24.04 LTS | x64 | Compatible según fabricante. |
| macOS | 13 Ventura o posterior | x64 / ARM64 | Compatible según fabricante. |

Los comandos de las prácticas están escritos para una terminal **bash**. En Windows se ejecutan en **Git Bash** (incluido en Git for Windows), donde `~` corresponde a `C:\Users\<usuario>`. En Linux y macOS se usa la terminal del sistema. Las rutas del curso (`~/cursos/...` y `~/labs/tools`) son las mismas en las tres plataformas.

# Software Requerido

| Componente | Edición y versión requerida | Arquitectura | Obligatorio u opcional | Fuente oficial |
|---|---|---|---|---|
| JDK Eclipse Temurin | 11.0.23+9 (JDK, no JRE) | x64 / ARM64 | Obligatorio (prácticas 1 a 9) | [https://github.com/adoptium/temurin11-binaries/releases/tag/jdk-11.0.23%2B9](https://github.com/adoptium/temurin11-binaries/releases/tag/jdk-11.0.23%2B9) |
| Apache Maven | 3.9.8 | Independiente (requiere JDK) | Obligatorio (prácticas 1 a 9) | [https://archive.apache.org/dist/maven/maven-3/3.9.8/binaries/](https://archive.apache.org/dist/maven/maven-3/3.9.8/binaries/) |
| IntelliJ IDEA Community Edition | 2024.1.4 | x64 / ARM64 | Obligatorio | [https://www.jetbrains.com/idea/download/other.html](https://www.jetbrains.com/idea/download/other.html) |
| SonarLint (plugin de IntelliJ IDEA) | 10.4.0.79986 | Plugin | Obligatorio (Práctica 8) | [https://plugins.jetbrains.com/plugin/7973-sonarlint](https://plugins.jetbrains.com/plugin/7973-sonarlint) |
| Git (Git for Windows incluye Git Bash) | 2.45.2 | x64 / ARM64 | Obligatorio | [https://git-scm.com/downloads](https://git-scm.com/downloads) |
| PostgreSQL (servidor y cliente `psql`) | 16.3 | x64 / ARM64 | Obligatorio (Práctica 8) | [https://www.postgresql.org/download/](https://www.postgresql.org/download/) |
| WireMock Standalone | 3.6.0 (JAR) | Independiente (requiere JDK) | Obligatorio (Práctica 9) | [https://repo1.maven.org/maven2/org/wiremock/wiremock-standalone/3.6.0/](https://repo1.maven.org/maven2/org/wiremock/wiremock-standalone/3.6.0/) |
| Servidor WebSocket del curso | `NotificationServer.java` (incluido en el repositorio) | Independiente (requiere JDK 11) | Obligatorio (Práctica 9) | [Capitulo09/recursos/NotificationServer.java](Capitulo09/recursos/NotificationServer.java) |
| curl | El incluido en el sistema o en Git Bash | — | Obligatorio (Práctica 9) | Incluido en Windows 10/11, Git Bash, Ubuntu y macOS |

Todos los componentes son gratuitos. No se requieren licencias comerciales ni cuentas en servicios en la nube.

# Herramientas y Dependencias

## Paquetes, frameworks y componentes adicionales

| Dependencia | Versión o restricción | Componente que la necesita | Método de instalación o habilitación |
|---|---|---|---|
| `maven-compiler-plugin` | 3.13.0, con `<release>11</release>` | Proyectos Maven de las prácticas | Declarado en el `pom.xml` que se crea en la Práctica 1; Maven lo descarga en la primera compilación. |
| PostgreSQL JDBC Driver (`org.postgresql:postgresql`) | 42.7.3 | Práctica 8 | Dependencia en el `pom.xml` de `java-se11-labs` (Paso 1 de la Práctica 8). |
| Bouncy Castle Provider (`org.bouncycastle:bcprov-jdk18on`) | 1.78.1 | Práctica 8 | Dependencia en el `pom.xml` de `java-se11-labs` (Paso 1 de la Práctica 8). |
| `keytool` | Incluido en el JDK 11 | Prácticas 8 y 9 | Disponible al instalar el JDK (`$JAVA_HOME/bin`). |
| Módulo `java.net.http` | Incluido en el JDK 11 | Práctica 9 | Parte del JDK; se declara con `requires java.net.http` en `module-info.java`. |

No se usan frameworks adicionales: las prácticas 1 a 7 solo emplean la biblioteca estándar de Java SE 11. Las prácticas 6 y 7 usan proyectos propios (`parallel-streams-lab` y `bookstore-modular`) que también se compilan con Maven o con `javac`/`jar` del JDK.

## Licencias, cuentas y permisos

| Recurso o servicio | Licencia o suscripción | Cuenta o rol requerido | Permiso mínimo | Responsable de habilitación |
|---|---|---|---|---|
| Estación del participante | — | Usuario local | Administrador para instalar JDK, PostgreSQL e IntelliJ IDEA | Área de TI del cliente |
| PostgreSQL local | Licencia PostgreSQL (libre) | Superusuario `postgres` | Crear bases de datos y usuarios | Participante, durante la instalación |
| Acceso al repositorio | Repositorio privado de GitHub | Cuenta de GitHub con acceso de lectura, o paquete ZIP entregado por el instructor | Lectura | Netec / instructor |

La contraseña del usuario `postgres` la define cada participante al instalar PostgreSQL. No la registres en el repositorio ni la compartas. Las credenciales de laboratorio (usuario `securelab`, KeyStores con contraseña de práctica) son solo para el entorno local del curso.

# Instalación y Configuración

## 1. Obtener los materiales

- Repositorio autorizado: [https://github.com/Netec-Mx/Custom-Netec-JAVA_INT-Priv-](https://github.com/Netec-Mx/Custom-Netec-JAVA_INT-Priv-).
- Revisión que se utilizará: rama `main`.
- Directorio de trabajo: `~/cursos` para los proyectos y `~/labs/tools` para las herramientas de la Práctica 9.
- Archivos distribuidos: los `README.md` de cada capítulo y `Capitulo09/recursos/NotificationServer.java`. Todo el código de los proyectos (`java-se11-labs`, `parallel-streams-lab`, `bookstore-modular`) se crea durante las prácticas.

**Plataforma y terminal:** Windows (Git Bash), Linux o macOS (bash).

```bash
mkdir -p ~/cursos ~/labs/tools
cd ~/cursos
git clone https://github.com/Netec-Mx/Custom-Netec-JAVA_INT-Priv-.git
```

**Resultado esperado:** existe la carpeta `~/cursos/Custom-Netec-JAVA_INT-Priv-` con las carpetas `Capitulo01` a `Capitulo09`.

## 2. Instalar o habilitar los componentes

### JDK Eclipse Temurin 11.0.23+9

1. Descarga el instalador **JDK 11.0.23+9** para tu sistema desde la [página de la versión jdk-11.0.23+9 de Temurin](https://github.com/adoptium/temurin11-binaries/releases/tag/jdk-11.0.23%2B9) (archivo `OpenJDK11U-jdk_<arquitectura>_<sistema>_hotspot_11.0.23_9`).
2. En Windows, ejecuta el `.msi` y activa las opciones **Set JAVA_HOME variable** y **Add to PATH**. En macOS usa el `.pkg`. En Linux, descomprime el `.tar.gz` en `/opt` y define `JAVA_HOME` (ver [Variables de Entorno](#variables-entorno)).
3. Si tienes otros JDK instalados, asegúrate de que el JDK 11 sea el primero en el `PATH`.
4. Comprueba la instalación en una terminal nueva.

```bash
java -version
javac -version
```

**Resultado esperado:** ambas salidas muestran `11.0.23` (por ejemplo, `openjdk version "11.0.23" 2024-04-16` y `javac 11.0.23`).

### Apache Maven 3.9.8

1. Descarga `apache-maven-3.9.8-bin.zip` (Windows) o `apache-maven-3.9.8-bin.tar.gz` (Linux/macOS) desde el [archivo de Apache Maven](https://archive.apache.org/dist/maven/maven-3/3.9.8/binaries/).
2. Descomprímelo en `C:\tools\apache-maven-3.9.8` (Windows) o en `/opt/apache-maven-3.9.8` (Linux/macOS).
3. Agrega la carpeta `bin` al `PATH` (ver [Variables de Entorno](#variables-entorno)).
4. Comprueba la instalación en una terminal nueva.

```bash
mvn -version
```

**Resultado esperado:** `Apache Maven 3.9.8` y `Java version: 11.0.23`. Si Maven muestra otra versión de Java, corrige `JAVA_HOME`.

### Git 2.45.2 (incluye Git Bash en Windows)

1. Descarga Git desde [git-scm.com](https://git-scm.com/downloads). Para la versión exacta en Windows, usa la [versión 2.45.2 de Git for Windows](https://github.com/git-for-windows/git/releases/tag/v2.45.2.windows.1).
2. En Windows, conserva las opciones predeterminadas del instalador (incluye **Git Bash**).
3. Comprueba la instalación.

```bash
git --version
```

**Resultado esperado:** `git version 2.45.2` (en Windows, `git version 2.45.2.windows.1`).

### IntelliJ IDEA Community Edition 2024.1.4 y SonarLint 10.4.0.79986

1. Descarga **IntelliJ IDEA Community Edition 2024.1.4** desde [Other versions](https://www.jetbrains.com/idea/download/other.html) e instálala.
2. Abre IntelliJ IDEA y ve a **File → Project Structure → SDKs**. Agrega el JDK 11.0.23 instalado.
3. Ve a **File → Settings → Build, Execution, Deployment → Build Tools → Maven** (en macOS, **IntelliJ IDEA → Settings**). En **Maven home path**, selecciona la carpeta de Maven 3.9.8.
4. Instala SonarLint: descarga la versión **10.4.0.79986** desde la [página del plugin](https://plugins.jetbrains.com/plugin/7973-sonarlint) (pestaña **Versions**). En **Settings → Plugins**, haz clic en el engrane y elige **Install Plugin from Disk...**. Selecciona el archivo descargado y reinicia el IDE.
5. Comprueba que aparece la ventana de herramientas **SonarLint** (**View → Tool Windows → SonarLint**).

**Resultado esperado:** IntelliJ IDEA reconoce el JDK 11 y Maven 3.9.8, y SonarLint aparece como plugin instalado.

### PostgreSQL 16.3

1. Descarga PostgreSQL 16.3 desde la [página oficial](https://www.postgresql.org/download/). En Windows y macOS usa el instalador de EDB. En Ubuntu usa el repositorio oficial de PostgreSQL (`postgresql-16`).
2. Durante la instalación:
   - Conserva el puerto **5432**.
   - Define una contraseña para el superusuario `postgres` y guárdala en un lugar seguro.
   - Incluye **Command Line Tools** para tener el cliente `psql`.
3. En Windows, agrega la carpeta `bin` de PostgreSQL al `PATH` (ver [Variables de Entorno](#variables-entorno)).
4. Comprueba la instalación y la conexión.

```bash
psql --version
psql -U postgres -h localhost -p 5432 -c "SELECT version();"
```

**Resultado esperado:** `psql (PostgreSQL) 16.3` y una fila que comienza con `PostgreSQL 16.3`.

> La base de datos `secure_lab_db` y el usuario `securelab` **no** se crean en esta preparación: el participante los crea en el Paso 1 de la Práctica 8.

### WireMock Standalone 3.6.0 y servidor WebSocket del curso (Práctica 9)

1. Descarga el JAR de WireMock en `~/labs/tools`.
2. Copia el servidor WebSocket del curso en la misma carpeta.
3. Comprueba que ambos inician (ver V-06 y V-07 en [Validación del Entorno](#validacion-entorno)).

```bash
cd ~/labs/tools
curl -L -o wiremock-standalone-3.6.0.jar \
  https://repo1.maven.org/maven2/org/wiremock/wiremock-standalone/3.6.0/wiremock-standalone-3.6.0.jar
cp ~/cursos/Custom-Netec-JAVA_INT-Priv-/Capitulo09/recursos/NotificationServer.java ~/labs/tools/
ls -la ~/labs/tools
```

**Resultado esperado:** `~/labs/tools` contiene `wiremock-standalone-3.6.0.jar` (unos 18 MB) y `NotificationServer.java`.

## 3. Preparar archivos de configuración

| Archivo | Ruta completa o relativa a la raíz definida | Configuración necesaria | Origen |
|---|---|---|---|
| `pom.xml` | `~/cursos/java-se11-labs/pom.xml` | `maven.compiler.release` 11, `maven-compiler-plugin` 3.13.0; en la Práctica 8 se agregan `postgresql` 42.7.3 y `bcprov-jdk18on` 1.78.1 | Se crea en la Práctica 1 |
| `keystore.p12` | `~/cursos/java-se11-labs/src/main/resources/keystore.p12` | KeyStore PKCS12 de la Práctica 8 (`CN=SecureLab`) | Se crea con `keytool` en la Práctica 8 |
| `localhost-https.p12` | `~/cursos/java-se11-labs/src/main/resources/localhost-https.p12` | KeyStore PKCS12 para HTTPS en `localhost` (con SAN `localhost` y `127.0.0.1`) | Se crea con `keytool` en el Paso 6 de la Práctica 9 |
| Stubs de WireMock | `~/cursos/java-se11-labs/stubs/mappings` | Definiciones JSON de la API REST simulada | Se crean en la Práctica 9 |

No se requieren archivos de configuración previos al curso: todos se generan durante las prácticas.

# Variables de Entorno <a id="variables-entorno"></a>

| Variable | Propósito | Valor o formato esperado | Alcance | Método de configuración |
|---|---|---|---|---|
| `JAVA_HOME` | Ubicar el JDK 11 para Maven e IntelliJ IDEA | Carpeta raíz del JDK 11.0.23 (por ejemplo, `C:\Program Files\Eclipse Adoptium\jdk-11.0.23.9-hotspot`) | Usuario | Instalador de Temurin (Windows) o perfil de shell |
| `PATH` | Ejecutar `java`, `javac`, `keytool`, `mvn` y `psql` desde cualquier terminal | Debe incluir `%JAVA_HOME%\bin`, la carpeta `bin` de Maven y la carpeta `bin` de PostgreSQL | Usuario | Variables de entorno del sistema (Windows) o perfil de shell |

**Configuración — Windows 11 (PowerShell, solo si el instalador no las definió):**

```powershell
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Eclipse Adoptium\jdk-11.0.23.9-hotspot", "User")
$p = [Environment]::GetEnvironmentVariable("Path", "User")
[Environment]::SetEnvironmentVariable("Path", "$p;C:\tools\apache-maven-3.9.8\bin;C:\Program Files\PostgreSQL\16\bin", "User")
```

Cierra y vuelve a abrir Git Bash e IntelliJ IDEA para que tomen los cambios. Ajusta las rutas si instalaste en otra ubicación.

**Configuración — Linux / macOS (bash o zsh):** agrega estas líneas a `~/.bashrc` o `~/.zshrc`.

```bash
export JAVA_HOME=/opt/jdk-11.0.23+9            # macOS: export JAVA_HOME=$(/usr/libexec/java_home -v 11)
export PATH="$JAVA_HOME/bin:/opt/apache-maven-3.9.8/bin:$PATH"
```

**Verificación sin exponer secretos:**

```bash
echo "$JAVA_HOME"
which java mvn psql keytool
```

# Configuración Base para los Laboratorios

## Parámetros comunes

| Parámetro | Valor o convención | Observaciones |
|---|---|---|
| Raíz de trabajo | `~/cursos` | En Windows (Git Bash) equivale a `C:\Users\<usuario>\cursos`. |
| Proyecto principal | `~/cursos/java-se11-labs` | Se crea en la Práctica 1 y se reutiliza de forma incremental en las prácticas 2, 3, 4, 5, 7, 8 y 9. |
| Proyectos independientes | `~/cursos/parallel-streams-lab` (Práctica 6) y `~/cursos/java-se11-labs/bookstore-modular` (Práctica 7) | Se crean durante las prácticas. |
| Herramientas | `~/labs/tools` | WireMock y `NotificationServer.java`. |
| Nivel de lenguaje | Java 11 (`release 11`) | Igual en Maven y en IntelliJ IDEA. |
| PostgreSQL | `localhost:5432` | Práctica 8. |
| WireMock HTTP / HTTPS | `localhost:8080` / `localhost:8443` | Práctica 9. |
| Servidor WebSocket | `ws://localhost:8090/ws/notifications` | Práctica 9. |

## Estructura de carpetas

```bash
mkdir -p ~/cursos ~/labs/tools
```

Las carpetas de cada proyecto las crea el participante en la práctica correspondiente. No borres `~/cursos/java-se11-labs` al terminar una práctica: las siguientes la reutilizan.

## Recursos compartidos

| Recurso | Nombre o identificador | Estructura o configuración base | Responsable de creación |
|---|---|---|---|
| Base de datos PostgreSQL | `secure_lab_db` | Base local por participante; propietario de trabajo `securelab`. Las tablas se crean en la Práctica 8. | Participante, en la Práctica 8 |
| Usuario de base de datos | `securelab` | Usuario local con privilegios sobre `secure_lab_db` (contraseña de laboratorio indicada en la Práctica 8). | Participante, en la Práctica 8 |

Cada participante trabaja con su propia instancia local de PostgreSQL, así que no hay recursos compartidos entre participantes.

## Correcciones de coherencia

| Inconsistencia o dependencia omitida | Configuración unificada | Ajuste necesario en las copias de trabajo |
|---|---|---|
| La Práctica 8 declaraba `bcprov-jdk15on` 1.78.1, que no existe en Maven Central. | `org.bouncycastle:bcprov-jdk18on:1.78.1`. | Corregido en `Capitulo08/README.md`. |
| La Práctica 9 reutilizaba el `keystore.p12` de la Práctica 8 con contraseña `changeit`, pero ese KeyStore usa otra contraseña y `CN=SecureLab`. El `HttpClient` rechaza la conexión con `No name matching localhost found`. | La Práctica 9 genera `localhost-https.p12` con `CN=localhost` y SAN `dns:localhost,ip:127.0.0.1`. | Corregido en `Capitulo09/README.md` (Paso 6 y referencias). |
| La Práctica 9 se conectaba a `ws://localhost:8080/ws/notifications`, pero WireMock 3.6.0 no ofrece un servidor WebSocket. | Servidor del curso `NotificationServer.java` en `ws://localhost:8090/ws/notifications`. | Recurso agregado en `Capitulo09/recursos` y pasos 7, 8 y 10 actualizados. |
| `Capitulo08/README.md` tenía líneas sobrantes al inicio del archivo. | Encabezado limpio. | Corregido. |
| `Capitulo03/README.md` no tenía las secciones de validación, limpieza y resumen. | Mismas secciones que las demás prácticas. | Agregadas. |
| Una referencia de la Práctica 6 apuntaba a una página inexistente (404) de O'Reilly. | Referencia vigente a la guía OCP Java SE 11 de Boyarsky y Selikoff. | Corregido. |
| La guía anterior enlazaba al repositorio `261108-Java-INT` y listaba Apache JMeter, que ninguna práctica usa. | Repositorio `Custom-Netec-JAVA_INT-Priv-`, sin JMeter. | Guía reescrita; el archivo `SETUP_GUIDE.md` se renombró a `Setup_Guide.md`. |
| La Práctica 8 indica `C:\cursos\java-se11-labs` para Windows, pero los comandos usan `~/cursos/...`. | Usar Git Bash con `~/cursos/java-se11-labs` (`C:\Users\<usuario>\cursos\java-se11-labs`). | El instructor debe indicarlo al iniciar la Práctica 8. |

# Servicios Externos Requeridos

| Servicio | URL, endpoint o región | Uso | Acceso requerido |
|---|---|---|---|
| Maven Central | `https://repo.maven.apache.org/maven2` | Descarga de plugins y dependencias de Maven | Salida HTTPS a Internet |
| GitHub | `https://github.com` | Clonar el repositorio del curso | Cuenta con acceso de lectura, o paquete ZIP del instructor |

## Configuración de red

| Origen | Destino | Protocolo y puerto | Sentido | Condición |
|---|---|---|---|---|
| Estación del participante | `repo.maven.apache.org`, `repo1.maven.org` | HTTPS 443 | Saliente | Obligatorio. Si hay proxy corporativo, configúralo en `~/.m2/settings.xml`. |
| Estación del participante | `github.com` | HTTPS 443 | Saliente | Para clonar el repositorio. |
| Aplicaciones de las prácticas | PostgreSQL `localhost` | TCP 5432 | Local | Práctica 8. |
| Cliente HTTP de la Práctica 9 | WireMock `localhost` | HTTP 8080 y HTTPS 8443 | Local | Puertos libres; ciérralos al terminar. |
| Cliente WebSocket de la Práctica 9 | `NotificationServer` `localhost` | WS 8090 | Local | Puerto libre. |

Si el firewall de Windows pregunta al iniciar `java`, permite el acceso solo en **Redes privadas**. Todo el tráfico de las prácticas es local.

# Validación del Entorno <a id="validacion-entorno"></a>

| ID | Componente o capacidad | Procedimiento | Resultado esperado | Evidencia mínima |
|---|---|---|---|---|
| V-01 | JDK 11 | `java -version` y `javac -version` | Versión `11.0.23` en ambos | Salida de la terminal |
| V-02 | Maven | `mvn -version` | `Apache Maven 3.9.8` con `Java version: 11.0.23` | Salida de la terminal |
| V-03 | Compilación y acceso a Maven Central | Compilar el proyecto de prueba (ver [Comandos de Verificación](#comandos-verificacion)) | `BUILD SUCCESS` y salida `Entorno Java 11 listo` | Salida de la terminal |
| V-04 | Git y Git Bash | `git --version` | `git version 2.45.2` | Salida de la terminal |
| V-05 | PostgreSQL | `psql -U postgres -h localhost -p 5432 -c "SELECT version();"` | `PostgreSQL 16.3` | Salida de la terminal |
| V-06 | WireMock | Iniciar WireMock en el puerto 8080 y consultar `/__admin/mappings` | JSON con `"mappings"` | Salida de `curl` |
| V-07 | Servidor WebSocket | `java NotificationServer.java` en `~/labs/tools` | Mensaje `WebSocket escuchando en ws://localhost:8090/ws/notifications` | Salida de la terminal |
| V-08 | IntelliJ IDEA y SonarLint | Abrir el proyecto de prueba en el IDE y ejecutar **Analyze with SonarLint** | El IDE usa el SDK 11 y SonarLint muestra su ventana de resultados | Captura de pantalla autorizada |

**Criterio de entorno listo:** V-01 a V-08 aprobadas. Todos los componentes son obligatorios. Si falla V-05, V-06 o V-07, el participante puede iniciar el curso, pero debe corregirlo antes de la Práctica 8 (V-05) o de la Práctica 9 (V-06 y V-07).

# Comandos de Verificación <a id="comandos-verificacion"></a>

## JDK, Maven, Git y PostgreSQL

**Plataforma y terminal:** Windows (Git Bash), Linux o macOS (bash).  
**Directorio de ejecución:** cualquiera.  
**Prerrequisito:** terminal abierta después de configurar las variables de entorno.

```bash
java -version
javac -version
mvn -version
git --version
psql --version
```

**Resultado esperado:**

```text
openjdk version "11.0.23" 2024-04-16
...
javac 11.0.23
Apache Maven 3.9.8 (...)
Java version: 11.0.23, vendor: Eclipse Adoptium, ...
git version 2.45.2
psql (PostgreSQL) 16.3
```

**Interpretación:** confirma las versiones y el `PATH`. No prueba la conexión a PostgreSQL ni la descarga de dependencias: para eso usa las pruebas siguientes.

## Compilación de un proyecto Maven con Java 11

**Plataforma y terminal:** Windows (Git Bash), Linux o macOS (bash).  
**Directorio de ejecución:** `~/cursos`.  
**Prerrequisito:** acceso a Internet (Maven Central).

```bash
mkdir -p ~/cursos/verificacion-entorno/src/main/java/demo && cd ~/cursos/verificacion-entorno
cat > pom.xml <<'EOF'
<project xmlns="http://maven.apache.org/POM/4.0.0">
  <modelVersion>4.0.0</modelVersion>
  <groupId>demo</groupId>
  <artifactId>verificacion-entorno</artifactId>
  <version>1.0</version>
  <properties>
    <maven.compiler.release>11</maven.compiler.release>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>
  <build>
    <plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.13.0</version>
      </plugin>
    </plugins>
  </build>
</project>
EOF
cat > src/main/java/demo/Check.java <<'EOF'
package demo;
public class Check {
    public static void main(String[] args) {
        var version = Runtime.version().feature();
        System.out.println("Entorno Java " + version + " listo");
    }
}
EOF
mvn -q compile && java -cp target/classes demo.Check
```

**Resultado esperado:**

```text
Entorno Java 11 listo
```

**Interpretación:** confirma que Maven descarga plugins de Maven Central y compila con `release 11`. Al terminar V-08, borra la carpeta con `rm -rf ~/cursos/verificacion-entorno`.

## Conexión a PostgreSQL

**Plataforma y terminal:** Windows (Git Bash), Linux o macOS (bash).  
**Directorio de ejecución:** cualquiera.  
**Prerrequisito:** servicio de PostgreSQL iniciado.

```bash
psql -U postgres -h localhost -p 5432 -c "SELECT version();"
```

**Resultado esperado:**

```text
 PostgreSQL 16.3 on ...
(1 row)
```

**Interpretación:** confirma que el servicio escucha en el puerto 5432 y que la autenticación del superusuario funciona. En Git Bash para Windows, si `psql` no muestra la solicitud de contraseña, ejecuta `winpty psql ...`.

## WireMock y servidor WebSocket

**Plataforma y terminal:** Windows (Git Bash), Linux o macOS (bash), con dos terminales.  
**Directorio de ejecución:** `~/labs/tools`.  
**Prerrequisito:** archivos descargados en `~/labs/tools`.

Terminal 1:

```bash
cd ~/labs/tools
java -jar wiremock-standalone-3.6.0.jar --port 8080
```

Terminal 2:

```bash
curl -s http://localhost:8080/__admin/mappings
```

**Resultado esperado:**

```text
{
  "mappings" : [ ],
  "meta" : {
    "total" : 0
  }
}
```

Detén WireMock con `Ctrl+C` en la terminal 1 y, en la misma terminal, inicia el servidor WebSocket:

```bash
cd ~/labs/tools
java NotificationServer.java
```

**Resultado esperado:**

```text
[Servidor] WebSocket escuchando en ws://localhost:8090/ws/notifications
[Servidor] Presiona Ctrl+C para detener.
```

**Interpretación:** confirma que los puertos 8080 y 8090 están libres y que el JDK 11 ejecuta archivos fuente directamente. Detén el servidor con `Ctrl+C`. La comunicación HTTPS (puerto 8443) y WebSocket se prueban en la Práctica 9.

# Troubleshooting

| Síntoma | Causa probable | Comprobación | Corrección | Validación posterior |
|---|---|---|---|---|
| `java -version` muestra otra versión (8, 17, 21) | Otro JDK aparece antes en el `PATH` | `which -a java` | Pon `$JAVA_HOME/bin` del JDK 11 al inicio del `PATH` | V-01 |
| `mvn -version` muestra `Java version` distinta de 11 | `JAVA_HOME` apunta a otro JDK | `echo $JAVA_HOME` | Corrige `JAVA_HOME` y abre una terminal nueva | V-02 |
| `mvn: command not found` | Carpeta `bin` de Maven fuera del `PATH` | `echo $PATH` | Agrega `apache-maven-3.9.8/bin` al `PATH` | V-02 |
| Maven falla con `Could not transfer artifact` o `PKIX path building failed` | Proxy corporativo o inspección TLS | `curl -I https://repo.maven.apache.org/maven2/` | Configura el proxy en `~/.m2/settings.xml` o importa el certificado corporativo en el JDK (con TI) | V-03 |
| `release version 11 not supported` | Maven usa un JDK anterior a 11 | `mvn -version` | Corrige `JAVA_HOME` | V-03 |
| `psql: command not found` (Windows) | Carpeta `bin` de PostgreSQL fuera del `PATH` | `ls "/c/Program Files/PostgreSQL/16/bin"` | Agrega la carpeta al `PATH` | V-05 |
| `Connection refused` en el puerto 5432 | Servicio de PostgreSQL detenido | Windows: `services.msc` (`postgresql-x64-16`); Linux: `systemctl status postgresql` | Inicia el servicio | V-05 |
| `password authentication failed for user "postgres"` | Contraseña distinta a la definida en la instalación | — | Usa la contraseña correcta o restablécela con el administrador | V-05 |
| `Address already in use` / `BindException` en 8080, 8443 u 8090 | Otro proceso usa el puerto | Windows: `netstat -ano \| findstr :8080`; Linux/macOS: `lsof -i :8080` | Cierra el proceso o detén la instancia anterior de WireMock o del servidor | V-06 / V-07 |
| `error: class found on application class path` al ejecutar `java NotificationServer.java` | Hay un `NotificationServer.class` compilado en la carpeta | `ls ~/labs/tools` | Borra `NotificationServer.class` y ejecuta de nuevo | V-07 |
| SonarLint no aparece después de instalarlo | IDE sin reiniciar o versión del plugin incompatible | **Settings → Plugins → Installed** | Reinicia IntelliJ IDEA; usa SonarLint 10.4.0.79986 con IntelliJ IDEA 2024.1.4 | V-08 |

# Checklist Final

- [ ] La estación cumple el hardware mínimo (4 núcleos, 8 GB de RAM, 10 GB libres).
- [ ] JDK Temurin 11.0.23+9 instalado; `java`, `javac` y `keytool` disponibles.
- [ ] Maven 3.9.8 usa el JDK 11.
- [ ] Git 2.45.2 instalado y, en Windows, Git Bash disponible.
- [ ] IntelliJ IDEA CE 2024.1.4 configurado con el SDK 11 y Maven 3.9.8; SonarLint 10.4.0.79986 instalado.
- [ ] PostgreSQL 16.3 iniciado en `localhost:5432` y contraseña de `postgres` conocida por el participante.
- [ ] `~/labs/tools` contiene `wiremock-standalone-3.6.0.jar` y `NotificationServer.java`.
- [ ] Puertos 5432, 8080, 8443 y 8090 disponibles.
- [ ] Acceso a Maven Central comprobado (V-03).
- [ ] Validaciones V-01 a V-08 aprobadas y registradas.

# Resumen

Cada participante necesita, en su propia estación, el JDK Temurin 11.0.23+9 con Maven 3.9.8, IntelliJ IDEA CE 2024.1.4 con SonarLint, Git (Git Bash en Windows) y PostgreSQL 16.3 en el puerto 5432. Para la Práctica 9 también necesita WireMock Standalone 3.6.0 y el servidor WebSocket del curso en `~/labs/tools`. Las dependencias de Maven (driver JDBC de PostgreSQL y Bouncy Castle) se descargan solas durante las prácticas. Los proyectos, la base de datos y los KeyStores se crean dentro de cada práctica.

## Registro de validación

| Fecha | Responsable | Plataforma o entorno probado | Resultado | Limitaciones o acciones pendientes |
|---|---|---|---|---|
| 2026-11-09 | Revisión de contenido (asistida) | Windows 11 Pro, Git Bash, JDK 11.0.33 | Parcial | Se ejecutó `NotificationServer.java` (bienvenida, notificaciones, eco, cierre y reconexión) y se probó el HTTPS de `HttpClient` contra un KeyStore `localhost` con SAN (respuesta 200), además del fallo con `CN=SecureLab`. Se comprobó que existen en Maven Central `bcprov-jdk18on` 1.78.1, `postgresql` 42.7.3 y `wiremock-standalone` 3.6.0. **No** se ejecutaron de extremo a extremo las prácticas con Temurin 11.0.23, Maven 3.9.8, PostgreSQL 16.3 ni SonarLint. El instructor debe completar V-01 a V-08 antes del curso. |

## Referencias oficiales

| Documento o recurso | URL | Fecha de consulta |
|---|---|---|
| Eclipse Temurin 11.0.23+9 — binarios oficiales | [https://github.com/adoptium/temurin11-binaries/releases/tag/jdk-11.0.23%2B9](https://github.com/adoptium/temurin11-binaries/releases/tag/jdk-11.0.23%2B9) | 2026-11-09 |
| Apache Maven 3.9.8 — binarios | [https://archive.apache.org/dist/maven/maven-3/3.9.8/binaries/](https://archive.apache.org/dist/maven/maven-3/3.9.8/binaries/) | 2026-11-09 |
| Apache Maven — instalación | [https://maven.apache.org/install.html](https://maven.apache.org/install.html) | 2026-11-09 |
| IntelliJ IDEA — otras versiones | [https://www.jetbrains.com/idea/download/other.html](https://www.jetbrains.com/idea/download/other.html) | 2026-11-09 |
| SonarLint para IntelliJ IDEA | [https://plugins.jetbrains.com/plugin/7973-sonarlint](https://plugins.jetbrains.com/plugin/7973-sonarlint) | 2026-11-09 |
| Git — descargas | [https://git-scm.com/downloads](https://git-scm.com/downloads) | 2026-11-09 |
| Git for Windows 2.45.2 | [https://github.com/git-for-windows/git/releases/tag/v2.45.2.windows.1](https://github.com/git-for-windows/git/releases/tag/v2.45.2.windows.1) | 2026-11-09 |
| PostgreSQL — descargas | [https://www.postgresql.org/download/](https://www.postgresql.org/download/) | 2026-11-09 |
| PostgreSQL JDBC Driver 42.7.3 | [https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.3/](https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.3/) | 2026-11-09 |
| Bouncy Castle `bcprov-jdk18on` 1.78.1 | [https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk18on/1.78.1/](https://repo1.maven.org/maven2/org/bouncycastle/bcprov-jdk18on/1.78.1/) | 2026-11-09 |
| WireMock — ejecución standalone | [https://wiremock.org/docs/standalone/java-jar/](https://wiremock.org/docs/standalone/java-jar/) | 2026-11-09 |
| WireMock Standalone 3.6.0 (Maven Central) | [https://repo1.maven.org/maven2/org/wiremock/wiremock-standalone/3.6.0/](https://repo1.maven.org/maven2/org/wiremock/wiremock-standalone/3.6.0/) | 2026-11-09 |
| Java SE 11 — `keytool` | [https://docs.oracle.com/en/java/javase/11/tools/keytool.html](https://docs.oracle.com/en/java/javase/11/tools/keytool.html) | 2026-11-09 |
| Java SE 11 — `java.net.http` | [https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/package-summary.html](https://docs.oracle.com/en/java/javase/11/docs/api/java.net.http/java/net/http/package-summary.html) | 2026-11-09 |
