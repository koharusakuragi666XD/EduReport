package com.mycompany.edureport.interfaz;
import com.mycompany.edureport.*;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;

/**
 * Iconos dibujados con Java2D (sin archivos ni dependencias). Toman el color del
 * texto del componente, así que se ven bien en botones blancos, oscuros o deshabilitados.
 */
final class Iconos {

    private Iconos() { }

    private enum Tipo { MAS, PUNTOS, ATRAS, AYUDA, SOL, LUNA }

    static Icon mas()    { return new Vectorial(Tipo.MAS); }
    static Icon puntos() { return new Vectorial(Tipo.PUNTOS); }
    static Icon atras()  { return new Vectorial(Tipo.ATRAS); }
    static Icon ayuda()  { return new Vectorial(Tipo.AYUDA); }
    static Icon sol()    { return new Vectorial(Tipo.SOL); }
    static Icon luna()   { return new Vectorial(Tipo.LUNA); }

    private static final class Vectorial implements Icon {
        private static final int TAM = 18;
        private final Tipo tipo;

        Vectorial(Tipo tipo) {
            this.tipo = tipo;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            g2.translate(x, y);
            boolean activo = c == null || c.isEnabled();
            g2.setColor(!activo ? Tema.DESHABILITADO : (c != null ? c.getForeground() : Tema.TEXTO_SUAVE));
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            switch (tipo) {
                case MAS:
                    g2.drawLine(9, 3, 9, 15);
                    g2.drawLine(3, 9, 15, 9);
                    break;
                case PUNTOS:
                    for (int cy : new int[]{3, 9, 15}) {
                        g2.fill(new Ellipse2D.Double(9 - 1.7, cy - 1.7, 3.4, 3.4));
                    }
                    break;
                case ATRAS:
                    g2.drawLine(15, 9, 3, 9);
                    g2.drawLine(3, 9, 8, 4);
                    g2.drawLine(3, 9, 8, 14);
                    break;
                case AYUDA:
                    g2.drawOval(2, 2, 14, 14);
                    g2.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 12));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.drawString("?", 9 - fm.stringWidth("?") / 2,
                            9 + (fm.getAscent() - fm.getDescent()) / 2);
                    break;
                case SOL:
                    g2.fillOval(5, 5, 8, 8);
                    for (int i = 0; i < 8; i++) {
                        double a = i * Math.PI / 4;
                        g2.drawLine(9 + (int) (6 * Math.cos(a)), 9 + (int) (6 * Math.sin(a)),
                                9 + (int) (8 * Math.cos(a)), 9 + (int) (8 * Math.sin(a)));
                    }
                    break;
                case LUNA:
                    g2.fillArc(3, 2, 13, 14, 50, 260);
                    g2.setColor(Tema.FONDO);
                    g2.fillOval(7, 2, 11, 11);
                    break;
                default:
                    break;
            }
            g2.dispose();
        }

        @Override public int getIconWidth()  { return TAM; }
        @Override public int getIconHeight() { return TAM; }
    }
}
