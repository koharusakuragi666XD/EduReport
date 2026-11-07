package com.mycompany.edureport.interfaz;
import com.mycompany.edureport.*;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Ventana informativa que explica cómo preparar los archivos .xlsx que
 * acepta {@link ImportadorExcel}. Tiene una pestaña por cada tipo de importación.
 *
 * <p>Si cambias el formato en ImportadorExcel (columnas, validaciones), actualiza
 * los textos de {@link #htmlAlumnos()} y {@link #htmlMasivo()}.</p>
 */
public class DialogoAyudaExcel extends JDialog {

    public static final int PESTANA_ALUMNOS = 0;
    public static final int PESTANA_MASIVO = 1;

    public DialogoAyudaExcel(JFrame parent, int pestanaInicial) {
        super(parent, "Cómo preparar tu archivo de Excel", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Alumnos de un aula", crearPestana(htmlAlumnos()));
        pestanas.addTab("Aulas y alumnos (carga masiva)", crearPestana(htmlMasivo()));
        pestanas.setSelectedIndex(pestanaInicial == PESTANA_MASIVO ? PESTANA_MASIVO : PESTANA_ALUMNOS);

        JButton btnCerrar = new JButton("Entendido");
        btnCerrar.setBackground(Tema.PRIMARIO);
        btnCerrar.setForeground(Tema.CONTRASTE);
        btnCerrar.setFocusPainted(false);
        btnCerrar.setMargin(new Insets(8, 24, 8, 24));
        btnCerrar.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnCerrar.addActionListener(e -> dispose());

        // La plantilla que se genera corresponde a la pestaña que se está viendo.
        JButton btnPlantilla = new JButton();
        btnPlantilla.setBackground(Tema.SUPERFICIE);
        btnPlantilla.setForeground(Tema.PRIMARIO);
        btnPlantilla.setFocusPainted(false);
        btnPlantilla.setMargin(new Insets(8, 16, 8, 16));
        btnPlantilla.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Runnable actualizarTextoPlantilla = () -> btnPlantilla.setText(
                pestanas.getSelectedIndex() == PESTANA_MASIVO
                        ? "Descargar plantilla (aulas y alumnos)"
                        : "Descargar plantilla (alumnos)");
        actualizarTextoPlantilla.run();
        pestanas.addChangeListener(e -> actualizarTextoPlantilla.run());
        btnPlantilla.addActionListener(e ->
                descargarPlantilla(pestanas.getSelectedIndex() == PESTANA_MASIVO));

        JPanel pnlSur = new JPanel(new BorderLayout());
        pnlSur.setBackground(Tema.SUPERFICIE);
        pnlSur.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        pnlSur.add(btnPlantilla, BorderLayout.WEST);
        pnlSur.add(btnCerrar, BorderLayout.EAST);

        getContentPane().add(pestanas, BorderLayout.CENTER);
        getContentPane().add(pnlSur, BorderLayout.SOUTH);

        // Enter y Esc cierran la ventana.
        getRootPane().setDefaultButton(btnCerrar);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

        setSize(800, 680);
        setMinimumSize(new Dimension(560, 420));
        setLocationRelativeTo(parent);
    }

    /** Pide dónde guardar y genera la plantilla de Excel del tipo indicado. */
    private void descargarPlantilla(boolean masiva) {
        JFileChooser selector = new JFileChooser();
        selector.setDialogTitle("Guardar plantilla de Excel");
        selector.setFileFilter(new FileNameExtensionFilter("Archivos Excel (.xlsx)", "xlsx"));
        selector.setSelectedFile(new File(masiva
                ? "plantilla_aulas_y_alumnos.xlsx"
                : "plantilla_alumnos.xlsx"));

        if (selector.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File elegido = selector.getSelectedFile();
        if (!elegido.getName().toLowerCase().endsWith(".xlsx")) {
            elegido = new File(elegido.getParentFile(), elegido.getName() + ".xlsx");
        }
        final File archivo = elegido;

        if (archivo.exists()) {
            int confirmar = JOptionPane.showConfirmDialog(this,
                    "Ya existe un archivo con ese nombre.\n¿Quieres reemplazarlo?",
                    "Reemplazar archivo", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            if (confirmar != JOptionPane.YES_OPTION) return;
        }

        try {
            if (masiva) {
                GeneradorPlantillaExcel.generarPlantillaMasiva(archivo);
            } else {
                GeneradorPlantillaExcel.generarPlantillaAlumnos(archivo);
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo guardar la plantilla:\n" + ex.getMessage()
                            + "\n\nSi el archivo está abierto en Excel, ciérralo e inténtalo de nuevo.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int abrir = JOptionPane.showConfirmDialog(this,
                "Plantilla guardada en:\n" + archivo.getAbsolutePath() + "\n\n¿Quieres abrirla ahora?",
                "Plantilla creada", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE);
        if (abrir == JOptionPane.YES_OPTION && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(archivo);
            } catch (IOException | RuntimeException ex) {
                JOptionPane.showMessageDialog(this,
                        "No se pudo abrir el archivo automáticamente. Búscalo en:\n" + archivo.getAbsolutePath(),
                        "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    private JScrollPane crearPestana(String html) {
        JScrollPane scroll = new JScrollPane(crearEditor(html));
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }

    /** Panel HTML de solo lectura; el texto se ajusta solo al ancho de la ventana. */
    static JEditorPane crearEditor(String html) {
        JEditorPane panel = new JEditorPane();

        // Hoja de estilos propia para no modificar la que comparte toda la aplicación.
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet css = new StyleSheet();
        css.addStyleSheet(kit.getStyleSheet());
        kit.setStyleSheet(css);

        Font base = UIManager.getFont("Label.font");
        String familia = base != null ? base.getFamily() : "SansSerif";
        css.addRule("body { font-family: '" + familia + "'; font-size: 13pt; color: "
                + Tema.hex(Tema.TEXTO) + "; margin: 6px 14px; }");
        css.addRule("td { font-family: '" + familia + "'; font-size: 12pt; color: "
                + Tema.hex(Tema.TEXTO) + "; }");
        css.addRule("h2 { font-size: 18pt; color: " + Tema.hex(Tema.PRIMARIO)
                + "; margin-top: 8px; margin-bottom: 2px; }");
        css.addRule("h3 { font-size: 14pt; color: " + Tema.hex(Tema.PRIMARIO_HOVER)
                + "; margin-top: 14px; margin-bottom: 4px; }");
        css.addRule("p { margin-top: 4px; margin-bottom: 4px; }");
        css.addRule("li { margin-bottom: 5px; }");

        panel.setEditorKit(kit);
        panel.setEditable(false);
        panel.setBackground(Tema.FONDO);
        panel.setText(html);
        panel.setCaretPosition(0);   // que el scroll empiece arriba y no al final
        return panel;
    }

    // =====================================================================
    //  Contenido
    // =====================================================================

    static String htmlAlumnos() {
        StringBuilder h = new StringBuilder("<html><body>");

        h.append("<h2>Importar alumnos a un aula</h2>");
        h.append("<p>Agrega alumnos al <b>aula que tienes abierta</b> en este momento "
                + "(la que aparece en el título de la pantalla). Úsalo cuando el aula ya existe.</p>");
        h.append("<p>Para empezar rápido, usa el botón <b>Descargar plantilla</b> (abajo a la izquierda): genera un Excel con las columnas ya listas.</p>");

        h.append("<h3>Paso a paso</h3><ol>");
        h.append("<li>En Excel, escribe en la <b>fila 1</b> los encabezados: <i>Nombre, Apellido, Edad</i>.</li>");
        h.append("<li>Desde la <b>fila 2</b>, escribe un alumno por fila.</li>");
        h.append("<li>Guarda el archivo como <b>Libro de Excel (.xlsx)</b>.</li>");
        h.append("<li>Entra al aula, pulsa <b>Importar Excel</b> y elige tu archivo.</li>");
        h.append("</ol>");

        h.append("<h3>Así debe verse</h3>");
        h.append(tabla(
                new String[]{"Nombre", "Apellido", "Edad"},
                new String[][]{
                        {"Ana", "López", "16"},
                        {"Luis", "Pérez", "17"},
                        {"Marta", "Ruiz", "16"}
                }));

        h.append("<h3>Reglas</h3><ul>");
        h.append("<li><b>El orden de las columnas importa:</b> A = Nombre, B = Apellido, C = Edad. "
                + "El texto de los encabezados es libre. Si hay columnas extra a la derecha, se ignoran.</li>");
        h.append("<li><b>Nombre y apellido son obligatorios.</b> Si falta alguno, esa fila se omite.</li>");
        h.append(reglasComunes());
        h.append("</ul>");

        h.append(aviso(Tema.hex(Tema.PELIGRO_FONDO), Tema.hex(Tema.PELIGRO), "Ojo con los duplicados",
                "El sistema no revisa si un alumno ya existe. Si importas dos veces el mismo archivo, "
                        + "los alumnos quedarán repetidos."));

        h.append(resumenFinal());
        h.append("</body></html>");
        return h.toString();
    }

    static String htmlMasivo() {
        StringBuilder h = new StringBuilder("<html><body>");

        h.append("<h2>Importar aulas y alumnos (carga masiva)</h2>");
        h.append("<p>Crea las aulas <b>y</b> sus alumnos en un solo paso, desde la pantalla principal. "
                + "Es ideal para arrancar con todo el plantel. <b>Cada fila es un alumno</b>, y los datos "
                + "de su aula se repiten en cada fila.</p>");
        h.append("<p>Para empezar rápido, usa el botón <b>Descargar plantilla</b> (abajo a la izquierda): genera un Excel con las columnas ya listas.</p>");

        h.append("<h3>Paso a paso</h3><ol>");
        h.append("<li>En la <b>fila 1</b> escribe los encabezados: <i>Especialidad, Grado, Grupo, Turno, "
                + "Nombre, Apellido, Edad</i>.</li>");
        h.append("<li>Desde la <b>fila 2</b>, un alumno por fila, repitiendo los datos de su aula.</li>");
        h.append("<li>Guarda el archivo como <b>Libro de Excel (.xlsx)</b>.</li>");
        h.append("<li>En la pantalla principal pulsa <b>Importar Excel</b> y elige tu archivo.</li>");
        h.append("</ol>");

        h.append("<h3>Así debe verse</h3>");
        h.append(tabla(
                new String[]{"Especialidad", "Grado", "Grupo", "Turno", "Nombre", "Apellido", "Edad"},
                new String[][]{
                        {"Programación", "3", "A", "Matutino", "Ana", "López", "16"},
                        {"Programación", "3", "A", "Matutino", "Luis", "Pérez", "17"},
                        {"Programación", "3", "B", "Vespertino", "Marta", "Ruiz", "16"},
                        {"Contabilidad", "1", "A", "Matutino", "Diego", "Soto", "15"}
                }));
        h.append("<p>Con este ejemplo se crean <b>3 aulas</b> y <b>4 alumnos</b>.</p>");

        h.append("<h3>Cómo se forman las aulas</h3>");
        h.append("<p>Cada combinación distinta de <b>Especialidad + Grado + Grupo + Turno</b> es un aula. "
                + "La primera vez que aparece se crea; las filas siguientes con la misma combinación "
                + "agregan alumnos a esa misma aula. El orden de las filas no importa.</p>");

        h.append("<h3>Reglas</h3><ul>");
        h.append("<li><b>El orden de las columnas importa:</b> A = Especialidad, B = Grado, C = Grupo, "
                + "D = Turno, E = Nombre, F = Apellido, G = Edad. El texto de los encabezados es libre.</li>");
        h.append("<li><b>Obligatorios:</b> Especialidad, Grupo, Turno, Nombre y Apellido. "
                + "Si falta alguno, esa fila se omite.</li>");
        h.append("<li><b>Grado:</b> escribe solo el número (3). Algo como \"3°\" o \"tercero\" provoca error. "
                + "Si lo dejas vacío no se avisa y el aula se crea con grado 0.</li>");
        h.append("<li><b>Escribe igual los datos de cada aula:</b> las mayúsculas y los acentos cuentan. "
                + "\"Programación\" y \"Programacion\" se toman como aulas distintas. "
                + "Los espacios al inicio o al final sí se ignoran.</li>");
        h.append(reglasComunes());
        h.append("</ul>");

        h.append(aviso(Tema.hex(Tema.PELIGRO_FONDO), Tema.hex(Tema.PELIGRO), "Ojo con los duplicados",
                "Esta carga no revisa si el aula ya existe en el sistema, así que puede crear aulas repetidas "
                        + "(y alumnos repetidos si importas el mismo archivo otra vez). "
                        + "Para agregar alumnos a un aula que ya existe, abre esa aula y usa su botón "
                        + "<b>Importar Excel</b>."));

        h.append(resumenFinal());
        h.append("</body></html>");
        return h.toString();
    }

    /** Reglas que aplican por igual a los dos formatos. */
    private static String reglasComunes() {
        return "<li><b>Edad:</b> escribe un número entero (por ejemplo 16). Texto como \"16 años\" o "
                + "\"dieciséis\" provoca error en esa fila, y los decimales se recortan (15.9 se guarda como 15). "
                + "Escribe siempre una edad: si la dejas vacía, la fila puede marcarse como \"faltan columnas\".</li>"
                + "<li><b>Solo .xlsx.</b> No se aceptan .xls ni .csv. Si trabajas en Google Sheets, "
                + "descárgalo como Microsoft Excel (.xlsx).</li>"
                + "<li><b>Solo se lee la primera hoja</b> del libro.</li>"
                + "<li><b>La fila 1 siempre se ignora</b>, tenga encabezados o no. "
                + "Si pones un dato ahí, no se importará.</li>"
                + "<li><b>Fórmulas:</b> una celda con fórmula se lee como vacía. "
                + "Cópiala y usa Pegado especial → Valores antes de guardar.</li>"
                + "<li><b>Sin filas vacías</b> entre los datos, para que el número de fila que aparece en "
                + "los avisos coincida con el de Excel.</li>";
    }

    private static String resumenFinal() {
        return "<p>Al terminar verás un resumen con lo que se insertó y la lista de filas con problemas. "
                + "Las filas correctas se guardan aunque otras tengan errores.</p>";
    }

    // =====================================================================
    //  Piezas HTML
    // =====================================================================

    /** Tabla que imita una hoja de Excel (con letras de columna y números de fila). */
    private static String tabla(String[] encabezados, String[][] filas) {
        final String gris = Tema.hex(Tema.HOVER);
        final String borde = Tema.hex(Tema.BORDE_CONTROL);
        final String textoSuave = Tema.hex(Tema.TEXTO_SUAVE);
        final String primario = Tema.hex(Tema.PRIMARIO);
        final String contraste = Tema.hex(Tema.CONTRASTE);
        final String superficie = Tema.hex(Tema.SUPERFICIE);
        StringBuilder t = new StringBuilder();
        t.append("<table border='1' cellspacing='0' cellpadding='6' bordercolor='").append(borde).append("'>");

        // Letras de columna
        t.append("<tr><td bgcolor='").append(gris).append("'>&nbsp;</td>");
        for (int i = 0; i < encabezados.length; i++) {
            t.append("<td bgcolor='").append(gris).append("' align='center'><font color='").append(textoSuave).append("'>")
                    .append((char) ('A' + i)).append("</font></td>");
        }
        t.append("</tr>");

        // Fila 1: encabezados
        t.append("<tr><td bgcolor='").append(gris).append("' align='center'><font color='").append(textoSuave).append("'>1</font></td>");
        for (String enc : encabezados) {
            t.append("<td bgcolor='").append(primario).append("'><font color='").append(contraste)
                    .append("'><b>").append(enc).append("</b></font></td>");
        }
        t.append("</tr>");

        // Datos
        for (int f = 0; f < filas.length; f++) {
            t.append("<tr><td bgcolor='").append(gris).append("' align='center'><font color='").append(textoSuave).append("'>")
                    .append(f + 2).append("</font></td>");
            for (String celda : filas[f]) {
                t.append("<td bgcolor='").append(superficie).append("'>").append(celda).append("</td>");
            }
            t.append("</tr>");
        }
        t.append("</table>");
        return t.toString();
    }

    /** Recuadro de color para advertencias. */
    private static String aviso(String fondo, String borde, String titulo, String texto) {
        return "<br><table width='100%' border='1' cellspacing='0' cellpadding='10' bordercolor='" + borde + "'>"
                + "<tr><td bgcolor='" + fondo + "'><b>" + titulo + "</b><br>" + texto + "</td></tr></table>";
    }
}