package com.mycompany.edureport.interfaz;

import com.mycompany.edureport.*;

import javax.swing.*;
import java.awt.*;
import java.text.Normalizer;
import java.util.concurrent.ExecutionException;
import java.util.prefs.Preferences;
import java.util.prefs.BackingStoreException;

/**
 * Paleta, medidas y fábrica de componentes con el estilo de la app.
 *
 * <p>
 * Cambia los colores aquí y se actualiza toda la interfaz. Los estilos de
 * botones usan la
 * propiedad {@code FlatLaf.style} (requiere FlatLaf 2.0 o superior); en
 * versiones anteriores
 * se ven con los colores base que también se asignan.
 * </p>
 */
public final class Tema {

    private Tema() {
    }

    public enum Modo {
        LATTE, MOCHA
    }

    private static Modo modo = Modo.LATTE;
    private static final Preferences preferencias = Preferences.userNodeForPackage(Tema.class);
    private static final String CLAVE_MODO = "modo";

    public static Color FONDO;
    public static Color SUPERFICIE;
    public static Color BORDE;
    public static Color BORDE_CONTROL;
    public static Color PRIMARIO;
    public static Color PRIMARIO_HOVER;
    public static Color TEXTO;
    public static Color TEXTO_SUAVE;
    public static Color HOVER;
    public static Color HOVER_TARJETA;
    public static Color SELECCION;
    public static Color PELIGRO;
    public static Color PELIGRO_FONDO;
    public static Color DESHABILITADO;
    public static Color CONTRASTE;

    static {
        modo = cargarModo();
        aplicarPaleta();
    }

    private static final String STYLE = "FlatLaf.style";

    public static String hex(Color c) {
        return String.format("#%06X", c.getRGB() & 0xFFFFFF);
    }

    public static boolean esOscuro() {
        return modo == Modo.MOCHA;
    }

    private static Modo cargarModo() {
        String valor = preferencias.get(CLAVE_MODO, Modo.LATTE.name());

        if (valor.equals(Modo.MOCHA.name())) {
            return Modo.MOCHA;
        }

        return Modo.LATTE;
    }

    public static String nombreModo() {
        return esOscuro() ? "Modo día" : "Modo noche";
    }

    public static void alternar() {
        modo = esOscuro() ? Modo.LATTE : Modo.MOCHA;
        preferencias.put(CLAVE_MODO, modo.name());

        try {
            preferencias.flush();
        } catch (BackingStoreException e) {
            System.err.println("No se pudo guardar el modo de la aplicación.");
        }

        aplicarPaleta();
    }

    private static void aplicarPaleta() {
        boolean oscuro = modo == Modo.MOCHA;
        FONDO = color(oscuro ? 0x1E1E2E : 0xEFF1F5);
        SUPERFICIE = color(oscuro ? 0x181825 : 0xE6E9EF);
        BORDE = color(oscuro ? 0x313244 : 0xCCD0DA);
        BORDE_CONTROL = color(oscuro ? 0x45475A : 0xBCC0CC);
        PRIMARIO = color(oscuro ? 0x89B4FA : 0x1E66F5);
        PRIMARIO_HOVER = color(oscuro ? 0x74C7EC : 0x04A5E5);
        TEXTO = color(oscuro ? 0xCDD6F4 : 0x4C4F69);
        TEXTO_SUAVE = color(oscuro ? 0xA6ADC8 : 0x6C6F85);
        HOVER = color(oscuro ? 0x313244 : 0xDCE0E8);
        HOVER_TARJETA = color(oscuro ? 0x313244 : 0xE6E9EF);
        SELECCION = color(oscuro ? 0x45475A : 0xCCD0DA);
        PELIGRO = color(oscuro ? 0xF38BA8 : 0xD20F39);
        PELIGRO_FONDO = mezclar(PELIGRO, FONDO, 0.18f);
        DESHABILITADO = color(oscuro ? 0x585B70 : 0xBCC0CC);
        CONTRASTE = color(oscuro ? 0x1E1E2E : 0xEFF1F5);
    }

    private static Color color(int rgb) {
        return new Color(rgb);
    }

    private static Color mezclar(Color frente, Color fondo, float proporcion) {
        int r = (int) (frente.getRed() * proporcion + fondo.getRed() * (1 - proporcion));
        int g = (int) (frente.getGreen() * proporcion + fondo.getGreen() * (1 - proporcion));
        int b = (int) (frente.getBlue() * proporcion + fondo.getBlue() * (1 - proporcion));
        return new Color(r, g, b);
    }

    // =====================================================================
    // Utilidades de texto
    // =====================================================================

    /** Cambia estilo/tamaño conservando la fuente que define el Look and Feel. */
    public static void fuente(JComponent c, int estilo, float tamano) {
        c.setFont(c.getFont().deriveFont(estilo, tamano));
    }

    /** Escapa texto del usuario para mostrarlo dentro de HTML de Swing. */
    public static String esc(Object o) {
        if (o == null)
            return "";
        return String.valueOf(o)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\n", "<br>");
    }

    /**
     * Minúsculas y sin acentos, para búsquedas tolerantes ("perez" encuentra
     * "Pérez").
     */
    public static String normalizar(String s) {
        String n = Normalizer.normalize(s == null ? "" : s, Normalizer.Form.NFD);
        return n.replaceAll("\\p{M}", "").toLowerCase();
    }

    public static String plural(int n, String singular, String plural) {
        return n + " " + (n == 1 ? singular : plural);
    }

    /** Mensaje legible de una excepción (desenvuelve las de SwingWorker). */
    public static String causa(Throwable t) {
        Throwable c = t;
        while (c instanceof ExecutionException && c.getCause() != null)
            c = c.getCause();
        String m = c.getMessage();
        return m != null ? m : c.getClass().getSimpleName();
    }

    // =====================================================================
    // Diálogos
    // =====================================================================

    /**
     * Confirmación destructiva: botones "Eliminar" / "Cancelar" y Cancelar por
     * defecto.
     */
    public static boolean confirmarEliminacion(Component padre, String titulo, String mensaje) {
        Object[] opciones = { "Eliminar", "Cancelar" };
        int r = JOptionPane.showOptionDialog(padre, mensaje, titulo,
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, opciones, opciones[1]);
        return r == 0;
    }

    public static void mostrarError(Component padre, String mensaje) {
        JOptionPane.showMessageDialog(padre, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }

    // =====================================================================
    // Paneles de apoyo
    // =====================================================================

    /** Franja blanca superior con línea inferior. */
    public static JPanel encabezado(int vertical, int horizontal) {
        JPanel p = new JPanel(new BorderLayout(16, 0));
        p.setBackground(SUPERFICIE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, BORDE),
                BorderFactory.createEmptyBorder(vertical, horizontal, vertical, horizontal)));
        return p;
    }

    /**
     * Componentes en horizontal, separados por {@code hueco} px y centrados en
     * vertical.
     */
    public static JPanel fila(int hueco, JComponent... hijos) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        for (int i = 0; i < hijos.length; i++) {
            if (i > 0)
                p.add(Box.createHorizontalStrut(hueco));
            hijos[i].setAlignmentY(Component.CENTER_ALIGNMENT);
            p.add(hijos[i]);
        }
        return p;
    }

    /** Centra un componente en vertical dentro de su contenedor sin estirarlo. */
    public static JPanel centrado(JComponent c) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.add(c);
        return p;
    }

    // =====================================================================
    // Botones
    // =====================================================================

    private static JButton base(String texto) {
        JButton b = new JButton(texto);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(11, 16, 11, 16));
        b.setIconTextGap(6);
        fuente(b, Font.BOLD, 13f);
        return b;
    }

    /** Acción principal de la pantalla: una sola por vista. */
    public static JButton botonPrimario(String texto) {
        JButton b = base(texto);
        b.setBackground(PRIMARIO);
        b.setForeground(CONTRASTE);
        b.putClientProperty(STYLE, "background:" + hex(PRIMARIO)
                + "; foreground:" + hex(CONTRASTE)
                + "; hoverBackground:" + hex(PRIMARIO_HOVER)
                + "; pressedBackground:" + hex(PRIMARIO_HOVER.darker())
                + "; disabledBackground:" + hex(DESHABILITADO)
                + "; disabledText:" + hex(TEXTO_SUAVE) + "; borderWidth:0; arc:12");
        return b;
    }

    /** Acciones secundarias: contorno gris y fondo blanco. */
    public static JButton botonSecundario(String texto) {
        JButton b = base(texto);
        b.setBackground(SUPERFICIE);
        b.setForeground(PRIMARIO);
        b.putClientProperty(STYLE, "background:" + hex(SUPERFICIE) + "; foreground:" + hex(PRIMARIO)
                + "; borderColor:" + hex(BORDE_CONTROL)
                + "; hoverBackground:" + hex(HOVER)
                + "; pressedBackground:" + hex(SELECCION) + "; borderWidth:1; arc:12");
        return b;
    }

    /**
     * Acción destructiva: texto rojo sin relleno; solo se tiñe al pasar el cursor.
     */
    public static JButton botonPeligro(String texto) {
        JButton b = base(texto);
        b.setBackground(FONDO);
        b.setForeground(PELIGRO);
        b.putClientProperty(STYLE, "background:" + hex(FONDO) + "; foreground:" + hex(PELIGRO)
                + "; hoverBackground:" + hex(PELIGRO_FONDO)
                + "; pressedBackground:" + hex(PELIGRO_FONDO.darker()) + "; borderWidth:0; arc:12");
        return b;
    }

    /** Botón cuadrado con solo un icono y contorno (p. ej. "+"). */
    public static JButton botonSecundarioIcono(Icon icono, String descripcion) {
        JButton b = botonSecundario(null);
        b.setIcon(icono);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setPreferredSize(new Dimension(44, 44));
        b.setToolTipText(descripcion);
        b.getAccessibleContext().setAccessibleName(descripcion);
        return b;
    }

    /**
     * Botón de icono sin contorno (menús "⋮", ayuda). Solo se resalta al pasar el
     * cursor.
     */
    public static JButton botonIcono(Icon icono, String descripcion, int lado) {
        JButton b = new JButton(icono);
        b.putClientProperty("JButton.buttonType", "toolBarButton");
        b.setForeground(TEXTO_SUAVE);
        b.setFocusPainted(false);
        b.setMargin(new Insets(0, 0, 0, 0));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(lado, lado));
        b.setToolTipText(descripcion);
        b.getAccessibleContext().setAccessibleName(descripcion);
        return b;
    }

    /**
     * Filtro tipo "chip": píldora que se rellena al seleccionarse. Agrúpalos con
     * ButtonGroup.
     */
    public static JToggleButton chip(String texto) {
        JToggleButton b = new JToggleButton(texto);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(7, 14, 7, 14));
        b.setBackground(SUPERFICIE);
        b.setForeground(PRIMARIO);
        fuente(b, Font.BOLD, 13f);
        b.putClientProperty(STYLE, "arc:999; background:" + hex(SUPERFICIE) + "; foreground:" + hex(PRIMARIO)
                + "; borderColor:" + hex(BORDE_CONTROL)
                + "; hoverBackground:" + hex(HOVER)
                + "; selectedBackground:" + hex(PRIMARIO)
                + "; selectedForeground:" + hex(CONTRASTE) + "; borderWidth:1");
        return b;
    }

    public static JTextField campoBusqueda(String placeholder) {
        JTextField t = new JTextField();
        t.putClientProperty("JTextField.placeholderText", placeholder);
        t.putClientProperty("JTextField.showClearButton", true);
        t.setMargin(new Insets(10, 12, 10, 12));
        t.setPreferredSize(new Dimension(120, 44));
        t.getAccessibleContext().setAccessibleName(placeholder);
        return t;
    }
}
