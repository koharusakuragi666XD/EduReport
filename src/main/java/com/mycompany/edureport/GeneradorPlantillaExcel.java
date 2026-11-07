package com.mycompany.edureport;
import com.mycompany.edureport.interfaz.ImportadorExcel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.DataValidationConstraint.OperatorType;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Genera archivos .xlsx vacíos con el formato que espera {@link ImportadorExcel}.
 *
 * <p>La hoja <b>"Datos"</b> es siempre la primera, porque el importador solo lee la
 * primera hoja. La hoja <b>"Instrucciones"</b> va después y nunca se importa.</p>
 *
 * <p>Si cambias el orden o el número de columnas en ImportadorExcel, actualiza
 * las constantes de encabezados de esta clase.</p>
 */
public class GeneradorPlantillaExcel {

    /** Formato de "Importar Excel" dentro de un aula: A=Nombre, B=Apellido, C=Edad. */
    private static final String[] ENCABEZADOS_ALUMNOS = {"Nombre", "Apellido", "Edad"};

    /** Formato de la carga masiva: A..G, en este orden. */
    private static final String[] ENCABEZADOS_MASIVO =
            {"Especialidad", "Grado", "Grupo", "Turno", "Nombre", "Apellido", "Edad"};

    /** Cuántas filas (desde la 2) llevan validación de datos. */
    private static final int FILAS_VALIDADAS = 1000;

    private GeneradorPlantillaExcel() { }

    public static void generarPlantillaAlumnos(File destino) throws IOException {
        String[] instrucciones = {
                "1. Escribe a tus alumnos en la hoja \"Datos\", un alumno por fila, desde la fila 2.",
                "2. No cambies el orden de las columnas. La fila 1 se ignora al importar.",
                "3. Guarda el archivo como .xlsx. En EduReport abre el aula y pulsa \"Importar Excel\".",
                "",
                "Reglas:",
                "  - Nombre y Apellido son obligatorios (si falta alguno, la fila se omite).",
                "  - Edad es un número entero, por ejemplo 16.",
                "  - No uses fórmulas: pega los datos como valores.",
                "  - Esta hoja no se importa; solo se lee la primera hoja (\"Datos\")."
        };
        String[][] ejemplo = {
                {"Ana", "López", "16"},
                {"Luis", "Pérez", "17"}
        };
        // Columna C (índice 2) = Edad
        generar(destino, ENCABEZADOS_ALUMNOS, instrucciones, ejemplo, -1, 2);
    }

    public static void generarPlantillaMasiva(File destino) throws IOException {
        String[] instrucciones = {
                "1. Escribe un alumno por fila en la hoja \"Datos\", desde la fila 2.",
                "2. En cada fila repite los datos de su aula (Especialidad, Grado, Grupo y Turno).",
                "3. Guarda el archivo como .xlsx. En la pantalla principal pulsa \"Importar Excel\".",
                "",
                "Reglas:",
                "  - Cada combinación de Especialidad + Grado + Grupo + Turno es un aula.",
                "  - Escribe igual esos datos en todas las filas de un aula (mayúsculas y acentos cuentan).",
                "  - Grado es solo el número (3, no \"3°\"). Edad es un número entero.",
                "  - Obligatorios: Especialidad, Grupo, Turno, Nombre y Apellido.",
                "  - No uses fórmulas: pega los datos como valores.",
                "  - Esta hoja no se importa; solo se lee la primera hoja (\"Datos\")."
        };
        String[][] ejemplo = {
                {"Programación", "3", "A", "Matutino", "Ana", "López", "16"},
                {"Programación", "3", "A", "Matutino", "Luis", "Pérez", "17"},
                {"Contabilidad", "1", "A", "Vespertino", "Diego", "Soto", "15"}
        };
        // Columna B (índice 1) = Grado, columna G (índice 6) = Edad
        generar(destino, ENCABEZADOS_MASIVO, instrucciones, ejemplo, 1, 6);
    }

    // =====================================================================

    private static void generar(File destino, String[] encabezados, String[] instrucciones,
                                String[][] ejemplo, int colGrado, int colEdad) throws IOException {

        try (Workbook libro = new XSSFWorkbook()) {

            // ---- Estilos ----
            Font fuenteEncabezado = libro.createFont();
            fuenteEncabezado.setBold(true);
            fuenteEncabezado.setColor(IndexedColors.WHITE.getIndex());

            CellStyle estiloEncabezado = libro.createCellStyle();
            estiloEncabezado.setFont(fuenteEncabezado);
            estiloEncabezado.setFillForegroundColor(IndexedColors.GREY_80_PERCENT.getIndex());
            estiloEncabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            estiloEncabezado.setAlignment(HorizontalAlignment.CENTER);
            estiloEncabezado.setVerticalAlignment(VerticalAlignment.CENTER);

            Font fuenteTitulo = libro.createFont();
            fuenteTitulo.setBold(true);
            fuenteTitulo.setFontHeightInPoints((short) 14);
            CellStyle estiloTitulo = libro.createCellStyle();
            estiloTitulo.setFont(fuenteTitulo);

            Font fuenteNegrita = libro.createFont();
            fuenteNegrita.setBold(true);
            CellStyle estiloNegrita = libro.createCellStyle();
            estiloNegrita.setFont(fuenteNegrita);

            // ---- Hoja 1: "Datos" (la que se importa) ----
            Sheet datos = libro.createSheet("Datos");
            Row filaEncabezado = datos.createRow(0);
            filaEncabezado.setHeightInPoints(22);
            for (int i = 0; i < encabezados.length; i++) {
                Cell c = filaEncabezado.createCell(i);
                c.setCellValue(encabezados[i]);
                c.setCellStyle(estiloEncabezado);
                datos.setColumnWidth(i, anchoColumna(encabezados[i]));
            }
            datos.createFreezePane(0, 1);   // el encabezado queda fijo al hacer scroll

            // Validación: evita escribir "3°" o "16 años" en las columnas numéricas.
            if (colGrado >= 0) {
                agregarValidacionEntero(datos, colGrado, OperatorType.GREATER_OR_EQUAL, "1", null,
                        "Grado",
                        "Escribe solo el número, por ejemplo 3.",
                        "Escribe solo un número entero mayor o igual a 1 (sin \"°\" ni texto).");
            }
            agregarValidacionEntero(datos, colEdad, OperatorType.BETWEEN, "1", "120",
                    "Edad",
                    "Escribe un número entero, por ejemplo 16.",
                    "Escribe solo un número entero entre 1 y 120.");

            // ---- Hoja 2: "Instrucciones" (no se importa) ----
            Sheet ayuda = libro.createSheet("Instrucciones");
            for (int i = 0; i < encabezados.length; i++) {
                ayuda.setColumnWidth(i, anchoColumna(encabezados[i]));
            }

            int fila = 0;
            Cell titulo = ayuda.createRow(fila++).createCell(0);
            titulo.setCellValue("Cómo usar esta plantilla");
            titulo.setCellStyle(estiloTitulo);
            fila++;

            for (String linea : instrucciones) {
                Cell c = ayuda.createRow(fila++).createCell(0);
                c.setCellValue(linea);
                if ("Reglas:".equals(linea)) c.setCellStyle(estiloNegrita);
            }

            fila++;
            Cell lblEjemplo = ayuda.createRow(fila++).createCell(0);
            lblEjemplo.setCellValue("Ejemplo de cómo se llena la hoja \"Datos\":");
            lblEjemplo.setCellStyle(estiloNegrita);

            Row encEjemplo = ayuda.createRow(fila++);
            for (int i = 0; i < encabezados.length; i++) {
                Cell c = encEjemplo.createCell(i);
                c.setCellValue(encabezados[i]);
                c.setCellStyle(estiloEncabezado);
            }
            for (String[] datosFila : ejemplo) {
                Row r = ayuda.createRow(fila++);
                for (int i = 0; i < datosFila.length; i++) {
                    Cell c = r.createCell(i);
                    // Grado y Edad como número real, igual que debe quedar en la hoja de datos.
                    if (i == colGrado || i == colEdad) {
                        c.setCellValue(Integer.parseInt(datosFila[i]));
                    } else {
                        c.setCellValue(datosFila[i]);
                    }
                }
            }

            libro.setActiveSheet(0);   // se abre en "Datos"

            try (OutputStream salida = new FileOutputStream(destino)) {
                libro.write(salida);
            }
        }
    }

    private static void agregarValidacionEntero(Sheet hoja, int columna, int operador,
                                                String valor1, String valor2,
                                                String tituloAviso, String mensajeAviso,
                                                String mensajeError) {
        DataValidationHelper ayudante = hoja.getDataValidationHelper();
        DataValidationConstraint regla = ayudante.createIntegerConstraint(operador, valor1, valor2);
        CellRangeAddressList rango = new CellRangeAddressList(1, FILAS_VALIDADAS, columna, columna);
        DataValidation validacion = ayudante.createValidation(regla, rango);

        validacion.setShowPromptBox(true);
        validacion.createPromptBox(tituloAviso, mensajeAviso);
        validacion.setShowErrorBox(true);
        validacion.setErrorStyle(DataValidation.ErrorStyle.STOP);
        validacion.createErrorBox("Dato no válido", mensajeError);

        hoja.addValidationData(validacion);
    }

    /** Ancho en unidades de POI (1/256 de carácter), con un mínimo cómodo. */
    private static int anchoColumna(String encabezado) {
        int caracteres = Math.max(14, encabezado.length() + 6);
        return caracteres * 256;
    }
}