package com.mycompany.edureport;

import java.awt.Color;

/**
 * Niveles de severidad de un reporte y su escala de color: rojo → ámbar → amarillo.
 * Se evita el verde a propósito: un reporte "leve" no debe parecer algo positivo.
 */
public enum Severidad {

    GRAVE("Grave",       new Color(0xFDECEA), new Color(0xB42318), new Color(0xB42318)),
    MODERADO("Moderado", new Color(0xFDE3C3), new Color(0x8F4400), new Color(0xF08A00)),
    LEVE("Leve",         new Color(0xFEF5C2), new Color(0x665000), new Color(0xEAB800));

    private final String etiqueta;
    private final Color fondo;
    private final Color texto;
    private final Color punto;

    Severidad(String etiqueta, Color fondo, Color texto, Color punto) {
        this.etiqueta = etiqueta;
        this.fondo = fondo;
        this.texto = texto;
        this.punto = punto;
    }

    public String etiqueta() { return etiqueta; }
    /** Fondo suave de la píldora. */
    public Color fondo()     { return fondo; }
    /** Color del texto, oscuro para que cumpla contraste sobre el fondo. */
    public Color texto()     { return texto; }
    /** Color vivo del punto indicador. */
    public Color punto()     { return punto; }

    /** Convierte el texto guardado en la base ("Grave", "Moderado"...). Lo desconocido cuenta como leve. */
    public static Severidad desde(String valor) {
        if (valor != null) {
            String v = valor.trim();
            for (Severidad s : values()) {
                if (s.etiqueta.equalsIgnoreCase(v)) return s;
            }
        }
        return LEVE;
    }
}
