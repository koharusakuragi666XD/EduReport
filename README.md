<div align="center">
  <img src="src/main/resources/EduReportLogo.png" alt="EduReport" width="360">
  <p>Gestión escolar de aulas, alumnos e incidencias desde una aplicación de escritorio.</p>

  <img src="src/main/resources/EduReportIsotipo.png" alt="Isotipo de EduReport" width="96">

  <p>
    <a href="https://github.com/koharusakuragi666XD/EduReport">
      <img src="https://img.shields.io/badge/versión-1.3.0-1E66F5?style=for-the-badge" alt="Versión 1.3.0">
    </a>
    <a href="https://www.oracle.com/java/technologies/downloads/">
      <img src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25">
    </a>
    <a href="https://www.mongodb.com/">
      <img src="https://img.shields.io/badge/MongoDB-47A248?style=for-the-badge&logo=mongodb&logoColor=white" alt="MongoDB">
    </a>
    <a href="https://maven.apache.org/">
      <img src="https://img.shields.io/badge/Maven-3.9+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white" alt="Apache Maven">
    </a>
    <a href="https://opensource.org/licenses/MIT">
      <img src="https://img.shields.io/badge/licencia-MIT-yellow.svg?style=for-the-badge" alt="Licencia MIT">
    </a>
  </p>
</div>

## Sobre el proyecto

**EduReport** es una aplicación de escritorio desarrollada con Java Swing para centralizar la administración de aulas, alumnos y reportes escolares. Está pensada para agilizar el trabajo cotidiano: los datos se guardan en MongoDB, los alumnos pueden importarse desde Excel y los reportes se pueden consultar o exportar como PDF.

## Funciones

- **Aulas y alumnos:** creación, consulta, edición y eliminación con contadores actualizados.
- **Reportes individuales:** registro de incidencias académicas, de conducta o de asistencia con fecha, motivo y severidad.
- **Reportes grupales:** creación de un mismo reporte para varios alumnos mediante selección múltiple.
- **Importación masiva:** carga de alumnos desde `.xlsx`, con plantillas para alumnos independientes o aulas y alumnos combinados.
- **Exportación a PDF:** generación de reportes individuales y documentos con nombres de archivo seguros.
- **Búsqueda y filtros:** búsqueda tolerante a acentos y filtros por tipo o severidad.
- **Temas Catppuccin:** modos **Latte** y **Mocha**, con preferencia guardada entre ejecuciones.
- **Interfaz moderna:** FlatLaf, navegación por vistas, carga de datos en segundo plano e identidad visual propia.
- **Ayuda y créditos:** guía integrada para Excel, créditos de autoría y acceso al perfil de la artista.

## Tecnologías

| Área | Tecnología |
| :--- | :--- |
| Lenguaje y UI | Java 25, Java Swing |
| Apariencia | FlatLaf 3.7.2, paletas Catppuccin Latte/Mocha |
| Persistencia | MongoDB con `mongodb-driver-sync` |
| Documentos | Apache POI para Excel e iTextPDF para PDF |
| Proyecto | Apache Maven, JAR ejecutable con Shade |
| Pruebas | JUnit 5 |

## Requisitos

- Windows 10/11.
- JDK 25 configurado en `JAVA_HOME`.
- Apache Maven 3.9 o superior en `PATH`.
- MongoDB ejecutándose localmente en el puerto `27017`.

> La versión del proyecto está fijada a Java 25 mediante Maven Enforcer. MongoDB debe estar disponible antes de iniciar la aplicación.

## Instalación y configuración

```bash
git clone https://github.com/koharusakuragi666XD/EduReport.git
cd EduReport
```

La configuración predeterminada usa:

```text
URI:      mongodb://localhost:27017
Database: edureport
```

Para cambiarla, copia `.env.example` como referencia y define estas variables en tu entorno:

- `EDUREPORT_MONGODB_URI`
- `EDUREPORT_MONGODB_DATABASE`

En Windows (`cmd.exe`):

```bat
set EDUREPORT_MONGODB_URI=mongodb://localhost:27017
set EDUREPORT_MONGODB_DATABASE=edureport
```

## Compilar y ejecutar

Desde la carpeta del proyecto:

```bat
mvn clean package
java -jar target\EduReport-1.3.0.jar
```

Maven genera un JAR ejecutable con las dependencias incluidas. Si MongoDB no está disponible, EduReport muestra un error controlado y termina sin dejar una excepción sin manejar.

## Importar desde Excel

Los encabezados deben coincidir con una de estas estructuras:

### Solo alumnos

| Nombre | Apellido | Edad |
| :--- | :--- | :--- |
| Juan | Pérez | 15 |

### Aulas y alumnos combinados

| Especialidad | Grado | Grupo | Turno | Nombre | Apellido | Edad |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| Informática | 5 | A | Matutino | Ana | Gómez | 17 |

La aplicación incluye una opción de ayuda con las instrucciones de importación y la plantilla esperada.

## Estructura principal

```text
src/
├── main/java/com/mycompany/edureport/
│   ├── conexion/       # conexión y configuración de MongoDB
│   ├── dao/            # acceso a aulas, alumnos y reportes
│   ├── interfaz/       # ventanas, paneles, temas e importación
│   └── modelo/         # entidades del dominio
├── main/resources/     # logotipo, isotipo e iconos
└── test/                # pruebas automatizadas
```

## Historial reciente

### 1.3.0 — Identidad visual y productividad

- Se incorporaron el logotipo y el isotipo oficiales de EduReport.
- Se añadió el isotipo como icono de la ventana principal.
- Se mejoró la pantalla de créditos con la identidad visual, atribuciones y enlaces.
- Se añadió la creación de reportes grupales para varios alumnos.
- Se guardó la preferencia del último tema utilizado.
- Se mantiene el JAR ejecutable con Maven Shade.

### 1.2.0 — Interfaz y configuración

- Se reorganizó la interfaz bajo `com.mycompany.edureport.interfaz`.
- Se añadieron los modos Catppuccin Latte y Catppuccin Mocha.
- Se añadió configuración opcional de MongoDB mediante variables de entorno.

### 1.0.1

- Se corrigió la asociación de alumnos con aulas durante la importación masiva.
- Se corrigieron los contadores del resumen de importación.
- Se corrigió la generación de nombres seguros para archivos PDF.
- Se cerraron correctamente los archivos Excel después de importarlos.

### 1.1.0

- Se documentó el soporte para Windows 10/11.
- Se actualizó el requisito a Java 25 LTS.
- Se incorporó el JAR ejecutable con las dependencias incluidas.
- Se mejoró el mensaje mostrado cuando MongoDB no está disponible.

## Próximas mejoras

- [ ] Inicio de sesión y asignación de roles.
- [ ] Módulo de control de asistencia.
- [ ] Panel de seguimiento académico.
- [ ] Estadísticas gráficas para el sector directivo.

## Créditos

- **Desarrollo:** Sergio Said López Calvario.
- **Logotipo e isotipo:** Constanza Ureste Osorio.
- **Código:** disponible bajo licencia [MIT](LICENSE).
