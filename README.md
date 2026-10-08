# EduReport

![Java](https://shields.io)
![MongoDB](https://shields.io)
![Maven](https://shields.io)

EduReport es una aplicación de escritorio desarrollada en **Java Swing** diseñada para administrar de manera eficiente aulas, alumnos e incidencias escolares.

## Funciones principales

- **Gestión escolar:** Registrar y administrar aulas, alumnos y reportes de conducta de forma centralizada.
- **Importación masiva:** Carga de alumnos de manera rápida desde archivos Excel.
- **Reportes:** Generación automática de reportes en formato PDF.
- **Persistencia:** Almacenamiento seguro de toda la información en MongoDB.

## Tecnologías utilizadas

- **Lenguaje:** Java
- **Interfaz Gráfica:** Java Swing
- **Base de Datos:** MongoDB (NoSQL)
- **Gestor de Dependencias:** Maven
- **Librerías Clave:** Apache POI (manejo de Excel) e iTextPDF (generación de documentos)

## Requisitos previos

Antes de comenzar, asegúrate de contar con lo siguiente:
- **JDK** compatible con la versión indicada en el archivo `pom.xml`.
- **Apache Maven** instalado y configurado.
- **MongoDB** ejecutándose localmente en el puerto predeterminado `27017`.

## Instalación y configuración

Sigue estos pasos para clonar y preparar el proyecto:

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/koharusakuragi666XD/EduReport.git
   ```

2. **Acceder al directorio del proyecto:**
   ```bash
   cd EduReport
   ```

3. **Verificar la base de datos:**
   Asegúrate de que tu servicio de MongoDB esté activo. El sistema creará o utilizará automáticamente la base de datos llamada `"edureport"`.

4. **Compilar el proyecto:**
   Genera el empaquetado del proyecto ejecutando:
   ```bash
   mvn clean package
   ```

## Ejecución

Para iniciar la aplicación, abre el proyecto en tu IDE favorito compatible con Maven (como NetBeans, IntelliJ IDEA o Eclipse) y ejecuta la clase principal:
```text
com.mycompany.edureport.EduReport
```

---

## Formato de importación en Excel

Para que la importación masiva de datos funcione correctamente, los archivos de Excel deben respetar las siguientes estructuras de columnas:

### Opción A: Solo Alumnos

| Nombre | Apellido | Edad |
| :--- | :--- | :--- |
| *Ej: Juan* | *Ej: Pérez* | *Ej: 15* |

### Opción B: Aulas y Alumnos combinados

| Especialidad | Grado | Grupo | Turno | Nombre | Apellido | Edad |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| *Ej: Informática* | *Ej: 5* | *Ej: A* | *Ej: Matutino* | *Ej: Ana* | *Ej: Gómez* | *Ej: 17* |

---

## Mejoras futuras

- [ ] Sistema de inicio de sesión y asignación de roles de usuario.
- [ ] Módulo para el control de asistencia.
- [ ] Panel de seguimiento académico detallado.
- [ ] Estadísticas gráficas avanzadas para el sector directivo.
- [ ] Migración a una configuración externa para la cadena de conexión de MongoDB.

## Estado del proyecto

*   **Versión actual:** 1.0 (Funcional).
*   Desarrollado inicialmente como un anteproyecto escolar enfocado en bases de datos No Relacionales.
