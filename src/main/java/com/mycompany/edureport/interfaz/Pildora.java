package com.mycompany.edureport.interfaz;
import com.mycompany.edureport.*;

import javax.swing.*;
import java.awt.*;

/** Etiqueta con fondo en forma de píldora y, opcionalmente, un punto de color a la izquierda. */
public class Pildora extends JLabel {

    private static final int PUNTO = 8;

    private final Color fondo;
    private final Color punto;
    private final int anchoMinimo;

    public Pildora(String texto, Color fondo, Color colorTexto, Color punto, int anchoMinimo) {
        super(texto, SwingConstants.CENTER);
        this.fondo = fondo;
        this.punto = punto;
        this.anchoMinimo = anchoMinimo;
        setForeground(colorTexto);
        Tema.fuente(this, Font.BOLD, 12f);
        setOpaque(false);
        int izquierda = punto == null ? 10 : 10 + PUNTO + 6;
        setBorder(BorderFactory.createEmptyBorder(4, izquierda, 4, 10));
    }

    /** Píldora de severidad: "● Grave", "● Moderado", "● Leve". */
    public static Pildora severidad(Severidad s) {
        return new Pildora(s.etiqueta(), s.fondo(), s.texto(), s.punto(), 0);
    }

    /** Número de reportes de un alumno; en rojo si tiene alguno grave. */
    public static Pildora contador(int n, boolean conGraves) {
        return new Pildora(String.valueOf(n),
                conGraves ? Tema.PELIGRO_FONDO : Tema.HOVER,
                conGraves ? Tema.PELIGRO : Tema.TEXTO_SUAVE, null, 26);
    }

    /** Resumen de reportes graves de un aula. */
    public static Pildora graves(int n) {
        if (n > 0) {
            return new Pildora(Tema.plural(n, "grave", "graves"),
                    Tema.PELIGRO_FONDO, Tema.PELIGRO, null, 0);
        }
        return new Pildora("Sin graves", Tema.HOVER, Tema.TEXTO_SUAVE, null, 0);
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension d = super.getPreferredSize();
        d.width = Math.max(d.width, anchoMinimo);
        return d;
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(fondo);
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
        if (punto != null) {
            g2.setColor(punto);
            g2.fillOval(10, (getHeight() - PUNTO) / 2, PUNTO, PUNTO);
        }
        g2.dispose();
        super.paintComponent(g);
    }
}
