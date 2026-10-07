package com.mycompany.edureport;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import com.mycompany.edureport.dao.AlumnoDAO;
import com.mycompany.edureport.dao.ReporteDAO;
import com.mycompany.edureport.modelo.Alumno;
import com.mycompany.edureport.modelo.Aula;
import com.mycompany.edureport.modelo.Reporte;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Servicio de presentación que transforma los datos de un aula en un PDF.
 *
 * <p>No consulta MongoDB directamente: recibe DAO y aula como dependencias.
 * Esto permite reutilizarlo con otra fuente de datos al migrar el proyecto.</p>
 */
public class GeneradorPDF {

    private static final Font FUENTE_TITULO = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
    private static final Font FUENTE_SUBTITULO = new Font(Font.FontFamily.HELVETICA, 13, Font.BOLD);
    private static final Font FUENTE_ALUMNO = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
    private static final Font FUENTE_NORMAL = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);
    private static final Font FUENTE_PEQUEÑA = new Font(Font.FontFamily.HELVETICA, 9, Font.ITALIC);

    private static final BaseColor COLOR_GRAVE    = new BaseColor(200, 50, 50);
    private static final BaseColor COLOR_MODERADO = new BaseColor(200, 150, 0);
    private static final BaseColor COLOR_LEVE     = new BaseColor(50, 150, 50);
    private static final BaseColor COLOR_HEADER   = new BaseColor(52, 73, 94);
    private static final BaseColor COLOR_FILA     = new BaseColor(236, 240, 241);

    /** Genera el archivo completo y devuelve su ruta absoluta. */
    public static String generarReportePorAula(Aula aula, AlumnoDAO alumnoDAO,
                                               ReporteDAO reporteDAO) throws Exception {
        // El archivo se guarda en la carpeta personal del usuario para evitar depender
        // de una ruta fija del proyecto o de permisos de escritura especiales.
        String nombreArchivo = "Reporte_" + aula.getEspecialidad().replace(" ", "_")
                + "_" + aula.getGrado() + aula.getGrupo()
                + "_" + aula.getTurno() + ".pdf";

        String ruta = System.getProperty("user.home") + "/" + nombreArchivo;

        Document documento = new Document(PageSize.A4, 40, 40, 60, 40);
        PdfWriter writer = PdfWriter.getInstance(documento, new FileOutputStream(ruta));
        writer.setPageEvent(new PieDePageina());
        documento.open();

        agregarEncabezado(documento, aula);
        agregarResumen(documento, aula, alumnoDAO, reporteDAO);
        agregarDetalleAlumnos(documento, aula, alumnoDAO, reporteDAO);

        documento.close();
        return ruta;
    }

    private static void agregarEncabezado(Document documento, Aula aula) throws Exception {
        Paragraph titulo = new Paragraph("EduReport", FUENTE_TITULO);
        titulo.setAlignment(Element.ALIGN_CENTER);
        documento.add(titulo);

        Paragraph subtitulo = new Paragraph(
                "Reporte de Conducta — " + aula.getEspecialidad() +
                        " | " + aula.getGrado() + "° " + aula.getGrupo() +
                        " | " + aula.getTurno(), FUENTE_SUBTITULO
        );
        subtitulo.setAlignment(Element.ALIGN_CENTER);
        subtitulo.setSpacingBefore(4);
        subtitulo.setSpacingAfter(4);
        documento.add(subtitulo);

        String fechaHoy = new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date());
        Paragraph fecha = new Paragraph("Generado el: " + fechaHoy, FUENTE_PEQUEÑA);
        fecha.setAlignment(Element.ALIGN_CENTER);
        fecha.setSpacingAfter(16);
        documento.add(fecha);

        LineSeparator linea = new LineSeparator();
        linea.setLineColor(COLOR_HEADER);
        documento.add(new Chunk(linea));
        documento.add(Chunk.NEWLINE);
    }

    private static void agregarResumen(Document documento, Aula aula,
                                       AlumnoDAO alumnoDAO, ReporteDAO reporteDAO) throws Exception {
        Paragraph tituloResumen = new Paragraph("Resumen del Grupo", FUENTE_SUBTITULO);
        tituloResumen.setSpacingBefore(10);
        tituloResumen.setSpacingAfter(8);
        documento.add(tituloResumen);

        List<Alumno> alumnos = alumnoDAO.obtenerPorAula(aula.getId());
        int totalAlumnos = alumnos.size();
        int totalReportes = 0;
        int graves = 0, moderados = 0, leves = 0;
        int alumnosSinReporte = 0;

        // El resumen recorre todos los alumnos para contar reportes y severidades del aula.
        for (Alumno alumno : alumnos) {
            List<Reporte> reportes = reporteDAO.obtenerPorAlumnoOrdenados(alumno.getId());
            totalReportes += reportes.size();
            if (reportes.isEmpty()) alumnosSinReporte++;
            for (Reporte r : reportes) {
                switch (r.getSeveridad()) {
                    case "Grave":    graves++;    break;
                    case "Moderado": moderados++; break;
                    default:         leves++;     break;
                }
            }
        }

        PdfPTable tabla = new PdfPTable(3);
        tabla.setWidthPercentage(100);
        tabla.setSpacingAfter(16);

        agregarCeldaResumen(tabla, "Total Alumnos", String.valueOf(totalAlumnos), COLOR_HEADER);
        agregarCeldaResumen(tabla, "Total Reportes", String.valueOf(totalReportes), COLOR_HEADER);
        agregarCeldaResumen(tabla, "Sin Reportes", String.valueOf(alumnosSinReporte), COLOR_HEADER);
        agregarCeldaResumen(tabla, "Graves", String.valueOf(graves), COLOR_GRAVE);
        agregarCeldaResumen(tabla, "Moderados", String.valueOf(moderados), COLOR_MODERADO);
        agregarCeldaResumen(tabla, "Leves", String.valueOf(leves), COLOR_LEVE);

        documento.add(tabla);
    }

    private static void agregarCeldaResumen(PdfPTable tabla, String etiqueta,
                                            String valor, BaseColor color) {
        PdfPCell celda = new PdfPCell();
        celda.setPadding(10);
        celda.setBackgroundColor(color);

        Paragraph p = new Paragraph();
        Font fValor = new Font(Font.FontFamily.HELVETICA, 20, Font.BOLD, BaseColor.WHITE);
        Font fEtiqueta = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL, BaseColor.WHITE);
        p.add(new Chunk(valor + "\n", fValor));
        p.add(new Chunk(etiqueta, fEtiqueta));
        p.setAlignment(Element.ALIGN_CENTER);
        celda.addElement(p);
        tabla.addCell(celda);
    }

    /** Añade una sección por alumno y sus reportes, o un mensaje si no existen. */
    private static void agregarDetalleAlumnos(Document documento, Aula aula,
                                              AlumnoDAO alumnoDAO, ReporteDAO reporteDAO) throws Exception {
        Paragraph tituloDetalle = new Paragraph("Detalle por Alumno", FUENTE_SUBTITULO);
        tituloDetalle.setSpacingBefore(10);
        tituloDetalle.setSpacingAfter(8);
        documento.add(tituloDetalle);

        List<Alumno> alumnos = alumnoDAO.obtenerPorAula(aula.getId());
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        boolean filaSombreada = false;

        for (Alumno alumno : alumnos) {
            List<Reporte> reportes = reporteDAO.obtenerPorAlumnoOrdenados(alumno.getId());

            PdfPTable tablaAlumno = new PdfPTable(1);
            tablaAlumno.setWidthPercentage(100);
            tablaAlumno.setSpacingBefore(6);
            tablaAlumno.setSpacingAfter(4);

            PdfPCell celdaNombre = new PdfPCell();
            celdaNombre.setBackgroundColor(filaSombreada ? COLOR_FILA : BaseColor.WHITE);
            celdaNombre.setPadding(8);

            String textoAlumno = alumno.getApellido() + ", " + alumno.getNombre()
                    + "   |   Edad: " + alumno.getEdad()
                    + "   |   Reportes: " + reportes.size();

            Font fAlumno = reportes.isEmpty() ? FUENTE_ALUMNO :
                    new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD,
                            reportes.size() >= 3 ? COLOR_GRAVE : COLOR_MODERADO);

            celdaNombre.addElement(new Paragraph(textoAlumno, fAlumno));
            tablaAlumno.addCell(celdaNombre);
            documento.add(tablaAlumno);

            if (reportes.isEmpty()) {
                Paragraph sinReportes = new Paragraph("   Sin reportes registrados.", FUENTE_PEQUEÑA);
                sinReportes.setSpacingAfter(4);
                documento.add(sinReportes);
            } else {
                PdfPTable tablaReportes = new PdfPTable(new float[]{2f, 2f, 2f, 5f});
                tablaReportes.setWidthPercentage(97);
                tablaReportes.setSpacingAfter(4);

                agregarCeldaEncabezado(tablaReportes, "Fecha");
                agregarCeldaEncabezado(tablaReportes, "Tipo");
                agregarCeldaEncabezado(tablaReportes, "Severidad");
                agregarCeldaEncabezado(tablaReportes, "Motivo");

                for (Reporte r : reportes) {
                    BaseColor colorSev;
                    switch (r.getSeveridad()) {
                        case "Grave":    colorSev = COLOR_GRAVE;    break;
                        case "Moderado": colorSev = COLOR_MODERADO; break;
                        default:         colorSev = COLOR_LEVE;     break;
                    }
                    Font fSev = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, colorSev);

                    tablaReportes.addCell(new Phrase(sdf.format(r.getFecha()), FUENTE_NORMAL));
                    tablaReportes.addCell(new Phrase(r.getTipo(), FUENTE_NORMAL));
                    PdfPCell celdaSev = new PdfPCell(new Phrase(r.getSeveridad(), fSev));
                    celdaSev.setPadding(4);
                    tablaReportes.addCell(celdaSev);
                    tablaReportes.addCell(new Phrase(r.getMotivo(), FUENTE_NORMAL));
                }
                documento.add(tablaReportes);
            }
            filaSombreada = !filaSombreada;
        }
    }

    private static void agregarCeldaEncabezado(PdfPTable tabla, String texto) {
        Font f = new Font(Font.FontFamily.HELVETICA, 9, Font.BOLD, BaseColor.WHITE);
        PdfPCell celda = new PdfPCell(new Phrase(texto, f));
        celda.setBackgroundColor(COLOR_HEADER);
        celda.setPadding(5);
        tabla.addCell(celda);
    }

    static class PieDePageina extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            // iText invoca este evento en cada pagina, por eso el numero se actualiza solo.
            PdfContentByte cb = writer.getDirectContent();
            Font f = new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.GRAY);
            Phrase pie = new Phrase("EduReport  —  Página " + writer.getPageNumber(), f);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER, pie,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 10, 0);
        }
    }
}