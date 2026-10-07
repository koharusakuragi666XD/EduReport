/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.edureport.modelo;
/**
 *
 * @author sergio
 */

import java.util.Date;

/**
 * Incidencia registrada para un alumno.
 *
 * <p>Los valores de tipo y severidad se mantienen como texto porque actualmente
 * provienen de listas fijas en el diálogo de captura.</p>
 */
public class Reporte {
    private String id;
    private String alumnoId;
    private String tipo;
    private String severidad;
    private String motivo;
    private Date fecha;

    public Reporte(String id, String alumnoId, String tipo, String severidad, String motivo, Date fecha) {
        this.id = id;
        this.alumnoId = alumnoId;
        this.tipo = tipo;
        this.severidad = severidad;
        this.motivo = motivo;
        this.fecha = fecha;
    }

    public String getId() { return id; }
    public String getAlumnoId() { return alumnoId; }
    public String getTipo() { return tipo; }
    public String getSeveridad() { return severidad; }
    public String getMotivo() { return motivo; }
    public Date getFecha() { return fecha; }

    // Útil para depuración; la interfaz normalmente muestra cada campo por separado.
    @Override
    public String toString() {
        return tipo + " - " + severidad + " (" + fecha + ")";
    }
}
