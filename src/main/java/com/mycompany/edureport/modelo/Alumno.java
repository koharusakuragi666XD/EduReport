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
 * Datos de un alumno y referencia al aula a la que pertenece.
 *
 * <p>El modelo no contiene reglas de persistencia; los cambios se realizan
 * mediante {@code AlumnoDAO}.</p>
 */
public class Alumno {
    private String id;
    private String nombre;
    private String apellido;
    private int edad;
    private String aulaId;

    public Alumno(String id, String nombre, String apellido, int edad, String aulaId) {
        this.id = id;
        this.nombre = nombre;
        this.apellido = apellido;
        this.edad = edad;
        this.aulaId = aulaId;
    }

    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getApellido() { return apellido; }
    public int getEdad() { return edad; }
    public String getAulaId() { return aulaId; }

    // Esta cadena es la que aparece en la lista de alumnos de la ventana principal.
    @Override
    public String toString() {
        return apellido + ", " + nombre;
    }
}
