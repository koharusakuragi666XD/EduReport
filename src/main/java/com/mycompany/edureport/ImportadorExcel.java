package com.mycompany.edureport;

import com.mycompany.edureport.dao.AlumnoDAO;
import com.mycompany.edureport.dao.AulaDAO;
import com.mycompany.edureport.modelo.Aula;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * Adaptador entre hojas XLSX y los modelos persistidos por los DAO.
 *
 * Hay dos formatos: alumnos para un aula ya existente, y aulas+alumnos
 * para una carga masiva. La primera fila de ambos formatos se reserva para
 * encabezados y los errores se acumulan para reportarlos al final.
 */
public class ImportadorExcel {

    // Importa tres columnas: Nombre, Apellido y Edad; la primera fila se trata como encabezado.
    public static void importarAlumnos(JFrame parent, Aula aula,
                                       AlumnoDAO alumnoDAO, Runnable alRefrescar) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Selecciona el archivo Excel de alumnos");
        selector.setFileFilter(new FileNameExtensionFilter("Archivos Excel", "xlsx"));

        int resultado = selector.showOpenDialog(parent);
        if (resultado != JFileChooser.APPROVE_OPTION) return;

        String ruta = selector.getSelectedFile().getAbsolutePath();
        List<String> errores = new ArrayList<>();
        int insertados = 0;

        try (FileInputStream fis = new FileInputStream(ruta);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet hoja = workbook.getSheetAt(0);
            int filaNum = 0;

            for (Row fila : hoja) {
                if (filaNum == 0) { filaNum++; continue; }
                filaNum++;

                try {
                    if (fila.getLastCellNum() < 3) {
                        errores.add("Fila " + filaNum + ": faltan columnas.");
                        continue;
                    }

                    String nombre   = getCeldaString(fila.getCell(0));
                    String apellido = getCeldaString(fila.getCell(1));
                    int edad        = (int) getCeldaNumero(fila.getCell(2));

                    if (nombre.isEmpty() || apellido.isEmpty()) {
                        errores.add("Fila " + filaNum + ": nombre o apellido vacío.");
                        continue;
                    }

                    alumnoDAO.insertar(nombre, apellido, edad, aula.getId());
                    insertados++;

                } catch (Exception e) {
                    errores.add("Fila " + filaNum + ": " + e.getMessage());
                }
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                    "Error al leer el archivo:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        mostrarResumen(parent, errores, insertados, 0);
        alRefrescar.run();
    }

    public static void importarAulasYAlumnos(JFrame parent, AulaDAO aulaDAO,
                                             AlumnoDAO alumnoDAO, Runnable alRefrescar) {
        String ruta = seleccionarArchivo(parent);
        if (ruta == null) return;

        List<String> errores = new ArrayList<>();
        int aulasCreadas = 0;
        int alumnosInsertados = 0;
        Map<String, String> aulasMap = new LinkedHashMap<>();

        try {
            Workbook workbook = leerArchivo(ruta);
            Sheet hoja = workbook.getSheetAt(0);
            procesarFilas(hoja, aulaDAO, alumnoDAO, errores, aulasMap, aulasCreadas, alumnosInsertados);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                    "Error al leer el archivo:\n" + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        mostrarResumen(parent, errores, alumnosInsertados, aulasCreadas);
        alRefrescar.run();
    }

    private static String seleccionarArchivo(JFrame parent) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Selecciona el archivo Excel de aulas y alumnos");
        selector.setFileFilter(new FileNameExtensionFilter("Archivos Excel", "xlsx"));

        int resultado = selector.showOpenDialog(parent);
        if (resultado != JFileChooser.APPROVE_OPTION) return null;

        return selector.getSelectedFile().getAbsolutePath();
    }

    private static Workbook leerArchivo(String ruta) throws IOException {
        try (FileInputStream fis = new FileInputStream(ruta)) {
            return new XSSFWorkbook(fis);
        }
    }

    private static void procesarFilas(Sheet hoja, AulaDAO aulaDAO, AlumnoDAO alumnoDAO,
                                       List<String> errores, Map<String, String> aulasMap,
                                       int aulasCreadas, int alumnosInsertados) {
        int filaNum = 0;

        for (Row fila : hoja) {
            if (filaNum == 0) { filaNum++; continue; }
            filaNum++;

            try {
                // Formato esperado: Especialidad, Grado, Grupo, Turno, Nombre, Apellido, Edad.
                if (fila.getLastCellNum() < 7) {
                    errores.add("Fila " + filaNum + ": faltan columnas.");
                    continue;
                }

                String especialidad = getCeldaString(fila.getCell(0));
                int    grado        = (int) getCeldaNumero(fila.getCell(1));
                String grupo        = getCeldaString(fila.getCell(2));
                String turno        = getCeldaString(fila.getCell(3));
                String nombre       = getCeldaString(fila.getCell(4));
                String apellido     = getCeldaString(fila.getCell(5));
                int    edad         = (int) getCeldaNumero(fila.getCell(6));

                if (especialidad.isEmpty() || grupo.isEmpty() || turno.isEmpty()
                        || nombre.isEmpty() || apellido.isEmpty()) {
                    errores.add("Fila " + filaNum + ": hay campos vacíos.");
                    continue;
                }

                String claveAula = especialidad + "|" + grado + "|" + grupo + "|" + turno;

                // El mapa evita crear varias aulas cuando varias filas pertenecen al mismo grupo.
                if (!aulasMap.containsKey(claveAula)) {
                    aulaDAO.insertar(especialidad, grado, grupo, turno);
                    List<Aula> aulas = aulaDAO.obtenerTodas();
                    String nuevoId = aulas.get(aulas.size() - 1).getId();
                    aulasMap.put(claveAula, nuevoId);
                    aulasCreadas++;
                }

                String aulaId = aulasMap.get(claveAula);
                alumnoDAO.insertar(nombre, apellido, edad, aulaId);
                alumnosInsertados++;

            } catch (Exception e) {
                errores.add("Fila " + filaNum + ": " + e.getMessage());
            }
        }
    }

    // Centralizar la conversión evita repetir el tratamiento de celdas vacías o numéricas.
    private static String getCeldaString(Cell celda) {
        if (celda == null) return "";
        switch (celda.getCellType()) {
            case STRING:  return celda.getStringCellValue().trim();
            case NUMERIC: return String.valueOf((int) celda.getNumericCellValue());
            default:      return "";
        }
    }

    // Se devuelve double porque Apache POI representa así los valores numéricos.
    private static double getCeldaNumero(Cell celda) {
        if (celda == null) return 0;
        switch (celda.getCellType()) {
            case NUMERIC: return celda.getNumericCellValue();
            case STRING:  return Double.parseDouble(celda.getStringCellValue().trim());
            default:      return 0;
        }
    }

    private static void mostrarResumen(JFrame parent, List<String> errores,
                                       int alumnosInsertados, int aulasCreadas) {
        StringBuilder resumen = new StringBuilder();
        resumen.append("Importacion completada.\n");

        if (aulasCreadas > 0) {
            resumen.append("Aulas creadas: ").append(aulasCreadas).append("\n");
        }

        resumen.append("Alumnos insertados: ").append(alumnosInsertados).append("\n");

        if (!errores.isEmpty()) {
            resumen.append("\nFilas con errores:\n");
            for (String error : errores) {
                resumen.append("  • ").append(error).append("\n");
            }
        }

        JOptionPane.showMessageDialog(parent, resumen.toString(),
                errores.isEmpty() ? "Exito" : "Completado con advertencias",
                errores.isEmpty() ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
    }
}
