/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.edureport.modelo;
/**
 *
 * @author sergio
 */

/**
 * Modelo de una clase o grupo escolar.
 *
 * <p>Esta clase solo contiene datos y no conoce MongoDB ni Swing. Esa separación
 * facilita reemplazar la interfaz o la base de datos durante una migración.</p>
 */
public class Aula {
    private String id;
    private String especialidad;
    private int grado;
    private String grupo;
    private String turno;

    public Aula(String id, String especialidad, int grado, String grupo, String turno) {
        this.id = id;
        this.especialidad = especialidad;
        this.grado = grado;
        this.grupo = grupo;
        this.turno = turno;
    }

    // El id es el ObjectId de MongoDB convertido a texto para transportarlo entre capas.
    public String getId() { return id; }
    public String getEspecialidad() { return especialidad; }
    public int getGrado() { return grado; }
    public String getGrupo() { return grupo; }
    public String getTurno() { return turno; }

    // JList y los botones de la interfaz usan esta representación para mostrar el aula.
    @Override
    public String toString() {
        return especialidad + " - " + grado + "° " + grupo + " (" + turno + ")";
    }
}